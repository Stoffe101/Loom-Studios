package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

/** Real player skin face, with hat overlay; no per-frame image decode or texture creation. */
public final class LoomCharacterToggle {
    private LoomCharacterToggle(){}
    public static void draw(GuiGraphics g,int x,int y,int size,boolean visible,boolean hovered){
        g.fill(x,y,x+size,y+size,hovered?0xFF244353:0xFF172538);
        int edge=visible?LoomUiTheme.ACCENT:LoomUiTheme.BORDER;
        g.fill(x,y,x+size,y+1,edge);g.fill(x,y+size-1,x+size,y+size,edge);
        g.fill(x,y,x+1,y+size,edge);g.fill(x+size-1,y,x+size,y+size,edge);
        var player=Minecraft.getInstance().player;
        if(player!=null){var texture=player.getSkin().body().texturePath();
            int inner=size-4;
            g.blit(RenderPipelines.GUI_TEXTURED,texture,x+2,y+2,8,8,inner,inner,8,8,64,64);
            g.blit(RenderPipelines.GUI_TEXTURED,texture,x+2,y+2,40,8,inner,inner,8,8,64,64);
        }
    }
}
