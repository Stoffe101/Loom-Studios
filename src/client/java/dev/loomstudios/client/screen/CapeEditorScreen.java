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
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.BlendMode;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.LayerKind;
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
    private LoomButton gradientAngleButton;
    private LoomButton gradientStartButton;
    private LoomButton gradientEndButton;
    private LoomButton gradientRepeatButton;
    private LoomButton gradientDitherButton;
    private LoomButton layerOpacityDownButton;
    private LoomButton layerOpacityLabelButton;
    private LoomButton layerOpacityUpButton;
    private LoomButton undoButton;
    private LoomButton redoButton;

    private LoomCapeFaceWidget canvasWidget;
    private LoomColorPickerWidget colorPicker;
    private LoomLayerListWidget layerListWidget;
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

    public CapeEditorScreen(Screen parent) {
        super(Component.literal("Loom Studios - Cape Editor"));
        this.parent = parent;
        this.workspaceState = ClientProjectWorkspace.state();
        this.selectedLayerId = findEditableLayer();
    }

    @Override
    protected void init() {
        int margin = 12;
        int canvasY = 48;

        this.toolPanelWidth = Math.min(
                230,
                Math.max(190, this.width / 3)
        );
        this.toolPanelHeight = Math.max(
                120,
                this.height - canvasY - margin
        );

        int canvasWidth = Math.max(
                170,
                this.width - margin * 3 - this.toolPanelWidth
        );
        int canvasHeight = Math.max(
                140,
                this.height - canvasY - margin
        );

        int canvasX = margin;
        this.toolPanelX = canvasX + canvasWidth + margin;
        this.toolPanelY = canvasY;

        this.canvasWidget = addRenderableWidget(new LoomCapeFaceWidget(
                canvasX,
                canvasY,
                canvasWidth,
                canvasHeight,
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
        ));

        int contentWidth = Math.max(150, this.toolPanelWidth - 20);
        LinearLayout tools = LinearLayout.vertical().spacing(6);

        faceButton = createLoomButton(
                contentWidth,
                "Face: " + this.capeRegion.displayName(),
                this::cycleFace
        );
        tools.addChild(faceButton);

        LinearLayout resolutionRow = LinearLayout.horizontal().spacing(4);
        resolutionDownButton = createLoomButton(
                48, "Res -", () -> changeResolution(-1)
        );
        resolutionLabelButton = createLoomButton(
                Math.max(42, contentWidth - 104),
                resolutionLabel(),
                () -> { }
        );
        resolutionLabelButton.active = false;
        resolutionUpButton = createLoomButton(
                48, "Res +", () -> changeResolution(1)
        );
        resolutionRow.addChild(resolutionDownButton);
        resolutionRow.addChild(resolutionLabelButton);
        resolutionRow.addChild(resolutionUpButton);
        tools.addChild(resolutionRow);

        pencilButton = createLoomButton(
                contentWidth,
                "Pencil",
                () -> setTool(Tool.PENCIL)
        );
        tools.addChild(pencilButton);

        eraserButton = createLoomButton(
                contentWidth,
                "Eraser",
                () -> setTool(Tool.ERASER)
        );
        tools.addChild(eraserButton);

        fillButton = createLoomButton(
                contentWidth,
                "Fill",
                () -> setTool(Tool.FILL)
        );
        tools.addChild(fillButton);

        eyedropperButton = createLoomButton(
                contentWidth,
                "Eyedropper",
                () -> setTool(Tool.EYEDROPPER)
        );
        tools.addChild(eyedropperButton);

        selectButton = createLoomButton(
                contentWidth,
                "Select",
                () -> setTool(Tool.SELECT)
        );
        tools.addChild(selectButton);

        lineButton = createLoomButton(
                contentWidth,
                "Line",
                () -> setTool(Tool.LINE)
        );
        tools.addChild(lineButton);

        rectangleButton = createLoomButton(
                contentWidth,
                "Rectangle",
                () -> setTool(Tool.RECTANGLE)
        );
        tools.addChild(rectangleButton);

        rectangleModeButton = createLoomButton(
                contentWidth,
                "Rectangle: Outline",
                this::toggleRectangleMode
        );
        tools.addChild(rectangleModeButton);

        LinearLayout brushRow = LinearLayout.horizontal().spacing(4);
        brushDownButton = createLoomButton(
                48, "Brush -", () -> changeBrushSize(-1)
        );
        brushLabelButton = createLoomButton(
                Math.max(42, contentWidth - 104),
                brushLabel(),
                () -> { }
        );
        brushLabelButton.active = false;
        brushUpButton = createLoomButton(
                48, "Brush +", () -> changeBrushSize(1)
        );
        brushRow.addChild(brushDownButton);
        brushRow.addChild(brushLabelButton);
        brushRow.addChild(brushUpButton);
        tools.addChild(brushRow);

        symmetryButton = createLoomButton(
                contentWidth,
                symmetryLabel(),
                this::cycleSymmetry
        );
        tools.addChild(symmetryButton);

        selectionClearButton = createLoomButton(
                contentWidth,
                "Clear Selection",
                this::clearSelection
        );
        tools.addChild(selectionClearButton);

        LinearLayout selectionHorizontalRow = LinearLayout.horizontal().spacing(4);
        selectionLeftButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Move Left",
                () -> nudgeSelection(-1, 0)
        );
        selectionRightButton = createLoomButton(
                contentWidth - 4 - selectionLeftButton.getWidth(),
                "Move Right",
                () -> nudgeSelection(1, 0)
        );
        selectionHorizontalRow.addChild(selectionLeftButton);
        selectionHorizontalRow.addChild(selectionRightButton);
        tools.addChild(selectionHorizontalRow);

        LinearLayout selectionVerticalRow = LinearLayout.horizontal().spacing(4);
        selectionUpButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Move Up",
                () -> nudgeSelection(0, -1)
        );
        selectionDownButton = createLoomButton(
                contentWidth - 4 - selectionUpButton.getWidth(),
                "Move Down",
                () -> nudgeSelection(0, 1)
        );
        selectionVerticalRow.addChild(selectionUpButton);
        selectionVerticalRow.addChild(selectionDownButton);
        tools.addChild(selectionVerticalRow);

        LinearLayout selectionFlipRow = LinearLayout.horizontal().spacing(4);
        selectionFlipHorizontalButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Flip H",
                () -> flipSelection(true, false)
        );
        selectionFlipVerticalButton = createLoomButton(
                contentWidth - 4 - selectionFlipHorizontalButton.getWidth(),
                "Flip V",
                () -> flipSelection(false, true)
        );
        selectionFlipRow.addChild(selectionFlipHorizontalButton);
        selectionFlipRow.addChild(selectionFlipVerticalButton);
        tools.addChild(selectionFlipRow);

        LinearLayout zoomRow = LinearLayout.horizontal().spacing(4);
        zoomOutButton = createLoomButton(
                48, "Zoom -", () -> canvasWidget.zoomOut()
        );
        zoomLabelButton = createLoomButton(
                Math.max(42, contentWidth - 104),
                "100%",
                () -> canvasWidget.resetZoom()
        );
        zoomInButton = createLoomButton(
                48, "Zoom +", () -> canvasWidget.zoomIn()
        );
        zoomRow.addChild(zoomOutButton);
        zoomRow.addChild(zoomLabelButton);
        zoomRow.addChild(zoomInButton);
        tools.addChild(zoomRow);

        this.colorPicker = new LoomColorPickerWidget(
                0,
                0,
                contentWidth,
                150,
                this.selectedColor,
                this::setSelectedColor
        );
        tools.addChild(this.colorPicker);

        this.hexColorField = createHexColorField(contentWidth);
        tools.addChild(this.hexColorField);

        LinearLayout rgbaRow = LinearLayout.horizontal().spacing(4);
        int channelWidth = Math.max(30, (contentWidth - 12) / 4);

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
                contentWidth - channelWidth * 3 - 12
        );

        rgbaRow.addChild(this.redColorField);
        rgbaRow.addChild(this.greenColorField);
        rgbaRow.addChild(this.blueColorField);
        rgbaRow.addChild(this.alphaColorField);
        tools.addChild(rgbaRow);

        tools.addChild(new LoomPaletteButton(
                0,
                0,
                contentWidth,
                22,
                this::togglePaletteWindow
        ));

        this.layerListWidget = new LoomLayerListWidget(
                0,
                0,
                contentWidth,
                92,
                () -> this.workspaceState.project(),
                () -> this.selectedLayerId,
                this::selectLayer,
                this::toggleLayerVisibility
        );
        tools.addChild(this.layerListWidget);

        LinearLayout layerCreateRow = LinearLayout.horizontal().spacing(4);
        int layerThird = Math.max(38, (contentWidth - 8) / 3);

        layerAddButton = createLoomButton(
                layerThird,
                "New",
                this::addLayer
        );
        layerDuplicateButton = createLoomButton(
                layerThird,
                "Duplicate",
                this::duplicateLayer
        );
        layerDeleteButton = createLoomButton(
                contentWidth - layerThird * 2 - 8,
                "Delete",
                this::deleteLayer
        );

        layerCreateRow.addChild(layerAddButton);
        layerCreateRow.addChild(layerDuplicateButton);
        layerCreateRow.addChild(layerDeleteButton);
        tools.addChild(layerCreateRow);

        LinearLayout typedCreateRow = LinearLayout.horizontal().spacing(4);
        layerGradientAddButton = createLoomButton(
                (contentWidth - 4) / 2,
                "New Gradient",
                this::addGradientLayer
        );
        layerImportButton = createLoomButton(
                contentWidth - 4 - layerGradientAddButton.getWidth(),
                "Import PNG",
                this::openSmartImport
        );
        typedCreateRow.addChild(layerGradientAddButton);
        typedCreateRow.addChild(layerImportButton);
        tools.addChild(typedCreateRow);

        LinearLayout layerMoveRow = LinearLayout.horizontal().spacing(4);
        layerUpButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Layer Up",
                () -> moveLayer(1)
        );
        layerDownButton = createLoomButton(
                contentWidth - 4 - layerUpButton.getWidth(),
                "Layer Down",
                () -> moveLayer(-1)
        );
        layerMoveRow.addChild(layerUpButton);
        layerMoveRow.addChild(layerDownButton);
        tools.addChild(layerMoveRow);

        LinearLayout layerNameRow = LinearLayout.horizontal().spacing(4);
        this.layerNameField = new EditBox(
                this.font,
                0,
                0,
                Math.max(72, contentWidth - 74),
                18,
                Component.literal("Layer name")
        );
        this.layerNameField.setMaxLength(
                dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_NAME_CHARS
        );
        this.layerNameField.setHint(Component.literal("Layer name"));

        this.layerRenameButton = createLoomButton(
                Math.max(70, contentWidth - this.layerNameField.getWidth() - 4),
                "Rename",
                this::renameLayer
        );

        layerNameRow.addChild(this.layerNameField);
        layerNameRow.addChild(this.layerRenameButton);
        tools.addChild(layerNameRow);

        this.layerBlendButton = createLoomButton(
                contentWidth,
                "Blend: Normal",
                this::cycleLayerBlendMode
        );
        tools.addChild(this.layerBlendButton);

        this.layerEmissiveButton = createLoomButton(
                contentWidth,
                "Emissive: Off",
                this::toggleLayerEmissive
        );
        tools.addChild(this.layerEmissiveButton);

        this.layerLockButton = createLoomButton(
                contentWidth,
                "Lock: Off",
                this::toggleLayerLock
        );
        tools.addChild(this.layerLockButton);

        this.gradientTypeButton = createLoomButton(
                contentWidth,
                "Gradient Type",
                this::cycleGradientType
        );
        tools.addChild(this.gradientTypeButton);

        LinearLayout gradientAngleRow =
                LinearLayout.horizontal().spacing(4);
        gradientAngleRow.addChild(createLoomButton(
                48,
                "Angle -",
                () -> rotateGradient(-15.0)
        ));
        this.gradientAngleButton = createLoomButton(
                Math.max(42, contentWidth - 104),
                "0°",
                () -> { }
        );
        this.gradientAngleButton.active = false;
        gradientAngleRow.addChild(this.gradientAngleButton);
        gradientAngleRow.addChild(createLoomButton(
                48,
                "Angle +",
                () -> rotateGradient(15.0)
        ));
        tools.addChild(gradientAngleRow);

        LinearLayout gradientColorRow =
                LinearLayout.horizontal().spacing(4);
        this.gradientStartButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Set Start",
                () -> setGradientEndpoint(true)
        );
        this.gradientEndButton = createLoomButton(
                contentWidth - 4 - gradientStartButton.getWidth(),
                "Set End",
                () -> setGradientEndpoint(false)
        );
        gradientColorRow.addChild(this.gradientStartButton);
        gradientColorRow.addChild(this.gradientEndButton);
        tools.addChild(gradientColorRow);

        LinearLayout gradientEffectRow =
                LinearLayout.horizontal().spacing(4);
        this.gradientRepeatButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Repeat: Off",
                this::toggleGradientRepeat
        );
        this.gradientDitherButton = createLoomButton(
                contentWidth - 4 - gradientRepeatButton.getWidth(),
                "Dither: Off",
                this::toggleGradientDither
        );
        gradientEffectRow.addChild(this.gradientRepeatButton);
        gradientEffectRow.addChild(this.gradientDitherButton);
        tools.addChild(gradientEffectRow);

        LinearLayout layerOpacityRow = LinearLayout.horizontal().spacing(4);
        layerOpacityDownButton = createLoomButton(
                48,
                "Op -",
                () -> changeLayerOpacity(-0.1F)
        );
        layerOpacityLabelButton = createLoomButton(
                Math.max(42, contentWidth - 104),
                "Opacity 100%",
                () -> { }
        );
        layerOpacityLabelButton.active = false;
        layerOpacityUpButton = createLoomButton(
                48,
                "Op +",
                () -> changeLayerOpacity(0.1F)
        );
        layerOpacityRow.addChild(layerOpacityDownButton);
        layerOpacityRow.addChild(layerOpacityLabelButton);
        layerOpacityRow.addChild(layerOpacityUpButton);
        tools.addChild(layerOpacityRow);

        LinearLayout historyRow = LinearLayout.horizontal().spacing(4);
        undoButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Undo",
                this::undo
        );
        redoButton = createLoomButton(
                contentWidth - 4 - undoButton.getWidth(),
                "Redo",
                this::redo
        );
        historyRow.addChild(undoButton);
        historyRow.addChild(redoButton);
        tools.addChild(historyRow);

        tools.addChild(createLoomButton(
                contentWidth,
                "3D Preview",
                () -> this.minecraft.setScreen(
                        new LoomPlayerPreviewScreen(this)
                )
        ));

        tools.addChild(createLoomButton(
                contentWidth,
                "Save",
                this::save
        ));

        tools.addChild(createLoomButton(
                contentWidth,
                "Save + Equip",
                this::saveAndEquip
        ));

        tools.addChild(createLoomButton(
                contentWidth,
                "Back",
                () -> this.minecraft.setScreen(parent)
        ));

        ScrollableLayout scrollableTools = new ScrollableLayout(
                this.minecraft,
                tools,
                this.toolPanelHeight
        );
        scrollableTools.setMinWidth(this.toolPanelWidth);
        scrollableTools.setMaxHeight(this.toolPanelHeight);
        scrollableTools.arrangeElements();
        scrollableTools.setX(this.toolPanelX);
        scrollableTools.setY(this.toolPanelY);
        scrollableTools.visitWidgets(widget ->
                addRenderableWidget((AbstractWidget) widget)
        );

        restoreOrCreatePaletteWindow();
        syncColorFields();
        syncLayerFields();
        updateButtonStates();
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
    }

    private void updateButtonStates() {
        if (pencilButton != null) {
            pencilButton.setMessage(Component.literal(
                    tool == Tool.PENCIL ? "Pencil ●" : "Pencil"
            ));
        }

        if (eraserButton != null) {
            eraserButton.setMessage(Component.literal(
                    tool == Tool.ERASER ? "Eraser ●" : "Eraser"
            ));
        }

        if (fillButton != null) {
            fillButton.setMessage(Component.literal(
                    tool == Tool.FILL ? "Fill ●" : "Fill"
            ));
        }

        if (eyedropperButton != null) {
            eyedropperButton.setMessage(Component.literal(
                    tool == Tool.EYEDROPPER ? "Eyedropper ●" : "Eyedropper"
            ));
        }

        if (lineButton != null) {
            lineButton.setMessage(Component.literal(
                    tool == Tool.LINE ? "Line ●" : "Line"
            ));
        }

        if (rectangleButton != null) {
            rectangleButton.setMessage(Component.literal(
                    tool == Tool.RECTANGLE ? "Rectangle ●" : "Rectangle"
            ));
        }

        if (selectButton != null) {
            selectButton.setMessage(Component.literal(
                    tool == Tool.SELECT ? "Select ●" : "Select"
            ));
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
                layerImportButton.active =
                        layers.size() < dev.loomstudios.project.LoomProjectCodec.MAX_LAYER_COUNT;
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
            if (layerRenameButton != null) {
                layerRenameButton.active = layerNameField != null
                        && !layerNameField.getValue().trim().isEmpty();
            }
        }
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
        this.selection = null;
        syncLayerFields();
        updateButtonStates();
    }

    private void openSmartImport() {
        this.selection = null;
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
        LoomLayer layer = selectedLayer();

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setCapeLayerLocked(
                        project,
                        selectedLayerId,
                        !layer.locked()
                )
        );

        this.selection = null;
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
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                LoomUiTheme.BACKDROP
        );

        String title = workspaceState.project().name()
                + (workspaceState.dirty() ? " *" : "");

        graphics.drawString(
                this.font,
                Component.literal(title),
                18,
                18,
                LoomUiTheme.TEXT,
                false
        );

        graphics.drawString(
                this.font,
                Component.literal(
                        ClientProjectWorkspace.isCurrentProjectEquipped()
                                ? "Saved / Equipped"
                                : (workspaceState.dirty()
                                        ? "Unsaved edits"
                                        : "Saved, not equipped")
                ),
                18,
                30,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        if (this.capeRegion == CapeUvRegion.OUTSIDE) {
            graphics.drawString(
                    this.font,
                    Component.literal("Outside / Back = the main face other players see"),
                    Math.max(18, this.width - 360),
                    18,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        graphics.fill(
                this.toolPanelX - 4,
                this.toolPanelY - 4,
                this.toolPanelX + this.toolPanelWidth,
                this.toolPanelY + this.toolPanelHeight + 4,
                LoomUiTheme.PANEL
        );

        super.render(graphics, mouseX, mouseY, partialTick);
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
