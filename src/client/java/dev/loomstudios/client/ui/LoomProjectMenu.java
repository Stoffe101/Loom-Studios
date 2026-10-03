package dev.loomstudios.client.ui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
public final class LoomProjectMenu {
    private LoomProjectMenu(){}
    public static void render(GuiGraphics g,Font font,int x,int y,int mx,int my,String[] labels,int danger){
        LoomScreenChrome.panel(g,x,y,x+158,y+labels.length*23);
        for(int i=0;i<labels.length;i++){int row=y+i*23;boolean hot=mx>=x&&mx<x+158&&my>=row&&my<row+23;
            if(hot){g.fill(x+2,row+1,x+156,row+22,0xFF244353);g.fill(x+2,row+1,x+4,row+22,i==danger?0xFFFF718B:LoomUiTheme.ACCENT);}
            g.drawString(font,labels[i],x+10,row+8,i==danger?0xFFFF718B:hot?LoomUiTheme.ACCENT:LoomUiTheme.TEXT,false);
        }
    }
}
