package dev.loomstudios.image;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageTransformsTest {
    @Test
    void pixelImageOwnsPixelsAndValidatesBounds() {
        int[] pixels = {0xFF112233, 0xFF445566};
        PixelImage image = new PixelImage(2, 1, pixels);

        pixels[0] = 0;
        assertEquals(0xFF112233, image.pixelAt(0, 0));

        int[] copy = image.pixels();
        copy[1] = 0;
        assertEquals(0xFF445566, image.pixelAt(1, 0));

        assertThrows(
                IllegalArgumentException.class,
                () -> new PixelImage(2, 2, new int[3])
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new PixelImage(
                        PixelImage.MAX_DIMENSION + 1,
                        1,
                        new int[0]
                )
        );
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> image.pixelAt(2, 0)
        );
    }

    @Test
    void mirrorAndQuarterTurnTransformsPreserveExpectedPixels() {
        PixelImage source = image(
                3,
                2,
                1, 2, 3,
                4, 5, 6
        );

        assertPixels(
                ImageTransforms.mirrorHorizontal(source),
                3, 2,
                3, 2, 1,
                6, 5, 4
        );
        assertPixels(
                ImageTransforms.mirrorVertical(source),
                3, 2,
                4, 5, 6,
                1, 2, 3
        );
        assertPixels(
                ImageTransforms.rotateClockwise(source),
                2, 3,
                4, 1,
                5, 2,
                6, 3
        );
        assertPixels(
                ImageTransforms.rotateCounterClockwise(source),
                2, 3,
                3, 6,
                2, 5,
                1, 4
        );
    }

    @Test
    void cropAndNearestResizeAreDeterministic() {
        PixelImage source = image(
                3,
                2,
                1, 2, 3,
                4, 5, 6
        );

        assertPixels(
                ImageTransforms.crop(source, 1, 0, 2, 2),
                2, 2,
                2, 3,
                5, 6
        );

        PixelImage twoByTwo = image(
                2,
                2,
                1, 2,
                3, 4
        );

        assertPixels(
                ImageTransforms.resizeNearest(twoByTwo, 4, 4),
                4, 4,
                1, 1, 2, 2,
                1, 1, 2, 2,
                3, 3, 4, 4,
                3, 3, 4, 4
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageTransforms.crop(source, 2, 1, 2, 1)
        );
    }

    @Test
    void placementModesProduceStableGeometry() {
        assertEquals(
                new ImagePlacement(0, 0, 4, 2, 0, 1, 4, 2),
                ImagePlacementCalculator.calculate(
                        4, 2, 4, 4, ImagePlacementMode.FIT
                )
        );

        assertEquals(
                new ImagePlacement(0, 0, 4, 2, 0, 0, 2, 4),
                ImagePlacementCalculator.calculate(
                        4, 2, 2, 4, ImagePlacementMode.STRETCH
                )
        );

        assertEquals(
                new ImagePlacement(1, 0, 2, 2, 0, 0, 2, 2),
                ImagePlacementCalculator.calculate(
                        4, 2, 2, 2, ImagePlacementMode.CROP
                )
        );

        assertEquals(
                new ImagePlacement(0, 0, 4, 4, -1, -1, 4, 4),
                ImagePlacementCalculator.calculate(
                        4, 4, 2, 2, ImagePlacementMode.CENTER
                )
        );
    }

    @Test
    void fitPlacementLeavesTransparentLetterbox() {
        PixelImage source = image(2, 1, 1, 2);

        assertPixels(
                ImageTransforms.place(
                        source,
                        2,
                        3,
                        ImagePlacementMode.FIT
                ),
                2, 3,
                0, 0,
                1, 2,
                0, 0
        );
    }

    @Test
    void cropPlacementUsesCenteredSourceWindow() {
        PixelImage source = image(
                4,
                2,
                1, 2, 3, 4,
                5, 6, 7, 8
        );

        assertPixels(
                ImageTransforms.place(
                        source,
                        2,
                        2,
                        ImagePlacementMode.CROP
                ),
                2, 2,
                2, 3,
                6, 7
        );
    }

    @Test
    void centerPlacementClipsOversizedArtworkWithoutScaling() {
        PixelImage source = image(
                4,
                4,
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
                13, 14, 15, 16
        );

        assertPixels(
                ImageTransforms.place(
                        source,
                        2,
                        2,
                        ImagePlacementMode.CENTER
                ),
                2, 2,
                6, 7,
                10, 11
        );
    }

    @Test
    void oversizedCropArithmeticCannotWrapIntoValidBounds() {
        PixelImage source = image(2, 2, 1, 2, 3, 4);

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageTransforms.crop(
                        source,
                        1,
                        1,
                        Integer.MAX_VALUE,
                        1
                )
        );
    }

    @Test
    void oversizedPlacementArithmeticCannotWrapIntoSourceBounds() {
        PixelImage source = image(2, 2, 1, 2, 3, 4);
        ImagePlacement invalid = new ImagePlacement(
                1,
                1,
                Integer.MAX_VALUE,
                1,
                0,
                0,
                1,
                1
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageTransforms.place(source, 2, 2, invalid)
        );
    }

    @Test
    void fullyOffscreenDestinationWithExtremeOffsetIsSafelyClipped() {
        PixelImage source = image(1, 1, 7);
        ImagePlacement offscreen = new ImagePlacement(
                0,
                0,
                1,
                1,
                Integer.MIN_VALUE,
                0,
                Integer.MAX_VALUE,
                1
        );

        assertPixels(
                ImageTransforms.place(source, 2, 1, offscreen),
                2,
                1,
                0, 0
        );
    }

    @Test
    void customPlacementRejectsSourceRectangleOutsideImage() {
        PixelImage source = image(2, 2, 1, 2, 3, 4);
        ImagePlacement invalid = new ImagePlacement(
                1, 1, 2, 2,
                0, 0, 2, 2
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImageTransforms.place(source, 2, 2, invalid)
        );
    }

    private static PixelImage image(
            int width,
            int height,
            int... values
    ) {
        int[] pixels = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            pixels[i] = values[i] == 0
                    ? 0
                    : 0xFF000000 | values[i];
        }
        return new PixelImage(width, height, pixels);
    }

    private static void assertPixels(
            PixelImage actual,
            int width,
            int height,
            int... expectedValues
    ) {
        assertEquals(width, actual.width());
        assertEquals(height, actual.height());
        assertEquals(expectedValues.length, actual.pixels().length);

        for (int i = 0; i < expectedValues.length; i++) {
            int expected = expectedValues[i] == 0
                    ? 0
                    : 0xFF000000 | expectedValues[i];
            assertEquals(
                    expected,
                    actual.pixels()[i],
                    "pixel index " + i
            );
        }
    }
}
