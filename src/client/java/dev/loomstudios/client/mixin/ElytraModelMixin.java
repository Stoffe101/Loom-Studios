package dev.loomstudios.client.mixin;

import dev.loomstudios.client.render.PlayerCosmeticRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraModel.class)
public abstract class ElytraModelMixin {
    @Shadow @Final private ModelPart leftWing;
    @Shadow @Final private ModelPart rightWing;

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
