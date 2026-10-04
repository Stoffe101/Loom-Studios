package dev.loomstudios.client.render;

import dev.loomstudios.project.*;

/** Editor and runtime share the bounded, testable compositor. */
public final class LoomTextureCompiler {
    private LoomTextureCompiler() {}

    public static int[] compile(LoomCanvas c, int phase, boolean hue, boolean glow) {
        return TextureCompositor.compile(c, phase, hue, glow);
    }

    public static int[] compileAnimated(
            LoomProject p, AnimationChannel channel, int tick, int phase, boolean glow) {
        return TextureCompositor.compileAnimated(p, channel, tick, phase, glow);
    }

    public static int[] compileAnimated(
            LoomProject p,
            AnimationChannel channel,
            int tick,
            int phase,
            boolean glow,
            int imageTick) {
        return TextureCompositor.compileAnimated(p, channel, tick, phase, glow, imageTick);
    }

    public static int[] compileCapeRegion(
            LoomCanvas c, CapeUvRegion region, int phase, boolean hue, boolean glow) {
        return TextureCompositor.compileCapeRegion(c, region, phase, hue, glow);
    }
}
