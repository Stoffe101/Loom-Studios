package dev.loomstudios.client.ui.premium;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Smooth studio labels within existing bounds; text fields and tooltips keep native font providers. */
public final class PremiumText {
    private PremiumText() {}
    public static void drawString(GuiGraphics g,Font font,String value,int x,int y,int ink,boolean shadow) {
        if(!PremiumControls.label(g,value,x,y,Math.max(1,font.width(value)),10,ink,false))
            g.drawString(font,value,x,y,ink,shadow);
    }
    public static void drawString(GuiGraphics g,Font font,Component value,int x,int y,int ink,boolean shadow) {
        if(!PremiumControls.label(g,value.getString(),x,y,Math.max(1,font.width(value)),10,ink,false))
            g.drawString(font,value,x,y,ink,shadow);
    }
    public static void drawCenteredString(GuiGraphics g,Font font,Component value,int x,int y,int ink) {
        int width=Math.max(1,font.width(value));
        if(!PremiumControls.label(g,value.getString(),x-width/2,y,width,10,ink,true))
            g.drawCenteredString(font,value,x,y,ink);
    }
    public static void drawCenteredString(GuiGraphics g,Font font,String value,int x,int y,int ink) {
        int width=Math.max(1,font.width(value));
        if(!PremiumControls.label(g,value,x-width/2,y,width,10,ink,true))g.drawCenteredString(font,value,x,y,ink);
    }
}
