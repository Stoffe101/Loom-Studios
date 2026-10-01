package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.WorkspaceState;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomCapeFaceWidget;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomPaletteButton;
import dev.loomstudios.client.ui.LoomPaletteWindow;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.ProjectEdits;
import dev.loomstudios.project.ProjectResizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

public final class CapeEditorScreen extends Screen {
    private final Screen parent;
    private final Consumer<WorkspaceState> workspaceListener =
            state -> this.workspaceState = state;

    private WorkspaceState workspaceState;
    private Tool tool = Tool.PENCIL;
    private CapeUvRegion capeRegion = CapeUvRegion.OUTSIDE;
    private int selectedColor = 0xFF22D7E8;
    private int brushSize = 1;
    private UUID selectedLayerId;

    private LoomButton pencilButton;
    private LoomButton eraserButton;
    private LoomButton faceButton;
    private LoomButton resolutionDownButton;
    private LoomButton resolutionUpButton;
    private LoomButton brushDownButton;
    private LoomButton brushUpButton;
    private LoomButton resolutionLabelButton;
    private LoomButton brushLabelButton;
    private LoomButton undoButton;
    private LoomButton redoButton;

    private LoomCapeFaceWidget canvasWidget;
    private LoomColorPickerWidget colorPicker;
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
                }
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

        this.colorPicker = new LoomColorPickerWidget(
                0,
                0,
                contentWidth,
                132,
                this.selectedColor,
                color -> this.selectedColor = color
        );
        tools.addChild(this.colorPicker);

        tools.addChild(new LoomPaletteButton(
                0,
                0,
                contentWidth,
                22,
                this::togglePaletteWindow
        ));

        LinearLayout historyRow = LinearLayout.horizontal().spacing(4);
        undoButton = createLoomButton(
                (contentWidth - 4) / 2,
                "Undo",
                ClientProjectWorkspace::undo
        );
        redoButton = createLoomButton(
                contentWidth - 4 - undoButton.getWidth(),
                "Redo",
                ClientProjectWorkspace::redo
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

        int width = 230;
        int height = Math.min(302, Math.max(260, this.height - 28));

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

        if (this.colorPicker != null) {
            this.colorPicker.setColor(color);
        }
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
        updateButtonStates();
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
    }

    private UUID findEditableLayer() {
        return ClientProjectWorkspace.project().cape().layers().stream()
                .filter(layer -> !layer.emissive())
                .findFirst()
                .map(LoomLayer::id)
                .orElseThrow(() -> new IllegalStateException(
                        "Cape project has no editable paint layer"
                ));
    }

    private void editPixel(int x, int y) {
        int color = tool == Tool.ERASER ? 0x00000000 : selectedColor;

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.paintCapeRegionBrush(
                        project,
                        selectedLayerId,
                        this.capeRegion,
                        x,
                        y,
                        this.brushSize,
                        color
                )
        );
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
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private enum Tool {
        PENCIL,
        ERASER
    }
}
