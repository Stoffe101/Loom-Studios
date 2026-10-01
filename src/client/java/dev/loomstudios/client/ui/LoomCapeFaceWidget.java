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
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Focused cape-face editor with a revision-cached GPU preview.
 *
 * <p>High-resolution projects used to submit thousands of GUI rectangles every
 * frame. This widget now uploads the semantic face once per project revision
 * and draws it as one nearest-filtered texture, with only grid/hover overlays
 * remaining as GUI primitives.</p>
 */
public final class LoomCapeFaceWidget extends AbstractWidget {
    @FunctionalInterface
    public interface PixelAction {
        void apply(int localX, int localY);
    }

    public interface StrokeLifecycle {
        void begin();

        void end();
    }

    private static final int HEADER_HEIGHT = 24;
    private static final int INNER_MARGIN = 10;

    private final Supplier<LoomProject> projectSupplier;
    private final LongSupplier revisionSupplier;
    private final Supplier<CapeUvRegion> regionSupplier;
    private final PixelAction pixelAction;
    private final StrokeLifecycle strokeLifecycle;

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

    public LoomCapeFaceWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            LongSupplier revisionSupplier,
            Supplier<CapeUvRegion> regionSupplier,
            PixelAction pixelAction,
            StrokeLifecycle strokeLifecycle
    ) {
        super(x, y, width, height, Component.literal("Cape face canvas"));
        this.projectSupplier = Objects.requireNonNull(projectSupplier);
        this.revisionSupplier = Objects.requireNonNull(revisionSupplier);
        this.regionSupplier = Objects.requireNonNull(regionSupplier);
        this.pixelAction = Objects.requireNonNull(pixelAction);
        this.strokeLifecycle = Objects.requireNonNull(strokeLifecycle);
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
                + CanvasResolution.fromCanvas(project.cape()).label();

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
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(coords),
                    getRight() - textWidth - 10,
                    getY() + 8,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        ensurePreviewTexture(
                project,
                region,
                regionWidth,
                regionHeight,
                revisionSupplier.getAsLong()
        );

        CanvasGeometry geometry = geometry(region, resolutionScale);

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
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!strokeActive) {
            strokeActive = true;
            strokeLifecycle.begin();
        }

        applyAt(event.x(), event.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        applyAt(event.x(), event.y());
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if (strokeActive) {
            strokeActive = false;
            strokeLifecycle.end();
        }
    }

    private void applyAt(double mouseX, double mouseY) {
        CapeUvRegion region = regionSupplier.get();
        int scale = CanvasResolution.fromCanvas(projectSupplier.get().cape()).scale();
        CanvasGeometry geometry = geometry(region, scale);

        if (mouseX < geometry.left
                || mouseY < geometry.top
                || mouseX >= geometry.left + geometry.drawWidth
                || mouseY >= geometry.top + geometry.drawHeight) {
            return;
        }

        int localX = (int)((mouseX - geometry.left) / geometry.pixelScale);
        int localY = (int)((mouseY - geometry.top) / geometry.pixelScale);

        if (localX >= 0
                && localY >= 0
                && localX < region.width(scale)
                && localY < region.height(scale)) {
            pixelAction.apply(localX, localY);
        }
    }

    private void updateHover(double mouseX, double mouseY) {
        CapeUvRegion region = regionSupplier.get();
        int scale = CanvasResolution.fromCanvas(projectSupplier.get().cape()).scale();
        CanvasGeometry geometry = geometry(region, scale);

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

    private CanvasGeometry geometry(CapeUvRegion region, int scale) {
        int availableWidth = Math.max(1, getWidth() - INNER_MARGIN * 2);
        int availableHeight = Math.max(
                1,
                getHeight() - HEADER_HEIGHT - INNER_MARGIN
        );

        int pixelScale = Math.max(
                1,
                Math.min(
                        availableWidth / region.width(scale),
                        availableHeight / region.height(scale)
                )
        );

        int drawWidth = region.width(scale) * pixelScale;
        int drawHeight = region.height(scale) * pixelScale;
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

    public void close() {
        if (strokeActive) {
            strokeActive = false;
            strokeLifecycle.end();
        }

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
            int drawHeight
    ) {
    }
}
