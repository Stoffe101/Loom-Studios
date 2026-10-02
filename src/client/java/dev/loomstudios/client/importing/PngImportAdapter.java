package dev.loomstudios.client.importing;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.image.ImageTransforms;
import dev.loomstudios.image.PixelImage;
import dev.loomstudios.project.LoomProjectCodec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class PngImportAdapter {
    public static final long MAX_COMPRESSED_BYTES = 32L * 1024L * 1024L;

    private PngImportAdapter() {
    }

    public static LoadedImage load(Path path) throws IOException {
        Objects.requireNonNull(path, "path");

        if (!Files.isRegularFile(path)) {
            throw new IOException("Selected import path is not a file");
        }

        long byteSize = Files.size(path);
        if (byteSize <= 0 || byteSize > MAX_COMPRESSED_BYTES) {
            throw new IOException(
                    "PNG file size is outside the supported import range"
            );
        }

        // Extension is only a UX hint. NativeImage still has to parse the
        // actual content successfully.
        String fileName = path.getFileName().toString();
        String normalizedName = fileName.toLowerCase(Locale.ROOT);

        if (!normalizedName.endsWith(".png")) {
            throw new IOException("Smart Import currently supports PNG files");
        }

        try (InputStream stream = Files.newInputStream(path);
             NativeImage nativeImage = NativeImage.read(stream)) {
            int width = nativeImage.getWidth();
            int height = nativeImage.getHeight();

            if (width <= 0
                    || height <= 0
                    || width > PixelImage.MAX_DIMENSION
                    || height > PixelImage.MAX_DIMENSION
                    || (long) width * height > PixelImage.MAX_PIXELS) {
                throw new IOException(
                        "PNG dimensions exceed Smart Import safety limits"
                );
            }

            int[] pixels = new int[Math.multiplyExact(width, height)];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] =
                            nativeImage.getPixel(x, y);
                }
            }

            PixelImage original = new PixelImage(
                    width,
                    height,
                    pixels
            );

            PixelImage embedded = shrinkForProject(original);

            return new LoadedImage(
                    path.toAbsolutePath().normalize(),
                    original,
                    embedded
            );
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid or unsupported PNG", e);
        }
    }

    private static PixelImage shrinkForProject(PixelImage source) {
        int limit = LoomProjectCodec.MAX_EMBEDDED_IMAGE_DIMENSION;
        if (source.width() <= limit && source.height() <= limit) {
            return source;
        }

        double scale = Math.min(
                limit / (double) source.width(),
                limit / (double) source.height()
        );

        int width = Math.max(
                1,
                (int) Math.round(source.width() * scale)
        );
        int height = Math.max(
                1,
                (int) Math.round(source.height() * scale)
        );

        return ImageTransforms.resizeNearest(
                source,
                width,
                height
        );
    }

    public record LoadedImage(
            Path sourcePath,
            PixelImage original,
            PixelImage embedded
    ) {
        public LoadedImage {
            Objects.requireNonNull(sourcePath, "sourcePath");
            Objects.requireNonNull(original, "original");
            Objects.requireNonNull(embedded, "embedded");
        }
    }
}
