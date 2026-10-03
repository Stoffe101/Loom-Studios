package dev.loomstudios.project;
import java.util.*;
/** Original layered pixel templates, not copied raster assets from the illustrated references. */
public final class TemplateCatalog {
    public enum Kind {BLANK,GRADIENT,NATURE,SPACE,FANTASY,EMBLEM;
        public String label(){return name().substring(0,1)+name().substring(1).toLowerCase(java.util.Locale.ROOT);}}
    private TemplateCatalog(){}
    public static LoomProject create(Kind kind,long now){
        LoomProject p=LoomProjectFactory.blank(kind.label()+" Cape",now);if(kind==Kind.BLANK)return p;
        p=ProjectResizer.resizeCape(p,CanvasResolution.ULTRA);
        UUID base=p.cape().layers().getFirst().id();
        p=SurfaceEdits.cape(p,base,CapeUvRegion.OUTSIDE,patch->{int w=patch.width(),h=patch.height();int[] data=new int[w*h];
            int top=kind==Kind.NATURE?0xFF172B66:0xFF111738,bottom=kind==Kind.NATURE?0xFF71DBD5:0xFF7132A6;
            for(int y=0;y<h;y++)for(int x=0;x<w;x++){double t=y/(double)(h-1);int color=0xFF000000;for(int shift:new int[]{0,8,16})color|=(int)Math.round(((top>>>shift)&255)*(1-t)+((bottom>>>shift)&255)*t)<<shift;data[y*w+x]=color;}
            return new PixelPatch(w,h,data);});
        if(kind==Kind.GRADIENT)return p;
        p=ProjectEdits.addCapeLayer(p,kind==Kind.NATURE?"Landscape":"Motif");UUID motif=p.cape().layers().getLast().id();
        p=SurfaceEdits.cape(p,motif,CapeUvRegion.OUTSIDE,patch->{int w=patch.width(),h=patch.height();int[] data=patch.data();
            for(int y=0;y<h;y++)for(int x=0;x<w;x++){
                int color=0;
                if(kind==Kind.NATURE){if((x-29)*(x-29)+(y-13)*(y-13)<35)color=0xFFFFDA7A;if(y>35+Math.abs(x-12)*.6)color=0xFF4577A6;if(y>45+Math.sin(x*.25)*6)color=0xFF286B61;if(y>55+Math.sin(x*.4)*3)color=0xFF174B45;}
                if(kind==Kind.SPACE){if((x-18)*(x-18)+(y-24)*(y-24)<81&&(x-23)*(x-23)+(y-21)*(y-21)>75)color=0xFF7DEAF4;}
                if(kind==Kind.FANTASY){int diamond=Math.abs(x-20)+Math.abs(y-31);if(diamond>=14&&diamond<=16||Math.abs(x-20)<=1&&Math.abs(y-31)<23)color=0xFFFFCC77;}
                if(kind==Kind.EMBLEM){if(Math.abs(x-20)<=1&&y>=14&&y<=48||Math.abs(x-20)+Math.abs(y-30)>=11&&Math.abs(x-20)+Math.abs(y-30)<=13)color=0xFFB7EFFF;}
                if(color!=0)data[y*w+x]=color;
            }
            return new PixelPatch(w,h,data);});
        p=ProjectEdits.addCapeLayer(p,"Border & highlights");UUID trim=p.cape().layers().getLast().id();
        p=SurfaceEdits.cape(p,trim,CapeUvRegion.OUTSIDE,patch->{int w=patch.width(),h=patch.height();int[] data=patch.data();int accent=kind==Kind.FANTASY?0xFFFFCA6A:0xFF22D7E8;
            for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(x==1||x==w-2||y==h-3)data[y*w+x]=accent;
            if(kind!=Kind.NATURE)for(int i=0;i<12;i++){int x=5+(i*17)%30,y=5+(i*23)%51;if(data[y*w+x]==0){data[y*w+x]=i%3==0?0xFFFFFFFF:accent;if(i%3==0){data[y*w+x-1]=accent;data[y*w+x+1]=accent;data[(y-1)*w+x]=accent;data[(y+1)*w+x]=accent;}}}
            return new PixelPatch(w,h,data);});
        return p;
    }
}
