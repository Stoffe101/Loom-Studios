package dev.loomstudios.project;

import java.util.Arrays;

public enum GradientType {
    LINEAR("linear", "Linear"),
    RADIAL("radial", "Radial");

    private final String id;
    private final String displayName;

    GradientType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public GradientType next() {
        GradientType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static GradientType fromId(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown gradient type " + id
                ));
    }
}
