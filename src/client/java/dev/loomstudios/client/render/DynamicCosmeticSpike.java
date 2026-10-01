package dev.loomstudios.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Optional;

/**
 * Technical proof for Loom Studios runtime cape + Elytra textures.
 *
 * <p>The cape changes palette every two seconds. The Elytra uses a separate
 * dedicated texture so Minecraft never needs to fall back to the cape image.</p>
 */
public final class DynamicCosmeticSpike {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private static final int UPDATE_INTERVAL_TICKS = 40;

    // Temporary SPIKE-03 debug values. The final editor will expose a proper
    // slider/config value rather than cycling presets with a key.
    private static final float[] THICKNESS_PRESETS = {1.0F, 0.75F, 0.5F, 0.25F, 1.5F};
    private static int thicknessPresetIndex = 0;

    private static final Identifier CAPE_TEXTURE_ID =
            Identifier.fromNamespaceAndPath("loom-studios", "dynamic/spike_cape");
    private static final Identifier ELYTRA_TEXTURE_ID =
            Identifier.fromNamespaceAndPath("loom-studios", "dynamic/spike_elytra");

    private static final ClientAsset.ResourceTexture CAPE_ASSET =
            new ClientAsset.ResourceTexture(
                    Identifier.fromNamespaceAndPath("loom-studios", "dynamic_spike_cape"),
                    CAPE_TEXTURE_ID
            );
    private static final ClientAsset.ResourceTexture ELYTRA_ASSET =
            new ClientAsset.ResourceTexture(
                    Identifier.fromNamespaceAndPath("loom-studios", "dynamic_spike_elytra"),
                    ELYTRA_TEXTURE_ID
            );

    private static NativeImage capeImage;
    private static DynamicTexture capeTexture;
    private static NativeImage elytraImage;
    private static DynamicTexture elytraTexture;

    private static int ticksUntilUpdate;
    private static int phase;

    private static PlayerSkin cachedSource;
    private static PlayerSkin cachedPatched;

    private DynamicCosmeticSpike() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        ensureInitialized(client);

