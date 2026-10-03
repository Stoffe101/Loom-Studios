package dev.loomstudios.client.mixin;

import dev.loomstudios.client.render.LoomPreviewVisibility;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    // Suppress only the body submission. Vanilla cape/Elytra layers keep their normal state.
    @Inject(method="getRenderType",at=@At("HEAD"),cancellable=true)
    private void loom$previewBody(LivingEntityRenderState state,boolean body,boolean translucent,boolean outline,
                                  CallbackInfoReturnable<RenderType> callback){
        if(state instanceof LoomPreviewVisibility preview&&preview.loom$characterHidden())callback.setReturnValue(null);
    }
}
