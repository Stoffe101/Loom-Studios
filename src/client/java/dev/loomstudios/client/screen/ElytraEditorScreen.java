package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.LoomAnimationTimelineWidget;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomElytraCanvasWidget;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.client.ui.LoomLayerListWidget;
import dev.loomstudios.client.ui.LoomPaletteWindow;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomScreenChrome;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.AnimationAuthoring;
import dev.loomstudios.project.AnimationChannel;
import dev.loomstudios.project.AnimationEffectType;
import dev.loomstudios.project.AnimationEvaluator;
import dev.loomstudios.project.AnimationKeyframe;
import dev.loomstudios.project.AnimationTrack;
import dev.loomstudios.project.BlendMode;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeToElytraConverter;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.ElytraWing;
import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.LoomAnimation;
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

    private enum InspectorTab {
        LAYERS,
        COLOR,
        ANIMATION
    }

    private final Screen parent;

    private Tool tool = Tool.PENCIL;
    private int selectedColor = 0xFF22D7E8;
    private int brushSize = 1;
    private boolean linkedMirror = true;
    private UUID selectedLayerId;
    private UUID selectedTrackId;
    private int timelineTick;
    private double timelineCursor;
    private boolean timelinePlaying;

    private LoomElytraCanvasWidget canvasWidget;
    private LoomColorPickerWidget colorPicker;
    private LoomPlayerPreviewWidget previewWidget;
    private LoomLayerListWidget layerListWidget;
    private LoomAnimationTimelineWidget timelineWidget;
    private LoomPaletteWindow paletteWindow;
    private EditBox layerNameField;

    private boolean paletteWindowVisible;
    private boolean paletteWindowPinned;
    private int paletteWindowX = Integer.MIN_VALUE;
    private int paletteWindowY = Integer.MIN_VALUE;
    private String statusMessage = "";

    private LoomButton layerAddButton;
    private LoomButton layerDuplicateButton;
    private LoomButton layerDeleteButton;
    private LoomButton layerUpButton;
    private LoomButton layerDownButton;
    private LoomButton layerEditImageButton;
    private LoomButton layerOpacityDownButton;
    private LoomButton layerOpacityLabelButton;
    private LoomButton layerOpacityUpButton;
    private LoomButton layerBlendButton;
    private LoomButton swatchesButton;
    private LoomButton layerRenameButton;
    private LoomButton inspectorLayersButton;
    private LoomButton inspectorColorButton;
    private LoomButton inspectorAnimationButton;
    private LoomButton animationEffectButton;
    private LoomButton animationAddKeyButton;
    private LoomButton animationRemoveKeyButton;
    private LoomButton animationValueDownButton;
    private LoomButton animationValueLabelButton;
    private LoomButton animationValueUpButton;
    private LoomButton animationSpeedButton;
    private LoomButton animationDeleteButton;

    private LoomButton pencilButton;
    private LoomButton eraserButton;
    private LoomButton linkButton;
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
    private int contentBottom;
    private int toolRailLeft;
    private int toolRailRight;
    private int centerLeft;
    private int centerRight;
    private int canvasTop;
    private int canvasBottom;
    private int timelineTop;
    private int timelineBottom;
    private int rightPanelLeft;
    private int rightPanelRight;
    private int previewBottom;
    private int inspectorTop;
    private int inspectorBottom;
    private boolean compactMode;
    private InspectorTab inspectorTab = InspectorTab.LAYERS;

    public ElytraEditorScreen(Screen parent) {
        super(Component.literal("Loom Studios - Elytra Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ensureSelectedLayerExists();
        ensureSelectedTrackExists();

        this.compactMode = LoomUiTheme.compact(width, height);

        int margin = compactMode ? 4 : 8;
        int gap = compactMode ? 4 : 6;
        int headerHeight = LoomScreenChrome.headerHeight(compactMode);
        int navHeight = LoomScreenChrome.navHeight(compactMode);
        int footerHeight = 18;

        shellLeft = 0;
        shellTop = 0;
        shellRight = width;
        shellBottom = height;
        contentTop = headerHeight + navHeight + 4;
        contentBottom = height - footerHeight - 4;

        int toolRailWidth = compactMode ? 30 : 38;
        int rightWidth = compactMode
                ? Math.max(166, Math.min(184, width / 3))
                : Math.max(205, Math.min(235, width / 4));

        toolRailLeft = margin;
        toolRailRight = toolRailLeft + toolRailWidth;
        rightPanelRight = width - margin;
        rightPanelLeft = rightPanelRight - rightWidth;
        centerLeft = toolRailRight + gap;
        centerRight = rightPanelLeft - gap;

        int workspaceHeight = contentBottom - contentTop;
        int timelineHeight = compactMode
                ? Math.max(88, Math.min(104, workspaceHeight / 3))
                : Math.max(116, Math.min(148, workspaceHeight / 3));
        canvasTop = contentTop + (compactMode ? 22 : 25);
        timelineBottom = contentBottom;
        timelineTop = timelineBottom - timelineHeight;
        canvasBottom = timelineTop - gap;

        int previewHeight = compactMode
                ? 72
                : Math.max(100, Math.min(128, workspaceHeight / 3));
        previewBottom = contentTop + previewHeight;
        int tabHeight = compactMode ? 18 : 20;
        inspectorTop = previewBottom + tabHeight + 5;
        inspectorBottom = contentBottom;

        buildTopNavigation(headerHeight, navHeight, margin);
        buildToolRail();
        buildCanvasToolbar();
        buildCanvasAndTimeline();
        buildRightPanel(previewHeight, tabHeight);
        buildPaletteWindow(centerRight, contentTop);

        updateButtonStates();
        updateInspectorVisibility();
    }

    private void buildTopNavigation(
            int headerHeight,
            int navHeight,
            int margin
    ) {
        int y = headerHeight;
        int h = navHeight - 2;
        int normal = 72;
        int compact = 26;
        int x = margin;

        addRenderableWidget(navButton(
                x, y, compactMode ? compact : normal, h,
                "Home", LoomButton.Icon.HOME, false,
                this::goBack
        ));
        x += (compactMode ? compact : normal) + 3;

        addRenderableWidget(navButton(
                x, y, compactMode ? compact : normal, h,
                "Cape", LoomButton.Icon.CAPE, false,
                () -> minecraft.setScreen(new CapeEditorScreen(this))
        ));
        x += (compactMode ? compact : normal) + 3;

        LoomButton elytra = navButton(
                x, y, compactMode ? compact : normal, h,
                "Elytra", LoomButton.Icon.ELYTRA, true,
                () -> { }
        );
        elytra.active = false;
        addRenderableWidget(elytra);
        x += (compactMode ? compact : normal) + 3;

        addRenderableWidget(navButton(
                x, y, compactMode ? compact : normal, h,
                "Import", LoomButton.Icon.IMAGE, false,
                this::importImage
        ));
        x += (compactMode ? compact : normal) + 3;

        addRenderableWidget(navButton(
                x,
                y,
                compactMode ? compact : normal + 10,
                h,
                compactMode ? "Share" : "Share / Export",
                LoomButton.Icon.EXPORT,
                false,
                () -> minecraft.setScreen(
                        new LoomCodesScreen(
                                this,
                                ClientProjectWorkspace.project()
                        )
                )
        ));

        int action = compactMode ? 26 : 62;
        int right = width - margin;

        addRenderableWidget(navButton(
                right - action,
                y,
                action,
                h,
                compactMode ? "Equip" : "Save + Equip",
                LoomButton.Icon.EQUIP,
                false,
                this::saveAndEquip
        ));
        right -= action + 3;

        addRenderableWidget(navButton(
                right - action,
                y,
                action,
                h,
                "Save",
                LoomButton.Icon.SAVE,
                false,
                this::save
        ));
        right -= action + 3;

        int history = compactMode ? 24 : 42;
        redoButton = navButton(
                right - history,
                y,
                history,
                h,
                "Redo",
                LoomButton.Icon.REDO,
                false,
                this::redo
        );
        addRenderableWidget(redoButton);
        right -= history + 3;

        undoButton = navButton(
                right - history,
                y,
                history,
                h,
                "Undo",
                LoomButton.Icon.UNDO,
                false,
                this::undo
        );
        addRenderableWidget(undoButton);
    }

    private LoomButton navButton(
            int x,
            int y,
            int width,
            int height,
            String label,
            LoomButton.Icon icon,
            boolean selected,
            Runnable action
    ) {
        return new LoomButton(
                x,
                y,
                width,
                height,
                Component.literal(label),
                icon,
                compactMode,
                action
        ).setSelected(selected);
    }

    private LoomButton iconButton(
            int x,
            int y,
            int width,
            int height,
            String label,
            LoomButton.Icon icon,
            Runnable action
    ) {
        LoomButton button = new LoomButton(
                x,
                y,
                width,
                height,
                Component.literal(label),
                icon,
                compactMode,
                action
        );
        addRenderableWidget(button);
        return button;
    }

    private void buildToolRail() {
        int x = toolRailLeft + 2;
        int width = toolRailRight - toolRailLeft - 4;
        int h = compactMode ? 28 : 32;
        int gap = compactMode ? 4 : 5;
        int y = contentTop + 22;

        pencilButton = iconButton(
                x, y, width, h,
                "Pencil", LoomButton.Icon.PENCIL,
                () -> {
                    tool = Tool.PENCIL;
                    updateButtonStates();
                }
        );
        pencilButton.setIconOnly(true);
        y += h + gap;

        eraserButton = iconButton(
                x, y, width, h,
                "Eraser", LoomButton.Icon.ERASER,
                () -> {
                    tool = Tool.ERASER;
                    updateButtonStates();
                }
        );
        eraserButton.setIconOnly(true);
        y += h + gap;

        swatchesButton = iconButton(
                x, y, width, h,
                "Swatches", LoomButton.Icon.PALETTE,
                this::toggleSwatches
        );
        swatchesButton.setIconOnly(true);
        y += h + gap;

        lockButton = iconButton(
                x, y, width, h,
                "Layer Lock", LoomButton.Icon.LOCK,
                this::toggleLock
        );
        lockButton.setIconOnly(true);
    }

    private void buildCanvasToolbar() {
        int y = contentTop;
        int h = compactMode ? 19 : 22;
        int gap = 3;
        int x = centerLeft;
        int small = compactMode ? 22 : 28;

        linkButton = iconButton(
                x,
                y,
                compactMode ? 82 : 118,
                h,
                linkedMirror ? "Linked Mirror" : "Separate Wings",
                LoomButton.Icon.ELYTRA,
                this::toggleLinked
        );
        linkButton.setIconOnly(false);
        x += linkButton.getWidth() + gap;

        resolutionDownButton = iconButton(
                x, y, small, h,
                "Resolution -", LoomButton.Icon.DOWN,
                () -> changeResolution(-1)
        );
        resolutionDownButton.setIconOnly(true);
        x += small + gap;

        resolutionLabelButton = new LoomButton(
                x,
                y,
                compactMode ? 46 : 62,
                h,
                Component.literal("1x"),
                () -> { }
        );
        resolutionLabelButton.active = false;
        addRenderableWidget(resolutionLabelButton);
        x += resolutionLabelButton.getWidth() + gap;

        resolutionUpButton = iconButton(
                x, y, small, h,
                "Resolution +", LoomButton.Icon.UP,
                () -> changeResolution(1)
        );
        resolutionUpButton.setIconOnly(true);

        int right = centerRight;
        thicknessUpButton = iconButton(
                right - small,
                y,
                small,
                h,
                "Thickness +",
                LoomButton.Icon.UP,
                () -> changeThickness(0.25F)
        );
        thicknessUpButton.setIconOnly(true);
        right -= small + gap;

        thicknessLabelButton = new LoomButton(
                right - (compactMode ? 62 : 82),
                y,
                compactMode ? 62 : 82,
                h,
                Component.literal("Depth 100%"),
                () -> { }
        );
        thicknessLabelButton.active = false;
        addRenderableWidget(thicknessLabelButton);
        right -= thicknessLabelButton.getWidth() + gap;

        thicknessDownButton = iconButton(
                right - small,
                y,
                small,
                h,
                "Thickness -",
                LoomButton.Icon.DOWN,
                () -> changeThickness(-0.25F)
        );
        thicknessDownButton.setIconOnly(true);
    }

    private void buildCanvasAndTimeline() {
        this.canvasWidget = new LoomElytraCanvasWidget(
                centerLeft,
                canvasTop,
                centerRight - centerLeft,
                Math.max(80, canvasBottom - canvasTop),
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
        addRenderableWidget(canvasWidget);

        int contextH = compactMode ? 19 : 22;
        int contextY = canvasBottom - contextH - 3;
        int small = compactMode ? 22 : 28;
        int gap = 3;

        brushDownButton = iconButton(
                centerLeft + 5,
                contextY,
                small,
                contextH,
                "Brush -",
                LoomButton.Icon.DOWN,
                () -> changeBrush(-1)
        );
        brushDownButton.setIconOnly(true);

        brushLabelButton = new LoomButton(
                centerLeft + 5 + small + gap,
                contextY,
                compactMode ? 58 : 76,
                contextH,
                Component.literal("Brush 1"),
                () -> { }
        );
        brushLabelButton.active = false;
        addRenderableWidget(brushLabelButton);

        brushUpButton = iconButton(
                centerLeft
                        + 5
                        + small
                        + gap
                        + brushLabelButton.getWidth()
                        + gap,
                contextY,
                small,
                contextH,
                "Brush +",
                LoomButton.Icon.UP,
                () -> changeBrush(1)
        );
        brushUpButton.setIconOnly(true);

        this.timelineWidget = new LoomAnimationTimelineWidget(
                centerLeft,
                timelineTop,
                centerRight - centerLeft,
                timelineBottom - timelineTop,
                ClientProjectWorkspace::project,
                AnimationChannel.ELYTRA,
                () -> selectedTrackId,
                () -> timelineTick,
                () -> timelinePlaying,
                new LoomAnimationTimelineWidget.Controller() {
                    @Override
                    public void togglePlayback() {
                        toggleTimelinePlayback();
                    }

                    @Override
                    public void toggleTimelineLoop() {
                        ElytraEditorScreen.this.toggleTimelineLoop();
                    }

                    @Override
                    public void changeDuration(int deltaTicks) {
                        changeTimelineDuration(deltaTicks);
                    }

                    @Override
                    public void changePlaybackSpeed(float delta) {
                        changeTimelinePlaybackSpeed(delta);
                    }

                    @Override
                    public void scrubTo(int tick) {
                        scrubTimeline(tick);
                    }

                    @Override
                    public void addTrack() {
                        addAnimationTrack();
                    }

                    @Override
                    public void selectTrack(UUID trackId) {
                        selectAnimationTrack(trackId);
                    }

                    @Override
                    public void toggleTrack(UUID trackId) {
                        toggleAnimationTrack(trackId);
                    }

                    @Override
                    public void cycleEffect(UUID trackId) {
                        cycleAnimationEffect(trackId);
                    }

                    @Override
                    public void addKeyframe(UUID trackId) {
                        addAnimationKeyframe(trackId);
                    }

                    @Override
                    public void removeKeyframe(UUID trackId) {
                        removeAnimationKeyframe(trackId);
                    }

                    @Override
                    public void adjustKeyframeValue(
                            UUID trackId,
                            float delta
                    ) {
                        adjustAnimationKeyframeValue(
                                trackId,
                                delta
                        );
                    }

                    @Override
                    public void cycleTrackSpeed(UUID trackId) {
                        cycleAnimationTrackSpeed(trackId);
                    }

                    @Override
                    public void deleteTrack(UUID trackId) {
                        deleteAnimationTrack(trackId);
                    }
                },
                false
        );
        addRenderableWidget(timelineWidget);
    }

    private void buildRightPanel(
            int previewHeight,
            int tabHeight
    ) {
        int width = rightPanelRight - rightPanelLeft;

        previewWidget = new LoomPlayerPreviewWidget(
                rightPanelLeft + 2,
                contentTop + 20,
                width - 4,
                Math.max(42, previewHeight - 22),
                ClientProjectWorkspace::project,
                LoomPlayerPreviewWidget.Mode.ELYTRA
        );
        previewWidget.setTimelineTickSupplier(() -> timelineTick);
        addRenderableWidget(previewWidget);

        int tabY = previewBottom + 3;
        int tabGap = 3;
        int tabWidth = Math.max(42, (width - tabGap * 2) / 3);

        inspectorLayersButton = navButton(
                rightPanelLeft,
                tabY,
                tabWidth,
                tabHeight,
                "Layers",
                LoomButton.Icon.LAYERS,
                true,
                () -> setInspectorTab(InspectorTab.LAYERS)
        );
        addRenderableWidget(inspectorLayersButton);

        inspectorColorButton = navButton(
                rightPanelLeft + tabWidth + tabGap,
                tabY,
                tabWidth,
                tabHeight,
                "Color",
                LoomButton.Icon.PALETTE,
                false,
                () -> setInspectorTab(InspectorTab.COLOR)
        );
        addRenderableWidget(inspectorColorButton);

        inspectorAnimationButton = navButton(
                rightPanelLeft + (tabWidth + tabGap) * 2,
                tabY,
                width - (tabWidth + tabGap) * 2,
                tabHeight,
                "Anim",
                LoomButton.Icon.PLAY,
                false,
                () -> setInspectorTab(InspectorTab.ANIMATION)
        );
        addRenderableWidget(inspectorAnimationButton);

        buildLayerInspector();
        buildColorInspector();
        buildAnimationInspector();
    }

    private void buildLayerInspector() {
        int left = rightPanelLeft + 4;
        int width = rightPanelRight - rightPanelLeft - 8;
        int y = inspectorTop + 3;
        int h = compactMode ? 17 : 20;
        int gap = compactMode ? 2 : 3;

        int listHeight = compactMode ? 66 : 94;
        layerListWidget = new LoomLayerListWidget(
                left,
                y,
                width,
                listHeight,
                () -> ClientProjectWorkspace.project().elytra(),
                () -> selectedLayerId,
                this::selectLayer,
                this::toggleLayerVisibility,
                this::toggleLayerLock
        );
        addRenderableWidget(layerListWidget);
        y += listHeight + gap;

        int third = Math.max(28, (width - gap * 2) / 3);
        layerAddButton = iconButton(
                left, y, third, h,
                "Add", LoomButton.Icon.PLUS,
                this::addLayer
        );
        layerAddButton.setIconOnly(compactMode);

        layerDuplicateButton = iconButton(
                left + third + gap, y, third, h,
                "Copy", LoomButton.Icon.COPY,
                this::duplicateLayer
        );
        layerDuplicateButton.setIconOnly(compactMode);

        layerDeleteButton = iconButton(
                left + (third + gap) * 2,
                y,
                width - third * 2 - gap * 2,
                h,
                "Delete",
                LoomButton.Icon.DELETE,
                this::deleteLayer
        );
        layerDeleteButton
                .setIconOnly(compactMode)
                .setDanger(true);
        y += h + gap;

        int quarter = Math.max(24, (width - gap * 3) / 4);
        layerUpButton = iconButton(
                left, y, quarter, h,
                "Up", LoomButton.Icon.UP,
                () -> moveLayer(1)
        );
        layerUpButton.setIconOnly(true);

        layerDownButton = iconButton(
                left + quarter + gap, y, quarter, h,
                "Down", LoomButton.Icon.DOWN,
                () -> moveLayer(-1)
        );
        layerDownButton.setIconOnly(true);

        layerEditImageButton = iconButton(
                left + (quarter + gap) * 2,
                y,
                width - quarter * 2 - gap * 2,
                h,
                "Edit Image",
                LoomButton.Icon.IMAGE,
                this::editSelectedImage
        );
        layerEditImageButton.setIconOnly(compactMode);
        y += h + gap;

        int renameWidth = compactMode ? 44 : 52;
        layerNameField = new EditBox(
                font,
                left,
                y,
                Math.max(48, width - renameWidth - gap),
                h,
                Component.literal("Layer name")
        );
        layerNameField.setMaxLength(
                dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS
        );
        layerNameField.setHint(Component.literal("Layer name"));
        addRenderableWidget(layerNameField);

        layerRenameButton = iconButton(
                left + width - renameWidth,
                y,
                renameWidth,
                h,
                "Rename",
                LoomButton.Icon.SETTINGS,
                this::renameLayer
        );
        layerRenameButton.setIconOnly(compactMode);
        y += h + gap;

        int small = compactMode ? 22 : 28;
        layerOpacityDownButton = iconButton(
                left, y, small, h,
                "Opacity -", LoomButton.Icon.DOWN,
                () -> changeLayerOpacity(-0.1F)
        );
        layerOpacityDownButton.setIconOnly(true);

        int labelWidth = width - small * 2 - gap * 2;
        layerOpacityLabelButton = new LoomButton(
                left + small + gap,
                y,
                labelWidth,
                h,
                Component.literal("Opacity 100%"),
                () -> { }
        );
        layerOpacityLabelButton.active = false;
        addRenderableWidget(layerOpacityLabelButton);

        layerOpacityUpButton = iconButton(
                left + small + gap + labelWidth + gap,
                y,
                small,
                h,
                "Opacity +",
                LoomButton.Icon.UP,
                () -> changeLayerOpacity(0.1F)
        );
        layerOpacityUpButton.setIconOnly(true);
        y += h + gap;

        int half = (width - gap) / 2;
        layerBlendButton = iconButton(
                left, y, half, h,
                "Blend: Normal", LoomButton.Icon.LAYERS,
                this::cycleLayerBlendMode
        );
        layerBlendButton.setIconOnly(false);

        iconButton(
                left + half + gap,
                y,
                width - half - gap,
                h,
                "Import",
                LoomButton.Icon.IMAGE,
                this::importImage
        ).setIconOnly(compactMode);
        y += h + gap;

        iconButton(
                left,
                y,
                width,
                h,
                compactMode ? "Cape → Wings" : "Convert Cape to Wings",
                LoomButton.Icon.ELYTRA,
                this::convertCapeToElytra
        ).setIconOnly(false);
    }

    private void buildColorInspector() {
        int left = rightPanelLeft + 4;
        int width = rightPanelRight - rightPanelLeft - 8;
        int y = inspectorTop + 3;

        colorPicker = new LoomColorPickerWidget(
                left,
                y,
                width,
                compactMode ? 118 : 150,
                selectedColor,
                this::setSelectedColor
        );
        addRenderableWidget(colorPicker);

        int buttonY = y + (compactMode ? 121 : 153);
        swatchesButton = iconButton(
                left,
                buttonY,
                width,
                compactMode ? 18 : 20,
                "Swatches",
                LoomButton.Icon.PALETTE,
                this::toggleSwatches
        );
        swatchesButton.setIconOnly(false);
    }

    private void buildAnimationInspector() {
        int left = rightPanelLeft + 4;
        int width = rightPanelRight - rightPanelLeft - 8;
        int y = inspectorTop + 3;
        int h = compactMode ? 18 : 20;
        int gap = compactMode ? 3 : 4;

        animationEffectButton = iconButton(
                left,
                y,
                width,
                h,
                "Effect: Select a track",
                LoomButton.Icon.EMISSIVE,
                () -> withSelectedTrack(
                        track -> cycleAnimationEffect(track.id())
                )
        );
        animationEffectButton.setIconOnly(false);
        y += h + gap;

        int half = (width - gap) / 2;
        animationAddKeyButton = iconButton(
                left,
                y,
                half,
                h,
                compactMode ? "Add Key" : "Add Keyframe Here",
                LoomButton.Icon.PLUS,
                () -> withSelectedTrack(
                        track -> addAnimationKeyframe(track.id())
                )
        );
        animationAddKeyButton.setIconOnly(false);

        animationRemoveKeyButton = iconButton(
                left + half + gap,
                y,
                width - half - gap,
                h,
                compactMode ? "Remove Key" : "Remove Nearest Key",
                LoomButton.Icon.DELETE,
                () -> withSelectedTrack(
                        track -> removeAnimationKeyframe(track.id())
                )
        );
        animationRemoveKeyButton.setIconOnly(false);
        y += h + gap;

        int small = compactMode ? 26 : 32;
        animationValueDownButton = iconButton(
                left,
                y,
                small,
                h,
                "Value -",
                LoomButton.Icon.DOWN,
                () -> withSelectedTrack(
                        track -> adjustAnimationKeyframeValue(
                                track.id(),
                                -0.1F
                        )
                )
        );
        animationValueDownButton.setIconOnly(true);

        int labelWidth = width - small * 2 - gap * 2;
        animationValueLabelButton = new LoomButton(
                left + small + gap,
                y,
                labelWidth,
                h,
                Component.literal("Value"),
                () -> { }
        );
        animationValueLabelButton.active = false;
        addRenderableWidget(animationValueLabelButton);

        animationValueUpButton = iconButton(
                left + small + gap + labelWidth + gap,
                y,
                small,
                h,
                "Value +",
                LoomButton.Icon.UP,
                () -> withSelectedTrack(
                        track -> adjustAnimationKeyframeValue(
                                track.id(),
                                0.1F
                        )
                )
        );
        animationValueUpButton.setIconOnly(true);
        y += h + gap;

        animationSpeedButton = iconButton(
                left,
                y,
                width,
                h,
                "Track Speed",
                LoomButton.Icon.PLAY,
                () -> withSelectedTrack(
                        track -> cycleAnimationTrackSpeed(track.id())
                )
        );
        animationSpeedButton.setIconOnly(false);
        y += h + gap;

        animationDeleteButton = iconButton(
                left,
                y,
                width,
                h,
                "Delete Animation Track",
                LoomButton.Icon.DELETE,
                () -> withSelectedTrack(
                        track -> deleteAnimationTrack(track.id())
                )
        );
        animationDeleteButton
                .setDanger(true)
                .setIconOnly(false);
    }

    private void withSelectedTrack(
            java.util.function.Consumer<AnimationTrack> action
    ) {
        AnimationTrack track = findAnimationTrack(selectedTrackId);
        if (track != null) {
            action.accept(track);
        }
    }

    private void setInspectorTab(InspectorTab tab) {
        inspectorTab = tab;
        updateInspectorVisibility();
    }

    private void updateInspectorVisibility() {
        boolean layers = inspectorTab == InspectorTab.LAYERS;
        boolean color = inspectorTab == InspectorTab.COLOR;
        boolean animation = inspectorTab == InspectorTab.ANIMATION;

        if (inspectorLayersButton != null) {
            inspectorLayersButton.setSelected(layers);
        }
        if (inspectorColorButton != null) {
            inspectorColorButton.setSelected(color);
        }
        if (inspectorAnimationButton != null) {
            inspectorAnimationButton.setSelected(animation);
        }

        if (layerListWidget != null) layerListWidget.visible = layers;
        if (layerAddButton != null) layerAddButton.visible = layers;
        if (layerDuplicateButton != null) layerDuplicateButton.visible = layers;
        if (layerDeleteButton != null) layerDeleteButton.visible = layers;
        if (layerUpButton != null) layerUpButton.visible = layers;
        if (layerDownButton != null) layerDownButton.visible = layers;
        if (layerEditImageButton != null) layerEditImageButton.visible = layers;
        if (layerNameField != null) layerNameField.setVisible(layers);
        if (layerRenameButton != null) layerRenameButton.visible = layers;
        if (layerOpacityDownButton != null) layerOpacityDownButton.visible = layers;
        if (layerOpacityLabelButton != null) layerOpacityLabelButton.visible = layers;
        if (layerOpacityUpButton != null) layerOpacityUpButton.visible = layers;
        if (layerBlendButton != null) layerBlendButton.visible = layers;

        if (colorPicker != null) colorPicker.visible = color;
        if (swatchesButton != null) swatchesButton.visible = color;

        LoomButton[] animationButtons = {
                animationEffectButton,
                animationAddKeyButton,
                animationRemoveKeyButton,
                animationValueDownButton,
                animationValueLabelButton,
                animationValueUpButton,
                animationSpeedButton,
                animationDeleteButton
        };
        for (LoomButton button : animationButtons) {
            if (button != null) {
                button.visible = animation;
            }
        }
    }

    private void toggleTimelinePlayback() {
        LoomAnimation animation =
                ClientProjectWorkspace.project().animation();

        if (timelineTick >= animation.durationTicks()) {
            scrubTimeline(0);
        }

        timelinePlaying = !timelinePlaying;
        statusMessage = timelinePlaying
                ? "Animation preview playing"
                : "Animation preview paused";
    }

    private void scrubTimeline(int tick) {
        LoomAnimation animation =
                ClientProjectWorkspace.project().animation();
        timelineTick = Math.max(
                0,
                Math.min(animation.durationTicks(), tick)
        );
        timelineCursor = timelineTick;
    }

    private void toggleTimelineLoop() {
        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        project.animation().withLoop(
                                !project.animation().loop()
                        )
                )
        );
        updateButtonStates();
    }

    private void changeTimelineDuration(int deltaTicks) {
        LoomProjectSnapshot snapshot = animationSnapshot();
        int requested = snapshot.animation().durationTicks()
                + deltaTicks;

        var result = ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.changeDuration(
                                project.animation(),
                                requested
                        )
                )
        );

        timelineTick = Math.min(
                timelineTick,
                result.animation().durationTicks()
        );
        timelineCursor = timelineTick;
        updateButtonStates();
    }

    private void changeTimelinePlaybackSpeed(float delta) {
        LoomAnimation animation =
                ClientProjectWorkspace.project().animation();
        float next = Math.max(
                0.25F,
                Math.min(
                        4.0F,
                        animation.playbackSpeed() + delta
                )
        );

        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        project.animation().withPlaybackSpeed(next)
                )
        );
        updateButtonStates();
    }

    private void addAnimationTrack() {
        LoomLayer layer = selectedLayer();
        if (layer == null) {
            statusMessage = "Select an Elytra layer first";
            return;
        }

        var result = ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.addTrack(
                                project.animation(),
                                layer.id(),
                                AnimationChannel.ELYTRA,
                                AnimationEffectType.PULSE
                        )
                )
        );

        selectedTrackId = result.animation().tracks().getLast().id();
        scrubTimeline(0);
        statusMessage = "Added Pulse animation track";
        updateButtonStates();
    }

    private void selectAnimationTrack(UUID trackId) {
        if (findAnimationTrack(trackId) == null) {
            return;
        }
        selectedTrackId = trackId;
        updateButtonStates();
    }

    private void toggleAnimationTrack(UUID trackId) {
        AnimationTrack track = findAnimationTrack(trackId);
        if (track == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.replaceTrack(
                                project.animation(),
                                track.withEnabled(!track.enabled())
                        )
                )
        );
        updateButtonStates();
    }

    private void cycleAnimationEffect(UUID trackId) {
        AnimationTrack track = findAnimationTrack(trackId);
        if (track == null) {
            return;
        }

        AnimationEffectType next = track.effect().next();
        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.replaceTrack(
                                project.animation(),
                                track.withEffect(next)
                        )
                )
        );
        statusMessage = "Animation: " + next.displayName();
        updateButtonStates();
    }

    private void addAnimationKeyframe(UUID trackId) {
        AnimationTrack track = findAnimationTrack(trackId);
        if (track == null) {
            return;
        }

        LoomAnimation animation =
                ClientProjectWorkspace.project().animation();
        float value = AnimationEvaluator.valueAt(
                track,
                animation,
                timelineTick
        );
        AnimationTrack updated =
                AnimationAuthoring.addOrReplaceKeyframe(
                        track,
                        timelineTick,
                        value
                );

        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.replaceTrack(
                                project.animation(),
                                updated
                        )
                )
        );
        statusMessage = "Keyframe set at "
                + timelineTick
                + " ticks";
        updateButtonStates();
    }

    private void removeAnimationKeyframe(UUID trackId) {
        AnimationTrack track = findAnimationTrack(trackId);
        if (track == null || track.keyframes().size() <= 1) {
            return;
        }

        int targetTick = nearestKeyframeTick(
                track,
                timelineTick
        );
        AnimationTrack updated =
                AnimationAuthoring.removeKeyframe(
                        track,
                        targetTick
                );

        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.replaceTrack(
                                project.animation(),
                                updated
                        )
                )
        );
        scrubTimeline(targetTick);
        statusMessage = "Removed nearest keyframe";
        updateButtonStates();
    }

    private void adjustAnimationKeyframeValue(
            UUID trackId,
            float delta
    ) {
        AnimationTrack track = findAnimationTrack(trackId);
        if (track == null) {
            return;
        }

        LoomAnimation animation =
                ClientProjectWorkspace.project().animation();
        float current = track.keyframes().stream()
                .filter(frame -> frame.tick() == timelineTick)
                .findFirst()
                .map(AnimationKeyframe::value)
                .orElseGet(() ->
                        AnimationEvaluator.valueAt(
                                track,
                                animation,
                                timelineTick
                        )
                );

        float next = Math.max(
                LoomAnimation.MIN_KEYFRAME_VALUE,
                Math.min(
                        LoomAnimation.MAX_KEYFRAME_VALUE,
                        current + delta
                )
        );

        AnimationTrack updated =
                AnimationAuthoring.addOrReplaceKeyframe(
                        track,
                        timelineTick,
                        next
                );

        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.replaceTrack(
                                project.animation(),
                                updated
                        )
                )
        );
        statusMessage = String.format(
                java.util.Locale.ROOT,
                "Keyframe value %.2f",
                next
        );
        updateButtonStates();
    }

    private void cycleAnimationTrackSpeed(UUID trackId) {
        AnimationTrack track = findAnimationTrack(trackId);
        if (track == null) {
            return;
        }

        float current = track.speed();
        float next;
        if (current < 0.75F) {
            next = 1.0F;
        } else if (current < 1.25F) {
            next = 1.5F;
        } else if (current < 1.75F) {
            next = 2.0F;
        } else {
            next = 0.5F;
        }

        AnimationTrack updated = track.withSpeed(next);
        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.replaceTrack(
                                project.animation(),
                                updated
                        )
                )
        );
        updateButtonStates();
    }

    private void deleteAnimationTrack(UUID trackId) {
        if (findAnimationTrack(trackId) == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                project.withAnimation(
                        AnimationAuthoring.removeTrack(
                                project.animation(),
                                trackId
                        )
                )
        );
        selectedTrackId = null;
        ensureSelectedTrackExists();
        statusMessage = "Animation track deleted";
        updateButtonStates();
    }

    private AnimationTrack findAnimationTrack(UUID trackId) {
        if (trackId == null || !ClientProjectWorkspace.isInitialized()) {
            return null;
        }

        return ClientProjectWorkspace.project()
                .animation()
                .tracks()
                .stream()
                .filter(track ->
                        track.channel() == AnimationChannel.ELYTRA
                                && track.id().equals(trackId)
                )
                .findFirst()
                .orElse(null);
    }

    private void ensureSelectedTrackExists() {
        if (!ClientProjectWorkspace.isInitialized()) {
            selectedTrackId = null;
            return;
        }

        if (findAnimationTrack(selectedTrackId) != null) {
            return;
        }

        selectedTrackId = ClientProjectWorkspace.project()
                .animation()
                .tracks()
                .stream()
                .filter(track ->
                        track.channel() == AnimationChannel.ELYTRA
                )
                .findFirst()
                .map(AnimationTrack::id)
                .orElse(null);
    }

    private static int nearestKeyframeTick(
            AnimationTrack track,
            int tick
    ) {
        int nearest = track.keyframes().getFirst().tick();
        int distance = Math.abs(nearest - tick);

        for (AnimationKeyframe frame : track.keyframes()) {
            int candidateDistance = Math.abs(
                    frame.tick() - tick
            );
            if (candidateDistance < distance) {
                distance = candidateDistance;
                nearest = frame.tick();
            }
        }

        return nearest;
    }

    private record LoomProjectSnapshot(
            LoomAnimation animation
    ) {
    }

    private LoomProjectSnapshot animationSnapshot() {
        return new LoomProjectSnapshot(
                ClientProjectWorkspace.project().animation()
        );
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
        selectedTrackId = ClientProjectWorkspace.project()
                .animation()
                .tracks()
                .stream()
                .filter(track ->
                        track.channel() == AnimationChannel.ELYTRA
                                && track.layerId().equals(layerId)
                )
                .findFirst()
                .map(AnimationTrack::id)
                .orElse(selectedTrackId);
        syncLayerName();
        updateButtonStates();
    }

    private void addLayer() {
        var result = ClientProjectWorkspace.apply(project ->
                ProjectEdits.addElytraLayer(project, "Wing Layer")
        );
        selectedLayerId = result.elytra().layers().getLast().id();
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
        updateButtonStates();
    }

    private void moveLayer(int delta) {
        ensureSelectedLayerExists();
        if (selectedLayerId == null) {
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
        LoomLayer layer = selectedLayer();
        if (layer == null || layerNameField == null) {
            return;
        }

        String name = layerNameField.getValue().trim();
        if (name.isEmpty()) {
            syncLayerName();
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
            statusMessage = "Renamed layer";
        } catch (IllegalArgumentException e) {
            statusMessage = "Invalid layer name";
        }

        syncLayerName();
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

    private void importImage() {
        this.minecraft.setScreen(SmartImportScreen.forElytra(this));
    }

    private void editSelectedImage() {
        LoomLayer layer = selectedLayer();
        if (layer == null
                || layer.kind() != LayerKind.IMAGE
                || layer.locked()) {
            return;
        }

        this.minecraft.setScreen(
                SmartImportScreen.forElytraLayer(
                        this,
                        layer.id(),
                        layer
                )
        );
    }

    private void convertCapeToElytra() {
        var project = ClientProjectWorkspace.project();
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        int[] outside = LoomTextureCompiler.compileCapeRegion(
                project.cape(),
                CapeUvRegion.OUTSIDE,
                0,
                false,
                false
        );
        int[] converted = CapeToElytraConverter.convertOutsideFace(
                outside,
                CapeUvRegion.OUTSIDE.width(scale),
                CapeUvRegion.OUTSIDE.height(scale),
                project.elytra()
        );

        var result = ClientProjectWorkspace.apply(current ->
                ProjectEdits.addElytraPaintLayer(
                        current,
                        "Cape Conversion",
                        converted
                )
        );
        selectedLayerId = result.elytra().layers().getLast().id();
        statusMessage = "Cape Outside converted to editable wings";
        syncLayerName();
        updateButtonStates();
    }

    private void buildPaletteWindow(int anchorRight, int anchorTop) {
        if (this.paletteWindow != null) {
            this.paletteWindowX = this.paletteWindow.getX();
            this.paletteWindowY = this.paletteWindow.getY();
            this.paletteWindowPinned = this.paletteWindow.pinned();
        }

        boolean compact = this.width <= 700 || this.height <= 420;
        int width = compact
                ? Math.min(184, Math.max(164, this.width / 4 + 16))
                : 224;
        int height = compact
                ? Math.min(230, Math.max(186, this.height - 50))
                : Math.min(298, Math.max(250, this.height - 32));

        int defaultX = Math.max(8, anchorRight - width - 8);
        int defaultY = Math.max(8, anchorTop);

        int x = paletteWindowX == Integer.MIN_VALUE
                ? defaultX
                : paletteWindowX;
        int y = paletteWindowY == Integer.MIN_VALUE
                ? defaultY
                : paletteWindowY;

        this.paletteWindow = new LoomPaletteWindow(
                x,
                y,
                width,
                height,
                paletteWindowPinned,
                () -> selectedColor,
                this::setSelectedColor,
                message -> statusMessage = message,
                () -> this.width,
                () -> this.height
        );
        this.paletteWindow.visible = paletteWindowVisible;
        addRenderableWidget(this.paletteWindow);
        this.paletteWindow.moveTo(x, y);
    }

    private void toggleSwatches() {
        paletteWindowVisible = !paletteWindowVisible;
        if (paletteWindow != null) {
            paletteWindow.visible = paletteWindowVisible;
        }
        updateButtonStates();
    }

    private void setSelectedColor(int color) {
        selectedColor = color;
        if (colorPicker != null && colorPicker.color() != color) {
            colorPicker.setColor(color);
        }
    }

    private void syncLayerName() {
        if (layerNameField == null || layerNameField.isFocused()) {
            return;
        }

        LoomLayer layer = selectedLayer();
        layerNameField.setValue(layer == null ? "" : layer.name());
    }

    private void undo() {
        ClientProjectWorkspace.undo();
        ensureSelectedLayerExists();
        ensureSelectedTrackExists();
        clampTimelineAfterHistory();
        updateButtonStates();
    }

    private void redo() {
        ClientProjectWorkspace.redo();
        ensureSelectedLayerExists();
        ensureSelectedTrackExists();
        clampTimelineAfterHistory();
        updateButtonStates();
    }

    private void clampTimelineAfterHistory() {
        int duration = ClientProjectWorkspace.project()
                .animation()
                .durationTicks();
        timelineTick = Math.min(timelineTick, duration);
        timelineCursor = timelineTick;
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

        ensureSelectedTrackExists();

        CanvasResolution resolution = CanvasResolution.fromCanvas(
                ClientProjectWorkspace.project().elytra()
        );
        LoomLayer layer = selectedLayer();
        boolean editable = layer != null && layer.editableAsPaint();
        int layerCount = ClientProjectWorkspace.project()
                .elytra()
                .layers()
                .size();

        if (linkButton != null) {
            linkButton.setMessage(Component.literal(
                    linkedMirror
                            ? "Wings: Linked Mirror"
                            : "Wings: Separate"
            ));
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
        if (layerAddButton != null) {
            layerAddButton.active =
                    layerCount < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
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

        int selectedIndex = -1;
        if (layer != null) {
            var layers = ClientProjectWorkspace.project()
                    .elytra()
                    .layers();
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i).id().equals(layer.id())) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        if (layerUpButton != null) {
            layerUpButton.active =
                    selectedIndex >= 0 && selectedIndex < layerCount - 1;
        }
        if (layerDownButton != null) {
            layerDownButton.active = selectedIndex > 0;
        }
        if (layerEditImageButton != null) {
            layerEditImageButton.active =
                    layer != null
                            && layer.kind() == LayerKind.IMAGE
                            && !layer.locked();
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
                                    + Math.round(
                                            layer.opacity() * 100.0F
                                    )
                                    + "%"
            ));
        }
        if (layerBlendButton != null) {
            layerBlendButton.active = layer != null;
            layerBlendButton.setMessage(Component.literal(
                    layer == null
                            ? "Blend"
                            : "Blend: " + layer.blendMode().displayName()
            ));
        }
        if (swatchesButton != null) {
            swatchesButton.setMessage(Component.literal(
                    paletteWindowVisible
                            ? "Hide Swatches"
                            : "Swatches"
            ));
        }
        syncLayerName();

        if (undoButton != null) {
            undoButton.active = ClientProjectWorkspace.session().canUndo();
        }
        if (redoButton != null) {
            redoButton.active = ClientProjectWorkspace.session().canRedo();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!timelinePlaying
                || !ClientProjectWorkspace.isInitialized()) {
            return;
        }

        LoomAnimation animation =
                ClientProjectWorkspace.project().animation();

        timelineCursor += animation.playbackSpeed();

        if (timelineCursor > animation.durationTicks()) {
            if (animation.loop()) {
                timelineCursor %= animation.durationTicks();
            } else {
                timelineCursor = animation.durationTicks();
                timelinePlaying = false;
            }
        }

        timelineTick = Math.max(
                0,
                Math.min(
                        animation.durationTicks(),
                        (int)Math.floor(timelineCursor)
                )
        );
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

        if (!statusMessage.isBlank()) {
            graphics.drawString(
                    this.font,
                    Component.literal(statusMessage),
                    shellLeft + 16,
                    shellTop + 24,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

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
        if (paletteWindow != null) {
            paletteWindowX = paletteWindow.getX();
            paletteWindowY = paletteWindow.getY();
            paletteWindowPinned = paletteWindow.pinned();
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
