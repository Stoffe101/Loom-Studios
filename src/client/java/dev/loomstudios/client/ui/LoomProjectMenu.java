package dev.loomstudios.client.ui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
public final class LoomProjectMenu {
    public static final int WIDTH=174, GUTTER=6, ROW_HEIGHT=24;
    public static int height(int count){return GUTTER*2+count*ROW_HEIGHT;}
    public static int actionAt(double px,double py,int x,int y,int count){
        if(px<x+GUTTER||px>=x+WIDTH-GUTTER||py<y+GUTTER||py>=y+height(count)-GUTTER)return -1;
        return (int)(py-y-GUTTER)/ROW_HEIGHT;
    }
    private LoomProjectMenu(){}
    public static void render(GuiGraphics g,Font font,int x,int y,int mx,int my,String[] labels,int danger){
        dev.loomstudios.client.ui.premium.PremiumControls.occlude(g,x,y,WIDTH,height(labels.length));
        LoomScreenChrome.panel(g,x,y,x+WIDTH,y+height(labels.length));
        for(int i=0;i<labels.length;i++){int row=y+GUTTER+i*ROW_HEIGHT;boolean hot=actionAt(mx,my,x,y,labels.length)==i;
            if(hot){g.fill(x+GUTTER,row+1,x+WIDTH-GUTTER,row+22,0xFF244353);g.fill(x+GUTTER,row+1,x+GUTTER+2,row+22,i==danger?0xFFFF718B:LoomUiTheme.ACCENT);}
            int color=i==danger?0xFFFF718B:hot?LoomUiTheme.ACCENT:LoomUiTheme.TEXT;
            LoomButton.Icon icon=switch(i){case 0->LoomButton.Icon.PENCIL;case 1->LoomButton.Icon.PENCIL;case 2->LoomButton.Icon.EMISSIVE;case 3->LoomButton.Icon.COPY;case 4->i==danger?LoomButton.Icon.DELETE:LoomButton.Icon.CLOSE;case 5->LoomButton.Icon.CLOSE;case 6->LoomButton.Icon.EQUIP;case 7->LoomButton.Icon.FOLDER;default->LoomButton.Icon.SAVE;};
            if(dev.loomstudios.client.ui.premium.PremiumControls.button(g,x+GUTTER,row+1,WIDTH-2*GUTTER,21,labels[i],icon,
                    false,true,hot,false,false,false,i==danger))continue;
            LoomButton.drawIcon(g,x+GUTTER+6,row+4,14,icon,color);
            dev.loomstudios.client.ui.premium.PremiumText.drawString(g,font,font.plainSubstrByWidth(labels[i],122),x+GUTTER+26,row+8,color,false);
        }
    }
}
