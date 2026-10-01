package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.WorkspaceState;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomCapeFaceWidget;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.ProjectEdits;
import net.minecraft.client.gui.GuiGraphics;
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
    private UUID selectedLayerId;

    private LoomButton pencilButton;
    private LoomButton eraserButton;
    private LoomButton faceButton;
    private LoomButton undoButton;
    private LoomButton redoButton;

    public CapeEditorScreen(Screen parent) {
        super(Component.literal("Loom Studios - Cape Editor"));
        this.parent = parent;
        this.workspaceState = ClientProjectWorkspace.state();
        this.selectedLayerId = findEditableLayer();
    }

    @Override
    protected void init() {
        int margin = 18;
        int toolbarWidth = 150;
        int availableWidth = this.width - margin * 3 - toolbarWidth;
        int canvasWidth = Math.max(256, availableWidth);
        int canvasHeight = Math.min(
                this.height - 90,
                Math.max(160, canvasWidth / 2)
        );

        int canvasX = margin;
        int canvasY = 48;

        addRenderableWidget(new LoomCapeFaceWidget(
                canvasX,
                canvasY,
                canvasWidth,
                canvasHeight,
                () -> this.workspaceState.project(),
                () -> this.capeRegion,
                this::editPixel
        ));

        int toolsX = canvasX + canvasWidth + margin;
        int y = canvasY;

        faceButton = addLoomButton(
                toolsX, y, toolbarWidth, faceLabel(),
                this::cycleFace
        );
        y += 34;

        pencilButton = addLoomButton(
                toolsX, y, toolbarWidth, "Pencil",
                () -> setTool(Tool.PENCIL)
        );
        y += 26;

        eraserButton = addLoomButton(
                toolsX, y, toolbarWidth, "Eraser",
                () -> setTool(Tool.ERASER)
        );
        y += 34;

        addLoomButton(
                toolsX, y, 72, "Cyan",
                () -> this.selectedColor = 0xFF22D7E8
        );
        addLoomButton(
                toolsX + 78, y, 72, "Pink",
                () -> this.selectedColor = 0xFFFF36C8
        );
        y += 26;

        addLoomButton(
                toolsX, y, 72, "Violet",
                () -> this.selectedColor = 0xFF9B4DFF
        );
        addLoomButton(
                toolsX + 78, y, 72, "White",
                () -> this.selectedColor = 0xFFFFFFFF
        );
        y += 34;

        undoButton = addLoomButton(
                toolsX, y, 72, "Undo",
                ClientProjectWorkspace::undo
        );
        redoButton = addLoomButton(
                toolsX + 78, y, 72, "Redo",
                ClientProjectWorkspace::redo
        );
        y += 34;

        addLoomButton(
                toolsX, y, toolbarWidth, "3D Preview",
                () -> this.minecraft.setScreen(
                        new LoomPlayerPreviewScreen(this)
                )
        );
        y += 26;

        addLoomButton(
                toolsX, y, toolbarWidth, "Save",
                this::save
        );
        y += 26;

        addLoomButton(
                toolsX, y, toolbarWidth, "Save + Equip",
                this::saveAndEquip
        );
        y += 34;

        addLoomButton(
                toolsX, y, toolbarWidth, "Back",
                () -> this.minecraft.setScreen(parent)
        );

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
        super.removed();
    }

    private LoomButton addLoomButton(
            int x,
            int y,
            int width,
            String label,
            Runnable action
    ) {
        LoomButton button = new LoomButton(
                x,
                y,
                width,
                22,
                Component.literal(label),
                action
        );
        addRenderableWidget(button);
        return button;
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
                ProjectEdits.setCapeRegionPixel(
                        project,
                        selectedLayerId,
                        this.capeRegion,
                        x,
                        y,
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
