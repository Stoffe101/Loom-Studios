package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomElytraCanvasWidget;
import dev.loomstudios.client.ui.LoomLayerListWidget;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.BlendMode;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.ElytraWing;
import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.ProjectEdits;
import dev.loomstudios.project.ProjectResizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.UUID;

public final class ElytraEditorScreen extends Screen {
    private enum Tool {
        PENCIL,
        ERASER
    }

    private final Screen parent;

    private Tool tool = Tool.PENCIL;
    private int selectedColor = 0xFF22D7E8;
    private int brushSize = 1;
    private boolean linkedMirror = true;
    private LoomPlayerPreviewWidget.ElytraPose previewPose =
            LoomPlayerPreviewWidget.ElytraPose.STANDING;
    private UUID selectedLayerId;

    private LoomElytraCanvasWidget canvasWidget;
    private LoomColorPickerWidget colorPicker;
    private LoomPlayerPreviewWidget previewWidget;
    private LoomLayerListWidget layerListWidget;

    private LoomButton layerAddButton;
    private LoomButton layerDuplicateButton;
    private LoomButton layerDeleteButton;
    private LoomButton layerUpButton;
    private LoomButton layerDownButton;
    private EditBox layerNameField;
    private LoomButton layerRenameButton;
    private LoomButton layerOpacityDownButton;
    private LoomButton layerOpacityLabelButton;
    private LoomButton layerOpacityUpButton;
    private LoomButton layerBlendButton;
    private LoomButton capeConversionButton;

    private LoomButton pencilButton;
    private LoomButton eraserButton;
    private LoomButton linkButton;
    private LoomButton previewStandingButton;
    private LoomButton previewOpenButton;
    private LoomButton previewGlidingButton;
    private LoomButton brushDownButton;
    private LoomButton brushLabelButton;
    private LoomButton brushUpButton;
    private LoomButton resolutionDownButton;
    private LoomButton resolutionLabelButton;
    private LoomButton resolutionUpButton;
    private LoomButton thicknessDownButton;
    private LoomButton thicknessLabelButton;
    private LoomButton thicknessUpButton;
    private LoomButton lockButton;
    private LoomButton undoButton;
    private LoomButton redoButton;

    private int shellLeft;
    private int shellTop;
    private int shellRight;
    private int shellBottom;
    private int contentTop;

