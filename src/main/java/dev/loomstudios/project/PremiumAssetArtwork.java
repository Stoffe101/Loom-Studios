package dev.loomstudios.project;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Original detailed pixel-art sprites for the Asset Library's quality-first collection.
 *
 * <p>Artwork is built at 28–48 source pixels with multiple opaque/translucent
 * palette steps. All geometry is rasterized deterministically, once at class
 * initialization. Do not enlarge tiny 3px monochrome glyphs and call them HD.
 * Project placements copy the full ARGB artwork into their own Image layer.
 */
public final class PremiumAssetArtwork {
  public record Artwork(String category, String name, String keywords, PixelPatch pixels) {}
  private static final List<Artwork> ALL = create();
  public static List<Artwork> all() { return ALL; }
  private PremiumAssetArtwork() {}

  private static Artwork art(String category, String name, String keywords, Painter p) {
    return new Artwork(category, name, keywords, p.result());
  }

  private static List<Artwork> create() {
    var a = new ArrayList<Artwork>();
    a.add(art("Celestial", "Moonstone Crescent", "moon lunar night gold craters", crescent()));
    a.add(art("Celestial", "Frostfire Star", "sparkle star eight point cyan white", frostStar()));
    a.add(art("Celestial", "Starbound Constellation", "stars cluster constellation gold violet", constellation()));
    a.add(art("Celestial", "Astral Planet", "planet ring orbit space indigo", planet()));
    a.add(art("Clouds & Mist", "Silver Lining", "cloud puffy moonlit fluffy", silverCloud()));
    a.add(art("Clouds & Mist", "Tempest Cloud", "storm rain lightning thundercloud", tempest()));
    a.add(art("Clouds & Mist", "Moonlit Haze", "mist fog veil smoke wisps translucent", haze()));
    a.add(art("Clouds & Mist", "Aurora Ribbon", "northern lights sky glow translucent", aurora()));
    a.add(art("Nature", "Snowkissed Fir", "pine spruce tree snow alpine winter", fir()));
    a.add(art("Nature", "Sakura Bloom", "cherry pink blossom tree spring", sakura()));
    a.add(art("Nature", "Enchanted Oak", "oak lush tree forest fireflies", oak()));
    a.add(art("Nature", "Mushroom Grove", "mushroom fungus magical forest spores", mushrooms()));
    a.add(art("Fantasy", "Prismatic Crystal", "magic shard gem teal violet crystal", crystal()));
    a.add(art("Fantasy", "Phoenix Feather", "fire gold amber feather flame", feather()));
    a.add(art("Fantasy", "Runic Halo", "rune glowing magic circle sigil cyan", halo()));
    a.add(art("Decorations", "Roseglass Heart", "heart love blush pink red shine", heart()));
    a.add(art("Decorations", "Golden Laurel", "laurel wreath crest heraldry leaves", laurel()));
    return List.copyOf(a);
  }

  private static Painter crescent() {
    Painter p = new Painter(36,36);
    p.oval(7,3,28,32,0x4A7DAAFF);
    p.oval(9,4,28,31,0x669EC8FF);
    for(int y=2;y<34;y++)for(int x=4;x<34;x++) {
      double outer=Math.pow((x-17)/13.0,2)+Math.pow((y-17)/15.0,2);
      double inner=Math.pow((x-24)/11.6,2)+Math.pow((y-13)/14.2,2);
      if(outer<=1 && inner>1) {
        int c=outer>.88?0xFFF5FAFF:x<13?0xFFFFDA83:y<12?0xFFFFF1BE:0xFFF5C56F;
        p.put(x,y,c);
      }
    }
    p.dot(9,17,0xFFFFE7A3);p.dot(11,25,0xFFEBAE61);
    p.star(29,7,3,0xFFF6F6FF,0xFF69DDF3);
    p.star(29,28,2,0xFFFAE5A2,0xFFFFBC60);
    p.dot(4,29,0xBDEAD8FF);p.dot(31,18,0xFF94C8F8);
    return p;
  }

