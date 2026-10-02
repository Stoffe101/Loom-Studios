package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.importing.PngImportAdapter;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.palette.ColorPaletteLibrary;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomImagePreviewWidget;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.image.ImagePlacementMode;
import dev.loomstudios.image.ImageProcessingMode;
import dev.loomstudios.image.ImageProcessingPipeline;
import dev.loomstudios.image.ImageProcessingSettings;
import dev.loomstudios.image.PixelImage;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.ElytraWing;
import dev.loomstudios.project.ImageLayerData;
import dev.loomstudios.project.LayerTransform;
import dev.loomstudios.project.LoomCanvas;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.NormalizedRect;
import dev.loomstudios.project.ProjectEdits;
import dev.loomstudios.project.LayerRasterizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public final class SmartImportScreen extends Screen {
    private static final UUID PREVIEW_LAYER_ID =
            UUID.fromString("00000000-0000-4000-8000-00000000a11e");
    private static final UUID PREVIEW_LAYER_ID_RIGHT =
            UUID.fromString("00000000-0000-4000-8000-00000000a11f");

    private enum ImportTarget {
        CAPE,
        ELYTRA_LINKED,
        ELYTRA_SINGLE
    }

    private static final int[] COLOR_LIMITS = {
            0, 4, 8, 16, 32, 64, 128, 256
    };

    private final Screen parent;
    private final CapeUvRegion targetRegion;
    private final UUID editingLayerId;
    private final ImportTarget importTarget;

    private PngImportAdapter.LoadedImage loaded;
    private ImageLayerData editingBaseData;
    private String layerName = "Imported Image";
    private ImagePlacementMode placementMode = ImagePlacementMode.FIT;
    private boolean keepAspect = true;
    private ImageProcessingSettings processing =
            ImageProcessingSettings.defaults();

    private double offsetX;
    private double offsetY;
    private double scale = 1.0;
    private double rotationDegrees;
    private boolean mirrorHorizontal;
    private boolean mirrorVertical;

    private long revision;
    private long cacheRevision = Long.MIN_VALUE;
    private PixelImage processedCache;
    private PixelImage textureCache;
    private LoomProject candidateProjectCache;

    private LoomImagePreviewWidget originalPreview;
    private LoomImagePreviewWidget processedPreview;
    private LoomImagePreviewWidget texturePreview;

    private LoomButton placementButton;
    private LoomButton keepAspectButton;
    private LoomButton modeButton;
    private LoomButton ditherButton;
    private LoomButton paletteButton;
    private LoomButton colorLimitButton;
    private LoomButton posterizeButton;
    private LoomButton brightnessButton;
    private LoomButton contrastButton;
    private LoomButton saturationButton;
    private LoomButton scaleButton;
    private LoomButton rotationButton;
    private LoomButton mirrorHorizontalButton;
    private LoomButton mirrorVerticalButton;
    private LoomButton preview3dButton;
    private LoomButton applyButton;

    private String status = "Choose a PNG to begin";
    private int controlPanelX;
    private int controlPanelY;
    private int controlPanelWidth;
    private int controlPanelHeight;

    public SmartImportScreen(Screen parent, CapeUvRegion targetRegion) {
        this(
                parent,
                targetRegion,
                null,
                null,
                ImportTarget.CAPE
        );
    }

    public SmartImportScreen(
            Screen parent,
            CapeUvRegion targetRegion,
            UUID editingLayerId,
            LoomLayer editingLayer
    ) {
        this(
                parent,
                targetRegion,
                editingLayerId,
                editingLayer,
                ImportTarget.CAPE
        );
    }

    public static SmartImportScreen forElytra(Screen parent) {
        return new SmartImportScreen(
                parent,
                null,
                null,
                null,
                ImportTarget.ELYTRA_LINKED
        );
    }

    public static SmartImportScreen forElytraLayer(
            Screen parent,
            UUID editingLayerId,
            LoomLayer editingLayer
    ) {
        return new SmartImportScreen(
                parent,
                null,
                editingLayerId,
                editingLayer,
                ImportTarget.ELYTRA_SINGLE
        );
    }

    private SmartImportScreen(
            Screen parent,
            CapeUvRegion targetRegion,
            UUID editingLayerId,
            LoomLayer editingLayer,
            ImportTarget importTarget
    ) {
        super(Component.literal("Loom Studios - Smart Import"));
        this.parent = parent;
        this.targetRegion = targetRegion == null
                ? CapeUvRegion.OUTSIDE
                : targetRegion;
        this.editingLayerId = editingLayerId;
        this.importTarget = java.util.Objects.requireNonNull(
                importTarget,
                "importTarget"
        );

        if (editingLayer != null) {
            if (editingLayer.kind()
                    != dev.loomstudios.project.LayerKind.IMAGE) {
                throw new IllegalArgumentException(
                        "Smart Import can only edit Image layers"
                );
            }

            ImageLayerData data = editingLayer.imageData();
            this.editingBaseData = data;
            this.processing = data.processing();
            this.rotationDegrees =
                    data.transform().rotationDegrees();
            this.mirrorHorizontal =
                    data.transform().mirrorHorizontal();
            this.mirrorVertical =
                    data.transform().mirrorVertical();
            this.layerName = editingLayer.name();
            this.loaded = new PngImportAdapter.LoadedImage(
                    Path.of(editingLayer.name() + ".png"),
                    data.source(),
                    data.source()
            );
            this.status = "Editing Image layer: "
                    + editingLayer.name();
        } else if (this.importTarget == ImportTarget.ELYTRA_LINKED) {
            this.status = "Choose a PNG for linked Elytra wings";
        }
    }

    @Override
    protected void init() {
        int margin = 12;
        int top = 48;
        int panelWidth = Math.min(
                240,
                Math.max(190, this.width / 3)
        );

        this.controlPanelWidth = panelWidth;
        this.controlPanelX = this.width - margin - panelWidth;
        this.controlPanelY = top;
        this.controlPanelHeight = Math.max(
                120,
                this.height - top - margin
        );

        int workLeft = margin;
        int workTop = top;
        int workRight = controlPanelX - margin;
        int workWidth = Math.max(170, workRight - workLeft);
        int workHeight = Math.max(
                140,
                this.height - workTop - margin
        );

        int gap = 8;
        int topPreviewHeight = Math.max(
                86,
                (workHeight - gap) / 2
        );
        int halfWidth = Math.max(
                80,
                (workWidth - gap) / 2
        );

        originalPreview = addRenderableWidget(
                new LoomImagePreviewWidget(
                        workLeft,
                        workTop,
                        halfWidth,
                        topPreviewHeight,
                        Component.literal("Original"),
                        this::originalImage,
                        this::revision
                )
        );

        processedPreview = addRenderableWidget(
                new LoomImagePreviewWidget(
                        workLeft + halfWidth + gap,
                        workTop,
                        workWidth - halfWidth - gap,
                        topPreviewHeight,
                        Component.literal("Processed"),
                        this::processedImage,
                        this::revision
                )
        );

        texturePreview = addRenderableWidget(
                new LoomImagePreviewWidget(
                        workLeft,
                        workTop + topPreviewHeight + gap,
                        workWidth,
                        Math.max(
                                72,
                                workHeight - topPreviewHeight - gap
                        ),
                        Component.literal("Cape Texture"),
                        this::textureImage,
                        this::revision
                )
        );

        int contentWidth = Math.max(150, panelWidth - 20);
        LinearLayout controls = LinearLayout.vertical().spacing(6);

        controls.addChild(button(
                contentWidth,
                "Choose PNG",
                this::choosePng
        ));

        controls.addChild(readOnlyButton(
                contentWidth,
                "Target: " + targetRegion.displayName()
        ));

        placementButton = button(
                contentWidth,
                "",
                this::cyclePlacement
        );
        controls.addChild(placementButton);

        keepAspectButton = button(
                contentWidth,
                "",
                this::toggleKeepAspect
        );
        controls.addChild(keepAspectButton);

        LinearLayout moveXRow = LinearLayout.horizontal().spacing(4);
        moveXRow.addChild(button(
                (contentWidth - 4) / 2,
                "Move Left",
                () -> move(-1, 0)
        ));
        moveXRow.addChild(button(
                contentWidth - 4 - (contentWidth - 4) / 2,
                "Move Right",
                () -> move(1, 0)
        ));
        controls.addChild(moveXRow);

        LinearLayout moveYRow = LinearLayout.horizontal().spacing(4);
        moveYRow.addChild(button(
                (contentWidth - 4) / 2,
                "Move Up",
                () -> move(0, -1)
        ));
        moveYRow.addChild(button(
                contentWidth - 4 - (contentWidth - 4) / 2,
                "Move Down",
                () -> move(0, 1)
        ));
        controls.addChild(moveYRow);

        LinearLayout scaleRow = LinearLayout.horizontal().spacing(4);
        scaleRow.addChild(button(
                48,
                "Scale -",
                () -> changeScale(-0.1)
        ));
        scaleButton = readOnlyButton(
                Math.max(42, contentWidth - 104),
                ""
        );
        scaleRow.addChild(scaleButton);
        scaleRow.addChild(button(
                48,
                "Scale +",
                () -> changeScale(0.1)
        ));
        controls.addChild(scaleRow);

        LinearLayout rotationRow = LinearLayout.horizontal().spacing(4);
        rotationRow.addChild(button(
                48,
                "Rot -",
                () -> rotate(-15.0)
        ));
        rotationButton = readOnlyButton(
                Math.max(42, contentWidth - 104),
                ""
        );
        rotationRow.addChild(rotationButton);
        rotationRow.addChild(button(
                48,
                "Rot +",
                () -> rotate(15.0)
        ));
        controls.addChild(rotationRow);

        LinearLayout mirrorRow = LinearLayout.horizontal().spacing(4);
        mirrorHorizontalButton = button(
                (contentWidth - 4) / 2,
                "",
                () -> {
                    mirrorHorizontal = !mirrorHorizontal;
                    touch();
                }
        );
        mirrorVerticalButton = button(
                contentWidth - 4 - (contentWidth - 4) / 2,
                "",
                () -> {
                    mirrorVertical = !mirrorVertical;
                    touch();
                }
        );
        mirrorRow.addChild(mirrorHorizontalButton);
        mirrorRow.addChild(mirrorVerticalButton);
        controls.addChild(mirrorRow);

        modeButton = button(
                contentWidth,
                "",
                this::cycleProcessingMode
        );
        controls.addChild(modeButton);

        paletteButton = button(
                contentWidth,
                "",
                this::toggleSelectedPalette
        );
        controls.addChild(paletteButton);

        controls.addChild(adjustmentRow(
                contentWidth,
                "Brightness",
                () -> changeBrightness(-0.1F),
                () -> changeBrightness(0.1F)
        ));
        brightnessButton = lastReadOnly;
        lastReadOnly = null;

        controls.addChild(adjustmentRow(
                contentWidth,
                "Contrast",
                () -> changeContrast(-0.1F),
                () -> changeContrast(0.1F)
        ));
        contrastButton = lastReadOnly;
        lastReadOnly = null;

        controls.addChild(adjustmentRow(
                contentWidth,
                "Saturation",
                () -> changeSaturation(-0.1F),
                () -> changeSaturation(0.1F)
        ));
        saturationButton = lastReadOnly;
        lastReadOnly = null;

        LinearLayout colorRow = LinearLayout.horizontal().spacing(4);
        colorRow.addChild(button(
                48,
                "Colors -",
                () -> changeColorLimit(-1)
        ));
        colorLimitButton = readOnlyButton(
                Math.max(42, contentWidth - 104),
                ""
        );
        colorRow.addChild(colorLimitButton);
        colorRow.addChild(button(
                48,
                "Colors +",
                () -> changeColorLimit(1)
        ));
        controls.addChild(colorRow);

        ditherButton = button(
                contentWidth,
                "",
                () -> {
                    processing = processing.withColorReduction(
                            processing.colorLimit(),
                            !processing.dither()
                    );
                    touch();
                }
        );
        controls.addChild(ditherButton);

        LinearLayout posterizeRow =
                LinearLayout.horizontal().spacing(4);
        posterizeRow.addChild(button(
                48,
                "Levels -",
                () -> changePosterize(-1)
        ));
        posterizeButton = readOnlyButton(
                Math.max(42, contentWidth - 104),
                ""
        );
        posterizeRow.addChild(posterizeButton);
        posterizeRow.addChild(button(
                48,
                "Levels +",
                () -> changePosterize(1)
        ));
        controls.addChild(posterizeRow);

        preview3dButton = button(
                contentWidth,
                "3D Preview",
                this::open3dPreview
        );
        controls.addChild(preview3dButton);

        applyButton = button(
                contentWidth,
                "Apply as Image Layer",
                this::applyImport
        );
        controls.addChild(applyButton);

        controls.addChild(button(
                contentWidth,
                "Cancel",
                () -> this.minecraft.setScreen(parent)
        ));

        ScrollableLayout scrollable = new ScrollableLayout(
                this.minecraft,
                controls,
                controlPanelHeight
        );
        scrollable.setMinWidth(panelWidth);
        scrollable.setMaxHeight(controlPanelHeight);
        scrollable.arrangeElements();
        scrollable.setX(controlPanelX);
        scrollable.setY(controlPanelY);
        scrollable.visitWidgets(widget ->
                addRenderableWidget((AbstractWidget)widget)
        );

        updateButtonLabels();
    }

    private LoomButton lastReadOnly;

    private LinearLayout adjustmentRow(
            int width,
            String label,
            Runnable decrease,
            Runnable increase
    ) {
        LinearLayout row = LinearLayout.horizontal().spacing(4);
        row.addChild(button(48, label.substring(0, 3) + " -", decrease));
        lastReadOnly = readOnlyButton(
                Math.max(42, width - 104),
                ""
        );
        row.addChild(lastReadOnly);
        row.addChild(button(48, label.substring(0, 3) + " +", increase));
        return row;
    }

    private LoomButton button(
            int width,
            String label,
            Runnable action
    ) {
        return new LoomButton(
                0,
                0,
                width,
                22,
                Component.literal(label),
                action
        );
    }

    private LoomButton readOnlyButton(int width, String label) {
        LoomButton button = button(width, label, () -> { });
        button.active = false;
        return button;
    }

    private void choosePng() {
        String selected = TinyFileDialogs.tinyfd_openFileDialog(
                "Loom Studios - Import PNG",
                "",
                null,
                "PNG image (*.png)",
                false
        );

        if (selected == null || selected.isBlank()) {
            return;
        }

        try {
            loaded = PngImportAdapter.load(Path.of(selected));
            editingBaseData = null;
            resetTransform();
            processing = ImageProcessingSettings.defaults();
            layerName = loaded.sourcePath().getFileName().toString();

            PixelImage original = loaded.original();
            PixelImage embedded = loaded.embedded();

            status = original.equals(embedded)
                    ? "Loaded " + loaded.sourcePath().getFileName()
                    : "Loaded "
                            + loaded.sourcePath().getFileName()
                            + " • source stored at "
                            + embedded.width()
                            + "×"
                            + embedded.height();

            touch();
        } catch (IOException | IllegalArgumentException e) {
            LoomStudios.LOGGER.error(
                    "Failed to import Smart Import PNG {}",
                    selected,
                    e
            );
            status = "Import failed: " + e.getMessage();
            touch();
        }
    }

    private void resetTransform() {
        offsetX = 0.0;
        offsetY = 0.0;
        scale = 1.0;
        rotationDegrees = 0.0;
        mirrorHorizontal = false;
        mirrorVertical = false;
    }

    private void cyclePlacement() {
        ImagePlacementMode[] values = ImagePlacementMode.values();
        placementMode = values[
                (placementMode.ordinal() + 1) % values.length
        ];
        editingBaseData = null;
        resetTransform();
        touch();
    }

    private void toggleKeepAspect() {
        keepAspect = !keepAspect;
        editingBaseData = null;
        resetTransform();
        touch();
    }

    private void move(int dx, int dy) {
        LoomCanvas canvas = targetCanvas(
                ClientProjectWorkspace.project()
        );
        offsetX += dx / (double)canvas.width();
        offsetY += dy / (double)canvas.height();
        touch();
    }

    private void changeScale(double delta) {
        scale = Math.max(
                0.1,
                Math.min(4.0, scale + delta)
        );
        touch();
    }

    private void rotate(double delta) {
        rotationDegrees = normalizeDegrees(rotationDegrees + delta);
        touch();
    }

    private void cycleProcessingMode() {
        processing = processing.withMode(
                processing.mode().next()
        );
        touch();
    }

    private void toggleSelectedPalette() {
        if (!processing.palette().isEmpty()) {
            processing = processing.withPalette(java.util.List.of());
            touch();
            return;
        }

        ColorPaletteLibrary.refresh();
        ColorPaletteLibrary.selected().ifPresentOrElse(
                palette -> {
                    processing = processing.withPalette(palette.colors());
                    processing = processing.withMode(
                            ImageProcessingMode.PALETTE_LIMITED
                    );
                    touch();
                },
                () -> {
                    status = "No saved Swatches palette is available";
                    updateButtonLabels();
                }
        );
    }

    private void changeBrightness(float delta) {
        processing = processing.withAdjustments(
                clampAdjustment(processing.brightness() + delta),
                processing.contrast(),
                processing.saturation()
        );
        touch();
    }

    private void changeContrast(float delta) {
        processing = processing.withAdjustments(
                processing.brightness(),
                clampAdjustment(processing.contrast() + delta),
                processing.saturation()
        );
        touch();
    }

    private void changeSaturation(float delta) {
        processing = processing.withAdjustments(
                processing.brightness(),
                processing.contrast(),
                clampAdjustment(processing.saturation() + delta)
        );
        touch();
    }

    private void changeColorLimit(int direction) {
        int index = 0;
        for (int i = 0; i < COLOR_LIMITS.length; i++) {
            if (COLOR_LIMITS[i] == processing.colorLimit()) {
                index = i;
                break;
            }
        }

        index = Math.max(
                0,
                Math.min(
                        COLOR_LIMITS.length - 1,
                        index + direction
                )
        );

        processing = processing.withColorReduction(
                COLOR_LIMITS[index],
                processing.dither()
        );
        touch();
    }

    private void changePosterize(int delta) {
        processing = processing.withPosterizeLevels(
                Math.max(
                        2,
                        Math.min(
                                16,
                                processing.posterizeLevels() + delta
                        )
                )
        );
        touch();
    }

    private PixelImage originalImage() {
        return loaded == null ? null : loaded.original();
    }

    private PixelImage processedImage() {
        ensureCaches();
        return processedCache;
    }

    private PixelImage textureImage() {
        ensureCaches();
        return textureCache;
    }

    private LoomProject candidateProject() {
        ensureCaches();
        return candidateProjectCache == null
                ? ClientProjectWorkspace.project()
                : candidateProjectCache;
    }

    private void ensureCaches() {
        if (cacheRevision == revision) {
            return;
        }

        cacheRevision = revision;
        processedCache = null;
        textureCache = null;
        candidateProjectCache = null;

        if (loaded == null) {
            return;
        }

        LoomProject project = ClientProjectWorkspace.project();
        LoomCanvas canvas = targetCanvas(project);
        ImageLayerData primary = buildImageData(
                primaryTargetRect(project),
                false
        );

        processedCache = ImageProcessingPipeline.apply(
                primary.source(),
                primary.processing()
        );

        LoomLayer previewLayer = previewLayer(
                PREVIEW_LAYER_ID,
                primary
        );
        int[] raster = LayerRasterizer.rasterize(
                previewLayer,
                canvas.width(),
                canvas.height()
        );

        if (importTarget == ImportTarget.ELYTRA_LINKED) {
            ImageLayerData secondary = buildImageData(
                    ElytraWing.RIGHT.normalizedRect(project.elytra()),
                    true
            );
            LoomLayer rightPreview = previewLayer(
                    PREVIEW_LAYER_ID_RIGHT,
                    secondary
            );
            int[] rightRaster = LayerRasterizer.rasterize(
                    rightPreview,
                    canvas.width(),
                    canvas.height()
            );

            for (int i = 0; i < raster.length; i++) {
                if (((rightRaster[i] >>> 24) & 0xFF) != 0) {
                    raster[i] = rightRaster[i];
                }
            }

            LoomProject candidate = ProjectEdits.addElytraImageLayer(
                    project,
                    "Smart Import Left",
                    primary
            );
            candidate = ProjectEdits.addElytraImageLayer(
                    candidate,
                    "Smart Import Right",
                    secondary
            );
            candidateProjectCache = candidate;
        } else if (importTarget == ImportTarget.ELYTRA_SINGLE) {
            LoomLayer current = project.elytra().layers().stream()
                    .filter(layer -> layer.id().equals(editingLayerId))
                    .findFirst()
                    .orElse(null);

            if (current != null
                    && current.kind()
                    == dev.loomstudios.project.LayerKind.IMAGE) {
                candidateProjectCache = project.withElytra(
                        project.elytra().replaceLayer(
                                editingLayerId,
                                current.withImageData(primary)
                        )
                );
            }
        } else if (editingLayerId != null) {
            LoomLayer current = project.cape().layers().stream()
                    .filter(layer -> layer.id().equals(editingLayerId))
                    .findFirst()
                    .orElse(null);

            if (current != null
                    && current.kind()
                    == dev.loomstudios.project.LayerKind.IMAGE) {
                candidateProjectCache = project.withCape(
                        project.cape().replaceLayer(
                                editingLayerId,
                                current.withImageData(primary)
                        )
                );
            }
        } else {
            ArrayList<LoomLayer> layers =
                    new ArrayList<>(project.cape().layers());
            layers.add(previewLayer);

            candidateProjectCache = project.withCape(
                    new LoomCanvas(
                            project.cape().width(),
                            project.cape().height(),
                            layers
                    )
            );
        }

        textureCache = new PixelImage(
                canvas.width(),
                canvas.height(),
                raster
        );
    }

    private LoomLayer previewLayer(
            UUID id,
            ImageLayerData data
    ) {
        return LoomLayer.image(
                id,
                "Smart Import Preview",
                true,
                1.0F,
                dev.loomstudios.project.BlendMode.NORMAL,
                false,
                false,
                data
        );
    }

    private ImageLayerData buildImageData() {
        LoomProject project = ClientProjectWorkspace.project();
        return buildImageData(primaryTargetRect(project), false);
    }

    private ImageLayerData buildImageData(
            NormalizedRect target,
            boolean forceMirrorHorizontal
    ) {
        if (loaded == null) {
            throw new IllegalStateException(
                    "No Smart Import image is loaded"
            );
        }

        LoomProject project = ClientProjectWorkspace.project();
        LoomCanvas canvas = targetCanvas(project);

        ImagePlacementMode effectiveMode =
                !keepAspect && placementMode == ImagePlacementMode.FIT
                        ? ImagePlacementMode.STRETCH
                        : placementMode;

        ImageLayerData base =
                editingBaseData != null
                        && importTarget != ImportTarget.ELYTRA_LINKED
                ? editingBaseData
                : ImageLayerData.placed(
                        loaded.embedded(),
                        canvas.width(),
                        canvas.height(),
                        target,
                        effectiveMode
                );

        LayerTransform transform = base.transform();
        transform = new LayerTransform(
                transform.centerX() + offsetX,
                transform.centerY() + offsetY,
                transform.width() * scale,
                transform.height() * scale,
                rotationDegrees,
                mirrorHorizontal ^ forceMirrorHorizontal,
                mirrorVertical
        );

        return new ImageLayerData(
                base.source(),
                base.sourceCrop(),
                transform,
                base.clip(),
                processing
        );
    }

    private LoomCanvas targetCanvas(LoomProject project) {
        return importTarget == ImportTarget.CAPE
                ? project.cape()
                : project.elytra();
    }

    private NormalizedRect primaryTargetRect(LoomProject project) {
        if (importTarget == ImportTarget.CAPE) {
            int scale = CanvasResolution.fromCanvas(
                    project.cape()
            ).scale();

            return new NormalizedRect(
                    targetRegion.atlasX(0, scale)
                            / (double)project.cape().width(),
                    targetRegion.atlasY(0, scale)
                            / (double)project.cape().height(),
                    targetRegion.width(scale)
                            / (double)project.cape().width(),
                    targetRegion.height(scale)
                            / (double)project.cape().height()
            );
        }

        if (importTarget == ImportTarget.ELYTRA_SINGLE
                && editingBaseData != null) {
            return editingBaseData.clip();
        }

        return ElytraWing.LEFT.normalizedRect(project.elytra());
    }

    private void open3dPreview() {
        if (loaded == null) {
            return;
        }

        this.minecraft.setScreen(
                new LoomPlayerPreviewScreen(
                        this,
                        this::candidateProject,
                        importTarget != ImportTarget.CAPE
                )
        );
    }

    private void applyImport() {
        if (loaded == null) {
            return;
        }

        ImageLayerData primary = buildImageData();
        String baseName = layerName;
        int dot = baseName.lastIndexOf('.');
        String normalizedLayerName = dot > 0
                ? baseName.substring(0, dot)
                : baseName;

        try {
            if (importTarget == ImportTarget.ELYTRA_LINKED) {
                LoomProject project = ClientProjectWorkspace.project();
                ImageLayerData right = buildImageData(
                        ElytraWing.RIGHT.normalizedRect(project.elytra()),
                        true
                );

                ClientProjectWorkspace.apply(current -> {
                    LoomProject updated =
                            ProjectEdits.addElytraImageLayer(
                                    current,
                                    normalizedLayerName + " Left",
                                    primary
                            );
                    updated = ProjectEdits.addElytraImageLayer(
                            updated,
                            normalizedLayerName + " Right",
                            right
                    );
                    updated.encode();
                    return updated;
                });
                status = "Imported as linked editable Elytra Image layers";
            } else if (importTarget == ImportTarget.ELYTRA_SINGLE) {
                ClientProjectWorkspace.apply(project -> {
                    LoomProject updated =
                            ProjectEdits.setElytraImageData(
                                    project,
                                    editingLayerId,
                                    primary
                            );
                    updated.encode();
                    return updated;
                });
                status = "Updated editable Elytra Image layer";
            } else if (editingLayerId == null) {
                ClientProjectWorkspace.apply(project -> {
                    LoomProject updated =
                            ProjectEdits.addCapeImageLayer(
                                    project,
                                    normalizedLayerName,
                                    primary
                            );
                    updated.encode();
                    return updated;
                });
                status = "Imported as editable Image layer";
            } else {
                ClientProjectWorkspace.apply(project -> {
                    LoomProject updated =
                            ProjectEdits.setCapeImageData(
                                    project,
                                    editingLayerId,
                                    primary
                            );
                    updated.encode();
                    return updated;
                });
                status = "Updated editable Image layer";
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            LoomStudios.LOGGER.warn(
                    "Smart Import result rejected: {}",
                    e.getMessage()
            );
            status = "Import result is too large or invalid";
            updateButtonLabels();
            return;
        }

        if (parent instanceof CapeEditorScreen
                || parent instanceof ElytraEditorScreen) {
            this.minecraft.setScreen(parent);
        } else if (importTarget == ImportTarget.CAPE) {
            this.minecraft.setScreen(new CapeEditorScreen(parent));
        } else {
            this.minecraft.setScreen(new ElytraEditorScreen(parent));
        }
    }

    private void touch() {
        revision++;
        cacheRevision = Long.MIN_VALUE;
        updateButtonLabels();
    }

    private long revision() {
        return revision;
    }

    private void updateButtonLabels() {
        if (placementButton == null) {
            return;
        }

        boolean hasImage = loaded != null;

        placementButton.setMessage(Component.literal(
                "Placement: " + placementMode.name()
        ));
        keepAspectButton.setMessage(Component.literal(
                "Keep Aspect: " + (keepAspect ? "On" : "Off")
        ));
        modeButton.setMessage(Component.literal(
                "Mode: " + processing.mode().displayName()
        ));
        ditherButton.setMessage(Component.literal(
                "Dither: " + (processing.dither() ? "On" : "Off")
        ));
        paletteButton.setMessage(Component.literal(
                processing.palette().isEmpty()
                        ? "Palette: Auto"
                        : "Palette: Swatches ("
                                + processing.palette().size()
                                + ")"
        ));
        paletteButton.active = hasImage;
        colorLimitButton.setMessage(Component.literal(
                processing.colorLimit() == 0
                        ? "Colors: Auto"
                        : "Colors: " + processing.colorLimit()
        ));
        posterizeButton.setMessage(Component.literal(
                "Levels " + processing.posterizeLevels()
        ));
        brightnessButton.setMessage(Component.literal(
                percent(processing.brightness())
        ));
        contrastButton.setMessage(Component.literal(
                percent(processing.contrast())
        ));
        saturationButton.setMessage(Component.literal(
                percent(processing.saturation())
        ));
        scaleButton.setMessage(Component.literal(
                Math.round(scale * 100.0) + "%"
        ));
        rotationButton.setMessage(Component.literal(
                Math.round(rotationDegrees) + "°"
        ));
        mirrorHorizontalButton.setMessage(Component.literal(
                "Mirror H: " + (mirrorHorizontal ? "On" : "Off")
        ));
        mirrorVerticalButton.setMessage(Component.literal(
                "Mirror V: " + (mirrorVertical ? "On" : "Off")
        ));

        placementButton.active = hasImage;
        keepAspectButton.active = hasImage;
        modeButton.active = hasImage;
        ditherButton.active = hasImage;
        preview3dButton.active = hasImage;
        applyButton.active = hasImage;
    }

    private static String percent(float normalized) {
        int percent = Math.round(normalized * 100.0F);
        return (percent > 0 ? "+" : "") + percent + "%";
    }

    private static float clampAdjustment(float value) {
        return Math.max(-1.0F, Math.min(1.0F, value));
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        return normalized < 0.0
                ? normalized + 360.0
                : normalized;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                LoomUiTheme.BACKDROP
        );

        graphics.drawString(
                this.font,
                Component.literal(
                        importTarget == ImportTarget.CAPE
                                ? "Smart Import"
                                : "Smart Import • Elytra"
                ),
                18,
                17,
                LoomUiTheme.TEXT,
                false
        );

        graphics.drawString(
                this.font,
                Component.literal(status),
                18,
                31,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        graphics.fill(
                controlPanelX - 4,
                controlPanelY - 4,
                controlPanelX + controlPanelWidth,
                controlPanelY + controlPanelHeight + 4,
                LoomUiTheme.PANEL
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        if (originalPreview != null) {
            originalPreview.close();
        }
        if (processedPreview != null) {
            processedPreview.close();
        }
        if (texturePreview != null) {
            texturePreview.close();
        }
        super.removed();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }
}
