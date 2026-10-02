package dev.loomstudios.project;

public record LayerTransform(
        double centerX,
        double centerY,
        double width,
        double height,
        double rotationDegrees,
        boolean mirrorHorizontal,
        boolean mirrorVertical
) {
    public LayerTransform {
        if (!Double.isFinite(centerX)
                || !Double.isFinite(centerY)
                || !Double.isFinite(width)
                || !Double.isFinite(height)
                || !Double.isFinite(rotationDegrees)
                || width <= 0.0
                || height <= 0.0
                || width > 64.0
                || height > 64.0
                || centerX < -64.0
                || centerX > 65.0
                || centerY < -64.0
                || centerY > 65.0) {
            throw new IllegalArgumentException(
                    "Layer transform out of range"
            );
        }
    }

    public static LayerTransform fullCanvas() {
        return new LayerTransform(
                0.5, 0.5, 1.0, 1.0, 0.0, false, false
        );
    }

    public LayerTransform withCenter(double x, double y) {
        return new LayerTransform(
                x, y, width, height, rotationDegrees,
                mirrorHorizontal, mirrorVertical
        );
    }

    public LayerTransform withSize(double nextWidth, double nextHeight) {
        return new LayerTransform(
                centerX, centerY, nextWidth, nextHeight, rotationDegrees,
                mirrorHorizontal, mirrorVertical
        );
    }

    public LayerTransform withRotation(double degrees) {
        return new LayerTransform(
                centerX, centerY, width, height, degrees,
                mirrorHorizontal, mirrorVertical
        );
    }

    public LayerTransform withMirrors(
            boolean horizontal,
            boolean vertical
    ) {
        return new LayerTransform(
                centerX, centerY, width, height, rotationDegrees,
                horizontal, vertical
        );
    }
}
