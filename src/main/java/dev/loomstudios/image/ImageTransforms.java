package dev.loomstudios.image;

import java.util.Objects;

/**
 * Pure ARGB transform primitives for Smart Import previews and future
 * non-destructive image-layer compilation.
 */
public final class ImageTransforms {
    private ImageTransforms() {
    }

    public static PixelImage mirrorHorizontal(PixelImage source) {
        Objects.requireNonNull(source, "source");
        int[] output = new int[PixelImage.checkedPixelCount(
                source.width(),
                source.height()
        )];

        for (int y = 0; y < source.height(); y++) {
            for (int x = 0; x < source.width(); x++) {
                output[y * source.width() + x] =
                        source.pixelAt(source.width() - 1 - x, y);
            }
        }

        return new PixelImage(source.width(), source.height(), output);
    }

    public static PixelImage mirrorVertical(PixelImage source) {
        Objects.requireNonNull(source, "source");
        int[] output = new int[PixelImage.checkedPixelCount(
                source.width(),
                source.height()
        )];

        for (int y = 0; y < source.height(); y++) {
            for (int x = 0; x < source.width(); x++) {
                output[y * source.width() + x] =
                        source.pixelAt(x, source.height() - 1 - y);
            }
        }

        return new PixelImage(source.width(), source.height(), output);
    }

    public static PixelImage rotateClockwise(PixelImage source) {
        Objects.requireNonNull(source, "source");
        int width = source.height();
        int height = source.width();
        int[] output = new int[PixelImage.checkedPixelCount(width, height)];

        for (int y = 0; y < source.height(); y++) {
            for (int x = 0; x < source.width(); x++) {
                int destinationX = source.height() - 1 - y;
                int destinationY = x;
                output[destinationY * width + destinationX] =
                        source.pixelAt(x, y);
            }
        }

        return new PixelImage(width, height, output);
    }

    public static PixelImage rotateCounterClockwise(PixelImage source) {
        Objects.requireNonNull(source, "source");
        int width = source.height();
        int height = source.width();
        int[] output = new int[PixelImage.checkedPixelCount(width, height)];

        for (int y = 0; y < source.height(); y++) {
            for (int x = 0; x < source.width(); x++) {
                int destinationX = y;
                int destinationY = source.width() - 1 - x;
                output[destinationY * width + destinationX] =
                        source.pixelAt(x, y);
            }
        }

        return new PixelImage(width, height, output);
    }

    public static PixelImage crop(
            PixelImage source,
            int x,
            int y,
            int width,
            int height
    ) {
        Objects.requireNonNull(source, "source");

        if (x < 0
                || y < 0
                || width <= 0
                || height <= 0
                || x + width > source.width()
                || y + height > source.height()) {
            throw new IllegalArgumentException("Crop rectangle outside image");
        }

        int[] output = new int[PixelImage.checkedPixelCount(width, height)];

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                output[row * width + column] =
                        source.pixelAt(x + column, y + row);
            }
        }

        return new PixelImage(width, height, output);
    }

    public static PixelImage resizeNearest(
            PixelImage source,
            int targetWidth,
            int targetHeight
    ) {
        Objects.requireNonNull(source, "source");
        int[] output = new int[
                PixelImage.checkedPixelCount(targetWidth, targetHeight)
        ];

        for (int y = 0; y < targetHeight; y++) {
            int sourceY = y * source.height() / targetHeight;

            for (int x = 0; x < targetWidth; x++) {
                int sourceX = x * source.width() / targetWidth;
                output[y * targetWidth + x] =
                        source.pixelAt(sourceX, sourceY);
            }
        }

        return new PixelImage(targetWidth, targetHeight, output);
    }

    public static PixelImage place(
            PixelImage source,
            int targetWidth,
            int targetHeight,
            ImagePlacementMode mode
    ) {
        Objects.requireNonNull(source, "source");

        ImagePlacement placement = ImagePlacementCalculator.calculate(
                source.width(),
                source.height(),
                targetWidth,
                targetHeight,
                mode
        );

        return place(source, targetWidth, targetHeight, placement);
    }

    public static PixelImage place(
            PixelImage source,
            int targetWidth,
            int targetHeight,
            ImagePlacement placement
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(placement, "placement");

        if (placement.sourceX() + placement.sourceWidth() > source.width()
                || placement.sourceY() + placement.sourceHeight()
                > source.height()) {
            throw new IllegalArgumentException(
                    "Placement source rectangle exceeds image"
            );
        }

        int[] output = new int[
                PixelImage.checkedPixelCount(targetWidth, targetHeight)
        ];

        int startX = Math.max(0, placement.destinationX());
        int startY = Math.max(0, placement.destinationY());
        int endX = Math.min(
                targetWidth,
                placement.destinationX() + placement.destinationWidth()
        );
        int endY = Math.min(
                targetHeight,
                placement.destinationY() + placement.destinationHeight()
        );

        for (int y = startY; y < endY; y++) {
            int localY = y - placement.destinationY();
            int sourceY = placement.sourceY()
                    + (int)(
                            (long)localY
                                    * placement.sourceHeight()
                                    / placement.destinationHeight()
                    );

            for (int x = startX; x < endX; x++) {
                int localX = x - placement.destinationX();
                int sourceX = placement.sourceX()
                        + (int)(
                                (long)localX
                                        * placement.sourceWidth()
                                        / placement.destinationWidth()
                        );

                output[y * targetWidth + x] =
                        source.pixelAt(sourceX, sourceY);
            }
        }

        return new PixelImage(targetWidth, targetHeight, output);
    }
}
