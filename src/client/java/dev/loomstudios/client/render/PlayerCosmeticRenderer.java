package dev.loomstudios.client.render;

import dev.loomstudios.client.network.ClientCosmeticSync;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.nio.charset.StandardCharsets;
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

    private static final Map<UUID, CachedSkin> SKINS = new HashMap<>();
    private static final Map<UUID, CachedSkin> PREVIEW_SKINS = new HashMap<>();

    private static boolean emissivePassEnabled = true;
    private static String lastLocalProjectHash;
    private static String previewProjectHash;
    private static LoomProject hashedPreviewProject;
    private static long previewHashComputations;
    private static String cachedPreviewHash, cachedTimelineHash;
    private static boolean cachedAlphaGuide;
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

        boolean previewingLocal=preview != null && ClientProjectWorkspace.isLocalPlayer(playerId);
        if (previewingLocal) {
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
                RuntimeCosmeticCache.getOrCompile(
                        client,
                        projectHash,
                        project,
                        previewingLocal ? preview.timelineTick() : null,
                        previewingLocal && dev.loomstudios.client.project.LoomPreferences.get().enabled("alphaGuide",true)
                );

        Map<UUID,CachedSkin> skins=previewingLocal?PREVIEW_SKINS:SKINS;
        CachedSkin cached = skins.get(playerId);

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
            skins.put(playerId, cached);
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

        float userScale = bundle.project.runtime().elytraThickness();
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

        if (bundle == null) {
            return null;
        }

        boolean hasStaticEmissive =
                bundle.project.cape().layers().stream()
                        .anyMatch(layer -> layer.emissive());
        boolean hasAnimatedEmissive =
                bundle.project.animation().tracks().stream()
                        .anyMatch(track ->
                                track.enabled()
                                        && track.channel()
                                        == dev.loomstudios.project.AnimationChannel.CAPE
                                        && track.effect()
                                        == dev.loomstudios.project.AnimationEffectType.EMISSIVE_GLOW
                        );

        if (!hasStaticEmissive && !hasAnimatedEmissive) {
            return null;
        }

        return bundle.emissiveTextureId;
    }

    public static Identifier getElytraEmissiveTexture(AvatarRenderState state){
        if(!emissivePassEnabled||state.skin==null||state.skin.elytra()==null)return null;
        var bundle=RuntimeCosmeticCache.byElytraTexture(state.skin.elytra().texturePath());
        return bundle!=null&&bundle.hasElytraEmissive?bundle.elytraEmissiveTextureId:null;
    }

    public static <T> T withPreviewProject(
            Minecraft client,
            LoomProject project,
            Supplier<T> action
    ) {
        return withPreviewProjectInternal(
                client,
                project,
                null,
                action
        );
    }

    public static <T> T withPreviewProjectAtTick(
            Minecraft client,
            LoomProject project,
            int timelineTick,
            Supplier<T> action
    ) {
        int clampedTick = Math.max(
                0,
                Math.min(
                        project.animation().durationTicks(),
                        timelineTick
                )
        );

        return withPreviewProjectInternal(
                client,
                project,
                clampedTick,
                action
        );
    }

    private static <T> T withPreviewProjectInternal(
            Minecraft client,
            LoomProject project,
            Integer timelineTick,
            Supplier<T> action
    ) {
        boolean guide=dev.loomstudios.client.project.LoomPreferences.get().enabled("alphaGuide",true);
        if (hashedPreviewProject != project || guide!=cachedAlphaGuide) {
            cachedAlphaGuide=guide;
            previewHashComputations++;
            hashedPreviewProject = project;
            cachedPreviewHash = ClientProjectWorkspace.isInitialized() && ClientProjectWorkspace.project() == project
                    ? ClientProjectWorkspace.projectHash() : project.hash();
            cachedPreviewHash = LoomProjectCodec.sha256((cachedPreviewHash + (guide?"#alpha-guide-preview":"#plain-preview")).getBytes(StandardCharsets.UTF_8));
            cachedTimelineHash = LoomProjectCodec.sha256((cachedPreviewHash + "#timeline-preview").getBytes(StandardCharsets.UTF_8));
        }
        String hash = timelineTick == null ? cachedPreviewHash : cachedTimelineHash;

        if (previewProjectHash != null
                && !previewProjectHash.equals(hash)
                && (!ClientProjectWorkspace.isInitialized()
                || !previewProjectHash.equals(
                        ClientProjectWorkspace.equippedProjectHash()
                ))) {
            RuntimeCosmeticCache.release(client, previewProjectHash);
        }

        previewProjectHash = hash;
        PreviewOverride previous = PREVIEW_OVERRIDE.get();
        PREVIEW_OVERRIDE.set(new PreviewOverride(
                project,
                hash,
                timelineTick
        ));

        try {
            return action.get();
        } finally {
            if (previous == null) PREVIEW_OVERRIDE.remove(); else PREVIEW_OVERRIDE.set(previous);
        }
    }

    public static void clearPreviewProject(Minecraft client) {
        PREVIEW_OVERRIDE.remove();
        PREVIEW_SKINS.clear();

        if (previewProjectHash != null
                && (!ClientProjectWorkspace.isInitialized()
                || !previewProjectHash.equals(
                        ClientProjectWorkspace.equippedProjectHash()
                ))) {
            RuntimeCosmeticCache.release(client, previewProjectHash);
        }

        previewProjectHash = null;
        hashedPreviewProject = null; cachedPreviewHash = null; cachedTimelineHash = null;
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
            String projectHash,
            Integer timelineTick
    ) {
    }
}