        if (--ticksUntilUpdate <= 0) {
            ticksUntilUpdate = UPDATE_INTERVAL_TICKS;
            phase = (phase + 1) % 4;
            redrawCape();
            capeTexture.upload();
            LoomStudios.LOGGER.debug("SPIKE-02 cape texture uploaded, phase={}", phase);
        }
    }

    public static void apply(Avatar avatar, AvatarRenderState state) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != avatar || state.skin == null) {
            return;
        }

        ensureInitialized(client);

        if (state.skin != cachedSource || cachedPatched == null) {
            cachedSource = state.skin;
            cachedPatched = state.skin.with(new PlayerSkin.Patch(
                    Optional.empty(),
                    Optional.of(CAPE_ASSET),
                    Optional.of(ELYTRA_ASSET),
                    Optional.empty()
            ));
        }

        state.skin = cachedPatched;
        state.showCape = true;
    }

    private static void ensureInitialized(Minecraft client) {
        if (capeTexture != null && elytraTexture != null) {
            return;
        }

        capeImage = new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false);
        redrawCape();
        capeTexture = new DynamicTexture(
                () -> "Loom Studios SPIKE-02 dynamic cape",
                capeImage
        );
        client.getTextureManager().register(CAPE_TEXTURE_ID, capeTexture);
        capeTexture.upload();

        elytraImage = new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false);
        redrawElytra();
        elytraTexture = new DynamicTexture(
                () -> "Loom Studios SPIKE-03 dedicated Elytra",
                elytraImage
        );
        client.getTextureManager().register(ELYTRA_TEXTURE_ID, elytraTexture);
        elytraTexture.upload();

        ticksUntilUpdate = UPDATE_INTERVAL_TICKS;

        LoomStudios.LOGGER.info(
                "Runtime cosmetic textures registered: cape={}, elytra={}",
                CAPE_TEXTURE_ID,
                ELYTRA_TEXTURE_ID
        );
    }

    private static void redrawCape() {
        if (capeImage == null) {
            return;
        }

        int background = switch (phase) {
            case 0 -> 0xFF081A2A;
            case 1 -> 0xFF1A0A34;
            case 2 -> 0xFF052B2D;
            default -> 0xFF29102A;
        };

        int primary = switch (phase) {
            case 0 -> 0xFF00E5FF;
            case 1 -> 0xFFFF2FD1;
            case 2 -> 0xFF7CFF6B;
            default -> 0xFFFFB02E;
        };

        int secondary = switch (phase) {
            case 0 -> 0xFFFF2FD1;
            case 1 -> 0xFF00E5FF;
            case 2 -> 0xFFFF2FD1;
            default -> 0xFF7D5CFF;
        };

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int checker = ((x / 4) + (y / 4) + phase) & 1;
                capeImage.setPixel(x, y, checker == 0 ? background : darken(background, 18));
            }
        }

        drawRect(capeImage, 8, 5, 5, 20, primary);
        drawRect(capeImage, 8, 20, 15, 5, primary);
        drawRect(capeImage, 27, 5, 18, 5, secondary);
        drawRect(capeImage, 27, 5, 5, 11, secondary);
        drawRect(capeImage, 27, 13, 18, 5, secondary);
        drawRect(capeImage, 40, 13, 5, 11, secondary);
        drawRect(capeImage, 27, 20, 18, 5, secondary);

        int stripeX = 50 + phase * 3;
        drawRect(capeImage, stripeX, 2, 3, 28, primary);
    }

    private static void redrawElytra() {
        if (elytraImage == null) {
            return;
        }

        // Keep every UV face opaque so 100% thickness looks exactly like a
        // normal volumetric Elytra. Thickness is now controlled by geometry,
        // not by punching transparent holes into side-face texture regions.
        int base = 0xFF081421;
        int edge = 0xFF00BFCB;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int checker = ((x / 4) + (y / 4)) & 1;
                elytraImage.setPixel(x, y, checker == 0 ? base : darken(base, 12));
            }
        }

        // Main vanilla wing face UV regions.
        drawWingFace(24, 2, false);
        drawWingFace(36, 2, true);

        // Give the surrounding UV strips some visible color so side/top/bottom
        // faces remain intentionally present at vanilla thickness.
        drawRect(elytraImage, 22, 0, 24, 2, edge);
        drawRect(elytraImage, 22, 22, 24, 2, edge);
        drawRect(elytraImage, 22, 2, 2, 20, edge);
        drawRect(elytraImage, 34, 2, 2, 20, edge);
        drawRect(elytraImage, 46, 2, 2, 20, edge);
    }

    private static void drawWingFace(int startX, int startY, boolean reverse) {
        int deep = 0xFF091626;
        int cyan = 0xFF00DCE8;
        int violet = 0xFF8E2CFF;
        int pink = 0xFFFF36C8;

        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                int visualX = reverse ? 9 - x : x;
                int color;

                if (visualX <= 1) {
                    color = cyan;
                } else if ((y / 4) % 2 == 0 && visualX >= 6) {
                    color = pink;
                } else if (visualX == 5 || visualX == 6) {
                    color = violet;
                } else {
                    color = deep;
                }

                elytraImage.setPixel(startX + x, startY + y, color);
            }
        }
    }

    private static void drawRect(
            NativeImage target,
            int x,
            int y,
            int width,
            int height,
            int argb
    ) {
        for (int py = Math.max(0, y); py < Math.min(HEIGHT, y + height); py++) {
            for (int px = Math.max(0, x); px < Math.min(WIDTH, x + width); px++) {
                target.setPixel(px, py, argb);
            }
        }
    }

    private static int darken(int argb, int amount) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.max(0, ((argb >>> 16) & 0xFF) - amount);
        int g = Math.max(0, ((argb >>> 8) & 0xFF) - amount);
        int b = Math.max(0, (argb & 0xFF) - amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static boolean isUsingLoomElytra(AvatarRenderState state) {
        return state.skin != null
                && state.skin.elytra() != null
                && ELYTRA_TEXTURE_ID.equals(state.skin.elytra().texturePath());
    }

    public static float getElytraThicknessScale() {
        return THICKNESS_PRESETS[thicknessPresetIndex];
    }

    public static void cycleElytraThickness(Minecraft client) {
        thicknessPresetIndex = (thicknessPresetIndex + 1) % THICKNESS_PRESETS.length;
        float scale = getElytraThicknessScale();

        if (client.player != null) {
            int percent = Math.round(scale * 100.0F);
            client.player.displayClientMessage(
                    Component.literal("Loom Studios Elytra thickness: " + percent + "%"),
                    true
            );
        }

        LoomStudios.LOGGER.info(
                "SPIKE-03 Elytra thickness changed to {}%",
                Math.round(scale * 100.0F)
        );
    }

    public static void close() {
        Minecraft client = Minecraft.getInstance();

        if (capeTexture != null) {
            client.getTextureManager().release(CAPE_TEXTURE_ID);
            capeTexture = null;
            capeImage = null;
        }

        if (elytraTexture != null) {
            client.getTextureManager().release(ELYTRA_TEXTURE_ID);
            elytraTexture = null;
            elytraImage = null;
        }

        cachedSource = null;
        cachedPatched = null;
        ticksUntilUpdate = 0;
        phase = 0;
        thicknessPresetIndex = 0;
    }
}
