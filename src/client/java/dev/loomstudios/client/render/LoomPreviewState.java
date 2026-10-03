package dev.loomstudios.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

/** Own the snapshot submitted to deferred GUI rendering, never the world's state. */
public final class LoomPreviewState {
    public enum PreviewPose { STANDING, OPEN, GLIDING;
        public PreviewPose next(){return values()[(ordinal()+1)%values().length];}
        public String label(){return switch(this){case STANDING->"Standing";case OPEN->"Open wings";case GLIDING->"Gliding";};}
    }
    private LoomPreviewState() { }
    public static void pose(EntityRenderState state,PreviewPose pose) {
        if(state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState avatar){
            avatar.isFallFlying=pose==PreviewPose.GLIDING;
            avatar.fallFlyingTimeInTicks=pose==PreviewPose.GLIDING?20.0F:0.0F;
            avatar.elytraRotX=0.2617994F;
            avatar.elytraRotY=pose==PreviewPose.STANDING?0.0F:0.12F;
            avatar.elytraRotZ=pose==PreviewPose.STANDING?-0.2617994F:-0.9F;
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static EntityRenderState extract(LivingEntity entity) {
        EntityRenderer renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState();
        renderer.extractRenderState(entity,state,1.0F);
        state.lightCoords = 15728880; state.shadowPieces.clear(); state.outlineColor = 0;
        if (state instanceof LivingEntityRenderState living) living.isInvisible = false;
        if(state instanceof net.minecraft.client.renderer.entity.state.ArmedEntityRenderState armed){
            armed.leftHandItemState.clear();armed.rightHandItemState.clear();
            armed.leftHandItemStack=net.minecraft.world.item.ItemStack.EMPTY;armed.rightHandItemStack=net.minecraft.world.item.ItemStack.EMPTY;
        }
        return state;
    }
    public static void visibility(EntityRenderState state,boolean visible){
        ((LoomPreviewVisibility)state).loom$characterHidden(!visible);
        if(!visible&&state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState avatar){
            avatar.headItem.clear();avatar.heldOnHead.clear();avatar.wornHeadType=null;avatar.wornHeadProfile=null;
            avatar.headEquipment=net.minecraft.world.item.ItemStack.EMPTY;avatar.legsEquipment=net.minecraft.world.item.ItemStack.EMPTY;avatar.feetEquipment=net.minecraft.world.item.ItemStack.EMPTY;
            avatar.parrotOnLeftShoulder=null;avatar.parrotOnRightShoulder=null;avatar.arrowCount=0;avatar.stingerCount=0;
        }
    }
    public static void orient(EntityRenderState state, float yaw) {
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F;
            living.yRot = 0.0F; living.xRot = 0.0F;
            living.pose = Pose.STANDING;
            living.boundingBoxWidth /= living.scale;
            living.boundingBoxHeight /= living.scale;
            living.scale = 1.0F;
        }
    }
}
