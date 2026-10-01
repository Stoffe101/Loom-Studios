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
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

/**
 * SPIKE-06 additive/fullbright cape pass.
 *
 * <p>The base cape remains entirely vanilla. This layer only submits a second
 * PlayerCapeModel with the project's generated emissive mask and mirrors the
 * same chest-equipment offset/suppression rules as vanilla CapeLayer.</p>
 */
public final class LoomCapeGlowLayer
        extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final PlayerCapeModel model;
    private final EquipmentAssetManager equipmentAssets;

    public LoomCapeGlowLayer(
            RenderLayerParent<AvatarRenderState, PlayerModel> renderer,
            EntityModelSet modelSet,
            EquipmentAssetManager equipmentAssets
    ) {
        super(renderer);
        this.model = new PlayerCapeModel(modelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = equipmentAssets;
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
        if (state.isInvisible
                || !state.showCape
                || hasEquipmentLayer(
                        state.chestEquipment,
                        EquipmentClientInfo.LayerType.WINGS
                )) {
            return;
        }

        Identifier emissiveTexture = DynamicCosmeticSpike.getCapeEmissiveTexture(state);
        if (emissiveTexture == null) {
            return;
        }

        poseStack.pushPose();

        if (hasEquipmentLayer(
                state.chestEquipment,
                EquipmentClientInfo.LayerType.HUMANOID
        )) {
            poseStack.translate(0.0F, -0.053125F, 0.06875F);
        }

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

    private boolean hasEquipmentLayer(
            ItemStack stack,
            EquipmentClientInfo.LayerType layerType
    ) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return false;
        }

        ResourceKey assetId = (ResourceKey)equippable.assetId().get();
        EquipmentClientInfo clientInfo = this.equipmentAssets.get(assetId);
        return !clientInfo.getLayers(layerType).isEmpty();
    }
}
