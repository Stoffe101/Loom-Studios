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
        int timelineHeight = Math.max(
                110,
                Math.min(
                        154,
                        (int)Math.round(centerHeight * 0.38)
                )
        );
        int canvasHeight = Math.max(
                92,
                centerHeight - timelineHeight - gap
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

        ensureSelectedTrackExists();
        this.timelineWidget = new LoomAnimationTimelineWidget(
                centerLeft,
                contentTop + canvasHeight + gap,
                centerRight - centerLeft,
                timelineHeight,
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
                }
        );
        addRenderableWidget(this.timelineWidget);

        int rightWidth = previewRight - previewLeft;
        int rightHeight = shellBottom - contentTop - 7;
        int layerListHeight = Math.max(
                68,
                Math.min(90, rightHeight / 4)
        );
        int actionHeight = 20;
        int propertyRowsHeight = 20 * 6 + 5 * 5;
        int previewHeight = Math.max(
                76,
                rightHeight
                        - layerListHeight
                        - propertyRowsHeight
                        - 12
        );

        this.previewWidget = new LoomPlayerPreviewWidget(
                previewLeft,
                contentTop,
                rightWidth,
                previewHeight,
                ClientProjectWorkspace::project,
                LoomPlayerPreviewWidget.Mode.ELYTRA
        );
        this.previewWidget.setTimelineTickSupplier(
                () -> timelineTick
        );
        addRenderableWidget(this.previewWidget);

        int layerTop = contentTop + previewHeight + 5;
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

        int actionsTop = layerTop + layerListHeight + 5;
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

        actionsTop += actionHeight + 5;
        int quarter = Math.max(30, (rightWidth - 12) / 4);
        this.layerUpButton = new LoomButton(
                previewLeft,
                actionsTop,
                quarter,
                actionHeight,
                Component.literal("Up"),
                () -> moveLayer(1)
        );
        this.layerDownButton = new LoomButton(
                previewLeft + quarter + 4,
                actionsTop,
                quarter,
                actionHeight,
                Component.literal("Down"),
                () -> moveLayer(-1)
        );
        this.layerEditImageButton = new LoomButton(
                previewLeft + quarter * 2 + 8,
                actionsTop,
                rightWidth - quarter * 2 - 8,
                actionHeight,
                Component.literal("Edit Image"),
                this::editSelectedImage
        );
        addRenderableWidget(this.layerUpButton);
        addRenderableWidget(this.layerDownButton);
        addRenderableWidget(this.layerEditImageButton);

        actionsTop += actionHeight + 5;
        this.layerNameField = new EditBox(
                this.font,
                previewLeft,
                actionsTop,
                Math.max(60, rightWidth - 58),
                actionHeight,
                Component.literal("Layer name")
        );
        this.layerNameField.setMaxLength(
                dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS
        );
        addRenderableWidget(this.layerNameField);
        addRenderableWidget(new LoomButton(
                previewRight - 54,
                actionsTop,
                54,
                actionHeight,
                Component.literal("Rename"),
                this::renameLayer
        ));

        actionsTop += actionHeight + 5;
        int small = 38;
        this.layerOpacityDownButton = new LoomButton(
                previewLeft,
                actionsTop,
                small,
                actionHeight,
                Component.literal("-"),
                () -> changeLayerOpacity(-0.1F)
        );
        this.layerOpacityLabelButton = new LoomButton(
                previewLeft + small + 4,
                actionsTop,
                Math.max(42, rightWidth - small * 2 - 8),
                actionHeight,
                Component.literal("Opacity 100%"),
                () -> { }
        );
        this.layerOpacityLabelButton.active = false;
        this.layerOpacityUpButton = new LoomButton(
                previewRight - small,
                actionsTop,
                small,
                actionHeight,
                Component.literal("+"),
                () -> changeLayerOpacity(0.1F)
        );
        addRenderableWidget(this.layerOpacityDownButton);
        addRenderableWidget(this.layerOpacityLabelButton);
        addRenderableWidget(this.layerOpacityUpButton);

        actionsTop += actionHeight + 5;
        int half = Math.max(52, (rightWidth - 4) / 2);
        this.layerBlendButton = new LoomButton(
                previewLeft,
                actionsTop,
                half,
                actionHeight,
                Component.literal("Blend: Normal"),
                this::cycleLayerBlendMode
        );
        this.swatchesButton = new LoomButton(
                previewLeft + half + 4,
                actionsTop,
                rightWidth - half - 4,
                actionHeight,
                Component.literal("Swatches"),
                this::toggleSwatches
        );
        addRenderableWidget(this.layerBlendButton);
        addRenderableWidget(this.swatchesButton);

        actionsTop += actionHeight + 5;
        addRenderableWidget(new LoomButton(
                previewLeft,
                actionsTop,
                half,
                actionHeight,
                Component.literal("Import PNG"),
                this::importImage
        ));
        addRenderableWidget(new LoomButton(
                previewLeft + half + 4,
                actionsTop,
                rightWidth - half - 4,
                actionHeight,
                Component.literal("Cape → Wings"),
                this::convertCapeToElytra
        ));

        buildPaletteWindow(centerRight, contentTop);

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