  private static Painter frostStar() {
    Painter p=new Painter(36,36);
    p.diamond(18,18,16,0x4565BDE5);
    p.diamond(18,18,12,0xFF2D89C3);
    p.diamond(18,18,9,0xFF76DAF0);
    p.diamond(18,18,6,0xFFD1F8FF);
    p.diamond(18,18,3,0xFFFFFFFF);
    p.line(18,2,18,33,0xFF72DBF8);p.line(2,18,33,18,0xFF72DBF8);
    p.line(5,5,30,30,0xA947A7E7);p.line(30,5,5,30,0xA947A7E7);
    p.line(18,7,18,28,0xFFE2FCFF);p.line(7,18,28,18,0xFFE2FCFF);
    p.star(7,7,2,0xFFFFE8BC,0xFF8DB6FF);
    p.star(29,29,2,0xFFFFF8DD,0xFFA27FF0);
    return p;
  }

  private static Painter constellation() {
    Painter p=new Painter(48,36);
    int[][] stars={{7,25,3},{19,10,4},{34,13,3},{41,28,2},{20,29,2}};
    for(int i=1;i<stars.length;i++) {
      p.line(stars[i-1][0],stars[i-1][1],stars[i][0],stars[i][1],0x885C91DE);
    }
    for(int[] s:stars) {
      p.star(s[0],s[1],s[2],0xFFFFFFFF,0xFF99DBFF);
      p.dot(s[0]-1,s[1]-1,0xFFFFDA84);
    }
    p.dot(3,6,0xAA93D9FF);p.dot(42,5,0xFFCEB1FF);
    p.dot(30,30,0xFFFBE3B8);p.star(40,17,1,0xFFE9D5FF,0xFFF3A0CB);
    return p;
  }

  private static Painter planet() {
    Painter p=new Painter(48,40);
    p.oval(13,7,36,32,0xFF172F6D);
    p.oval(15,8,34,31,0xFF4E57AD);
    p.oval(16,9,30,25,0xFF8F73CB);
    p.oval(18,10,29,19,0xFFBFB5E9);
    p.line(16,17,30,18,0xAA2F377D);
    p.line(18,22,34,22,0xAA2C3F8C);
    // Orbital ring: dark rear arc first; bright foreground arc in front.
    p.line(5,18,18,9,0x8883A6E5);p.line(19,9,44,16,0x8894B2DB);
    p.line(4,24,18,30,0xFFE8C781);p.line(18,30,36,26,0xFFFFDE92);
    p.line(36,26,45,20,0xFFF1BD77);
    p.line(5,23,19,29,0xFFA67AC3);
    p.star(5,7,2,0xFFFFF8D3,0xFFD7DFFF);
    p.dot(43,33,0xFF85D9FF);
    return p;
  }

  private static Painter silverCloud() {
    Painter p=new Painter(48,30);
    for(int[] b:new int[][]{{13,16,10,10},{22,13,11,12},{31,17,11,10},{25,21,18,7}})p.oval(b[0]-b[2]/2,b[1]-b[3]/2,b[0]+b[2]/2+4,b[1]+b[3]/2+4,0xFF344F79);
    p.oval(6,15,22,24,0xFF6588AE);p.oval(14,10,31,24,0xFF9CB6D1);
    p.oval(24,13,43,25,0xFF6F92B7);p.rect(10,19,41,24,0xFF8BAACB);
    p.oval(15,10,27,18,0xFFE9F5FF);p.oval(27,14,40,20,0xFFC3DEEF);
    p.line(13,23,37,23,0xFF4B6A91);p.line(14,21,26,21,0xFFA4BDD4);
    p.dot(11,16,0xFFEAFCFF);p.dot(34,14,0xFFDDF7FF);
    return p;
  }

  private static Painter tempest() {
    Painter p=new Painter(48,40);
    p.oval(5,12,27,29,0xFF263454);p.oval(12,7,37,30,0xFF344163);
    p.oval(24,12,44,29,0xFF28324E);p.rect(8,19,41,29,0xFF26334F);
    p.oval(16,9,30,17,0xFF667492);p.oval(24,13,37,20,0xFF4E5E83);
    p.line(7,27,40,27,0xFF141F36);
    p.line(25,24,19,31,0xFFFFE9A3);p.line(19,31,24,31,0xFFFFE9A3);
    p.line(24,31,16,39,0xFFF5D269);
    p.line(24,24,18,32,0xFFFFFBDA);
    p.dot(7,34,0x8084B9E9);p.dot(38,35,0x8084B9E9);
    return p;
  }

