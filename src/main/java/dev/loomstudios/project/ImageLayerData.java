package dev.loomstudios.project;

import dev.loomstudios.image.ImagePlacement;
import dev.loomstudios.image.ImagePlacementCalculator;
import dev.loomstudios.image.ImagePlacementMode;
import dev.loomstudios.image.ImageProcessingSettings;
import dev.loomstudios.image.PixelImage;

import java.util.Objects;

public record ImageLayerData(
        PixelImage source,
        NormalizedRect sourceCrop,
        LayerTransform transform,
        NormalizedRect clip,
        ImageProcessingSettings processing
) {
    public ImageLayerData {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sourceCrop, "sourceCrop");
        Objects.requireNonNull(transform, "transform");
        Objects.requireNonNull(clip, "clip");
        Objects.requireNonNull(processing, "processing");

        if (source.width() > LoomProjectCodec.MAX_EMBEDDED_IMAGE_DIMENSION
                || source.height()
                > LoomProjectCodec.MAX_EMBEDDED_IMAGE_DIMENSION) {
            throw new IllegalArgumentException(
                    "Embedded image exceeds Loom project limit"
            );
        }
    }

    public static ImageLayerData placed(
            PixelImage source,
            int canvasWidth,
            int canvasHeight,
            NormalizedRect target,
            ImagePlacementMode mode
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(mode, "mode");

        int targetX = (int)Math.round(target.x() * canvasWidth);
        int targetY = (int)Math.round(target.y() * canvasHeight);
        int targetWidth = Math.max(
                1,
                (int)Math.round(target.width() * canvasWidth)
        );
        int targetHeight = Math.max(
                1,
                (int)Math.round(target.height() * canvasHeight)
        );

        ImagePlacement placement =
                ImagePlacementCalculator.calculate(
                        source.width(),
                        source.height(),
                        targetWidth,
                        targetHeight,
                        mode
                );

        NormalizedRect sourceCrop = new NormalizedRect(
                placement.sourceX() / (double)source.width(),
                placement.sourceY() / (double)source.height(),
                placement.sourceWidth() / (double)source.width(),
                placement.sourceHeight() / (double)source.height()
        );

        double destinationX =
                (targetX + placement.destinationX())
                        / (double)canvasWidth;
        double destinationY =
                (targetY + placement.destinationY())
                        / (double)canvasHeight;
        double destinationWidth =
                placement.destinationWidth() / (double)canvasWidth;
        double destinationHeight =
                placement.destinationHeight() / (double)canvasHeight;

        LayerTransform transform = new LayerTransform(
                destinationX + destinationWidth / 2.0,
                destinationY + destinationHeight / 2.0,
                destinationWidth,
                destinationHeight,
                0.0,
                false,
                false
        );

        return new ImageLayerData(
                source,
                sourceCrop,
                transform,
                target,
                ImageProcessingSettings.defaults()
        );
    }

    public ImageLayerData withTransform(LayerTransform next) {
        return new ImageLayerData(
                source,
                sourceCrop,
                next,
                clip,
                processing
        );
    }

    public ImageLayerData withSourceCrop(NormalizedRect next) {
        return new ImageLayerData(
                source,
                next,
                transform,
                clip,
                processing
        );
    }

    public ImageLayerData withProcessing(
            ImageProcessingSettings next
    ) {
        return new ImageLayerData(
                source,
                sourceCrop,
                transform,
                clip,
                next
        );
    }
}
