package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.ui.LoomWorkspaceLayout;
import dev.loomstudios.client.ui.LoomInspectorLayout;
import dev.loomstudios.client.ui.LoomSlider;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

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
import dev.loomstudios.project.PixelDrawing.Tool;
import dev.loomstudios.project.PixelDrawing;
import dev.loomstudios.project.PixelSelection;
import dev.loomstudios.project.SurfaceEdits;
import dev.loomstudios.client.project.PixelClipboard;
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

public final class ElytraEditorScreen extends LoomPointerScreen {
    private enum InspectorTab {
        LAYERS,
        COLOR,
        ANIMATION,
        PROPERTIES
    }

    private final Screen parent;
    private boolean workspaceTooSmall;

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
    private LoomWorkspaceLayout workspaceLayout;
    private java.util.List<AbstractWidget> layerWidgets = java.util.List.of();
    private java.util.List<AbstractWidget> colorWidgets = java.util.List.of();
    private java.util.List<AbstractWidget> propertyWidgets = java.util.List.of();
    private java.util.List<AbstractWidget> animationWidgets = java.util.List.of();
    private LoomButton inspectorPropertiesButton;
    private LoomButton separateButton;
    private LoomSlider opacitySlider;
    private java.util.List<AbstractWidget> animationPlaybackWidgets = java.util.List.of();
    private int animationPage;
    private LoomButton animationPageButton, trackLoopButton, playbackLabel;
    private int laidOutTrackCount;
    private boolean compactMode;
    private InspectorTab inspectorTab = InspectorTab.LAYERS;

