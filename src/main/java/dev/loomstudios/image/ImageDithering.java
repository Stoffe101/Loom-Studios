package dev.loomstudios.image;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic error-diffusion dithering for Smart Import.
 */
public final class ImageDithering {
    private ImageDithering() {
    }

    public static PixelImage floydSteinberg(
            PixelImage source,
            List<Integer> palette
    ) {
        Objects.requireNonNull(source, "source");
        ImageColorReduction.validatePalette(palette);

        int width = source.width();
        int height = source.height();
        int[] input = source.pixels();
        int[] output = new int[input.length];

        double[] currentRed = new double[width + 2];
        double[] currentGreen = new double[width + 2];
        double[] currentBlue = new double[width + 2];
        double[] nextRed = new double[width + 2];
        double[] nextGreen = new double[width + 2];
        double[] nextBlue = new double[width + 2];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int argb = input[index];
                int alpha = (argb >>> 24) & 0xFF;

                if (alpha == 0) {
                    output[index] = argb;
                    continue;
                }

                int errorIndex = x + 1;
                double red = clampChannel(
                        ((argb >>> 16) & 0xFF)
                                + currentRed[errorIndex]
                );
                double green = clampChannel(
                        ((argb >>> 8) & 0xFF)
                                + currentGreen[errorIndex]
                );
                double blue = clampChannel(
                        (argb & 0xFF)
                                + currentBlue[errorIndex]
                );

                int adjusted = 0xFF000000
                        | ((int)Math.round(red) << 16)
                        | ((int)Math.round(green) << 8)
                        | (int)Math.round(blue);

                int nearest = ImageColorReduction.nearestPaletteColor(
                        adjusted,
                        palette
                );
                output[index] = (alpha << 24)
                        | (nearest & 0x00FFFFFF);

                double errorRed =
                        red - ((nearest >>> 16) & 0xFF);
                double errorGreen =
                        green - ((nearest >>> 8) & 0xFF);
                double errorBlue =
                        blue - (nearest & 0xFF);

                if (x + 1 < width
                        && isVisible(input[index + 1])) {
                    addError(
                            currentRed,
                            currentGreen,
                            currentBlue,
                            errorIndex + 1,
                            errorRed,
                            errorGreen,
                            errorBlue,
                            7.0 / 16.0
                    );
                }

                if (y + 1 < height) {
                    int below = index + width;

                    if (x > 0 && isVisible(input[below - 1])) {
                        addError(
                                nextRed,
                                nextGreen,
                                nextBlue,
                                errorIndex - 1,
                                errorRed,
                                errorGreen,
                                errorBlue,
                                3.0 / 16.0
                        );
                    }

                    if (isVisible(input[below])) {
                        addError(
                                nextRed,
                                nextGreen,
                                nextBlue,
                                errorIndex,
                                errorRed,
                                errorGreen,
                                errorBlue,
                                5.0 / 16.0
                        );
                    }

                    if (x + 1 < width
                            && isVisible(input[below + 1])) {
                        addError(
                                nextRed,
                                nextGreen,
                                nextBlue,
                                errorIndex + 1,
                                errorRed,
                                errorGreen,
                                errorBlue,
                                1.0 / 16.0
                        );
                    }
                }
            }

            double[] swap = currentRed;
            currentRed = nextRed;
            nextRed = swap;

            swap = currentGreen;
            currentGreen = nextGreen;
            nextGreen = swap;

            swap = currentBlue;
            currentBlue = nextBlue;
            nextBlue = swap;

            Arrays.fill(nextRed, 0.0);
            Arrays.fill(nextGreen, 0.0);
            Arrays.fill(nextBlue, 0.0);
        }

        return new PixelImage(width, height, output);
    }

    public static PixelImage reduceColors(
            PixelImage source,
            int maxColors
    ) {
        Objects.requireNonNull(source, "source");

        if (hasAtMostColors(source, maxColors)) {
            return source;
        }

        List<Integer> palette = ImageColorReduction.extractPalette(
                source,
                maxColors
        );

        if (palette.isEmpty()) {
            return source;
        }

        return floydSteinberg(source, palette);
    }

    private static boolean hasAtMostColors(
            PixelImage source,
            int maxColors
    ) {
        if (maxColors < 1
                || maxColors > ImageColorReduction.MAX_PALETTE_COLORS) {
            throw new IllegalArgumentException(
                    "Color count must be between 1 and "
                            + ImageColorReduction.MAX_PALETTE_COLORS
            );
        }

        java.util.HashSet<Integer> colors =
                new java.util.HashSet<>(maxColors + 1);

        for (int argb : source.pixels()) {
            if (((argb >>> 24) & 0xFF) == 0) {
                continue;
            }

            colors.add(argb & 0x00FFFFFF);
            if (colors.size() > maxColors) {
                return false;
            }
        }

        return true;
    }

    private static boolean isVisible(int argb) {
        return ((argb >>> 24) & 0xFF) != 0;
    }

    private static double clampChannel(double value) {
        return Math.max(0.0, Math.min(255.0, value));
    }

    private static void addError(
            double[] red,
            double[] green,
            double[] blue,
            int index,
            double errorRed,
            double errorGreen,
            double errorBlue,
            double weight
    ) {
        red[index] += errorRed * weight;
        green[index] += errorGreen * weight;
        blue[index] += errorBlue * weight;
    }
}
