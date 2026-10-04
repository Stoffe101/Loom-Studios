package dev.loomstudios.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
            throw new IllegalArgumentException(
                    "Canvas dimensions out of range"
            );
        }

        Objects.requireNonNull(layers, "layers");
        if (layers.size() > LoomProjectCodec.MAX_LAYER_COUNT) {
            throw new IllegalArgumentException("Too many layers");
        }

        int expectedPixels = Math.multiplyExact(width, height);

        for (LoomLayer layer : layers) {
            Objects.requireNonNull(layer, "layer");

            if (layer.kind() == LayerKind.PAINT
                    && layer.pixelCount() != expectedPixels) {
                throw new IllegalArgumentException(
                        "Paint layer pixel count does not match canvas dimensions"
                );
            }
        }

        for(LoomLayer layer:layers)if(layer.maskLength()!=0&&layer.maskLength()!=width*height)throw new IllegalArgumentException("Layer mask size mismatch");
        layers = List.copyOf(layers);
    }

    public LoomCanvas replaceLayer(
            UUID layerId,
            LoomLayer replacement
    ) {
        Objects.requireNonNull(layerId, "layerId");
        Objects.requireNonNull(replacement, "replacement");

        List<LoomLayer> next = new ArrayList<>(layers);
        boolean replaced = false;

        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).id().equals(layerId)) {
                next.set(i, replacement);
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            throw new IllegalArgumentException(
                    "Unknown layer " + layerId
            );
        }

        return new LoomCanvas(width, height, next);
    }
}
