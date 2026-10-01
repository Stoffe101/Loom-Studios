package dev.loomstudios.project;

import java.util.List;
import java.util.Objects;

public record LoomCanvas(
        int width,
        int height,
        List<LoomLayer> layers
) {
    public LoomCanvas {
        if (width <= 0
                || height <= 0
                || width > LoomProjectCodec.MAX_CANVAS_DIMENSION
                || height > LoomProjectCodec.MAX_CANVAS_DIMENSION) {
            throw new IllegalArgumentException("Canvas dimensions out of range");
        }

        Objects.requireNonNull(layers, "layers");
        if (layers.size() > LoomProjectCodec.MAX_LAYER_COUNT) {
            throw new IllegalArgumentException("Too many layers");
        }

        int expectedPixels = Math.multiplyExact(width, height);
        for (LoomLayer layer : layers) {
            Objects.requireNonNull(layer, "layer");
            if (layer.pixelCount() != expectedPixels) {
                throw new IllegalArgumentException(
                        "Layer pixel count does not match canvas dimensions"
                );
            }
        }

        layers = List.copyOf(layers);
    }
}
