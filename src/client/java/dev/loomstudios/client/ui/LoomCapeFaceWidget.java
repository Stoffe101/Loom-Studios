package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.PixelSelection;
import dev.loomstudios.ui.CanvasViewportTransform;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
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

/**
 * Focused cape-face editor with revision-cached GPU preview, crisp zoom, middle-mouse panning,
 * temporary brush-size preview and shape previews.
 */
public final class LoomCapeFaceWidget extends AbstractWidget implements LoomMiddlePanTarget {
    @FunctionalInterface
    public interface PixelAction {
        void apply(int localX, int localY);
    }

    @FunctionalInterface
    public interface ShapeAction {
        void apply(int startX, int startY, int endX, int endY);
    }

    @FunctionalInterface
    public interface SelectionAction {
        void apply(PixelSelection selection);
    }

    public interface StrokeLifecycle {
        void begin();

        void end();
    }

    public enum GestureMode {
        BRUSH,
        CLICK,
        LINE,
        RECTANGLE,
        CIRCLE,
        SELECTION
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
    private final Supplier<PixelSelection> selectionSupplier;
    private final SelectionAction selectionAction;
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

    private boolean gridVisible = dev.loomstudios.client.project.LoomPreferences.get().enabled("grid",true);
    public void toggleGrid() { gridVisible = !gridVisible; }
    public boolean gridVisible() { return gridVisible; }

    private java.util.function.BooleanSupplier shapeFilledSupplier = () -> false;
    public void setShapeFilledSupplier(java.util.function.BooleanSupplier supplier) { shapeFilledSupplier = supplier; }

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
            Supplier<PixelSelection> selectionSupplier,
            SelectionAction selectionAction,
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
        this.selectionSupplier = Objects.requireNonNull(selectionSupplier);
        this.selectionAction = Objects.requireNonNull(selectionAction);
        this.strokeLifecycle = Objects.requireNonNull(strokeLifecycle);
        this.brushSizeSupplier = Objects.requireNonNull(brushSizeSupplier);
        this.gestureModeSupplier = Objects.requireNonNull(gestureModeSupplier);
    }

    public record ViewState(int zoomIndex, int panX, int panY, boolean gridVisible) { }
    public ViewState viewState() { return new ViewState(zoomIndex, panX, panY, gridVisible); }
    public void restoreViewState(ViewState state) { if (state != null) { zoomIndex = state.zoomIndex; panX = state.panX; panY = state.panY; gridVisible = state.gridVisible; } }

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

        String header = regionWidth + "×" + regionHeight + " px";

        dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
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
                dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
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

        CanvasViewportTransform geometry = geometry(region, resolutionScale);
        LoomScreenChrome.workSurface(graphics, geometry.clipLeft(), geometry.clipTop(), geometry.clipRight(), geometry.clipBottom());

