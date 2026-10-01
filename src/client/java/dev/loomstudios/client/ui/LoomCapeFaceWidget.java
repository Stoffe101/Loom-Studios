package dev.loomstudios.client.ui;

import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Focused cape-face editor. It intentionally edits one real UV face at a time
 * instead of presenting the mostly-unused 64x32 atlas as the primary canvas.
 */
public final class LoomCapeFaceWidget extends AbstractWidget {
    @FunctionalInterface
    public interface PixelAction {
        void apply(int localX, int localY);
    }

    private static final int HEADER_HEIGHT = 24;
    private static final int INNER_MARGIN = 10;

    private final Supplier<LoomProject> projectSupplier;
    private final Supplier<CapeUvRegion> regionSupplier;
    private final PixelAction pixelAction;

    private int hoveredX = -1;
    private int hoveredY = -1;

    public LoomCapeFaceWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            Supplier<CapeUvRegion> regionSupplier,
            PixelAction pixelAction
    ) {
        super(x, y, width, height, Component.literal("Cape face canvas"));
        this.projectSupplier = Objects.requireNonNull(projectSupplier);
        this.regionSupplier = Objects.requireNonNull(regionSupplier);
        this.pixelAction = Objects.requireNonNull(pixelAction);
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        LoomProject project = projectSupplier.get();
        CapeUvRegion region = regionSupplier.get();

        int[] pixels = LoomTextureCompiler.compile(
                project.cape(),
                0,
                false,
                false
        );

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
                LoomUiTheme.PANEL_INNER
        );

        String header = region.displayName()
                + "  •  "
                + region.width()
                + "×"
                + region.height();

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(header),
                getX() + 10,
                getY() + 8,
                LoomUiTheme.TEXT,
                false
        );

        updateHover(mouseX, mouseY);

        if (hoveredX >= 0) {
            String coords = "Pixel "
                    + hoveredX
                    + ", "
                    + hoveredY
                    + "  •  UV "
                    + region.atlasX(hoveredX)
                    + ", "
                    + region.atlasY(hoveredY);

            int width = Minecraft.getInstance().font.width(coords);
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(coords),
                    getRight() - width - 10,
                    getY() + 8,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        CanvasGeometry geometry = geometry(region);

        for (int y = 0; y < region.height(); y++) {
            for (int x = 0; x < region.width(); x++) {
                int px = geometry.left + x * geometry.pixelScale;
                int py = geometry.top + y * geometry.pixelScale;

                int checker = ((x + y) & 1) == 0
                        ? 0xFF303A44
                        : 0xFF222A31;

                graphics.fill(
                        px,
                        py,
                        px + geometry.pixelScale,
                        py + geometry.pixelScale,
                        checker
                );

                int atlasX = region.atlasX(x);
                int atlasY = region.atlasY(y);
                int color = pixels[
                        atlasY * project.cape().width() + atlasX
                ];

                if (((color >>> 24) & 0xFF) != 0) {
                    graphics.fill(
                            px,
                            py,
                            px + geometry.pixelScale,
                            py + geometry.pixelScale,
                            color
                    );
                }

                if (geometry.pixelScale >= 4) {
                    graphics.fill(
                            px,
                            py,
                            px + geometry.pixelScale,
                            py + 1,
                            0x3A000000
                    );
                    graphics.fill(
                            px,
                            py,
                            px + 1,
                            py + geometry.pixelScale,
                            0x3A000000
                    );
                }
            }
        }

        if (hoveredX >= 0) {
            int px = geometry.left + hoveredX * geometry.pixelScale;
            int py = geometry.top + hoveredY * geometry.pixelScale;
            int s = geometry.pixelScale;

            graphics.fill(px, py, px + s, py + 2, LoomUiTheme.ACCENT);
            graphics.fill(px, py + s - 2, px + s, py + s, LoomUiTheme.ACCENT);
            graphics.fill(px, py, px + 2, py + s, LoomUiTheme.ACCENT);
            graphics.fill(px + s - 2, py, px + s, py + s, LoomUiTheme.ACCENT);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        applyAt(event.x(), event.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        applyAt(event.x(), event.y());
    }

    private void applyAt(double mouseX, double mouseY) {
        CapeUvRegion region = regionSupplier.get();
        CanvasGeometry geometry = geometry(region);

        int localX = (int)((mouseX - geometry.left) / geometry.pixelScale);
        int localY = (int)((mouseY - geometry.top) / geometry.pixelScale);

        if (localX >= 0
                && localY >= 0
                && localX < region.width()
                && localY < region.height()
                && mouseX >= geometry.left
                && mouseY >= geometry.top) {
            pixelAction.apply(localX, localY);
        }
    }

    private void updateHover(double mouseX, double mouseY) {
        CapeUvRegion region = regionSupplier.get();
        CanvasGeometry geometry = geometry(region);

        if (mouseX < geometry.left
                || mouseY < geometry.top
                || mouseX >= geometry.left + geometry.drawWidth
                || mouseY >= geometry.top + geometry.drawHeight) {
            hoveredX = -1;
            hoveredY = -1;
            return;
        }

        hoveredX = (int)((mouseX - geometry.left) / geometry.pixelScale);
        hoveredY = (int)((mouseY - geometry.top) / geometry.pixelScale);
    }

    private CanvasGeometry geometry(CapeUvRegion region) {
        int availableWidth = Math.max(1, getWidth() - INNER_MARGIN * 2);
        int availableHeight = Math.max(
                1,
                getHeight() - HEADER_HEIGHT - INNER_MARGIN
        );

        int pixelScale = Math.max(
                1,
                Math.min(
                        availableWidth / region.width(),
                        availableHeight / region.height()
                )
        );

        int drawWidth = region.width() * pixelScale;
        int drawHeight = region.height() * pixelScale;
        int left = getX() + (getWidth() - drawWidth) / 2;
        int top = getY()
                + HEADER_HEIGHT
                + (availableHeight - drawHeight) / 2;

        return new CanvasGeometry(
                left,
                top,
                pixelScale,
                drawWidth,
                drawHeight
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    private record CanvasGeometry(
            int left,
            int top,
            int pixelScale,
            int drawWidth,
            int drawHeight
    ) {
    }
}
