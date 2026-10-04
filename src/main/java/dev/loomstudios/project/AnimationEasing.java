package dev.loomstudios.project;

public enum AnimationEasing {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    SMOOTH("Smooth"),
    STEP("Step"),
  CUSTOM("Custom curve");
    private final String label;

    AnimationEasing(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public float apply(float t) {
        return switch (this) {
            case LINEAR -> t;
            case EASE_IN -> t * t;
            case EASE_OUT -> 1 - (1 - t) * (1 - t);
            case SMOOTH -> t * t * (3 - 2 * t);
            case CUSTOM -> KeyframeCurve.DEFAULT.apply(t);
      case STEP -> t >= 1 ? 1 : 0;
        };
    }
}
