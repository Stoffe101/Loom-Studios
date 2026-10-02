package dev.loomstudios.client.render;

import dev.loomstudios.project.CanvasCompositor;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.LoomCanvas;

/**
 * Client runtime wrapper around the deterministic common-core compositor.
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
        return CanvasCompositor.compile(
                canvas,
                emissiveOnly,
                source -> animateHue
                        && ((source >>> 24) & 0xFF) != 0
                        ? rotateChannels(source, animationPhase)
                        : source
        );
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
        int[] full = compile(
                canvas,
                animationPhase,
                animateHue,
                emissiveOnly
        );
        int[] output = new int[regionWidth * regionHeight];

        for (int y = 0; y < regionHeight; y++) {
            int atlasY = region.atlasY(y, scale);

            for (int x = 0; x < regionWidth; x++) {
                int atlasX = region.atlasX(x, scale);
                output[y * regionWidth + x] =
                        full[atlasY * canvas.width() + atlasX];
            }
        }

        return output;
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
