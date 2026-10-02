package dev.loomstudios.image;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable ARGB image used by Smart Import processing before project-layer
 * integration.
 */
public record PixelImage(
        int width,
        int height,
        int[] pixels
) {
    public static final int MAX_DIMENSION = 4096;
    public static final int MAX_PIXELS = 16 * 1024 * 1024;

    public PixelImage {
        int expected = checkedPixelCount(width, height);
        Objects.requireNonNull(pixels, "pixels");

        if (pixels.length != expected) {
            throw new IllegalArgumentException(
                    "Pixel count does not match image dimensions"
            );
        }

        pixels = pixels.clone();
    }

    static int checkedPixelCount(int width, int height) {
        if (width <= 0
                || height <= 0
                || width > MAX_DIMENSION
                || height > MAX_DIMENSION) {
            throw new IllegalArgumentException("Image dimensions out of range");
        }

        long count = (long)width * height;
        if (count > MAX_PIXELS) {
            throw new IllegalArgumentException("Image pixel count out of range");
        }

        return (int)count;
    }

    @Override
    public int[] pixels() {
        return pixels.clone();
    }

    public int pixelAt(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IndexOutOfBoundsException(
                    "Pixel outside image: " + x + "," + y
            );
        }

        return pixels[y * width + x];
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PixelImage that)) {
            return false;
        }

        return width == that.width
                && height == that.height
                && Arrays.equals(pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(width, height);
        return 31 * result + Arrays.hashCode(pixels);
    }
}
