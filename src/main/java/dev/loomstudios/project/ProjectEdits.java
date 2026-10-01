package dev.loomstudios.project;

import java.util.ArrayDeque;
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

    public static LoomProject paintCapeRegionLine(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            int startX,
            int startY,
            int endX,
            int endY,
            int brushSize,
            int argb
    ) {
        return paintCapeRegionShape(
                project,
                layerId,
                region,
                brushSize,
                argb,
                (pixels, canvas, scale, regionWidth, regionHeight) -> {
                    int x0 = startX;
                    int y0 = startY;
                    int x1 = endX;
                    int y1 = endY;
                    int dx = Math.abs(x1 - x0);
                    int sx = x0 < x1 ? 1 : -1;
                    int dy = -Math.abs(y1 - y0);
                    int sy = y0 < y1 ? 1 : -1;
                    int error = dx + dy;

                    while (true) {
                        paintBrushInto(
                                pixels,
                                canvas,
                                region,
                                scale,
                                regionWidth,
                                regionHeight,
                                x0,
                                y0,
                                brushSize,
                                argb
                        );

                        if (x0 == x1 && y0 == y1) {
                            break;
                        }

                        int doubled = 2 * error;
                        if (doubled >= dy) {
                            error += dy;
                            x0 += sx;
                        }
                        if (doubled <= dx) {
                            error += dx;
                            y0 += sy;
                        }
                    }
                }
        );
    }

    public static LoomProject paintCapeRegionRectangle(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            int startX,
            int startY,
            int endX,
            int endY,
            int brushSize,
            int argb,
            boolean filled
    ) {
        return paintCapeRegionShape(
                project,
                layerId,
                region,
                brushSize,
                argb,
                (pixels, canvas, scale, regionWidth, regionHeight) -> {
                    int minX = Math.min(startX, endX);
                    int maxX = Math.max(startX, endX);
                    int minY = Math.min(startY, endY);
                    int maxY = Math.max(startY, endY);

                    if (filled) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int x = minX; x <= maxX; x++) {
                                paintBrushInto(
                                        pixels,
                                        canvas,
                                        region,
                                        scale,
                                        regionWidth,
                                        regionHeight,
                                        x,
                                        y,
                                        1,
                                        argb
                                );
                            }
                        }
                        return;
                    }

                    for (int x = minX; x <= maxX; x++) {
                        paintBrushInto(
                                pixels, canvas, region, scale,
                                regionWidth, regionHeight,
                                x, minY, brushSize, argb
                        );
                        paintBrushInto(
                                pixels, canvas, region, scale,
                                regionWidth, regionHeight,
                                x, maxY, brushSize, argb
                        );
                    }

                    for (int y = minY; y <= maxY; y++) {
                        paintBrushInto(
                                pixels, canvas, region, scale,
                                regionWidth, regionHeight,
                                minX, y, brushSize, argb
                        );
                        paintBrushInto(
                                pixels, canvas, region, scale,
                                regionWidth, regionHeight,
                                maxX, y, brushSize, argb
                        );
                    }
                }
        );
    }

    private static LoomProject paintCapeRegionShape(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            int brushSize,
            int argb,
            RegionPainter painter
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
        int[] before = pixels.clone();

        painter.paint(
                pixels,
                canvas,
                scale,
                regionWidth,
                regionHeight
        );

        if (java.util.Arrays.equals(before, pixels)) {
            return project;
        }

        LoomCanvas nextCanvas = canvas.replaceLayer(
                layerId,
                layer.withPixels(pixels)
        );
        return project.withCape(nextCanvas);
    }

    private static void paintBrushInto(
            int[] pixels,
            LoomCanvas canvas,
            CapeUvRegion region,
            int scale,
            int regionWidth,
            int regionHeight,
            int localX,
            int localY,
            int brushSize,
            int argb
    ) {
        double radius = Math.max(0.5, brushSize / 2.0);
        int minX = localX - brushSize / 2;
        int minY = localY - brushSize / 2;

        for (int by = 0; by < brushSize; by++) {
            for (int bx = 0; bx < brushSize; bx++) {
                int px = minX + bx;
                int py = minY + by;

                if (px < 0
                        || py < 0
                        || px >= regionWidth
                        || py >= regionHeight) {
                    continue;
                }

                double dx = px + 0.5 - (localX + 0.5);
                double dy = py + 0.5 - (localY + 0.5);
                if (brushSize > 2 && dx * dx + dy * dy > radius * radius) {
                    continue;
                }

                int atlasX = region.atlasX(px, scale);
                int atlasY = region.atlasY(py, scale);
                pixels[atlasY * canvas.width() + atlasX] = argb;
            }
        }
    }

    @FunctionalInterface
    private interface RegionPainter {
        void paint(
                int[] pixels,
                LoomCanvas canvas,
                int scale,
                int regionWidth,
                int regionHeight
        );
    }

    public static LoomProject floodFillCapeRegion(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            int localX,
            int localY,
            int argb
    ) {
        LoomCanvas canvas = project.cape();
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int regionWidth = region.width(scale);
        int regionHeight = region.height(scale);

        if (localX < 0
                || localY < 0
                || localX >= regionWidth
                || localY >= regionHeight) {
            return project;
        }

        LoomLayer layer = canvas.layers().stream()
                .filter(candidate -> candidate.id().equals(layerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown layer " + layerId
                ));

        int startAtlasX = region.atlasX(localX, scale);
        int startAtlasY = region.atlasY(localY, scale);
        int startIndex = startAtlasY * canvas.width() + startAtlasX;
        int target = layer.pixelAt(startIndex);

        if (target == argb) {
            return project;
        }

        int[] pixels = layer.pixels();
        boolean[] visited = new boolean[regionWidth * regionHeight];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(localY * regionWidth + localX);

        boolean changed = false;

        while (!queue.isEmpty()) {
            int localIndex = queue.removeFirst();
            if (visited[localIndex]) {
                continue;
            }
            visited[localIndex] = true;

            int x = localIndex % regionWidth;
            int y = localIndex / regionWidth;
            int atlasX = region.atlasX(x, scale);
            int atlasY = region.atlasY(y, scale);
            int atlasIndex = atlasY * canvas.width() + atlasX;

            if (pixels[atlasIndex] != target) {
                continue;
            }

            pixels[atlasIndex] = argb;
            changed = true;

            if (x > 0) {
                queue.add(localIndex - 1);
            }
            if (x + 1 < regionWidth) {
                queue.add(localIndex + 1);
            }
            if (y > 0) {
                queue.add(localIndex - regionWidth);
            }
            if (y + 1 < regionHeight) {
                queue.add(localIndex + regionWidth);
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
