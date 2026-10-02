package dev.loomstudios.project;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

/**
 * Deterministic common-core layer compositor used by runtime compilation and
 * cross-surface authoring operations such as Cape -> Elytra conversion.
 */
public final class CanvasCompositor {
    private CanvasCompositor() {
    }

    public static int[] compile(LoomCanvas canvas) {
        return compile(canvas, false, IntUnaryOperator.identity());
    }

    public static int[] compile(
            LoomCanvas canvas,
            boolean emissiveOnly,
            IntUnaryOperator pixelTransform
    ) {
        Objects.requireNonNull(canvas, "canvas");
        Objects.requireNonNull(pixelTransform, "pixelTransform");

        int[] output = new int[
                Math.multiplyExact(canvas.width(), canvas.height())
        ];

        for (LoomLayer layer : canvas.layers()) {
            if (!layer.visible()
                    || (emissiveOnly && !layer.emissive())) {
                continue;
            }

            int[] raster = LayerRasterizer.rasterize(
                    layer,
                    canvas.width(),
                    canvas.height()
            );

            for (int i = 0; i < output.length; i++) {
                int source = pixelTransform.applyAsInt(raster[i]);
                source = multiplyAlpha(source, layer.opacity());
                output[i] = blend(
                        output[i],
                        source,
                        layer.blendMode()
                );
            }
        }

        return output;
    }

    static int multiplyAlpha(int argb, float opacity) {
        int alpha = (argb >>> 24) & 0xFF;
        int scaled = Math.round(alpha * opacity);
        return (argb & 0x00FFFFFF) | (scaled << 24);
    }

    static int blend(
            int destination,
            int source,
            BlendMode mode
    ) {
        if (mode == BlendMode.NORMAL) {
            return blendNormal(destination, source);
        }

        int sa = (source >>> 24) & 0xFF;
        if (sa == 0) {
            return destination;
        }

        int da = (destination >>> 24) & 0xFF;
        if (da == 0) {
            return source;
        }

        int sr = (source >>> 16) & 0xFF;
        int sg = (source >>> 8) & 0xFF;
        int sb = source & 0xFF;

        int dr = (destination >>> 16) & 0xFF;
        int dg = (destination >>> 8) & 0xFF;
        int db = destination & 0xFF;

        int blendedSource = (sa << 24)
                | (blendChannel(dr, sr, mode) << 16)
                | (blendChannel(dg, sg, mode) << 8)
                | blendChannel(db, sb, mode);

        return blendNormal(destination, blendedSource);
    }

    private static int blendChannel(
            int destination,
            int source,
            BlendMode mode
    ) {
        return switch (mode) {
            case NORMAL -> source;
            case ADD -> Math.min(255, destination + source);
            case SCREEN -> 255
                    - ((255 - destination)
                    * (255 - source) + 127) / 255;
            case MULTIPLY -> (destination * source + 127) / 255;
            case OVERLAY -> destination < 128
                    ? (2 * destination * source + 127) / 255
                    : 255 - (
                            2
                                    * (255 - destination)
                                    * (255 - source)
                                    + 127
                    ) / 255;
        };
    }

    private static int blendNormal(int destination, int source) {
        int sa = (source >>> 24) & 0xFF;
        if (sa == 0) {
            return destination;
        }
        if (sa == 255) {
            return source;
        }

        int da = (destination >>> 24) & 0xFF;
        int outA = sa + ((da * (255 - sa) + 127) / 255);
        if (outA == 0) {
            return 0;
        }

        int sr = (source >>> 16) & 0xFF;
        int sg = (source >>> 8) & 0xFF;
        int sb = source & 0xFF;

        int dr = (destination >>> 16) & 0xFF;
        int dg = (destination >>> 8) & 0xFF;
        int db = destination & 0xFF;

        int dstWeight = (da * (255 - sa) + 127) / 255;

        int outR = (sr * sa + dr * dstWeight) / outA;
        int outG = (sg * sa + dg * dstWeight) / outA;
        int outB = (sb * sa + db * dstWeight) / outA;

        return (outA << 24)
                | (outR << 16)
                | (outG << 8)
                | outB;
    }
}
