package dev.loomstudios.project;

import java.util.Objects;

/**
 * Deterministic cape-to-Elytra starting conversion.
 *
 * <p>The compiled cape Outside face is aspect-fit into the semantic left wing
 * and mirrored into the right wing. Pixels outside the two front faces remain
 * transparent so the result is an editable starting Paint layer instead of a
 * destructive rewrite of the existing Elytra stack.</p>
 */
public final class CapeToElytraConverter {
    private CapeToElytraConverter() {
    }

    public static int[] convertOutsideFace(
            int[] sourcePixels,
            int sourceWidth,
            int sourceHeight,
            LoomCanvas targetElytra
    ) {
        Objects.requireNonNull(sourcePixels, "sourcePixels");
        Objects.requireNonNull(targetElytra, "targetElytra");

        if (sourceWidth <= 0
                || sourceHeight <= 0
                || sourcePixels.length
                != Math.multiplyExact(sourceWidth, sourceHeight)) {
            throw new IllegalArgumentException(
                    "Invalid compiled cape face dimensions"
            );
        }

        int scale = CanvasResolution.fromCanvas(targetElytra).scale();
        int wingWidth = ElytraWing.LEFT.width(scale);
        int wingHeight = ElytraWing.LEFT.height(scale);

        double fit = Math.min(
                wingWidth / (double)sourceWidth,
                wingHeight / (double)sourceHeight
        );
        int drawWidth = Math.max(
                1,
                Math.min(
                        wingWidth,
                        (int)Math.round(sourceWidth * fit)
                )
        );
        int drawHeight = Math.max(
                1,
                Math.min(
                        wingHeight,
                        (int)Math.round(sourceHeight * fit)
                )
        );

        int offsetX = (wingWidth - drawWidth) / 2;
        int offsetY = (wingHeight - drawHeight) / 2;

        int[] output = new int[
                targetElytra.width() * targetElytra.height()
        ];

        for (int y = 0; y < drawHeight; y++) {
            int sourceY = Math.min(
                    sourceHeight - 1,
                    y * sourceHeight / drawHeight
            );

            for (int x = 0; x < drawWidth; x++) {
                int sourceX = Math.min(
                        sourceWidth - 1,
                        x * sourceWidth / drawWidth
                );
                int color = sourcePixels[
                        sourceY * sourceWidth + sourceX
                ];

                int leftX = offsetX + x;
                int localY = offsetY + y;
                write(
                        output,
                        targetElytra.width(),
                        ElytraWing.LEFT,
                        leftX,
                        localY,
                        scale,
                        color
                );

                write(
                        output,
                        targetElytra.width(),
                        ElytraWing.RIGHT,
                        ElytraWing.RIGHT.mirroredLocalX(
                                leftX,
                                scale
                        ),
                        localY,
                        scale,
                        color
                );
            }
        }

        return output;
    }

    private static void write(
            int[] output,
            int canvasWidth,
            ElytraWing wing,
            int localX,
            int localY,
            int scale,
            int color
    ) {
        int atlasX = wing.atlasX(localX, scale);
        int atlasY = wing.atlasY(localY, scale);
        output[atlasY * canvasWidth + atlasX] = color;
    }
}
