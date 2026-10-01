package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Focused cape-face editor with revision-cached GPU preview, crisp zoom,
 * middle-mouse panning, temporary brush-size preview and shape previews.
 */
public final class LoomCapeFaceWidget extends AbstractWidget {
    @FunctionalInterface
    public interface PixelAction {
        void apply(int localX, int localY);
    }

    @FunctionalInterface
    public interface ShapeAction {
        void apply(int startX, int startY, int endX, int endY);
    }

    public interface StrokeLifecycle {
        void begin();

        void end();
    }

    public enum GestureMode {
        BRUSH,
        CLICK,
        LINE,
        RECTANGLE
    }

    private static final int HEADER_HEIGHT = 24;
    private static final int INNER_MARGIN = 10;
    private static final long BRUSH_PREVIEW_NANOS = 1_400_000_000L;
    private static final float[] ZOOM_STEPS = {
            1.0F, 1.25F, 1.5F, 2.0F, 3.0F, 4.0F, 6.0F, 8.0F
    };

    private final Supplier<LoomProject> projectSupplier;
    private final LongSupplier revisionSupplier;
    private final Supplier<CapeUvRegion> regionSupplier;
    private final PixelAction pixelAction;
    private final ShapeAction shapeAction;
    private final StrokeLifecycle strokeLifecycle;
    private final IntSupplier brushSizeSupplier;
    private final Supplier<GestureMode> gestureModeSupplier;

    private final Identifier textureId = Identifier.fromNamespaceAndPath(
            LoomStudios.MOD_ID,
            "editor/cape_face_" + UUID.randomUUID().toString().replace("-", "")
    );

    private DynamicTexture previewTexture;
    private NativeImage previewImage;
    private long renderedRevision = Long.MIN_VALUE;
    private CapeUvRegion renderedRegion;
    private int renderedWidth = -1;
    private int renderedHeight = -1;

    private int hoveredX = -1;
    private int hoveredY = -1;

    private boolean strokeActive;
    private boolean shapeActive;
    private int shapeStartX;
    private int shapeStartY;
    private int shapeEndX;
    private int shapeEndY;

    private boolean panning;
    private int zoomIndex;
    private int panX;
    private int panY;

    private long brushPreviewUntilNanos;