        graphics.enableScissor(
                geometry.clipLeft(),
                geometry.clipTop(),
                geometry.clipRight(),
                geometry.clipBottom()
        );

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                geometry.left(),
                geometry.top(),
                0.0F,
                0.0F,
                geometry.drawWidth(),
                geometry.drawHeight(),
                regionWidth,
                regionHeight,
                regionWidth,
                regionHeight
        );

        if (geometry.pixelScale() >= 4 && gridVisible) {
            renderGrid(graphics, geometry, regionWidth, regionHeight);
        }

        renderCursor(graphics, geometry);
        renderSelectionOutline(graphics, geometry);
        renderShapePreview(graphics, geometry);
        graphics.disableScissor();

        if (zoomIndex > 0) {
            dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
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
            CanvasViewportTransform geometry,
            int regionWidth,
            int regionHeight
    ) {
        int gridColor = 0x3A000000;

        for (int x = 0; x <= regionWidth; x++) {
            int px = geometry.left() + x * geometry.pixelScale();
            graphics.fill(
                    px,
                    geometry.top(),
                    px + 1,
                    geometry.top() + geometry.drawHeight(),
                    gridColor
            );
        }

        for (int y = 0; y <= regionHeight; y++) {
            int py = geometry.top() + y * geometry.pixelScale();
            graphics.fill(
                    geometry.left(),
                    py,
                    geometry.left() + geometry.drawWidth(),
                    py + 1,
                    gridColor
            );
        }
    }

    private void renderCursor(
            GuiGraphics graphics,
            CanvasViewportTransform geometry
    ) {
        if (gestureModeSupplier.get() == GestureMode.SELECTION) return;
        boolean temporaryBrushPreview =
                gestureModeSupplier.get() == GestureMode.BRUSH
                        && System.nanoTime() < brushPreviewUntilNanos;

        if (!temporaryBrushPreview && (hoveredX < 0 || hoveredY < 0)) {
            return;
        }

        int cursorX = hoveredX;
        int cursorY = hoveredY;

        if (temporaryBrushPreview && (cursorX < 0 || cursorY < 0)) {
            CapeUvRegion region = regionSupplier.get();
            int scale = CanvasResolution.fromCanvas(
                    projectSupplier.get().cape()
            ).scale();

            cursorX = Math.max(0, region.width(scale) / 2);
            cursorY = Math.max(0, region.height(scale) / 2);
        }

        int px = geometry.left() + cursorX * geometry.pixelScale();
        int py = geometry.top() + cursorY * geometry.pixelScale();
        int s = geometry.pixelScale();

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

    private void renderShapePreview(GuiGraphics graphics, CanvasViewportTransform geometry) {
        if (!shapeActive) return;
        GestureMode mode = gestureModeSupplier.get();
        if (mode == GestureMode.LINE) {
            drawScreenLine(graphics,
                    geometry.screenX(shapeStartX) + geometry.pixelScale() / 2,
                    geometry.screenY(shapeStartY) + geometry.pixelScale() / 2,
                    geometry.screenX(shapeEndX) + geometry.pixelScale() / 2,
                    geometry.screenY(shapeEndY) + geometry.pixelScale() / 2, LoomUiTheme.ACCENT);
        } else if (mode == GestureMode.CIRCLE) {
            dev.loomstudios.project.PixelShapes.ellipse(shapeStartX,shapeStartY,shapeEndX,shapeEndY,shapeFilledSupplier.getAsBoolean(),
                    (x,y) -> graphics.fill(geometry.screenX(x),geometry.screenY(y),
                            geometry.screenX(x+1),geometry.screenY(y+1),0xAA58D8ED));
        } else {
            drawPixelOutline(graphics, geometry.selection(PixelSelection.between(
                    shapeStartX, shapeStartY, shapeEndX, shapeEndY)), LoomUiTheme.ACCENT);
        }
    }

    private static void drawPixelOutline(GuiGraphics g, CanvasViewportTransform.Rect r, int color) {
        // Boundary-aligned outline, with contrast outside. No fictitious resize handle.
        g.fill(r.left() - 1, r.top() - 1, r.right() + 1, r.top(), 0xFF07121C);
        g.fill(r.left() - 1, r.bottom(), r.right() + 1, r.bottom() + 1, 0xFF07121C);
        g.fill(r.left() - 1, r.top(), r.left(), r.bottom(), 0xFF07121C);
        g.fill(r.right(), r.top(), r.right() + 1, r.bottom(), 0xFF07121C);
        g.fill(r.left(), r.top(), r.right(), r.top() + 1, color);
        g.fill(r.left(), r.bottom() - 1, r.right(), r.bottom(), color);
        g.fill(r.left(), r.top(), r.left() + 1, r.bottom(), color);
        g.fill(r.right() - 1, r.top(), r.right(), r.bottom(), color);
    }


    private void renderSelectionOutline(GuiGraphics graphics, CanvasViewportTransform geometry) {
        PixelSelection selection = selectionSupplier.get();
        if (selection != null && !shapeActive) drawPixelOutline(graphics, geometry.selection(selection), LoomUiTheme.ACCENT);
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
    revision = 31 * revision + dev.loomstudios.client.project.EditorOverlayState.revision();
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

        int[]
        atlas =
            dev.loomstudios.client.project.EditorOverlayState.composite(
                project, dev.loomstudios.project.AnimationChannel.CAPE), pixels = new int[regionWidth * regionHeight];
    int scale =
                project.cape().width() / 64;
    for (int y = 0; y < regionHeight; y++)
      for (int x = 0; x < regionWidth; x++)
        pixels[y * regionWidth + x] =
            atlas[region.atlasY(y, scale) * project.cape().width() +
                region.atlasX(x, scale
        )];

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

            if (gestureModeSupplier.get() == GestureMode.SELECTION) {
                selectionAction.apply(
                        PixelSelection.between(
                                shapeStartX,
                                shapeStartY,
                                shapeEndX,
                                shapeEndY
                        )
                );
            } else {
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

        if (mode == GestureMode.SELECTION) {
            shapeActive = true;
            shapeStartX = pixel[0];
            shapeStartY = pixel[1];
            shapeEndX = pixel[0];
            shapeEndY = pixel[1];
            return;
        }

        if (!strokeActive) {
            strokeActive = true;
            strokeLifecycle.begin();
        }

        if (mode == GestureMode.LINE || (mode == GestureMode.RECTANGLE || mode == GestureMode.CIRCLE)) {
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
        return geometry(region, scale).pixelAt(mouseX, mouseY);
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

    private CanvasViewportTransform geometry(CapeUvRegion region, int scale) {
        return CanvasViewportTransform.fit(getX() + INNER_MARGIN, getY() + HEADER_HEIGHT,
                getRight() - INNER_MARGIN, getBottom() - INNER_MARGIN,
                region.width(scale), region.height(scale), ZOOM_STEPS[zoomIndex], panX, panY);
    }


    private void clampPan() {
        LoomProject project = projectSupplier.get();
        CapeUvRegion region = regionSupplier.get();
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();
        var t = geometry(region, scale);
        panX = t.left() - (t.clipLeft() + (t.clipRight() - t.clipLeft() - t.drawWidth()) / 2);
        panY = t.top() - (t.clipTop() + (t.clipBottom() - t.clipTop() - t.drawHeight()) / 2);
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

}
