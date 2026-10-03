package dev.loomstudios.client.ui;

import dev.loomstudios.project.LayerKind;
import dev.loomstudios.project.LoomCanvas;
import dev.loomstudios.project.LoomLayer;
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
    private java.util.function.Function<UUID,String> groupLabels=id->"";
    public LoomLayerListWidget setGroupLabels(java.util.function.Function<UUID,String> labels){groupLabels=labels;return this;}
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBILITY_HIT_WIDTH = 18;
    private static final int LOCK_HIT_WIDTH = 20;

    private final Supplier<LoomCanvas> canvasSupplier;
    private final Supplier<UUID> selectedLayerSupplier;
    private final Consumer<UUID> onSelect;
    private final Consumer<UUID> onToggleVisibility;
    private final Consumer<UUID> onToggleLock;

    private int scrollRows;
    private Runnable manage;
    private java.util.Set<UUID> multi=java.util.Set.of();
    private java.util.function.BiConsumer<UUID,net.minecraft.client.input.MouseButtonEvent> selectionHandler;
    public LoomLayerListWidget setManage(Runnable action){manage=action;return this;}
    public LoomLayerListWidget setMultiSelection(java.util.Set<UUID> ids,java.util.function.BiConsumer<UUID,net.minecraft.client.input.MouseButtonEvent> handler){multi=ids;selectionHandler=handler;return this;}
    private boolean elytraThumbnails;
    public LoomLayerListWidget setElytraThumbnails(boolean elytra) { elytraThumbnails = elytra; return this; }
    private boolean draggingScrollbar;
    private record Thumbnail(LoomLayer layer, int width, int height, int[] pixels) { }
    private final java.util.Map<UUID, Thumbnail> thumbnails = new java.util.HashMap<>();

    public LoomLayerListWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomCanvas> canvasSupplier,
            Supplier<UUID> selectedLayerSupplier,
            Consumer<UUID> onSelect,
            Consumer<UUID> onToggleVisibility,
            Consumer<UUID> onToggleLock
    ) {
        super(x, y, width, height, Component.literal("Layers"));
        this.canvasSupplier = Objects.requireNonNull(canvasSupplier);
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

        List<LoomLayer> layers = canvasSupplier.get().layers();
        if(manage!=null){boolean hot=mouseX>=getRight()-52&&mouseX<getRight()-3&&mouseY>=getY()+2&&mouseY<getY()+17;graphics.fill(getRight()-52,getY()+2,getRight()-3,getY()+17,hot?0xFF244353:0xFF1B3044);graphics.drawString(Minecraft.getInstance().font,"Manage",getRight()-46,getY()+5,LoomUiTheme.ACCENT,false);}
        thumbnails.keySet().removeIf(id -> layers.stream().noneMatch(layer -> layer.id().equals(id)));
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
            boolean isSelected = layer.id().equals(selected)||multi.contains(layer.id());

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
            drawThumbnail(graphics, getX() + 21, y + 1, layer);
            drawKindIcon(graphics, getX() + 39, y + 4, layer.kind());

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
                    groupLabels.apply(layer.id()).isEmpty()?layer.name():groupLabels.apply(layer.id())+" · "+layer.name(),
                    Math.max(18, opacityX - (getX() + 52) - 4)
            );

            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(name),
                    getX() + 52,
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

    private void drawThumbnail(GuiGraphics g, int left, int top, LoomLayer layer) {
        var canvas = canvasSupplier.get();
        Thumbnail cached = thumbnails.get(layer.id());
        if (cached == null || cached.layer() != layer || cached.width() != canvas.width() || cached.height() != canvas.height()) {
            cached = new Thumbnail(layer, canvas.width(), canvas.height(), dev.loomstudios.project.LayerRasterizer.rasterize(layer, canvas.width(), canvas.height()));
            thumbnails.put(layer.id(), cached);
        }
        int scale = dev.loomstudios.project.CanvasResolution.fromCanvas(canvas).scale();
        for (int y = 0; y < 8; y++) for (int x = 0; x < 6; x++) {
            int sx = scale + x * 10 * scale / 6, sy = scale + y * 16 * scale / 8;
            if (elytraThumbnails) {
                var wing = x < 3 ? dev.loomstudios.project.ElytraWing.LEFT : dev.loomstudios.project.ElytraWing.RIGHT;
                sx = wing.atlasX((x % 3) * wing.width(scale) / 3, scale);
                sy = wing.atlasY(y * wing.height(scale) / 8, scale);
            }
            int color = cached.pixels()[sy * cached.width() + sx];
            int px = left + x * 2, py = top + y * 2;
            g.fill(px, py, px + 2, py + 2, ((x+y)&1) == 0 ? 0xFF34445A : 0xFF233044);
            g.fill(px, py, px + 2, py + 2, color);
        }
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(manage!=null&&event.button()==0&&isMouseOver(event.x(),event.y())&&event.y()<getY()+HEADER_HEIGHT&&event.x()>getRight()-52){manage.run();return true;}
        if (isMouseOver(event.x(), event.y()) && event.x() >= getRight() - 6
                && canvasSupplier.get().layers().size() > visibleRows()) {
            draggingScrollbar = true; scrollTo(event.y()); return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    private void scrollTo(double y) {
        int max = Math.max(0, canvasSupplier.get().layers().size() - visibleRows());
        double ratio = Math.max(0, Math.min(1, (y - getY() - HEADER_HEIGHT) / Math.max(1, getHeight() - HEADER_HEIGHT - 3)));
        scrollRows = (int)Math.round(ratio * max);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollbar) { scrollTo(event.y()); return true; }
        return super.mouseDragged(event, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingScrollbar) { draggingScrollbar = false; return true; }
        return super.mouseReleased(event);
    }

    private static void drawVisibilityIcon(GuiGraphics g, int x, int y, boolean visible) {
        int color = visible ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT_FAINT;
        g.fill(x+2,y+1,x+7,y+2,color); g.fill(x+2,y+6,x+7,y+7,color);
        g.fill(x,y+3,x+2,y+5,color); g.fill(x+7,y+3,x+9,y+5,color);
        g.fill(x+1,y+2,x+3,y+3,color); g.fill(x+6,y+2,x+8,y+3,color);
        g.fill(x+1,y+5,x+3,y+6,color); g.fill(x+6,y+5,x+8,y+6,color);
        if (visible) g.fill(x+4,y+3,x+6,y+5,color);
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
        List<LoomLayer> layers = canvasSupplier.get().layers();
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
            if(selectionHandler!=null)selectionHandler.accept(layer.id(),event);else onSelect.accept(layer.id());
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

        List<LoomLayer> layers = canvasSupplier.get().layers();
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
