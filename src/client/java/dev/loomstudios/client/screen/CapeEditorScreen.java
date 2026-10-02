package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.WorkspaceState;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomCapeFaceWidget;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomLayerListWidget;
import dev.loomstudios.client.ui.LoomPaletteButton;
import dev.loomstudios.client.ui.LoomPaletteWindow;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomScreenChrome;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.BlendMode;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.GradientAuthoring;
import dev.loomstudios.project.GradientLayerData;
import dev.loomstudios.project.GradientStop;
import dev.loomstudios.project.NormalizedRect;
import dev.loomstudios.project.PixelSelection;
import dev.loomstudios.project.ProjectEdits;
import dev.loomstudios.project.ProjectResizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public final class CapeEditorScreen extends Screen {
    private final Screen parent;
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
        this.compactMode = LoomUiTheme.compact(width, height);

        int margin = compactMode ? 4 : 8;
        int gap = compactMode ? 4 : 6;
        int headerHeight = LoomScreenChrome.headerHeight(compactMode);
        int navHeight = LoomScreenChrome.navHeight(compactMode);
        int footerHeight = 18;

        this.contentTop = headerHeight + navHeight + 4;
        this.contentBottom = height - footerHeight - 4;

        int toolRailWidth = compactMode ? 30 : 38;
        int rightPanelWidth = compactMode
                ? Math.max(166, Math.min(182, width / 3))
                : Math.max(205, Math.min(232, width / 4));

        this.toolRailLeft = margin;
        this.toolRailRight = toolRailLeft + toolRailWidth;
        this.rightPanelRight = width - margin;
        this.rightPanelLeft = rightPanelRight - rightPanelWidth;

        this.canvasLeft = toolRailRight + gap;
        this.canvasRight = rightPanelLeft - gap;

        int canvasToolbarHeight = compactMode ? 19 : 22;
        int contextHeight = compactMode ? 43 : 50;

        this.canvasTop = contentTop + canvasToolbarHeight + 3;
        this.contextTop = contentBottom - contextHeight;
        this.canvasBottom = contextTop - 4;

        int previewHeight = compactMode
                ? 72
                : Math.max(
                        100,
                        Math.min(
                                130,
                                (contentBottom - contentTop) / 3
                        )
                );
        this.previewBottom = contentTop + previewHeight;
        int inspectorTabHeight = compactMode ? 18 : 20;
        this.inspectorTop = previewBottom + inspectorTabHeight + 5;
        this.inspectorBottom = contentBottom;

        this.toolPanelX = rightPanelLeft;
        this.toolPanelY = inspectorTop;
        this.toolPanelWidth = rightPanelWidth;
        this.toolPanelHeight = inspectorBottom - inspectorTop;

        buildTopNavigation(
                headerHeight,
                navHeight,
                margin
        );
        buildToolRail();
        buildCanvasToolbar(canvasToolbarHeight);
        buildCanvas();
        buildContextBar();
        buildRightPanel(previewHeight, inspectorTabHeight);

        restoreOrCreatePaletteWindow();
        syncColorFields();
        syncLayerFields();
        updateButtonStates();
        updateInspectorVisibility();
    }

    private void buildTopNavigation(
            int headerHeight,
            int navHeight,
            int margin
    ) {
        int y = headerHeight;
        int buttonHeight = navHeight - 2;
        int compactWidth = 26;
        int normalWidth = 72;
        int x = margin;

        addRenderableWidget(navButton(
                x,
                y,
                compactMode ? compactWidth : normalWidth,
                buttonHeight,
                "Home",
                LoomButton.Icon.HOME,
                false,
                () -> minecraft.setScreen(parent)
        ));
        x += (compactMode ? compactWidth : normalWidth) + 3;

        LoomButton cape = navButton(
                x,
                y,
                compactMode ? compactWidth : normalWidth,
                buttonHeight,
                "Cape",
                LoomButton.Icon.CAPE,
                true,
                () -> { }
        );
        cape.active = false;
        addRenderableWidget(cape);
        x += (compactMode ? compactWidth : normalWidth) + 3;

        addRenderableWidget(navButton(
                x,
                y,
                compactMode ? compactWidth : normalWidth,
                buttonHeight,
                "Elytra",
                LoomButton.Icon.ELYTRA,
                false,
                () -> minecraft.setScreen(
                        new ElytraEditorScreen(this)
                )
        ));
        x += (compactMode ? compactWidth : normalWidth) + 3;

        addRenderableWidget(navButton(
                x,
                y,
                compactMode ? compactWidth : normalWidth,
                buttonHeight,
                "Import",
                LoomButton.Icon.IMAGE,
                false,
                this::openSmartImport
        ));
        x += (compactMode ? compactWidth : normalWidth) + 3;

        addRenderableWidget(navButton(
                x,
                y,
                compactMode ? compactWidth : normalWidth + 10,
                buttonHeight,
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

        int actionWidth = compactMode ? 26 : 62;
        int right = width - margin;

        addRenderableWidget(navButton(
                right - actionWidth,
                y,
                actionWidth,
                buttonHeight,
                compactMode ? "Equip" : "Save + Equip",
                LoomButton.Icon.EQUIP,
                false,
                this::saveAndEquip
        ));
        right -= actionWidth + 3;

        addRenderableWidget(navButton(
                right - actionWidth,
                y,
                actionWidth,
                buttonHeight,
                "Save",
                LoomButton.Icon.SAVE,
                false,
                this::save
        ));
        right -= actionWidth + 3;

        int historyWidth = compactMode ? 24 : 42;
        redoButton = navButton(
                right - historyWidth,
                y,
                historyWidth,
                buttonHeight,
                "Redo",
                LoomButton.Icon.REDO,
                false,
                this::redo
        );
        addRenderableWidget(redoButton);
        right -= historyWidth + 3;

        undoButton = navButton(
                right - historyWidth,
                y,
                historyWidth,
                buttonHeight,
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
        int buttonWidth = toolRailRight - toolRailLeft - 4;
        int buttonHeight = compactMode ? 26 : 30;
        int gap = compactMode ? 3 : 4;
        int y = contentTop + 22;

        pencilButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Pencil", LoomButton.Icon.PENCIL,
                () -> setTool(Tool.PENCIL)
        );
        pencilButton.setIconOnly(true);
        y += buttonHeight + gap;

        eraserButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Eraser", LoomButton.Icon.ERASER,
                () -> setTool(Tool.ERASER)
        );
        eraserButton.setIconOnly(true);
        y += buttonHeight + gap;

        fillButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Fill", LoomButton.Icon.FILL,
                () -> setTool(Tool.FILL)
        );
        fillButton.setIconOnly(true);
        y += buttonHeight + gap;

        eyedropperButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Eyedropper", LoomButton.Icon.EYEDROPPER,
                () -> setTool(Tool.EYEDROPPER)
        );
        eyedropperButton.setIconOnly(true);
        y += buttonHeight + gap;

        selectButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Select", LoomButton.Icon.SELECT,
                () -> setTool(Tool.SELECT)
        );
        selectButton.setIconOnly(true);
        y += buttonHeight + gap;

        lineButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Line", LoomButton.Icon.LINE,
                () -> setTool(Tool.LINE)
        );
        lineButton.setIconOnly(true);
        y += buttonHeight + gap;

        rectangleButton = iconButton(
                x, y, buttonWidth, buttonHeight,
                "Rectangle", LoomButton.Icon.RECTANGLE,
                () -> setTool(Tool.RECTANGLE)
        );
        rectangleButton.setIconOnly(true);
    }

    private void buildCanvasToolbar(int toolbarHeight) {
        int y = contentTop;
        int h = toolbarHeight;
        int x = canvasLeft;
        int gap = 3;

        int faceWidth = compactMode ? 92 : 128;
        faceButton = iconButton(
                x,
                y,
                faceWidth,
                h,
                faceLabel(),
                LoomButton.Icon.CAPE,
                this::cycleFace
        );
        faceButton.setIconOnly(false);
        x += faceWidth + gap;

        int small = compactMode ? 22 : 28;
        resolutionDownButton = iconButton(
                x,
                y,
                small,
                h,
                "Resolution -",
                LoomButton.Icon.DOWN,
                () -> changeResolution(-1)
        );
        resolutionDownButton.setIconOnly(true);
        x += small + gap;

        int resolutionWidth = compactMode ? 70 : 92;
        resolutionLabelButton = new LoomButton(
                x,
                y,
                resolutionWidth,
                h,
                Component.literal(resolutionLabel()),
                () -> { }
        );
        resolutionLabelButton.active = false;
        addRenderableWidget(resolutionLabelButton);
        x += resolutionWidth + gap;

        resolutionUpButton = iconButton(
                x,
                y,
                small,
                h,
                "Resolution +",
                LoomButton.Icon.UP,
                () -> changeResolution(1)
        );
        resolutionUpButton.setIconOnly(true);

        int right = canvasRight;
        zoomInButton = iconButton(
                right - small,
                y,
                small,
                h,
                "Zoom +",
                LoomButton.Icon.ZOOM_IN,
                () -> canvasWidget.zoomIn()
        );
        zoomInButton.setIconOnly(true);
        right -= small + gap;

        int zoomWidth = compactMode ? 42 : 52;
        zoomLabelButton = new LoomButton(
                right - zoomWidth,
                y,
                zoomWidth,
                h,
                Component.literal("100%"),
                () -> canvasWidget.resetZoom()
        );
        addRenderableWidget(zoomLabelButton);
        right -= zoomWidth + gap;

        zoomOutButton = iconButton(
                right - small,
                y,
                small,
                h,
                "Zoom -",
                LoomButton.Icon.ZOOM_OUT,
                () -> canvasWidget.zoomOut()
        );
        zoomOutButton.setIconOnly(true);
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

    private void buildContextBar() {
        int x = canvasLeft + 5;
        int y = contextTop + 4;
        int h = compactMode ? 17 : 20;
        int gap = 3;
        int small = compactMode ? 22 : 28;

        brushDownButton = iconButton(
                x, y, small, h,
                "Brush -", LoomButton.Icon.DOWN,
                () -> changeBrushSize(-1)
        );
        brushDownButton.setIconOnly(true);
        x += small + gap;

        int brushLabelWidth = compactMode ? 54 : 72;
        brushLabelButton = new LoomButton(
                x,
                y,
                brushLabelWidth,
                h,
                Component.literal(brushLabel()),
                () -> { }
        );
        brushLabelButton.active = false;
        addRenderableWidget(brushLabelButton);
        x += brushLabelWidth + gap;

        brushUpButton = iconButton(
                x, y, small, h,
                "Brush +", LoomButton.Icon.UP,
                () -> changeBrushSize(1)
        );
        brushUpButton.setIconOnly(true);
        x += small + gap;

        symmetryButton = iconButton(
                x,
                y,
                compactMode ? 74 : 112,
                h,
                symmetryLabel(),
                LoomButton.Icon.FLIP_H,
                this::cycleSymmetry
        );
        symmetryButton.setIconOnly(false);

        rectangleModeButton = iconButton(
                x + (compactMode ? 78 : 116),
                y,
                compactMode ? 72 : 112,
                h,
                "Rectangle: Outline",
                LoomButton.Icon.RECTANGLE,
                this::toggleRectangleMode
        );
        rectangleModeButton.setIconOnly(false);

        int secondY = y + h + 3;
        int control = compactMode ? 24 : 31;

        selectionClearButton = iconButton(
                canvasLeft + 5,
                secondY,
                compactMode ? 58 : 82,
                h,
                "Clear",
                LoomButton.Icon.DELETE,
                this::clearSelection
        );
        selectionClearButton.setIconOnly(false);

        int sx = canvasLeft
                + 5
                + selectionClearButton.getWidth()
                + 3;

        selectionLeftButton = iconButton(
                sx, secondY, control, h,
                "Left", LoomButton.Icon.MOVE,
                () -> nudgeSelection(-1, 0)
        );
        selectionLeftButton.setIconOnly(true);
        sx += control + 3;

        selectionRightButton = iconButton(
                sx, secondY, control, h,
                "Right", LoomButton.Icon.MOVE,
                () -> nudgeSelection(1, 0)
        );
        selectionRightButton.setIconOnly(true);
        sx += control + 3;

        selectionUpButton = iconButton(
                sx, secondY, control, h,
                "Up", LoomButton.Icon.UP,
                () -> nudgeSelection(0, -1)
        );
        selectionUpButton.setIconOnly(true);
        sx += control + 3;

        selectionDownButton = iconButton(
                sx, secondY, control, h,
                "Down", LoomButton.Icon.DOWN,
                () -> nudgeSelection(0, 1)
        );
        selectionDownButton.setIconOnly(true);
        sx += control + 3;

        selectionFlipHorizontalButton = iconButton(
                sx, secondY, control, h,
                "Flip H", LoomButton.Icon.FLIP_H,
                () -> flipSelection(true, false)
        );
        selectionFlipHorizontalButton.setIconOnly(true);
        sx += control + 3;

        selectionFlipVerticalButton = iconButton(
                sx, secondY, control, h,
                "Flip V", LoomButton.Icon.FLIP_V,
                () -> flipSelection(false, true)
        );
        selectionFlipVerticalButton.setIconOnly(true);
    }

    private void buildRightPanel(
            int previewHeight,
            int inspectorTabHeight
    ) {
        this.previewWidget = new LoomPlayerPreviewWidget(
                rightPanelLeft + 2,
                contentTop + 20,
                rightPanelRight - rightPanelLeft - 4,
                Math.max(42, previewHeight - 22),
                () -> workspaceState.project(),
                LoomPlayerPreviewWidget.Mode.CAPE
        );
        addRenderableWidget(previewWidget);

        int tabsY = previewBottom + 3;
        int tabGap = 3;
        int tabWidth = Math.max(
                42,
                (rightPanelRight - rightPanelLeft - tabGap * 2) / 3
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
                rightPanelRight
                        - (rightPanelLeft + (tabWidth + tabGap) * 2),
                inspectorTabHeight,
                "Props",
                LoomButton.Icon.SETTINGS,
                false,
                () -> setInspectorTab(InspectorTab.PROPERTIES)
        );
        addRenderableWidget(inspectorPropertiesButton);

        buildLayerInspector();
        buildColorInspector();
        buildPropertyInspector();
    }

    private void buildLayerInspector() {
        int left = rightPanelLeft + 4;
        int panelWidth = rightPanelRight - rightPanelLeft - 8;
        int y = inspectorTop + 3;
        int h = compactMode ? 17 : 20;
        int gap = compactMode ? 2 : 3;

        int listHeight = compactMode ? 62 : 92;
        this.layerListWidget = new LoomLayerListWidget(
                left,
                y,
                panelWidth,
                listHeight,
                () -> this.workspaceState.project().cape(),
                () -> this.selectedLayerId,
                this::selectLayer,
                this::toggleLayerVisibility,
                this::toggleLayerLock
        );
        addRenderableWidget(layerListWidget);
        y += listHeight + gap;

        int third = Math.max(
                32,
                (panelWidth - gap * 2) / 3
        );

        layerAddButton = iconButton(
                left,
                y,
                third,
                h,
                "+ Paint",
                LoomButton.Icon.PLUS,
                this::addLayer
        );
        layerAddButton.setIconOnly(compactMode);

        layerGradientAddButton = iconButton(
                left + third + gap,
                y,
                third,
                h,
                "Gradient",
                LoomButton.Icon.GRADIENT,
                this::addGradientLayer
        );
        layerGradientAddButton.setIconOnly(compactMode);

        layerImportButton = iconButton(
                left + (third + gap) * 2,
                y,
                panelWidth - third * 2 - gap * 2,
                h,
                "Import",
                LoomButton.Icon.IMAGE,
                this::openSmartImport
        );
        layerImportButton.setIconOnly(compactMode);
        y += h + gap;

        int quarter = Math.max(
                25,
                (panelWidth - gap * 3) / 4
        );

        layerDuplicateButton = iconButton(
                left, y, quarter, h,
                "Copy", LoomButton.Icon.COPY,
                this::duplicateLayer
        );
        layerDuplicateButton.setIconOnly(true);

        layerDeleteButton = iconButton(
                left + quarter + gap,
                y,
                quarter,
                h,
                "Delete",
                LoomButton.Icon.DELETE,
                this::deleteLayer
        );
        layerDeleteButton
                .setIconOnly(true)
                .setDanger(true);

        layerUpButton = iconButton(
                left + (quarter + gap) * 2,
                y,
                quarter,
                h,
                "Up",
                LoomButton.Icon.UP,
                () -> moveLayer(1)
        );
        layerUpButton.setIconOnly(true);

        layerDownButton = iconButton(
                left + (quarter + gap) * 3,
                y,
                panelWidth - quarter * 3 - gap * 3,
                h,
                "Down",
                LoomButton.Icon.DOWN,
                () -> moveLayer(-1)
        );
        layerDownButton.setIconOnly(true);
        y += h + gap;

        int renameWidth = compactMode ? 44 : 52;
        this.layerNameField = new EditBox(
                this.font,
                left,
                y,
                Math.max(
                        48,
                        panelWidth - renameWidth - gap
                ),
                h,
                Component.literal("Layer name")
        );
        this.layerNameField.setMaxLength(
                dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS
        );
        this.layerNameField.setHint(
                Component.literal("Layer name")
        );
        addRenderableWidget(layerNameField);

        layerRenameButton = iconButton(
                left + panelWidth - renameWidth,
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
                left,
                y,
                small,
                h,
                "Opacity -",
                LoomButton.Icon.DOWN,
                () -> changeLayerOpacity(-0.1F)
        );
        layerOpacityDownButton.setIconOnly(true);

        int opacityLabelWidth =
                panelWidth - small * 2 - gap * 2;
        layerOpacityLabelButton = new LoomButton(
                left + small + gap,
                y,
                opacityLabelWidth,
                h,
                Component.literal("Opacity 100%"),
                () -> { }
        );
        layerOpacityLabelButton.active = false;
        addRenderableWidget(layerOpacityLabelButton);

        layerOpacityUpButton = iconButton(
                left
                        + small
                        + gap
                        + opacityLabelWidth
                        + gap,
                y,
                small,
                h,
                "Opacity +",
                LoomButton.Icon.UP,
                () -> changeLayerOpacity(0.1F)
        );
        layerOpacityUpButton.setIconOnly(true);
        y += h + gap;

        layerBlendButton = iconButton(
                left,
                y,
                panelWidth,
                h,
                "Blend: Normal",
                LoomButton.Icon.LAYERS,
                this::cycleLayerBlendMode
        );
        layerBlendButton.setIconOnly(false);
        y += h + gap;

        int half = (panelWidth - gap) / 2;
        layerEmissiveButton = iconButton(
                left,
                y,
                half,
                h,
                "Emissive",
                LoomButton.Icon.EMISSIVE,
                this::toggleLayerEmissive
        );
        layerEmissiveButton.setIconOnly(compactMode);

        layerLockButton = iconButton(
                left + half + gap,
                y,
                panelWidth - half - gap,
                h,
                "Lock",
                LoomButton.Icon.LOCK,
                this::toggleLayerLock
        );
        layerLockButton.setIconOnly(compactMode);
    }

    private void buildColorInspector() {
        int left = rightPanelLeft + 4;
        int panelWidth = rightPanelRight - rightPanelLeft - 8;
        int y = inspectorTop + 3;
        int gap = 3;

        int pickerHeight = compactMode ? 118 : 150;
        this.colorPicker = new LoomColorPickerWidget(
                left,
                y,
                panelWidth,
                pickerHeight,
                this.selectedColor,
                this::setSelectedColor
        );
        addRenderableWidget(colorPicker);
        y += pickerHeight + gap;

        this.hexColorField = createHexColorField(panelWidth);
        this.hexColorField.setX(left);
        this.hexColorField.setY(y);
        addRenderableWidget(hexColorField);
        y += 18 + gap;

        int channelGap = 3;
        int channelWidth = Math.max(
                26,
                (panelWidth - channelGap * 3) / 4
        );

        this.redColorField = createChannelField(
                channelWidth,
                "R",
                16
        );
        this.greenColorField = createChannelField(
                channelWidth,
                "G",
                8
        );
        this.blueColorField = createChannelField(
                channelWidth,
                "B",
                0
        );
        this.alphaColorField = createAlphaField(
                panelWidth
                        - channelWidth * 3
                        - channelGap * 3
        );

        EditBox[] fields = {
                redColorField,
                greenColorField,
                blueColorField,
                alphaColorField
        };
        int fx = left;
        for (EditBox field : fields) {
            field.setX(fx);
            field.setY(y);
            addRenderableWidget(field);
            fx += field.getWidth() + channelGap;
        }
        y += 18 + gap;

        this.paletteButton = new LoomPaletteButton(
                left,
                y,
                panelWidth,
                compactMode ? 18 : 20,
                this::togglePaletteWindow
        );
        addRenderableWidget(paletteButton);
    }

    private void buildPropertyInspector() {
        int left = rightPanelLeft + 4;
        int panelWidth = rightPanelRight - rightPanelLeft - 8;
        int y = inspectorTop + 3;
        int h = compactMode ? 16 : 18;
        int gap = compactMode ? 2 : 3;
        int small = compactMode ? 22 : 28;
        int centerWidth = panelWidth - small * 2 - gap * 2;

        gradientTypeButton = iconButton(
                left,
                y,
                panelWidth,
                h,
                "Gradient: Linear",
                LoomButton.Icon.GRADIENT,
                this::cycleGradientType
        );
        gradientTypeButton.setIconOnly(false);
        y += h + gap;

        gradientAngleDownButton = iconButton(
                left,
                y,
                small,
                h,
                "Angle -",
                LoomButton.Icon.DOWN,
                () -> rotateGradient(-15.0)
        );
        gradientAngleDownButton.setIconOnly(true);

        gradientAngleButton = new LoomButton(
                left + small + gap,
                y,
                centerWidth,
                h,
                Component.literal("0°"),
                () -> { }
        );
        gradientAngleButton.active = false;
        addRenderableWidget(gradientAngleButton);

        gradientAngleUpButton = iconButton(
                left + small + gap + centerWidth + gap,
                y,
                small,
                h,
                "Angle +",
                LoomButton.Icon.UP,
                () -> rotateGradient(15.0)
        );
        gradientAngleUpButton.setIconOnly(true);
        y += h + gap;

        int quarter = Math.max(
                22,
                (panelWidth - gap * 3) / 4
        );
        gradientMoveLeftButton = iconButton(
                left, y, quarter, h,
                "Left", LoomButton.Icon.MOVE,
                () -> moveGradient(-1, 0)
        );
        gradientMoveLeftButton.setIconOnly(true);
        gradientMoveUpButton = iconButton(
                left + quarter + gap,
                y,
                quarter,
                h,
                "Up",
                LoomButton.Icon.UP,
                () -> moveGradient(0, -1)
        );
        gradientMoveUpButton.setIconOnly(true);
        gradientMoveDownButton = iconButton(
                left + (quarter + gap) * 2,
                y,
                quarter,
                h,
                "Down",
                LoomButton.Icon.DOWN,
                () -> moveGradient(0, 1)
        );
        gradientMoveDownButton.setIconOnly(true);
        gradientMoveRightButton = iconButton(
                left + (quarter + gap) * 3,
                y,
                panelWidth - quarter * 3 - gap * 3,
                h,
                "Right",
                LoomButton.Icon.MOVE,
                () -> moveGradient(1, 0)
        );
        gradientMoveRightButton.setIconOnly(true);
        y += h + gap;

        gradientScaleDownButton = iconButton(
                left,
                y,
                small,
                h,
                "Scale -",
                LoomButton.Icon.DOWN,
                () -> scaleGradient(0.9)
        );
        gradientScaleDownButton.setIconOnly(true);

        gradientScaleLabelButton = new LoomButton(
                left + small + gap,
                y,
                centerWidth,
                h,
                Component.literal("100%"),
                () -> { }
        );
        gradientScaleLabelButton.active = false;
        addRenderableWidget(gradientScaleLabelButton);

        gradientScaleUpButton = iconButton(
                left + small + gap + centerWidth + gap,
                y,
                small,
                h,
                "Scale +",
                LoomButton.Icon.UP,
                () -> scaleGradient(1.1)
        );
        gradientScaleUpButton.setIconOnly(true);
        y += h + gap;

        int third = Math.max(
                28,
                (panelWidth - gap * 2) / 3
        );
        gradientMirrorHorizontalButton = iconButton(
                left, y, third, h,
                "Mirror H", LoomButton.Icon.FLIP_H,
                () -> toggleGradientMirror(true)
        );
        gradientMirrorHorizontalButton.setIconOnly(compactMode);

        gradientMirrorVerticalButton = iconButton(
                left + third + gap,
                y,
                third,
                h,
                "Mirror V", LoomButton.Icon.FLIP_V,
                () -> toggleGradientMirror(false)
        );
        gradientMirrorVerticalButton.setIconOnly(compactMode);

        gradientResetTransformButton = iconButton(
                left + (third + gap) * 2,
                y,
                panelWidth - third * 2 - gap * 2,
                h,
                "Reset",
                LoomButton.Icon.RESET,
                this::resetGradientTransform
        );
        gradientResetTransformButton.setIconOnly(compactMode);
        y += h + gap;

        int half = (panelWidth - gap) / 2;
        gradientRepeatButton = iconButton(
                left,
                y,
                half,
                h,
                "Repeat: Off",
                LoomButton.Icon.LOOP,
                this::toggleGradientRepeat
        );
        gradientRepeatButton.setIconOnly(compactMode);

        gradientDitherButton = iconButton(
                left + half + gap,
                y,
                panelWidth - half - gap,
                h,
                "Dither: Off",
                LoomButton.Icon.GRADIENT,
                this::toggleGradientDither
        );
        gradientDitherButton.setIconOnly(compactMode);
        y += h + gap;

        gradientStopPreviousButton = iconButton(
                left,
                y,
                small,
                h,
                "Stop <",
                LoomButton.Icon.BACK,
                () -> selectGradientStop(-1)
        );
        gradientStopPreviousButton.setIconOnly(true);

        gradientStopLabelButton = new LoomButton(
                left + small + gap,
                y,
                centerWidth,
                h,
                Component.literal("Stop"),
                () -> { }
        );
        gradientStopLabelButton.active = false;
        addRenderableWidget(gradientStopLabelButton);

        gradientStopNextButton = iconButton(
                left + small + gap + centerWidth + gap,
                y,
                small,
                h,
                "Stop >",
                LoomButton.Icon.PLAY,
                () -> selectGradientStop(1)
        );
        gradientStopNextButton.setIconOnly(true);
        y += h + gap;

        gradientStopAddButton = iconButton(
                left,
                y,
                half,
                h,
                "Add Stop",
                LoomButton.Icon.PLUS,
                this::addGradientStop
        );
        gradientStopAddButton.setIconOnly(compactMode);

        gradientStopRemoveButton = iconButton(
                left + half + gap,
                y,
                panelWidth - half - gap,
                h,
                "Remove Stop",
                LoomButton.Icon.DELETE,
                this::removeGradientStop
        );
        gradientStopRemoveButton.setIconOnly(compactMode);
        y += h + gap;

        gradientStopPositionDownButton = iconButton(
                left,
                y,
                half,
                h,
                "Position -",
                LoomButton.Icon.DOWN,
                () -> moveGradientStop(-0.05)
        );
        gradientStopPositionDownButton.setIconOnly(compactMode);

        gradientStopPositionUpButton = iconButton(
                left + half + gap,
                y,
                panelWidth - half - gap,
                h,
                "Position +",
                LoomButton.Icon.UP,
                () -> moveGradientStop(0.05)
        );
        gradientStopPositionUpButton.setIconOnly(compactMode);
        y += h + gap;

        gradientStopColorButton = iconButton(
                left,
                y,
                panelWidth,
                h,
                compactMode
                        ? "Set Color"
                        : "Set Stop = Current Color",
                LoomButton.Icon.PALETTE,
                this::setSelectedGradientStopColor
        );
        gradientStopColorButton.setIconOnly(false);
    }

    private void setInspectorTab(InspectorTab tab) {
        this.inspectorTab = tab;
        updateInspectorVisibility();
    }

    private void updateInspectorVisibility() {
        boolean layers = inspectorTab == InspectorTab.LAYERS;
        boolean color = inspectorTab == InspectorTab.COLOR;
        boolean properties =
                inspectorTab == InspectorTab.PROPERTIES;

        LoomLayer layer = workspaceState == null
                || workspaceState.project().cape().layers().isEmpty()
                ? null
                : selectedLayer();

        boolean gradient = properties
                && layer != null
                && layer.kind() == LayerKind.GRADIENT;

        if (inspectorLayersButton != null) {
            inspectorLayersButton.setSelected(layers);
        }
        if (inspectorColorButton != null) {
            inspectorColorButton.setSelected(color);
        }
        if (inspectorPropertiesButton != null) {
            inspectorPropertiesButton.setSelected(properties);
        }

        if (layerListWidget != null) layerListWidget.visible = layers;
        if (layerAddButton != null) layerAddButton.visible = layers;
        if (layerGradientAddButton != null) layerGradientAddButton.visible = layers;
        if (layerImportButton != null) layerImportButton.visible = layers;
        if (layerDuplicateButton != null) layerDuplicateButton.visible = layers;
        if (layerDeleteButton != null) layerDeleteButton.visible = layers;
        if (layerUpButton != null) layerUpButton.visible = layers;
        if (layerDownButton != null) layerDownButton.visible = layers;
        if (layerNameField != null) layerNameField.setVisible(layers);
        if (layerRenameButton != null) layerRenameButton.visible = layers;
        if (layerOpacityDownButton != null) layerOpacityDownButton.visible = layers;
        if (layerOpacityLabelButton != null) layerOpacityLabelButton.visible = layers;
        if (layerOpacityUpButton != null) layerOpacityUpButton.visible = layers;
        if (layerBlendButton != null) layerBlendButton.visible = layers;
        if (layerEmissiveButton != null) layerEmissiveButton.visible = layers;
        if (layerLockButton != null) layerLockButton.visible = layers;

        if (colorPicker != null) colorPicker.visible = color;
        if (hexColorField != null) hexColorField.setVisible(color);
        if (redColorField != null) redColorField.setVisible(color);
        if (greenColorField != null) greenColorField.setVisible(color);
        if (blueColorField != null) blueColorField.setVisible(color);
        if (alphaColorField != null) alphaColorField.setVisible(color);
        if (paletteButton != null) paletteButton.visible = color;

        LoomButton[] gradientButtons = {
                gradientTypeButton,
                gradientAngleDownButton,
                gradientAngleButton,
                gradientAngleUpButton,
                gradientMoveLeftButton,
                gradientMoveUpButton,
                gradientMoveDownButton,
                gradientMoveRightButton,
                gradientScaleDownButton,
                gradientScaleLabelButton,
                gradientScaleUpButton,
                gradientMirrorHorizontalButton,
                gradientMirrorVerticalButton,
                gradientResetTransformButton,
                gradientRepeatButton,
                gradientDitherButton,
                gradientStopPreviousButton,
                gradientStopLabelButton,
                gradientStopNextButton,
                gradientStopAddButton,
                gradientStopRemoveButton,
                gradientStopPositionDownButton,
                gradientStopPositionUpButton,
                gradientStopColorButton
        };
        for (LoomButton button : gradientButtons) {
            if (button != null) {
                button.visible = gradient;
            }
        }

        updateContextVisibility();
    }

    private void updateContextVisibility() {
        boolean selectionTool = tool == Tool.SELECT;
        boolean rectangleTool = tool == Tool.RECTANGLE;
        boolean brushTool = tool == Tool.PENCIL
                || tool == Tool.ERASER
                || tool == Tool.LINE
                || tool == Tool.RECTANGLE;

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
        return "Face: " + this.capeRegion.displayName();
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
        return "Brush " + brushSize + " px";
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
        return "Symmetry: " + this.symmetryMode.label;
    }

    private LoomCapeFaceWidget.GestureMode gestureMode() {
        return switch (tool) {
            case PENCIL, ERASER -> LoomCapeFaceWidget.GestureMode.BRUSH;
            case LINE -> LoomCapeFaceWidget.GestureMode.LINE;
            case RECTANGLE -> LoomCapeFaceWidget.GestureMode.RECTANGLE;
            case SELECT -> LoomCapeFaceWidget.GestureMode.SELECTION;
            case FILL, EYEDROPPER -> LoomCapeFaceWidget.GestureMode.CLICK;
        };
    }

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

        if (selectButton != null) {
            selectButton.setMessage(Component.literal("Select"));
            selectButton.setSelected(tool == Tool.SELECT);
        }

        boolean editablePaintSelection = workspaceState != null
                && !workspaceState.project().cape().layers().isEmpty()
                && selectedLayer().editableAsPaint();
        boolean hasSelection = selection != null && editablePaintSelection;
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
                            ? "Rectangle: Filled"
                            : "Rectangle: Outline"
            ));
        }

        if (undoButton != null) {
            undoButton.active = ClientProjectWorkspace.session().canUndo();
        }

        if (redoButton != null) {
            redoButton.active = ClientProjectWorkspace.session().canRedo();
        }

        if (resolutionLabelButton != null) {
            resolutionLabelButton.setMessage(Component.literal(resolutionLabel()));
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
            resolutionUpButton.active = resolution != CanvasResolution.ULTRA;
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
                                        ? "Emissive: On"
                                        : "Emissive: Off"
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
                                    "Stop "
                                            + (stopIndex + 1)
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
            case LINE, RECTANGLE, SELECT -> {
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
            case RECTANGLE -> ClientProjectWorkspace.apply(project ->
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

            result = ProjectEdits.paintCapeRegionRectangle(
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

    private void notifyPlayer(String text) {
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
        LoomScreenChrome.renderBackdrop(
                graphics,
                width,
                height
        );
        LoomScreenChrome.renderBrandHeader(
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
        if (!compactMode) {
            LoomScreenChrome.panelHeader(
                    graphics,
                    toolRailLeft,
                    contentTop,
                    toolRailRight,
                    "Tools"
            );
        }

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
        LoomScreenChrome.panelHeader(
                graphics,
                rightPanelLeft,
                contentTop,
                rightPanelRight,
                "3D Preview"
        );

        LoomScreenChrome.panel(
                graphics,
                rightPanelLeft,
                inspectorTop - 2,
                rightPanelRight,
                inspectorBottom
        );

        if (!compactMode) {
            String inspectorTitle = switch (inspectorTab) {
                case LAYERS -> "Layers";
                case COLOR -> "Color";
                case PROPERTIES -> {
                    LoomLayer layer = selectedLayer();
                    yield layer.kind() == LayerKind.GRADIENT
                            ? "Gradient Properties"
                            : layer.kind() == LayerKind.IMAGE
                                    ? "Image Properties"
                                    : "Layer Properties";
                }
            };
            graphics.drawString(
                    font,
                    Component.literal(inspectorTitle),
                    rightPanelLeft + 7,
                    inspectorTop + 2,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        if (inspectorTab == InspectorTab.PROPERTIES
                && selectedLayer().kind() != LayerKind.GRADIENT) {
            String hint = selectedLayer().kind() == LayerKind.IMAGE
                    ? "Use Edit Image in Layers to reopen Smart Import."
                    : "Paint layers use the tool rail and context bar.";
            graphics.drawString(
                    font,
                    Component.literal(
                            font.plainSubstrByWidth(
                                    hint,
                                    rightPanelRight - rightPanelLeft - 12
                            )
                    ),
                    rightPanelLeft + 6,
                    inspectorTop + 18,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        String status = ClientProjectWorkspace.isCurrentProjectEquipped()
                ? "Saved / Equipped"
                : workspaceState.dirty()
                        ? "Unsaved edits"
                        : "Saved, not equipped";

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
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (paletteWindowVisible
                && paletteWindow != null
                && paletteWindow.visible
                && paletteWindow.isMouseOver(event.x(), event.y())
                && paletteWindow.mouseClicked(event, doubleClick)) {
            this.setFocused(paletteWindow);
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
        if (isEditingText()) {
            return super.keyPressed(event);
        }

        if (event.hasControlDownWithQuirk()) {
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
                case 66 -> {
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
                case 76 -> {
                    setTool(Tool.LINE);
                    return true;
                }
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
        RECTANGLE
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
}
