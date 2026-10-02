package dev.loomstudios.project;

public record GradientStop(
        double position,
        int argb
) {
    public GradientStop {
        if (!Double.isFinite(position)
                || position < 0.0
                || position > 1.0) {
            throw new IllegalArgumentException(
                    "Gradient stop out of range"
            );
        }
    }
}
