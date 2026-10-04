package dev.loomstudios.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.AnimationChannel;
import dev.loomstudios.project.AnimationEffectType;
import dev.loomstudios.project.AnimationEvaluator;
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
    private static final Map<String, RuntimeBundle> BUNDLES = new java.util.LinkedHashMap<>(16,.75f,true);
    private static final Map<Identifier, RuntimeBundle> BY_CAPE_TEXTURE = new HashMap<>();
    private static final Map<Identifier, RuntimeBundle> BY_ELYTRA_TEXTURE = new HashMap<>();

    private static boolean hasImageFrames(dev.loomstudios.project.LoomCanvas canvas){return canvas.layers().stream().anyMatch(l->l.visible()&&l.imageData()!=null&&!l.imageData().frames().isEmpty());}
    private static long bundleBytes(RuntimeBundle bundle){return dev.loomstudios.project.ProjectMemory.artworkBytes(bundle.project)+8L*((long)bundle.project.cape().width()*bundle.project.cape().height()+(long)bundle.project.elytra().width()*bundle.project.elytra().height());}
    private static void trimBundles(Minecraft client){
        long retained=BUNDLES.values().stream().mapToLong(RuntimeCosmeticCache::bundleBytes).sum();
        var iterator=BUNDLES.entrySet().iterator();
        while((retained>96L*1024*1024||BUNDLES.size()>32)&&BUNDLES.size()>1){var old=iterator.next().getValue();retained-=bundleBytes(old);iterator.remove();BY_CAPE_TEXTURE.remove(old.capeTextureId);BY_ELYTRA_TEXTURE.remove(old.elytraTextureId);client.getTextureManager().release(old.capeTextureId);client.getTextureManager().release(old.elytraTextureId);client.getTextureManager().release(old.emissiveTextureId);client.getTextureManager().release(old.elytraEmissiveTextureId);}
    }
    private static int frameStamp(dev.loomstudios.project.LoomCanvas canvas,int tick){int stamp=1;for(var layer:canvas.layers())if(layer.visible()&&layer.imageData()!=null&&!layer.imageData().frames().isEmpty())stamp=31*stamp+System.identityHashCode(layer.imageData().frameAt(tick));return stamp;}
    private RuntimeCosmeticCache() {
    }

    public static RuntimeBundle getOrCompile(
            Minecraft client,
            String projectHash,
            LoomProject project
    ) {
        return getOrCompile(
                client,
                projectHash,
                project,
                null
        );
    }

    public static RuntimeBundle getOrCompile(
            Minecraft client,
            String projectHash,
            LoomProject project,
            Integer fixedTimelineTick
    ) {
        return getOrCompile(client,projectHash,project,fixedTimelineTick,false);
    }

    public static RuntimeBundle getOrCompile(Minecraft client,String projectHash,LoomProject project,
            Integer fixedTimelineTick,boolean alphaGuide) {
        RuntimeBundle existing = BUNDLES.get(projectHash);
        if (existing != null) {
            existing.lastUseNanos=System.nanoTime();
            if (fixedTimelineTick != null
                    && !fixedTimelineTick.equals(
                            existing.fixedTimelineTick
                    )) {
                existing.fixedTimelineTick = fixedTimelineTick;
                existing.capeTimelineTick = fixedTimelineTick;
                existing.elytraTimelineTick = fixedTimelineTick;
                existing.legacyPhase = 0;
                if(existing.project.animation().hasEnabledTracks(AnimationChannel.CAPE)||hasImageFrames(existing.project.cape())) {
                    redrawCape(existing,fixedTimelineTick); redrawEmissive(existing,fixedTimelineTick);
                    existing.capeTexture.upload(); existing.emissiveTexture.upload();
                }
                if(existing.project.animation().hasEnabledTracks(AnimationChannel.ELYTRA)||hasImageFrames(existing.project.elytra())) {
                    redrawElytra(existing,fixedTimelineTick); redrawElytraEmissive(existing,fixedTimelineTick);
                    existing.elytraTexture.upload(); if(existing.hasElytraEmissive)existing.elytraEmissiveTexture.upload();
                }
            }
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
                ),
                fixedTimelineTick
        );

        int initialTimelineTick = fixedTimelineTick == null
                ? 0
                : fixedTimelineTick;
        bundle.legacyPhase = 0;
        bundle.capeTimelineTick = initialTimelineTick;
        bundle.elytraTimelineTick = initialTimelineTick;
        bundle.alphaGuide = alphaGuide;
        redrawCape(bundle, initialTimelineTick);
        redrawElytra(bundle, initialTimelineTick);
        redrawEmissive(bundle, initialTimelineTick);
        redrawElytraEmissive(bundle,initialTimelineTick);

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

        bundle.elytraEmissiveTexture=new DynamicTexture(()->"Loom Studios emissive Elytra "+suffix,bundle.elytraEmissiveImage);
        client.getTextureManager().register(bundle.elytraEmissiveTextureId,bundle.elytraEmissiveTexture);
        client.getTextureManager().register(capeId, bundle.capeTexture);
        client.getTextureManager().register(elytraId, bundle.elytraTexture);
        client.getTextureManager().register(emissiveId, bundle.emissiveTexture);

        bundle.capeTexture.upload();
        bundle.elytraTexture.upload();
        bundle.emissiveTexture.upload();
        bundle.elytraEmissiveTexture.upload();

        BUNDLES.put(projectHash, bundle);
        BY_CAPE_TEXTURE.put(capeId, bundle);
        BY_ELYTRA_TEXTURE.put(elytraId, bundle);
        trimBundles(client);

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
            if (bundle.fixedTimelineTick != null || System.nanoTime()-bundle.lastUseNanos>2_000_000_000L) {
                continue;
            }

            int legacyPhase = (int)(
                    (gameTime
                            / bundle.project.runtime()
                                    .animationPeriodTicks())
                            % 4L
            );
            int timelineTick = hasImageFrames(bundle.project.cape())||hasImageFrames(bundle.project.elytra())
                    ? (int)Math.floorMod(gameTime, Integer.MAX_VALUE)
                    : AnimationEvaluator.timelineTick(bundle.project.animation(),gameTime);

            boolean capeAnimated =
                    bundle.project.animation()
                            .hasEnabledTracks(AnimationChannel.CAPE) || hasImageFrames(bundle.project.cape())
                            || bundle.project.runtime()
                                    .hueCycleEnabled();

            boolean elytraAnimated =
                    bundle.project.animation()
                            .hasEnabledTracks(AnimationChannel.ELYTRA) || hasImageFrames(bundle.project.elytra());

            int capeStamp=bundle.project.animation().hasEnabledTracks(AnimationChannel.CAPE)?timelineTick:frameStamp(bundle.project.cape(),timelineTick);
            int wingStamp=bundle.project.animation().hasEnabledTracks(AnimationChannel.ELYTRA)?timelineTick:frameStamp(bundle.project.elytra(),timelineTick);
            if (capeAnimated
                    && (bundle.capeFrameStamp != capeStamp
                    || bundle.legacyPhase != legacyPhase)) {
                bundle.capeTimelineTick = timelineTick;bundle.capeFrameStamp=capeStamp;
                bundle.legacyPhase = legacyPhase;
                redrawCape(bundle, timelineTick);
                redrawEmissive(bundle, timelineTick);
                bundle.capeTexture.upload();
                bundle.emissiveTexture.upload();
            }

            if (elytraAnimated
                    && bundle.elytraFrameStamp != wingStamp) {
                bundle.elytraTimelineTick = timelineTick;bundle.elytraFrameStamp=wingStamp;
                redrawElytra(bundle, timelineTick);
                redrawElytraEmissive(bundle,timelineTick);
                bundle.elytraTexture.upload();
                if(bundle.hasElytraEmissive)bundle.elytraEmissiveTexture.upload();
            }
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
        client.getTextureManager().release(bundle.elytraEmissiveTextureId);
    }

    public static void close(Minecraft client) {
        for (RuntimeBundle bundle : BUNDLES.values()) {
            client.getTextureManager().release(bundle.capeTextureId);
            client.getTextureManager().release(bundle.elytraTextureId);
            client.getTextureManager().release(bundle.emissiveTextureId);
            client.getTextureManager().release(bundle.elytraEmissiveTextureId);
        }

        BUNDLES.clear();
        BY_CAPE_TEXTURE.clear();
        BY_ELYTRA_TEXTURE.clear();
    }

    private static void redrawCape(
            RuntimeBundle bundle,
            int timelineTick
    ) {
        writePixels(
                bundle.capeImage,
                LoomTextureCompiler.compileAnimated(
                        bundle.project,
                        AnimationChannel.CAPE,
                        timelineTick,
                        bundle.legacyPhase,
                        false
                )
        );
        if (bundle.alphaGuide) applyAlphaGuide(bundle.capeImage);
    }

    private static void redrawElytra(
            RuntimeBundle bundle,
            int timelineTick
    ) {
        writePixels(
                bundle.elytraImage,
                LoomTextureCompiler.compileAnimated(
                        bundle.project,
                        AnimationChannel.ELYTRA,
                        timelineTick,
                        0,
                        false
                )
        );
        if (bundle.alphaGuide) applyAlphaGuide(bundle.elytraImage);
    }

    private static void redrawEmissive(
            RuntimeBundle bundle,
            int timelineTick
    ) {
        boolean hasEmissiveLayer =
                bundle.project.cape().layers().stream()
                        .anyMatch(layer -> layer.emissive());

        boolean hasAuthoredEmissive =
                bundle.project.animation().tracks().stream()
                        .anyMatch(track ->
                                track.enabled()
                                        && track.channel()
                                        == AnimationChannel.CAPE
                                        && track.effect()
                                        == AnimationEffectType.EMISSIVE_GLOW
                        );

        if (!hasEmissiveLayer && !hasAuthoredEmissive) {
            clear(bundle.emissiveImage);
            return;
        }

        writePixels(
                bundle.emissiveImage,
                LoomTextureCompiler.compileAnimated(
                        bundle.project,
                        AnimationChannel.CAPE,
                        timelineTick,
                        bundle.legacyPhase,
                        true
                )
        );
    }

    private static void redrawElytraEmissive(RuntimeBundle bundle,int tick){
        if(bundle.hasElytraEmissive)writePixels(bundle.elytraEmissiveImage,LoomTextureCompiler.compileAnimated(bundle.project,AnimationChannel.ELYTRA,tick,0,true));
        else clear(bundle.elytraEmissiveImage);
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

    private static void applyAlphaGuide(NativeImage image) {
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++) {
            int color=image.getPixel(x,y), alpha=color>>>24;
            if(alpha==255)continue;
            int checker=((x/2+y/2)&1)==0 ? 0xFF697B91 : 0xFF3E4D65;
            int result=0xFF000000;
            for(int shift:new int[]{0,8,16}) {
                int channel=(((color>>>shift)&255)*alpha+((checker>>>shift)&255)*(255-alpha)+127)/255;
                result|=channel<<shift;
            }
            image.setPixel(x,y,result);
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
        private long lastUseNanos=System.nanoTime();
        private int capeFrameStamp=Integer.MIN_VALUE,elytraFrameStamp=Integer.MIN_VALUE;
        final String projectHash;
        final LoomProject project;
        final Identifier capeTextureId;
        final Identifier elytraTextureId;
        final Identifier emissiveTextureId;
        final Identifier elytraEmissiveTextureId;
        final boolean hasElytraEmissive;
        final ClientAsset.ResourceTexture capeAsset;
        final ClientAsset.ResourceTexture elytraAsset;
        final NativeImage capeImage;
        final NativeImage elytraImage;
        final NativeImage emissiveImage;
        final NativeImage elytraEmissiveImage;

        boolean alphaGuide;
        DynamicTexture capeTexture;
        DynamicTexture elytraTexture;
        DynamicTexture emissiveTexture;
        DynamicTexture elytraEmissiveTexture;
        int legacyPhase = -1;
        int capeTimelineTick = -1;
        int elytraTimelineTick = -1;
        Integer fixedTimelineTick;

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
                NativeImage emissiveImage,
                Integer fixedTimelineTick
        ) {
            this.projectHash = projectHash;
            this.project = project;
            this.capeTextureId = capeTextureId;
            this.elytraTextureId = elytraTextureId;
            this.emissiveTextureId = emissiveTextureId;
            this.elytraEmissiveTextureId=Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID,"dynamic/elytra_emissive_"+projectHash.substring(0,16));
            this.elytraEmissiveImage=new NativeImage(NativeImage.Format.RGBA,project.elytra().width(),project.elytra().height(),false);
            this.hasElytraEmissive=project.elytra().layers().stream().anyMatch(l->l.emissive())||project.animation().tracks().stream().anyMatch(t->t.enabled()&&t.channel()==AnimationChannel.ELYTRA&&t.effect()==AnimationEffectType.EMISSIVE_GLOW);
            this.capeAsset = capeAsset;
            this.elytraAsset = elytraAsset;
            this.capeImage = capeImage;
            this.elytraImage = elytraImage;
            this.emissiveImage = emissiveImage;
            this.fixedTimelineTick = fixedTimelineTick;
        }
    }
}
