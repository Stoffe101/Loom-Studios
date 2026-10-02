package dev.loomstudios.project;

import java.util.Arrays;

/**
 * Layer blend modes.
 *
 * <p>Schema v1 stored enum ordinals, so existing values must never be
 * reordered. Schema v2+ stores stable string ids.</p>
 */
public enum BlendMode {
    NORMAL("normal", "Normal"),
    ADD("add", "Add / Glow"),
    SCREEN("screen", "Screen"),
    MULTIPLY("multiply", "Multiply"),
    OVERLAY("overlay", "Overlay");

    private final String id;
    private final String displayName;

    BlendMode(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public BlendMode next() {
        BlendMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static BlendMode fromId(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown blend mode " + id
                ));
    }
}
