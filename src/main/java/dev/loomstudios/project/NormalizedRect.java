package dev.loomstudios.project;

public record NormalizedRect(
        double x,
        double y,
        double width,
        double height
) {
    private static final double EPSILON = 1.0E-9;

    public NormalizedRect {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(width)
                || !Double.isFinite(height)
                || x < 0.0
                || y < 0.0
                || width <= 0.0
                || height <= 0.0
                || x + width > 1.0 + EPSILON
                || y + height > 1.0 + EPSILON) {
            throw new IllegalArgumentException(
                    "Normalized rectangle out of range"
            );
        }
    }

    public static NormalizedRect fullCanvas() {
        return new NormalizedRect(0.0, 0.0, 1.0, 1.0);
    }

    public double centerX() {
        return x + width / 2.0;
    }

    public double centerY() {
        return y + height / 2.0;
    }

    public boolean contains(double px, double py) {
        return px >= x
                && py >= y
                && px <= x + width
                && py <= y + height;
    }
}
