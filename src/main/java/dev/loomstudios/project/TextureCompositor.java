package dev.loomstudios.project;


import java.awt.Color;
import java.util.List;

/** Non-destructive typed-layer compiler used by editor/runtime surfaces. */
public final class TextureCompositor {
    private TextureCompositor() {}

    public static int[] compile(
            LoomCanvas canvas, int animationPhase, boolean animateHue, boolean emissiveOnly) {
        int[] output = new int[Math.multiplyExact(canvas.width(), canvas.height())];

        int[] below = new int[output.length];
        for (LoomLayer layer : canvas.layers()) {
            if (!layer.visible()) {
                below = new int[output.length];
                continue;
            }

            int[] raster = LayerRasterizer.rasterize(layer, canvas.width(), canvas.height());

            raster = applyMask(layer, raster, below);
            below = alphaRaster(raster, layer.opacity());
            if (emissiveOnly && !layer.emissive()) continue;
            for (int i = 0; i < output.length; i++) {
                int source = raster[i];

                if (animateHue && ((source >>> 24) & 0xFF) != 0) {
                    source = rotateChannels(source, animationPhase);
                }

                source = multiplyAlpha(source, layer.opacity());
                output[i] = blend(output[i], source, layer.blendMode());
            }
        }

        return output;
    }

    public static int[] compileAnimated(
            LoomProject project,
            AnimationChannel channel,
            int timelineTick,
            int legacyHuePhase,
            boolean emissiveOnly) {
        LoomCanvas canvas = channel == AnimationChannel.CAPE ? project.cape() : project.elytra();
        LoomAnimation animation = project.animation();
        int[] output = new int[Math.multiplyExact(canvas.width(), canvas.height())];

        int[] below = new int[output.length];
        for (LoomLayer layer : canvas.layers()) {
            if (!layer.visible()) {
                below = new int[output.length];
                continue;
            }

            List<AnimationTrack> tracks =
                    animation.tracks().stream()
                            .filter(
                                    track ->
                                            track.enabled()
                                                    && track.channel() == channel
                                                    && track.layerId().equals(layer.id()))
                            .toList();

            boolean authoredEmissive =
                    tracks.stream()
                            .anyMatch(track -> track.effect() == AnimationEffectType.EMISSIVE_GLOW);

            int[] raster =
                    LayerRasterizer.rasterize(layer, canvas.width(), canvas.height(), timelineTick);

            int shiftX = 0;
            int shiftY = 0;
            float alphaMultiplier = 1.0F;
            float hueCycles = 0.0F;
            float sparkleIntensity = -1.0F;
            float emissiveMultiplier = 1.0F;

            for (AnimationTrack track : tracks) {
                float value = AnimationEvaluator.valueAt(track, animation, timelineTick);

                switch (track.parameters()) {
                    case EffectParameters.Pulse v ->
                            alphaMultiplier *=
                                    v.minOpacity()
                                            + (v.maxOpacity() - v.minOpacity())
                                                    * clamp(value, 0, 1);
                    case EffectParameters.Hue v -> hueCycles += value * v.cycles();
                    case EffectParameters.Scroll v -> {
                        shiftX +=
                                Math.round(value * v.directionX() * v.distance() * canvas.width());
                        shiftY +=
                                Math.round(value * v.directionY() * v.distance() * canvas.height());
                    }
                    case EffectParameters.Gradient v -> {
                        if (v.legacyScroll()) shiftY += Math.round(value * canvas.height());
                        else applyGradient(raster, canvas.width(), canvas.height(), v, value);
                    }
                    case EffectParameters.Sparkle v -> {
                        for (int i = 0; i < raster.length; i++) {
                            int x = i % canvas.width(),
                                    y = i / canvas.width(),
                                    cell =
                                            (y / v.size()) * (canvas.width() / v.size() + 1)
                                                    + x / v.size();
                            if (!sparkleVisible(
                                    cell ^ v.seed(),
                                    timelineTick,
                                    clamp(value * v.density(), 0, 1))) raster[i] &= 0xFFFFFF;
                            else raster[i] = brighten(raster[i], v.brightness());
                        }
                    }
                    case EffectParameters.Glow v -> {
                        emissiveMultiplier *= clamp(value * v.intensity(), 0, 4);
                        if (v.falloff() > 0 && emissiveOnly)
                            raster = glowHalo(raster, canvas.width(), canvas.height(), v.falloff());
                    }
                }
            }

            if (shiftX != 0 || shiftY != 0) {
                raster = shiftRaster(raster, canvas.width(), canvas.height(), shiftX, shiftY);
            }

            raster = applyMask(layer, raster, below);
            below = alphaRaster(raster, layer.opacity() * alphaMultiplier);
            if (emissiveOnly && !layer.emissive() && !authoredEmissive) continue;
            for (int i = 0; i < output.length; i++) {
                int source = raster[i];
                if (((source >>> 24) & 0xFF) == 0) {
                    continue;
                }

                if (channel == AnimationChannel.CAPE && project.runtime().hueCycleEnabled()) {
                    source = rotateChannels(source, legacyHuePhase);
                }

                if (hueCycles != 0.0F) {
                    source = rotateHue(source, hueCycles);
                }

                if (sparkleIntensity >= 0.0F
                        && !sparkleVisible(i, timelineTick, sparkleIntensity)) {
                    source &= 0x00FFFFFF;
                }

                float effectiveOpacity = layer.opacity() * alphaMultiplier;

                if (emissiveOnly) {
                    effectiveOpacity *= emissiveMultiplier;
                }

                source = multiplyAlpha(source, effectiveOpacity);

                output[i] = blend(output[i], source, layer.blendMode());
            }
        }

        return output;
    }

