package dev.loomstudios.project;

/** Typed bounded authored parameters. Keyframes animate each effect's primary scalar. */
public sealed interface EffectParameters {
    AnimationEffectType effect();

    static void range(float v, float min, float max) {
        if (!Float.isFinite(v) || v < min || v > max)
            throw new IllegalArgumentException("Effect parameter out of range");
    }

    record Pulse(float minOpacity, float maxOpacity) implements EffectParameters {
        public Pulse {
            range(minOpacity, 0, 1);
            range(maxOpacity, minOpacity, 1);
        }

        public AnimationEffectType effect() {
            return AnimationEffectType.PULSE;
        }
    }

    record Scroll(float directionX, float directionY, float distance) implements EffectParameters {
        public Scroll {
            range(directionX, -1, 1);
            range(directionY, -1, 1);
            range(distance, 0, 4);
        }

        public AnimationEffectType effect() {
            return AnimationEffectType.SCROLL;
        }
    }

    record Gradient(
            float angle,
            float width,
            int firstColor,
            int secondColor,
            float offset,
            boolean legacyScroll)
            implements EffectParameters {
        public Gradient {
            range(angle, -360, 360);
            range(width, .01F, 4);
            range(offset, -4, 4);
        }

        public AnimationEffectType effect() {
            return AnimationEffectType.MOVING_GRADIENT;
        }
    }

    record Sparkle(float density, int seed, int size, float brightness)
            implements EffectParameters {
        public Sparkle {
            range(density, 0, 1);
            range(brightness, 0, 4);
            if (size < 1 || size > 8)
                throw new IllegalArgumentException("Sparkle size out of range");
        }

        public AnimationEffectType effect() {
            return AnimationEffectType.SPARKLE;
        }
    }

    record Glow(float intensity, float falloff) implements EffectParameters {
        public Glow {
            range(intensity, 0, 4);
            range(falloff, 0, 1);
        }

        public AnimationEffectType effect() {
            return AnimationEffectType.EMISSIVE_GLOW;
        }
    }

    record Hue(float cycles) implements EffectParameters {
        public Hue {
            range(cycles, -4, 4);
        }

        public AnimationEffectType effect() {
            return AnimationEffectType.HUE_SHIFT;
        }
    }

    static EffectParameters forAuthoring(AnimationEffectType effect) {
        return effect == AnimationEffectType.MOVING_GRADIENT
                ? new Gradient(90, 1, 0xFF39CEDB, 0xFFC675EB, 0, false)
                : defaults(effect);
    }

    static EffectParameters defaults(AnimationEffectType effect) {
        return switch (effect) {
            case PULSE -> new Pulse(0, 1);
            case SCROLL -> new Scroll(1, 0, 1);
            case MOVING_GRADIENT -> new Gradient(90, 1, 0xFF22D7E8, 0xFF9B4DFF, 0, true);
            case SPARKLE -> new Sparkle(1, 0, 1, 1);
            case EMISSIVE_GLOW -> new Glow(1, 0);
            case HUE_SHIFT -> new Hue(1);
        };
    }
}
