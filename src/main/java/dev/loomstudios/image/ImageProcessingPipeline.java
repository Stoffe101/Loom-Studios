package dev.loomstudios.image;

import java.util.List;
import java.util.Objects;

public final class ImageProcessingPipeline {
    private ImageProcessingPipeline() {
    }

    public static PixelImage apply(
            PixelImage source,
            ImageProcessingSettings settings
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(settings, "settings");

        source = settings.background().apply(source);
        if(settings.tintStrength()>0) {
            int[] colors=source.pixels();float t=settings.tintStrength();int tint=settings.tintColor();
            for(int i=0;i<colors.length;i++){int c=colors[i],rgb=0;for(int shift:new int[]{0,8,16}){int v=c>>>shift&255,channel=tint>>>shift&255;rgb|=Math.round(v*(1-t)+v*channel/255f*t)<<shift;}colors[i]=(c&0xFF000000)|rgb;}
            source=new PixelImage(source.width(),source.height(),colors);
        }
        PixelImage adjusted = ImageAdjustments.adjust(
                source,
                settings.brightness(),
                settings.contrast(),
                settings.saturation()
        );

        PixelImage result = switch (settings.mode()) {
            case DIRECT -> adjusted;
            case PIXEL_ART -> reduce(
                    adjusted,
                    settings.colorLimit() == 0
                            ? 16
                            : settings.colorLimit(),
                    settings.dither()
            );
            case OUTLINE -> ImageEdges.outline(adjusted);
            case MONOCHROME ->
                    ImageColorReduction.monochrome(adjusted);
            case PALETTE_LIMITED -> paletteLimited(
                    adjusted,
                    settings
            );
            case POSTERIZE -> ImageColorReduction.posterize(
                    adjusted,
                    settings.posterizeLevels()
            );
        };

        if (settings.mode() != ImageProcessingMode.PIXEL_ART
                && settings.mode()
                != ImageProcessingMode.PALETTE_LIMITED
                && settings.colorLimit() > 0) {
            return reduce(
                    result,
                    settings.colorLimit(),
                    settings.dither()
            );
        }

        return result;
    }

    private static PixelImage paletteLimited(
            PixelImage source,
            ImageProcessingSettings settings
    ) {
        List<Integer> palette = settings.palette();

        if (palette.isEmpty()) {
            palette = ImageColorReduction.extractPalette(
                    source,
                    settings.colorLimit() == 0
                            ? 8
                            : settings.colorLimit()
            );
        }

        if (palette.isEmpty()) {
            return source;
        }

        return settings.dither()
                ? ImageDithering.floydSteinberg(source, palette)
                : ImageColorReduction.mapToPalette(source, palette);
    }

    private static PixelImage reduce(
            PixelImage source,
            int colors,
            boolean dither
    ) {
        return dither
                ? ImageDithering.reduceColors(source, colors)
                : ImageColorReduction.reduceColors(source, colors);
    }
}
