package dev.loomstudios.project;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Temporary development-project factory used until the Phase-2 editor creates
 * projects interactively. The output already uses the real schema/codec.
 */
public final class LoomProjectFactory {

    private LoomProjectFactory() {
    }

    public static LoomProject forPlayer(UUID playerId) {
        int[] accents = {
                0xFF00DCE8,
                0xFFFF36C8,
                0xFF8E5CFF,
                0xFF61EA72,
                0xFFFFAD32,
                0xFF4A8DFF
        };

        int accent = accents[Math.floorMod(playerId.hashCode(), accents.length)];
        int secondary = rotateAccent(accent, 2);

        UUID projectId = UUID.nameUUIDFromBytes(
                ("loom-studios:development:" + playerId)
                        .getBytes(StandardCharsets.UTF_8)
        );

        LoomCanvas cape = createCape(projectId, accent, secondary);
        LoomCanvas elytra = createElytra(projectId, accent, secondary);

        return new LoomProject(
                LoomProject.CURRENT_SCHEMA_VERSION,
                projectId,
                "Development " + playerId.toString().substring(0, 8),
                cape,
                elytra,
                LoomRuntimeSettings.defaults()
        );
    }

    private static LoomCanvas createCape(
            UUID projectId,
            int accent,
            int secondary
    ) {
        int[] base = new int[LoomProject.TEXTURE_WIDTH * LoomProject.TEXTURE_HEIGHT];
        int background = darken(accent, 105);

        for (int y = 0; y < LoomProject.TEXTURE_HEIGHT; y++) {
            for (int x = 0; x < LoomProject.TEXTURE_WIDTH; x++) {
                int checker = ((x / 4) + (y / 4)) & 1;
                base[index(x, y)] =
                        checker == 0 ? background : darken(background, 14);
            }
        }

        drawRect(base, 8, 5, 5, 20, accent);
        drawRect(base, 8, 20, 15, 5, accent);
        drawRect(base, 27, 5, 18, 5, secondary);
        drawRect(base, 27, 5, 5, 11, secondary);
        drawRect(base, 27, 13, 18, 5, secondary);
        drawRect(base, 40, 13, 5, 11, secondary);
        drawRect(base, 27, 20, 18, 5, secondary);

        int[] glow = new int[LoomProject.TEXTURE_WIDTH * LoomProject.TEXTURE_HEIGHT];
        drawRect(glow, 8, 5, 2, 20, accent);
        drawRect(glow, 8, 22, 12, 2, accent);
        drawRect(glow, 27, 5, 15, 2, secondary);
        drawRect(glow, 27, 5, 2, 9, secondary);
        drawRect(glow, 27, 13, 15, 2, secondary);
        drawRect(glow, 40, 13, 2, 11, secondary);
        drawRect(glow, 27, 22, 15, 2, secondary);

        LoomLayer baseLayer = new LoomLayer(
                stableLayerId(projectId, "cape-base"),
                "Base",
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                base
        );

        LoomLayer glowLayer = new LoomLayer(
                stableLayerId(projectId, "cape-glow"),
                "Glow",
                true,
                1.0F,
                BlendMode.NORMAL,
                true,
                glow
        );

        return new LoomCanvas(
                LoomProject.TEXTURE_WIDTH,
                LoomProject.TEXTURE_HEIGHT,
                List.of(baseLayer, glowLayer)
        );
    }

    private static LoomCanvas createElytra(
            UUID projectId,
            int accent,
            int secondary
    ) {
        int[] pixels = new int[LoomProject.TEXTURE_WIDTH * LoomProject.TEXTURE_HEIGHT];
        int base = darken(accent, 115);
        int edge = darken(accent, 82);

        for (int y = 0; y < LoomProject.TEXTURE_HEIGHT; y++) {
            for (int x = 0; x < LoomProject.TEXTURE_WIDTH; x++) {
                int checker = ((x / 4) + (y / 4)) & 1;
                pixels[index(x, y)] =
                        checker == 0 ? base : darken(base, 12);
            }
        }

        drawWingFace(pixels, 24, 2, false, accent, secondary, base);
        drawWingFace(pixels, 36, 2, true, accent, secondary, base);

        // Keep UV edge faces opaque so custom artwork remains volumetric, but
        // make them recess visually instead of the old bright neon borders.
        drawRect(pixels, 22, 0, 24, 2, edge);
        drawRect(pixels, 22, 22, 24, 2, edge);
        drawRect(pixels, 22, 2, 2, 20, edge);
        drawRect(pixels, 34, 2, 2, 20, edge);
        drawRect(pixels, 46, 2, 2, 20, edge);

        LoomLayer baseLayer = new LoomLayer(
                stableLayerId(projectId, "elytra-base"),
                "Elytra Base",
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                pixels
        );

        return new LoomCanvas(
                LoomProject.TEXTURE_WIDTH,
                LoomProject.TEXTURE_HEIGHT,
                List.of(baseLayer)
        );
    }

    private static UUID stableLayerId(UUID projectId, String suffix) {
        return UUID.nameUUIDFromBytes(
                (projectId + ":" + suffix).getBytes(StandardCharsets.UTF_8)
        );
    }

    private static void drawWingFace(
            int[] target,
            int startX,
            int startY,
            boolean reverse,
            int accent,
            int secondary,
            int base
    ) {
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                int visualX = reverse ? 9 - x : x;
                int color;

                if (visualX <= 1) {
                    color = accent;
                } else if ((y / 4) % 2 == 0 && visualX >= 6) {
                    color = secondary;
                } else if (visualX == 5 || visualX == 6) {
                    color = lighten(accent, 35);
                } else {
                    color = base;
                }

                target[index(startX + x, startY + y)] = color;
            }
        }
    }

    private static void drawRect(
            int[] target,
            int x,
            int y,
            int width,
            int height,
            int argb
    ) {
        for (int py = Math.max(0, y);
             py < Math.min(LoomProject.TEXTURE_HEIGHT, y + height);
             py++) {
            for (int px = Math.max(0, x);
                 px < Math.min(LoomProject.TEXTURE_WIDTH, x + width);
                 px++) {
                target[index(px, py)] = argb;
            }
        }
    }

    private static int index(int x, int y) {
        return y * LoomProject.TEXTURE_WIDTH + x;
    }

    private static int rotateAccent(int argb, int phase) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        return switch (Math.floorMod(phase, 4)) {
            case 1 -> (a << 24) | (b << 16) | (r << 8) | g;
            case 2 -> (a << 24) | (g << 16) | (b << 8) | r;
            case 3 -> (a << 24)
                    | ((255 - r) << 16)
                    | ((255 - g) << 8)
                    | (255 - b);
            default -> argb;
        };
    }

    private static int darken(int argb, int amount) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.max(0, ((argb >>> 16) & 0xFF) - amount);
        int g = Math.max(0, ((argb >>> 8) & 0xFF) - amount);
        int b = Math.max(0, (argb & 0xFF) - amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lighten(int argb, int amount) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, ((argb >>> 16) & 0xFF) + amount);
        int g = Math.min(255, ((argb >>> 8) & 0xFF) + amount);
        int b = Math.min(255, (argb & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
