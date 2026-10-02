package dev.loomstudios.client.ui;

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
 * Compact layer stack. The visual order is top-most layer first.
 *
 * <p>Click a row to select it. Click the visibility square on the left to
 * toggle that layer without changing the selection.</p>
 */
public final class LoomLayerListWidget extends AbstractWidget {
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 18;

    private final Supplier<LoomProject> projectSupplier;
    private final Supplier<UUID> selectedLayerSupplier;
    private final Consumer<UUID> onSelect;
    private final Consumer<UUID> onToggleVisibility;

    private int scrollRows;

    public LoomLayerListWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            Supplier<UUID> selectedLayerSupplier,
            Consumer<UUID> onSelect,
            Consumer<UUID> onToggleVisibility
    ) {
        super(x, y, width, height, Component.literal("Layers"));
        this.projectSupplier = Objects.requireNonNull(projectSupplier);
        this.selectedLayerSupplier = Objects.requireNonNull(selectedLayerSupplier);
        this.onSelect = Objects.requireNonNull(onSelect);
        this.onToggleVisibility = Objects.requireNonNull(onToggleVisibility);
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

            int eyeX = getX() + 6;
            int eyeY = y + 5;
            graphics.fill(
                    eyeX,
                    eyeY,
                    eyeX + 8,
                    eyeY + 8,
                    layer.visible()
                            ? LoomUiTheme.ACCENT
                            : 0xFF3A454E
            );

            String kind = switch (layer.kind()) {
                case PAINT -> "P";
                case IMAGE -> "I";
                case GRADIENT -> "G";
            };
            String name = "["
                    + kind
                    + "] "
                    + (layer.locked() ? "[L] " : "")
                    + layer.name();
            int maxNameWidth = Math.max(24, getWidth() - 72);
            name = Minecraft.getInstance().font.plainSubstrByWidth(
                    name,
                    maxNameWidth
            );

            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(name),
                    getX() + 20,
                    y + 5,
                    isSelected ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                    false
            );

            String opacity = Math.round(layer.opacity() * 100.0F) + "%";
            int opacityWidth = Minecraft.getInstance().font.width(opacity);

            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(opacity),
                    getRight() - opacityWidth - 7,
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

        if (event.x() < getX() + 18) {
            onToggleVisibility.accept(layer.id());
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
