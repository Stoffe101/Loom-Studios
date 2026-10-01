package dev.loomstudios.client.render;

import dev.loomstudios.project.BlendMode;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomCanvas;
import dev.loomstudios.project.LoomLayer;

/**
 * Minimal non-destructive canvas compiler for Phase 1.
 */
public final class LoomTextureCompiler {
    private LoomTextureCompiler() {
    }

    public static int[] compile(
            LoomCanvas canvas,
            int animationPhase,
            boolean animateHue,
            boolean emissiveOnly
    ) {
        int[] output = new int[Math.multiplyExact(canvas.width(), canvas.height())];

        for (LoomLayer layer : canvas.layers()) {
            if (!layer.visible() || (emissiveOnly && !layer.emissive())) {
                continue;
            }

            for (int i = 0; i < output.length; i++) {
                int source = layer.pixelAt(i);

                if (animateHue && ((source >>> 24) & 0xFF) != 0) {
                    source = rotateChannels(source, animationPhase);
                }

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

    public static int[] compileCapeRegion(
            LoomCanvas canvas,
            CapeUvRegion region,
            int animationPhase,
            boolean animateHue,
            boolean emissiveOnly
    ) {
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int regionWidth = region.width(scale);
        int regionHeight = region.height(scale);
        int[] output = new int[regionWidth * regionHeight];

        for (LoomLayer layer : canvas.layers()) {
            if (!layer.visible() || (emissiveOnly && !layer.emissive())) {
                continue;
            }

            for (int y = 0; y < regionHeight; y++) {
                int atlasY = region.atlasY(y, scale);

                for (int x = 0; x < regionWidth; x++) {
                    int atlasX = region.atlasX(x, scale);
                    int source = layer.pixelAt(
                            atlasY * canvas.width() + atlasX
                    );

                    if (animateHue && ((source >>> 24) & 0xFF) != 0) {
                        source = rotateChannels(source, animationPhase);
                    }

                    source = multiplyAlpha(source, layer.opacity());
                    int index = y * regionWidth + x;
                    output[index] = blend(
                            output[index],
                            source,
                            layer.blendMode()
                    );
                }
            }
        }

        return output;
    }

    private static int multiplyAlpha(int argb, float opacity) {
        int alpha = (argb >>> 24) & 0xFF;
        int scaled = Math.round(alpha * opacity);
        return (argb & 0x00FFFFFF) | (scaled << 24);
    }

    private static int blend(
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

        int br = blendChannel(dr, sr, mode);
        int bg = blendChannel(dg, sg, mode);
        int bb = blendChannel(db, sb, mode);

        int blendedSource = (sa << 24)
                | (br << 16)
                | (bg << 8)
                | bb;

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
                    - ((255 - destination) * (255 - source) + 127) / 255;
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

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private static int rotateChannels(int argb, int phase) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        return switch (Math.floorMod(phase, 4)) {
            case 1 -> (a << 24) | (b << 16) | (r << 8) | g;
            case 2 -> (a << 24) | (g << 16) | (b << 8) | r;
            case 3 -> (a << 24)
                    | ((255 - r) << 16)
                    | ((255 - g) << 8)
                    | (255 - b);
            default -> argb;
        };
    }
}
