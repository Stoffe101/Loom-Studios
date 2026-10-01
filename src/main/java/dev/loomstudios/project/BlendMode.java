package dev.loomstudios.project;

/**
 * Stable serialized layer blend modes.
 *
 * <p>Append new values only. The schema-v1 codec stores the enum ordinal.</p>
 */
public enum BlendMode {
    NORMAL("Normal"),
    ADD("Add / Glow"),
    SCREEN("Screen"),
    MULTIPLY("Multiply"),
    OVERLAY("Overlay");

    private final String displayName;

    BlendMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public BlendMode next() {
        BlendMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