    public ElytraEditorScreen(Screen parent) {
        super(Component.literal("Loom Studios - Elytra Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ensureSelectedLayerExists();

        int margin = Math.max(8, Math.min(14, this.width / 70));
        shellLeft = margin;
        shellTop = Math.max(7, margin / 2);
        shellRight = this.width - margin;
        shellBottom = this.height - margin;
        contentTop = shellTop + 36;

        int gap = 8;
        int totalWidth = shellRight - shellLeft;
        int leftWidth = Math.max(
                118,
                Math.min(150, (int)Math.round(totalWidth * 0.19))
        );
        int previewWidth = Math.max(
                148,
                Math.min(220, (int)Math.round(totalWidth * 0.23))
        );

        int leftX = shellLeft + 7;
        int leftRight = leftX + leftWidth;
        int previewRight = shellRight - 7;
        int previewLeft = previewRight - previewWidth;
        int centerLeft = leftRight + gap;
        int centerRight = previewLeft - gap;

        if (centerRight - centerLeft < 230) {
            int shortage = 230 - (centerRight - centerLeft);
            int shaveLeft = Math.min(
                    shortage / 2,
                    Math.max(0, leftWidth - 110)
            );
            leftRight -= shaveLeft;
            centerLeft -= shaveLeft;

            int remaining = shortage - shaveLeft;
            int shaveRight = Math.min(
                    remaining,
                    Math.max(0, previewWidth - 138)
            );
            previewLeft += shaveRight;
            centerRight += shaveRight;
        }

        buildControls(
                leftX,
                contentTop,
                leftRight - leftX,
                shellBottom - contentTop - 7
        );

        int centerHeight = shellBottom - contentTop - 7;
        int colorHeight = Math.max(
                116,
                Math.min(145, centerHeight / 3)
        );
        int canvasHeight = Math.max(
                100,
                centerHeight - colorHeight - gap
        );

        this.canvasWidget = new LoomElytraCanvasWidget(
                centerLeft,
                contentTop,
                centerRight - centerLeft,
                canvasHeight,
                ClientProjectWorkspace::project,
                ClientProjectWorkspace::revision,
                this::editWing,
                new LoomElytraCanvasWidget.StrokeLifecycle() {
                    @Override
                    public void begin() {
                        ClientProjectWorkspace.beginCompoundEdit();
                    }

                    @Override
                    public void end() {
                        ClientProjectWorkspace.endCompoundEdit();
                        updateButtonStates();
                    }
                }
        );
        addRenderableWidget(this.canvasWidget);

        this.colorPicker = new LoomColorPickerWidget(
                centerLeft,
                contentTop + canvasHeight + gap,
                centerRight - centerLeft,
                colorHeight,
                selectedColor,
                color -> selectedColor = color
        );
        addRenderableWidget(this.colorPicker);

        int rightWidth = previewRight - previewLeft;
        int rightHeight = shellBottom - contentTop - 7;
        int layerListHeight = Math.max(
                62,
                Math.min(82, rightHeight / 4)
        );
        int actionHeight = 18;
        int actionRows = 6;
        int previewHeight = Math.max(
                72,
                rightHeight
                        - layerListHeight
                        - actionHeight * actionRows
                        - 30
        );

        this.previewWidget = new LoomPlayerPreviewWidget(
                previewLeft,
                contentTop,
                rightWidth,
                previewHeight,
                ClientProjectWorkspace::project,
                LoomPlayerPreviewWidget.Mode.ELYTRA
        );
        this.previewWidget.setElytraPose(previewPose);
        addRenderableWidget(this.previewWidget);

        int layerTop = contentTop + previewHeight + 6;
        this.layerListWidget = new LoomLayerListWidget(
                previewLeft,
                layerTop,
                rightWidth,
                layerListHeight,
                () -> ClientProjectWorkspace.project().elytra(),
                () -> selectedLayerId,
                this::selectLayer,
                this::toggleLayerVisibility,
                this::toggleLayerLock
        );
        addRenderableWidget(this.layerListWidget);

        int actionsTop = layerTop + layerListHeight + 4;
        int rowGap = 4;
        int third = Math.max(38, (rightWidth - 8) / 3);

        this.layerAddButton = new LoomButton(
                previewLeft,
                actionsTop,
                third,
                actionHeight,
                Component.literal("+ Layer"),
                this::addLayer
        );
        this.layerDuplicateButton = new LoomButton(
                previewLeft + third + 4,
                actionsTop,
                third,
                actionHeight,
                Component.literal("Copy"),
                this::duplicateLayer
        );
        this.layerDeleteButton = new LoomButton(
                previewLeft + third * 2 + 8,
                actionsTop,
                Math.max(38, rightWidth - third * 2 - 8),
                actionHeight,
                Component.literal("Delete"),
                this::deleteLayer
        );
        addRenderableWidget(this.layerAddButton);
        addRenderableWidget(this.layerDuplicateButton);
        addRenderableWidget(this.layerDeleteButton);

        int row = actionsTop + actionHeight + rowGap;
        int half = Math.max(48, (rightWidth - 4) / 2);
        this.layerUpButton = new LoomButton(
                previewLeft,
                row,
                half,
                actionHeight,
                Component.literal("Layer Up"),
                () -> moveLayer(1)
        );
        this.layerDownButton = new LoomButton(
                previewLeft + half + 4,
                row,
                rightWidth - half - 4,
                actionHeight,
                Component.literal("Layer Down"),
                () -> moveLayer(-1)
        );
        addRenderableWidget(this.layerUpButton);
        addRenderableWidget(this.layerDownButton);

        row += actionHeight + rowGap;
        int renameWidth = Math.max(48, Math.min(58, rightWidth / 3));
        this.layerNameField = new EditBox(
                this.font,
                previewLeft,
                row,
                Math.max(56, rightWidth - renameWidth - 4),
                actionHeight,
                Component.literal("Layer name")
        );
        this.layerNameField.setMaxLength(
                dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS
        );
        this.layerNameField.setHint(Component.literal("Layer name"));
        this.layerRenameButton = new LoomButton(
                previewLeft + this.layerNameField.getWidth() + 4,
                row,
                rightWidth - this.layerNameField.getWidth() - 4,
                actionHeight,
                Component.literal("Rename"),
                this::renameLayer
        );
        addRenderableWidget(this.layerNameField);
        addRenderableWidget(this.layerRenameButton);

        row += actionHeight + rowGap;
        int small = 34;
        this.layerOpacityDownButton = new LoomButton(
                previewLeft,
                row,
                small,
                actionHeight,
                Component.literal("-"),
                () -> changeLayerOpacity(-0.1F)
        );
        this.layerOpacityLabelButton = new LoomButton(
                previewLeft + small + 4,
                row,
                Math.max(42, rightWidth - small * 2 - 8),
                actionHeight,
                Component.literal("Opacity 100%"),
                () -> { }
        );
        this.layerOpacityLabelButton.active = false;
        this.layerOpacityUpButton = new LoomButton(
                previewRight - small,
                row,
                small,
                actionHeight,
                Component.literal("+"),
                () -> changeLayerOpacity(0.1F)
        );
        addRenderableWidget(this.layerOpacityDownButton);
        addRenderableWidget(this.layerOpacityLabelButton);
        addRenderableWidget(this.layerOpacityUpButton);

        row += actionHeight + rowGap;
        this.layerBlendButton = new LoomButton(
                previewLeft,
                row,
                rightWidth,
                actionHeight,
                Component.literal("Blend: Normal"),
                this::cycleLayerBlendMode
        );
        addRenderableWidget(this.layerBlendButton);

        row += actionHeight + rowGap;
        addRenderableWidget(new LoomButton(
                previewLeft,
                row,
                rightWidth,
                actionHeight,
                Component.literal("Reset 3D View"),
                this.previewWidget::resetView
        ));

        syncLayerFields();
        updateButtonStates();
    }

    private void buildControls(
            int x,
            int y,
            int width,
            int height
    ) {
        int gap = 5;
        int row = y;

        addRenderableWidget(new LoomButton(
                x,
                row,
                width,
                20,
                Component.literal("Home"),
                this::goBack
        ));
        row += 20 + gap;

        this.linkButton = new LoomButton(
                x,
                row,
                width,
                22,
                Component.literal("Wings: Linked Mirror"),
                this::toggleLinked
        );
        addRenderableWidget(this.linkButton);
        row += 22 + gap;

        this.capeConversionButton = new LoomButton(
                x,
                row,
                width,
                20,
                Component.literal("Cape -> Elytra Layer"),
                this::convertCapeToElytra
        );
        addRenderableWidget(this.capeConversionButton);
        row += 20 + gap;

        int poseThird = Math.max(32, (width - 8) / 3);
        this.previewStandingButton = new LoomButton(
                x,
                row,
                poseThird,
                20,
                Component.literal("Stand"),
                () -> setPreviewPose(
                        LoomPlayerPreviewWidget.ElytraPose.STANDING
                )
        );
        this.previewOpenButton = new LoomButton(
                x + poseThird + 4,
                row,
                poseThird,
                20,
                Component.literal("Open"),
                () -> setPreviewPose(
                        LoomPlayerPreviewWidget.ElytraPose.OPEN
                )
        );
        this.previewGlidingButton = new LoomButton(
                x + poseThird * 2 + 8,
                row,
                Math.max(32, width - poseThird * 2 - 8),
                20,
                Component.literal("Glide"),
                () -> setPreviewPose(
                        LoomPlayerPreviewWidget.ElytraPose.GLIDING
                )
        );
        addRenderableWidget(this.previewStandingButton);
        addRenderableWidget(this.previewOpenButton);
        addRenderableWidget(this.previewGlidingButton);
        row += 20 + gap;

        int half = Math.max(48, (width - 4) / 2);
        this.pencilButton = new LoomButton(
                x,
                row,
                half,
                20,
                Component.literal("Pencil"),
                () -> {
                    tool = Tool.PENCIL;
                    updateButtonStates();
                }
        );
        this.eraserButton = new LoomButton(
                x + half + 4,
                row,
                width - half - 4,
                20,
                Component.literal("Eraser"),
                () -> {
                    tool = Tool.ERASER;
                    updateButtonStates();
                }
        );
        addRenderableWidget(this.pencilButton);
        addRenderableWidget(this.eraserButton);
        row += 20 + gap;

        int small = 34;
        this.brushDownButton = new LoomButton(
                x,
                row,
                small,
                20,
                Component.literal("-"),
                () -> changeBrush(-1)
        );
        this.brushLabelButton = new LoomButton(
                x + small + 4,
                row,
                Math.max(42, width - small * 2 - 8),
                20,
                Component.literal("Brush 1"),
                () -> { }
        );
        this.brushLabelButton.active = false;
        this.brushUpButton = new LoomButton(
                x + width - small,
                row,
                small,
                20,
                Component.literal("+"),
                () -> changeBrush(1)
        );
        addRenderableWidget(this.brushDownButton);
        addRenderableWidget(this.brushLabelButton);
        addRenderableWidget(this.brushUpButton);
        row += 20 + gap;

        this.resolutionDownButton = new LoomButton(
                x,
                row,
                small,
                20,
                Component.literal("-"),
                () -> changeResolution(-1)
        );
        this.resolutionLabelButton = new LoomButton(
                x + small + 4,
                row,
                Math.max(42, width - small * 2 - 8),
                20,
                Component.literal("1x"),
                () -> { }
        );
        this.resolutionLabelButton.active = false;
        this.resolutionUpButton = new LoomButton(
                x + width - small,
                row,
                small,
                20,
                Component.literal("+"),
                () -> changeResolution(1)
        );
        addRenderableWidget(this.resolutionDownButton);
        addRenderableWidget(this.resolutionLabelButton);
        addRenderableWidget(this.resolutionUpButton);
        row += 20 + gap;

        this.thicknessDownButton = new LoomButton(
                x,
                row,
                small,
                20,
                Component.literal("-"),
                () -> changeThickness(-0.25F)
        );
        this.thicknessLabelButton = new LoomButton(
                x + small + 4,
                row,
                Math.max(42, width - small * 2 - 8),
                20,
                Component.literal("Depth 100%"),
                () -> { }
        );
        this.thicknessLabelButton.active = false;
        this.thicknessUpButton = new LoomButton(
                x + width - small,
                row,
                small,
                20,
                Component.literal("+"),
                () -> changeThickness(0.25F)
        );
        addRenderableWidget(this.thicknessDownButton);
        addRenderableWidget(this.thicknessLabelButton);
        addRenderableWidget(this.thicknessUpButton);
        row += 20 + gap;

        this.lockButton = new LoomButton(
                x,
                row,
                width,
                20,
                Component.literal("Layer Lock: Off"),
                this::toggleLock
        );
        addRenderableWidget(this.lockButton);
        row += 20 + gap;

        this.undoButton = new LoomButton(
                x,
                row,
                half,
                20,
                Component.literal("Undo"),
                this::undo
        );
        this.redoButton = new LoomButton(
                x + half + 4,
                row,
                width - half - 4,
                20,
                Component.literal("Redo"),
                this::redo
        );
        addRenderableWidget(this.undoButton);
        addRenderableWidget(this.redoButton);
        row += 20 + gap;

        addRenderableWidget(new LoomButton(
                x,
                row,
                width,
                20,
                Component.literal("Save"),
                this::save
        ));
        row += 20 + gap;

        addRenderableWidget(new LoomButton(
                x,
                row,
                width,
                20,
                Component.literal("Save + Equip"),
                this::saveAndEquip
        ));
    }

    private void editWing(ElytraWing wing, int x, int y) {
        LoomLayer layer = selectedLayer();
        if (layer == null || !layer.editableAsPaint()) {
            return;
        }

        int color = tool == Tool.ERASER ? 0 : selectedColor;
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.paintElytraWingBrush(
                        project,
                        selectedLayerId,
                        wing,
                        x,
                        y,
                        brushSize,
                        color,
                        linkedMirror
                )
        );
    }

    private void convertCapeToElytra() {
        var result = ClientProjectWorkspace.apply(
                ProjectEdits::addCapeConversionElytraLayer
        );

        selectedLayerId = result.elytra()
                .layers()
                .getLast()
                .id();
        updateButtonStates();
    }

    private void setPreviewPose(
            LoomPlayerPreviewWidget.ElytraPose pose
    ) {
        previewPose = pose;
        if (previewWidget != null) {
            previewWidget.setElytraPose(pose);
        }
        updateButtonStates();
    }

    private void toggleLinked() {
        linkedMirror = !linkedMirror;
        updateButtonStates();
    }

    private void changeBrush(int delta) {
        int scale = CanvasResolution.fromCanvas(
                ClientProjectWorkspace.project().elytra()
        ).scale();
        brushSize = Math.max(
                1,
                Math.min(scale * 8, brushSize + delta)
        );
        updateButtonStates();
    }

    private void changeResolution(int direction) {
        CanvasResolution current = CanvasResolution.fromCanvas(
                ClientProjectWorkspace.project().elytra()
        );
        CanvasResolution next = direction < 0
                ? current.lower()
                : current.higher();

        if (next == current) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectResizer.resizeElytra(project, next)
        );
        brushSize = Math.min(brushSize, next.scale() * 8);
        updateButtonStates();
    }

