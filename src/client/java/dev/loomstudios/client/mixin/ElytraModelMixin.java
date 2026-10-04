package dev.loomstudios.client.mixin;

import dev.loomstudios.client.render.PlayerCosmeticRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraModel.class)
public abstract class ElytraModelMixin {
    @Shadow @Final private ModelPart leftWing;
    @Shadow @Final private ModelPart rightWing;

    @Unique private java.util.List<ModelPart.Cube> loomStudios$vanillaRight;
    @Unique private java.util.List<ModelPart.Cube> loomStudios$authoredRight;
    @Unique private java.util.List<ModelPart.Cube> loomStudios$vanillaLeft;
    @Unique private java.util.List<ModelPart.Cube> loomStudios$authoredLeft;
    @Inject(method="setupAnim",at=@At("HEAD"))
    private void loomStudios$chooseWingUvs(HumanoidRenderState state,CallbackInfo ci){
        var accessor=(ModelPartCubesAccessor)(Object)rightWing;
        if(loomStudios$vanillaRight==null){loomStudios$vanillaRight=accessor.loomStudios$getCubes();loomStudios$authoredRight=((ModelPartCubesAccessor)(Object)dev.loomstudios.client.render.LoomWingGeometry.rightWing()).loomStudios$getCubes();}
        var leftAccessor=(ModelPartCubesAccessor)(Object)leftWing;
        if(loomStudios$vanillaLeft==null){loomStudios$vanillaLeft=leftAccessor.loomStudios$getCubes();loomStudios$authoredLeft=((ModelPartCubesAccessor)(Object)dev.loomstudios.client.render.LoomWingGeometry.leftWing()).loomStudios$getCubes();}
        boolean authored=state instanceof AvatarRenderState avatar&&PlayerCosmeticRenderer.usesLoomElytra(avatar);
        leftAccessor.loomStudios$setCubes(authored?loomStudios$authoredLeft:loomStudios$vanillaLeft);
        accessor.loomStudios$setCubes(authored?loomStudios$authoredRight:loomStudios$vanillaRight);
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void loomStudios$applyThickness(HumanoidRenderState state, CallbackInfo ci) {
        float depthScale = 1.0F;

        if (state instanceof AvatarRenderState avatarState) {
            depthScale = PlayerCosmeticRenderer.getElytraThicknessScale(avatarState);
        }

        this.leftWing.zScale = depthScale;
        this.rightWing.zScale = depthScale;
    }
}
