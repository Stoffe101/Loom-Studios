package dev.loomstudios.project;

public enum AnimationEffectType {
    PULSE("pulse", "Pulse"),
    SCROLL("scroll", "Scroll"),
    HUE_SHIFT("hue_shift", "Hue Shift"),
    MOVING_GRADIENT("moving_gradient", "Moving Gradient"),
    SPARKLE("sparkle", "Sparkle"),
    EMISSIVE_GLOW("emissive_glow", "Emissive Glow");

    private final String id;
    private final String displayName;

    AnimationEffectType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public AnimationEffectType next() {
        AnimationEffectType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static AnimationEffectType fromId(String id) {
        for (AnimationEffectType value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                "Unknown animation effect " + id
        );
    }
}
