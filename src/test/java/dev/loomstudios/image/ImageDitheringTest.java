package dev.loomstudios.image;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageDitheringTest {
    private static final List<Integer> BLACK_WHITE =
            List.of(0xFF000000, 0xFFFFFFFF);

    @Test
    void floydSteinbergIsDeterministicAndPaletteLimited() {
        PixelImage source = new PixelImage(
                4,
                2,
                new int[]{
                        0xFF404040,
                        0xFF606060,
                        0xFFA0A0A0,
                        0xFFC0C0C0,
                        0xFF505050,
                        0xFF707070,
                        0xFF909090,
                        0xFFB0B0B0
                }
        );

        PixelImage first =
                ImageDithering.floydSteinberg(source, BLACK_WHITE);
        PixelImage second =
                ImageDithering.floydSteinberg(source, BLACK_WHITE);

        assertEquals(first, second);

        for (int argb : first.pixels()) {
            int rgb = argb & 0x00FFFFFF;
            assertTrue(
                    rgb == 0x000000 || rgb == 0xFFFFFF,
                    () -> "Unexpected dither color "
                            + Integer.toHexString(argb)
            );
        }
    }

    @Test
    void ditheringPreservesSourceAlpha() {
        PixelImage source = new PixelImage(
                3,
                1,
                new int[]{
                        0x40404040,
                        0x80808080,
                        0xC0C0C0C0
                }
        );

        PixelImage dithered =
                ImageDithering.floydSteinberg(source, BLACK_WHITE);

        for (int i = 0; i < source.pixels().length; i++) {
            assertEquals(
                    (source.pixels()[i] >>> 24) & 0xFF,
                    (dithered.pixels()[i] >>> 24) & 0xFF
            );
        }
    }

    @Test
    void transparentPixelsRemainUntouchedAndBlockErrorDiffusion() {
        PixelImage source = new PixelImage(
                3,
                1,
                new int[]{
                        0xFF707070,
                        0x00123456,
                        0xFF707070
                }
        );

        PixelImage dithered =
                ImageDithering.floydSteinberg(source, BLACK_WHITE);

        assertEquals(0x00123456, dithered.pixelAt(1, 0));

        PixelImage isolatedRight = new PixelImage(
                1,
                1,
                new int[]{0xFF707070}
        );

        assertEquals(
                ImageDithering.floydSteinberg(
                        isolatedRight,
                        BLACK_WHITE
                ).pixelAt(0, 0),
                dithered.pixelAt(2, 0)
        );
    }

    @Test
    void ditheredReductionLeavesAlreadyBoundedArtworkStable() {
        PixelImage source = new PixelImage(
                4,
                1,
                new int[]{
                        0xFFFF0000,
                        0xFF00FF00,
                        0x80FF0000,
                        0x00010203
                }
        );

        assertSame(
                source,
                ImageDithering.reduceColors(source, 2)
        );
    }

    @Test
    void ditheredReductionHonorsRequestedColorMaximum() {
        PixelImage source = new PixelImage(
                6,
                1,
                new int[]{
                        0xFFFF0000,
                        0xFFFF8000,
                        0xFFFFFF00,
                        0xFF00FF00,
                        0xFF00FFFF,
                        0xFF0000FF
                }
        );

        PixelImage dithered =
                ImageDithering.reduceColors(source, 3);

        java.util.Set<Integer> colors = new java.util.HashSet<>();
        for (int argb : dithered.pixels()) {
            if (((argb >>> 24) & 0xFF) != 0) {
                colors.add(argb & 0x00FFFFFF);
            }
        }

        assertTrue(colors.size() <= 3);
    }

    @Test
    void invalidPaletteAndReductionSizesAreRejected() {
        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0xFF808080}
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageDithering.floydSteinberg(
                        source,
                        List.of()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageDithering.reduceColors(source, 0)
        );
    }
}
