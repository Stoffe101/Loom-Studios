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
        List<Integer> palette,
        int tintColor, float tintStrength, BackgroundRemoval background
) {
    public ImageProcessingSettings(ImageProcessingMode mode,float brightness,float contrast,float saturation,int colorLimit,boolean dither,int posterizeLevels,List<Integer> palette){this(mode,brightness,contrast,saturation,colorLimit,dither,posterizeLevels,palette,0xFFFFFFFF,0,BackgroundRemoval.none());}
    public ImageProcessingSettings withTint(int color,float strength){return new ImageProcessingSettings(mode,brightness,contrast,saturation,colorLimit,dither,posterizeLevels,palette,color,strength,background);}
    public ImageProcessingSettings withBackground(BackgroundRemoval value){return new ImageProcessingSettings(mode,brightness,contrast,saturation,colorLimit,dither,posterizeLevels,palette,tintColor,tintStrength,value);}
    public ImageProcessingSettings {
        Objects.requireNonNull(background,"background");
        if(!Float.isFinite(tintStrength)||tintStrength<0||tintStrength>1)throw new IllegalArgumentException("Tint strength must be 0–1");
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
                palette, tintColor, tintStrength, background
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
                palette, tintColor, tintStrength, background
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
                palette, tintColor, tintStrength, background
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
                palette, tintColor, tintStrength, background
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
                nextPalette, tintColor, tintStrength, background
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
