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

    public static LoomProject setCapeRegionPixel(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            int localX,
            int localY,
            int argb
    ) {
        int scale = CanvasResolution.fromCanvas(project.cape()).scale();

        if (localX < 0
                || localY < 0
                || localX >= region.width(scale)
                || localY >= region.height(scale)) {
            return project;
        }

        return setCapePixel(
                project,
                layerId,
                region.atlasX(localX, scale),
                region.atlasY(localY, scale),
                argb
        );
    }

    public static LoomProject paintCapeRegionBrush(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            int localX,
            int localY,
            int brushSize,
            int argb
    ) {
        if (brushSize < 1 || brushSize > 32) {
            throw new IllegalArgumentException("Brush size out of range");
        }

        LoomCanvas canvas = project.cape();
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int regionWidth = region.width(scale);
        int regionHeight = region.height(scale);

        LoomLayer layer = canvas.layers().stream()
                .filter(candidate -> candidate.id().equals(layerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown layer " + layerId
                ));

        int[] pixels = layer.pixels();
        boolean changed = false;
        double radius = Math.max(0.5, brushSize / 2.0);

        int minX = localX - brushSize / 2;
        int minY = localY - brushSize / 2;

        for (int by = 0; by < brushSize; by++) {
            for (int bx = 0; bx < brushSize; bx++) {
                int px = minX + bx;
                int py = minY + by;

                if (px < 0 || py < 0 || px >= regionWidth || py >= regionHeight) {
                    continue;
                }

                double dx = px + 0.5 - (localX + 0.5);
                double dy = py + 0.5 - (localY + 0.5);
                if (brushSize > 2 && dx * dx + dy * dy > radius * radius) {
                    continue;
                }

                int atlasX = region.atlasX(px, scale);
                int atlasY = region.atlasY(py, scale);
                int index = atlasY * canvas.width() + atlasX;

                if (pixels[index] != argb) {
                    pixels[index] = argb;
                    changed = true;
                }
            }
        }

        if (!changed) {
            return project;
        }

        LoomCanvas nextCanvas = canvas.replaceLayer(
                layerId,
                layer.withPixels(pixels)
        );
        return project.withCape(nextCanvas);
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
