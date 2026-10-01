package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.ProjectDescriptor;
import dev.loomstudios.client.project.ProjectLibraryIndex;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomUiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.List;

public final class LoomHomeScreen extends Screen {
    private static final Component TITLE = Component.literal("Loom Studios");

    public LoomHomeScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        ProjectLibraryIndex.refresh();

        int center = this.width / 2;
        int top = Math.max(28, this.height / 2 - 120);

        addRenderableWidget(new LoomButton(
                center - 150,
                top + 42,
                140,
                22,
                Component.literal("Create New Cape"),
                this::createNewCape
        ));

        LoomButton open = new LoomButton(
                center + 10,
                top + 42,
                140,
                22,
                Component.literal("Open Selected"),
                this::openSelected
        );
        open.active = ProjectLibraryIndex.selected().isPresent();
        addRenderableWidget(open);

        List<ProjectDescriptor> recent = ProjectLibraryIndex.entries()
                .stream()
                .limit(5)
                .toList();

        int y = top + 88;
        for (ProjectDescriptor descriptor : recent) {
            LoomButton projectButton = new LoomButton(
                    center - 150,
                    y,
                    300,
                    22,
                    Component.literal(descriptor.name()),
                    () -> {
                        ProjectLibraryIndex.select(descriptor.projectId());
                        this.rebuildWidgets();
                    }
            );
            projectButton.active = !ProjectLibraryIndex.selected()
                    .map(selected ->
                            selected.projectId().equals(descriptor.projectId()))
                    .orElse(false);
            addRenderableWidget(projectButton);
            y += 26;
        }
    }

    private void createNewCape() {
        if (this.minecraft.player == null) {
            return;
        }

        ClientProjectWorkspace.createBlank(
                "Untitled Cape",
                System.currentTimeMillis(),
                this.minecraft.player.getUUID()
        );
        this.minecraft.setScreen(new CapeEditorScreen(this));
    }

    private void openSelected() {
        if (this.minecraft.player == null) {
            return;
        }

        ProjectLibraryIndex.selected().ifPresent(descriptor -> {
            try {
                ClientProjectWorkspace.open(
                        descriptor.projectPath(),
                        this.minecraft.player.getUUID()
                );
                this.minecraft.setScreen(new CapeEditorScreen(this));
            } catch (IOException | IllegalArgumentException e) {
                LoomStudios.LOGGER.error(
                        "Failed to open Loom project {}",
                        descriptor.projectPath(),
                        e
                );
            }
        });
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int panelWidth = Math.min(380, this.width - 32);
        int panelHeight = Math.min(300, this.height - 32);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        graphics.fill(
                left,
                top,
                left + panelWidth,
                top + panelHeight,
                LoomUiTheme.BACKDROP
        );
        graphics.fill(
                left + 2,
                top + 2,
                left + panelWidth - 2,
                top + panelHeight - 2,
                LoomUiTheme.PANEL
        );
        graphics.fill(
                left,
                top,
                left + panelWidth,
                top + 3,
                LoomUiTheme.ACCENT
        );

        graphics.drawCenteredString(
                this.font,
                TITLE,
                this.width / 2,
                top + 14,
                LoomUiTheme.TEXT
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal("Create, edit, preview and equip Loom projects"),
                this.width / 2,
                top + 28,
                LoomUiTheme.TEXT_MUTED
        );

        graphics.drawString(
                this.font,
                Component.literal("Recent Projects"),
                this.width / 2 - 150,
                top + 72,
                LoomUiTheme.ACCENT,
                false
        );

        if (ProjectLibraryIndex.entries().isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("No saved projects yet"),
                    this.width / 2,
                    top + 112,
                    LoomUiTheme.TEXT_MUTED
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
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
