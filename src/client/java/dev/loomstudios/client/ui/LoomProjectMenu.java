package dev.loomstudios.client.ui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
public final class LoomProjectMenu {
    private LoomProjectMenu(){}
    public static void render(GuiGraphics g,Font font,int x,int y,int mx,int my,String[] labels,int danger){
        dev.loomstudios.client.ui.premium.PremiumControls.occlude(g,x,y,158,labels.length*23);
        LoomScreenChrome.panel(g,x,y,x+158,y+labels.length*23);
        for(int i=0;i<labels.length;i++){int row=y+i*23;boolean hot=mx>=x&&mx<x+158&&my>=row&&my<row+23;
            if(hot){g.fill(x+2,row+1,x+156,row+22,0xFF244353);g.fill(x+2,row+1,x+4,row+22,i==danger?0xFFFF718B:LoomUiTheme.ACCENT);}
            int color=i==danger?0xFFFF718B:hot?LoomUiTheme.ACCENT:LoomUiTheme.TEXT;
            LoomButton.Icon icon=switch(i){case 0->LoomButton.Icon.PENCIL;case 1->LoomButton.Icon.PENCIL;case 2->LoomButton.Icon.EMISSIVE;case 3->LoomButton.Icon.COPY;case 4->i==danger?LoomButton.Icon.DELETE:LoomButton.Icon.CLOSE;case 5->LoomButton.Icon.CLOSE;case 6->LoomButton.Icon.EQUIP;case 7->LoomButton.Icon.FOLDER;default->LoomButton.Icon.SAVE;};
            LoomButton.drawIcon(g,x+8,row+4,14,icon,color);
            g.drawString(font,font.plainSubstrByWidth(labels[i],122),x+28,row+8,color,false);
        }
    }
}
