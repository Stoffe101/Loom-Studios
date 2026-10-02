package dev.loomstudios.project;

import dev.loomstudios.image.ImageProcessingPipeline;
import dev.loomstudios.image.PixelImage;

import java.util.List;

public final class LayerRasterizer {
    private static final int[][] BAYER_4 = {
            {0, 8, 2, 10},
            {12, 4, 14, 6},
            {3, 11, 1, 9},
            {15, 7, 13, 5}
    };

    private LayerRasterizer() {
    }

    public static int[] rasterize(
            LoomLayer layer,
            int canvasWidth,
            int canvasHeight
    ) {
        return switch (layer.kind()) {
            case PAINT -> layer.pixels();
            case IMAGE -> rasterizeImage(
                    layer.imageData(),
                    canvasWidth,
                    canvasHeight
            );
            case GRADIENT -> rasterizeGradient(
                    layer.gradientData(),
                    canvasWidth,
                    canvasHeight
            );
        };
    }

    private static int[] rasterizeImage(
            ImageLayerData data,
            int canvasWidth,
            int canvasHeight
    ) {
        PixelImage processed = ImageProcessingPipeline.apply(
                data.source(),
                data.processing()
        );
        int[] output = new int[canvasWidth * canvasHeight];

        for (int y = 0; y < canvasHeight; y++) {
            double normalizedY = (y + 0.5) / canvasHeight;

            for (int x = 0; x < canvasWidth; x++) {
                double normalizedX = (x + 0.5) / canvasWidth;

                if (!data.clip().contains(normalizedX, normalizedY)) {
                    continue;
                }

                double[] uv = inverseTransform(
                        data.transform(),
                        normalizedX,
                        normalizedY,
                        canvasWidth,
                        canvasHeight
                );

                double u = uv[0];
                double v = uv[1];

                if (data.transform().mirrorHorizontal()) {
                    u = 1.0 - u;
                }
                if (data.transform().mirrorVertical()) {
                    v = 1.0 - v;
                }

                if (u < 0.0 || v < 0.0 || u >= 1.0 || v >= 1.0) {
                    continue;
                }

                double sourceU = data.sourceCrop().x()
                        + u * data.sourceCrop().width();
                double sourceV = data.sourceCrop().y()
                        + v * data.sourceCrop().height();

                int sourceX = Math.min(
                        processed.width() - 1,
                        (int)Math.floor(sourceU * processed.width())
                );
                int sourceY = Math.min(
                        processed.height() - 1,
                        (int)Math.floor(sourceV * processed.height())
                );

                output[y * canvasWidth + x] =
                        processed.pixelAt(sourceX, sourceY);
            }
        }

        return output;
    }

    private static int[] rasterizeGradient(
            GradientLayerData data,
            int canvasWidth,
            int canvasHeight
    ) {
        int[] output = new int[canvasWidth * canvasHeight];

        for (int y = 0; y < canvasHeight; y++) {
            double normalizedY = (y + 0.5) / canvasHeight;

            for (int x = 0; x < canvasWidth; x++) {
                double normalizedX = (x + 0.5) / canvasWidth;

                if (!data.clip().contains(normalizedX, normalizedY)) {
                    continue;
                }

                double[] uv = inverseTransform(
                        data.transform(),
                        normalizedX,
                        normalizedY,
                        canvasWidth,
                        canvasHeight
                );

                double u = uv[0];
                double v = uv[1];

                if (u < 0.0 || v < 0.0 || u > 1.0 || v > 1.0) {
                    continue;
                }

                double t = switch (data.type()) {
                    case LINEAR -> u;
                    case RADIAL -> Math.sqrt(
                            Math.pow((u - 0.5) * 2.0, 2.0)
                                    + Math.pow((v - 0.5) * 2.0, 2.0)
                    );
                };

                if (data.repeat()) {
                    t = t - Math.floor(t);
                } else {
                    t = Math.max(0.0, Math.min(1.0, t));
                }

                if (data.dither()) {
                    t = Math.max(
                            0.0,
                            Math.min(
                                    1.0,
                                    t + (BAYER_4[y & 3][x & 3] - 7.5)
                                            / 510.0
                            )
                    );
                }

                output[y * canvasWidth + x] =
                        sampleStops(data.stops(), t);
            }
        }

        return output;
    }

    private static double[] inverseTransform(
            LayerTransform transform,
            double normalizedX,
            double normalizedY,
            int canvasWidth,
            int canvasHeight
    ) {
        double dx = (normalizedX - transform.centerX())
                * canvasWidth;
        double dy = (normalizedY - transform.centerY())
                * canvasHeight;

        double radians = Math.toRadians(
                -transform.rotationDegrees()
        );
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedX = cos * dx - sin * dy;
        double rotatedY = sin * dx + cos * dy;

        double widthPixels = transform.width() * canvasWidth;
        double heightPixels = transform.height() * canvasHeight;

        return new double[]{
                rotatedX / widthPixels + 0.5,
                rotatedY / heightPixels + 0.5
        };
    }

    private static int sampleStops(
            List<GradientStop> stops,
            double position
    ) {
        if (position <= stops.getFirst().position()) {
            return stops.getFirst().argb();
        }

        if (position >= stops.getLast().position()) {
            return stops.getLast().argb();
        }

        GradientStop left = stops.getFirst();
        GradientStop right = stops.getLast();

        for (int i = 1; i < stops.size(); i++) {
            GradientStop candidate = stops.get(i);
            if (candidate.position() >= position) {
                right = candidate;
                left = stops.get(i - 1);
                break;
            }
        }

        double span = right.position() - left.position();
        double t = span <= 0.0
                ? 0.0
                : (position - left.position()) / span;

        return interpolateArgb(left.argb(), right.argb(), t);
    }

    private static int interpolateArgb(
            int first,
            int second,
            double t
    ) {
        int a = channel(
                (first >>> 24) & 0xFF,
                (second >>> 24) & 0xFF,
                t
        );
        int r = channel(
                (first >>> 16) & 0xFF,
                (second >>> 16) & 0xFF,
                t
        );
        int g = channel(
                (first >>> 8) & 0xFF,
                (second >>> 8) & 0xFF,
                t
        );
        int b = channel(
                first & 0xFF,
                second & 0xFF,
                t
        );

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int channel(int first, int second, double t) {
        return Math.max(
                0,
                Math.min(
                        255,
                        (int)Math.round(first + (second - first) * t)
                )
        );
    }
}
