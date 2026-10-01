package dev.loomstudios.project;

import java.util.UUID;

public final class ProjectEdits {
    private ProjectEdits() {
    }

    public static LoomProject setCapePixel(
            LoomProject project,
            UUID layerId,
            int x,
            int y,
            int argb
    ) {
        return setPixel(project, true, layerId, x, y, argb);
    }

    public static LoomProject setElytraPixel(
            LoomProject project,
            UUID layerId,
            int x,
            int y,
            int argb
    ) {
        return setPixel(project, false, layerId, x, y, argb);
    }

    private static LoomProject setPixel(
            LoomProject project,
            boolean capeTarget,
            UUID layerId,
            int x,
            int y,
            int argb
    ) {
        LoomCanvas canvas = capeTarget ? project.cape() : project.elytra();

        if (x < 0 || y < 0 || x >= canvas.width() || y >= canvas.height()) {
            return project;
        }

        LoomLayer layer = canvas.layers().stream()
                .filter(candidate -> candidate.id().equals(layerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown layer " + layerId
                ));

        int index = y * canvas.width() + x;
        if (layer.pixelAt(index) == argb) {
            return project;
        }

        int[] pixels = layer.pixels();
        pixels[index] = argb;

        LoomCanvas changed = canvas.replaceLayer(
                layerId,
                layer.withPixels(pixels)
        );

        return capeTarget
                ? project.withCape(changed)
                : project.withElytra(changed);
    }
}