    private void changeThickness(float delta) {
        float current = ClientProjectWorkspace.project()
                .runtime()
                .elytraThickness();
        float next = Math.max(
                0.25F,
                Math.min(2.0F, current + delta)
        );

        if (Math.abs(next - current) < 1.0E-6F) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                project.withRuntime(
                        project.runtime().withElytraThickness(next)
                )
        );
        updateButtonStates();
    }

    private void toggleLock() {
        ensureSelectedLayerExists();
        if (selectedLayerId != null) {
            toggleLayerLock(selectedLayerId);
        }
    }

    private void toggleLayerLock(UUID layerId) {
        LoomLayer layer = findLayer(layerId);
        if (layer == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setElytraLayerLocked(
                        project,
                        layerId,
                        !layer.locked()
                )
        );
        updateButtonStates();
    }

    private void toggleLayerVisibility(UUID layerId) {
        LoomLayer layer = findLayer(layerId);
        if (layer == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setElytraLayerVisible(
                        project,
                        layerId,
                        !layer.visible()
                )
        );
        updateButtonStates();
    }

    private void selectLayer(UUID layerId) {
        if (findLayer(layerId) == null) {
            return;
        }
        selectedLayerId = layerId;
        syncLayerFields();
        updateButtonStates();
    }

    private void addLayer() {
        var result = ClientProjectWorkspace.apply(project ->
                ProjectEdits.addElytraLayer(project, "Wing Layer")
        );
        selectedLayerId = result.elytra().layers().getLast().id();
        syncLayerFields();
        updateButtonStates();
    }

    private void duplicateLayer() {
        ensureSelectedLayerExists();
        if (selectedLayerId == null) {
            return;
        }

        var before = ClientProjectWorkspace.project().elytra().layers();
        int index = -1;
        for (int i = 0; i < before.size(); i++) {
            if (before.get(i).id().equals(selectedLayerId)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return;
        }

        int originalIndex = index;
        var result = ClientProjectWorkspace.apply(project ->
                ProjectEdits.duplicateElytraLayer(
                        project,
                        selectedLayerId
                )
        );
        selectedLayerId = result.elytra()
                .layers()
                .get(originalIndex + 1)
                .id();
        syncLayerFields();
        updateButtonStates();
    }

    private void deleteLayer() {
        ensureSelectedLayerExists();
        var before = ClientProjectWorkspace.project().elytra().layers();
        if (selectedLayerId == null || before.size() <= 1) {
            return;
        }

        int index = 0;
        for (int i = 0; i < before.size(); i++) {
            if (before.get(i).id().equals(selectedLayerId)) {
                index = i;
                break;
            }
        }

        int oldIndex = index;
        var result = ClientProjectWorkspace.apply(project ->
                ProjectEdits.removeElytraLayer(
                        project,
                        selectedLayerId
                )
        );
        int nextIndex = Math.min(
                oldIndex,
                result.elytra().layers().size() - 1
        );
        selectedLayerId = result.elytra()
                .layers()
                .get(nextIndex)
                .id();
        syncLayerFields();
        updateButtonStates();
    }

    private void moveLayer(int delta) {
        ensureSelectedLayerExists();
        if (selectedLayerId == null || delta == 0) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.moveElytraLayer(
                        project,
                        selectedLayerId,
                        delta
                )
        );
        updateButtonStates();
    }

    private void renameLayer() {
        if (layerNameField == null || selectedLayerId == null) {
            return;
        }

        String name = layerNameField.getValue().trim();
        if (name.isEmpty()) {
            syncLayerFields();
            return;
        }

        try {
            ClientProjectWorkspace.apply(project ->
                    ProjectEdits.renameElytraLayer(
                            project,
                            selectedLayerId,
                            name
                    )
            );
        } catch (IllegalArgumentException ignored) {
        }

        syncLayerFields();
        updateButtonStates();
    }

    private void changeLayerOpacity(float delta) {
        LoomLayer layer = selectedLayer();
        if (layer == null) {
            return;
        }

        float next = Math.max(
                0.0F,
                Math.min(1.0F, layer.opacity() + delta)
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setElytraLayerOpacity(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void cycleLayerBlendMode() {
        LoomLayer layer = selectedLayer();
        if (layer == null) {
            return;
        }

        BlendMode next = layer.blendMode().next();
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setElytraLayerBlendMode(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void syncLayerFields() {
        if (layerNameField == null) {
            return;
        }

        LoomLayer layer = selectedLayer();
        if (layer != null && !layerNameField.isFocused()) {
            layerNameField.setValue(layer.name());
        }
    }

    private void undo() {
        ClientProjectWorkspace.undo();
        ensureSelectedLayerExists();
        syncLayerFields();
        updateButtonStates();
    }

    private void redo() {
        ClientProjectWorkspace.redo();
        ensureSelectedLayerExists();
        syncLayerFields();
        updateButtonStates();
    }

    private void save() {
        try {
            ClientProjectWorkspace.save();
        } catch (IOException e) {
            LoomStudios.LOGGER.error("Failed to save Loom Elytra project", e);
        }
        updateButtonStates();
    }

    private void saveAndEquip() {
        try {
            ClientProjectWorkspace.saveAndEquip();
        } catch (IOException | IllegalStateException e) {
            LoomStudios.LOGGER.error(
                    "Failed to save/equip Loom Elytra project",
                    e
            );
        }
        updateButtonStates();
    }

    private void ensureSelectedPaintLayer() {
        ensureSelectedLayerExists();

        LoomLayer current = selectedLayer();
        if (current != null && current.kind() == LayerKind.PAINT) {
            return;
        }

        selectedLayerId = ClientProjectWorkspace.project()
                .elytra()
                .layers()
                .stream()
                .filter(layer -> layer.kind() == LayerKind.PAINT)
                .findFirst()
                .map(LoomLayer::id)
                .orElse(selectedLayerId);
    }

    private void ensureSelectedLayerExists() {
        if (!ClientProjectWorkspace.isInitialized()
                || ClientProjectWorkspace.project()
                        .elytra()
                        .layers()
                        .isEmpty()) {
            selectedLayerId = null;
            return;
        }

        boolean exists = selectedLayerId != null
                && findLayer(selectedLayerId) != null;
        if (!exists) {
            selectedLayerId = ClientProjectWorkspace.project()
                    .elytra()
                    .layers()
                    .getLast()
                    .id();
        }
    }

    private LoomLayer selectedLayer() {
        ensureSelectedLayerExists();
        return selectedLayerId == null
                ? null
                : findLayer(selectedLayerId);
    }

    private LoomLayer findLayer(UUID layerId) {
        if (layerId == null || !ClientProjectWorkspace.isInitialized()) {
            return null;
        }

        return ClientProjectWorkspace.project()
                .elytra()
                .layers()
                .stream()
                .filter(layer -> layer.id().equals(layerId))
                .findFirst()
                .orElse(null);
    }

    private void updateButtonStates() {
        if (!ClientProjectWorkspace.isInitialized()) {
            return;
        }

        CanvasResolution resolution = CanvasResolution.fromCanvas(
                ClientProjectWorkspace.project().elytra()
        );
        LoomLayer layer = selectedLayer();
        boolean editable = layer != null && layer.editableAsPaint();
        var layers = ClientProjectWorkspace.project()
                .elytra()
                .layers();
        int layerCount = layers.size();
        int selectedIndex = -1;
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).id().equals(selectedLayerId)) {
                selectedIndex = i;
                break;
            }
        }

        if (linkButton != null) {
            linkButton.setMessage(Component.literal(
                    linkedMirror
                            ? "Wings: Linked Mirror"
                            : "Wings: Separate"
            ));
        }
        if (previewStandingButton != null) {
            previewStandingButton.active =
                    previewPose
                            != LoomPlayerPreviewWidget.ElytraPose.STANDING;
        }
        if (previewOpenButton != null) {
            previewOpenButton.active =
                    previewPose
                            != LoomPlayerPreviewWidget.ElytraPose.OPEN;
        }
        if (previewGlidingButton != null) {
            previewGlidingButton.active =
                    previewPose
                            != LoomPlayerPreviewWidget.ElytraPose.GLIDING;
        }
        if (pencilButton != null) {
            pencilButton.active = editable && tool != Tool.PENCIL;
        }
        if (eraserButton != null) {
            eraserButton.active = editable && tool != Tool.ERASER;
        }
        if (brushDownButton != null) {
            brushDownButton.active = editable && brushSize > 1;
        }
        if (brushUpButton != null) {
            brushUpButton.active =
                    editable && brushSize < resolution.scale() * 8;
        }
        if (brushLabelButton != null) {
            brushLabelButton.setMessage(Component.literal(
                    "Brush " + brushSize
            ));
        }
        if (resolutionDownButton != null) {
            resolutionDownButton.active =
                    resolution != CanvasResolution.STANDARD;
        }
        if (resolutionUpButton != null) {
            resolutionUpButton.active =
                    resolution != CanvasResolution.ULTRA;
        }
        if (resolutionLabelButton != null) {
            resolutionLabelButton.setMessage(Component.literal(
                    resolution.label()
            ));
        }
        float thickness = ClientProjectWorkspace.project()
                .runtime()
                .elytraThickness();
        if (thicknessDownButton != null) {
            thicknessDownButton.active = thickness > 0.25F;
        }
        if (thicknessUpButton != null) {
            thicknessUpButton.active = thickness < 2.0F;
        }
        if (thicknessLabelButton != null) {
            thicknessLabelButton.setMessage(Component.literal(
                    "Depth " + Math.round(thickness * 100.0F) + "%"
            ));
        }
        if (lockButton != null) {
            lockButton.active = layer != null;
            lockButton.setMessage(Component.literal(
                    "Layer Lock: "
                            + (layer != null && layer.locked()
                                    ? "On"
                                    : "Off")
            ));
        }
        boolean hasLayerCapacity =
                layerCount
                        < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
        if (capeConversionButton != null) {
            capeConversionButton.active = hasLayerCapacity;
        }
        if (layerAddButton != null) {
            layerAddButton.active = hasLayerCapacity;
        }
        if (layerDuplicateButton != null) {
            layerDuplicateButton.active =
                    layer != null
                            && layerCount
                            < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
        }
        if (layerDeleteButton != null) {
            layerDeleteButton.active = layer != null && layerCount > 1;
        }
        if (layerUpButton != null) {
            layerUpButton.active =
                    selectedIndex >= 0 && selectedIndex < layerCount - 1;
        }
        if (layerDownButton != null) {
            layerDownButton.active = selectedIndex > 0;
        }
        if (layerRenameButton != null) {
            layerRenameButton.active = layer != null;
        }
        if (layerOpacityDownButton != null) {
            layerOpacityDownButton.active =
                    layer != null && layer.opacity() > 0.0F;
        }
        if (layerOpacityUpButton != null) {
            layerOpacityUpButton.active =
                    layer != null && layer.opacity() < 1.0F;
        }
        if (layerOpacityLabelButton != null) {
            layerOpacityLabelButton.setMessage(Component.literal(
                    layer == null
                            ? "Opacity"
                            : "Opacity "
                                    + Math.round(layer.opacity() * 100.0F)
                                    + "%"
            ));
        }
        if (layerBlendButton != null) {
            layerBlendButton.active = layer != null;
            layerBlendButton.setMessage(Component.literal(
                    layer == null
                            ? "Blend"
                            : "Blend: "
                                    + layer.blendMode().displayName()
            ));
        }
        if (undoButton != null) {
            undoButton.active = ClientProjectWorkspace.session().canUndo();
        }
        if (redoButton != null) {
            redoButton.active = ClientProjectWorkspace.session().canRedo();
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                shellLeft,
                shellTop,
                shellRight,
                shellBottom,
                LoomUiTheme.BACKDROP
        );
        graphics.fill(
                shellLeft + 1,
                shellTop + 1,
                shellRight - 1,
                shellBottom - 1,
                LoomUiTheme.PANEL
        );

        graphics.fill(
                shellLeft + 7,
                shellTop + 5,
                shellRight - 7,
                contentTop - 5,
                LoomUiTheme.PANEL_INNER
        );
        graphics.fill(
                shellLeft + 7,
                contentTop - 7,
                shellRight - 7,
                contentTop - 5,
                LoomUiTheme.ACCENT
        );

        graphics.drawString(
                this.font,
                Component.literal("Loom Studios"),
                shellLeft + 16,
                shellTop + 12,
                LoomUiTheme.TEXT,
                false
        );
        graphics.drawString(
                this.font,
                Component.literal("Elytra Editor"),
                shellLeft + 88,
                shellTop + 12,
                LoomUiTheme.ACCENT,
                false
        );

        graphics.drawString(
                this.font,
                Component.literal(
                        linkedMirror
                                ? "Linked Mirror"
                                : "Separate Wings"
                ),
                shellRight - this.font.width(
                        linkedMirror ? "Linked Mirror" : "Separate Wings"
                ) - 16,
                shellTop + 12,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void goBack() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        goBack();
    }

    @Override
    public void removed() {
        if (canvasWidget != null) {
            canvasWidget.close();
        }
        super.removed();
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
