package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.WorkspaceState;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomCapeFaceWidget;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomInspectorLayout;
import dev.loomstudios.client.ui.LoomLayerListWidget;
import dev.loomstudios.client.ui.LoomPaletteButton;
import dev.loomstudios.client.ui.LoomPaletteWindow;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomScreenChrome;
import dev.loomstudios.client.ui.LoomSlider;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.BlendMode;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.GradientAuthoring;
import dev.loomstudios.project.GradientLayerData;
import dev.loomstudios.project.GradientStop;
import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.NormalizedRect;
import dev.loomstudios.project.PixelSelection;
import dev.loomstudios.project.ProjectEdits;
import dev.loomstudios.project.ProjectResizer;
import dev.loomstudios.ui.LoomWorkspaceLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class CapeEditorScreen extends LoomPointerScreen {
    private final Screen parent;
    private boolean workspaceTooSmall;
    private final Consumer<WorkspaceState> workspaceListener =
            state -> {
                this.workspaceState = state;
                ensureSelectedLayerExists();
            };

    private WorkspaceState workspaceState;
    private Tool tool = Tool.PENCIL;
    private CapeUvRegion capeRegion = CapeUvRegion.OUTSIDE;
    private int selectedColor = 0xFF22D7E8;
    private int brushSize = 1;
    private boolean rectangleFilled;
    private SymmetryMode symmetryMode = SymmetryMode.NONE;
    private PixelSelection selection;
    private UUID selectedLayerId;
    private int selectedGradientStopIndex;

    private LoomButton pencilButton;
    private LoomButton eraserButton;
    private LoomButton fillButton;
    private LoomButton eyedropperButton;
    private LoomButton selectButton;
    private LoomButton lineButton;
    private LoomButton rectangleButton;
    private LoomButton circleButton;
    private LoomButton rectangleModeButton;
    private LoomButton faceButton;
    private LoomButton resolutionDownButton;
    private LoomButton resolutionUpButton;
    private LoomButton brushDownButton;
    private LoomButton brushUpButton;
    private LoomButton resolutionLabelButton;
    private LoomButton brushLabelButton;
    private LoomButton symmetryButton;
    private LoomButton selectionClearButton;
    private LoomButton selectionLeftButton;
    private LoomButton selectionRightButton;
    private LoomButton selectionUpButton;
    private LoomButton selectionDownButton;
    private LoomButton selectionFlipHorizontalButton;
    private LoomButton selectionFlipVerticalButton;
    private LoomButton zoomOutButton;
    private LoomButton zoomLabelButton;
    private LoomButton zoomInButton;
    private LoomButton layerAddButton;
    private LoomButton layerDuplicateButton;
    private LoomButton layerDeleteButton;
    private LoomButton layerUpButton;
    private LoomButton layerDownButton;
    private EditBox layerNameField;
    private LoomButton layerRenameButton;
    private LoomButton layerBlendButton;
    private LoomButton layerEmissiveButton;
    private LoomButton layerLockButton;
    private LoomButton layerGradientAddButton;
    private LoomButton layerImportButton;
    private LoomButton gradientTypeButton;
    private LoomButton gradientAngleDownButton;
    private LoomButton gradientAngleButton;
    private LoomButton gradientAngleUpButton;
    private LoomButton gradientMoveLeftButton;
    private LoomButton gradientMoveUpButton;
    private LoomButton gradientMoveDownButton;
    private LoomButton gradientMoveRightButton;
    private LoomButton gradientScaleDownButton;
    private LoomButton gradientScaleLabelButton;
    private LoomButton gradientScaleUpButton;
    private LoomButton gradientMirrorHorizontalButton;
    private LoomButton gradientMirrorVerticalButton;
    private LoomButton gradientResetTransformButton;
    private LoomButton gradientStartButton;
    private LoomButton gradientEndButton;
    private LoomButton gradientRepeatButton;
    private LoomButton gradientDitherButton;
    private LoomButton gradientStopLabelButton;
    private LoomButton gradientStopPreviousButton;
    private LoomButton gradientStopNextButton;
    private LoomButton gradientStopAddButton;
    private LoomButton gradientStopRemoveButton;
    private LoomButton gradientStopPositionDownButton;
    private LoomButton gradientStopPositionUpButton;
    private LoomButton gradientStopColorButton;
    private LoomButton layerOpacityDownButton;
    private LoomButton layerOpacityLabelButton;
    private LoomButton layerOpacityUpButton;
    private LoomButton undoButton;
    private LoomButton redoButton;

    private LoomCapeFaceWidget canvasWidget;
    private LoomColorPickerWidget colorPicker;
    private LoomLayerListWidget layerListWidget;
    private LoomPlayerPreviewWidget previewWidget;
    private LoomPaletteButton paletteButton;
    private LoomButton inspectorLayersButton;
    private LoomButton inspectorColorButton;
    private LoomButton inspectorPropertiesButton;
    private EditBox hexColorField;
    private EditBox redColorField;
    private EditBox greenColorField;
    private EditBox blueColorField;
    private EditBox alphaColorField;
    private boolean syncingColorFields;
    private LoomPaletteWindow paletteWindow;

    private boolean paletteWindowVisible;
    private boolean paletteWindowPinned;
    private int paletteWindowX = Integer.MIN_VALUE;
    private int paletteWindowY = Integer.MIN_VALUE;

    private int toolPanelX;
    private int toolPanelY;
    private int toolPanelWidth;
    private int toolPanelHeight;

    private LoomWorkspaceLayout workspaceLayout;
    private java.util.List<AbstractWidget> layerWidgets = java.util.List.of();
    private java.util.List<AbstractWidget> colorWidgets = java.util.List.of();
    private java.util.List<AbstractWidget> propertyWidgets = java.util.List.of();
    private java.util.List<AbstractWidget> gradientBasics = java.util.List.of();
    private java.util.List<AbstractWidget> gradientTransform = java.util.List.of();
    private java.util.List<AbstractWidget> gradientStops = java.util.List.of();
    private LoomSlider opacitySlider;
    private int propertyPage;
    private LoomButton propertyPageButton;
    private LoomButton gridButton;
    private boolean compactMode;
    private InspectorTab inspectorTab = InspectorTab.LAYERS;
    private int contentTop;
    private int contentBottom;
    private int toolRailLeft;
    private int toolRailRight;
    private int canvasLeft;
    private int canvasRight;
    private int canvasTop;
    private int canvasBottom;
    private int contextTop;
    private int rightPanelLeft;
    private boolean inspectorDividerDragging;
    private int proposedInspectorWidth;
    private int rightPanelRight;
    private int previewBottom;
    private int inspectorTop;
    private int inspectorBottom;

    public CapeEditorScreen(Screen parent) {
        super(Component.literal("Loom Studios - Cape Editor"));
        this.parent = parent;
        this.workspaceState = ClientProjectWorkspace.state();
        this.selectedLayerId = findEditableLayer();
    }

    @Override
    protected void init() {
        tooltipLayerState="";
        LoomCapeFaceWidget.ViewState viewState = canvasWidget == null ? null : canvasWidget.viewState();
        LoomPlayerPreviewWidget.ViewState previewState = previewWidget == null ? null : previewWidget.viewState();
        if (canvasWidget != null) canvasWidget.close();
        workspaceTooSmall = width < 600 || height < 320;
        if (workspaceTooSmall) {
            ClientProjectWorkspace.endCompoundEdit();
            addRenderableWidget(new LoomButton(Math.max(0, (width - 100) / 2), height / 2 + 24, 100, 22,
                    Component.literal("Back"), this::onClose));
            return;
        }
        workspaceLayout = LoomWorkspaceLayout.create(width, height, false, 0,
                dev.loomstudios.client.ui.LoomInspectorResize.preference(width,height));
        compactMode = workspaceLayout.compact();
        var layout = workspaceLayout;
        contentTop = layout.tools().top(); contentBottom = layout.tools().bottom();
        toolRailLeft = layout.tools().left(); toolRailRight = layout.tools().right();
        canvasLeft = layout.canvas().left(); canvasRight = layout.canvas().right();
        canvasTop = layout.canvas().top(); canvasBottom = layout.canvas().bottom()-26;
        contextTop = layout.context().top();
        rightPanelLeft = layout.preview().left(); rightPanelRight = layout.preview().right();
        previewBottom = layout.preview().bottom();
        inspectorTop = layout.inspector().top(); inspectorBottom = layout.inspector().bottom();
        toolPanelX = rightPanelLeft; toolPanelY = inspectorTop;
        toolPanelWidth = rightPanelRight - rightPanelLeft; toolPanelHeight = inspectorBottom - inspectorTop;
        buildTopNavigation(layout.headerHeight(), layout.navHeight(), 8);
        buildToolRail(); buildCanvasToolbar(22); buildCanvas(); buildContextBar();
        int actionWidth=(canvasRight-canvasLeft- 8)/ 3;
        iconButton(canvasLeft,canvasBottom+3,actionWidth,22,"Animate cape",LoomButton.Icon.PLAY,()->minecraft.setScreen(new LoomAnimationScreen(this,dev.loomstudios.project.AnimationChannel.CAPE,selectedLayerId))).setIconOnly(false);
        iconButton(canvasLeft+actionWidth+4,canvasBottom+3,actionWidth,22,"Surface tools",LoomButton.Icon.SELECT,()->minecraft.setScreen(new LoomSurfaceToolsScreen(this,false,selectedLayerId,capeRegion,null,null,selectedColor)))
        .setIconOnly(false);
    iconButton(
            canvasLeft + 2 * (actionWidth + 4),
            canvasBottom + 3,
            canvasRight - (canvasLeft + 2 * (actionWidth + 4)),
            22,
            "Assets",
            LoomButton.Icon.IMAGE,
            () ->
                minecraft.setScreen(
                    new LoomAssetLibraryScreen(
                        this, false, selectedLayerId, capeRegion, null, null, selectedColor))).setIconOnly(false);
        buildRightPanel(layout.preview().height(), 22);
        canvasWidget.setShapeFilledSupplier(() -> rectangleFilled);
        canvasWidget.restoreViewState(viewState); previewWidget.restoreViewState(previewState);
        restoreOrCreatePaletteWindow(); syncColorFields(); syncLayerFields();
        updateButtonStates(); updateInspectorVisibility();
    }


    /** Restore selection after placing an independently editable asset in the library. */
    public void focusAssetLayer(UUID id) {
        if (id != null && (ClientProjectWorkspace.project().cape())
                .layers().stream().anyMatch(layer -> layer.id().equals(id))) {
            selectedLayerId = id;
        }
    }

    private void buildTopNavigation(int headerHeight, int navHeight, int margin) {
        int x = margin, y = headerHeight + 2, h = navHeight - 4;
        String[] labels = {"Home", "Cape", "Elytra", "Import", "Export"};
        LoomButton.Icon[] icons = {LoomButton.Icon.HOME, LoomButton.Icon.CAPE, LoomButton.Icon.ELYTRA, LoomButton.Icon.IMAGE, LoomButton.Icon.EXPORT};
        Runnable[] actions = {() -> dev.loomstudios.client.project.WorkspaceNavigation.request(this,()->minecraft.setScreen(new LoomHomeScreen())), () -> { }, () -> minecraft.setScreen(new ElytraEditorScreen(this)), this::openSmartImport,
                () -> minecraft.setScreen(new LoomCodesScreen(this, ClientProjectWorkspace.project()))};
        for (int i = 0; i < labels.length; i++) {
            int w = font.width(labels[i]) + 28;
            LoomButton button = navButton(x, y, w, h, labels[i], icons[i], i == 1, actions[i]);
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

    private void buildToolRail() {
        int x = toolRailLeft + 2;
        int buttonWidth = toolRailRight - toolRailLeft - 4;
        int buttonHeight = compactMode ? Math.min(25,(contentBottom-contentTop-10-7*3)/8) : 30;
        int gap = compactMode ? 3 : 4;
        int y = contentTop + 5;

        pencilButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Pencil", LoomButton.Icon.PENCIL,
                () -> setTool(Tool.PENCIL)
        );
        pencilButton.setIconOnly(compactMode);
        y += buttonHeight + gap;

        eraserButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Eraser", LoomButton.Icon.ERASER,
                () -> setTool(Tool.ERASER)
        );
        eraserButton.setIconOnly(compactMode);
        y += buttonHeight + gap;

        fillButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Fill", LoomButton.Icon.FILL,
                () -> setTool(Tool.FILL)
        );
        fillButton.setIconOnly(compactMode);
        y += buttonHeight + gap;

        eyedropperButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Eyedropper", LoomButton.Icon.EYEDROPPER,
                () -> setTool(Tool.EYEDROPPER)
        );
        eyedropperButton.setIconOnly(compactMode);
        y += buttonHeight + gap;

        selectButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Select", LoomButton.Icon.SELECT,
                () -> setTool(Tool.SELECT)
        );
        selectButton.setIconOnly(compactMode);
        y += buttonHeight + gap;

        lineButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Line", LoomButton.Icon.LINE,
                () -> setTool(Tool.LINE)
        );
        lineButton.setIconOnly(compactMode);
        y += buttonHeight + gap;

        rectangleButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Rectangle", LoomButton.Icon.RECTANGLE,
                () -> setTool(Tool.RECTANGLE)
        );
        rectangleButton.setIconOnly(compactMode);
        y += buttonHeight + gap;
        circleButton = iconButton(x,y,buttonWidth,buttonHeight,"Circle",LoomButton.Icon.CIRCLE,() -> setTool(Tool.CIRCLE));
        circleButton.setIconOnly(compactMode);
        circleButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Circle / ellipse (C) · drag bounds · Outline or Filled")));
    }

    private void buildCanvasToolbar(int toolbarHeight) {
        int y = contentTop, h = 22, x = canvasLeft, small = 20, gap = 3;
        faceButton = iconButton(x, y, compactMode ? 88 : 128, h, faceLabel(), LoomButton.Icon.CAPE, this::cycleFace).setIconOnly(false);
        x += faceButton.getWidth() + gap;
        resolutionDownButton = iconButton(x, y, small, h, "Resolution -", LoomButton.Icon.MINUS, () -> changeResolution(-1)).setIconOnly(true); x += small + gap;
        resolutionLabelButton = new LoomButton(x, y, compactMode ? 32 : 74, h, Component.literal(compactMode ? CanvasResolution.fromCanvas(workspaceState.project().cape()).label() : resolutionLabel()), () -> {});
        resolutionLabelButton.active = false; addRenderableWidget(resolutionLabelButton); x += resolutionLabelButton.getWidth() + gap;
        resolutionUpButton = iconButton(x, y, small, h, "Resolution +", LoomButton.Icon.PLUS, () -> changeResolution(1)).setIconOnly(true);
        int right = canvasRight;
        gridButton = iconButton(right - small, y, small, h, "Toggle pixel grid", LoomButton.Icon.GRID, () -> canvasWidget.toggleGrid()).setIconOnly(true); right -= small + gap;
        zoomInButton = iconButton(right - small, y, small, h, "Zoom in", LoomButton.Icon.ZOOM_IN, () -> canvasWidget.zoomIn()).setIconOnly(true); right -= small + gap;
        zoomLabelButton = new LoomButton(right - 36, y, 36, h, Component.literal("100%"), () -> canvasWidget.resetZoom());
        addRenderableWidget(zoomLabelButton); right -= 39;
        zoomOutButton = iconButton(right - small, y, small, h, "Zoom out", LoomButton.Icon.ZOOM_OUT, () -> canvasWidget.zoomOut()).setIconOnly(true);
    }


    private void buildCanvas() {
        this.canvasWidget = addRenderableWidget(
                new LoomCapeFaceWidget(
                        canvasLeft,
                        canvasTop,
                        Math.max(120, canvasRight - canvasLeft),
                        Math.max(90, canvasBottom - canvasTop),
                        () -> this.workspaceState.project(),
                        () -> this.workspaceState.revision(),
                        () -> this.capeRegion,
                        this::editPixel,
                        this::commitShape,
                        () -> this.selection,
                        this::setSelection,
                        new LoomCapeFaceWidget.StrokeLifecycle() {
                            @Override
                            public void begin() {
                                if(selectedLayer()!=null&&selectedLayer().editableAsPaint()&&tool!=Tool.ERASER&&tool!=Tool.SELECT&&tool!=Tool.EYEDROPPER)dev.loomstudios.client.palette.EditorColors.use(selectedColor);
                                ClientProjectWorkspace.beginCompoundEdit();
                            }

                            @Override
                            public void end() {
                                ClientProjectWorkspace.endCompoundEdit();
                                updateButtonStates();
                            }
                        },
                        () -> this.brushSize,
                        this::gestureMode
                )
        );
    }

    private java.util.List<LoomButton> clipboardButtons=new java.util.ArrayList<>();
    private void buildContextBar() {
        clipboardButtons.clear();
        int x = canvasLeft + 5, y = contextTop + 3, h = 20, small = 22;
        brushDownButton = iconButton(x, y, small, h, "Brush size -", LoomButton.Icon.MINUS, () -> changeBrushSize(-1)).setIconOnly(true);
        brushLabelButton = new LoomButton(x + 25, y, 72, h, Component.literal(brushLabel()), () -> {});
        brushLabelButton.active = false; addRenderableWidget(brushLabelButton);
        brushUpButton = iconButton(x + 100, y, small, h, "Brush size +", LoomButton.Icon.PLUS, () -> changeBrushSize(1)).setIconOnly(true);
        symmetryButton = iconButton(x + 128, y, 110, h, symmetryLabel(), LoomButton.Icon.FLIP_H, this::cycleSymmetry).setIconOnly(false);
        rectangleModeButton = iconButton(x + 244, y, compactMode ? 78 : 110, h, "Outline", LoomButton.Icon.RECTANGLE, this::toggleRectangleMode).setIconOnly(false);
        selectionLeftButton = iconButton(x, y, small, h, "Nudge left (←)", LoomButton.Icon.LEFT, () -> nudgeSelection(-1, 0)).setIconOnly(true);
        selectionUpButton = iconButton(x + 25, y, small, h, "Nudge up (↑)", LoomButton.Icon.UP, () -> nudgeSelection(0, -1)).setIconOnly(true);
        selectionDownButton = iconButton(x + 50, y, small, h, "Nudge down (↓)", LoomButton.Icon.DOWN, () -> nudgeSelection(0, 1)).setIconOnly(true);
        selectionRightButton = iconButton(x + 75, y, small, h, "Nudge right (→)", LoomButton.Icon.RIGHT, () -> nudgeSelection(1, 0)).setIconOnly(true);
        selectionFlipHorizontalButton = iconButton(x + 106, y, 25, h, "Flip H", LoomButton.Icon.FLIP_H, () -> flipSelection(true, false)).setIconOnly(false);
        selectionFlipVerticalButton = iconButton(x + 134, y, 25, h, "Flip V", LoomButton.Icon.FLIP_V, () -> flipSelection(false, true)).setIconOnly(false);
        selectionClearButton = iconButton(x + 162, y, 25, h, "Clear", LoomButton.Icon.SELECT, this::clearSelection).setIconOnly(true);
        selectionFlipHorizontalButton.setIconOnly(true);selectionFlipVerticalButton.setIconOnly(true);
        clipboardButtons.add(iconButton(x+190,y,25,h,"Rotate selection 90° (Ctrl+R)",LoomButton.Icon.ROTATE_RIGHT,this::rotatePixelSelection).setIconOnly(true));
        clipboardButtons.add(iconButton(x+218,y,25,h,"Copy pixels (Ctrl+C)",LoomButton.Icon.COPY,this::copyPixelSelection).setIconOnly(true));
        clipboardButtons.add(iconButton(x+246,y,25,h,"Paste pixels (Ctrl+V)",LoomButton.Icon.PLUS,this::pastePixelSelection).setIconOnly(true));
    }


    private void buildRightPanel(
            int previewHeight,
            int inspectorTabHeight
    ) {
        this.previewWidget = new LoomPlayerPreviewWidget(
                rightPanelLeft,
                contentTop,
                rightPanelRight - rightPanelLeft,
                previewHeight,
                () -> workspaceState.project(),
                LoomPlayerPreviewWidget.Mode.CAPE
        );
        addRenderableWidget(previewWidget);

        int tabsY = workspaceLayout.inspectorTabs().top();
        int tabGap = 3;
        int tabWidth = Math.max(
                42,
                (rightPanelRight - rightPanelLeft - tabGap * 3) / 4
        );

        inspectorLayersButton = navButton(
                rightPanelLeft,
                tabsY,
                tabWidth,
                inspectorTabHeight,
                "Layers",
                LoomButton.Icon.LAYERS,
                true,
                () -> setInspectorTab(InspectorTab.LAYERS)
        );
        addRenderableWidget(inspectorLayersButton);

        inspectorColorButton = navButton(
                rightPanelLeft + tabWidth + tabGap,
                tabsY,
                tabWidth,
                inspectorTabHeight,
                "Color",
                LoomButton.Icon.PALETTE,
                false,
                () -> setInspectorTab(InspectorTab.COLOR)
        );
        addRenderableWidget(inspectorColorButton);

        inspectorPropertiesButton = navButton(
                rightPanelLeft + (tabWidth + tabGap) * 2,
                tabsY,
                tabWidth,
                inspectorTabHeight,
                "Props",
                LoomButton.Icon.SETTINGS,
                false,
                () -> setInspectorTab(InspectorTab.PROPERTIES)
        );
        addRenderableWidget(inspectorPropertiesButton);

        inspectorLayersButton.setIcon(LoomButton.Icon.NONE).setIconOnly(false);
        inspectorColorButton.setIcon(LoomButton.Icon.NONE).setIconOnly(false);
        inspectorPropertiesButton.setIcon(LoomButton.Icon.NONE).setIconOnly(false);
        addRenderableWidget(new LoomButton(rightPanelLeft+3*(tabWidth+tabGap),tabsY,rightPanelRight-(rightPanelLeft+3*(tabWidth+tabGap)),inspectorTabHeight,Component.literal("Anim"),()->minecraft.setScreen(new LoomAnimationScreen(this,dev.loomstudios.project.AnimationChannel.CAPE,selectedLayerId))));
        buildLayerInspector(); buildColorInspector(); buildPropertyInspector();
    }

    private void buildLayerInspector() {
        int start = children().size();
        var rows = inspectorRows();
        int count = workspaceState.project().cape().layers().size();
        int listHeight = Math.min(Math.max(44, 20 + count * 18), workspaceLayout.inspector().height() - (inspectorRows().padding() + 2 * inspectorRows().stride() + 4));
        var r = rows.row(listHeight);
        layerListWidget = addRenderableWidget(new LoomLayerListWidget(r.left(), r.top(), r.width(), r.height(),
                () -> workspaceState.project().cape(), () -> selectedLayerId, this::selectLayer, this::toggleLayerVisibility, this::toggleLayerLock).setManage(()->minecraft.setScreen(new LoomLayerManagerScreen(this,false,selectedLayerId))));
        r = rows.row(22);
        layerAddButton = cellButton(r, 0, 3, "Add paint layer", LoomButton.Icon.PLUS, this::addLayer);
        layerGradientAddButton = cellButton(r, 1, 3, "Add gradient layer", LoomButton.Icon.GRADIENT, this::addGradientLayer);
        layerImportButton = cellButton(r, 2, 3, "Import image layer", LoomButton.Icon.IMAGE, this::openSmartImport);
        r = rows.row(22);
        layerDuplicateButton = cellButton(r, 0, 4, "Duplicate layer", LoomButton.Icon.COPY, this::duplicateLayer);
        layerUpButton = cellButton(r, 1, 4, "Move layer up", LoomButton.Icon.UP, () -> moveLayer(1));
        layerDownButton = cellButton(r, 2, 4, "Move layer down", LoomButton.Icon.DOWN, () -> moveLayer(-1));
        layerDeleteButton = cellButton(r, 3, 4, "Delete layer", LoomButton.Icon.DELETE, this::deleteLayer).setDanger(true);
        layerWidgets = widgetsSince(start);
    }


    private void buildColorInspector() {
        int start = children().size();
        var rows = inspectorRows();
        var r = rows.row(Math.min(118, workspaceLayout.inspector().height() - 84));
        colorPicker = addRenderableWidget(new LoomColorPickerWidget(r.left(), r.top(), r.width(), r.height(), selectedColor, this::setSelectedColor));
        r = rows.row(18);
        hexColorField = createHexColorField(r.width()); hexColorField.setX(r.left()); hexColorField.setY(r.top()); addRenderableWidget(hexColorField);
        r = rows.row(18);
        int w = (r.width() - 9) / 4;
        redColorField = createChannelField(w, "R", 16); greenColorField = createChannelField(w, "G", 8);
        blueColorField = createChannelField(w, "B", 0); alphaColorField = createAlphaField(r.width() - 3 * w - 9);
        EditBox[] channels = {redColorField, greenColorField, blueColorField, alphaColorField};
        for (int i = 0; i < channels.length; i++) { channels[i].setX(r.left() + i * (w + 3)); channels[i].setY(r.top()); addRenderableWidget(channels[i]); }
        r = rows.row(20);
        paletteButton = addRenderableWidget(new LoomPaletteButton(r.left(), r.top(), r.width(), r.height(), this::togglePaletteWindow));
        colorWidgets = widgetsSince(start);
    }


    private void buildPropertyInspector() {
        int start = children().size();
        var rows = inspectorRows(); var r = rows.row(22);
        layerNameField = new EditBox(font, r.left(), r.top(), r.width() - 27, r.height(), Component.literal("Layer name"));
        layerNameField.setMaxLength(dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS); addRenderableWidget(layerNameField);
        layerRenameButton = iconButton(r.right() - 24, r.top(), 24, r.height(), "Rename layer", LoomButton.Icon.PENCIL, this::renameLayer).setIconOnly(true);
        r = rows.row(22);
        opacitySlider = addRenderableWidget(new LoomSlider(r.left(), r.top(), r.width(), "Opacity", () -> selectedLayer().opacity(), value -> changeLayerOpacity((float)value - selectedLayer().opacity()))); opacitySlider.setHeight(r.height());
        r = rows.row(22);
        layerBlendButton = rowButton(r, "Blend: Normal", LoomButton.Icon.LAYERS, this::cycleLayerBlendMode);
        r = rows.row(22);
        layerEmissiveButton = cellButton(r, 0, 2, "Emissive", LoomButton.Icon.EMISSIVE, this::toggleLayerEmissive).setIconOnly(false);
        layerLockButton = cellButton(r, 1, 2, "Lock", LoomButton.Icon.LOCK, this::toggleLayerLock).setIconOnly(false);
        r = rows.row(22);
        rowButton(r, "Edit image", LoomButton.Icon.IMAGE, this::openSmartImport).setIconOnly(false);
        propertyWidgets = widgetsSince(start);
        // One explicit page selector. Layer / Gradient / Transform / Stops.
        r = new LoomWorkspaceLayout.Rect(rightPanelLeft + rows.padding(), inspectorTop + rows.padding(), rightPanelRight - rows.padding(), inspectorTop + rows.padding() + rows.controlHeight());
        propertyPageButton = rowButton(r, "Layer properties", LoomButton.Icon.SETTINGS, () -> { propertyPage = (propertyPage + 1) % (selectedLayer().kind() == LayerKind.GRADIENT ? 4 : 1); updateInspectorVisibility(); });
        // Move the layer rows below the page selector.
        for (AbstractWidget widget : propertyWidgets) widget.setY(widget.getY() + rows.stride());
        start = children().size(); rows = inspectorRows(); rows.row(22); r = rows.row(22);
        gradientTypeButton = rowButton(r, "Gradient: Linear", LoomButton.Icon.GRADIENT, this::cycleGradientType);
        r = rows.row(22);
        gradientAngleDownButton = cellButton(r, 0, 3, "Angle -", LoomButton.Icon.MINUS, () -> rotateGradient(-15));
        gradientAngleButton = cellButton(r, 1, 3, "0°", LoomButton.Icon.NONE, () -> {}); gradientAngleButton.active = false;
        gradientAngleUpButton = cellButton(r, 2, 3, "Angle +", LoomButton.Icon.PLUS, () -> rotateGradient(15));
        r = rows.row(22);
        gradientRepeatButton = cellButton(r, 0, 2, "Repeat", LoomButton.Icon.LOOP, this::toggleGradientRepeat).setIconOnly(true);
        gradientDitherButton = cellButton(r, 1, 2, "Dither", LoomButton.Icon.GRADIENT, this::toggleGradientDither).setIconOnly(true);
        gradientBasics = widgetsSince(start);
        start = children().size(); rows = inspectorRows(); rows.row(22); r = rows.row(22);
        gradientMoveLeftButton = cellButton(r, 0, 4, "Left", LoomButton.Icon.LEFT, () -> moveGradient(-1,0));
        gradientMoveUpButton = cellButton(r, 1, 4, "Up", LoomButton.Icon.UP, () -> moveGradient(0,-1));
        gradientMoveDownButton = cellButton(r, 2, 4, "Down", LoomButton.Icon.DOWN, () -> moveGradient(0,1));
        gradientMoveRightButton = cellButton(r, 3, 4, "Right", LoomButton.Icon.RIGHT, () -> moveGradient(1,0));
        r = rows.row(22);
        gradientScaleDownButton = cellButton(r, 0, 3, "Scale -", LoomButton.Icon.MINUS, () -> scaleGradient(0.9));
        gradientScaleLabelButton = cellButton(r, 1, 3, "100%", LoomButton.Icon.NONE, () -> {}); gradientScaleLabelButton.active = false;
        gradientScaleUpButton = cellButton(r, 2, 3, "Scale +", LoomButton.Icon.PLUS, () -> scaleGradient(1.1));
        r = rows.row(22);
        gradientMirrorHorizontalButton = cellButton(r, 0, 2, "Mirror H", LoomButton.Icon.FLIP_H, () -> toggleGradientMirror(true));
        gradientMirrorVerticalButton = cellButton(r, 1, 2, "Mirror V", LoomButton.Icon.FLIP_V, () -> toggleGradientMirror(false));
        r = rows.row(22); gradientResetTransformButton = rowButton(r, "Reset transform", LoomButton.Icon.RESET, this::resetGradientTransform);
        gradientTransform = widgetsSince(start);
        start = children().size(); rows = inspectorRows(); rows.row(22); r = rows.row(22);
        gradientStopPreviousButton = cellButton(r, 0, 3, "Previous stop", LoomButton.Icon.LEFT, () -> selectGradientStop(-1));
        gradientStopLabelButton = cellButton(r, 1, 3, "Stop", LoomButton.Icon.NONE, () -> {}); gradientStopLabelButton.active = false;
        gradientStopNextButton = cellButton(r, 2, 3, "Next stop", LoomButton.Icon.RIGHT, () -> selectGradientStop(1));
        r = rows.row(22);
        gradientStopAddButton = cellButton(r, 0, 2, "Add stop", LoomButton.Icon.PLUS, this::addGradientStop);
        gradientStopRemoveButton = cellButton(r, 1, 2, "Remove stop", LoomButton.Icon.DELETE, this::removeGradientStop);
        r = rows.row(22);
        gradientStopPositionDownButton = cellButton(r, 0, 2, "Position -", LoomButton.Icon.MINUS, () -> moveGradientStop(-0.05));
        gradientStopPositionUpButton = cellButton(r, 1, 2, "Position +", LoomButton.Icon.PLUS, () -> moveGradientStop(0.05));
        r = rows.row(22); gradientStopColorButton = rowButton(r, "Use current color", LoomButton.Icon.PALETTE, this::setSelectedGradientStopColor);
        gradientStops = widgetsSince(start);
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
        this.inspectorTab = tab;
        updateInspectorVisibility();
    }

    private void updateInspectorVisibility() {
        if (layerListWidget != null) {
            int count = workspaceState.project().cape().layers().size();
            int target = Math.min(Math.max(44, 20 + count * 18), workspaceLayout.inspector().height() - (inspectorRows().padding() + 2 * inspectorRows().stride() + 4));
            int delta = target - layerListWidget.getHeight();
            if (delta != 0) {
                layerListWidget.setHeight(target);
                for (AbstractWidget widget : layerWidgets) if (widget != layerListWidget) widget.setY(widget.getY() + delta);
            }
        }

        boolean layers = inspectorTab == InspectorTab.LAYERS, color = inspectorTab == InspectorTab.COLOR, properties = inspectorTab == InspectorTab.PROPERTIES;
        boolean gradient = selectedLayer().kind() == LayerKind.GRADIENT;
        if (!gradient) propertyPage = 0;
        inspectorLayersButton.setSelected(layers); inspectorColorButton.setSelected(color); inspectorPropertiesButton.setSelected(properties);
        show(layerWidgets, layers); show(colorWidgets, color);
        show(propertyWidgets, properties && propertyPage == 0);
        show(gradientBasics, properties && gradient && propertyPage == 1);
        show(gradientTransform, properties && gradient && propertyPage == 2);
        show(gradientStops, properties && gradient && propertyPage == 3);
        propertyPageButton.visible = properties;
        propertyPageButton.setMessage(Component.literal(switch (propertyPage) { case 1 -> "Gradient settings  ›"; case 2 -> "Transform  ›"; case 3 -> "Color stops  ›"; default -> gradient ? "Layer properties  ›" : "Layer properties"; }));
        // The image editor action exists only for image layers.
        if (!propertyWidgets.isEmpty()) propertyWidgets.getLast().visible = properties && propertyPage == 0 && selectedLayer().kind() == LayerKind.IMAGE;
        opacitySlider.active = !selectedLayer().locked();
        if (gridButton != null && canvasWidget != null) gridButton.setSelected(canvasWidget.gridVisible());
        updateContextVisibility();
    }


    private void updateContextVisibility() {
        boolean selectionTool = tool == Tool.SELECT;
        boolean rectangleTool = (tool == Tool.RECTANGLE || tool == Tool.CIRCLE);
        if(rectangleModeButton != null) rectangleModeButton.setIcon(tool == Tool.CIRCLE ? LoomButton.Icon.CIRCLE : LoomButton.Icon.RECTANGLE);
        boolean brushTool = tool == Tool.PENCIL
                || tool == Tool.ERASER
                || tool == Tool.LINE
                || (tool == Tool.RECTANGLE || tool == Tool.CIRCLE);

        if (brushDownButton != null) brushDownButton.visible = brushTool;
        if (brushLabelButton != null) brushLabelButton.visible = brushTool;
        if (brushUpButton != null) brushUpButton.visible = brushTool;
        if (symmetryButton != null) symmetryButton.visible = brushTool;
        if (rectangleModeButton != null) rectangleModeButton.visible = rectangleTool;

        if (selectionClearButton != null) selectionClearButton.visible = selectionTool;
        if (selectionLeftButton != null) selectionLeftButton.visible = selectionTool;
        if (selectionRightButton != null) selectionRightButton.visible = selectionTool;
        if (selectionUpButton != null) selectionUpButton.visible = selectionTool;
        if (selectionDownButton != null) selectionDownButton.visible = selectionTool;
        if (selectionFlipHorizontalButton != null) selectionFlipHorizontalButton.visible = selectionTool;
        if (selectionFlipVerticalButton != null) selectionFlipVerticalButton.visible = selectionTool;
    }

    @Override
    public void added() {
        super.added();
        ClientProjectWorkspace.addListener(workspaceListener);
    }

    @Override
    public void removed() {
        ClientProjectWorkspace.removeListener(workspaceListener);

        if (canvasWidget != null) {
            canvasWidget.close();
        }

        if (paletteWindow != null) {
            this.paletteWindowX = paletteWindow.getX();
            this.paletteWindowY = paletteWindow.getY();
            this.paletteWindowPinned = paletteWindow.pinned();
        }

        ClientProjectWorkspace.endCompoundEdit();
        super.removed();
    }

    private EditBox createHexColorField(int width) {
        EditBox field = new EditBox(
                this.font,
                0,
                0,
                width,
                18,
                Component.literal("Hex color")
        );
        field.setHint(Component.literal("#RRGGBB"));
        field.setMaxLength(7);
        field.setFilter(value ->
                value.matches("#?[0-9A-Fa-f]{0,6}")
        );
        field.setResponder(this::applyHexColorField);
        return field;
    }

    private EditBox createChannelField(
            int width,
            String hint,
            int shift
    ) {
        EditBox field = new EditBox(
                this.font,
                0,
                0,
                Math.max(30, width),
                18,
                Component.literal(hint + " channel")
        );
        field.setHint(Component.literal(hint));
        field.setMaxLength(3);
        field.setFilter(value -> value.matches("[0-9]{0,3}"));
        field.setResponder(ignored -> applyNumericColorFields());
        return field;
    }

    private EditBox createAlphaField(int width) {
        EditBox field = new EditBox(
                this.font,
                0,
                0,
                Math.max(30, width),
                18,
                Component.literal("Alpha channel")
        );
        field.setHint(Component.literal("A"));
        field.setMaxLength(3);
        field.setFilter(value -> value.matches("[0-9]{0,3}"));
        field.setResponder(ignored -> applyNumericColorFields());
        return field;
    }

    private LoomButton createLoomButton(
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

    private void cycleFace() {
        this.capeRegion = this.capeRegion.next();
        clearSelection();
        if (faceButton != null) {
            faceButton.setMessage(Component.literal(faceLabel()));
        }
    }

    private String faceLabel() {
        return switch (capeRegion) {
            case OUTSIDE -> "Back";
            case INSIDE -> "Front";
            default -> capeRegion.displayName();
        };
    }


    private void togglePaletteWindow() {
        this.paletteWindowVisible = !this.paletteWindowVisible;

        if (this.paletteWindow != null) {
            this.paletteWindow.visible = this.paletteWindowVisible;
        }
    }

    private void restoreOrCreatePaletteWindow() {
        if (this.paletteWindow != null) {
            this.paletteWindowX = this.paletteWindow.getX();
            this.paletteWindowY = this.paletteWindow.getY();
            this.paletteWindowPinned = this.paletteWindow.pinned();
        }

        boolean compact = this.width <= 700 || this.height <= 420;

        int width = compact
                ? Math.min(188, Math.max(168, this.width / 4 + 20))
                : 230;
        int height = compact
                ? Math.min(236, Math.max(190, this.height - 48))
                : Math.min(302, Math.max(260, this.height - 28));

        int defaultX = Math.max(
                8,
                this.toolPanelX - width - 10
        );
        int defaultY = Math.max(8, this.toolPanelY);

        int x = this.paletteWindowX == Integer.MIN_VALUE
                ? defaultX
                : this.paletteWindowX;
        int y = this.paletteWindowY == Integer.MIN_VALUE
                ? defaultY
                : this.paletteWindowY;

        this.paletteWindow = new LoomPaletteWindow(
                x,
                y,
                width,
                height,
                this.paletteWindowPinned,
                () -> this.selectedColor,
                this::setSelectedColor,
                this::notifyPlayer,
                () -> this.width,
                () -> this.height
        );
        this.paletteWindow.setOnClose(this::closePaletteWindow);
        this.paletteWindow.visible = this.paletteWindowVisible;
        addRenderableWidget(this.paletteWindow);
        this.paletteWindow.moveTo(x, y);
    }

    private void setSelectedColor(int color) {
        this.selectedColor = color;

        if (this.colorPicker != null
                && this.colorPicker.color() != color) {
            this.colorPicker.setColor(color);
        }

        syncColorFields();
    }

    private void applyHexColorField(String value) {
        if (syncingColorFields) {
            return;
        }

        String normalized = value.startsWith("#")
                ? value.substring(1)
                : value;

        if (normalized.length() != 6) {
            return;
        }

        try {
            int rgb = Integer.parseInt(normalized, 16);
            int alpha = selectedColor & 0xFF000000;
            setSelectedColor(alpha | rgb);
        } catch (NumberFormatException ignored) {
        }
    }

    private void applyNumericColorFields() {
        if (syncingColorFields
                || redColorField == null
                || greenColorField == null
                || blueColorField == null
                || alphaColorField == null) {
            return;
        }

        try {
            int red = Integer.parseInt(redColorField.getValue());
            int green = Integer.parseInt(greenColorField.getValue());
            int blue = Integer.parseInt(blueColorField.getValue());
            int alpha = Integer.parseInt(alphaColorField.getValue());

            if (red > 255
                    || green > 255
                    || blue > 255
                    || alpha > 255) {
                return;
            }

            setSelectedColor(
                    (alpha << 24)
                            | (red << 16)
                            | (green << 8)
                            | blue
            );
        } catch (NumberFormatException ignored) {
        }
    }

    private void syncColorFields() {
        if (hexColorField == null) {
            return;
        }

        syncingColorFields = true;

        if (!hexColorField.isFocused()) {
            hexColorField.setValue(
                    String.format("#%06X", selectedColor & 0x00FFFFFF)
            );
        }
        if (!redColorField.isFocused()) {
            redColorField.setValue(
                    Integer.toString((selectedColor >>> 16) & 0xFF)
            );
        }
        if (!greenColorField.isFocused()) {
            greenColorField.setValue(
                    Integer.toString((selectedColor >>> 8) & 0xFF)
            );
        }
        if (!blueColorField.isFocused()) {
            blueColorField.setValue(
                    Integer.toString(selectedColor & 0xFF)
            );
        }
        if (!alphaColorField.isFocused()) {
            alphaColorField.setValue(
                    Integer.toString((selectedColor >>> 24) & 0xFF)
            );
        }

        syncingColorFields = false;
    }

    private String resolutionLabel() {
        CanvasResolution resolution =
                CanvasResolution.fromCanvas(this.workspaceState.project().cape());
        return resolution.label()
                + " "
                + resolution.width()
                + "x"
                + resolution.height();
    }

    private void changeResolution(int delta) {
        CanvasResolution current =
                CanvasResolution.fromCanvas(ClientProjectWorkspace.project().cape());
        CanvasResolution next = delta > 0 ? current.higher() : current.lower();

        if (next == current) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectResizer.resizeCape(project, next)
        );
        clearSelection();

        int maxBrush = next.scale() * 8;
        this.brushSize = Math.min(this.brushSize, maxBrush);
        updateButtonStates();
    }

    private String brushLabel() {
        return "Size " + brushSize + " px";
    }

    private void changeBrushSize(int delta) {
        int max = Math.max(
                1,
                CanvasResolution.fromCanvas(
                        ClientProjectWorkspace.project().cape()
                ).scale() * 8
        );
        this.brushSize = Math.max(1, Math.min(max, this.brushSize + delta));

        if (canvasWidget != null) {
            canvasWidget.showBrushSizePreview();
        }

        updateButtonStates();
    }

    private void toggleRectangleMode() {
        this.rectangleFilled = !this.rectangleFilled;
        updateButtonStates();
    }

    private void cycleSymmetry() {
        this.symmetryMode = this.symmetryMode.next();
        updateButtonStates();
    }

    private String symmetryLabel() {
        return "Mirror: " + switch (symmetryMode) { case NONE -> "Off"; case HORIZONTAL -> "H"; case VERTICAL -> "V"; case BOTH -> "H+V"; };
    }

    private LoomCapeFaceWidget.GestureMode gestureMode() {
        return switch (tool) {
            case PENCIL, ERASER -> LoomCapeFaceWidget.GestureMode.BRUSH;
            case LINE -> LoomCapeFaceWidget.GestureMode.LINE;
            case RECTANGLE -> LoomCapeFaceWidget.GestureMode.RECTANGLE;
            case CIRCLE -> LoomCapeFaceWidget.GestureMode.CIRCLE;
            case SELECT -> LoomCapeFaceWidget.GestureMode.SELECTION;
            case FILL, EYEDROPPER -> LoomCapeFaceWidget.GestureMode.CLICK;
        };
    }

    private String tooltipLayerState="";
    private void setTool(Tool next) {
        this.tool = next;
        updateButtonStates();
        updateContextVisibility();
    }

    private void updateButtonStates() {
        if (pencilButton != null) {
            pencilButton.setMessage(Component.literal("Pencil"));
            pencilButton.setSelected(tool == Tool.PENCIL);
        }

        if (eraserButton != null) {
            eraserButton.setMessage(Component.literal("Eraser"));
            eraserButton.setSelected(tool == Tool.ERASER);
        }

        if (fillButton != null) {
            fillButton.setMessage(Component.literal("Fill"));
            fillButton.setSelected(tool == Tool.FILL);
        }

        if (eyedropperButton != null) {
            eyedropperButton.setMessage(Component.literal("Eyedropper"));
            eyedropperButton.setSelected(tool == Tool.EYEDROPPER);
        }

        if (lineButton != null) {
            lineButton.setMessage(Component.literal("Line"));
            lineButton.setSelected(tool == Tool.LINE);
        }

        if (rectangleButton != null) {
            rectangleButton.setMessage(Component.literal("Rectangle"));
            rectangleButton.setSelected(tool == Tool.RECTANGLE);
        }

        if (circleButton != null) circleButton.setSelected(tool == Tool.CIRCLE);

        if (selectButton != null) {
            selectButton.setMessage(Component.literal("Select"));
            selectButton.setSelected(tool == Tool.SELECT);
        }
        String state=selectedLayer()==null?"none":selectedLayer().kind()+":"+selectedLayer().locked();
        if(!state.equals(tooltipLayerState)){tooltipLayerState=state;LoomButton[] buttons={pencilButton,eraserButton,fillButton,eyedropperButton,selectButton,lineButton,rectangleButton,circleButton};String[] tools={"PENCIL","ERASER","FILL","EYEDROPPER","SELECT","LINE","RECTANGLE","CIRCLE"};for(int i=0;i<buttons.length;i++)if(buttons[i]!=null)buttons[i].setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(dev.loomstudios.client.ui.LoomToolGuidance.tooltip(tools[i],selectedLayer(),false))));}

        boolean editablePaintSelection = workspaceState != null
                && !workspaceState.project().cape().layers().isEmpty()
                && selectedLayer().editableAsPaint();
        boolean hasSelection = selection != null && editablePaintSelection;
        for(int i=0;i<clipboardButtons.size();i++){LoomButton b=clipboardButtons.get(i);b.visible=tool==Tool.SELECT;b.active=editablePaintSelection&&(i==2?dev.loomstudios.client.project.PixelClipboard.patch!=null:selection!=null);}
        if (selectionClearButton != null) {
            selectionClearButton.active = hasSelection;
        }
        if (selectionLeftButton != null) {
            selectionLeftButton.active = hasSelection && selection.minX() > 0;
        }
        if (selectionRightButton != null) {
            int scale = CanvasResolution.fromCanvas(
                    this.workspaceState.project().cape()
            ).scale();
            selectionRightButton.active = hasSelection
                    && selection.maxX() + 1 < this.capeRegion.width(scale);
        }
        if (selectionUpButton != null) {
            selectionUpButton.active = hasSelection && selection.minY() > 0;
        }
        if (selectionDownButton != null) {
            int scale = CanvasResolution.fromCanvas(
                    this.workspaceState.project().cape()
            ).scale();
            selectionDownButton.active = hasSelection
                    && selection.maxY() + 1 < this.capeRegion.height(scale);
        }
        if (selectionFlipHorizontalButton != null) {
            selectionFlipHorizontalButton.active = hasSelection;
        }
        if (selectionFlipVerticalButton != null) {
            selectionFlipVerticalButton.active = hasSelection;
        }

        if (rectangleModeButton != null) {
            rectangleModeButton.setMessage(Component.literal(
                    rectangleFilled
                            ? "Filled"
                            : "Outline"
            ));
        }

        if (undoButton != null) {
            undoButton.active = ClientProjectWorkspace.session().canUndo();
        }

        if (redoButton != null) {
            redoButton.active = ClientProjectWorkspace.session().canRedo();
        }

        if (resolutionLabelButton != null) {
            resolutionLabelButton.setMessage(Component.literal(compactMode ? CanvasResolution.fromCanvas(workspaceState.project().cape()).label() : resolutionLabel()));
        }
        if (brushLabelButton != null) {
            brushLabelButton.setMessage(Component.literal(brushLabel()));
        }

        if (symmetryButton != null) {
            symmetryButton.setMessage(Component.literal(symmetryLabel()));
        }

        if (canvasWidget != null) {
            if (zoomLabelButton != null) {
                zoomLabelButton.setMessage(
                        Component.literal(canvasWidget.zoomPercent() + "%")
                );
            }
            if (zoomOutButton != null) {
                zoomOutButton.active = canvasWidget.canZoomOut();
            }
            if (zoomInButton != null) {
                zoomInButton.active = canvasWidget.canZoomIn();
            }
        }

        CanvasResolution resolution =
                CanvasResolution.fromCanvas(this.workspaceState.project().cape());

        if (resolutionDownButton != null) {
            resolutionDownButton.active = resolution != CanvasResolution.STANDARD;
        }
        if (resolutionUpButton != null) {
            resolutionUpButton.active = resolution != CanvasResolution.MAXIMUM;
        }

        if (brushDownButton != null) {
            brushDownButton.active = brushSize > 1;
        }
        if (brushUpButton != null) {
            brushUpButton.active = brushSize
                    < CanvasResolution.fromCanvas(
                            this.workspaceState.project().cape()
                    ).scale() * 8;
        }

        if (workspaceState != null
                && !workspaceState.project().cape().layers().isEmpty()) {
            ensureSelectedLayerExists();

            var layers = workspaceState.project().cape().layers();
            int selectedIndex = -1;
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i).id().equals(selectedLayerId)) {
                    selectedIndex = i;
                    break;
                }
            }

            LoomLayer layer = selectedLayer();
            boolean editablePaint = layer.editableAsPaint();
            boolean editableGradient =
                    layer.kind() == LayerKind.GRADIENT && !layer.locked();

            if (pencilButton != null) {
                pencilButton.active = editablePaint;
            }
            if (eraserButton != null) {
                eraserButton.active = editablePaint;
            }
            if (fillButton != null) {
                fillButton.active = editablePaint;
            }
            if (lineButton != null) {
                lineButton.active = editablePaint;
            }
            if (rectangleButton != null) {
                rectangleButton.active = editablePaint;
            }
            if (rectangleModeButton != null) {
                rectangleModeButton.active = editablePaint;
            }
            if (circleButton != null) circleButton.active = editablePaint;

            if (selectButton != null) {
                selectButton.active = editablePaint;
            }
            if (symmetryButton != null) {
                symmetryButton.active = editablePaint;
            }

            if (layerDeleteButton != null) {
                layerDeleteButton.active = layers.size() > 1;
            }
            if (layerDuplicateButton != null) {
                layerDuplicateButton.active =
                        layers.size() < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
            }
            if (layerAddButton != null) {
                layerAddButton.active =
                        layers.size() < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
            }
            if (layerGradientAddButton != null) {
                layerGradientAddButton.active =
                        layers.size() < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
            }
            if (layerImportButton != null) {
                boolean editingImage = layer.kind() == LayerKind.IMAGE;
                layerImportButton.active = editingImage
                        ? !layer.locked()
                        : layers.size()
                                < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
                layerImportButton.setMessage(Component.literal(
                        editingImage ? "Edit Image" : "Import PNG"
                ));
            }
            if (layerUpButton != null) {
                layerUpButton.active =
                        selectedIndex >= 0 && selectedIndex < layers.size() - 1;
            }
            if (layerDownButton != null) {
                layerDownButton.active = selectedIndex > 0;
            }
            if (layerOpacityDownButton != null) {
                layerOpacityDownButton.active = layer.opacity() > 0.0F;
            }
            if (layerOpacityUpButton != null) {
                layerOpacityUpButton.active = layer.opacity() < 1.0F;
            }
            if (layerOpacityLabelButton != null) {
                layerOpacityLabelButton.setMessage(
                        Component.literal(
                                "Opacity "
                                        + Math.round(layer.opacity() * 100.0F)
                                        + "%"
                        )
                );
            }
            if (layerBlendButton != null) {
                layerBlendButton.setMessage(
                        Component.literal(
                                "Blend: " + layer.blendMode().displayName()
                        )
                );
            }
            if (layerEmissiveButton != null) {
                layerEmissiveButton.setMessage(
                        Component.literal(
                                layer.emissive()
                                        ? "Glow: On"
                                        : "Glow: Off"
                        )
                );
            }
            if (layerLockButton != null) {
                layerLockButton.setMessage(Component.literal(
                        "Lock: " + (layer.locked() ? "On" : "Off")
                ));
            }

            GradientLayerData gradient = layer.gradientData();
            if (gradientTypeButton != null) {
                gradientTypeButton.active = editableGradient;
                gradientTypeButton.setMessage(Component.literal(
                        gradient == null
                                ? "Gradient Type"
                                : "Gradient: "
                                        + gradient.type().displayName()
                ));
            }
            if (gradientAngleDownButton != null) {
                gradientAngleDownButton.active = editableGradient;
            }
            if (gradientAngleButton != null) {
                gradientAngleButton.setMessage(Component.literal(
                        gradient == null
                                ? "Angle"
                                : Math.round(
                                        gradient.transform()
                                                .rotationDegrees()
                                ) + "°"
                ));
            }
            if (gradientAngleUpButton != null) {
                gradientAngleUpButton.active = editableGradient;
            }
            if (gradientMoveLeftButton != null) {
                gradientMoveLeftButton.active = editableGradient;
            }
            if (gradientMoveUpButton != null) {
                gradientMoveUpButton.active = editableGradient;
            }
            if (gradientMoveDownButton != null) {
                gradientMoveDownButton.active = editableGradient;
            }
            if (gradientMoveRightButton != null) {
                gradientMoveRightButton.active = editableGradient;
            }
            if (gradientScaleDownButton != null) {
                gradientScaleDownButton.active = editableGradient;
            }
            if (gradientScaleLabelButton != null) {
                gradientScaleLabelButton.setMessage(Component.literal(
                        gradient == null
                                ? "Scale"
                                : GradientAuthoring.scalePercent(gradient)
                                        + "%"
                ));
            }
            if (gradientScaleUpButton != null) {
                gradientScaleUpButton.active = editableGradient;
            }
            if (gradientMirrorHorizontalButton != null) {
                gradientMirrorHorizontalButton.active = editableGradient;
                gradientMirrorHorizontalButton.setMessage(
                        Component.literal(
                                gradient != null
                                        && gradient.transform()
                                                .mirrorHorizontal()
                                        ? "H: On"
                                        : "Mirror H"
                        )
                );
            }
            if (gradientMirrorVerticalButton != null) {
                gradientMirrorVerticalButton.active = editableGradient;
                gradientMirrorVerticalButton.setMessage(
                        Component.literal(
                                gradient != null
                                        && gradient.transform()
                                                .mirrorVertical()
                                        ? "V: On"
                                        : "Mirror V"
                        )
                );
            }
            if (gradientResetTransformButton != null) {
                gradientResetTransformButton.active = editableGradient;
            }
            if (gradientStartButton != null) {
                gradientStartButton.active = editableGradient;
            }
            if (gradientEndButton != null) {
                gradientEndButton.active = editableGradient;
            }
            if (gradientRepeatButton != null) {
                gradientRepeatButton.active = editableGradient;
                gradientRepeatButton.setMessage(Component.literal(
                        "Repeat: "
                                + (gradient != null && gradient.repeat()
                                        ? "On"
                                        : "Off")
                ));
            }
            if (gradientDitherButton != null) {
                gradientDitherButton.active = editableGradient;
                gradientDitherButton.setMessage(Component.literal(
                        "Dither: "
                                + (gradient != null && gradient.dither()
                                        ? "On"
                                        : "Off")
                ));
            }

            if (gradient != null) {
                int stopIndex = clampedGradientStopIndex(gradient);
                GradientStop stop = gradient.stops().get(stopIndex);
                int stopCount = gradient.stops().size();

                if (gradientStopLabelButton != null) {
                    gradientStopLabelButton.setMessage(
                            Component.literal(
                                    "" + (stopIndex + 1)
                                            + "/"
                                            + stopCount
                                            + " "
                                            + Math.round(
                                                    stop.position() * 100.0
                                            )
                                            + "%"
                            )
                    );
                }
                if (gradientStopPreviousButton != null) {
                    gradientStopPreviousButton.active =
                            stopIndex > 0;
                }
                if (gradientStopNextButton != null) {
                    gradientStopNextButton.active =
                            stopIndex + 1 < stopCount;
                }
                if (gradientStopAddButton != null) {
                    gradientStopAddButton.active =
                            editableGradient
                                    && stopCount
                                    < dev.loomstudios.project.LoomProjectCodec.MAX_GRADIENT_STOPS;
                }
                if (gradientStopRemoveButton != null) {
                    gradientStopRemoveButton.active =
                            editableGradient && stopCount > 2;
                }
                if (gradientStopPositionDownButton != null) {
                    gradientStopPositionDownButton.active =
                            editableGradient
                                    && stop.position()
                                    > (stopIndex == 0
                                            ? 0.0
                                            : gradient.stops()
                                                    .get(stopIndex - 1)
                                                    .position()
                                                    + 0.001);
                }
                if (gradientStopPositionUpButton != null) {
                    gradientStopPositionUpButton.active =
                            editableGradient
                                    && stop.position()
                                    < (stopIndex + 1 == stopCount
                                            ? 1.0
                                            : gradient.stops()
                                                    .get(stopIndex + 1)
                                                    .position()
                                                    - 0.001);
                }
                if (gradientStopColorButton != null) {
                    gradientStopColorButton.active = editableGradient;
                }
            } else {
                if (gradientStopLabelButton != null) {
                    gradientStopLabelButton.setMessage(
                            Component.literal("Stop")
                    );
                }
                if (gradientStopPreviousButton != null) {
                    gradientStopPreviousButton.active = false;
                }
                if (gradientStopNextButton != null) {
                    gradientStopNextButton.active = false;
                }
                if (gradientStopAddButton != null) {
                    gradientStopAddButton.active = false;
                }
                if (gradientStopRemoveButton != null) {
                    gradientStopRemoveButton.active = false;
                }
                if (gradientStopPositionDownButton != null) {
                    gradientStopPositionDownButton.active = false;
                }
                if (gradientStopPositionUpButton != null) {
                    gradientStopPositionUpButton.active = false;
                }
                if (gradientStopColorButton != null) {
                    gradientStopColorButton.active = false;
                }
            }
            if (layerRenameButton != null) {
                layerRenameButton.active = layerNameField != null
                        && !layerNameField.getValue().trim().isEmpty();
            }
        }

        updateInspectorVisibility();
    }

    private UUID findEditableLayer() {
        var layers = ClientProjectWorkspace.project().cape().layers();

        return layers.stream()
                .filter(LoomLayer::editableAsPaint)
                .findFirst()
                .or(() -> layers.stream().findFirst())
                .map(LoomLayer::id)
                .orElseThrow(() -> new IllegalStateException(
                        "Cape project has no layers"
                ));
    }

    private void ensureSelectedLayerExists() {
        if (workspaceState == null
                || workspaceState.project().cape().layers().isEmpty()) {
            return;
        }

        boolean exists = selectedLayerId != null
                && workspaceState.project().cape().layers().stream()
                .anyMatch(layer -> layer.id().equals(selectedLayerId));

        if (!exists) {
            selectedLayerId = workspaceState.project()
                    .cape()
                    .layers()
                    .getLast()
                    .id();
        }
    }

    private LoomLayer selectedLayer() {
        ensureSelectedLayerExists();

        return workspaceState.project().cape().layers().stream()
                .filter(layer -> layer.id().equals(selectedLayerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Selected cape layer does not exist"
                ));
    }

    private void selectLayer(UUID layerId) {
        this.selectedLayerId = layerId;
        this.selectedGradientStopIndex = 0;
        this.selection = null;
        syncLayerFields();
        updateButtonStates();
    }

    private void toggleLayerVisibility(UUID layerId) {
        LoomLayer layer = workspaceState.project().cape().layers().stream()
                .filter(candidate -> candidate.id().equals(layerId))
                .findFirst()
                .orElse(null);

        if (layer == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeLayerVisible(
                        project,
                        layerId,
                        !layer.visible()
                )
        );
        updateButtonStates();
    }

    private void addLayer() {
        var result = ClientProjectWorkspace.apply(project ->
                ProjectEdits.addCapeLayer(project, "Layer")
        );
        selectedLayerId = result.cape().layers().getLast().id();
        syncLayerFields();
        updateButtonStates();
    }

    private void addGradientLayer() {
        LoomProject project = ClientProjectWorkspace.project();
        NormalizedRect target = activeFaceRect(project);
        int complement = (selectedColor & 0xFF000000)
                | ((~selectedColor) & 0x00FFFFFF);

        GradientLayerData gradient =
                GradientLayerData.defaultLinear(
                        selectedColor,
                        complement,
                        target
                );

        var result = ClientProjectWorkspace.apply(current ->
                ProjectEdits.addCapeGradientLayer(
                        current,
                        "Gradient",
                        gradient
                )
        );

        selectedLayerId = result.cape().layers().getLast().id();
        this.selectedGradientStopIndex = 0;
        this.selection = null;
        this.inspectorTab = InspectorTab.PROPERTIES;
        syncLayerFields();
        updateButtonStates();
    }

    private void openSmartImport() {
        this.selection = null;
        LoomLayer layer = selectedLayer();

        if (layer.kind() == LayerKind.IMAGE) {
            if (layer.locked()) {
                return;
            }

            this.minecraft.setScreen(
                    new SmartImportScreen(
                            this,
                            this.capeRegion,
                            layer.id(),
                            layer
                    )
            );
            return;
        }

        this.minecraft.setScreen(
                new SmartImportScreen(this, this.capeRegion)
        );
    }

    private void duplicateLayer() {
        ensureSelectedLayerExists();

        var before = ClientProjectWorkspace.project().cape().layers();
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
                ProjectEdits.duplicateCapeLayer(
                        project,
                        selectedLayerId
                )
        );

        selectedLayerId = result.cape()
                .layers()
                .get(originalIndex + 1)
                .id();
        syncLayerFields();
        updateButtonStates();
    }

    private void deleteLayer() {
        ensureSelectedLayerExists();

        var before = ClientProjectWorkspace.project().cape().layers();
        if (before.size() <= 1) {
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
                ProjectEdits.removeCapeLayer(
                        project,
                        selectedLayerId
                )
        );

        int nextIndex = Math.min(
                oldIndex,
                result.cape().layers().size() - 1
        );
        selectedLayerId = result.cape()
                .layers()
                .get(nextIndex)
                .id();
        syncLayerFields();
        updateButtonStates();
    }

    private void moveLayer(int delta) {
        ensureSelectedLayerExists();

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.moveCapeLayer(
                        project,
                        selectedLayerId,
                        delta
                )
        );
        updateButtonStates();
    }

    private void renameLayer() {
        if (layerNameField == null) {
            return;
        }

        String name = layerNameField.getValue().trim();
        if (name.isEmpty()) {
            syncLayerFields();
            return;
        }

        try {
            ClientProjectWorkspace.apply(project ->
                    ProjectEdits.renameCapeLayer(
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

    private void cycleLayerBlendMode() {
        LoomLayer layer = selectedLayer();
        BlendMode next = layer.blendMode().next();

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeLayerBlendMode(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void toggleLayerEmissive() {
        LoomLayer layer = selectedLayer();

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeLayerEmissive(
                        project,
                        selectedLayerId,
                        !layer.emissive()
                )
        );
        updateButtonStates();
    }

    private void toggleLayerLock() {
        ensureSelectedLayerExists();
        toggleLayerLock(selectedLayerId);
    }

    private void toggleLayerLock(UUID layerId) {
        LoomLayer layer = workspaceState.project().cape().layers().stream()
                .filter(candidate -> candidate.id().equals(layerId))
                .findFirst()
                .orElse(null);

        if (layer == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeLayerLocked(
                        project,
                        layerId,
                        !layer.locked()
                )
        );

        if (layerId.equals(selectedLayerId)) {
            this.selection = null;
        }
        updateButtonStates();
    }

    private void cycleGradientType() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withType(gradient.type().next())
                )
        );
        updateButtonStates();
    }

    private void rotateGradient(double delta) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        double next = normalizeDegrees(
                gradient.transform().rotationDegrees() + delta
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withTransform(
                                gradient.transform().withRotation(next)
                        )
                )
        );
        updateButtonStates();
    }

    private void moveGradient(int horizontal, int vertical) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        GradientLayerData next = GradientAuthoring.translate(
                gradient,
                horizontal,
                vertical
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void scaleGradient(double factor) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData next = GradientAuthoring.scale(
                layer.gradientData(),
                factor
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void toggleGradientMirror(boolean horizontal) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData next = horizontal
                ? GradientAuthoring.toggleMirrorHorizontal(
                        layer.gradientData()
                )
                : GradientAuthoring.toggleMirrorVertical(
                        layer.gradientData()
                );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void resetGradientTransform() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData next = GradientAuthoring.resetTransform(
                layer.gradientData()
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void setGradientEndpoint(boolean start) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        ArrayList<GradientStop> stops =
                new ArrayList<>(gradient.stops());

        int index = start ? 0 : stops.size() - 1;
        GradientStop previous = stops.get(index);
        stops.set(
                index,
                new GradientStop(
                        previous.position(),
                        selectedColor
                )
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withStops(stops)
                )
        );
        updateButtonStates();
    }

    private void selectGradientStop(int delta) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT) {
            return;
        }

        int count = layer.gradientData().stops().size();
        selectedGradientStopIndex = Math.max(
                0,
                Math.min(
                        count - 1,
                        selectedGradientStopIndex + delta
                )
        );
        updateButtonStates();
    }

    private void addGradientStop() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        if (gradient.stops().size()
                >= dev.loomstudios.project.LoomProjectCodec.MAX_GRADIENT_STOPS) {
            return;
        }

        ArrayList<GradientStop> stops =
                new ArrayList<>(gradient.stops());

        int gapIndex = 0;
        double largestGap = -1.0;
        for (int i = 0; i < stops.size() - 1; i++) {
            double gap = stops.get(i + 1).position()
                    - stops.get(i).position();
            if (gap > largestGap) {
                largestGap = gap;
                gapIndex = i;
            }
        }

        GradientStop left = stops.get(gapIndex);
        GradientStop right = stops.get(gapIndex + 1);
        double position =
                (left.position() + right.position()) / 2.0;
        int color = interpolateArgb(
                left.argb(),
                right.argb(),
                0.5
        );

        stops.add(new GradientStop(position, color));
        stops.sort(java.util.Comparator.comparingDouble(
                GradientStop::position
        ));

        selectedGradientStopIndex = 0;
        for (int i = 0; i < stops.size(); i++) {
            if (Math.abs(stops.get(i).position() - position) < 1.0E-9) {
                selectedGradientStopIndex = i;
                break;
            }
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withStops(stops)
                )
        );
        updateButtonStates();
    }

    private void removeGradientStop() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        if (gradient.stops().size() <= 2) {
            return;
        }

        ArrayList<GradientStop> stops =
                new ArrayList<>(gradient.stops());
        int index = clampedGradientStopIndex(gradient);
        stops.remove(index);
        selectedGradientStopIndex = Math.max(
                0,
                Math.min(index, stops.size() - 1)
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withStops(stops)
                )
        );
        updateButtonStates();
    }

    private void moveGradientStop(double delta) {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        ArrayList<GradientStop> stops =
                new ArrayList<>(gradient.stops());
        int index = clampedGradientStopIndex(gradient);
        GradientStop current = stops.get(index);

        double min = index == 0
                ? 0.0
                : stops.get(index - 1).position() + 0.001;
        double max = index + 1 == stops.size()
                ? 1.0
                : stops.get(index + 1).position() - 0.001;

        double next = Math.max(
                min,
                Math.min(max, current.position() + delta)
        );
        stops.set(index, new GradientStop(next, current.argb()));

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withStops(stops)
                )
        );
        updateButtonStates();
    }

    private void setSelectedGradientStopColor() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        ArrayList<GradientStop> stops =
                new ArrayList<>(gradient.stops());
        int index = clampedGradientStopIndex(gradient);
        GradientStop current = stops.get(index);
        stops.set(
                index,
                new GradientStop(current.position(), selectedColor)
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withStops(stops)
                )
        );
        updateButtonStates();
    }

    private int clampedGradientStopIndex(
            GradientLayerData gradient
    ) {
        selectedGradientStopIndex = Math.max(
                0,
                Math.min(
                        gradient.stops().size() - 1,
                        selectedGradientStopIndex
                )
        );
        return selectedGradientStopIndex;
    }

    private void toggleGradientRepeat() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withRepeat(!gradient.repeat())
                )
        );
        updateButtonStates();
    }

    private void toggleGradientDither() {
        LoomLayer layer = selectedLayer();
        if (layer.kind() != LayerKind.GRADIENT || layer.locked()) {
            return;
        }

        GradientLayerData gradient = layer.gradientData();
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeGradientData(
                        project,
                        selectedLayerId,
                        gradient.withDither(!gradient.dither())
                )
        );
        updateButtonStates();
    }

    private NormalizedRect activeFaceRect(LoomProject project) {
        int scale = CanvasResolution.fromCanvas(
                project.cape()
        ).scale();

        return new NormalizedRect(
                capeRegion.atlasX(0, scale)
                        / (double)project.cape().width(),
                capeRegion.atlasY(0, scale)
                        / (double)project.cape().height(),
                capeRegion.width(scale)
                        / (double)project.cape().width(),
                capeRegion.height(scale)
                        / (double)project.cape().height()
        );
    }

    private static int interpolateArgb(
            int first,
            int second,
            double t
    ) {
        int a = interpolateChannel(
                (first >>> 24) & 0xFF,
                (second >>> 24) & 0xFF,
                t
        );
        int r = interpolateChannel(
                (first >>> 16) & 0xFF,
                (second >>> 16) & 0xFF,
                t
        );
        int g = interpolateChannel(
                (first >>> 8) & 0xFF,
                (second >>> 8) & 0xFF,
                t
        );
        int b = interpolateChannel(
                first & 0xFF,
                second & 0xFF,
                t
        );

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int interpolateChannel(
            int first,
            int second,
            double t
    ) {
        return Math.max(
                0,
                Math.min(
                        255,
                        (int)Math.round(first + (second - first) * t)
                )
        );
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        return normalized < 0.0
                ? normalized + 360.0
                : normalized;
    }

    private void syncLayerFields() {
        if (layerNameField == null
                || workspaceState == null
                || workspaceState.project().cape().layers().isEmpty()) {
            return;
        }

        ensureSelectedLayerExists();
        LoomLayer layer = selectedLayer();

        if (!layerNameField.isFocused()) {
            layerNameField.setValue(layer.name());
        }
    }

    private void changeLayerOpacity(float delta) {
        LoomLayer layer = selectedLayer();
        float next = Math.max(
                0.0F,
                Math.min(1.0F, layer.opacity() + delta)
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeLayerOpacity(
                        project,
                        selectedLayerId,
                        next
                )
        );
        updateButtonStates();
    }

    private void editPixel(int x, int y) {
        if (tool != Tool.EYEDROPPER
                && !selectedLayer().editableAsPaint()) {
            return;
        }

        switch (tool) {
            case PENCIL -> ClientProjectWorkspace.apply(project ->
                    paintBrushWithSymmetry(
                            project,
                            x,
                            y,
                            selectedColor
                    )
            );
            case ERASER -> ClientProjectWorkspace.apply(project ->
                    paintBrushWithSymmetry(
                            project,
                            x,
                            y,
                            0x00000000
                    )
            );
            case FILL -> ClientProjectWorkspace.apply(project ->
                    fillWithSymmetry(
                            project,
                            x,
                            y,
                            selectedColor
                    )
            );
            case EYEDROPPER -> sampleVisibleColor(x, y);
            case LINE, RECTANGLE, CIRCLE, SELECT -> {
            }
        }

        updateButtonStates();
    }

    private void commitShape(
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        if (!selectedLayer().editableAsPaint()) {
            return;
        }

        switch (tool) {
            case LINE -> ClientProjectWorkspace.apply(project ->
                    paintLineWithSymmetry(
                            project,
                            startX,
                            startY,
                            endX,
                            endY
                    )
            );
            case RECTANGLE, CIRCLE -> ClientProjectWorkspace.apply(project ->
                    paintRectangleWithSymmetry(
                            project,
                            startX,
                            startY,
                            endX,
                            endY
                    )
            );
            default -> {
            }
        }

        updateButtonStates();
    }

    private dev.loomstudios.project.LoomProject paintBrushWithSymmetry(
            dev.loomstudios.project.LoomProject project,
            int x,
            int y,
            int color
    ) {
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        int width = capeRegion.width(scale);
        int height = capeRegion.height(scale);
        var result = project;

        for (int mask : symmetryMasks()) {
            int px = (mask & 1) != 0 ? width - 1 - x : x;
            int py = (mask & 2) != 0 ? height - 1 - y : y;

            result = ProjectEdits.paintCapeRegionBrush(
                    result,
                    selectedLayerId,
                    capeRegion,
                    px,
                    py,
                    brushSize,
                    color
            );
        }

        return result;
    }

    private dev.loomstudios.project.LoomProject fillWithSymmetry(
            dev.loomstudios.project.LoomProject project,
            int x,
            int y,
            int color
    ) {
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        int width = capeRegion.width(scale);
        int height = capeRegion.height(scale);
        var result = project;

        for (int mask : symmetryMasks()) {
            int px = (mask & 1) != 0 ? width - 1 - x : x;
            int py = (mask & 2) != 0 ? height - 1 - y : y;

            result = ProjectEdits.floodFillCapeRegion(
                    result,
                    selectedLayerId,
                    capeRegion,
                    px,
                    py,
                    color
            );
        }

        return result;
    }

    private dev.loomstudios.project.LoomProject paintLineWithSymmetry(
            dev.loomstudios.project.LoomProject project,
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        int width = capeRegion.width(scale);
        int height = capeRegion.height(scale);
        var result = project;

        for (int mask : symmetryMasks()) {
            int sx = (mask & 1) != 0 ? width - 1 - startX : startX;
            int sy = (mask & 2) != 0 ? height - 1 - startY : startY;
            int ex = (mask & 1) != 0 ? width - 1 - endX : endX;
            int ey = (mask & 2) != 0 ? height - 1 - endY : endY;

            result = ProjectEdits.paintCapeRegionLine(
                    result,
                    selectedLayerId,
                    capeRegion,
                    sx,
                    sy,
                    ex,
                    ey,
                    brushSize,
                    selectedColor
            );
        }

        return result;
    }

    private dev.loomstudios.project.LoomProject paintRectangleWithSymmetry(
            dev.loomstudios.project.LoomProject project,
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        int width = capeRegion.width(scale);
        int height = capeRegion.height(scale);
        var result = project;

        for (int mask : symmetryMasks()) {
            int sx = (mask & 1) != 0 ? width - 1 - startX : startX;
            int sy = (mask & 2) != 0 ? height - 1 - startY : startY;
            int ex = (mask & 1) != 0 ? width - 1 - endX : endX;
            int ey = (mask & 2) != 0 ? height - 1 - endY : endY;

            result = tool == Tool.CIRCLE
                    ? ProjectEdits.paintCapeRegionEllipse(result, selectedLayerId, capeRegion, sx, sy, ex, ey, brushSize, selectedColor, rectangleFilled)
                    : ProjectEdits.paintCapeRegionRectangle(
                    result,
                    selectedLayerId,
                    capeRegion,
                    sx,
                    sy,
                    ex,
                    ey,
                    brushSize,
                    selectedColor,
                    rectangleFilled
            );
        }

        return result;
    }

    private int[] symmetryMasks() {
        return switch (symmetryMode) {
            case NONE -> new int[]{0};
            case HORIZONTAL -> new int[]{0, 1};
            case VERTICAL -> new int[]{0, 2};
            case BOTH -> new int[]{0, 1, 2, 3};
        };
    }

    private void setSelection(PixelSelection selection) {
        this.selection = selection;
        updateButtonStates();
    }

    private void copyPixelSelection(){if(selection!=null){dev.loomstudios.client.project.PixelClipboard.patch=dev.loomstudios.project.SurfaceEdits.cape(ClientProjectWorkspace.project(),selectedLayerId,capeRegion).crop(selection);notifyPlayer("Pixels copied");updateButtonStates();}}
    private void pastePixelSelection(){var clipboard=dev.loomstudios.client.project.PixelClipboard.patch;if(clipboard==null)return;int x=selection==null?0:selection.minX(),y=selection==null?0:selection.minY();try{ClientProjectWorkspace.apply(p->dev.loomstudios.project.SurfaceEdits.cape(p,selectedLayerId,capeRegion,patch->patch.paste(clipboard,x,y)));selection=new PixelSelection(x,y,x+clipboard.width()-1,y+clipboard.height()-1);updateButtonStates();}catch(IllegalArgumentException e){notifyPlayer(e.getMessage());}}
    private void rotatePixelSelection(){if(selection==null)return;PixelSelection sel=selection;try{ClientProjectWorkspace.apply(p->dev.loomstudios.project.SurfaceEdits.cape(p,selectedLayerId,capeRegion,patch->patch.clear(sel).paste(patch.crop(sel).rotate(),sel.minX(),sel.minY())));selection=new PixelSelection(sel.minX(),sel.minY(),sel.minX()+sel.height()-1,sel.minY()+sel.width()-1);updateButtonStates();}catch(IllegalArgumentException e){notifyPlayer(e.getMessage());}}
    private void clearSelection() {
        this.selection = null;
        updateButtonStates();
    }

    private void nudgeSelection(int deltaX, int deltaY) {
        if (selection == null) {
            return;
        }

        int scale = CanvasResolution.fromCanvas(
                ClientProjectWorkspace.project().cape()
        ).scale();
        int regionWidth = capeRegion.width(scale);
        int regionHeight = capeRegion.height(scale);

        int targetMinX = Math.max(
                0,
                Math.min(
                        regionWidth - selection.width(),
                        selection.minX() + deltaX
                )
        );
        int targetMinY = Math.max(
                0,
                Math.min(
                        regionHeight - selection.height(),
                        selection.minY() + deltaY
                )
        );

        int actualDeltaX = targetMinX - selection.minX();
        int actualDeltaY = targetMinY - selection.minY();

        if (actualDeltaX == 0 && actualDeltaY == 0) {
            return;
        }

        PixelSelection currentSelection = selection;
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.moveCapeRegionSelection(
                        project,
                        selectedLayerId,
                        capeRegion,
                        currentSelection,
                        actualDeltaX,
                        actualDeltaY
                )
        );

        this.selection = new PixelSelection(
                targetMinX,
                targetMinY,
                targetMinX + currentSelection.width() - 1,
                targetMinY + currentSelection.height() - 1
        );
        updateButtonStates();
    }

    private void flipSelection(boolean horizontal, boolean vertical) {
        if (selection == null) {
            return;
        }

        PixelSelection currentSelection = selection;
        ClientProjectWorkspace.apply(project ->
                ProjectEdits.flipCapeRegionSelection(
                        project,
                        selectedLayerId,
                        capeRegion,
                        currentSelection,
                        horizontal,
                        vertical
                )
        );
        updateButtonStates();
    }

    private void sampleVisibleColor(int x, int y) {
        var project = ClientProjectWorkspace.project();
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        int width = this.capeRegion.width(scale);
        int height = this.capeRegion.height(scale);

        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }

        int[] pixels = LoomTextureCompiler.compileCapeRegion(
                project.cape(),
                this.capeRegion,
                0,
                false,
                false
        );

        int color = pixels[y * width + x];
        if (((color >>> 24) & 0xFF) == 0) {
            return;
        }

        setSelectedColor(color);
    }

    private void undo() {
        ClientProjectWorkspace.undo();
        clearSelection();
        syncLayerFields();
        updateButtonStates();
    }

    private void redo() {
        ClientProjectWorkspace.redo();
        clearSelection();
        syncLayerFields();
        updateButtonStates();
    }

    private void save() {
        try {
            ClientProjectWorkspace.save();
            notifyPlayer("Loom project saved");
        } catch (IOException e) {
            LoomStudios.LOGGER.error("Failed to save Loom project", e);
            notifyPlayer("Failed to save Loom project");
        }
        updateButtonStates();
    }

    private void saveAndEquip() {
        try {
            ClientProjectWorkspace.saveAndEquip();
            notifyPlayer("Loom project saved and equipped");
        } catch (IOException | IllegalStateException e) {
            LoomStudios.LOGGER.error(
                    "Failed to save/equip Loom project",
                    e
            );
            notifyPlayer("Failed to save/equip Loom project");
        }
        updateButtonStates();
    }

    private String feedback="";
    private long feedbackUntil;
    private dev.loomstudios.project.LoomProject feedbackProject;
    private void notifyPlayer(String text) {
        feedback=text;feedbackUntil=System.currentTimeMillis()+5000;feedbackProject=ClientProjectWorkspace.project();
        if (this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(
                    Component.literal(text),
                    true
            );
        }
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
            dev.loomstudios.client.ui.premium.PremiumText.drawCenteredString(graphics,font, title, width / 2, height / 2 - 24, LoomUiTheme.TEXT);
            dev.loomstudios.client.ui.premium.PremiumText.drawCenteredString(graphics,font, Component.literal("Increase window size or reduce GUI scale"), width / 2, height / 2, LoomUiTheme.TEXT_MUTED);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        LoomScreenChrome.renderBackdrop(
                graphics,
                width,
                height
        );
        LoomScreenChrome.renderEditorHeader(
                graphics,
                width,
                compactMode
                        ? "Cape Editor"
                        : "Cape Editor • "
                                + workspaceState.project().name()
                                + (workspaceState.dirty() ? " *" : ""),
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
                canvasLeft,
                canvasTop,
                canvasRight,
                canvasBottom
        );
        LoomScreenChrome.panel(
                graphics,
                canvasLeft,
                contextTop,
                canvasRight,
                contentBottom
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

        if (tool == Tool.FILL || tool == Tool.EYEDROPPER) {
            dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,font, Component.literal(tool == Tool.FILL ? "Click a pixel to fill its area" : "Click a pixel to pick its color"), canvasLeft + 8, contextTop + 9, LoomUiTheme.TEXT_MUTED, false);
        }
        String status = ClientProjectWorkspace.isCurrentProjectEquipped()
                ? "Saved / Equipped"
                : workspaceState.dirty()
                        ? "Unsaved edits"
                        : "Saved, not equipped";
        if(ClientProjectWorkspace.session().editError()!=null)status=ClientProjectWorkspace.session().editError();
        status=System.currentTimeMillis()<feedbackUntil&&feedbackProject==ClientProjectWorkspace.project()?feedback:dev.loomstudios.client.ui.LoomToolGuidance.status(tool.name(),selectedLayer(),false,status);

        LoomScreenChrome.footer(
                graphics,
                width,
                height,
                status,
                capeRegion.displayName()
                        + " • "
                        + resolutionLabel()
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
        updateButtonStates();
        if(!hasChoices()) {
            int divider=rightPanelLeft-3;
            boolean hover=dev.loomstudios.client.ui.LoomInspectorResize.onDivider(
                mouseX,mouseY,divider,contentTop,contentBottom);
            int pendingRight=inspectorDividerDragging
                ? width-8-proposedInspectorWidth : rightPanelLeft;
            dev.loomstudios.client.ui.LoomInspectorResize.render(
                graphics,divider,contentTop,contentBottom,hover||inspectorDividerDragging,
                inspectorDividerDragging,pendingRight);
            if(inspectorDividerDragging)
                dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,font,
                    Component.literal(proposedInspectorWidth+" GUI px"),
                    Math.min(width-115,Math.max(10,pendingRight+9)),
                    (contentTop+contentBottom)/2-26,LoomUiTheme.ACCENT,false);
        }
    }

    private void closePaletteWindow() {
        paletteWindowVisible = false;
        if (paletteWindow != null) paletteWindow.visible = false;
        if (getFocused() == paletteWindow) setFocused(null);
        updateButtonStates();
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if(!workspaceTooSmall && !hasChoices()
            && dev.loomstudios.client.ui.LoomInspectorResize.onDivider(
                event.x(),event.y(),rightPanelLeft-3,contentTop,contentBottom)) {
            if(event.button()==1) {
                dev.loomstudios.client.ui.LoomInspectorResize.reset(width,height);
                rebuildWidgets();
                return true;
            }
            if(event.button()==0) {
                inspectorDividerDragging=true;
                proposedInspectorWidth=workspaceLayout.preview().width();
                return true;
            }
        }
        if (paletteWindowVisible && paletteWindow != null && paletteWindow.visible
                && paletteWindow.isMouseOver(event.x(),event.y())) {
            boolean handled=paletteWindow.mouseClicked(event,doubleClick);
            if(handled && paletteWindow.visible)setFocused(paletteWindow);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if(inspectorDividerDragging && event.button()==0) {
            proposedInspectorWidth=dev.loomstudios.client.ui.LoomInspectorResize
                .requestedAtX(width,height,event.x());
            return true;
        }
        if (paletteWindowVisible
                && paletteWindow != null
                && paletteWindow.visible
                && paletteWindow.mouseDragged(event, dx, dy)) {
            return true;
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if(inspectorDividerDragging && event.button()==0) {
            inspectorDividerDragging=false;
            dev.loomstudios.client.ui.LoomInspectorResize.persist(
                width,height,proposedInspectorWidth);
            rebuildWidgets();
            return true;
        }
        if (paletteWindowVisible
                && paletteWindow != null
                && paletteWindow.visible
                && paletteWindow.mouseReleased(event)) {
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (paletteWindowVisible
                && paletteWindow != null
                && paletteWindow.visible
                && paletteWindow.isMouseOver(mouseX, mouseY)
                && paletteWindow.mouseScrolled(
                        mouseX,
                        mouseY,
                        scrollX,
                        scrollY
                )) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256 && paletteWindowVisible) { closePaletteWindow(); return true; }
        if (workspaceTooSmall) return super.keyPressed(event);
        if (isEditingText()) {
            return super.keyPressed(event);
        }

        if (event.hasControlDownWithQuirk()) {
            if(selectedLayer()!=null&&selectedLayer().editableAsPaint()){
                if(event.key()==67){copyPixelSelection();return true;}if(event.key()==86){pastePixelSelection();return true;}if(event.key()==82){rotatePixelSelection();return true;}
            }
            if (event.key() == 90) {
                undo();
                return true;
            }
            if (event.key() == 89) {
                redo();
                return true;
            }
            if (event.key() == 83) {
                if (event.hasShiftDown()) {
                    saveAndEquip();
                } else {
                    save();
                }
                return true;
            }
        }

        if (!event.hasControlDown()
                && !event.hasAltDown()
                && !event.hasShiftDown()) {
            if (tool == Tool.SELECT && selection != null) {
                switch (event.key()) {
                    case 263 -> {
                        nudgeSelection(-1, 0);
                        return true;
                    }
                    case 262 -> {
                        nudgeSelection(1, 0);
                        return true;
                    }
                    case 265 -> {
                        nudgeSelection(0, -1);
                        return true;
                    }
                    case 264 -> {
                        nudgeSelection(0, 1);
                        return true;
                    }
                    default -> {
                    }
                }
            }

            switch (event.key()) {
                case 66,80 -> {
                    setTool(Tool.PENCIL);
                    return true;
                }
                case 69 -> {
                    setTool(Tool.ERASER);
                    return true;
                }
                case 71 -> {
                    setTool(Tool.FILL);
                    return true;
                }
                case 73 -> {
                    setTool(Tool.EYEDROPPER);
                    return true;
                }
                case 83,86 -> {setTool(Tool.SELECT);return true;}
                case 76 -> {
                    setTool(Tool.LINE);
                    return true;
                }
                case 67 -> { setTool(Tool.CIRCLE); return true; }
                case 82 -> {
                    setTool(Tool.RECTANGLE);
                    return true;
                }
                case 91 -> {
                    changeBrushSize(-1);
                    return true;
                }
                case 93 -> {
                    changeBrushSize(1);
                    return true;
                }
                case 48 -> {
                    if (canvasWidget != null) {
                        canvasWidget.resetZoom();
                    }
                    return true;
                }
                default -> {
                }
            }
        }

        return super.keyPressed(event);
    }

    @Override public void onClose(){if(parent instanceof ElytraEditorScreen)minecraft.setScreen(parent);else dev.loomstudios.client.project.WorkspaceNavigation.request(this,()->minecraft.setScreen(parent));}

    private boolean isEditingText() {
        if (paletteWindowVisible
                && paletteWindow != null
                && paletteWindow.isEditingName()) {
            return true;
        }

        return (layerNameField != null && layerNameField.isFocused())
                || (hexColorField != null && hexColorField.isFocused())
                || (redColorField != null && redColorField.isFocused())
                || (greenColorField != null && greenColorField.isFocused())
                || (blueColorField != null && blueColorField.isFocused())
                || (alphaColorField != null && alphaColorField.isFocused());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private enum Tool {
        PENCIL,
        ERASER,
        FILL,
        EYEDROPPER,
        SELECT,
        LINE,
        RECTANGLE,
        CIRCLE
    }

    private enum InspectorTab {
        LAYERS,
        COLOR,
        PROPERTIES
    }

    private enum SymmetryMode {
        NONE("Off"),
        HORIZONTAL("Horizontal"),
        VERTICAL("Vertical"),
        BOTH("Both");

        private final String label;

        SymmetryMode(String label) {
            this.label = label;
        }

        private SymmetryMode next() {
            SymmetryMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

  private java.util.BitSet creativeSelection() {
    if (selection == null) return null;
    int w = capeRegion.width(ClientProjectWorkspace.project().cape().width() / 64);
    var bits = new java.util.BitSet();
    for (int y = selection.minY(); y <= selection.maxY(); y++)
      for (int x = selection.minX(); x <= selection.maxX(); x++) bits.set(y * w + x);
    return bits;
}
}
