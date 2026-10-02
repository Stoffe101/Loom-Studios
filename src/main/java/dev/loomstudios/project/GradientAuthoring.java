package dev.loomstudios.project;

import java.util.Objects;

/**
 * Pure, bounded authoring helpers for Gradient-layer transforms.
 *
 * <p>The UI expresses movement in layer-relative steps so controls behave
 * consistently for gradients that cover a full canvas or only one semantic
 * cape face.</p>
 */
public final class GradientAuthoring {
    private static final double MOVE_FRACTION = 0.05;
    private static final double MIN_SIZE = 1.0 / 4096.0;
    private static final double MAX_SIZE = 64.0;
    private static final double MIN_CENTER = -64.0;
    private static final double MAX_CENTER = 65.0;

    private GradientAuthoring() {
    }

    public static GradientLayerData translate(
            GradientLayerData data,
            int horizontalSteps,
            int verticalSteps
    ) {
        Objects.requireNonNull(data, "data");
        LayerTransform transform = data.transform();

        double x = clamp(
                transform.centerX()
                        + horizontalSteps
                        * transform.width()
                        * MOVE_FRACTION,
                MIN_CENTER,
                MAX_CENTER
        );
        double y = clamp(
                transform.centerY()
                        + verticalSteps
                        * transform.height()
                        * MOVE_FRACTION,
                MIN_CENTER,
                MAX_CENTER
        );

        return data.withTransform(transform.withCenter(x, y));
    }

    public static GradientLayerData scale(
            GradientLayerData data,
            double requestedFactor
    ) {
        Objects.requireNonNull(data, "data");

        if (!Double.isFinite(requestedFactor)
                || requestedFactor <= 0.0) {
            throw new IllegalArgumentException(
                    "Gradient scale factor must be positive and finite"
            );
        }

        LayerTransform transform = data.transform();
        double minimumFactor = Math.max(
                MIN_SIZE / transform.width(),
                MIN_SIZE / transform.height()
        );
        double maximumFactor = Math.min(
                MAX_SIZE / transform.width(),
                MAX_SIZE / transform.height()
        );
        double factor = clamp(
                requestedFactor,
                minimumFactor,
                maximumFactor
        );

        return data.withTransform(
                transform.withSize(
                        transform.width() * factor,
                        transform.height() * factor
                )
        );
    }

    public static GradientLayerData toggleMirrorHorizontal(
            GradientLayerData data
    ) {
        Objects.requireNonNull(data, "data");
        LayerTransform transform = data.transform();
        return data.withTransform(
                transform.withMirrors(
                        !transform.mirrorHorizontal(),
                        transform.mirrorVertical()
                )
        );
    }

    public static GradientLayerData toggleMirrorVertical(
            GradientLayerData data
    ) {
        Objects.requireNonNull(data, "data");
        LayerTransform transform = data.transform();
        return data.withTransform(
                transform.withMirrors(
                        transform.mirrorHorizontal(),
                        !transform.mirrorVertical()
                )
        );
    }

    public static GradientLayerData resetTransform(
            GradientLayerData data
    ) {
        Objects.requireNonNull(data, "data");
        NormalizedRect clip = data.clip();

        return data.withTransform(
                new LayerTransform(
                        clip.centerX(),
                        clip.centerY(),
                        clip.width(),
                        clip.height(),
                        0.0,
                        false,
                        false
                )
        );
    }

    public static int scalePercent(GradientLayerData data) {
        Objects.requireNonNull(data, "data");
        double baseWidth = data.clip().width();
        if (baseWidth <= 0.0) {
            return 100;
        }

        return (int)Math.round(
                data.transform().width() / baseWidth * 100.0
        );
    }

    private static double clamp(
            double value,
            double minimum,
            double maximum
    ) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
