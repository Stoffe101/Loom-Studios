package dev.loomstudios.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * SPIKE-06 additive/fullbright cape pass.
 *
 * <p>The base cape remains entirely vanilla. This layer only submits a second
 * PlayerCapeModel with the project's generated emissive mask.</p>
 */
public final class LoomCapeGlowLayer
        extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final PlayerCapeModel model;

    public LoomCapeGlowLayer(
            RenderLayerParent<AvatarRenderState, PlayerModel> renderer,
            EntityModelSet modelSet
    ) {
        super(renderer);
        this.model = new PlayerCapeModel(modelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            AvatarRenderState state,
            float yRot,
            float xRot
    ) {
        if (state.isInvisible || !state.showCape || state.chestEquipment.is(Items.ELYTRA)) {
            return;
        }

        Identifier emissiveTexture = DynamicCosmeticSpike.getCapeEmissiveTexture(state);
        if (emissiveTexture == null) {
            return;
        }

        poseStack.pushPose();
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(emissiveTexture, false),
                15728880,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                (ModelFeatureRenderer.CrumblingOverlay)null
        );
        poseStack.popPose();
    }
}
