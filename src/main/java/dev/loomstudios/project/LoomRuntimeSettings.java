package dev.loomstudios.project;

/**
 * Small set of runtime-facing project settings already proven by the
 * foundation spikes. More effect/timeline data will move into dedicated
 * animation structures in Phase 6.
 */
public record LoomRuntimeSettings(
        float elytraThickness,
        int animationPeriodTicks,
        boolean hueCycleEnabled,
        boolean emissiveEnabled
) {
    public LoomRuntimeSettings {
        if (!Float.isFinite(elytraThickness)
                || elytraThickness < 0.25F
                || elytraThickness > 2.0F) {
            throw new IllegalArgumentException("Elytra thickness out of range");
        }

        if (animationPeriodTicks < 10 || animationPeriodTicks > 1200) {
            throw new IllegalArgumentException("Animation period out of range");
        }
    }

    public static LoomRuntimeSettings defaults() {
        return new LoomRuntimeSettings(1.0F, 40, true, true);
    }

    public LoomRuntimeSettings withElytraThickness(float thickness) {
        return new LoomRuntimeSettings(
                thickness,
                animationPeriodTicks,
                hueCycleEnabled,
                emissiveEnabled
        );
    }

    public LoomRuntimeSettings withEmissiveEnabled(boolean enabled) {
        return new LoomRuntimeSettings(
                elytraThickness,
                animationPeriodTicks,
                hueCycleEnabled,
                enabled
        );
    }

    /**
     * Normal editor-created projects are static unless the user explicitly
     * adds animation/effect behavior later.
     */
    public static LoomRuntimeSettings editorDefaults() {
        return new LoomRuntimeSettings(1.0F, 40, false, false);
    }
}