  private static Painter haze() {
    Painter p=new Painter(48,25);
    for(int y=6;y<23;y++)for(int x=2;x<47;x++) {
      double f=(double)x/48;
      double ridge=14+3*Math.sin((x+2)*.20)+2*Math.cos(x*.37);
      double dist=Math.abs(y-ridge), edge=Math.min(x-1,47-x);
      double alpha=Math.max(0,1-dist/9.5)*Math.max(0,Math.min(1,edge/6.0));
      alpha*=.7+.3*Math.sin(x*.11+y*.31);
      int a=(int)Math.round(105*alpha);
      if(a>6) p.put(x,y,(a<<24)|0xA2C5EE);
    }
    for(int x=6;x<43;x++)if(x%3!=1)p.dot(x,17+(int)Math.round(2*Math.sin(x*.21)),0x286BF2EE);
    return p;
  }

  private static Painter aurora() {
    Painter p=new Painter(48,36);
    for(int x=2;x<46;x++){
      double crest=5+5*Math.sin(x*.12);
      for(int y=0;y<36;y++){
        double t=(y-crest)/27;
        if(t<0||t>1)continue;
        double wave=Math.sin(x*.28+t*4);
        double width=.18+.22*(1-t);
        if(Math.abs(wave*.20+(t-.45))>width)continue;
        int alpha=(int)(Math.sin(Math.PI*t)*90);
        int rgb=x<22?0x4DEAC8:0x9C92F5;
        p.put(x,y,(Math.max(0,alpha)<<24)|rgb);
      }
    }
    for(int[] d:new int[][]{{7,7},{39,5},{31,27}})p.star(d[0],d[1],1,0xFFF3FFFF,0xFF78D6D6);
    return p;
  }

  private static Painter fir() {
    Painter p=new Painter(36,48);
    p.rect(16,34,20,47,0xFF3C2D33);p.rect(17,34,19,47,0xFF98704C);
    for(int layer=0;layer<5;layer++){
      int yy=3+layer*7, half=4+layer*3;
      for(int y=yy;y<yy+13;y++){
        int dx=Math.min(half,(y-yy+1)*half/9);
        for(int x=18-dx;x<=18+dx;x++){
          int shade=(x+y+layer)%7<2?0xFF78B49F:((x+2*y)%5==0?0xFF23485C:0xFF2A726A);
          if(y>yy+8)shade=0xFF19485B;
          p.put(x,y,shade);
        }
      }
      for(int x=18-half/2;x<=18+half/2;x++){
        int snowY=yy+5+(Math.abs(x-18)*3)/Math.max(1,half/2);
        p.dot(x,snowY,0xFFDCEFFF);
        if(x%3==0)p.dot(x,snowY+1,0xFF9CCEDF);
      }
    }
    p.dot(12,35,0xFFFFF2C0);p.dot(21,25,0xFFB4F8E8);
    return p;
  }

  private static Painter sakura() {
    Painter p=new Painter(48,48);
    p.rect(22,23,26,47,0xFF6D4553);
    p.rect(24,24,25,45,0xFFA97980);
    p.line(24,29,12,21,0xFF76515C);p.line(25,31,39,19,0xFF76515C);
    p.line(24,26,22,13,0xFFA97980);
    for(int[] b:new int[][]{{12,17,9},{22,12,10},{35,17,10},{29,24,8},{9,26,8},{38,26,6}}){
      int cx=b[0],cy=b[1],rad=b[2];
      p.oval(cx-rad,cy-rad/2,cx+rad,cy+rad/2+4,0xFF8E547C);
      p.oval(cx-rad+2,cy-rad/2-1,cx+rad-2,cy+rad/2+1,0xFFE8A3CB);
      p.oval(cx-rad/2,cy-rad/2-2,cx+rad/2,cy+rad/2-1,0xFFFFD3DF);
      p.dot(cx-3,cy-2,0xFFFFF2E9);p.dot(cx+3,cy,0xFFF8B5CD);
    }
    p.dot(6,35,0xB8FFDAE7);p.dot(42,40,0x99FFB5CD);p.dot(11,44,0xA8F5BFD7);
    return p;
  }

  private static Painter oak() {
    Painter p=new Painter(48,48);
    p.rect(21,25,27,47,0xFF4B3A3A);p.rect(24,26,26,44,0xFFA57956);
    p.line(23,32,13,23,0xFF715447);p.line(26,34,37,20,0xFF715447);
    for(int[] b:new int[][]{{12,21,9},{24,13,12},{35,22,10},{20,27,11},{31,29,9}}){
      int cx=b[0],cy=b[1],r=b[2];
      p.oval(cx-r,cy-r/2,cx+r,cy+r/2+5,0xFF173E48);
      p.oval(cx-r+2,cy-r/2-1,cx+r-2,cy+r/2+1,0xFF347F66);
      p.oval(cx-r/2,cy-r/2-2,cx+r/2,cy+2,0xFF8FBC83);
      p.dot(cx-2,cy-2,0xFFC0D88F);
    }
    p.star(7,35,2,0xFFFFE7AD,0xFF86D4B0);
    p.star(41,12,2,0xFFFFEEA4,0xFF8AEFCE);
    return p;
  }

