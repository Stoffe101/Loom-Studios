package dev.loomstudios.image;

import java.util.Objects;

/**
 * Pure placement math shared by Smart Import preview and eventual image layers.
 */
public final class ImagePlacementCalculator {
    private ImagePlacementCalculator() {
    }

    public static ImagePlacement calculate(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight,
            ImagePlacementMode mode
    ) {
        PixelImage.checkedPixelCount(sourceWidth, sourceHeight);
        PixelImage.checkedPixelCount(targetWidth, targetHeight);
        Objects.requireNonNull(mode, "mode");

        return switch (mode) {
            case FIT -> fit(
                    sourceWidth,
                    sourceHeight,
                    targetWidth,
                    targetHeight
            );
            case STRETCH -> new ImagePlacement(
                    0,
                    0,
                    sourceWidth,
                    sourceHeight,
                    0,
                    0,
                    targetWidth,
                    targetHeight
            );
            case CROP -> crop(
                    sourceWidth,
                    sourceHeight,
                    targetWidth,
                    targetHeight
            );
            case CENTER -> new ImagePlacement(
                    0,
                    0,
                    sourceWidth,
                    sourceHeight,
                    Math.floorDiv(targetWidth - sourceWidth, 2),
                    Math.floorDiv(targetHeight - sourceHeight, 2),
                    sourceWidth,
                    sourceHeight
            );
        };
    }

    private static ImagePlacement fit(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight
    ) {
        double scale = Math.min(
                targetWidth / (double)sourceWidth,
                targetHeight / (double)sourceHeight
        );

        int destinationWidth = Math.max(
                1,
                Math.min(
                        targetWidth,
                        (int)Math.round(sourceWidth * scale)
                )
        );
        int destinationHeight = Math.max(
                1,
                Math.min(
                        targetHeight,
                        (int)Math.round(sourceHeight * scale)
                )
        );

        return new ImagePlacement(
                0,
                0,
                sourceWidth,
                sourceHeight,
                (targetWidth - destinationWidth) / 2,
                (targetHeight - destinationHeight) / 2,
                destinationWidth,
                destinationHeight
        );
    }

    private static ImagePlacement crop(
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight
    ) {
        double sourceAspect = sourceWidth / (double)sourceHeight;
        double targetAspect = targetWidth / (double)targetHeight;

        int sourceX = 0;
        int sourceY = 0;
        int cropWidth = sourceWidth;
        int cropHeight = sourceHeight;

        if (sourceAspect > targetAspect) {
            cropWidth = Math.max(
                    1,
                    Math.min(
                            sourceWidth,
                            (int)Math.round(sourceHeight * targetAspect)
                    )
            );
            sourceX = (sourceWidth - cropWidth) / 2;
        } else if (sourceAspect < targetAspect) {
            cropHeight = Math.max(
                    1,
                    Math.min(
                            sourceHeight,
                            (int)Math.round(sourceWidth / targetAspect)
                    )
            );
            sourceY = (sourceHeight - cropHeight) / 2;
        }

        return new ImagePlacement(
                sourceX,
                sourceY,
                cropWidth,
                cropHeight,
                0,
                0,
                targetWidth,
                targetHeight
        );
    }
}
