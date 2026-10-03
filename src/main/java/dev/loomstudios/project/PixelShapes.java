package dev.loomstudios.project;

/** Pixel-centre ellipse raster; inclusive drag bounds, symmetric in both axes. */
public final class PixelShapes {
    @FunctionalInterface public interface Pixel { void accept(int x, int y); }
    private PixelShapes() { }
    public static void ellipse(int x0, int y0, int x1, int y1, boolean filled, Pixel pixel) {
        int left = Math.min(x0,x1), right = Math.max(x0,x1);
        int top = Math.min(y0,y1), bottom = Math.max(y0,y1);
        double cx = (left+right)/2.0, cy = (top+bottom)/2.0;
        double rx = (right-left+1)/2.0, ry = (bottom-top+1)/2.0;
        for (int y=top;y<=bottom;y++) for (int x=left;x<=right;x++) {
            if (!inside(x,y,cx,cy,rx,ry)) continue;
            if (filled || !inside(x-1,y,cx,cy,rx,ry) || !inside(x+1,y,cx,cy,rx,ry)
                    || !inside(x,y-1,cx,cy,rx,ry) || !inside(x,y+1,cx,cy,rx,ry)) pixel.accept(x,y);
        }
    }
    private static boolean inside(int x,int y,double cx,double cy,double rx,double ry) {
        double dx=(x-cx)/rx,dy=(y-cy)/ry; return dx*dx+dy*dy<=1.0;
    }
}
