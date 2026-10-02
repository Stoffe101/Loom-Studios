package dev.loomstudios.image;

import java.util.List;
import java.util.Objects;

public record ImageProcessingSettings(
        ImageProcessingMode mode,
        float brightness,
        float contrast,
        float saturation,
        int colorLimit,
        boolean dither,
        int posterizeLevels,
        List<Integer> palette
) {
    public ImageProcessingSettings {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(palette, "palette");

        validateUnit(brightness, "brightness");
        validateUnit(contrast, "contrast");
        validateUnit(saturation, "saturation");

        if (colorLimit != 0
                && (colorLimit < 1
                || colorLimit > ImageColorReduction.MAX_PALETTE_COLORS)) {
            throw new IllegalArgumentException("Color limit out of range");
        }

        if (posterizeLevels < 2 || posterizeLevels > 256) {
            throw new IllegalArgumentException("Posterize levels out of range");
        }

        if (palette.size() > ImageColorReduction.MAX_PALETTE_COLORS) {
            throw new IllegalArgumentException("Palette too large");
        }

        palette = List.copyOf(palette);
    }

    public static ImageProcessingSettings defaults() {
        return new ImageProcessingSettings(
                ImageProcessingMode.DIRECT,
                0.0F,
                0.0F,
                0.0F,
                0,
                false,
                4,
                List.of()
        );
    }

    public ImageProcessingSettings withMode(ImageProcessingMode next) {
        return new ImageProcessingSettings(
                next,
                brightness,
                contrast,
                saturation,
                colorLimit,
                dither,
                posterizeLevels,
                palette
        );
    }

    public ImageProcessingSettings withAdjustments(
            float nextBrightness,
            float nextContrast,
            float nextSaturation
    ) {
        return new ImageProcessingSettings(
                mode,
                nextBrightness,
                nextContrast,
                nextSaturation,
                colorLimit,
                dither,
                posterizeLevels,
                palette
        );
    }

    public ImageProcessingSettings withColorReduction(
            int nextColorLimit,
            boolean nextDither
    ) {
        return new ImageProcessingSettings(
                mode,
                brightness,
                contrast,
                saturation,
                nextColorLimit,
                nextDither,
                posterizeLevels,
                palette
        );
    }

    public ImageProcessingSettings withPosterizeLevels(int levels) {
        return new ImageProcessingSettings(
                mode,
                brightness,
                contrast,
                saturation,
                colorLimit,
                dither,
                levels,
                palette
        );
    }

    public ImageProcessingSettings withPalette(List<Integer> nextPalette) {
        return new ImageProcessingSettings(
                mode,
                brightness,
                contrast,
                saturation,
                colorLimit,
                dither,
                posterizeLevels,
                nextPalette
        );
    }

    private static void validateUnit(float value, String label) {
        if (!Float.isFinite(value) || value < -1.0F || value > 1.0F) {
            throw new IllegalArgumentException(
                    label + " must be between -1 and 1"
            );
        }
    }
}
