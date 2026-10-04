package dev.loomstudios.project;

public record AnimationKeyframe(int tick, float value, AnimationEasing easing) {
    public AnimationKeyframe(int tick, float value) {
        this(tick, value, AnimationEasing.LINEAR);
    }

    public AnimationKeyframe {
        java.util.Objects.requireNonNull(easing, "easing");
        if (tick < 0 || tick > LoomAnimation.MAX_DURATION_TICKS) {
            throw new IllegalArgumentException("Animation keyframe tick out of range");
        }
        if (!Float.isFinite(value)
                || value < LoomAnimation.MIN_KEYFRAME_VALUE
                || value > LoomAnimation.MAX_KEYFRAME_VALUE) {
            throw new IllegalArgumentException("Animation keyframe value out of range");
        }
    }
}
