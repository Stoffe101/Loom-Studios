package dev.loomstudios.project;

import java.util.Arrays;

public enum LayerKind {
    PAINT("paint", "Paint"),
    IMAGE("image", "Image"),
    GRADIENT("gradient", "Gradient");

    private final String id;
    private final String displayName;

    LayerKind(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static LayerKind fromId(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown layer kind " + id
                ));
    }
}
