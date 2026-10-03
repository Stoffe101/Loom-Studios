package dev.loomstudios.project;

import java.util.ArrayDeque;
/** Semantic pixels only: no atlas coordinates or UI state in rasterization. */
public final class PixelDrawing {
    public enum Tool {PENCIL,ERASER,FILL,EYEDROPPER,SELECT,LINE,RECTANGLE,CIRCLE}
    private PixelDrawing(){}
    public static PixelPatch shape(PixelPatch source,Tool tool,int x0,int y0,int x1,int y1,int brush,int color,boolean filled){
        int w=source.width(),h=source.height();int[] p=source.data();
        PixelShapes.Pixel plot=(x,y)->brush(p,w,h,x,y,brush,color);
        switch(tool){
            case LINE->{int x=x0,y=y0,dx=Math.abs(x1-x0),sx=x0<x1?1:-1,dy=-Math.abs(y1-y0),sy=y0<y1?1:-1,e=dx+dy;while(true){plot.accept(x,y);if(x==x1&&y==y1)break;int e2=2*e;if(e2>=dy){e+=dy;x+=sx;}if(e2<=dx){e+=dx;y+=sy;}}}
            case CIRCLE->PixelShapes.ellipse(x0,y0,x1,y1,filled,plot);
            case RECTANGLE->{for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)if(filled||x==Math.min(x0,x1)||x==Math.max(x0,x1)||y==Math.min(y0,y1)||y==Math.max(y0,y1))plot.accept(x,y);}
            case FILL->{if(x0>=0&&y0>=0&&x0<w&&y0<h){int old=p[y0*w+x0];if(old!=color){ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(y0*w+x0);p[y0*w+x0]=color;while(!queue.isEmpty()){int i=queue.remove();int x=i%w,y=i/w;int[] n={x>0?i-1:-1,x+1<w?i+1:-1,y>0?i-w:-1,y+1<h?i+w:-1};for(int j:n)if(j>=0&&p[j]==old){p[j]=color;queue.add(j);}}}}}
            case PENCIL,ERASER->plot.accept(x1,y1);
            default->{ }
        }
        return new PixelPatch(w,h,p);
    }
    private static void brush(int[] pixels,int w,int h,int x,int y,int size,int color){
        double radius=Math.max(.5,size/2.0);for(int by=0;by<size;by++)for(int bx=0;bx<size;bx++){int px=x-size/2+bx,py=y-size/2+by;if(px<0||py<0||px>=w||py>=h)continue;double dx=px-x,dy=py-y;if(size>2&&dx*dx+dy*dy>radius*radius)continue;pixels[py*w+px]=color;}
    }
}
