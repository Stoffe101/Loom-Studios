package dev.loomstudios.image;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ImageColorReductionTest {
    @Test
    void reduceColorsLeavesAlreadyBoundedArtworkPixelStable() {
        PixelImage source = new PixelImage(
                3,
                1,
                new int[]{
                        0x80FF0000,
                        0xFF00FF00,
                        0x00ABCDEF
                }
        );

        assertSame(
                source,
                ImageColorReduction.reduceColors(source, 2)
        );
    }

    @Test
    void reduceColorsHonorsRequestedMaximumAndPreservesAlpha() {
        PixelImage source = new PixelImage(
                4,
                2,
                new int[]{
                        0x40FF0000,
                        0x8000FF00,
                        0xC00000FF,
                        0xFFFFFFFF,
                        0xFFFF8000,
                        0xFF00FFFF,
                        0xFFFF00FF,
                        0xFF444444
                }
        );

        PixelImage reduced = ImageColorReduction.reduceColors(source, 3);

        assertTrue(visibleRgbColors(reduced).size() <= 3);

        for (int i = 0; i < source.pixels().length; i++) {
            assertEquals(
                    (source.pixels()[i] >>> 24) & 0xFF,
                    (reduced.pixels()[i] >>> 24) & 0xFF
            );
        }
    }

    @Test
    void paletteExtractionAndReductionAreDeterministic() {
        PixelImage source = new PixelImage(
                6,
                1,
                new int[]{
                        0xFFFF0000,
                        0xFFEE1000,
                        0xFF00FF00,
                        0xFF10EE00,
                        0xFF0000FF,
                        0xFF0010EE
                }
        );

        List<Integer> firstPalette =
                ImageColorReduction.extractPalette(source, 3);
        List<Integer> secondPalette =
                ImageColorReduction.extractPalette(source, 3);

        assertEquals(firstPalette, secondPalette);
        assertEquals(
                ImageColorReduction.reduceColors(source, 3),
                ImageColorReduction.reduceColors(source, 3)
        );
    }

    @Test
    void paletteLimitedMappingUsesNearestRgbAndKeepsSourceAlpha() {
        PixelImage source = new PixelImage(
                3,
                1,
                new int[]{
                        0x80F01010,
                        0xFF1010F0,
                        0x00112233
                }
        );

        PixelImage mapped = ImageColorReduction.mapToPalette(
                source,
                List.of(0xFFFF0000, 0xFF0000FF)
        );

        assertArrayEquals(
                new int[]{
                        0x80FF0000,
                        0xFF0000FF,
                        0x00112233
                },
                mapped.pixels()
        );
    }

    @Test
    void posterizeUsesRequestedPerChannelLevels() {
        PixelImage source = new PixelImage(
                3,
                1,
                new int[]{
                        0x80101010,
                        0xFF808080,
                        0xFFEFEFEF
                }
        );

        PixelImage posterized =
                ImageColorReduction.posterize(source, 2);

        assertArrayEquals(
                new int[]{
                        0x80000000,
                        0xFFFFFFFF,
                        0xFFFFFFFF
                },
                posterized.pixels()
        );
    }

    @Test
    void monochromeUsesLuminanceAndPreservesTransparency() {
        PixelImage source = new PixelImage(
                2,
                1,
                new int[]{
                        0x80FF0000,
                        0x00123456
                }
        );

        PixelImage monochrome =
                ImageColorReduction.monochrome(source);

        assertEquals(0x80363636, monochrome.pixelAt(0, 0));
        assertEquals(0x00123456, monochrome.pixelAt(1, 0));
    }

    @Test
    void reductionAndPaletteBoundsAreStrict() {
        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0xFFFFFFFF}
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageColorReduction.reduceColors(source, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageColorReduction.extractPalette(
                        source,
                        ImageColorReduction.MAX_PALETTE_COLORS + 1
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageColorReduction.mapToPalette(
                        source,
                        List.of()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageColorReduction.posterize(source, 1)
        );
    }

    private static Set<Integer> visibleRgbColors(PixelImage image) {
        Set<Integer> colors = new HashSet<>();

        for (int argb : image.pixels()) {
            if (((argb >>> 24) & 0xFF) != 0) {
                colors.add(argb & 0x00FFFFFF);
            }
        }

        return colors;
    }
}
