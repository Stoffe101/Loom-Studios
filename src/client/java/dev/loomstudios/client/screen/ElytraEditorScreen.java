package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomElytraCanvasWidget;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.ElytraWing;
import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.ProjectEdits;
import dev.loomstudios.project.ProjectResizer;
import net.minecraft.client.gui.GuiGraphics;
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

    private LoomElytraCanvasWidget canvasWidget;
    private LoomColorPickerWidget colorPicker;
    private LoomPlayerPreviewWidget previewWidget;

    private LoomButton pencilButton;
    private LoomButton eraserButton;
    private LoomButton linkButton;
    private LoomButton brushDownButton;
    private LoomButton brushLabelButton;
    private LoomButton brushUpButton;
    private LoomButton resolutionDownButton;
    private LoomButton resolutionLabelButton;
    private LoomButton resolutionUpButton;
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
        ensureSelectedPaintLayer();

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

        int previewHeight = Math.max(
                100,
                shellBottom - contentTop - 34
        );
        this.previewWidget = new LoomPlayerPreviewWidget(
                previewLeft,
                contentTop,
                previewRight - previewLeft,
                previewHeight,
                ClientProjectWorkspace::project,
                LoomPlayerPreviewWidget.Mode.ELYTRA
        );
        addRenderableWidget(this.previewWidget);

        addRenderableWidget(new LoomButton(
                previewLeft,
                contentTop + previewHeight + 6,
                previewRight - previewLeft,
                20,
                Component.literal("Reset 3D View"),
                this.previewWidget::resetView
        ));

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
        LoomLayer layer = selectedPaintLayer();
        if (layer == null || layer.locked()) {
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

    private void toggleLock() {
        LoomLayer layer = selectedPaintLayer();
        if (layer == null) {
            return;
        }

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.setElytraLayerLocked(
                        project,
                        selectedLayerId,
                        !layer.locked()
                )
        );
        updateButtonStates();
    }

    private void undo() {
        ClientProjectWorkspace.undo();
        ensureSelectedPaintLayer();
        updateButtonStates();
    }

    private void redo() {
        ClientProjectWorkspace.redo();
        ensureSelectedPaintLayer();
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
        if (!ClientProjectWorkspace.isInitialized()) {
            selectedLayerId = null;
            return;
        }

        boolean exists = selectedLayerId != null
                && ClientProjectWorkspace.project().elytra().layers().stream()
                .anyMatch(layer -> layer.id().equals(selectedLayerId)
                        && layer.kind() == LayerKind.PAINT);

        if (exists) {
            return;
        }

        selectedLayerId = ClientProjectWorkspace.project()
                .elytra()
                .layers()
                .stream()
                .filter(layer -> layer.kind() == LayerKind.PAINT)
                .findFirst()
                .map(LoomLayer::id)
                .orElse(null);
    }

    private LoomLayer selectedPaintLayer() {
        ensureSelectedPaintLayer();
        if (selectedLayerId == null) {
            return null;
        }

        return ClientProjectWorkspace.project().elytra().layers().stream()
                .filter(layer -> layer.id().equals(selectedLayerId))
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
        LoomLayer layer = selectedPaintLayer();
        boolean editable = layer != null && !layer.locked();

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
        if (lockButton != null) {
            lockButton.active = layer != null;
            lockButton.setMessage(Component.literal(
                    "Layer Lock: "
                            + (layer != null && layer.locked()
                                    ? "On"
                                    : "Off")
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
