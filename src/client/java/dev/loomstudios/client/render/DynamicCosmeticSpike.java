package dev.loomstudios.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.network.ClientCosmeticSync;
import dev.loomstudios.project.LoomProject;
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
 * Runtime compiler/cache bridge shared by the foundation proof and Phase-1
 * project core.
 */
public final class DynamicCosmeticSpike {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;

    /**
     * Minecraft's raw Elytra box is intentionally chunky. With Loom's fully
     * painted edge faces, raw zScale=1 looks much fatter than the normal
     * vanilla Elytra texture. 100% Loom thickness therefore maps to this
     * calibrated vanilla-looking visual baseline.
     *
     * Non-Loom Elytras remain at raw vanilla zScale=1.
     */
    public static final float ELYTRA_VISUAL_BASELINE_Z_SCALE = 0.5F;

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
            int phase = (int)(
                    (gameTime / bundle.project.runtime().animationPeriodTicks()) % 4L
            );

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
        LoomProject project = ClientCosmeticSync.projectFor(playerId);
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
            LoomProject project
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
                        Identifier.fromNamespaceAndPath(
                                LoomStudios.MOD_ID,
                                "cape_asset_" + suffix
                        ),
                        capeId
                ),
                new ClientAsset.ResourceTexture(
                        Identifier.fromNamespaceAndPath(
                                LoomStudios.MOD_ID,
                                "elytra_asset_" + suffix
                        ),
                        elytraId
                ),
                new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false),
                new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false),
                new NativeImage(NativeImage.Format.RGBA, WIDTH, HEIGHT, false)
        );

        bundle.phase = 0;
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
        int[] pixels = LoomTextureCompiler.compile(
                bundle.project.cape(),
                bundle.phase,
                bundle.project.runtime().hueCycleEnabled(),
                false
        );
        writePixels(bundle.capeImage, pixels);
    }

    private static void redrawElytra(RuntimeBundle bundle) {
        int[] pixels = LoomTextureCompiler.compile(
                bundle.project.elytra(),
                0,
                false,
                false
        );
        writePixels(bundle.elytraImage, pixels);
    }

    private static void redrawEmissive(RuntimeBundle bundle) {
        if (!bundle.project.runtime().emissiveEnabled()) {
            clear(bundle.emissiveImage);
            return;
        }

        int[] pixels = LoomTextureCompiler.compile(
                bundle.project.cape(),
                bundle.phase,
                bundle.project.runtime().hueCycleEnabled(),
                true
        );
        writePixels(bundle.emissiveImage, pixels);

        // SPIKE-06 moving shimmer remains procedural for now. Phase 6 will
        // replace this with real animation tracks/procedural effect nodes.
        int shimmerX = 49 + bundle.phase * 3;
        drawRect(bundle.emissiveImage, shimmerX, 4, 2, 24, 0xCCFFFFFF);
    }

    private static void writePixels(NativeImage target, int[] pixels) {
        if (pixels.length != WIDTH * HEIGHT) {
            throw new IllegalArgumentException("Unexpected compiled texture dimensions");
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                target.setPixel(x, y, pixels[y * WIDTH + x]);
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

    public static float getElytraThicknessScale(AvatarRenderState state) {
        if (state.skin == null || state.skin.elytra() == null) {
            return 1.0F;
        }

        RuntimeBundle bundle = BY_ELYTRA_TEXTURE.get(state.skin.elytra().texturePath());
        if (bundle == null) {
            return 1.0F;
        }

        Minecraft client = Minecraft.getInstance();
        float userScale;

        if (client.player != null && state.id == client.player.getId()) {
            userScale = THICKNESS_PRESETS[thicknessPresetIndex];
        } else {
            userScale = bundle.project.runtime().elytraThickness();
        }

        return ELYTRA_VISUAL_BASELINE_Z_SCALE * userScale;
    }

    public static Identifier getCapeEmissiveTexture(AvatarRenderState state) {
        if (!emissivePassEnabled
                || state.skin == null
                || state.skin.cape() == null) {
            return null;
        }

        RuntimeBundle bundle = BY_CAPE_TEXTURE.get(state.skin.cape().texturePath());
        if (bundle == null || !bundle.project.runtime().emissiveEnabled()) {
            return null;
        }

        return bundle.emissiveTextureId;
    }

    public static void cycleElytraThickness(Minecraft client) {
        thicknessPresetIndex =
                (thicknessPresetIndex + 1) % THICKNESS_PRESETS.length;

        if (client.player != null) {
            int percent = Math.round(
                    THICKNESS_PRESETS[thicknessPresetIndex] * 100.0F
            );
            client.player.displayClientMessage(
                    Component.literal(
                            "Loom Studios Elytra thickness: "
                                    + percent
                                    + "%"
                                    + (percent == 100 ? " (vanilla-like)" : "")
                    ),
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
        private final LoomProject project;
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
                LoomProject project,
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