  private static Painter mushrooms() {
    Painter p=new Painter(40,36);
    p.rect(10,20,16,33,0xFFB8BEA9);p.rect(24,24,28,34,0xFFD9CFBA);
    p.rect(12,20,14,33,0xFFF7E7C8);
    p.oval(3,10,24,23,0xFF542D67);
    p.oval(5,10,22,17,0xFFB76498);
    p.dot(10,13,0xFFFFD9E9);p.dot(18,15,0xFFD2A5E9);p.dot(7,17,0xFFF9B5C6);
    p.oval(19,17,35,27,0xFF276A6A);
    p.oval(21,16,34,23,0xFF77D5B6);
    p.dot(26,19,0xFFF4FFCB);p.dot(32,22,0xFFC8F8E9);
    p.star(3,30,1,0xFFFFF0CA,0xFF8AE8C6);
    p.star(35,8,2,0xFFFFD9A3,0xFF94CCFF);
    return p;
  }

  private static Painter crystal() {
    Painter p=new Painter(36,48);
    p.diamond(18,23,20,0xFF1A4162);
    for(int y=3;y<45;y++){
      int dx=(int)(15*(1-Math.abs(y-24)/22.0));
      for(int x=18-dx;x<=18+dx;x++) {
        int c=x<18?0xFF58C4C2:0xFF6D78DA;
        if(x<15)c=0xFF2D92A9;
        if(x>22)c=0xFF4355AB;
        if(y<12)c=0xFFD2F9EF;
        p.put(x,y,c);
      }
    }
    p.line(18,4,18,44,0xFFD6F6FF);
    p.line(11,14,17,4,0xFFB7FFF2);
    p.line(23,13,30,23,0xFFAE89ED);
    p.line(8,26,17,43,0xFF53ABB9);
    p.star(5,8,2,0xFFE9FFFF,0xFF92D5F5);
    p.star(31,37,2,0xFFECD9FF,0xFF9F8EFF);
    return p;
  }

  private static Painter feather() {
    Painter p=new Painter(40,48);
    p.line(13,45,30,5,0xFF4F4155);
    p.line(14,44,29,6,0xFFFFE8B0);
    for(int y=6;y<39;y++){
      int cx=30-(y-6)/2, rx=(int)(4+9*Math.sin(Math.PI*(y-5)/40));
      for(int x=cx-rx;x<=cx+rx;x++){
        double side=(x-cx)/(double)Math.max(1,rx);
        int color=side<-.55?0xFF8C3C62:side<0?0xFFDE605A:side<.6?0xFFF69B59:0xFFFFC16F;
        if(y<14)color=side<0?0xFFFFBF7D:0xFFFFEDAD;
        p.put(x,y,color);
      }
    }
    p.line(29,9,14,42,0xFFFFE9B0);
    for(int y=15;y<34;y+=5){
      int x=30-(y-6)/2;
      p.line(x,y,x+5,y-4,0xFFFFD58D);
      p.line(x,y,x-6,y-3,0xFFB94860);
    }
    p.star(30,4,2,0xFFFFF7C9,0xFFFFBD6A);
    return p;
  }

  private static Painter halo() {
    Painter p=new Painter(44,44);
    for(int y=2;y<42;y++)for(int x=2;x<42;x++){
      double d=Math.hypot(x-22,y-22);
      if(d>17.3&&d<18.8)p.put(x,y,0xAA7CC8E8);
      else if(d>15.9&&d<17.3)p.put(x,y,0xFF6DE4E0);
      else if(d>15.0&&d<15.9)p.put(x,y,0x6656D4DB);
    }
    for(int[] pt:new int[][]{{22,3},{3,22},{40,22},{22,40}})p.star(pt[0],pt[1],2,0xFFFFFFFF,0xFF87E8F4);
    p.diamond(22,22,9,0x6645BFC3);
    p.diamond(22,22,7,0xFF467EAC);
    p.diamond(22,22,5,0xFFA3E5EA);
    p.diamond(22,22,2,0xFFFFFFFF);
    for(int angle=0;angle<360;angle+=45){
      double t=Math.toRadians(angle);
      int x=(int)Math.round(22+12*Math.cos(t)),y=(int)Math.round(22+12*Math.sin(t));
      p.dot(x,y,0xFFFFDB89);
    }
    return p;
  }

