package dev.loomstudios.project;

import java.util.Objects;

/**
 * Non-destructive Cape -> Elytra starting conversion.
 *
 * <p>The visible composited Cape Outside/Back face is sampled into the left
 * Elytra front face. The same source is mirrored into the right wing so the
 * unfolded result reads as a symmetric pair in the semantic Elytra editor.</p>
 */
public final class CapeToElytraConversion {
    private CapeToElytraConversion() {
    }

    public static int[] rasterize(LoomProject project) {
        Objects.requireNonNull(project, "project");

        LoomCanvas cape = project.cape();
        LoomCanvas elytra = project.elytra();

        int capeScale = CanvasResolution.fromCanvas(cape).scale();
        int elytraScale = CanvasResolution.fromCanvas(elytra).scale();

        int sourceWidth = CapeUvRegion.OUTSIDE.width(capeScale);
        int sourceHeight = CapeUvRegion.OUTSIDE.height(capeScale);
        int targetWidth = ElytraWing.LEFT.width(elytraScale);
        int targetHeight = ElytraWing.LEFT.height(elytraScale);

        int[] capePixels = CanvasCompositor.compile(cape);
        int[] output = new int[
                Math.multiplyExact(elytra.width(), elytra.height())
        ];

        for (int y = 0; y < targetHeight; y++) {
            int sourceY = Math.min(
                    sourceHeight - 1,
                    y * sourceHeight / targetHeight
            );

            for (int x = 0; x < targetWidth; x++) {
                int sourceX = Math.min(
                        sourceWidth - 1,
                        x * sourceWidth / targetWidth
                );

                int sourceAtlasX = CapeUvRegion.OUTSIDE.atlasX(
                        sourceX,
                        capeScale
                );
                int sourceAtlasY = CapeUvRegion.OUTSIDE.atlasY(
                        sourceY,
                        capeScale
                );
                int color = capePixels[
                        sourceAtlasY * cape.width() + sourceAtlasX
                ];

                write(
                        output,
                        elytra.width(),
                        ElytraWing.LEFT,
                        x,
                        y,
                        elytraScale,
                        color
                );
                write(
                        output,
                        elytra.width(),
                        ElytraWing.RIGHT,
                        ElytraWing.RIGHT.mirroredLocalX(
                                x,
                                elytraScale
                        ),
                        y,
                        elytraScale,
                        color
                );
            }
        }

        return output;
    }

    private static void write(
            int[] target,
            int canvasWidth,
            ElytraWing wing,
            int localX,
            int localY,
            int scale,
            int argb
    ) {
        int atlasX = wing.atlasX(localX, scale);
        int atlasY = wing.atlasY(localY, scale);
        target[atlasY * canvasWidth + atlasX] = argb;
    }
}
