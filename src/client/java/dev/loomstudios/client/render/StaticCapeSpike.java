package dev.loomstudios.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.Optional;

public final class StaticCapeSpike {
    private static final Identifier CAPE_TEXTURE =
            Identifier.fromNamespaceAndPath("loom-studios", "textures/cape/spike_test.png");

    private static final ClientAsset.ResourceTexture CAPE_ASSET =
            new ClientAsset.ResourceTexture(
                    Identifier.fromNamespaceAndPath("loom-studios", "spike_test_cape"),
                    CAPE_TEXTURE
            );

    private static PlayerSkin cachedSource;
    private static PlayerSkin cachedPatched;

    private StaticCapeSpike() {
    }

    public static void apply(Avatar avatar, AvatarRenderState state) {
        if (Minecraft.getInstance().player != avatar || state.skin == null) {
            return;
        }

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

        // Development-spike behavior only. The real editor/equip system will respect
        // explicit Loom Studios settings and vanilla cape visibility preferences.
        state.showCape = true;
    }

    public static void clearCache() {
        cachedSource = null;
        cachedPatched = null;
    }
}