    public static int[] compileCapeRegion(
            LoomCanvas canvas,
            CapeUvRegion region,
            int animationPhase,
            boolean animateHue,
            boolean emissiveOnly) {
        int scale = CanvasResolution.fromCanvas(canvas).scale();
        int regionWidth = region.width(scale);
        int regionHeight = region.height(scale);
        int[] output = new int[regionWidth * regionHeight];

        int[] atlas = compile(canvas, animationPhase, animateHue, emissiveOnly);
        for (int y = 0; y < regionHeight; y++)
            for (int x = 0; x < regionWidth; x++)
                output[y * regionWidth + x] =
                        atlas[region.atlasY(y, scale) * canvas.width() + region.atlasX(x, scale)];

        return output;
    }

    private static int[] applyMask(LoomLayer layer, int[] raster, int[] below) {
        for (int i = 0; i < raster.length; i++) {
            float alpha = layer.maskAt(i) / 255f;
            if (layer.clipToBelow()) alpha *= ((below[i] >>> 24) & 255) / 255f;
            raster[i] = multiplyAlpha(raster[i], alpha);
        }
        return raster;
    }

    private static int[] alphaRaster(int[] raster, float opacity) {
        int[] result = new int[raster.length];
        for (int i = 0; i < raster.length; i++) result[i] = multiplyAlpha(raster[i], opacity);
        return result;
    }

    private static int brighten(int color, float brightness) {
        int rgb = 0;
        for (int s : new int[] {0, 8, 16})
            rgb |= Math.min(255, Math.round((color >>> s & 255) * brightness)) << s;
        return (color & 0xFF000000) | rgb;
    }

    private static void applyGradient(
            int[] raster, int w, int h, EffectParameters.Gradient settings, float value) {
        double angle = Math.toRadians(settings.angle()), dx = Math.cos(angle), dy = Math.sin(angle);
        for (int i = 0; i < raster.length; i++) {
            double phase =
                    ((i % w) / (double) w * dx + (i / w) / (double) h * dy) / settings.width()
                            + settings.offset()
                            + value;
            phase -= Math.floor(phase);
            int rgb = 0;
            for (int s : new int[] {0, 8, 16})
                rgb |=
                        (int)
                                        Math.round(
                                                (settings.firstColor() >>> s & 255) * (1 - phase)
                                                        + (settings.secondColor() >>> s & 255)
                                                                * phase)
                                << s;
            raster[i] = (raster[i] & 0xFF000000) | rgb;
        }
    }

