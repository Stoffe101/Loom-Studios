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

    public String valueLabel(){return switch(this){case PULSE->"Opacity";case SCROLL->"Horizontal offset";case HUE_SHIFT->"Hue rotation";case MOVING_GRADIENT->"Vertical offset";case SPARKLE->"Shimmer strength";case EMISSIVE_GLOW->"Glow intensity";};}
    public String description(){return switch(this){case PULSE->"Fade this layer in and out";case SCROLL->"Wrap the pattern horizontally";case HUE_SHIFT->"Rotate this layer's colors";case MOVING_GRADIENT->"Wrap the pattern vertically";case SPARKLE->"Twinkle existing highlight pixels";case EMISSIVE_GLOW->"Light up this layer's highlights";};}

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
