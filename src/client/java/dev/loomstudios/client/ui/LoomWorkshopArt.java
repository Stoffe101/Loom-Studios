package dev.loomstudios.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Code-native pixel artwork: scales with the GUI and never owns input or content bounds. */
public final class LoomWorkshopArt {
    private LoomWorkshopArt() { }

    public static void timber(GuiGraphics g, int l, int t, int r, int b) {
        g.fill(l, t, r, b, 0xFF563822);
        for (int y = t; y < b; y += 7) {
            g.fill(l, y, r, Math.min(b, y + 2), 0xFF895737);
            g.fill(l, Math.min(b, y + 5), r, Math.min(b, y + 7), 0xFF34251E);
            for (int x = l + ((y / 7) % 2) * 23; x < r; x += 47) {
                g.fill(x, y + 2, Math.min(r, x + 1), Math.min(b, y + 5), 0xFF35251B);
                g.fill(Math.min(r,x + 4), y + 3, Math.min(r,x + 18), Math.min(b,y + 4), 0xFF6A452B);
            }
        }
        g.fill(l, t, r, t + 1, 0xFFB3814E);
        g.fill(l, b - 1, r, b, 0xFF261B17);
    }

    public static void bracket(GuiGraphics g, int x, int y, int size) {
        g.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF19202B);
        g.fill(x, y, x + size, y + size, 0xFF596273);
        g.fill(x, y, x + size, y + 1, 0xFF9C9AA3);
        g.fill(x, y, x + 1, y + size, 0xFF85868F);
        g.fill(x + 2, y + size - 2, x + size, y + size, 0xFF313C4F);
        int c = size / 2;
        g.fill(x + c - 1, y + c - 1, x + c + 2, y + c + 2, 0xFF24262C);
        g.fill(x + c - 1, y + c - 1, x + c + 1, y + c, LoomUiTheme.GOLD);
    }

    public static void lantern(GuiGraphics g, int x, int y, int scale) {
        g.fill(x - scale * 3, y + scale * 4, x + scale * 11, y + scale * 17, 0x12FFBC54);
        g.fill(x, y, x + 8 * scale, y + 2 * scale, 0xFF5B6170);
        g.fill(x + 3 * scale, y + 2 * scale, x + 5 * scale, y + 5 * scale, 0xFF323744);
        g.fill(x - scale, y + 5 * scale, x + 9 * scale, y + 15 * scale, 0xFF363845);
        g.fill(x + scale, y + 6 * scale, x + 7 * scale, y + 14 * scale, 0xFFF29936);
        g.fill(x + 2 * scale, y + 7 * scale, x + 6 * scale, y + 13 * scale, 0xFFFFD55C);
        g.fill(x + 3 * scale, y + 8 * scale, x + 5 * scale, y + 12 * scale, 0xFFFFF4BD);
        g.fill(x - scale, y + 15 * scale, x + 9 * scale, y + 17 * scale, 0xFF5E5050);
    }

    public static void banner(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x - 2, y, x + w + 2, y + 2, LoomUiTheme.GOLD);
        g.fill(x, y + 2, x + w, y + h - 3, 0xFFAC884F);
        g.fill(x + 1, y + 2, x + w - 1, y + h - 4, 0xFF17364C);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 5, accent);
        for (int i = 0; i < 3; i++) g.fill(x + i + 1, y + h - 4 + i, x + w - i - 1, y + h - 3 + i, LoomUiTheme.GOLD);
        int cx = x + w / 2, cy = y + h / 2;
        g.fill(cx, cy - 3, cx + 1, cy + 4, 0xFFFFEABC);
        g.fill(cx - 3, cy, cx + 4, cy + 1, 0xFFFFEABC);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFEABC);
    }

    public static void logo(GuiGraphics g, int center, int top, int unit) {
        String[] letters = {
            "10000100001000010000100001000011111", // L
            "00000000000111010001100011000101110", // o
            "00000000000111010001100011000101110", // o
            "00000000001101010101101011010110101", // m
            "00000000000000000000000000000000000",
            "01111100001000001110000010000111110", // S
            "00100001001111100100001000010000011", // t
            "00000000001000110001100011000101111", // u
            "00001000010111110001100011000101111", // d
            "00100000000110000100001000010001110", // i
            "00000000000111010001100011000101110", // o
            "00000000000111110000011100000111110"  // s
        };
        int l = center - (letters.length * 6 - 1) * unit / 2;
        for (int a = 0; a < letters.length; a++) {
            int color = mix(0xFF24E4F0, 0xFFD879FF, a / (float)(letters.length - 1));
            for (int i = 0; i < 35; i++) if (letters[a].charAt(i) == '1') {
                int x = l + (a * 6 + i % 5) * unit, y = top + (i / 5) * unit;
                g.fill(x - 1, y - 1, x + unit + 1, y + unit + 1, 0x2835D5EA);
                g.fill(x + 1, y + 2, x + unit + 1, y + unit + 2, 0xFF183A66);
                g.fill(x, y, x + unit, y + unit, color);
            }
        }
    }

    public static int mix(int a, int b, float amount) {
        int red = Math.round(((a >> 16)&255)*(1-amount)+((b >> 16)&255)*amount);
        int green = Math.round(((a >> 8)&255)*(1-amount)+((b >> 8)&255)*amount);
        int blue = Math.round((a&255)*(1-amount)+(b&255)*amount);
        return 0xFF000000 | red<<16 | green<<8 | blue;
    }

    private static final net.minecraft.resources.Identifier PREVIEW_SCENE=net.minecraft.resources.Identifier.fromNamespaceAndPath(
            "loom-studios","textures/ui/preview-courtyard.png");
    public static void previewScene(GuiGraphics g,int l,int t,int r,int b) {
        if(b<=t||r<=l)return;
        double aspect=(r-l)/(double)(b-t);
        int sourceW=1024,sourceH=1536;
        if(aspect>1024/1536.0)sourceH=Math.max(1,(int)Math.round(1024/aspect));
        else sourceW=Math.max(1,(int)Math.round(1536*aspect));
        int u=(1024-sourceW)/2,v=1536-sourceH;
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,PREVIEW_SCENE,l,t,(float)u,(float)v,
                r-l,b-t,sourceW,sourceH,1024,1536);
    }
}
