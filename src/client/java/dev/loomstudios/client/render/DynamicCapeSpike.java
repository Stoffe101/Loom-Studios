package dev.loomstudios.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Optional;

/**
 * SPIKE-02 proof: a single in-memory cape texture that updates while the
 * development client is running.
 *
 * <p>This intentionally remains local-player-only. The real editor/compiler
 * will replace the procedural pattern while keeping the same runtime-texture
 * ownership model.</p>
 */
public final class DynamicCapeSpike {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private static final int UPDATE_INTERVAL_TICKS = 40;

    private static final Identifier TEXTURE_ID =
            Identifier.fromNamespaceAndPath("loom-studios", "dynamic/spike_cape");
    private static final ClientAsset.ResourceTexture CAPE_ASSET =
            new ClientAsset.ResourceTexture(
                    Identifier.fromNamespaceAndPath("loom-studios", "dynamic_spike_cape"),
                    TEXTURE_ID
            );

    private static NativeImage image;
    private static DynamicTexture texture;
    private static int ticksUntilUpdate;
    private static int phase;

    private static PlayerSkin cachedSource;
    private static PlayerSkin cachedPatched;

    private DynamicCapeSpike() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        ensureInitialized(client);

        if (--ticksUntilUpdate <= 0) {
            ticksUntilUpdate = UPDATE_INTERVAL_TICKS;
            phase = (phase + 1) % 4;
            redraw();
            texture.upload();
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
                    Optional.empty(),
                    Optional.empty()
            ));
        }

        state.skin = cachedPatched;
        state.showCape = true;
    }

    private static void ensureInitialized(Minecraft client) {
        if (texture != null) {
            return;
        }

        image = new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false);
        redraw();

        texture = new DynamicTexture(
                () -> "Loom Studios SPIKE-02 dynamic cape",
                image
        );
        client.getTextureManager().register(TEXTURE_ID, texture);
        texture.upload();
        ticksUntilUpdate = UPDATE_INTERVAL_TICKS;

        LoomStudios.LOGGER.info(
                "SPIKE-02 dynamic cape registered at {} ({}x{})",
                TEXTURE_ID,
                WIDTH,
                HEIGHT
        );
    }

    private static void redraw() {
        if (image == null) {
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
                image.setPixel(x, y, checker == 0 ? background : darken(background, 18));
            }
        }

        // Draw a deliberately oversized animated LS motif across the texture.
        // Exact cape-UV artwork comes later; this spike only proves live GPU uploads.
        drawRect(8, 5, 5, 20, primary);
        drawRect(8, 20, 15, 5, primary);
        drawRect(27, 5, 18, 5, secondary);
        drawRect(27, 5, 5, 11, secondary);
        drawRect(27, 13, 18, 5, secondary);
        drawRect(40, 13, 5, 11, secondary);
        drawRect(27, 20, 18, 5, secondary);

        // Moving highlight strip makes the live update unmistakable.
        int stripeX = 50 + phase * 3;
        drawRect(stripeX, 2, 3, 28, primary);
    }

    private static void drawRect(int x, int y, int width, int height, int argb) {
        for (int py = Math.max(0, y); py < Math.min(HEIGHT, y + height); py++) {
            for (int px = Math.max(0, x); px < Math.min(WIDTH, x + width); px++) {
                image.setPixel(px, py, argb);
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

    public static void close() {
        Minecraft client = Minecraft.getInstance();

        if (texture != null) {
            client.getTextureManager().release(TEXTURE_ID);
            texture = null;
            image = null;
        }

        cachedSource = null;
        cachedPatched = null;
        ticksUntilUpdate = 0;
        phase = 0;
    }
}
