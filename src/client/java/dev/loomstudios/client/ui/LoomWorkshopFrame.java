package dev.loomstudios.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Resource-backed workshop chrome; slices stay inside reserved header, rim and footer bounds. */
final class LoomWorkshopFrame {
    private static final Identifier IMAGE=Identifier.fromNamespaceAndPath("loom-studios","textures/ui/workshop-frame.png");
    private static final int WIDTH=1672,HEIGHT=941,SIDE=90,TOP=220,BOTTOM=110;
    private LoomWorkshopFrame() {}
    static void draw(GuiGraphics g,int width,int height) {
        int head=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height));
        int bottom=height-25,mid=Math.max(0,bottom-head);
        // Keep lanterns, banners and metal brackets at their source aspect ratio.
        // Only the undecorated sign face grows; timber repeats into the remaining space.
        int sign=Math.min(width-190,LoomUiTheme.compact(width,height)?270:410);
        int cap=Math.round(560.0F*head/TOP),left=(width-sign)/2;
        int tile=Math.max(1,Math.round(40.0F*head/TOP));
        for(int x=0;x<width;x+=tile)slice(g,x,0,Math.min(tile,width-x),head,270,0,40,TOP);
        slice(g,left-cap,0,cap,head,0,0,560,TOP);
        slice(g,left,0,sign,head,560,0,552,TOP);
        slice(g,left+sign,0,cap,head,1112,0,560,TOP);
        slice(g,0,head,7,mid,0,TOP,SIDE,HEIGHT-TOP-BOTTOM);
        slice(g,width-7,head,7,mid,WIDTH-SIDE,TOP,SIDE,HEIGHT-TOP-BOTTOM);
        slice(g,0,bottom,7,7,0,HEIGHT-BOTTOM,SIDE,BOTTOM);
        slice(g,7,bottom,width-14,7,SIDE,HEIGHT-BOTTOM,WIDTH-2*SIDE,BOTTOM);
        slice(g,width-7,bottom,7,7,WIDTH-SIDE,HEIGHT-BOTTOM,SIDE,BOTTOM);
    }
    private static void slice(GuiGraphics g,int x,int y,int w,int h,int u,int v,int sw,int sh) {
        if(w>0&&h>0)g.blit(RenderPipelines.GUI_TEXTURED,IMAGE,x,y,(float)u,(float)v,w,h,sw,sh,WIDTH,HEIGHT);
    }
}
