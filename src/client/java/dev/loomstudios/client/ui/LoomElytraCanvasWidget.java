package dev.loomstudios.client.ui;

import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.ElytraWing;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Unfolded semantic Elytra wing editor surface.
 */
public final class LoomElytraCanvasWidget extends AbstractWidget {
    @FunctionalInterface
    public interface PixelAction {
        void apply(ElytraWing wing, int x, int y);
    }

    public interface StrokeLifecycle {
        void begin();
        void end();
    }

    private final Supplier<LoomProject> projectSupplier;
    private final LongSupplier revisionSupplier;
    private final PixelAction pixelAction;
    private final StrokeLifecycle strokeLifecycle;

    private long renderedRevision = Long.MIN_VALUE;
    private int[] compiledPixels;
    private boolean strokeActive;

    public LoomElytraCanvasWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            LongSupplier revisionSupplier,
            PixelAction pixelAction,
            StrokeLifecycle strokeLifecycle
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.literal("Elytra Canvas")
        );
        this.projectSupplier = Objects.requireNonNull(
                projectSupplier,
                "projectSupplier"
        );
        this.revisionSupplier = Objects.requireNonNull(
                revisionSupplier,
                "revisionSupplier"
        );
        this.pixelAction = Objects.requireNonNull(pixelAction, "pixelAction");
        this.strokeLifecycle = Objects.requireNonNull(
                strokeLifecycle,
                "strokeLifecycle"
        );
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        LoomProject project = projectSupplier.get();
        ensureCompiled(project);

        graphics.fill(getX(), getY(), getRight(), getBottom(), LoomUiTheme.BORDER);
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                LoomUiTheme.PANEL_INNER
        );

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Elytra Canvas"),
                getX() + 8,
                getY() + 7,
                LoomUiTheme.TEXT,
                false
        );

        int scale = CanvasResolution.fromCanvas(project.elytra()).scale();
        WingGeometry geometry = geometry(scale);

        renderWing(graphics, project, ElytraWing.LEFT, geometry.leftX, geometry.top, geometry.pixelScale);
        renderWing(graphics, project, ElytraWing.RIGHT, geometry.rightX, geometry.top, geometry.pixelScale);

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.literal("Left"),
                geometry.leftX + ElytraWing.LEFT.width(scale) * geometry.pixelScale / 2,
                geometry.top - 12,
                LoomUiTheme.TEXT_MUTED
        );
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.literal("Right"),
                geometry.rightX + ElytraWing.RIGHT.width(scale) * geometry.pixelScale / 2,
                geometry.top - 12,
                LoomUiTheme.TEXT_MUTED
        );

        int[] hover = localPixel(mouseX, mouseY, scale, geometry);
        if (hover != null) {
            ElytraWing wing = hover[0] == 0 ? ElytraWing.LEFT : ElytraWing.RIGHT;
            int originX = wing == ElytraWing.LEFT
                    ? geometry.leftX
                    : geometry.rightX;
            int px = originX + hover[1] * geometry.pixelScale;
            int py = geometry.top + hover[2] * geometry.pixelScale;
            graphics.fill(px, py, px + geometry.pixelScale, py + 1, LoomUiTheme.ACCENT);
            graphics.fill(px, py + geometry.pixelScale - 1, px + geometry.pixelScale, py + geometry.pixelScale, LoomUiTheme.ACCENT);
            graphics.fill(px, py, px + 1, py + geometry.pixelScale, LoomUiTheme.ACCENT);
            graphics.fill(px + geometry.pixelScale - 1, py, px + geometry.pixelScale, py + geometry.pixelScale, LoomUiTheme.ACCENT);
        }
    }

    private void ensureCompiled(LoomProject project) {
        long revision = revisionSupplier.getAsLong();
        if (compiledPixels != null && renderedRevision == revision) {
            return;
        }

        compiledPixels = LoomTextureCompiler.compile(
                project.elytra(),
                0,
                false,
                false
        );
        renderedRevision = revision;
    }

    private void renderWing(
            GuiGraphics graphics,
            LoomProject project,
            ElytraWing wing,
            int originX,
            int originY,
            int pixelScale
    ) {
        int scale = CanvasResolution.fromCanvas(project.elytra()).scale();
        int width = wing.width(scale);
        int height = wing.height(scale);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int px = originX + x * pixelScale;
                int py = originY + y * pixelScale;
                int checker = ((x + y) & 1) == 0
                        ? 0xFF303943
                        : 0xFF212831;
                graphics.fill(px, py, px + pixelScale, py + pixelScale, checker);

                int atlasX = wing.atlasX(x, scale);
                int atlasY = wing.atlasY(y, scale);
                int color = compiledPixels[
                        atlasY * project.elytra().width() + atlasX
                ];

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
                    graphics.fill(px, py, px + pixelScale, py + 1, 0x26000000);
                    graphics.fill(px, py, px + 1, py + pixelScale, 0x26000000);
                }
            }
        }
    }

    private WingGeometry geometry(int scale) {
        int header = 34;
        int bottomPadding = 8;
        int availableWidth = Math.max(1, getWidth() - 20);
        int availableHeight = Math.max(
                1,
                getHeight() - header - bottomPadding
        );
        int wingWidth = ElytraWing.LEFT.width(scale);
        int wingHeight = ElytraWing.LEFT.height(scale);
        int gapPixels = Math.max(10, availableWidth / 18);

        int fitByWidth = Math.max(
                1,
                (availableWidth - gapPixels) / Math.max(1, wingWidth * 2)
        );
        int fitByHeight = Math.max(
                1,
                availableHeight / Math.max(1, wingHeight)
        );
        int pixelScale = Math.max(1, Math.min(fitByWidth, fitByHeight));

        int drawWingWidth = wingWidth * pixelScale;
        int drawWingHeight = wingHeight * pixelScale;
        int gap = Math.max(8, Math.min(gapPixels, availableWidth - drawWingWidth * 2));
        int totalWidth = drawWingWidth * 2 + gap;

        int leftX = getX() + (getWidth() - totalWidth) / 2;
        int rightX = leftX + drawWingWidth + gap;
        int top = getY() + header
                + Math.max(0, (availableHeight - drawWingHeight) / 2);

        return new WingGeometry(leftX, rightX, top, pixelScale);
    }

    private int[] localPixel(
            double mouseX,
            double mouseY,
            int scale,
            WingGeometry geometry
    ) {
        int wingWidth = ElytraWing.LEFT.width(scale);
        int wingHeight = ElytraWing.LEFT.height(scale);
        int drawWidth = wingWidth * geometry.pixelScale;
        int drawHeight = wingHeight * geometry.pixelScale;

        if (mouseY < geometry.top || mouseY >= geometry.top + drawHeight) {
            return null;
        }

        ElytraWing wing;
        int originX;
        int wingIndex;

        if (mouseX >= geometry.leftX
                && mouseX < geometry.leftX + drawWidth) {
            wing = ElytraWing.LEFT;
            originX = geometry.leftX;
            wingIndex = 0;
        } else if (mouseX >= geometry.rightX
                && mouseX < geometry.rightX + drawWidth) {
            wing = ElytraWing.RIGHT;
            originX = geometry.rightX;
            wingIndex = 1;
        } else {
            return null;
        }

        int x = (int)((mouseX - originX) / geometry.pixelScale);
        int y = (int)((mouseY - geometry.top) / geometry.pixelScale);

        if (x < 0 || y < 0
                || x >= wing.width(scale)
                || y >= wing.height(scale)) {
            return null;
        }

        return new int[]{wingIndex, x, y};
    }

    private void applyAt(double mouseX, double mouseY) {
        LoomProject project = projectSupplier.get();
        int scale = CanvasResolution.fromCanvas(project.elytra()).scale();
        WingGeometry geometry = geometry(scale);
        int[] pixel = localPixel(mouseX, mouseY, scale, geometry);

        if (pixel == null) {
            return;
        }

        ElytraWing wing = pixel[0] == 0
                ? ElytraWing.LEFT
                : ElytraWing.RIGHT;
        pixelAction.apply(wing, pixel[1], pixel[2]);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!strokeActive) {
            strokeActive = true;
            strokeLifecycle.begin();
        }
        applyAt(event.x(), event.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (event.button() == 0) {
            applyAt(event.x(), event.y());
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if (strokeActive) {
            strokeActive = false;
            strokeLifecycle.end();
        }
    }

    public void close() {
        if (strokeActive) {
            strokeActive = false;
            strokeLifecycle.end();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    private record WingGeometry(
            int leftX,
            int rightX,
            int top,
            int pixelScale
    ) {
    }
}