    public ElytraEditorScreen(Screen parent) {
        super(Component.literal("Loom Studios - Elytra Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        tooltipLayerState="";
        LoomElytraCanvasWidget.ViewState viewState = canvasWidget == null ? null : canvasWidget.viewState();
        LoomPlayerPreviewWidget.ViewState previewState = previewWidget == null ? null : previewWidget.viewState();
        if (canvasWidget != null) canvasWidget.close();
        workspaceTooSmall = width < 600 || height < 320;
        if (workspaceTooSmall) {
            ClientProjectWorkspace.endCompoundEdit();
            addRenderableWidget(new LoomButton(Math.max(0, (width - 100) / 2), height / 2 + 24, 100, 22,
                    Component.literal("Back"), this::onClose));
            return;
        }
        ensureSelectedLayerExists(); ensureSelectedTrackExists();
        laidOutTrackCount = (int)ClientProjectWorkspace.project().animation().tracks().stream().filter(t -> t.channel() == AnimationChannel.ELYTRA).count();
        workspaceLayout = LoomWorkspaceLayout.create(width, height, true, laidOutTrackCount);
        var layout = workspaceLayout;
        compactMode = layout.compact();
        shellLeft = 0; shellTop = 0; shellRight = width; shellBottom = height;
        contentTop = layout.tools().top(); contentBottom = layout.tools().bottom();
        toolRailLeft = layout.tools().left(); toolRailRight = layout.tools().right();
        centerLeft = layout.canvas().left(); centerRight = layout.canvas().right();
        canvasTop = layout.canvas().top(); canvasBottom = layout.canvas().bottom();
        timelineTop = layout.timeline().top(); timelineBottom = layout.timeline().bottom();
        rightPanelLeft = layout.preview().left(); rightPanelRight = layout.preview().right();
        previewBottom = layout.preview().bottom(); inspectorTop = layout.inspector().top(); inspectorBottom = layout.inspector().bottom();
        buildTopNavigation(layout.headerHeight(), layout.navHeight(), 8);
        buildToolRail(); buildCanvasToolbar(); buildCanvasAndTimeline();
        buildRightPanel(layout.preview().height(), 22); buildPaletteWindow(centerRight, contentTop);
        canvasWidget.restoreViewState(viewState); previewWidget.restoreViewState(previewState);
        updateButtonStates(); updateInspectorVisibility();
    }


    private void buildTopNavigation(int headerHeight, int navHeight, int margin) {
        int x = margin, y = headerHeight + 2, h = navHeight - 4;
        String[] labels = {"Home", "Cape", "Elytra", "Import", "Export"};
        LoomButton.Icon[] icons = {LoomButton.Icon.HOME, LoomButton.Icon.CAPE, LoomButton.Icon.ELYTRA, LoomButton.Icon.IMAGE, LoomButton.Icon.EXPORT};
        Runnable[] actions = {() -> dev.loomstudios.client.project.WorkspaceNavigation.request(this,()->minecraft.setScreen(new LoomHomeScreen())), () -> minecraft.setScreen(new CapeEditorScreen(this)), () -> { }, this::importImage,
                () -> minecraft.setScreen(new LoomCodesScreen(this, ClientProjectWorkspace.project()))};
        for (int i = 0; i < labels.length; i++) {
            int w = font.width(labels[i]) + 28;
            LoomButton button = navButton(x, y, w, h, labels[i], icons[i], i == 2, actions[i]);
            button.setIconOnly(false);
            addRenderableWidget(button);
            x += w + 3;
        }
        int right = width - margin;
        int equipWidth = font.width("Save + Equip") + 28;
        LoomButton equip = navButton(right - equipWidth, y, equipWidth, h, "Save + Equip", LoomButton.Icon.EQUIP, false, this::saveAndEquip);
        equip.setIconOnly(false).setPrimary(true);
        addRenderableWidget(equip);
        right -= equipWidth + 3;
        int saveWidth = font.width("Save") + 28;
        LoomButton save = navButton(right - saveWidth, y, saveWidth, h, "Save", LoomButton.Icon.SAVE, false, this::save);
        save.setIconOnly(false);
        addRenderableWidget(save);
        right -= saveWidth + 3;
        redoButton = navButton(right - 24, y, 24, h, "Redo (Ctrl+Y)", LoomButton.Icon.REDO, false, this::redo);
        redoButton.setIconOnly(true);
        addRenderableWidget(redoButton);
        right -= 27;
        undoButton = navButton(right - 24, y, 24, h, "Undo (Ctrl+Z)", LoomButton.Icon.UNDO, false, this::undo);
        undoButton.setIconOnly(true);
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

    private final java.util.Map<Tool,LoomButton> drawingButtons=new java.util.EnumMap<>(Tool.class);
    private boolean shapeFilled;
    private PixelSelection selection;
    private ElytraWing selectionWing=ElytraWing.LEFT;
    private java.util.List<LoomButton> selectionButtons=new java.util.ArrayList<>();
    private LoomButton filledButton;
    private void buildToolRail() {
        drawingButtons.clear();selectionButtons.clear();
        int x=toolRailLeft+2,w=toolRailRight-toolRailLeft-4,y=contentTop+5,h=Math.min(compactMode?25:30,(contentBottom-y-32)/9);
        LoomButton.Icon[] icons={LoomButton.Icon.PENCIL,LoomButton.Icon.ERASER,LoomButton.Icon.FILL,LoomButton.Icon.EYEDROPPER,LoomButton.Icon.SELECT,LoomButton.Icon.LINE,LoomButton.Icon.RECTANGLE,LoomButton.Icon.CIRCLE};
        String[] labels={"Pencil (B)","Eraser (E)","Fill (G)","Eyedropper (I)","Select (S)","Line (L)","Rectangle (R)","Circle (C)"};
        for(int i=0;i<Tool.values().length;i++){Tool t=Tool.values()[i];LoomButton button=iconButton(x,y+i*(h+4),w,h,labels[i],icons[i],()->{tool=t;updateButtonStates();}).setIconOnly(compactMode);drawingButtons.put(t,button);}
        pencilButton=drawingButtons.get(Tool.PENCIL);eraserButton=drawingButtons.get(Tool.ERASER);
        iconButton(x,y+8*(h+4),w,h,"Swatches",LoomButton.Icon.PALETTE,this::toggleSwatches).setIconOnly(compactMode);
    }


    private void buildCanvasToolbar() {
        int x = centerLeft, y = contentTop, h = 22;
        linkButton = iconButton(x, y, compactMode ? 78 : 96, h, "Linked", LoomButton.Icon.ELYTRA, () -> { linkedMirror = true; updateButtonStates(); }).setIconOnly(false);
        x += linkButton.getWidth() + 3;
        separateButton = iconButton(x, y, compactMode ? 68 : 90, h, "Separate", LoomButton.Icon.NONE, () -> { linkedMirror = false; updateButtonStates(); }).setIconOnly(false);
        x += separateButton.getWidth() + 8;
        resolutionDownButton = iconButton(x, y, 22, h, "Resolution -", LoomButton.Icon.MINUS, () -> changeResolution(-1)).setIconOnly(true); x += 25;
        resolutionLabelButton = new LoomButton(x, y, 32, h, Component.literal("1x"), () -> {}); resolutionLabelButton.active = false; addRenderableWidget(resolutionLabelButton); x += 35;
        resolutionUpButton = iconButton(x, y, 22, h, "Resolution +", LoomButton.Icon.PLUS, () -> changeResolution(1)).setIconOnly(true);
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
        canvasWidget.setDrawing(()->tool,()->selectedColor,()->brushSize,()->shapeFilled,this::finishShape);
        canvasWidget.setSelection(selectionWing,selection);
        addRenderableWidget(canvasWidget);

        int contextH = compactMode ? 19 : 22;
        int contextY = workspaceLayout.context().top() + 3;
        int small = compactMode ? 22 : 28;
        int gap = 3;

        brushDownButton = iconButton(
                centerLeft + 5,
                contextY,
                small,
                contextH,
                "Brush -",
                LoomButton.Icon.MINUS,
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
                LoomButton.Icon.PLUS,
                () -> changeBrush(1)
        );
        brushUpButton.setIconOnly(true);

        filledButton=iconButton(centerLeft+154,contextY,80,contextH,"Outline",LoomButton.Icon.CIRCLE,()->{shapeFilled=!shapeFilled;updateButtonStates();}).setIconOnly(false);
        Runnable[] actions={()->flipWingSelection(true),()->flipWingSelection(false),this::rotateWingSelection,this::copyWingSelection,this::pasteWingSelection,()->{selection=null;canvasWidget.setSelection(selectionWing,null);updateButtonStates();}};
        LoomButton.Icon[] icons={LoomButton.Icon.FLIP_H,LoomButton.Icon.FLIP_V,LoomButton.Icon.ROTATE_RIGHT,LoomButton.Icon.COPY,LoomButton.Icon.PLUS,LoomButton.Icon.CLOSE};
        String[] labels={"Flip H","Flip V","Rotate 90° (Ctrl+R)","Copy pixels (Ctrl+C)","Paste pixels (Ctrl+V)","Clear selection"};
        for(int i=0;i<actions.length;i++)selectionButtons.add(iconButton(centerLeft+5+i*30,contextY,27,contextH,labels[i],icons[i],actions[i]).setIconOnly(true));
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

    private void buildRightPanel(int previewHeight, int tabHeight) {
        previewWidget = addRenderableWidget(new LoomPlayerPreviewWidget(rightPanelLeft, contentTop,
                rightPanelRight - rightPanelLeft, previewHeight, ClientProjectWorkspace::project, LoomPlayerPreviewWidget.Mode.ELYTRA));
        previewWidget.setTimelineTickSupplier(() -> timelineTick);
        int y = workspaceLayout.inspectorTabs().top(), gap = 3, w = (rightPanelRight - rightPanelLeft - 9) / 4;
        inspectorLayersButton = iconButton(rightPanelLeft, y, w, tabHeight, "Layers", LoomButton.Icon.NONE, () -> setInspectorTab(InspectorTab.LAYERS)).setIconOnly(false);
        inspectorColorButton = iconButton(rightPanelLeft + w + gap, y, w, tabHeight, "Color", LoomButton.Icon.NONE, () -> setInspectorTab(InspectorTab.COLOR)).setIconOnly(false);
        inspectorPropertiesButton = iconButton(rightPanelLeft + 2 * (w + gap), y, w, tabHeight, "Props", LoomButton.Icon.NONE, () -> setInspectorTab(InspectorTab.PROPERTIES)).setIconOnly(false);
        inspectorAnimationButton = iconButton(rightPanelLeft + 3 * (w + gap), y, rightPanelRight - (rightPanelLeft + 3 * (w + gap)), tabHeight, "Anim", LoomButton.Icon.NONE, () -> {
            if(inspectorTab==InspectorTab.ANIMATION)minecraft.setScreen(new LoomAnimationScreen(this,AnimationChannel.ELYTRA,selectedLayerId));else setInspectorTab(InspectorTab.ANIMATION);
        }).setIconOnly(false);
        inspectorAnimationButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Animation properties · Click again for full timeline studio")));
        buildLayerInspector(); buildColorInspector(); buildPropertyInspector(); buildAnimationInspector();
    }


    private void buildLayerInspector() {
        int start = children().size(); var rows = inspectorRows();
        int count = ClientProjectWorkspace.project().elytra().layers().size();
        var r = rows.row(Math.min(Math.max(44, 20 + count * 18), workspaceLayout.inspector().height() - (inspectorRows().padding() + 2 * inspectorRows().stride() + 4)));
        layerListWidget = addRenderableWidget(new LoomLayerListWidget(r.left(), r.top(), r.width(), r.height(),
                () -> ClientProjectWorkspace.project().elytra(), () -> selectedLayerId, this::selectLayer, this::toggleLayerVisibility, this::toggleLayerLock).setElytraThumbnails(true).setManage(()->minecraft.setScreen(new LoomLayerManagerScreen(this,true,selectedLayerId))));
        r = rows.row(22);
        layerAddButton = cellButton(r, 0, 3, "Add layer", LoomButton.Icon.PLUS, this::addLayer);
        layerDuplicateButton = cellButton(r, 1, 3, "Duplicate layer", LoomButton.Icon.COPY, this::duplicateLayer);
        layerDeleteButton = cellButton(r, 2, 3, "Delete layer", LoomButton.Icon.DELETE, this::deleteLayer).setDanger(true);
        r = rows.row(22);
        layerUpButton = cellButton(r, 0, 3, "Move layer up", LoomButton.Icon.UP, () -> moveLayer(1));
        layerDownButton = cellButton(r, 1, 3, "Move layer down", LoomButton.Icon.DOWN, () -> moveLayer(-1));
        layerEditImageButton = cellButton(r, 2, 3, "Edit image", LoomButton.Icon.IMAGE, this::editSelectedImage);
        layerWidgets = widgetsSince(start);
    }

    private void buildPropertyInspector() {
        int start = children().size(); var rows = inspectorRows(); var r = rows.row(22);
        layerNameField = addRenderableWidget(new EditBox(font, r.left(), r.top(), r.width() - 27, r.height(), Component.literal("Layer name")));
        layerNameField.setMaxLength(dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS);
        layerRenameButton = iconButton(r.right() - 24, r.top(), 24, r.height(), "Rename layer", LoomButton.Icon.PENCIL, this::renameLayer).setIconOnly(true);
        r = rows.row(22);
        opacitySlider = addRenderableWidget(new LoomSlider(r.left(), r.top(), r.width(), "Opacity", () -> selectedLayer().opacity(), value -> changeLayerOpacity((float)value - selectedLayer().opacity()))); opacitySlider.setHeight(r.height());
        r = rows.row(22); layerBlendButton = rowButton(r, "Blend: Normal", LoomButton.Icon.LAYERS, this::cycleLayerBlendMode);
        r = rows.row(22);
        lockButton = cellButton(r, 0, 2, "Lock layer", LoomButton.Icon.LOCK, this::toggleLock);
        cellButton(r, 1, 2, "Import image", LoomButton.Icon.IMAGE, this::importImage);
        r = rows.row(22);
        thicknessDownButton = cellButton(r, 0, 3, "Thickness -", LoomButton.Icon.MINUS, () -> changeThickness(-0.25F));
        thicknessLabelButton = cellButton(r, 1, 3, "100%", LoomButton.Icon.NONE, () -> {}); thicknessLabelButton.active = false;
        thicknessUpButton = cellButton(r, 2, 3, "Thickness +", LoomButton.Icon.PLUS, () -> changeThickness(0.25F));
        r = rows.row(22); rowButton(r, "Cape to wings", LoomButton.Icon.ELYTRA, this::convertCapeToElytra);
        propertyWidgets = widgetsSince(start);
    }


    private void buildColorInspector() {
        int start = children().size(); var rows = inspectorRows();
        var r = rows.row(Math.min(110, workspaceLayout.inspector().height() - 62));
        colorPicker = addRenderableWidget(new LoomColorPickerWidget(r.left(), r.top(), r.width(), r.height(), selectedColor, this::setSelectedColor));
        r = rows.row(22);
        LoomSlider colorAlphaSlider = addRenderableWidget(new LoomSlider(r.left(), r.top(), r.width(), "Color alpha",
                () -> (selectedColor >>> 24) / 255.0,
                value -> setSelectedColor(((int)Math.round(value * 255) << 24) | (selectedColor & 0xFFFFFF))));
        colorAlphaSlider.setHeight(r.height());
        r = rows.row(22); swatchesButton = rowButton(r, "Swatches", LoomButton.Icon.PALETTE, this::toggleSwatches);
        colorWidgets = widgetsSince(start);
    }


    private void buildAnimationInspector() {
        int start = children().size();
        int left = rightPanelLeft + 6;
        int width = rightPanelRight - rightPanelLeft - 12;
        var compactRows = inspectorRows();
        int y = inspectorTop + compactRows.padding();
        int h = compactRows.controlHeight();
        int gap = compactRows.gap();

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
                "Add key",
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
                "Remove key",
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
                LoomButton.Icon.MINUS,
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
                LoomButton.Icon.PLUS,
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
                "Delete track",
                LoomButton.Icon.DELETE,
                () -> withSelectedTrack(
                        track -> deleteAnimationTrack(track.id())
                )
        );
        animationDeleteButton.setDanger(true).setIconOnly(false);
        animationWidgets = widgetsSince(start).stream().filter(widget -> widget != animationSpeedButton).toList();
        for (AbstractWidget widget : animationWidgets) widget.setY(widget.getY() + compactRows.stride());
        animationDeleteButton.setY(animationDeleteButton.getY() - compactRows.stride());
        var rows = inspectorRows(); var r = rows.row(22);
        animationPageButton = rowButton(r, "Keyframes  ›", LoomButton.Icon.PLAY, () -> { animationPage = 1 - animationPage; updateInspectorVisibility(); });
        start = children().size(); rows.row(22); r = rows.row(22);
        animationSpeedButton.setY(inspectorTop + compactRows.padding() + compactRows.stride());
        trackLoopButton = rowButton(r, "Track loop", LoomButton.Icon.LOOP, () -> withSelectedTrack(track -> ClientProjectWorkspace.apply(project -> project.withAnimation(AnimationAuthoring.replaceTrack(project.animation(), track.withLoop(!track.loop()))))));
        r = rows.row(22);
        cellButton(r,0,3,"Playback speed -",LoomButton.Icon.MINUS,() -> changeTimelinePlaybackSpeed(-0.25F));
        playbackLabel = cellButton(r,1,3,"1.0x",LoomButton.Icon.NONE,() -> {}); playbackLabel.active = false;
        cellButton(r,2,3,"Playback speed +",LoomButton.Icon.PLUS,() -> changeTimelinePlaybackSpeed(0.25F));
        animationPlaybackWidgets = new java.util.ArrayList<>(widgetsSince(start));
        animationPlaybackWidgets.add(animationSpeedButton);
    }

    private void withSelectedTrack(
            java.util.function.Consumer<AnimationTrack> action
    ) {
        AnimationTrack track = findAnimationTrack(selectedTrackId);
        if (track != null) {
            action.accept(track);
        }
    }


    private java.util.List<AbstractWidget> widgetsSince(int index) {
        return children().subList(index, children().size()).stream()
                .filter(child -> child instanceof AbstractWidget).map(child -> (AbstractWidget)child).toList();
    }
    private static void show(java.util.List<AbstractWidget> widgets, boolean visible) {
        for (AbstractWidget widget : widgets) widget.visible = visible;
    }
    private LoomInspectorLayout inspectorRows() { return new LoomInspectorLayout(workspaceLayout.inspector()); }
    private LoomButton rowButton(LoomWorkspaceLayout.Rect row, String label, LoomButton.Icon icon, Runnable action) {
        return iconButton(row.left(), row.top(), row.width(), row.height(), label, icon, action).setIconOnly(false);
    }
    private LoomButton cellButton(LoomWorkspaceLayout.Rect row, int index, int count, String label, LoomButton.Icon icon, Runnable action) {
        int cell = (row.width() - (count - 1) * 3) / count;
        int x = row.left() + index * (cell + 3);
        int w = index == count - 1 ? row.right() - x : cell;
        return iconButton(x, row.top(), w, row.height(), label, icon, action).setIconOnly(count > 2 && icon != LoomButton.Icon.NONE);
    }

    private void setInspectorTab(InspectorTab tab) {
        ClientProjectWorkspace.endCompoundEdit();
        setFocused(null);
        inspectorTab = tab;
        updateInspectorVisibility();
    }

    private void updateInspectorVisibility() {
        if (layerListWidget != null) {
            int count = ClientProjectWorkspace.project().elytra().layers().size();
            int target = Math.min(Math.max(44, 20 + count * 18), workspaceLayout.inspector().height() - (inspectorRows().padding() + 2 * inspectorRows().stride() + 4));
            int delta = target - layerListWidget.getHeight();
            if (delta != 0) {
                layerListWidget.setHeight(target);
                for (AbstractWidget widget : layerWidgets) if (widget != layerListWidget) widget.setY(widget.getY() + delta);
            }
        }

        boolean layers = inspectorTab == InspectorTab.LAYERS, color = inspectorTab == InspectorTab.COLOR;
        boolean animation = inspectorTab == InspectorTab.ANIMATION, properties = inspectorTab == InspectorTab.PROPERTIES;
        inspectorLayersButton.setSelected(layers); inspectorColorButton.setSelected(color);
        inspectorPropertiesButton.setSelected(properties); inspectorAnimationButton.setSelected(animation);
        show(layerWidgets, layers); show(colorWidgets, color); show(propertyWidgets, properties);
        boolean hasTrack = findAnimationTrack(selectedTrackId) != null;
        show(animationWidgets, animation && hasTrack && animationPage == 0);
        show(animationPlaybackWidgets, animation && hasTrack && animationPage == 1);
        animationPageButton.visible = animation && hasTrack;
        animationPageButton.setMessage(Component.literal(animationPage == 0 ? "Keyframes  ›" : "Playback  ›"));
        var track = findAnimationTrack(selectedTrackId);
        trackLoopButton.setMessage(Component.literal(track != null && track.loop() ? "Track loop: On" : "Track loop: Off"));
        trackLoopButton.setSelected(track != null && track.loop());
        playbackLabel.setMessage(Component.literal(String.format(java.util.Locale.ROOT,"%.2fx",ClientProjectWorkspace.project().animation().playbackSpeed())));
        opacitySlider.active = selectedLayer() != null && !selectedLayer().locked();
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
        inspectorTab = InspectorTab.ANIMATION;
        scrubTimeline(0);
        statusMessage = "Added Pulse animation track";
        updateButtonStates();
    }

    private void selectAnimationTrack(UUID trackId) {
        if (findAnimationTrack(trackId) == null) {
            return;
        }
        selectedTrackId = trackId;
        inspectorTab = InspectorTab.ANIMATION;
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

    private void editWing(ElytraWing wing,int x,int y) {
        if(tool!=Tool.ERASER&&tool!=Tool.SELECT&&tool!=Tool.EYEDROPPER)dev.loomstudios.client.palette.EditorColors.use(selectedColor);
        if(tool==Tool.EYEDROPPER){int[] p=LoomTextureCompiler.compile(ClientProjectWorkspace.project().elytra(),0,false,false);int scale=ClientProjectWorkspace.project().elytra().width()/64;setSelectedColor(p[wing.atlasY(y,scale)*ClientProjectWorkspace.project().elytra().width()+wing.atlasX(x,scale)]);return;}
        LoomLayer layer=selectedLayer();if(layer==null||!layer.editableAsPaint())return;
        if(tool==Tool.FILL)ClientProjectWorkspace.apply(p->SurfaceEdits.wing(p,selectedLayerId,wing,linkedMirror,patch->PixelDrawing.shape(patch,tool,x,y,x,y,1,selectedColor,false)));
        else ClientProjectWorkspace.apply(p->ProjectEdits.paintElytraWingBrush(p,selectedLayerId,wing,x,y,brushSize,tool==Tool.ERASER?0:selectedColor,linkedMirror));
    }
    private void finishShape(ElytraWing wing,int x0,int y0,int x1,int y1){
        if(selectedLayer()==null||!selectedLayer().editableAsPaint())return;
        if(tool==Tool.SELECT){selectionWing=wing;selection=PixelSelection.between(x0,y0,x1,y1);canvasWidget.setSelection(wing,selection);updateButtonStates();return;}
        dev.loomstudios.client.palette.EditorColors.use(selectedColor);
        ClientProjectWorkspace.apply(p->SurfaceEdits.wing(p,selectedLayerId,wing,linkedMirror,patch->PixelDrawing.shape(patch,tool,x0,y0,x1,y1,brushSize,selectedColor,shapeFilled)));
    }
    private void copyWingSelection(){if(selection!=null){PixelClipboard.patch=SurfaceEdits.wing(ClientProjectWorkspace.project(),selectedLayerId,selectionWing).crop(selection);statusMessage="Pixels copied";updateButtonStates();}}
    private void pasteWingSelection(){if(PixelClipboard.patch==null)return;int x=selection==null?0:selection.minX(),y=selection==null?0:selection.minY();try{ClientProjectWorkspace.apply(p->SurfaceEdits.wing(p,selectedLayerId,selectionWing,linkedMirror,patch->patch.paste(PixelClipboard.patch,x,y)));selection=new PixelSelection(x,y,x+PixelClipboard.patch.width()-1,y+PixelClipboard.patch.height()-1);canvasWidget.setSelection(selectionWing,selection);updateButtonStates();}catch(IllegalArgumentException e){statusMessage=e.getMessage();}}
    private void flipWingSelection(boolean horizontal){if(selection==null)return;PixelSelection sel=selection;ClientProjectWorkspace.apply(p->SurfaceEdits.wing(p,selectedLayerId,selectionWing,linkedMirror,patch->patch.paste(patch.crop(sel).flip(horizontal),sel.minX(),sel.minY())));}
    private void rotateWingSelection(){if(selection==null)return;PixelSelection sel=selection;try{ClientProjectWorkspace.apply(p->SurfaceEdits.wing(p,selectedLayerId,selectionWing,linkedMirror,patch->patch.clear(sel).paste(patch.crop(sel).rotate(),sel.minX(),sel.minY())));selection=new PixelSelection(sel.minX(),sel.minY(),sel.minX()+sel.height()-1,sel.minY()+sel.width()-1);canvasWidget.setSelection(selectionWing,selection);}catch(IllegalArgumentException e){statusMessage=e.getMessage();}}
    private void nudgeWingSelection(int dx,int dy){if(selection==null)return;PixelSelection sel=selection;var patch=SurfaceEdits.wing(ClientProjectWorkspace.project(),selectedLayerId,selectionWing);int x=Math.max(0,Math.min(patch.width()-sel.width(),sel.minX()+dx)),y=Math.max(0,Math.min(patch.height()-sel.height(),sel.minY()+dy));ClientProjectWorkspace.apply(p->SurfaceEdits.wing(p,selectedLayerId,selectionWing,linkedMirror,face->face.clear(sel).paste(face.crop(sel),x,y)));selection=new PixelSelection(x,y,x+sel.width()-1,y+sel.height()-1);canvasWidget.setSelection(selectionWing,selection);}


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
        selection=null;if(canvasWidget!=null)canvasWidget.setSelection(selectionWing,null);
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
        selection=null;if(canvasWidget!=null)canvasWidget.setSelection(selectionWing,null);
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
        this.paletteWindow.setOnClose(this::closePaletteWindow);
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
        selection=null;if(canvasWidget!=null)canvasWidget.setSelection(selectionWing,null);
        ClientProjectWorkspace.undo();
        ensureSelectedLayerExists();
        ensureSelectedTrackExists();
        clampTimelineAfterHistory();
        updateButtonStates();
    }

    private void redo() {
        selection=null;if(canvasWidget!=null)canvasWidget.setSelection(selectionWing,null);
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
            statusMessage="Design saved";
        } catch (IOException e) {
            statusMessage="Save failed; edits are still open";
            LoomStudios.LOGGER.error("Failed to save Loom Elytra project", e);
        }
        updateButtonStates();
    }

    private void saveAndEquip() {
        try {
            ClientProjectWorkspace.saveAndEquip();
            statusMessage="Design saved and equipped";
        } catch (IOException | IllegalStateException e) {
            statusMessage="Save / equip failed; check diagnostics";
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

    private String tooltipLayerState="";
    private void updateButtonStates() {
        boolean paint=selectedLayer()!=null&&selectedLayer().editableAsPaint();
        for(var entry:drawingButtons.entrySet()){entry.getValue().setSelected(tool==entry.getKey());entry.getValue().active=paint||entry.getKey()==Tool.EYEDROPPER;}
        String state=selectedLayer()==null?"none":selectedLayer().kind()+":"+selectedLayer().locked();
        if(!state.equals(tooltipLayerState)){tooltipLayerState=state;for(var entry:drawingButtons.entrySet())entry.getValue().setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(dev.loomstudios.client.ui.LoomToolGuidance.tooltip(entry.getKey().name(),selectedLayer(),true))));}
        if(filledButton!=null){filledButton.visible=tool==Tool.CIRCLE||tool==Tool.RECTANGLE;filledButton.active=paint;filledButton.setMessage(Component.literal(shapeFilled?"Filled":"Outline"));}
        for(int i=0;i<selectionButtons.size();i++){var button=selectionButtons.get(i);button.visible=tool==Tool.SELECT;button.active=paint&&(i==4?PixelClipboard.patch!=null:selection!=null);}
        if(brushDownButton!=null){brushDownButton.visible=tool!=Tool.SELECT&&tool!=Tool.FILL&&tool!=Tool.EYEDROPPER;brushLabelButton.visible=brushDownButton.visible;brushUpButton.visible=brushDownButton.visible;}

        if (!ClientProjectWorkspace.isInitialized()) {
            return;
        }

        ensureSelectedTrackExists();
        int trackCount = (int)ClientProjectWorkspace.project().animation().tracks().stream().filter(t -> t.channel() == AnimationChannel.ELYTRA).count();
        if (workspaceLayout != null && trackCount != laidOutTrackCount) {
            laidOutTrackCount = trackCount;
            workspaceLayout = LoomWorkspaceLayout.create(width, height, true, trackCount);
            var canvas = workspaceLayout.canvas(); var timeline = workspaceLayout.timeline();
            canvasBottom = canvas.bottom(); timelineTop = timeline.top();
            canvasWidget.setHeight(canvas.height());
            timelineWidget.setY(timeline.top()); timelineWidget.setHeight(timeline.height());
            brushDownButton.setY(workspaceLayout.context().top() + 3);
            brushLabelButton.setY(workspaceLayout.context().top() + 3);
            brushUpButton.setY(workspaceLayout.context().top() + 3);
        }

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
            linkButton.setMessage(Component.literal("Linked"));
            linkButton.setSelected(linkedMirror);
            separateButton.setSelected(!linkedMirror);
        }
        if (pencilButton != null) {
            pencilButton.active = editable;
            pencilButton.setSelected(tool == Tool.PENCIL);
        }
        if (eraserButton != null) {
            eraserButton.active = editable;
            eraserButton.setSelected(tool == Tool.ERASER);
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
                    Math.round(thickness * 100.0F) + "%"
            ));
        }
        if (lockButton != null) {
            lockButton.active = layer != null;
            lockButton.setSelected(layer != null && layer.locked());
            lockButton.setMessage(Component.literal(
                    layer != null && layer.locked()
                            ? "Unlock"
                            : "Lock"
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
            swatchesButton.setSelected(paletteWindowVisible);
        }

        AnimationTrack selectedTrack =
                findAnimationTrack(selectedTrackId);
        if (animationEffectButton != null) {
            animationEffectButton.active = selectedTrack != null;
            animationEffectButton.setMessage(Component.literal(
                    selectedTrack == null
                            ? "Effect: Select a track"
                            : "Effect: "
                                    + selectedTrack.effect().displayName()
            ));
        }
        if (animationAddKeyButton != null) {
            animationAddKeyButton.active = selectedTrack != null;
        }
        if (animationRemoveKeyButton != null) {
            animationRemoveKeyButton.active =
                    selectedTrack != null
                            && selectedTrack.keyframes().size() > 1;
        }
        if (animationValueDownButton != null) {
            animationValueDownButton.active = selectedTrack != null;
        }
        if (animationValueUpButton != null) {
            animationValueUpButton.active = selectedTrack != null;
        }
        if (animationValueLabelButton != null) {
            float value = selectedTrack == null
                    ? 0.0F
                    : AnimationEvaluator.valueAt(
                            selectedTrack,
                            ClientProjectWorkspace.project().animation(),
                            timelineTick
                    );
            animationValueLabelButton.setMessage(Component.literal(
                    selectedTrack == null
                            ? "Value"
                            : String.format(
                                    java.util.Locale.ROOT,
                                    "Value %.2f",
                                    value
                            )
            ));
        }
        if (animationSpeedButton != null) {
            animationSpeedButton.active = selectedTrack != null;
            animationSpeedButton.setMessage(Component.literal(
                    selectedTrack == null
                            ? "Track Speed"
                            : String.format(
                                    java.util.Locale.ROOT,
                                    "Track Speed %.2fx",
                                    selectedTrack.speed()
                            )
            ));
        }
        if (animationDeleteButton != null) {
            animationDeleteButton.active = selectedTrack != null;
        }

        syncLayerName();
        updateInspectorVisibility();

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
        if (workspaceTooSmall) {
            LoomScreenChrome.renderBackdrop(graphics, width, height);
            graphics.drawCenteredString(font, title, width / 2, height / 2 - 24, LoomUiTheme.TEXT);
            graphics.drawCenteredString(font, Component.literal("Increase window size or reduce GUI scale"), width / 2, height / 2, LoomUiTheme.TEXT_MUTED);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        LoomScreenChrome.renderBackdrop(graphics, width, height);
        LoomScreenChrome.renderEditorHeader(
                graphics,
                width,
                compactMode
                        ? "Elytra Editor"
                        : "Elytra Editor • "
                                + (linkedMirror
                                        ? "Linked Mirror"
                                        : "Separate Wings"),
                compactMode
        );

        LoomScreenChrome.panel(
                graphics,
                toolRailLeft,
                contentTop,
                toolRailRight,
                contentBottom
        );


        LoomScreenChrome.panel(
                graphics,
                centerLeft,
                canvasTop,
                centerRight,
                canvasBottom
        );
        LoomScreenChrome.panel(
                graphics,
                centerLeft,
                timelineTop,
                centerRight,
                timelineBottom
        );

        LoomScreenChrome.panel(
                graphics,
                rightPanelLeft,
                contentTop,
                rightPanelRight,
                previewBottom
        );


        LoomScreenChrome.panel(
                graphics,
                rightPanelLeft,
                inspectorTop - 2,
                rightPanelRight,
                inspectorBottom
        );

        if (inspectorTab == InspectorTab.ANIMATION && findAnimationTrack(selectedTrackId) == null) {
            graphics.drawString(font, Component.literal("Animate a layer"), rightPanelLeft + 8, inspectorTop + 12, LoomUiTheme.ACCENT_ALT, false);
            graphics.drawString(font, Component.literal("1. Select a layer"), rightPanelLeft + 8, inspectorTop + 32, LoomUiTheme.TEXT_MUTED, false);
            graphics.drawString(font, Component.literal("2. Add a track below"), rightPanelLeft + 8, inspectorTop + 48, LoomUiTheme.TEXT_MUTED, false);
            graphics.drawString(font, Component.literal("3. Scrub, add key, set value"), rightPanelLeft + 8, inspectorTop + 64, LoomUiTheme.TEXT_MUTED, false);
        }
        var context = workspaceLayout.context();
        LoomScreenChrome.panel(graphics, context.left(), context.top(), context.right(), context.bottom());
        String status = statusMessage.isBlank()
                ? (ClientProjectWorkspace.isCurrentProjectEquipped()
                        ? "Saved / Equipped"
                        : ClientProjectWorkspace.session().isDirty()
                                ? "Unsaved edits"
                                : "Saved, not equipped")
                : statusMessage;
        status=dev.loomstudios.client.ui.LoomToolGuidance.status(tool.name(),selectedLayer(),true,status);

        LoomScreenChrome.footer(
                graphics,
                width,
                height,
                status,
                linkedMirror ? "Linked Wings" : "Separate Wings"
        );

        super.render(graphics, mouseX, mouseY, partialTick);
        updateButtonStates();
    }

    private void closePaletteWindow() {
        paletteWindowVisible = false;
        if (paletteWindow != null) paletteWindow.visible = false;
        if (getFocused() == paletteWindow) setFocused(null);
        updateButtonStates();
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (paletteWindowVisible && paletteWindow != null && paletteWindow.visible && paletteWindow.isMouseOver(event.x(),event.y())) {
            boolean handled=paletteWindow.mouseClicked(event,doubleClick);
            if(handled && paletteWindow.visible)setFocused(paletteWindow);
            return true;
        }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) {
        if (paletteWindowVisible && paletteWindow != null && paletteWindow.visible && paletteWindow.mouseDragged(event,dx,dy)) return true;
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (paletteWindowVisible && paletteWindow != null && paletteWindow.visible && paletteWindow.mouseReleased(event)) return true;
        return super.mouseReleased(event);
    }
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy) {
        if (paletteWindowVisible && paletteWindow != null && paletteWindow.visible && paletteWindow.isMouseOver(x,y)) {
            paletteWindow.mouseScrolled(x,y,dx,dy); return true;
        }
        return super.mouseScrolled(x,y,dx,dy);
    }

    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256 && paletteWindowVisible) { closePaletteWindow(); return true; }
        if (workspaceTooSmall) return super.keyPressed(event);
        if (getFocused() instanceof EditBox) return super.keyPressed(event);
        if(event.hasControlDownWithQuirk()&&selectedLayer()!=null&&selectedLayer().editableAsPaint()){
            if(event.key()==67){copyWingSelection();return true;}if(event.key()==86){pasteWingSelection();return true;}if(event.key()==82){rotateWingSelection();return true;}}
        if(!event.hasControlDown()&&!event.hasAltDown()){
            Tool chosen=switch(event.key()){case 66,80->Tool.PENCIL;case 69->Tool.ERASER;case 71->Tool.FILL;case 73->Tool.EYEDROPPER;case 83->Tool.SELECT;case 76->Tool.LINE;case 82->Tool.RECTANGLE;case 67->Tool.CIRCLE;default->null;};
            if(chosen!=null){tool=chosen;updateButtonStates();return true;}
            if(tool==Tool.SELECT&&selection!=null){switch(event.key()){case 263->nudgeWingSelection(-1,0);case 262->nudgeWingSelection(1,0);case 265->nudgeWingSelection(0,-1);case 264->nudgeWingSelection(0,1);default->{}}if(event.key()>=262&&event.key()<=265)return true;}
        }
        if (event.hasControlDownWithQuirk()) {
            if (event.key() == 90) { undo(); return true; }
            if (event.key() == 89) { redo(); return true; }
            if (event.key() == 83) { if (event.hasShiftDown()) saveAndEquip(); else save(); return true; }
        } else if (!event.hasAltDown()) {
            if (event.key() == 80) { tool = Tool.PENCIL; updateButtonStates(); return true; }
            if (event.key() == 69) { tool = Tool.ERASER; updateButtonStates(); return true; }
        }
        return super.keyPressed(event);
    }

    private void goBack() {
        if(parent instanceof CapeEditorScreen)this.minecraft.setScreen(parent);else dev.loomstudios.client.project.WorkspaceNavigation.request(this,()->minecraft.setScreen(parent));
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
        ClientProjectWorkspace.endCompoundEdit();
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
