package dev.loomstudios.project;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable typed layer used by schema v2.
 *
 * <p>The legacy seven-argument constructor remains source-compatible with
 * schema-v1 paint-layer call sites and creates an unlocked paint layer.</p>
 */
public record LoomLayer(
        UUID id,
        String name,
        boolean visible,
        float opacity,
        BlendMode blendMode,
        boolean emissive,
        boolean locked,
        LayerKind kind,
        int[] pixels,
        ImageLayerData imageData,
        GradientLayerData gradientData
) {
    public LoomLayer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(blendMode, "blendMode");
        Objects.requireNonNull(kind, "kind");

        if (name.isBlank()
                || name.length() > LoomProjectCodec.MAX_LAYER_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid layer name");
        }

        if (!Float.isFinite(opacity)
                || opacity < 0.0F
                || opacity > 1.0F) {
            throw new IllegalArgumentException(
                    "Layer opacity out of range"
            );
        }

        switch (kind) {
            case PAINT -> {
                Objects.requireNonNull(pixels, "pixels");
                if (imageData != null || gradientData != null) {
                    throw new IllegalArgumentException(
                            "Paint layer cannot contain typed payloads"
                    );
                }
                pixels = pixels.clone();
            }
            case IMAGE -> {
                Objects.requireNonNull(imageData, "imageData");
                if (gradientData != null) {
                    throw new IllegalArgumentException(
                            "Image layer cannot contain gradient payload"
                    );
                }
                pixels = new int[0];
            }
            case GRADIENT -> {
                Objects.requireNonNull(gradientData, "gradientData");
                if (imageData != null) {
                    throw new IllegalArgumentException(
                            "Gradient layer cannot contain image payload"
                    );
                }
                pixels = new int[0];
            }
        }
    }

    public LoomLayer(
            UUID id,
            String name,
            boolean visible,
            float opacity,
            BlendMode blendMode,
            boolean emissive,
            int[] pixels
    ) {
        this(
                id,
                name,
                visible,
                opacity,
                blendMode,
                emissive,
                false,
                LayerKind.PAINT,
                pixels,
                null,
                null
        );
    }

    public static LoomLayer paint(
            UUID id,
            String name,
            boolean visible,
            float opacity,
            BlendMode blendMode,
            boolean emissive,
            boolean locked,
            int[] pixels
    ) {
        return new LoomLayer(
                id, name, visible, opacity, blendMode, emissive,
                locked, LayerKind.PAINT, pixels, null, null
        );
    }

    public static LoomLayer image(
            UUID id,
            String name,
            boolean visible,
            float opacity,
            BlendMode blendMode,
            boolean emissive,
            boolean locked,
            ImageLayerData data
    ) {
        return new LoomLayer(
                id, name, visible, opacity, blendMode, emissive,
                locked, LayerKind.IMAGE, new int[0], data, null
        );
    }

    public static LoomLayer gradient(
            UUID id,
            String name,
            boolean visible,
            float opacity,
            BlendMode blendMode,
            boolean emissive,
            boolean locked,
            GradientLayerData data
    ) {
        return new LoomLayer(
                id, name, visible, opacity, blendMode, emissive,
                locked, LayerKind.GRADIENT, new int[0], null, data
        );
    }

    @Override
    public int[] pixels() {
        return pixels.clone();
    }

    public int pixelAt(int index) {
        requirePaint();
        return pixels[index];
    }

    public int pixelCount() {
        return pixels.length;
    }

    public boolean editableAsPaint() {
        return kind == LayerKind.PAINT && !locked;
    }

    public LoomLayer withPixels(int[] nextPixels) {
        requirePaint();
        return copy(
                id, name, visible, opacity, blendMode, emissive,
                locked, kind, nextPixels, imageData, gradientData
        );
    }

    public LoomLayer withName(String nextName) {
        return copy(
                id, nextName, visible, opacity, blendMode, emissive,
                locked, kind, pixels, imageData, gradientData
        );
    }

    public LoomLayer withVisible(boolean nextVisible) {
        return copy(
                id, name, nextVisible, opacity, blendMode, emissive,
                locked, kind, pixels, imageData, gradientData
        );
    }

    public LoomLayer withOpacity(float nextOpacity) {
        return copy(
                id, name, visible, nextOpacity, blendMode, emissive,
                locked, kind, pixels, imageData, gradientData
        );
    }

    public LoomLayer withEmissive(boolean nextEmissive) {
        return copy(
                id, name, visible, opacity, blendMode, nextEmissive,
                locked, kind, pixels, imageData, gradientData
        );
    }

    public LoomLayer withBlendMode(BlendMode nextBlendMode) {
        return copy(
                id, name, visible, opacity,
                Objects.requireNonNull(nextBlendMode, "nextBlendMode"),
                emissive, locked, kind, pixels, imageData, gradientData
        );
    }

    public LoomLayer withLocked(boolean nextLocked) {
        return copy(
                id, name, visible, opacity, blendMode, emissive,
                nextLocked, kind, pixels, imageData, gradientData
        );
    }

    public LoomLayer withImageData(ImageLayerData nextData) {
        if (kind != LayerKind.IMAGE) {
            throw new IllegalStateException(
                    "Layer is not an image layer"
            );
        }

        return LoomLayer.image(
                id, name, visible, opacity, blendMode, emissive,
                locked, Objects.requireNonNull(nextData, "nextData")
        );
    }

    public LoomLayer withGradientData(GradientLayerData nextData) {
        if (kind != LayerKind.GRADIENT) {
            throw new IllegalStateException(
                    "Layer is not a gradient layer"
            );
        }

        return LoomLayer.gradient(
                id, name, visible, opacity, blendMode, emissive,
                locked, Objects.requireNonNull(nextData, "nextData")
        );
    }

    public LoomLayer duplicate(UUID nextId, String nextName) {
        return copy(
                nextId, nextName, visible, opacity, blendMode, emissive,
                locked, kind, pixels, imageData, gradientData
        );
    }

    private void requirePaint() {
        if (kind != LayerKind.PAINT) {
            throw new IllegalStateException(
                    "Pixel editing requires a paint layer"
            );
        }
    }

    private static LoomLayer copy(
            UUID id,
            String name,
            boolean visible,
            float opacity,
            BlendMode blendMode,
            boolean emissive,
            boolean locked,
            LayerKind kind,
            int[] pixels,
            ImageLayerData imageData,
            GradientLayerData gradientData
    ) {
        return new LoomLayer(
                id, name, visible, opacity, blendMode, emissive,
                locked, kind, pixels, imageData, gradientData
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof LoomLayer that)) {
            return false;
        }

        return visible == that.visible
                && Float.compare(opacity, that.opacity) == 0
                && emissive == that.emissive
                && locked == that.locked
                && id.equals(that.id)
                && name.equals(that.name)
                && blendMode == that.blendMode
                && kind == that.kind
                && Arrays.equals(pixels, that.pixels)
                && Objects.equals(imageData, that.imageData)
                && Objects.equals(gradientData, that.gradientData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                id, name, visible, opacity, blendMode, emissive,
                locked, kind, imageData, gradientData
        );
        return 31 * result + Arrays.hashCode(pixels);
    }
}
