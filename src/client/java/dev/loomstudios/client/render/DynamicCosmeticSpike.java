package dev.loomstudios.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.network.ClientCosmeticSync;
import dev.loomstudios.network.ProofProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Foundation runtime for SPIKE-02 through SPIKE-06.
 *
 * <p>Saved projects eventually compile into this same long-lived texture-bundle
 * concept. Animation is evaluated locally from project parameters and world
 * time; no rendered frames are sent over the network.</p>
 */
public final class DynamicCosmeticSpike {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;

    private static final float[] THICKNESS_PRESETS = {1.0F, 0.75F, 0.5F, 0.25F, 1.5F};
    private static int thicknessPresetIndex;
    private static boolean emissivePassEnabled = true;

    private static final Map<String, RuntimeBundle> BUNDLES = new HashMap<>();
    private static final Map<Identifier, RuntimeBundle> BY_CAPE_TEXTURE = new HashMap<>();
    private static final Map<Identifier, RuntimeBundle> BY_ELYTRA_TEXTURE = new HashMap<>();
    private static final Map<UUID, CachedSkin> SKINS = new HashMap<>();

    private DynamicCosmeticSpike() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        ClientCosmeticSync.ensureLocalProject(client.player.getUUID());

        long gameTime = client.level.getGameTime();
        for (RuntimeBundle bundle : BUNDLES.values()) {
            int phase = (int)((gameTime / bundle.project.animationPeriodTicks()) % 4L);
            if (phase != bundle.phase) {
                bundle.phase = phase;
                redrawCape(bundle);
                redrawEmissive(bundle);
                bundle.capeTexture.upload();
                bundle.emissiveTexture.upload();
            }
        }
    }

    public static void apply(Avatar avatar, AvatarRenderState state) {
        if (state.skin == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            ClientCosmeticSync.ensureLocalProject(client.player.getUUID());
        }

        UUID playerId = avatar.getUUID();
        ProofProject project = ClientCosmeticSync.projectFor(playerId);
        String projectHash = ClientCosmeticSync.projectHashFor(playerId);

        if (project == null || projectHash == null) {
            return;
        }

        RuntimeBundle bundle = ensureBundle(client, projectHash, project);
        CachedSkin cached = SKINS.get(playerId);

        if (cached == null
                || cached.source != state.skin
                || !cached.projectHash.equals(projectHash)) {
            PlayerSkin patched = state.skin.with(new PlayerSkin.Patch(
                    Optional.empty(),
                    Optional.of(bundle.capeAsset),
                    Optional.of(bundle.elytraAsset),
                    Optional.empty()
            ));

            cached = new CachedSkin(state.skin, projectHash, patched);
            SKINS.put(playerId, cached);
        }

        state.skin = cached.patched;
        state.showCape = true;
    }

    private static RuntimeBundle ensureBundle(
            Minecraft client,
            String projectHash,
            ProofProject project
    ) {
        RuntimeBundle existing = BUNDLES.get(projectHash);
        if (existing != null) {
            return existing;
        }

        String suffix = projectHash.substring(0, 16);
        Identifier capeId = Identifier.fromNamespaceAndPath(
                LoomStudios.MOD_ID,
                "dynamic/cape_" + suffix
        );
        Identifier elytraId = Identifier.fromNamespaceAndPath(
                LoomStudios.MOD_ID,
                "dynamic/elytra_" + suffix
        );
        Identifier emissiveId = Identifier.fromNamespaceAndPath(
                LoomStudios.MOD_ID,
                "dynamic/emissive_" + suffix
        );

        RuntimeBundle bundle = new RuntimeBundle(
                project,
                capeId,
                elytraId,
                emissiveId,
                new ClientAsset.ResourceTexture(
                        Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "cape_asset_" + suffix),
                        capeId
                ),
                new ClientAsset.ResourceTexture(
                        Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "elytra_asset_" + suffix),
                        elytraId
                ),
                new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false),
                new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false),
                new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false)
        );

        redrawCape(bundle);
        redrawElytra(bundle);
        redrawEmissive(bundle);

        bundle.capeTexture = new DynamicTexture(
                () -> "Loom Studios cape " + suffix,
                bundle.capeImage
        );
        bundle.elytraTexture = new DynamicTexture(
                () -> "Loom Studios Elytra " + suffix,
                bundle.elytraImage
        );
        bundle.emissiveTexture = new DynamicTexture(
                () -> "Loom Studios emissive cape " + suffix,
                bundle.emissiveImage
        );

        client.getTextureManager().register(capeId, bundle.capeTexture);
        client.getTextureManager().register(elytraId, bundle.elytraTexture);
        client.getTextureManager().register(emissiveId, bundle.emissiveTexture);

        bundle.capeTexture.upload();
        bundle.elytraTexture.upload();
        bundle.emissiveTexture.upload();

        BUNDLES.put(projectHash, bundle);
        BY_CAPE_TEXTURE.put(capeId, bundle);
        BY_ELYTRA_TEXTURE.put(elytraId, bundle);

        LoomStudios.LOGGER.info(
                "Compiled runtime Loom textures for project {}",
                suffix
        );

        return bundle;
    }

    private static void redrawCape(RuntimeBundle bundle) {
        int accent = rotateAccent(bundle.project.accentArgb(), bundle.phase);
        int secondary = rotateAccent(bundle.project.accentArgb(), bundle.phase + 2);
        int background = darken(accent, 105);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int checker = ((x / 4) + (y / 4) + bundle.phase) & 1;
                bundle.capeImage.setPixel(
                        x,
                        y,
                        checker == 0 ? background : darken(background, 14)
                );
            }
        }

        drawRect(bundle.capeImage, 8, 5, 5, 20, accent);
        drawRect(bundle.capeImage, 8, 20, 15, 5, accent);
        drawRect(bundle.capeImage, 27, 5, 18, 5, secondary);
        drawRect(bundle.capeImage, 27, 5, 5, 11, secondary);
        drawRect(bundle.capeImage, 27, 13, 18, 5, secondary);
        drawRect(bundle.capeImage, 40, 13, 5, 11, secondary);
        drawRect(bundle.capeImage, 27, 20, 18, 5, secondary);

        int stripeX = 48 + bundle.phase * 3;
        drawRect(bundle.capeImage, stripeX, 2, 3, 28, accent);
    }

    private static void redrawElytra(RuntimeBundle bundle) {
        int accent = bundle.project.accentArgb();
        int secondary = rotateAccent(accent, 2);
        int base = darken(accent, 115);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int checker = ((x / 4) + (y / 4)) & 1;
                bundle.elytraImage.setPixel(
                        x,
                        y,
                        checker == 0 ? base : darken(base, 12)
                );
            }
        }

        drawWingFace(bundle.elytraImage, 24, 2, false, accent, secondary, base);
        drawWingFace(bundle.elytraImage, 36, 2, true, accent, secondary, base);

        drawRect(bundle.elytraImage, 22, 0, 24, 2, accent);
        drawRect(bundle.elytraImage, 22, 22, 24, 2, accent);
        drawRect(bundle.elytraImage, 22, 2, 2, 20, accent);
        drawRect(bundle.elytraImage, 34, 2, 2, 20, accent);
        drawRect(bundle.elytraImage, 46, 2, 2, 20, accent);
    }

    private static void redrawEmissive(RuntimeBundle bundle) {
        clear(bundle.emissiveImage);

        if (!bundle.project.emissiveEnabled()) {
            return;
        }

        int glow = rotateAccent(bundle.project.accentArgb(), bundle.phase + 1);
        int pulse = (bundle.phase & 1) == 0 ? glow : lighten(glow, 55);

        // Small glowing LS strokes plus a moving vertical shimmer.
        drawRect(bundle.emissiveImage, 8, 5, 2, 20, pulse);
        drawRect(bundle.emissiveImage, 8, 22, 12, 2, pulse);
        drawRect(bundle.emissiveImage, 27, 5, 15, 2, glow);
        drawRect(bundle.emissiveImage, 27, 5, 2, 9, glow);
        drawRect(bundle.emissiveImage, 27, 13, 15, 2, glow);
        drawRect(bundle.emissiveImage, 40, 13, 2, 11, glow);
        drawRect(bundle.emissiveImage, 27, 22, 15, 2, glow);

        int shimmerX = 49 + bundle.phase * 3;
        drawRect(bundle.emissiveImage, shimmerX, 4, 2, 24, 0xCCFFFFFF);
    }

    private static void drawWingFace(
            NativeImage target,
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

                target.setPixel(startX + x, startY + y, color);
            }
        }
    }

    private static void clear(NativeImage target) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                target.setPixel(x, y, 0x00000000);
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

    private static int rotateAccent(int argb, int phase) {
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

    public static float getElytraThicknessScale(AvatarRenderState state) {
        if (state.skin == null || state.skin.elytra() == null) {
            return 1.0F;
        }

        RuntimeBundle bundle = BY_ELYTRA_TEXTURE.get(state.skin.elytra().texturePath());
        if (bundle == null) {
            return 1.0F;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player != null && state.id == client.player.getId()) {
            return THICKNESS_PRESETS[thicknessPresetIndex];
        }

        return bundle.project.elytraThickness();
    }

    public static Identifier getCapeEmissiveTexture(AvatarRenderState state) {
        if (!emissivePassEnabled
                || state.skin == null
                || state.skin.cape() == null) {
            return null;
        }

        RuntimeBundle bundle = BY_CAPE_TEXTURE.get(state.skin.cape().texturePath());
        if (bundle == null || !bundle.project.emissiveEnabled()) {
            return null;
        }

        return bundle.emissiveTextureId;
    }

    public static void cycleElytraThickness(Minecraft client) {
        thicknessPresetIndex = (thicknessPresetIndex + 1) % THICKNESS_PRESETS.length;
        if (client.player != null) {
            int percent = Math.round(THICKNESS_PRESETS[thicknessPresetIndex] * 100.0F);
            client.player.displayClientMessage(
                    Component.literal("Loom Studios Elytra thickness: " + percent + "%"),
                    true
            );
        }
    }

    public static void toggleEmissivePass(Minecraft client) {
        emissivePassEnabled = !emissivePassEnabled;
        if (client.player != null) {
            client.player.displayClientMessage(
                    Component.literal(
                            "Loom Studios emissive pass: "
                                    + (emissivePassEnabled ? "ON" : "OFF")
                    ),
                    true
            );
        }
    }

    public static void close() {
        Minecraft client = Minecraft.getInstance();

        for (RuntimeBundle bundle : BUNDLES.values()) {
            client.getTextureManager().release(bundle.capeTextureId);
            client.getTextureManager().release(bundle.elytraTextureId);
            client.getTextureManager().release(bundle.emissiveTextureId);
        }

        BUNDLES.clear();
        BY_CAPE_TEXTURE.clear();
        BY_ELYTRA_TEXTURE.clear();
        SKINS.clear();
        thicknessPresetIndex = 0;
        emissivePassEnabled = true;
    }

    private record CachedSkin(
            PlayerSkin source,
            String projectHash,
            PlayerSkin patched
    ) {
    }

    private static final class RuntimeBundle {
        private final ProofProject project;
        private final Identifier capeTextureId;
        private final Identifier elytraTextureId;
        private final Identifier emissiveTextureId;
        private final ClientAsset.ResourceTexture capeAsset;
        private final ClientAsset.ResourceTexture elytraAsset;
        private final NativeImage capeImage;
        private final NativeImage elytraImage;
        private final NativeImage emissiveImage;

        private DynamicTexture capeTexture;
        private DynamicTexture elytraTexture;
        private DynamicTexture emissiveTexture;
        private int phase = -1;

        private RuntimeBundle(
                ProofProject project,
                Identifier capeTextureId,
                Identifier elytraTextureId,
                Identifier emissiveTextureId,
                ClientAsset.ResourceTexture capeAsset,
                ClientAsset.ResourceTexture elytraAsset,
                NativeImage capeImage,
                NativeImage elytraImage,
                NativeImage emissiveImage
        ) {
            this.project = project;
            this.capeTextureId = capeTextureId;
            this.elytraTextureId = elytraTextureId;
            this.emissiveTextureId = emissiveTextureId;
            this.capeAsset = capeAsset;
            this.elytraAsset = elytraAsset;
            this.capeImage = capeImage;
            this.elytraImage = elytraImage;
            this.emissiveImage = emissiveImage;
        }
    }
}
