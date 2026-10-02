package dev.loomstudios.image;

import java.util.Objects;

/**
 * Deterministic color adjustments used by Smart Import processing.
 *
 * <p>All controls use the normalized range -1..1. Alpha is preserved.</p>
 */
public final class ImageAdjustments {
    private ImageAdjustments() {
    }

    public static PixelImage adjust(
            PixelImage source,
            float brightness,
            float contrast,
            float saturation
    ) {
        Objects.requireNonNull(source, "source");
        validateAmount(brightness, "brightness");
        validateAmount(contrast, "contrast");
        validateAmount(saturation, "saturation");

        int[] input = source.pixels();
        int[] output = new int[input.length];

        for (int i = 0; i < input.length; i++) {
            int argb = input[i];
            int alpha = (argb >>> 24) & 0xFF;

            double red = ((argb >>> 16) & 0xFF) / 255.0;
            double green = ((argb >>> 8) & 0xFF) / 255.0;
            double blue = (argb & 0xFF) / 255.0;

            red += brightness;
            green += brightness;
            blue += brightness;

            double contrastScale = 1.0 + contrast;
            red = (red - 0.5) * contrastScale + 0.5;
            green = (green - 0.5) * contrastScale + 0.5;
            blue = (blue - 0.5) * contrastScale + 0.5;

            double luminance =
                    red * 0.2126
                            + green * 0.7152
                            + blue * 0.0722;
            double saturationScale = 1.0 + saturation;

            red = luminance + (red - luminance) * saturationScale;
            green = luminance + (green - luminance) * saturationScale;
            blue = luminance + (blue - luminance) * saturationScale;

            output[i] = (alpha << 24)
                    | (toByte(red) << 16)
                    | (toByte(green) << 8)
                    | toByte(blue);
        }

        return new PixelImage(source.width(), source.height(), output);
    }

    public static PixelImage brightness(PixelImage source, float amount) {
        return adjust(source, amount, 0.0F, 0.0F);
    }

    public static PixelImage contrast(PixelImage source, float amount) {
        return adjust(source, 0.0F, amount, 0.0F);
    }

    public static PixelImage saturation(PixelImage source, float amount) {
        return adjust(source, 0.0F, 0.0F, amount);
    }

    private static void validateAmount(float value, String label) {
        if (!Float.isFinite(value) || value < -1.0F || value > 1.0F) {
            throw new IllegalArgumentException(
                    label + " must be between -1 and 1"
            );
        }
    }

    private static int toByte(double value) {
        return Math.max(
                0,
                Math.min(255, (int)Math.round(value * 255.0))
        );
    }
}
