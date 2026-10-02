package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.image.ImageTransforms;
import dev.loomstudios.image.PixelImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class LoomImagePreviewWidget extends AbstractWidget {
    private static final int HEADER_HEIGHT = 20;
    private static final int MAX_PREVIEW_DIMENSION = 512;

    private final Component title;
    private final Supplier<PixelImage> imageSupplier;
    private final LongSupplier revisionSupplier;
    private final Identifier textureId;

    private DynamicTexture texture;
    private NativeImage nativeImage;
    private long renderedRevision = Long.MIN_VALUE;
    private int sourceWidth;
    private int sourceHeight;

    public LoomImagePreviewWidget(
            int x,
            int y,
            int width,
            int height,
            Component title,
            Supplier<PixelImage> imageSupplier,
            LongSupplier revisionSupplier
    ) {
        super(x, y, width, height, title);
        this.title = Objects.requireNonNull(title, "title");
        this.imageSupplier = Objects.requireNonNull(
                imageSupplier,
                "imageSupplier"
        );
        this.revisionSupplier = Objects.requireNonNull(
                revisionSupplier,
                "revisionSupplier"
        );
        this.textureId = Identifier.fromNamespaceAndPath(
                LoomStudios.MOD_ID,
                "editor/import_preview_"
                        + UUID.randomUUID().toString().replace("-", "")
        );
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
                LoomUiTheme.PANEL_INNER
        );

        PixelImage current = imageSupplier.get();
        String dimensions = current == null
                ? "No image"
                : current.width() + "×" + current.height();

        graphics.drawString(
                Minecraft.getInstance().font,
                title,
                getX() + 6,
                getY() + 6,
                LoomUiTheme.TEXT,
                false
        );

        int dimWidth = Minecraft.getInstance().font.width(dimensions);
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(dimensions),
                getRight() - dimWidth - 6,
                getY() + 6,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        int contentLeft = getX() + 6;
        int contentTop = getY() + HEADER_HEIGHT + 4;
        int contentRight = getRight() - 6;
        int contentBottom = getBottom() - 6;

        renderChecker(
                graphics,
                contentLeft,
                contentTop,
                contentRight,
                contentBottom
        );

        if (current == null) {
            return;
        }

        ensureTexture(current, revisionSupplier.getAsLong());

        int availableWidth = Math.max(1, contentRight - contentLeft);
        int availableHeight = Math.max(1, contentBottom - contentTop);
        double scale = Math.min(
                availableWidth / (double) sourceWidth,
                availableHeight / (double) sourceHeight
        );

        int drawWidth = Math.max(
                1,
                (int)Math.floor(sourceWidth * scale)
        );
        int drawHeight = Math.max(
                1,
                (int)Math.floor(sourceHeight * scale)
        );
        int left = contentLeft + (availableWidth - drawWidth) / 2;
        int top = contentTop + (availableHeight - drawHeight) / 2;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                left,
                top,
                0.0F,
                0.0F,
                drawWidth,
                drawHeight,
                sourceWidth,
                sourceHeight,
                sourceWidth,
                sourceHeight
        );
    }

    private void ensureTexture(PixelImage source, long revision) {
        if (texture != null && renderedRevision == revision) {
            return;
        }

        PixelImage preview = fitPreview(source);

        if (texture != null) {
            Minecraft.getInstance()
                    .getTextureManager()
                    .release(textureId);
            texture = null;
            nativeImage = null;
        }

        sourceWidth = preview.width();
        sourceHeight = preview.height();
        nativeImage = new NativeImage(
                NativeImage.Format.RGBA,
                sourceWidth,
                sourceHeight,
                false
        );

        int[] pixels = preview.pixels();
        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                nativeImage.setPixel(
                        x,
                        y,
                        pixels[y * sourceWidth + x]
                );
            }
        }

        texture = new DynamicTexture(
                () -> "Loom Studios Smart Import preview",
                nativeImage
        );
        Minecraft.getInstance()
                .getTextureManager()
                .register(textureId, texture);
        texture.upload();
        renderedRevision = revision;
    }

    private static PixelImage fitPreview(PixelImage source) {
        if (source.width() <= MAX_PREVIEW_DIMENSION
                && source.height() <= MAX_PREVIEW_DIMENSION) {
            return source;
        }

        double scale = Math.min(
                MAX_PREVIEW_DIMENSION / (double)source.width(),
                MAX_PREVIEW_DIMENSION / (double)source.height()
        );

        return ImageTransforms.resizeNearest(
                source,
                Math.max(
                        1,
                        (int)Math.round(source.width() * scale)
                ),
                Math.max(
                        1,
                        (int)Math.round(source.height() * scale)
                )
        );
    }

    private static void renderChecker(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        int size = 8;
        for (int y = top; y < bottom; y += size) {
            for (int x = left; x < right; x += size) {
                boolean alternate =
                        (((x - left) / size) + ((y - top) / size)) % 2 != 0;
                graphics.fill(
                        x,
                        y,
                        Math.min(right, x + size),
                        Math.min(bottom, y + size),
                        alternate ? 0xFF3B4148 : 0xFF262C32
                );
            }
        }
    }

    public void close() {
        if (texture != null) {
            Minecraft.getInstance()
                    .getTextureManager()
                    .release(textureId);
            texture = null;
            nativeImage = null;
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
