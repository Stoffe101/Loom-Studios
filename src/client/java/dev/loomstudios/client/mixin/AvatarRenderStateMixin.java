package dev.loomstudios.client.mixin;

import dev.loomstudios.client.render.LoomPreviewVisibility;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements LoomPreviewVisibility {
    @Unique private boolean loom$hideCharacter;
    public boolean loom$characterHidden(){return loom$hideCharacter;}
    public void loom$characterHidden(boolean value){loom$hideCharacter=value;}
}