    public LoomCapeFaceWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            LongSupplier revisionSupplier,
            Supplier<CapeUvRegion> regionSupplier,
            PixelAction pixelAction,
            ShapeAction shapeAction,
            StrokeLifecycle strokeLifecycle,
            IntSupplier brushSizeSupplier,
            Supplier<GestureMode> gestureModeSupplier
    ) {
        super(x, y, width, height, Component.literal("Cape face canvas"));
        this.projectSupplier = Objects.requireNonNull(projectSupplier);
        this.revisionSupplier = Objects.requireNonNull(revisionSupplier);
        this.regionSupplier = Objects.requireNonNull(regionSupplier);
        this.pixelAction = Objects.requireNonNull(pixelAction);
        this.shapeAction = Objects.requireNonNull(shapeAction);
        this.strokeLifecycle = Objects.requireNonNull(strokeLifecycle);
        this.brushSizeSupplier = Objects.requireNonNull(brushSizeSupplier);
        this.gestureModeSupplier = Objects.requireNonNull(gestureModeSupplier);
    }

    public int zoomPercent() {
        return Math.round(ZOOM_STEPS[zoomIndex] * 100.0F);
    }

    public boolean canZoomIn() {
        return zoomIndex + 1 < ZOOM_STEPS.length;
    }

    public boolean canZoomOut() {
        return zoomIndex > 0;
    }

    public void zoomIn() {
        setZoomIndex(Math.min(ZOOM_STEPS.length - 1, zoomIndex + 1));
    }

    public void zoomOut() {
        setZoomIndex(Math.max(0, zoomIndex - 1));
    }

    public void resetZoom() {
        zoomIndex = 0;
        panX = 0;
        panY = 0;
    }

    public void showBrushSizePreview() {
        brushPreviewUntilNanos = System.nanoTime() + BRUSH_PREVIEW_NANOS;
    }

    private void setZoomIndex(int next) {
        if (next == zoomIndex) {
            return;
        }

        zoomIndex = next;

        if (zoomIndex == 0) {
            panX = 0;
            panY = 0;
        } else {
            clampPan();
        }
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
        int resolutionScale = CanvasResolution.fromCanvas(project.cape()).scale();
        int regionWidth = region.width(resolutionScale);
        int regionHeight = region.height(resolutionScale);

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
                + regionWidth
                + "×"
                + regionHeight
                + "  •  "
                + CanvasResolution.fromCanvas(project.cape()).label()
                + "  •  "
                + zoomPercent()
                + "%";

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
                    + region.atlasX(hoveredX, resolutionScale)
                    + ", "
                    + region.atlasY(hoveredY, resolutionScale);

            int textWidth = Minecraft.getInstance().font.width(coords);
            int left = getRight() - textWidth - 10;

            if (left > getX() + 120) {
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.literal(coords),
                        left,
                        getY() + 8,
                        LoomUiTheme.TEXT_MUTED,
                        false
                );
            }
        }

        ensurePreviewTexture(
                project,
                region,
                regionWidth,
                regionHeight,
                revisionSupplier.getAsLong()
        );

        CanvasGeometry geometry = geometry(region, resolutionScale);

        graphics.enableScissor(
                geometry.clipLeft,
                geometry.clipTop,
                geometry.clipRight,
                geometry.clipBottom
        );

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                geometry.left,
                geometry.top,
                0.0F,
                0.0F,
                geometry.drawWidth,
                geometry.drawHeight,
                regionWidth,
                regionHeight,
                regionWidth,
                regionHeight
        );

        if (geometry.pixelScale >= 4) {
            renderGrid(graphics, geometry, regionWidth, regionHeight);
        }

        renderCursor(graphics, geometry);
        renderShapePreview(graphics, geometry);
        graphics.disableScissor();

        if (zoomIndex > 0) {
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal("Mouse wheel: zoom  •  Middle drag: pan"),
                    getX() + 10,
                    getBottom() - 12,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }
    }

    private void renderGrid(
            GuiGraphics graphics,
            CanvasGeometry geometry,
            int regionWidth,
            int regionHeight
    ) {
        int gridColor = 0x3A000000;

        for (int x = 0; x <= regionWidth; x++) {
            int px = geometry.left + x * geometry.pixelScale;
            graphics.fill(
                    px,
                    geometry.top,
                    px + 1,
                    geometry.top + geometry.drawHeight,
                    gridColor
            );
        }

        for (int y = 0; y <= regionHeight; y++) {
            int py = geometry.top + y * geometry.pixelScale;
            graphics.fill(
                    geometry.left,
                    py,
                    geometry.left + geometry.drawWidth,
                    py + 1,
                    gridColor
            );
        }
    }

    private void renderCursor(
            GuiGraphics graphics,
            CanvasGeometry geometry
    ) {
        if (hoveredX < 0 || hoveredY < 0) {
            return;
        }

        int px = geometry.left + hoveredX * geometry.pixelScale;
        int py = geometry.top + hoveredY * geometry.pixelScale;
        int s = geometry.pixelScale;

        boolean temporaryBrushPreview =
                gestureModeSupplier.get() == GestureMode.BRUSH
                        && System.nanoTime() < brushPreviewUntilNanos;

        if (!temporaryBrushPreview) {
            graphics.fill(px, py, px + s, py + 2, LoomUiTheme.ACCENT);
            graphics.fill(px, py + s - 2, px + s, py + s, LoomUiTheme.ACCENT);
            graphics.fill(px, py, px + 2, py + s, LoomUiTheme.ACCENT);
            graphics.fill(px + s - 2, py, px + s, py + s, LoomUiTheme.ACCENT);
            return;
        }

        int brushSize = Math.max(1, brushSizeSupplier.getAsInt());
        int centerX = px + s / 2;
        int centerY = py + s / 2;
        int radius = Math.max(
                Math.max(2, s / 2),
                Math.round(brushSize * s / 2.0F)
        );

        drawCircleOutline(
                graphics,
                centerX,
                centerY,
                radius,
                LoomUiTheme.ACCENT
        );

        graphics.fill(
                centerX - 1,
                centerY - 1,
                centerX + 2,
                centerY + 2,
                0xCCFFFFFF
        );
    }

    private void renderShapePreview(
            GuiGraphics graphics,
            CanvasGeometry geometry
    ) {
        if (!shapeActive) {
            return;
        }

        int startX = geometry.left
                + shapeStartX * geometry.pixelScale
                + geometry.pixelScale / 2;
        int startY = geometry.top
                + shapeStartY * geometry.pixelScale
                + geometry.pixelScale / 2;
        int endX = geometry.left
                + shapeEndX * geometry.pixelScale
                + geometry.pixelScale / 2;
        int endY = geometry.top
                + shapeEndY * geometry.pixelScale
                + geometry.pixelScale / 2;

        GestureMode mode = gestureModeSupplier.get();

        if (mode == GestureMode.LINE) {
            drawScreenLine(
                    graphics,
                    startX,
                    startY,
                    endX,
                    endY,
                    LoomUiTheme.ACCENT
            );
            return;
        }

        if (mode == GestureMode.RECTANGLE) {
            int left = Math.min(startX, endX);
            int right = Math.max(startX, endX);
            int top = Math.min(startY, endY);
            int bottom = Math.max(startY, endY);

            graphics.fill(left, top, right + 1, top + 2, LoomUiTheme.ACCENT);
            graphics.fill(left, bottom - 1, right + 1, bottom + 1, LoomUiTheme.ACCENT);
            graphics.fill(left, top, left + 2, bottom + 1, LoomUiTheme.ACCENT);
            graphics.fill(right - 1, top, right + 1, bottom + 1, LoomUiTheme.ACCENT);
        }
    }

    private static void drawScreenLine(
            GuiGraphics graphics,
            int x0,
            int y0,
            int x1,
            int y1,
            int color
    ) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0);
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;

        while (true) {
            graphics.fill(x0, y0, x0 + 2, y0 + 2, color);

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int doubled = 2 * error;
            if (doubled >= dy) {
                error += dy;
                x0 += sx;
            }
            if (doubled <= dx) {
                error += dx;
                y0 += sy;
            }
        }
    }

    private static void drawCircleOutline(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        int x = radius;
        int y = 0;
        int error = 1 - x;

        while (x >= y) {
            plotCirclePoints(graphics, centerX, centerY, x, y, color);
            y++;

            if (error < 0) {
                error += 2 * y + 1;
            } else {
                x--;
                error += 2 * (y - x) + 1;
            }
        }
    }

    private static void plotCirclePoints(
            GuiGraphics graphics,
            int cx,
            int cy,
            int x,
            int y,
            int color
    ) {
        plot(graphics, cx + x, cy + y, color);
        plot(graphics, cx + y, cy + x, color);
        plot(graphics, cx - y, cy + x, color);
        plot(graphics, cx - x, cy + y, color);
        plot(graphics, cx - x, cy - y, color);
        plot(graphics, cx - y, cy - x, color);
        plot(graphics, cx + y, cy - x, color);
        plot(graphics, cx + x, cy - y, color);
    }

    private static void plot(
            GuiGraphics graphics,
            int x,
            int y,
            int color
    ) {
        graphics.fill(x, y, x + 2, y + 2, color);
    }

    private void ensurePreviewTexture(
            LoomProject project,
            CapeUvRegion region,
            int regionWidth,
            int regionHeight,
            long revision
    ) {
        boolean dimensionsChanged = previewImage == null
                || previewImage.getWidth() != regionWidth
                || previewImage.getHeight() != regionHeight;

        if (dimensionsChanged) {
            releaseTexture();

            previewImage = new NativeImage(
                    NativeImage.Format.RGBA,
                    regionWidth,
                    regionHeight,
                    false
            );
            previewTexture = new DynamicTexture(
                    () -> "Loom Studios editor cape face",
                    previewImage
            );
            Minecraft.getInstance().getTextureManager().register(
                    textureId,
                    previewTexture
            );

            renderedRevision = Long.MIN_VALUE;
            renderedRegion = null;
            renderedWidth = regionWidth;
            renderedHeight = regionHeight;
        }

        if (renderedRevision == revision
                && renderedRegion == region
                && renderedWidth == regionWidth
                && renderedHeight == regionHeight) {
            return;
        }

        int[] pixels = LoomTextureCompiler.compileCapeRegion(
                project.cape(),
                region,
                0,
                false,
                false
        );

        for (int y = 0; y < regionHeight; y++) {
            for (int x = 0; x < regionWidth; x++) {
                int color = pixels[y * regionWidth + x];

                if (((color >>> 24) & 0xFF) == 0) {
                    color = ((x + y) & 1) == 0
                            ? 0xFF303A44
                            : 0xFF222A31;
                }

                previewImage.setPixel(x, y, color);
            }
        }

        previewTexture.upload();
        renderedRevision = revision;
        renderedRegion = region;
        renderedWidth = regionWidth;
        renderedHeight = regionHeight;
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (!isMouseOver(event.x(), event.y())) {
            return false;
        }

        if (event.button() == 2) {
            panning = true;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if (panning && event.button() == 2) {
            panX += (int)Math.round(dx);
            panY += (int)Math.round(dy);
            clampPan();
            return true;
        }

        if (shapeActive && event.button() == 0) {
            int[] pixel = localPixel(event.x(), event.y());
            if (pixel != null) {
                shapeEndX = pixel[0];
                shapeEndY = pixel[1];
            }
            return true;
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 2 && panning) {
            panning = false;
            return true;
        }

        if (event.button() == 0 && shapeActive) {
            shapeActive = false;
            shapeAction.apply(
                    shapeStartX,
                    shapeStartY,
                    shapeEndX,
                    shapeEndY
            );

            if (strokeActive) {
                strokeActive = false;
                strokeLifecycle.end();
            }
            return true;
        }

        return super.mouseReleased(event);
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

        if (scrollY > 0.0) {
            zoomIn();
        } else {
            zoomOut();
        }
        return true;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int[] pixel = localPixel(event.x(), event.y());
        if (pixel == null) {
            return;
        }

        GestureMode mode = gestureModeSupplier.get();

        if (!strokeActive) {
            strokeActive = true;
            strokeLifecycle.begin();
        }

        if (mode == GestureMode.LINE || mode == GestureMode.RECTANGLE) {
            shapeActive = true;
            shapeStartX = pixel[0];
            shapeStartY = pixel[1];
            shapeEndX = pixel[0];
            shapeEndY = pixel[1];
            return;
        }

        pixelAction.apply(pixel[0], pixel[1]);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (gestureModeSupplier.get() != GestureMode.BRUSH) {
            return;
        }

        int[] pixel = localPixel(event.x(), event.y());
        if (pixel != null) {
            pixelAction.apply(pixel[0], pixel[1]);
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if (strokeActive) {
            strokeActive = false;
            strokeLifecycle.end();
        }
    }

    private int[] localPixel(double mouseX, double mouseY) {
        CapeUvRegion region = regionSupplier.get();
        int scale = CanvasResolution.fromCanvas(projectSupplier.get().cape()).scale();
        CanvasGeometry geometry = geometry(region, scale);

        if (mouseX < geometry.clipLeft
                || mouseY < geometry.clipTop
                || mouseX >= geometry.clipRight
                || mouseY >= geometry.clipBottom
                || mouseX < geometry.left
                || mouseY < geometry.top
                || mouseX >= geometry.left + geometry.drawWidth
                || mouseY >= geometry.top + geometry.drawHeight) {
            return null;
        }

        int localX = (int)((mouseX - geometry.left) / geometry.pixelScale);
        int localY = (int)((mouseY - geometry.top) / geometry.pixelScale);

        if (localX < 0
                || localY < 0
                || localX >= region.width(scale)
                || localY >= region.height(scale)) {
            return null;
        }

        return new int[]{localX, localY};
    }

    private void updateHover(double mouseX, double mouseY) {
        int[] pixel = localPixel(mouseX, mouseY);

        if (pixel == null) {
            hoveredX = -1;
            hoveredY = -1;
            return;
        }

        hoveredX = pixel[0];
        hoveredY = pixel[1];
    }

    private CanvasGeometry geometry(CapeUvRegion region, int scale) {
        int clipLeft = getX() + INNER_MARGIN;
        int clipTop = getY() + HEADER_HEIGHT;
        int clipRight = getRight() - INNER_MARGIN;
        int clipBottom = getBottom() - INNER_MARGIN;

        int availableWidth = Math.max(1, clipRight - clipLeft);
        int availableHeight = Math.max(1, clipBottom - clipTop);

        int fitPixelScale = Math.max(
                1,
                Math.min(
                        availableWidth / region.width(scale),
                        availableHeight / region.height(scale)
                )
        );

        int pixelScale = Math.max(
                1,
                Math.round(fitPixelScale * ZOOM_STEPS[zoomIndex])
        );

        int drawWidth = region.width(scale) * pixelScale;
        int drawHeight = region.height(scale) * pixelScale;

        int maxPanX = Math.max(0, (drawWidth - availableWidth) / 2);
        int maxPanY = Math.max(0, (drawHeight - availableHeight) / 2);

        panX = Math.max(-maxPanX, Math.min(maxPanX, panX));
        panY = Math.max(-maxPanY, Math.min(maxPanY, panY));

        int left = clipLeft
                + (availableWidth - drawWidth) / 2
                + panX;
        int top = clipTop
                + (availableHeight - drawHeight) / 2
                + panY;

        return new CanvasGeometry(
                left,
                top,
                pixelScale,
                drawWidth,
                drawHeight,
                clipLeft,
                clipTop,
                clipRight,
                clipBottom
        );
    }

    private void clampPan() {
        LoomProject project = projectSupplier.get();
        CapeUvRegion region = regionSupplier.get();
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        geometry(region, scale);
    }

    public void close() {
        if (strokeActive) {
            strokeActive = false;
            strokeLifecycle.end();
        }

        shapeActive = false;
        panning = false;
        releaseTexture();
    }

    private void releaseTexture() {
        if (previewTexture != null) {
            Minecraft.getInstance().getTextureManager().release(textureId);
            previewTexture = null;
            previewImage = null;
        }
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
            int drawHeight,
            int clipLeft,
            int clipTop,
            int clipRight,
            int clipBottom
    ) {
    }
}
