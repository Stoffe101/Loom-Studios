package dev.loomstudios.project;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable paint-layer representation for the first real .loom schema.
 *
 * <p>Phase 3 will add more layer types without changing the core rule that
 * editable layers, not flattened textures, are the source of truth.</p>
 */
public record LoomLayer(
        UUID id,
        String name,
        boolean visible,
        float opacity,
        BlendMode blendMode,
        boolean emissive,
        int[] pixels
) {
    public LoomLayer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(blendMode, "blendMode");
        Objects.requireNonNull(pixels, "pixels");

        if (name.isBlank() || name.length() > LoomProjectCodec.MAX_LAYER_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid layer name");
        }

        if (!Float.isFinite(opacity) || opacity < 0.0F || opacity > 1.0F) {
            throw new IllegalArgumentException("Layer opacity out of range");
        }

        pixels = pixels.clone();
    }

    @Override
    public int[] pixels() {
        return pixels.clone();
    }

    public int pixelAt(int index) {
        return pixels[index];
    }

    public int pixelCount() {
        return pixels.length;
    }
}