    private static int[] glowHalo(int[] source, int w, int h, float falloff) {
        int[] out = source.clone();
        int radius = Math.max(1, Math.round(falloff * 4));
        for (int step = 0; step < radius; step++) {
            int[] previous = out;
            out = previous.clone();
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    int index = y * w + x;
                    if ((source[index] >>> 24) != 0) continue;
                    int best = out[index];
                    int[] neighbors = {
                        x > 0 ? index - 1 : -1,
                        x + 1 < w ? index + 1 : -1,
                        y > 0 ? index - w : -1,
                        y + 1 < h ? index + w : -1
                    };
                    for (int next : neighbors)
                        if (next >= 0) {
                            int candidate = multiplyAlpha(previous[next], falloff * .7f);
                            if ((candidate >>> 24) > (best >>> 24)) best = candidate;
                        }
                    out[index] = best;
                }
        }
        return out;
    }

    private static int multiplyAlpha(int argb, float opacity) {
        int alpha = (argb >>> 24) & 0xFF;
        int scaled = Math.min(255, Math.max(0, Math.round(alpha * opacity)));
        return (argb & 0x00FFFFFF) | (scaled << 24);
    }

    private static int blend(int destination, int source, BlendMode mode) {
        if (mode == BlendMode.NORMAL) {
            return blendNormal(destination, source);
        }

        int sa = (source >>> 24) & 0xFF;
        if (sa == 0) {
            return destination;
        }

        int da = (destination >>> 24) & 0xFF;
        if (da == 0) {
            return source;
        }

        int sr = (source >>> 16) & 0xFF;
        int sg = (source >>> 8) & 0xFF;
        int sb = source & 0xFF;

        int dr = (destination >>> 16) & 0xFF;
        int dg = (destination >>> 8) & 0xFF;
        int db = destination & 0xFF;

        int br = blendChannel(dr, sr, mode);
        int bg = blendChannel(dg, sg, mode);
        int bb = blendChannel(db, sb, mode);

        int blendedSource = (sa << 24) | (br << 16) | (bg << 8) | bb;

        return blendNormal(destination, blendedSource);
    }

    private static int blendChannel(int destination, int source, BlendMode mode) {
        return switch (mode) {
            case NORMAL -> source;
            case ADD -> Math.min(255, destination + source);
            case SCREEN -> 255 - ((255 - destination) * (255 - source) + 127) / 255;
            case MULTIPLY -> (destination * source + 127) / 255;
            case OVERLAY ->
                    destination < 128
                            ? (2 * destination * source + 127) / 255
                            : 255 - (2 * (255 - destination) * (255 - source) + 127) / 255;
        };
    }

    private static int blendNormal(int destination, int source) {
        int sa = (source >>> 24) & 0xFF;
        if (sa == 0) {
            return destination;
        }

        if (sa == 255) {
            return source;
        }

        int da = (destination >>> 24) & 0xFF;
        int outA = sa + ((da * (255 - sa) + 127) / 255);

        if (outA == 0) {
            return 0;
        }

        int sr = (source >>> 16) & 0xFF;
        int sg = (source >>> 8) & 0xFF;
        int sb = source & 0xFF;

        int dr = (destination >>> 16) & 0xFF;
        int dg = (destination >>> 8) & 0xFF;
        int db = destination & 0xFF;

        int dstWeight = (da * (255 - sa) + 127) / 255;

        int outR = (sr * sa + dr * dstWeight) / outA;
        int outG = (sg * sa + dg * dstWeight) / outA;
        int outB = (sb * sa + db * dstWeight) / outA;

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private static int[] shiftRaster(int[] source, int width, int height, int shiftX, int shiftY) {
        int[] shifted = new int[source.length];

        for (int y = 0; y < height; y++) {
            int destinationY = Math.floorMod(y + shiftY, height);

            for (int x = 0; x < width; x++) {
                int destinationX = Math.floorMod(x + shiftX, width);
                shifted[destinationY * width + destinationX] = source[y * width + x];
            }
        }

        return shifted;
    }

    private static int rotateHue(int argb, float cycles) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        float[] hsb = Color.RGBtoHSB(red, green, blue, null);

        float hue = hsb[0] + cycles;
        hue -= (float) Math.floor(hue);

        int rgb = Color.HSBtoRGB(hue, hsb[1], hsb[2]);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private static boolean sparkleVisible(int pixelIndex, int timelineTick, float intensity) {
        int hash = pixelIndex * 0x45D9F3B ^ timelineTick * 0x119DE1F3;
        hash ^= hash >>> 16;
        int sample = hash & 0xFFFF;
        return sample / 65535.0F <= intensity;
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int rotateChannels(int argb, int phase) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        return switch (Math.floorMod(phase, 4)) {
            case 1 -> (a << 24) | (b << 16) | (r << 8) | g;
            case 2 -> (a << 24) | (g << 16) | (b << 8) | r;
            case 3 -> (a << 24) | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
            default -> argb;
        };
    }
}
