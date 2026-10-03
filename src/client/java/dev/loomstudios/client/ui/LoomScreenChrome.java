package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** One workshop shell; artwork stays inside reserved chrome bounds. */
public final class LoomScreenChrome {
    private LoomScreenChrome() { }

    public static void renderBackdrop(GuiGraphics g, int width, int height) {
        g.fill(0, 0, width, height, LoomUiTheme.BACKDROP);
        LoomWorkshopArt.timber(g, 0, 0, 7, height - 18);
        LoomWorkshopArt.timber(g, width - 7, 0, width, height - 18);
        LoomWorkshopArt.timber(g, 0, height - 25, width, height - 18);
        for (int y : new int[]{headerHeight(LoomUiTheme.compact(width,height))-4, height-32}) {
            LoomWorkshopArt.bracket(g,0,y,7);
            LoomWorkshopArt.bracket(g,width-7,y,7);
        }
    }

    public static void workSurface(GuiGraphics g, int l, int t, int r, int b) {
        g.fill(l, t, r, b, 0xFF111E30);
        for (int x = l; x < r; x += 12) g.fill(x, t, Math.min(x + 1,r), b, 0x182E4968);
        for (int y = t; y < b; y += 12) g.fill(l, y, r, Math.min(y + 1,b), 0x182E4968);
    }

    public static int headerHeight(boolean compact) { return compact ? 36 : 56; }
    public static int navHeight(boolean compact) { return compact ? 22 : 26; }

    public static void renderEditorHeader(GuiGraphics g, int width, String title, boolean compact) {
        renderBrandHeader(g,width,title,compact);
    }

    public static void renderBrandHeader(GuiGraphics g, int width, String subtitle, boolean compact) {
        int h = headerHeight(compact), center = width / 2;
        LoomWorkshopArt.timber(g, 0, 0, width, h);
        // Recessed sign, steel corners, stitched pennants and two warm lanterns.
        int signWidth = Math.min(width - 96, compact ? 250 : 390);
        int l = center - signWidth / 2, r = center + signWidth / 2;
        g.fill(l-3,2,r+3,h-1,0xFF241C1B);
        g.fill(l,4,r,h-4,0xFF070F1C);
        g.fill(l+2,5,r-2,6,0xFF455665);
        g.fill(l+2,h-6,r-2,h-5,0xFF835B36);
        int unit = compact ? 2 : 4;
        LoomWorkshopArt.logo(g,center,compact ? 7 : 8,unit);
        LoomWorkshopArt.banner(g,l+12,compact ? 8 : 10,compact ? 10 : 14,compact ? 21 : 34,0xFF158EAB);
        LoomWorkshopArt.banner(g,r-(compact ? 22 : 26),compact ? 8 : 10,compact ? 10 : 14,compact ? 21 : 34,0xFF7440BC);
        for(int x : new int[]{l-3,r-4}) {
            LoomWorkshopArt.bracket(g,x,2,7);
            LoomWorkshopArt.bracket(g,x,h-10,7);
        }
        int lampY = compact ? 4 : 3;
        int lampScale = compact ? 1 : 2;
        LoomWorkshopArt.lantern(g,Math.max(17,l/2-4),lampY,lampScale);
        LoomWorkshopArt.lantern(g,width-Math.max(25,l/2+12),lampY,lampScale);
        var font = Minecraft.getInstance().font;
        String sub = font.plainSubstrByWidth(subtitle == null ? "" : subtitle,signWidth-64);
        int y = compact ? 24 : 41;
        g.drawCenteredString(font,Component.literal(sub),center,y,LoomUiTheme.TEXT);
        g.fill(l+20,y+4,center-font.width(sub)/2-8,y+5,LoomUiTheme.GOLD);
        g.fill(center+font.width(sub)/2+8,y+4,r-20,y+5,LoomUiTheme.GOLD);
    }

    public static void panel(GuiGraphics g, int l, int t, int r, int b) {
        if(r<=l || b<=t) return;
        g.fill(l,t,r,b,LoomUiTheme.BORDER);
        g.fill(l+1,t+1,r-1,b-1,LoomUiTheme.PANEL_INNER);
    }

    public static void panelHeader(GuiGraphics g, int l, int t, int r, String title) {
        g.fill(l+2,t+2,r-2,t+19,LoomUiTheme.PANEL_HEADER);
        g.fill(l+3,t+18,r-3,t+19,LoomUiTheme.BORDER_SOFT);
        g.drawString(Minecraft.getInstance().font,Component.literal(title),l+7,t+6,LoomUiTheme.TEXT,false);
    }

    public static void footer(GuiGraphics g, int width, int height, String status, String rightText) {
        int top = height - 18;
        var font = Minecraft.getInstance().font;
        g.fill(0,top,width,height,LoomUiTheme.PANEL_INNER);
        g.fill(0,top,width,top+1,LoomUiTheme.BORDER);
        String right = font.plainSubstrByWidth(rightText == null ? "" : rightText,width/3-12);
        int rw = font.width(right);
        int plaqueWidth = Math.min(184,width/3), plaqueLeft = (width-plaqueWidth)/2;
        String left = font.plainSubstrByWidth(status == null ? "" : status,plaqueLeft-16);
        g.drawString(font,Component.literal(left),8,top+5,LoomUiTheme.TEXT_MUTED,false);
        g.fill(plaqueLeft,top+1,plaqueLeft+plaqueWidth, height-1,0xFF9A7544);
        g.fill(plaqueLeft+2,top+2,plaqueLeft+plaqueWidth-2,height-2,0xFFE4C897);
        g.fill(plaqueLeft+4,top+3,plaqueLeft+plaqueWidth-4,top+4,0xFFF5DFB4);
        g.drawCenteredString(font,Component.literal("Weave higher stories."),width/2,top+5,0xFF51361F);
        g.drawString(font,Component.literal(right),width-rw-8,top+5,LoomUiTheme.ACCENT_ALT,false);
    }
}
