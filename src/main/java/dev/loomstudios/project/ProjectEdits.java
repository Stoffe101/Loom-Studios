package dev.loomstudios.project;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProjectEdits {
    private ProjectEdits() {
    }

    public static LoomProject addCapeLayer(
            LoomProject project,
            String name
    ) {
        LoomCanvas canvas = project.cape();

        if (canvas.layers().size() >= LoomProjectCodec.MAX_LAYER_COUNT) {
            throw new IllegalStateException("Cape already has the maximum layer count");
        }

        int[] transparent = new int[canvas.width() * canvas.height()];
        LoomLayer layer = new LoomLayer(
                UUID.randomUUID(),
                uniqueLayerName(canvas.layers(), name),
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                transparent
        );

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        next.add(layer);

        return project.withCape(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject addCapeImageLayer(
            LoomProject project,
            String name,
            ImageLayerData data
    ) {
        LoomCanvas canvas = project.cape();
        ensureLayerCapacity(canvas);

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        next.add(LoomLayer.image(
                UUID.randomUUID(),
                uniqueLayerName(canvas.layers(), name),
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                java.util.Objects.requireNonNull(data, "data")
        ));

        return project.withCape(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject addCapeGradientLayer(
            LoomProject project,
            String name,
            GradientLayerData data
    ) {
        LoomCanvas canvas = project.cape();
        ensureLayerCapacity(canvas);

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        next.add(LoomLayer.gradient(
                UUID.randomUUID(),
                uniqueLayerName(canvas.layers(), name),
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                java.util.Objects.requireNonNull(data, "data")
        ));

        return project.withCape(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject duplicateCapeLayer(
            LoomProject project,
            UUID layerId
    ) {
        LoomCanvas canvas = project.cape();

        if (canvas.layers().size() >= LoomProjectCodec.MAX_LAYER_COUNT) {
            throw new IllegalStateException("Cape already has the maximum layer count");
        }

        List<LoomLayer> next = new ArrayList<>(canvas.layers());

        for (int i = 0; i < next.size(); i++) {
            LoomLayer source = next.get(i);
            if (!source.id().equals(layerId)) {
                continue;
            }

            LoomLayer copy = source.duplicate(
                    UUID.randomUUID(),
                    uniqueLayerName(
                            next,
                            source.name() + " Copy"
                    )
            );
            next.add(i + 1, copy);

            return project.withCape(
                    new LoomCanvas(canvas.width(), canvas.height(), next)
            );
        }

        throw new IllegalArgumentException("Unknown layer " + layerId);
    }

    public static LoomProject removeCapeLayer(
            LoomProject project,
            UUID layerId
    ) {
        LoomCanvas canvas = project.cape();

        if (canvas.layers().size() <= 1) {
            return project;
        }

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        boolean removed = next.removeIf(layer -> layer.id().equals(layerId));

        if (!removed) {
            throw new IllegalArgumentException("Unknown layer " + layerId);
        }

        return project.withCape(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject moveCapeLayer(
            LoomProject project,
            UUID layerId,
            int delta
    ) {
        if (delta == 0) {
            return project;
        }

        LoomCanvas canvas = project.cape();
        List<LoomLayer> next = new ArrayList<>(canvas.layers());

        for (int i = 0; i < next.size(); i++) {
            if (!next.get(i).id().equals(layerId)) {
                continue;
            }

            int target = Math.max(
                    0,
                    Math.min(next.size() - 1, i + delta)
            );

            if (target == i) {
                return project;
            }

            LoomLayer layer = next.remove(i);
            next.add(target, layer);

            return project.withCape(
                    new LoomCanvas(canvas.width(), canvas.height(), next)
            );
        }

        throw new IllegalArgumentException("Unknown layer " + layerId);
    }

    public static LoomProject setCapeLayerVisible(
            LoomProject project,
            UUID layerId,
            boolean visible
    ) {
        return updateCapeLayer(
                project,
                layerId,
                layer -> layer.withVisible(visible)
        );
    }

    public static LoomProject setCapeLayerOpacity(
            LoomProject project,
            UUID layerId,
            float opacity
    ) {
        float clamped = Math.max(0.0F, Math.min(1.0F, opacity));
        return updateCapeLayer(
                project,
                layerId,
                layer -> layer.withOpacity(clamped)
        );
    }

    public static LoomProject renameCapeLayer(
            LoomProject project,
            UUID layerId,
            String name
    ) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()
                || normalized.length() > LoomProjectCodec.MAX_LAYER_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid layer name");
        }

        return updateCapeLayer(
                project,
                layerId,
                layer -> layer.withName(normalized)
        );
    }

    public static LoomProject setCapeLayerEmissive(
            LoomProject project,
            UUID layerId,
            boolean emissive
    ) {
        LoomProject updated = updateCapeLayer(
                project,
                layerId,
                layer -> layer.withEmissive(emissive)
        );

        boolean hasEmissiveLayer = updated.cape().layers().stream()
                .anyMatch(LoomLayer::emissive);

        if (updated.runtime().emissiveEnabled() == hasEmissiveLayer) {
            return updated;
        }

        return updated.withRuntime(
                updated.runtime().withEmissiveEnabled(hasEmissiveLayer)
        );
    }

    public static LoomProject setCapeLayerLocked(
            LoomProject project,
            UUID layerId,
            boolean locked
    ) {
        return updateCapeLayer(
                project,
                layerId,
                layer -> layer.withLocked(locked)
        );
    }

    public static LoomProject setCapeImageData(
            LoomProject project,
            UUID layerId,
            ImageLayerData data
    ) {
        return updateCapeLayer(
                project,
                layerId,
                layer -> {
                    requireUnlockedKind(layer, LayerKind.IMAGE);
                    return layer.withImageData(
                            java.util.Objects.requireNonNull(data, "data")
                    );
                }
        );
    }

    public static LoomProject setCapeGradientData(
            LoomProject project,
            UUID layerId,
            GradientLayerData data
    ) {
        return updateCapeLayer(
                project,
                layerId,
                layer -> {
                    requireUnlockedKind(layer, LayerKind.GRADIENT);
                    return layer.withGradientData(
                            java.util.Objects.requireNonNull(data, "data")
                    );
                }
        );
    }

    public static LoomProject setCapeLayerBlendMode(
            LoomProject project,
            UUID layerId,
            BlendMode blendMode
    ) {
        return updateCapeLayer(
                project,
                layerId,
                layer -> layer.withBlendMode(blendMode)
        );
    }

    private static LoomProject updateCapeLayer(
            LoomProject project,
            UUID layerId,
            java.util.function.UnaryOperator<LoomLayer> edit
    ) {
        LoomCanvas canvas = project.cape();
        LoomLayer current = canvas.layers().stream()
                .filter(layer -> layer.id().equals(layerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown layer " + layerId
                ));

        LoomLayer replacement = edit.apply(current);
        if (replacement.equals(current)) {
            return project;
        }

        return project.withCape(
                canvas.replaceLayer(layerId, replacement)
        );
    }

    private static void ensureLayerCapacity(
            LoomCanvas canvas
    ) {
        if (canvas.layers().size() >= LoomProjectCodec.MAX_LAYER_COUNT) {
            throw new IllegalStateException(
                    "Cape already has the maximum layer count"
            );
        }
    }

    private static void requireUnlockedKind(
            LoomLayer layer,
            LayerKind expectedKind
    ) {
        if (layer.kind() != expectedKind) {
            throw new IllegalStateException(
                    "Expected "
                            + expectedKind.displayName()
                            + " layer"
            );
        }
        if (layer.locked()) {
            throw new IllegalStateException("Layer is locked");
        }
    }

    private static LoomLayer requireEditablePaintLayer(
            LoomCanvas canvas,
            UUID layerId
    ) {
        LoomLayer layer = canvas.layers().stream()
                .filter(candidate -> candidate.id().equals(layerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown layer " + layerId
                ));

        if (layer.kind() != LayerKind.PAINT) {
            throw new IllegalStateException(
                    "Paint tools require a paint layer"
            );
        }

        if (layer.locked()) {
            throw new IllegalStateException("Layer is locked");
        }

        return layer;
    }

    private static String uniqueLayerName(
            List<LoomLayer> layers,
            String requested
    ) {
        String base = requested == null || requested.isBlank()
                ? "Layer"
                : requested.trim();

        if (base.length() > LoomProjectCodec.MAX_LAYER_NAME_CHARS) {
            base = base.substring(0, LoomProjectCodec.MAX_LAYER_NAME_CHARS);
        }

        String candidate = base;
        int suffix = 2;

        while (containsLayerName(layers, candidate)) {
            String suffixText = " " + suffix++;
            int maxBase = Math.max(
                    1,
                    LoomProjectCodec.MAX_LAYER_NAME_CHARS
                            - suffixText.length()
            );
            String shortened = base.length() > maxBase
                    ? base.substring(0, maxBase)
                    : base;
            candidate = shortened + suffixText;
        }

        return candidate;
    }

    private static boolean containsLayerName(
            List<LoomLayer> layers,
            String name
    ) {
        return layers.stream()
                .anyMatch(layer -> layer.name().equalsIgnoreCase(name));
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

        LoomLayer layer = requireEditablePaintLayer(
                canvas,
                layerId
        );

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

    public static LoomProject moveCapeRegionSelection(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            PixelSelection selection,
            int deltaX,
            int deltaY
    ) {
        if (deltaX == 0 && deltaY == 0) {
            return project;
        }

        LoomCanvas canvas = project.cape();
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int regionWidth = region.width(scale);
        int regionHeight = region.height(scale);
        validateSelection(selection, regionWidth, regionHeight);

        LoomLayer layer = requireEditablePaintLayer(
                canvas,
                layerId
        );

        int[] pixels = layer.pixels();
        int[] captured = new int[selection.width() * selection.height()];

        for (int y = 0; y < selection.height(); y++) {
            for (int x = 0; x < selection.width(); x++) {
                int sourceX = selection.minX() + x;
                int sourceY = selection.minY() + y;
                int atlasX = region.atlasX(sourceX, scale);
                int atlasY = region.atlasY(sourceY, scale);

                captured[y * selection.width() + x] =
                        pixels[atlasY * canvas.width() + atlasX];
                pixels[atlasY * canvas.width() + atlasX] = 0;
            }
        }

        for (int y = 0; y < selection.height(); y++) {
            for (int x = 0; x < selection.width(); x++) {
                int destinationX = selection.minX() + x + deltaX;
                int destinationY = selection.minY() + y + deltaY;

                if (destinationX < 0
                        || destinationY < 0
                        || destinationX >= regionWidth
                        || destinationY >= regionHeight) {
                    continue;
                }

                int atlasX = region.atlasX(destinationX, scale);
                int atlasY = region.atlasY(destinationY, scale);
                pixels[atlasY * canvas.width() + atlasX] =
                        captured[y * selection.width() + x];
            }
        }

        return project.withCape(
                canvas.replaceLayer(
                        layerId,
                        layer.withPixels(pixels)
                )
        );
    }

    public static LoomProject flipCapeRegionSelection(
            LoomProject project,
            UUID layerId,
            CapeUvRegion region,
            PixelSelection selection,
            boolean horizontal,
            boolean vertical
    ) {
        if (!horizontal && !vertical) {
            return project;
        }

        LoomCanvas canvas = project.cape();
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int regionWidth = region.width(scale);
        int regionHeight = region.height(scale);
        validateSelection(selection, regionWidth, regionHeight);

        LoomLayer layer = requireEditablePaintLayer(
                canvas,
                layerId
        );

        int[] pixels = layer.pixels();
        int[] captured = new int[selection.width() * selection.height()];

        for (int y = 0; y < selection.height(); y++) {
            for (int x = 0; x < selection.width(); x++) {
                int sourceX = selection.minX() + x;
                int sourceY = selection.minY() + y;
                int atlasX = region.atlasX(sourceX, scale);
                int atlasY = region.atlasY(sourceY, scale);
                captured[y * selection.width() + x] =
                        pixels[atlasY * canvas.width() + atlasX];
            }
        }

        for (int y = 0; y < selection.height(); y++) {
            for (int x = 0; x < selection.width(); x++) {
                int sourceX = horizontal
                        ? selection.width() - 1 - x
                        : x;
                int sourceY = vertical
                        ? selection.height() - 1 - y
                        : y;

                int destinationX = selection.minX() + x;
                int destinationY = selection.minY() + y;
                int atlasX = region.atlasX(destinationX, scale);
                int atlasY = region.atlasY(destinationY, scale);

                pixels[atlasY * canvas.width() + atlasX] =
                        captured[
                                sourceY * selection.width()
                                        + sourceX
                        ];
            }
        }

        return project.withCape(
                canvas.replaceLayer(
                        layerId,
                        layer.withPixels(pixels)
                )
        );
    }

    private static void validateSelection(
            PixelSelection selection,
            int regionWidth,
            int regionHeight
    ) {
        if (selection == null
                || selection.maxX() >= regionWidth
                || selection.maxY() >= regionHeight) {
            throw new IllegalArgumentException(
                    "Selection is outside semantic region"
            );
        }
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

        LoomLayer layer = requireEditablePaintLayer(
                canvas,
                layerId
        );

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

        LoomLayer layer = requireEditablePaintLayer(
                canvas,
                layerId
        );

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

    public static LoomProject addElytraLayer(
            LoomProject project,
            String name
    ) {
        LoomCanvas canvas = project.elytra();
        ensureLayerCapacity(canvas);

        int[] transparent = new int[canvas.width() * canvas.height()];
        LoomLayer layer = LoomLayer.paint(
                UUID.randomUUID(),
                uniqueLayerName(canvas.layers(), name),
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                transparent
        );

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        next.add(layer);

        return project.withElytra(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject addElytraPaintLayer(
            LoomProject project,
            String name,
            int[] pixels
    ) {
        LoomCanvas canvas = project.elytra();
        ensureLayerCapacity(canvas);

        if (pixels == null
                || pixels.length != canvas.width() * canvas.height()) {
            throw new IllegalArgumentException(
                    "Elytra paint layer pixel count does not match canvas"
            );
        }

        LoomLayer layer = LoomLayer.paint(
                UUID.randomUUID(),
                uniqueLayerName(canvas.layers(), name),
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                pixels
        );

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        next.add(layer);

        return project.withElytra(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject addElytraImageLayer(
            LoomProject project,
            String name,
            ImageLayerData data
    ) {
        LoomCanvas canvas = project.elytra();
        ensureLayerCapacity(canvas);

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        next.add(LoomLayer.image(
                UUID.randomUUID(),
                uniqueLayerName(canvas.layers(), name),
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                java.util.Objects.requireNonNull(data, "data")
        ));

        return project.withElytra(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject setElytraImageData(
            LoomProject project,
            UUID layerId,
            ImageLayerData data
    ) {
        return updateElytraLayer(
                project,
                layerId,
                layer -> {
                    requireUnlockedKind(layer, LayerKind.IMAGE);
                    return layer.withImageData(
                            java.util.Objects.requireNonNull(data, "data")
                    );
                }
        );
    }

    public static LoomProject duplicateElytraLayer(
            LoomProject project,
            UUID layerId
    ) {
        LoomCanvas canvas = project.elytra();
        ensureLayerCapacity(canvas);
        List<LoomLayer> next = new ArrayList<>(canvas.layers());

        for (int i = 0; i < next.size(); i++) {
            LoomLayer source = next.get(i);
            if (!source.id().equals(layerId)) {
                continue;
            }

            LoomLayer copy = source.duplicate(
                    UUID.randomUUID(),
                    uniqueLayerName(next, source.name() + " Copy")
            );
            next.add(i + 1, copy);

            return project.withElytra(
                    new LoomCanvas(canvas.width(), canvas.height(), next)
            );
        }

        throw new IllegalArgumentException(
                "Unknown Elytra layer " + layerId
        );
    }

    public static LoomProject removeElytraLayer(
            LoomProject project,
            UUID layerId
    ) {
        LoomCanvas canvas = project.elytra();
        if (canvas.layers().size() <= 1) {
            return project;
        }

        List<LoomLayer> next = new ArrayList<>(canvas.layers());
        boolean removed = next.removeIf(layer -> layer.id().equals(layerId));
        if (!removed) {
            throw new IllegalArgumentException(
                    "Unknown Elytra layer " + layerId
            );
        }

        return project.withElytra(
                new LoomCanvas(canvas.width(), canvas.height(), next)
        );
    }

    public static LoomProject moveElytraLayer(
            LoomProject project,
            UUID layerId,
            int delta
    ) {
        if (delta == 0) {
            return project;
        }

        LoomCanvas canvas = project.elytra();
        List<LoomLayer> next = new ArrayList<>(canvas.layers());

        for (int i = 0; i < next.size(); i++) {
            if (!next.get(i).id().equals(layerId)) {
                continue;
            }

            int target = Math.max(
                    0,
                    Math.min(next.size() - 1, i + delta)
            );
            if (target == i) {
                return project;
            }

            LoomLayer layer = next.remove(i);
            next.add(target, layer);
            return project.withElytra(
                    new LoomCanvas(canvas.width(), canvas.height(), next)
            );
        }

        throw new IllegalArgumentException(
                "Unknown Elytra layer " + layerId
        );
    }

    public static LoomProject setElytraLayerVisible(
            LoomProject project,
            UUID layerId,
            boolean visible
    ) {
        return updateElytraLayer(
                project,
                layerId,
                layer -> layer.withVisible(visible)
        );
    }

    public static LoomProject setElytraLayerOpacity(
            LoomProject project,
            UUID layerId,
            float opacity
    ) {
        float clamped = Math.max(0.0F, Math.min(1.0F, opacity));
        return updateElytraLayer(
                project,
                layerId,
                layer -> layer.withOpacity(clamped)
        );
    }

    public static LoomProject renameElytraLayer(
            LoomProject project,
            UUID layerId,
            String name
    ) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()
                || normalized.length()
                > LoomProjectCodec.MAX_LAYER_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid layer name");
        }

        return updateElytraLayer(
                project,
                layerId,
                layer -> layer.withName(normalized)
        );
    }

    public static LoomProject setElytraLayerBlendMode(
            LoomProject project,
            UUID layerId,
            BlendMode blendMode
    ) {
        return updateElytraLayer(
                project,
                layerId,
                layer -> layer.withBlendMode(blendMode)
        );
    }

    public static LoomProject setElytraLayerLocked(
            LoomProject project,
            UUID layerId,
            boolean locked
    ) {
        return updateElytraLayer(
                project,
                layerId,
                layer -> layer.withLocked(locked)
        );
    }

    private static LoomProject updateElytraLayer(
            LoomProject project,
            UUID layerId,
            java.util.function.UnaryOperator<LoomLayer> edit
    ) {
        LoomCanvas canvas = project.elytra();
        LoomLayer current = canvas.layers().stream()
                .filter(layer -> layer.id().equals(layerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown Elytra layer " + layerId
                ));

        LoomLayer replacement = edit.apply(current);
        if (replacement.equals(current)) {
            return project;
        }

        return project.withElytra(
                canvas.replaceLayer(layerId, replacement)
        );
    }

    public static LoomProject setElytraWingPixel(
            LoomProject project,
            UUID layerId,
            ElytraWing wing,
            int localX,
            int localY,
            int argb,
            boolean linkedMirror
    ) {
        LoomCanvas canvas = project.elytra();
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int width = wing.width(scale);
        int height = wing.height(scale);

        if (localX < 0
                || localY < 0
                || localX >= width
                || localY >= height) {
            return project;
        }

        LoomLayer layer = requireEditablePaintLayer(canvas, layerId);
        int[] pixels = layer.pixels();
        boolean changed = writeElytraWingPixel(
                pixels,
                canvas.width(),
                wing,
                localX,
                localY,
                scale,
                argb
        );

        if (linkedMirror) {
            ElytraWing opposite = wing.opposite();
            changed |= writeElytraWingPixel(
                    pixels,
                    canvas.width(),
                    opposite,
                    opposite.mirroredLocalX(localX, scale),
                    localY,
                    scale,
                    argb
            );
        }

        if (!changed) {
            return project;
        }

        return project.withElytra(
                canvas.replaceLayer(
                        layerId,
                        layer.withPixels(pixels)
                )
        );
    }

    public static LoomProject paintElytraWingBrush(
            LoomProject project,
            UUID layerId,
            ElytraWing wing,
            int localX,
            int localY,
            int brushSize,
            int argb,
            boolean linkedMirror
    ) {
        if (brushSize < 1 || brushSize > 32) {
            throw new IllegalArgumentException("Brush size out of range");
        }

        LoomCanvas canvas = project.elytra();
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int width = wing.width(scale);
        int height = wing.height(scale);

        LoomLayer layer = requireEditablePaintLayer(canvas, layerId);
        int[] pixels = layer.pixels();
        boolean changed = false;
        double radius = Math.max(0.5, brushSize / 2.0);
        int minX = localX - brushSize / 2;
        int minY = localY - brushSize / 2;

        for (int by = 0; by < brushSize; by++) {
            for (int bx = 0; bx < brushSize; bx++) {
                int px = minX + bx;
                int py = minY + by;

                if (px < 0 || py < 0 || px >= width || py >= height) {
                    continue;
                }

                double dx = px + 0.5 - (localX + 0.5);
                double dy = py + 0.5 - (localY + 0.5);
                if (brushSize > 2
                        && dx * dx + dy * dy > radius * radius) {
                    continue;
                }

                changed |= writeElytraWingPixel(
                        pixels,
                        canvas.width(),
                        wing,
                        px,
                        py,
                        scale,
                        argb
                );

                if (linkedMirror) {
                    ElytraWing opposite = wing.opposite();
                    changed |= writeElytraWingPixel(
                            pixels,
                            canvas.width(),
                            opposite,
                            opposite.mirroredLocalX(px, scale),
                            py,
                            scale,
                            argb
                    );
                }
            }
        }

        if (!changed) {
            return project;
        }

        return project.withElytra(
                canvas.replaceLayer(
                        layerId,
                        layer.withPixels(pixels)
                )
        );
    }

    private static boolean writeElytraWingPixel(
            int[] pixels,
            int canvasWidth,
            ElytraWing wing,
            int localX,
            int localY,
            int scale,
            int argb
    ) {
        int atlasX = wing.atlasX(localX, scale);
        int atlasY = wing.atlasY(localY, scale);
        int index = atlasY * canvasWidth + atlasX;

        if (pixels[index] == argb) {
            return false;
        }

        pixels[index] = argb;
        return true;
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

        LoomLayer layer = requireEditablePaintLayer(
                canvas,
                layerId
        );

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
