package dev.loomstudios.client.ui;

import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Compact typed-layer stack. The visual order is top-most layer first.
 *
 * <p>The row exposes three direct affordances: visibility on the left,
 * type identity beside the layer name, and lock state on the right. Clicking
 * the rest of a row selects it.</p>
 */
public final class LoomLayerListWidget extends AbstractWidget {
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBILITY_HIT_WIDTH = 18;
    private static final int LOCK_HIT_WIDTH = 20;

    private final Supplier<LoomProject> projectSupplier;
    private final Supplier<UUID> selectedLayerSupplier;
    private final Consumer<UUID> onSelect;
    private final Consumer<UUID> onToggleVisibility;
    private final Consumer<UUID> onToggleLock;

    private int scrollRows;

    public LoomLayerListWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            Supplier<UUID> selectedLayerSupplier,
            Consumer<UUID> onSelect,
            Consumer<UUID> onToggleVisibility,
            Consumer<UUID> onToggleLock
    ) {
        super(x, y, width, height, Component.literal("Layers"));
        this.projectSupplier = Objects.requireNonNull(projectSupplier);
        this.selectedLayerSupplier = Objects.requireNonNull(selectedLayerSupplier);
        this.onSelect = Objects.requireNonNull(onSelect);
        this.onToggleVisibility = Objects.requireNonNull(onToggleVisibility);
        this.onToggleLock = Objects.requireNonNull(onToggleLock);
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                getX(),
                getY(),
                getRight(),
                getBottom(),
                LoomUiTheme.BORDER
        );
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                LoomUiTheme.PANEL
        );

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Layers"),
                getX() + 7,
                getY() + 5,
                LoomUiTheme.TEXT,
                false
        );

        List<LoomLayer> layers = projectSupplier.get().cape().layers();
        clampScroll(layers.size());

        int visibleRows = visibleRows();
        UUID selected = selectedLayerSupplier.get();

        for (int row = 0; row < visibleRows; row++) {
            int visualIndex = scrollRows + row;
            int layerIndex = layers.size() - 1 - visualIndex;

            if (layerIndex < 0) {
                break;
            }

            LoomLayer layer = layers.get(layerIndex);
            int y = getY() + HEADER_HEIGHT + row * ROW_HEIGHT;
            boolean isSelected = layer.id().equals(selected);

            graphics.fill(
                    getX() + 2,
                    y,
                    getRight() - 2,
                    y + ROW_HEIGHT - 1,
                    isSelected ? 0xFF213744 : LoomUiTheme.PANEL_INNER
            );
            if (isSelected) {
                graphics.fill(
                        getX() + 2,
                        y,
                        getX() + 4,
                        y + ROW_HEIGHT - 1,
                        LoomUiTheme.ACCENT
                );
            }

            drawVisibilityIcon(
                    graphics,
                    getX() + 6,
                    y + 5,
                    layer.visible()
            );
            drawKindIcon(
                    graphics,
                    getX() + 20,
                    y + 5,
                    layer.kind()
            );

            int lockX = getRight() - 15;
            drawLockIcon(
                    graphics,
                    lockX,
                    y + 5,
                    layer.locked()
            );

            String opacity = Math.round(layer.opacity() * 100.0F) + "%";
            int opacityWidth = Minecraft.getInstance().font.width(opacity);
            int opacityX = lockX - opacityWidth - 6;

            String name = Minecraft.getInstance().font.plainSubstrByWidth(
                    layer.name(),
                    Math.max(18, opacityX - (getX() + 34) - 4)
            );

            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(name),
                    getX() + 34,
                    y + 5,
                    isSelected ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                    false
            );

            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(opacity),
                    opacityX,
                    y + 5,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        if (layers.size() > visibleRows) {
            int trackTop = getY() + HEADER_HEIGHT + 2;
            int trackBottom = getBottom() - 3;
            int trackHeight = Math.max(1, trackBottom - trackTop);
            int thumbHeight = Math.max(
                    12,
                    trackHeight * visibleRows / layers.size()
            );
            int maxScroll = layers.size() - visibleRows;
            int thumbY = trackTop
                    + (int)((trackHeight - thumbHeight)
                    * (scrollRows / (double)Math.max(1, maxScroll)));

            graphics.fill(
                    getRight() - 3,
                    trackTop,
                    getRight() - 1,
                    trackBottom,
                    0xFF1B252E
            );
            graphics.fill(
                    getRight() - 4,
                    thumbY,
                    getRight(),
                    thumbY + thumbHeight,
                    LoomUiTheme.TEXT_MUTED
            );
        }
    }

    private static void drawVisibilityIcon(
            GuiGraphics graphics,
            int x,
            int y,
            boolean visible
    ) {
        int color = visible ? LoomUiTheme.ACCENT : 0xFF3A454E;
        graphics.fill(x + 1, y + 3, x + 8, y + 5, color);
        graphics.fill(x + 3, y + 1, x + 6, y + 7, color);
        graphics.fill(
                x + 4,
                y + 3,
                x + 5,
                y + 4,
                LoomUiTheme.PANEL_INNER
        );
    }

    private static void drawKindIcon(
            GuiGraphics graphics,
            int x,
            int y,
            LayerKind kind
    ) {
        switch (kind) {
            case PAINT -> {
                graphics.fill(x + 1, y + 6, x + 7, y + 8, LoomUiTheme.TEXT);
                graphics.fill(x + 5, y + 2, x + 8, y + 7, LoomUiTheme.ACCENT);
                graphics.fill(x + 7, y + 1, x + 9, y + 3, LoomUiTheme.TEXT);
            }
            case IMAGE -> {
                graphics.fill(x + 1, y + 1, x + 9, y + 2, LoomUiTheme.TEXT_MUTED);
                graphics.fill(x + 1, y + 8, x + 9, y + 9, LoomUiTheme.TEXT_MUTED);
                graphics.fill(x + 1, y + 2, x + 2, y + 8, LoomUiTheme.TEXT_MUTED);
                graphics.fill(x + 8, y + 2, x + 9, y + 8, LoomUiTheme.TEXT_MUTED);
                graphics.fill(x + 3, y + 5, x + 5, y + 8, LoomUiTheme.ACCENT);
                graphics.fill(x + 5, y + 4, x + 8, y + 8, LoomUiTheme.ACCENT_ALT);
            }
            case GRADIENT -> {
                graphics.fill(x + 1, y + 1, x + 5, y + 9, LoomUiTheme.ACCENT);
                graphics.fill(x + 5, y + 1, x + 9, y + 9, LoomUiTheme.ACCENT_ALT);
            }
        }
    }

    private static void drawLockIcon(
            GuiGraphics graphics,
            int x,
            int y,
            boolean locked
    ) {
        int color = locked ? LoomUiTheme.ACCENT_ALT : LoomUiTheme.TEXT_MUTED;

        graphics.fill(x + 2, y + 4, x + 8, y + 9, color);
        if (locked) {
            graphics.fill(x + 3, y + 1, x + 7, y + 2, color);
            graphics.fill(x + 2, y + 2, x + 3, y + 5, color);
            graphics.fill(x + 7, y + 2, x + 8, y + 5, color);
        } else {
            graphics.fill(x + 4, y + 1, x + 8, y + 2, color);
            graphics.fill(x + 7, y + 2, x + 8, y + 4, color);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        List<LoomLayer> layers = projectSupplier.get().cape().layers();
        int localY = (int)event.y() - getY() - HEADER_HEIGHT;

        if (localY < 0) {
            return;
        }

        int row = localY / ROW_HEIGHT;
        int visualIndex = scrollRows + row;
        int layerIndex = layers.size() - 1 - visualIndex;

        if (layerIndex < 0 || layerIndex >= layers.size()) {
            return;
        }

        LoomLayer layer = layers.get(layerIndex);

        if (event.x() < getX() + VISIBILITY_HIT_WIDTH) {
            onToggleVisibility.accept(layer.id());
        } else if (event.x() >= getRight() - LOCK_HIT_WIDTH) {
            onToggleLock.accept(layer.id());
        } else {
            onSelect.accept(layer.id());
        }
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (!isMouseOver(mouseX, mouseY) || scrollY == 0.0) {
            return false;
        }

        List<LoomLayer> layers = projectSupplier.get().cape().layers();
        int max = Math.max(0, layers.size() - visibleRows());

        scrollRows = Math.max(
                0,
                Math.min(max, scrollRows + (scrollY < 0 ? 1 : -1))
        );
        return true;
    }

    private int visibleRows() {
        return Math.max(
                1,
                (getHeight() - HEADER_HEIGHT - 2) / ROW_HEIGHT
        );
    }

    private void clampScroll(int layerCount) {
        int max = Math.max(0, layerCount - visibleRows());
        scrollRows = Math.max(0, Math.min(max, scrollRows));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
