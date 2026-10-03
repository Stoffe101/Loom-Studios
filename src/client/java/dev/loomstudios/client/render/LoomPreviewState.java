package dev.loomstudios.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

/** Own the snapshot submitted to deferred GUI rendering, never the world's state. */
public final class LoomPreviewState {
    private LoomPreviewState() { }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EntityRenderState extract(LivingEntity entity) {
        EntityRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState();
        renderer.extractRenderState(entity,state,1.0F);
        state.lightCoords = 15728880; state.shadowPieces.clear(); state.outlineColor = 0;
        if (state instanceof LivingEntityRenderState living) living.isInvisible = false;
        return state;
    }
    public static void orient(EntityRenderState state, float yaw) {
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F + yaw;
            living.yRot = 0.0F; living.xRot = 0.0F;
            living.pose = Pose.STANDING;
            living.boundingBoxWidth /= living.scale;
            living.boundingBoxHeight /= living.scale;
            living.scale = 1.0F;
        }
    }
}
