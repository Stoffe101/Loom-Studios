package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomColorPickerWidget;
import dev.loomstudios.client.ui.LoomElytraCanvasWidget;
import dev.loomstudios.client.ui.LoomLayerListWidget;
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
    private LoomLayerListWidget layerListWidget;

    private LoomButton layerAddButton;
    private LoomButton layerDuplicateButton;
    private LoomButton layerDeleteButton;

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
                72,
                Math.min(96, rightHeight / 3)
        );
        int actionHeight = 20;
        int previewHeight = Math.max(
                84,
                rightHeight - layerListHeight - actionHeight * 2 - 18
        );

        this.previewWidget = new LoomPlayerPreviewWidget(
                previewLeft,
                contentTop,
                rightWidth,
                previewHeight,
                ClientProjectWorkspace::project,
                LoomPlayerPreviewWidget.Mode.ELYTRA
        );
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

        addRenderableWidget(new LoomButton(
                previewLeft,
                actionsTop + actionHeight + 5,
                rightWidth,
                actionHeight,
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

    private void undo() {
        ClientProjectWorkspace.undo();
        ensureSelectedLayerExists();
        updateButtonStates();
    }

    private void redo() {
        ClientProjectWorkspace.redo();
        ensureSelectedLayerExists();
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
