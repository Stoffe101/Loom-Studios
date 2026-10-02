package dev.loomstudios.image;

import java.util.Arrays;

public enum ImageProcessingMode {
    DIRECT("direct", "Direct"),
    PIXEL_ART("pixel_art", "Pixel Art"),
    OUTLINE("outline", "Outline Only"),
    MONOCHROME("monochrome", "Monochrome"),
    PALETTE_LIMITED("palette_limited", "Palette Limited"),
    POSTERIZE("posterize", "Posterize");

    private final String id;
    private final String displayName;

    ImageProcessingMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public ImageProcessingMode next() {
        ImageProcessingMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ImageProcessingMode fromId(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown image processing mode " + id
                ));
    }
}
