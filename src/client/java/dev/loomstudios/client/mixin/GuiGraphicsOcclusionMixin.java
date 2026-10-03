package dev.loomstudios.client.mixin;

import dev.loomstudios.client.ui.premium.PremiumControls;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve native paint order when studio labels/controls are collected for one late overlay. */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsOcclusionMixin {
    /** Flush before Minecraft moves deferred tooltips into their higher stratum. */
    @Inject(method="renderDeferredElements()V",at=@At("HEAD"))
    private void loom$finishBeforeTooltips(CallbackInfo callback) {
        PremiumControls.finish((GuiGraphics)(Object)this);
    }

    @Inject(method="fill(IIIII)V",at=@At("HEAD"))
    private void loom$occludeEarlierPaint(int x1,int y1,int x2,int y2,int color,CallbackInfo callback) {
        if((color>>>24)<254)return;
        PremiumControls.occlude((GuiGraphics)(Object)this,Math.min(x1,x2),Math.min(y1,y2),
                Math.abs(x2-x1),Math.abs(y2-y1));
    }
}