  private static Painter heart() {
    Painter p=new Painter(36,36);
    for(int y=3;y<33;y++)for(int x=3;x<33;x++){
      double xx=(x-17.5)/12.5,yy=(19-y)/13.0;
      double h=Math.pow(xx*xx+yy*yy-1,3)-xx*xx*yy*yy*yy;
      if(h<=0){
        int c=(x<11||y>27)?0xFFAA3C78:
            x<19?0xFFF076A5:y<18?0xFFFFB8C5:0xFFE45089;
        p.put(x,y,c);
      }
    }
    p.oval(10,7,16,11,0xFFFFE4E8);
    p.line(10,13,11,17,0xFFFFC0D1);
    p.dot(19,25,0xFFD96698);
    p.star(29,5,2,0xFFFFFFFF,0xFFFFB4DE);
    return p;
  }

  private static Painter laurel() {
    Painter p=new Painter(40,44);
    p.line(18,38,8,17,0xFF5F6243);
    p.line(22,38,32,17,0xFF5F6243);
    for(int i=0;i<6;i++){
      int y=34-i*4, offset=(int)(3+6*Math.sin(i*Math.PI/12.0));
      int xL=16-offset,xR=24+offset;
      p.oval(xL-5,y-4,xL+2,y,0xFF396E54);
      p.oval(xL-4,y-5,xL+1,y-2,0xFFB5BA78);
      p.oval(xR-2,y-4,xR+5,y,0xFF396E54);
      p.oval(xR-1,y-5,xR+4,y-2,0xFFB5BA78);
    }
    p.line(17,38,22,38,0xFFFFD994);
    p.star(20,9,3,0xFFFFF2BE,0xFFEEBA68);
    return p;
  }

  /** Integer-pixel palette painter. No runtime GPU allocations or random effects. */
  private static final class Painter {
    final int w,h;final int[] a;
    Painter(int w,int h){this.w=w;this.h=h;this.a=new int[w*h];}
    void put(int x,int y,int c){if(x>=0&&x<w&&y>=0&&y<h)a[y*w+x]=c;}
    void dot(int x,int y,int c){put(x,y,c);}
    void rect(int x0,int y0,int x1,int y1,int c){
      for(int y=Math.max(0,y0);y<=Math.min(h-1,y1);y++)
        for(int x=Math.max(0,x0);x<=Math.min(w-1,x1);x++)put(x,y,c);
    }
    void oval(int x0,int y0,int x1,int y1,int c){
      double rx=(x1-x0)/2.0,ry=(y1-y0)/2.0,cx=(x0+x1)/2.0,cy=(y0+y1)/2.0;
      if(rx<=0||ry<=0)return;
      for(int y=Math.max(0,y0);y<=Math.min(h-1,y1);y++)
        for(int x=Math.max(0,x0);x<=Math.min(w-1,x1);x++)
          if(Math.pow((x-cx)/rx,2)+Math.pow((y-cy)/ry,2)<=1)put(x,y,c);
    }
    void diamond(int cx,int cy,int radius,int c){
      for(int y=cy-radius;y<=cy+radius;y++)
        for(int x=cx-radius;x<=cx+radius;x++)
          if(Math.abs(x-cx)+Math.abs(y-cy)<=radius)put(x,y,c);
    }
    void line(int x0,int y0,int x1,int y1,int c){
      int dx=Math.abs(x1-x0),sx=x0<x1?1:-1,dy=-Math.abs(y1-y0),sy=y0<y1?1:-1,err=dx+dy;
      while(true){put(x0,y0,c);if(x0==x1&&y0==y1)break;int e2=err*2;
        if(e2>=dy){err+=dy;x0+=sx;}if(e2<=dx){err+=dx;y0+=sy;}}
    }
    void star(int x,int y,int radius,int core,int ray){
      for(int r=radius;r>=1;r--){
        put(x+r,y,ray);put(x-r,y,ray);put(x,y+r,ray);put(x,y-r,ray);
      }
      put(x,y,core);
    }
    PixelPatch result(){return new PixelPatch(w,h,a);}
  }
}
