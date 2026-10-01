package dev.loomstudios.client.render;

import dev.loomstudios.client.network.ClientCosmeticSync;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Production render-state facade for Loom cape/Elytra cosmetics.
 */
public final class PlayerCosmeticRenderer {
    public static final float ELYTRA_VISUAL_BASELINE_Z_SCALE = 0.5F;

    private static final float[] THICKNESS_PRESETS = {
            1.0F, 0.75F, 0.5F, 0.25F, 1.5F
    };

    private static final Map<UUID, CachedSkin> SKINS = new HashMap<>();

    private static int thicknessPresetIndex;
    private static boolean emissivePassEnabled = true;
    private static String lastLocalProjectHash;
    private static String previewProjectHash;
    private static final ThreadLocal<PreviewOverride> PREVIEW_OVERRIDE = new ThreadLocal<>();

    private PlayerCosmeticRenderer() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        ClientProjectWorkspace.ensure(client.player.getUUID());

        String currentLocalHash = ClientProjectWorkspace.equippedProjectHash();
        if (lastLocalProjectHash != null
                && !lastLocalProjectHash.equals(currentLocalHash)) {
            RuntimeCosmeticCache.release(client, lastLocalProjectHash);
            SKINS.remove(client.player.getUUID());
        }
        lastLocalProjectHash = currentLocalHash;

        RuntimeCosmeticCache.tick(client);
    }

    public static void apply(Avatar avatar, AvatarRenderState state) {
        if (state.skin == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            ClientProjectWorkspace.ensure(client.player.getUUID());
        }

        UUID playerId = avatar.getUUID();
        PreviewOverride preview = PREVIEW_OVERRIDE.get();

        LoomProject project;
        String projectHash;

        if (preview != null && ClientProjectWorkspace.isLocalPlayer(playerId)) {
            project = preview.project();
            projectHash = preview.projectHash();
        } else {
            project = ClientCosmeticSync.projectFor(playerId);
            projectHash = ClientCosmeticSync.projectHashFor(playerId);
        }

        if (project == null || projectHash == null) {
            return;
        }

        RuntimeCosmeticCache.RuntimeBundle bundle =
                RuntimeCosmeticCache.getOrCompile(client, projectHash, project);

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

    public static float getElytraThicknessScale(AvatarRenderState state) {
        if (state.skin == null || state.skin.elytra() == null) {
            return 1.0F;
        }

        RuntimeCosmeticCache.RuntimeBundle bundle =
                RuntimeCosmeticCache.byElytraTexture(
                        state.skin.elytra().texturePath()
                );

        if (bundle == null) {
            return 1.0F;
        }

        Minecraft client = Minecraft.getInstance();
        float userScale = bundle.project.runtime().elytraThickness();

        if (client.player != null
                && state.id == client.player.getId()
                && ClientProjectWorkspace.isInitialized()
                && bundle.projectHash.equals(
                        ClientProjectWorkspace.equippedProjectHash()
                )) {
            userScale = THICKNESS_PRESETS[thicknessPresetIndex];
        }

        return ELYTRA_VISUAL_BASELINE_Z_SCALE * userScale;
    }

    public static Identifier getCapeEmissiveTexture(AvatarRenderState state) {
        if (!emissivePassEnabled
                || state.skin == null
                || state.skin.cape() == null) {
            return null;
        }

        RuntimeCosmeticCache.RuntimeBundle bundle =
                RuntimeCosmeticCache.byCapeTexture(
                        state.skin.cape().texturePath()
                );

        if (bundle == null || !bundle.project.runtime().emissiveEnabled()) {
            return null;
        }

        return bundle.emissiveTextureId;
    }

    public static <T> T withPreviewProject(
            Minecraft client,
            LoomProject project,
            Supplier<T> action
    ) {
        String hash = ClientProjectWorkspace.isInitialized()
                && ClientProjectWorkspace.project() == project
                ? ClientProjectWorkspace.projectHash()
                : project.hash();

        if (previewProjectHash != null
                && !previewProjectHash.equals(hash)
                && (!ClientProjectWorkspace.isInitialized()
                || !previewProjectHash.equals(
                        ClientProjectWorkspace.equippedProjectHash()
                ))) {
            RuntimeCosmeticCache.release(client, previewProjectHash);
        }

        previewProjectHash = hash;
        PREVIEW_OVERRIDE.set(new PreviewOverride(project, hash));

        try {
            return action.get();
        } finally {
            PREVIEW_OVERRIDE.remove();
        }
    }

    public static void clearPreviewProject(Minecraft client) {
        PREVIEW_OVERRIDE.remove();

        if (previewProjectHash != null
                && (!ClientProjectWorkspace.isInitialized()
                || !previewProjectHash.equals(
                        ClientProjectWorkspace.equippedProjectHash()
                ))) {
            RuntimeCosmeticCache.release(client, previewProjectHash);
        }

        previewProjectHash = null;
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

    public static void invalidatePlayer(UUID playerId) {
        SKINS.remove(playerId);
    }

    public static void close(Minecraft client) {
        SKINS.clear();
        thicknessPresetIndex = 0;
        emissivePassEnabled = true;
        clearPreviewProject(client);
        RuntimeCosmeticCache.close(client);
        lastLocalProjectHash = null;
    }

    private record CachedSkin(
            PlayerSkin source,
            String projectHash,
            PlayerSkin patched
    ) {
    }

    private record PreviewOverride(
            LoomProject project,
            String projectHash
    ) {
    }
}
