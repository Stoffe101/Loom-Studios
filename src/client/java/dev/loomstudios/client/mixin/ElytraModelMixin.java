package dev.loomstudios.client.mixin;

import dev.loomstudios.client.render.DynamicCosmeticSpike;
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

        if (state instanceof AvatarRenderState avatarState
                && DynamicCosmeticSpike.isUsingLoomElytra(avatarState)) {
            depthScale = DynamicCosmeticSpike.getElytraThicknessScale();
        }

        // ElytraModel instances are reused, so always restore vanilla scale for
        // non-Loom entities instead of only mutating Loom players.
        this.leftWing.zScale = depthScale;
        this.rightWing.zScale = depthScale;
    }
}
