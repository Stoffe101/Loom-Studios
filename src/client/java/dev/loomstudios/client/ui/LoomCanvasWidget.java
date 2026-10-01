package dev.loomstudios.client.ui;

import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Supplier;

public final class LoomCanvasWidget extends AbstractWidget {
    @FunctionalInterface
    public interface PixelAction {
        void apply(int x, int y);
    }

    private final Supplier<LoomProject> projectSupplier;
    private final PixelAction pixelAction;

    public LoomCanvasWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            PixelAction pixelAction
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.literal("Cape canvas")
        );
        this.projectSupplier = Objects.requireNonNull(
                projectSupplier,
                "projectSupplier"
        );
        this.pixelAction = Objects.requireNonNull(pixelAction, "pixelAction");
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        LoomProject project = projectSupplier.get();
        int[] pixels = LoomTextureCompiler.compile(
                project.cape(),
                0,
                false,
                false
        );

        int pixelScale = Math.max(
                1,
                Math.min(
                        getWidth() / project.cape().width(),
                        getHeight() / project.cape().height()
                )
        );

        int drawWidth = project.cape().width() * pixelScale;
        int drawHeight = project.cape().height() * pixelScale;
        int left = getX() + (getWidth() - drawWidth) / 2;
        int top = getY() + (getHeight() - drawHeight) / 2;

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

        for (int y = 0; y < project.cape().height(); y++) {
            for (int x = 0; x < project.cape().width(); x++) {
                int px = left + x * pixelScale;
                int py = top + y * pixelScale;
                int checker = ((x + y) & 1) == 0
                        ? 0xFF273039
                        : 0xFF1B2229;

                graphics.fill(
                        px,
                        py,
                        px + pixelScale,
                        py + pixelScale,
                        checker
                );

                int color = pixels[y * project.cape().width() + x];
                if (((color >>> 24) & 0xFF) != 0) {
                    graphics.fill(
                            px,
                            py,
                            px + pixelScale,
                            py + pixelScale,
                            color
                    );
                }

                if (pixelScale >= 5) {
                    graphics.fill(
                            px,
                            py,
                            px + pixelScale,
                            py + 1,
                            0x26000000
                    );
                    graphics.fill(
                            px,
                            py,
                            px + 1,
                            py + pixelScale,
                            0x26000000
                    );
                }
            }
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
        LoomProject project = projectSupplier.get();

        int pixelScale = Math.max(
                1,
                Math.min(
                        getWidth() / project.cape().width(),
                        getHeight() / project.cape().height()
                )
        );

        int drawWidth = project.cape().width() * pixelScale;
        int drawHeight = project.cape().height() * pixelScale;
        int left = getX() + (getWidth() - drawWidth) / 2;
        int top = getY() + (getHeight() - drawHeight) / 2;

        int pixelX = (int)((mouseX - left) / pixelScale);
        int pixelY = (int)((mouseY - top) / pixelScale);

        if (pixelX >= 0
                && pixelY >= 0
                && pixelX < project.cape().width()
                && pixelY < project.cape().height()) {
            pixelAction.apply(pixelX, pixelY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
