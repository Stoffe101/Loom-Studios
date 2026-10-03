package dev.loomstudios.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.Items;

/** Matches vanilla wing geometry and attachment; only authored highlights become fullbright. */
public final class LoomElytraGlowLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
    private final ElytraModel model;
    public LoomElytraGlowLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent,EntityModelSet models){
        super(parent);model=new ElytraModel(models.bakeLayer(ModelLayers.ELYTRA));
    }
    @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,AvatarRenderState state,float yaw,float pitch){
        if(state.isInvisible||!state.chestEquipment.is(Items.ELYTRA))return;
        var texture=PlayerCosmeticRenderer.getElytraEmissiveTexture(state);
        if(texture==null)return;
        pose.pushPose();
        try{
            pose.translate(0,0,.125F);
            collector.submitModel(model,state,pose,RenderTypes.entityTranslucentEmissive(texture,false),15728880,
                    OverlayTexture.NO_OVERLAY,state.outlineColor,(ModelFeatureRenderer.CrumblingOverlay)null);
        }finally{pose.popPose();}
    }
}
