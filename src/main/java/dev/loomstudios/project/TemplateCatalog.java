package dev.loomstudios.project;
import java.util.*;
/** Original layered pixel templates, not copied raster assets from the illustrated references. */
public final class TemplateCatalog {
    public enum Kind {BLANK,GRADIENT,NATURE,SPACE,FANTASY,EMBLEM,AURORA,DRAGON,PHOENIX,CRYSTAL,CAT,FOX,FROG,HEART;
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
                if(kind==Kind.AURORA){double band=21+Math.sin(x*.18)*9;if(Math.abs(y-band)<7)color=y<band?0xFF70FFC6:0xFF54B9EE;if(y>49+Math.abs(x-24)*.4)color=0xFF142F40;}
                if(kind==Kind.DRAGON){if(y>15&&y<46&&Math.abs(x-(19+Math.sin(y*.17)*7))<4)color=0xFFE65469;if(y>24&&y<39&&Math.abs(x-20)>5&&Math.abs(x-20)<18-(y-24)*.8)color=0xFFB52D58;if(y>=16&&y<=23&&x>=23&&x<=30)color=0xFFFFB655;if(y==18&&x==28)color=0xFFFFFFFF;}
                if(kind==Kind.PHOENIX){int d=Math.abs(x-20);if(y>17&&y<47&&d<3||y>23&&y<39&&d<19-(y-23)*.75)color=y%5<2?0xFFFFDF75:0xFFFF794A;if(y>40&&y<56&&d<3+(y-40)/3&&d>(y-40)/4)color=0xFFFFAC4B;}
                if(kind==Kind.CRYSTAL){int d=Math.abs(x-20);if(y>11&&y<51&&d<Math.min((y-11)*.6,(51-y)*.7))color=x<20?0xFF8EF2FF:0xFFAD83FF;if(Math.abs(x-20)<1&&y>11&&y<51)color=0xFFF4F4FF;}
                if(kind==Kind.CAT||kind==Kind.FOX||kind==Kind.FROG){int cx=x-20,cy=y-31;boolean face=cx*cx/1.3+cy*cy<110;boolean ears=y>=16&&y<=26&&((x>=9&&x<=16&&y>16+Math.abs(x-12))||(x>=24&&x<=31&&y>16+Math.abs(x-28)));if(face||ears)color=kind==Kind.CAT?0xFFF2CAE0:kind==Kind.FOX?0xFFFFAB62:0xFF93E8AB;if(kind==Kind.FROG&&(Math.pow(x-13,2)+Math.pow(y-22,2)<20||Math.pow(x-27,2)+Math.pow(y-22,2)<20))color=0xFF93E8AB;if((Math.abs(cx-5)<=1||Math.abs(cx+5)<=1)&&Math.abs(cy+1)<=1)color=0xFF263049;if(Math.abs(cx)<=1&&cy>=3&&cy<=4)color=0xFFE77FA4;if(cy==5&&Math.abs(cx)<4)color=0xFF263049;}
                if(kind==Kind.HEART){double hx=(x-20)/11.0,hy=(31-y)/11.0;double a=hx*hx+hy*hy-1;if(a*a*a-hx*hx*hy*hy*hy<0)color=x<20?0xFFFF8CBD:0xFFEC5A9A;}
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
