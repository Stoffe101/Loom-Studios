package dev.loomstudios.project;

import java.util.ArrayList;
import java.util.List;

public final class ProjectResizer {
    private ProjectResizer() {
    }

    public static LoomProject resizeCape(
            LoomProject project,
            CanvasResolution resolution
    ) {
        LoomCanvas resized = resizeCanvas(project.cape(), resolution);
        return resized.equals(project.cape())
                ? project
                : project.withCape(resized);
    }

    public static LoomProject resizeElytra(
            LoomProject project,
            CanvasResolution resolution
    ) {
        LoomCanvas resized = resizeCanvas(project.elytra(), resolution);
        return resized.equals(project.elytra())
                ? project
                : project.withElytra(resized);
    }

    public static LoomCanvas resizeCanvas(
            LoomCanvas source,
            CanvasResolution resolution
    ) {
        if (source.width() == resolution.width()
                && source.height() == resolution.height()) {
            return source;
        }

        List<LoomLayer> layers = new ArrayList<>(source.layers().size());

        for (LoomLayer layer : source.layers()) {
            if (layer.kind() != LayerKind.PAINT) {
                // Typed layers use normalized placement and therefore retain
                // their authored geometry when the backing canvas scale moves
                // between 1x, 2x and 4x.
                layers.add(layer);
                continue;
            }

            int[] next = new int[resolution.width() * resolution.height()];

            for (int y = 0; y < resolution.height(); y++) {
                int sourceY = y * source.height() / resolution.height();

                for (int x = 0; x < resolution.width(); x++) {
                    int sourceX = x * source.width() / resolution.width();
                    next[y * resolution.width() + x] =
                            layer.pixelAt(sourceY * source.width() + sourceX);
                }
            }

            layers.add(layer.withPixels(next));
        }

        return new LoomCanvas(
                resolution.width(),
                resolution.height(),
                layers
        );
    }
}
