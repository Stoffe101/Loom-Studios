package dev.loomstudios.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Owns compiled GPU-backed runtime textures keyed by immutable project hash.
 */
public final class RuntimeCosmeticCache {
    private static final Map<String, RuntimeBundle> BUNDLES = new HashMap<>();
    private static final Map<Identifier, RuntimeBundle> BY_CAPE_TEXTURE = new HashMap<>();
    private static final Map<Identifier, RuntimeBundle> BY_ELYTRA_TEXTURE = new HashMap<>();

    private RuntimeCosmeticCache() {
    }

    public static RuntimeBundle getOrCompile(
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
                projectHash,
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
                new NativeImage(
                        NativeImage.Format.RGBA,
                        project.cape().width(),
                        project.cape().height(),
                        false
                ),
                new NativeImage(
                        NativeImage.Format.RGBA,
                        project.elytra().width(),
                        project.elytra().height(),
                        false
                ),
                new NativeImage(
                        NativeImage.Format.RGBA,
                        project.cape().width(),
                        project.cape().height(),
                        false
                )
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

    public static void tick(Minecraft client) {
        if (client.level == null) {
            return;
        }

        long gameTime = client.level.getGameTime();

        for (RuntimeBundle bundle : BUNDLES.values()) {
            int phase = (int)(
                    (gameTime / bundle.project.runtime().animationPeriodTicks()) % 4L
            );

            if (phase == bundle.phase) {
                continue;
            }

            bundle.phase = phase;
            redrawCape(bundle);
            redrawEmissive(bundle);
            bundle.capeTexture.upload();
            bundle.emissiveTexture.upload();
        }
    }

    public static RuntimeBundle byCapeTexture(Identifier texture) {
        return BY_CAPE_TEXTURE.get(texture);
    }

    public static RuntimeBundle byElytraTexture(Identifier texture) {
        return BY_ELYTRA_TEXTURE.get(texture);
    }

    public static void release(Minecraft client, String projectHash) {
        RuntimeBundle bundle = BUNDLES.remove(projectHash);
        if (bundle == null) {
            return;
        }

        BY_CAPE_TEXTURE.remove(bundle.capeTextureId);
        BY_ELYTRA_TEXTURE.remove(bundle.elytraTextureId);

        client.getTextureManager().release(bundle.capeTextureId);
        client.getTextureManager().release(bundle.elytraTextureId);
        client.getTextureManager().release(bundle.emissiveTextureId);
    }

    public static void close(Minecraft client) {
        for (RuntimeBundle bundle : BUNDLES.values()) {
            client.getTextureManager().release(bundle.capeTextureId);
            client.getTextureManager().release(bundle.elytraTextureId);
            client.getTextureManager().release(bundle.emissiveTextureId);
        }

        BUNDLES.clear();
        BY_CAPE_TEXTURE.clear();
        BY_ELYTRA_TEXTURE.clear();
    }

    private static void redrawCape(RuntimeBundle bundle) {
        writePixels(
                bundle.capeImage,
                LoomTextureCompiler.compile(
                        bundle.project.cape(),
                        bundle.phase,
                        bundle.project.runtime().hueCycleEnabled(),
                        false
                )
        );
    }

    private static void redrawElytra(RuntimeBundle bundle) {
        writePixels(
                bundle.elytraImage,
                LoomTextureCompiler.compile(
                        bundle.project.elytra(),
                        0,
                        false,
                        false
                )
        );
    }

    private static void redrawEmissive(RuntimeBundle bundle) {
        if (!bundle.project.runtime().emissiveEnabled()) {
            clear(bundle.emissiveImage);
            return;
        }

        writePixels(
                bundle.emissiveImage,
                LoomTextureCompiler.compile(
                        bundle.project.cape(),
                        bundle.phase,
                        bundle.project.runtime().hueCycleEnabled(),
                        true
                )
        );
    }

    private static void writePixels(NativeImage target, int[] pixels) {
        int width = target.getWidth();
        int height = target.getHeight();

        if (pixels.length != width * height) {
            throw new IllegalArgumentException("Unexpected compiled texture dimensions");
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                target.setPixel(x, y, pixels[y * width + x]);
            }
        }
    }

    private static void clear(NativeImage target) {
        for (int y = 0; y < target.getHeight(); y++) {
            for (int x = 0; x < target.getWidth(); x++) {
                target.setPixel(x, y, 0x00000000);
            }
        }
    }

    public static final class RuntimeBundle {
        final String projectHash;
        final LoomProject project;
        final Identifier capeTextureId;
        final Identifier elytraTextureId;
        final Identifier emissiveTextureId;
        final ClientAsset.ResourceTexture capeAsset;
        final ClientAsset.ResourceTexture elytraAsset;
        final NativeImage capeImage;
        final NativeImage elytraImage;
        final NativeImage emissiveImage;

        DynamicTexture capeTexture;
        DynamicTexture elytraTexture;
        DynamicTexture emissiveTexture;
        int phase = -1;

        RuntimeBundle(
                String projectHash,
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
            this.projectHash = projectHash;
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
