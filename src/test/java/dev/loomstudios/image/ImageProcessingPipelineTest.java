package dev.loomstudios.image;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageProcessingPipelineTest {
    @Test
    void outlineKeepsOnlyVisibleEdges() {
        int[] pixels = new int[25];
        for (int y = 1; y < 4; y++) {
            for (int x = 1; x < 4; x++) {
                pixels[y * 5 + x] = 0xFFFFFFFF;
            }
        }

        PixelImage outlined = ImageEdges.outline(
                new PixelImage(5, 5, pixels)
        );

        assertEquals(0, outlined.pixelAt(2, 2));
        assertEquals(0xFFFFFFFF, outlined.pixelAt(1, 1));
        assertEquals(0, outlined.pixelAt(0, 0));
    }

    @Test
    void pixelArtPresetUsesBoundedPalette() {
        PixelImage source = new PixelImage(
                4,
                1,
                new int[]{
                        0xFFFF0000,
                        0xFF00FF00,
                        0xFF0000FF,
                        0xFFFFFFFF
                }
        );

        ImageProcessingSettings settings =
                ImageProcessingSettings.defaults()
                        .withMode(ImageProcessingMode.PIXEL_ART)
                        .withColorReduction(2, false);

        PixelImage result = ImageProcessingPipeline.apply(
                source,
                settings
        );

        long distinct = java.util.Arrays.stream(result.pixels())
                .map(color -> color & 0x00FFFFFF)
                .distinct()
                .count();

        assertTrue(distinct <= 2);
    }

    @Test
    void paletteLimitedPreservesAlpha() {
        PixelImage source = new PixelImage(
                2,
                1,
                new int[]{
                        0x80F01010,
                        0x4000F010
                }
        );

        ImageProcessingSettings settings =
                ImageProcessingSettings.defaults()
                        .withMode(ImageProcessingMode.PALETTE_LIMITED)
                        .withPalette(List.of(
                                0xFFFF0000,
                                0xFF00FF00
                        ));

        PixelImage result = ImageProcessingPipeline.apply(
                source,
                settings
        );

        assertEquals(0x80, (result.pixelAt(0, 0) >>> 24) & 0xFF);
        assertEquals(0x40, (result.pixelAt(1, 0) >>> 24) & 0xFF);
        assertEquals(0x00FF0000, result.pixelAt(0, 0) & 0x00FFFFFF);
        assertEquals(0x0000FF00, result.pixelAt(1, 0) & 0x00FFFFFF);
    }

    @Test
    void directModeWithNoAdjustmentsIsStable() {
        PixelImage source = new PixelImage(
                2,
                2,
                new int[]{
                        0x00112233,
                        0x80123456,
                        0xFFABCDEF,
                        0xFFFFFFFF
                }
        );

        PixelImage result = ImageProcessingPipeline.apply(
                source,
                ImageProcessingSettings.defaults()
        );

        assertEquals(source, result);
    }

    @Test
    void processingIdsRoundTrip() {
        for (ImageProcessingMode mode : ImageProcessingMode.values()) {
            assertSame(
                    mode,
                    ImageProcessingMode.fromId(mode.id())
            );
        }
    }
}
