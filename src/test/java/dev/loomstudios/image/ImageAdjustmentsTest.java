package dev.loomstudios.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageAdjustmentsTest {
    @Test
    void zeroAdjustmentsArePixelStable() {
        PixelImage source = new PixelImage(
                2,
                1,
                new int[]{0x804080C0, 0xFF112233}
        );

        assertEquals(
                source,
                ImageAdjustments.adjust(source, 0.0F, 0.0F, 0.0F)
        );
    }

    @Test
    void brightnessPreservesAlphaAndUsesNormalizedRange() {
        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0x80000000}
        );

        PixelImage brighter = ImageAdjustments.brightness(source, 0.25F);

        assertEquals(0x80404040, brighter.pixelAt(0, 0));
    }

    @Test
    void fullDesaturationUsesLuminanceAndPreservesAlpha() {
        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0x80FF0000}
        );

        PixelImage grayscale = ImageAdjustments.saturation(source, -1.0F);

        assertEquals(0x80363636, grayscale.pixelAt(0, 0));
    }

    @Test
    void adjustmentAmountsAreStrictlyBounded() {
        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0xFFFFFFFF}
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageAdjustments.brightness(source, 1.01F)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageAdjustments.contrast(source, -1.01F)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageAdjustments.saturation(
                        source,
                        Float.NaN
                )
        );
    }
}
