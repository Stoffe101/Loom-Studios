package dev.loomstudios.image;

import java.util.Objects;

public final class ImageEdges {
    private static final int ALPHA_THRESHOLD = 48;
    private static final int LUMINANCE_THRESHOLD = 40;

    private ImageEdges() {
    }

    public static PixelImage outline(PixelImage source) {
        Objects.requireNonNull(source, "source");

        int[] output = new int[source.width() * source.height()];

        for (int y = 0; y < source.height(); y++) {
            for (int x = 0; x < source.width(); x++) {
                int center = source.pixelAt(x, y);
                int alpha = (center >>> 24) & 0xFF;

                if (alpha == 0) {
                    continue;
                }

                int luminance = luminance(center);
                boolean edge = x == 0
                        || y == 0
                        || x + 1 == source.width()
                        || y + 1 == source.height();

                for (int dy = -1; dy <= 1 && !edge; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }

                        int neighbor = source.pixelAt(x + dx, y + dy);
                        int neighborAlpha = (neighbor >>> 24) & 0xFF;

                        if (Math.abs(alpha - neighborAlpha)
                                >= ALPHA_THRESHOLD
                                || Math.abs(
                                        luminance - luminance(neighbor)
                                ) >= LUMINANCE_THRESHOLD) {
                            edge = true;
                            break;
                        }
                    }
                }

                output[y * source.width() + x] = edge ? center : 0;
            }
        }

        return new PixelImage(
                source.width(),
                source.height(),
                output
        );
    }

    private static int luminance(int argb) {
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        return (int)Math.round(
                red * 0.2126
                        + green * 0.7152
                        + blue * 0.0722
        );
    }
}
