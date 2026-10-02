package dev.loomstudios.image;

/**
 * Maps a rectangular source crop to a destination rectangle.
 *
 * <p>Destination x/y may be negative for centered artwork larger than the
 * target. Rendering clips to the target without mutating this placement.</p>
 */
public record ImagePlacement(
        int sourceX,
        int sourceY,
        int sourceWidth,
        int sourceHeight,
        int destinationX,
        int destinationY,
        int destinationWidth,
        int destinationHeight
) {
    public ImagePlacement {
        if (sourceX < 0
                || sourceY < 0
                || sourceWidth <= 0
                || sourceHeight <= 0) {
            throw new IllegalArgumentException("Invalid source placement rectangle");
        }

        if (destinationWidth <= 0 || destinationHeight <= 0) {
            throw new IllegalArgumentException(
                    "Invalid destination placement rectangle"
            );
        }
    }
}
