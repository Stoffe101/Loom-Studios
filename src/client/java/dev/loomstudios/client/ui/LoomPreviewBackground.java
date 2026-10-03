package dev.loomstudios.client.ui;

import dev.loomstudios.client.project.LoomPreferences;
import net.minecraft.client.gui.GuiGraphics;

public enum LoomPreviewBackground {
    SCENIC("Scenic"),LIGHT("Neutral light"),DARK("Neutral dark"),CHECKER("Checkerboard");
    private final String label;
    LoomPreviewBackground(String label){this.label=label;}
    public String label(){return label;}
    public static LoomPreviewBackground current(){try{return valueOf(LoomPreferences.get().choice("previewBackground","SCENIC"));}catch(IllegalArgumentException e){return SCENIC;}}
    public static void cycle(){try{LoomPreferences.get().set("previewBackground",values()[(current().ordinal()+1)%values().length].name());}catch(java.io.IOException e){dev.loomstudios.client.project.LoomDiagnostics.record("Preview preference",e);}}
    public static void render(GuiGraphics g,int l,int t,int r,int b){
        var kind=current();if(kind==SCENIC){LoomWorkshopArt.previewScene(g,l,t,r,b);return;}
        g.fill(l,t,r,b,kind==LIGHT?0xFFE0E5EC:0xFF151922);
        if(kind==CHECKER)for(int y=t;y<b;y+=12)for(int x=l;x<r;x+=12)g.fill(x,y,Math.min(r,x+12),Math.min(b,y+12),((x-l)/12+(y-t)/12)%2==0?0xFF48515E:0xFF252C36);
    }
}
