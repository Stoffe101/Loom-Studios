package dev.loomstudios.client.debug;

import dev.loomstudios.image.PixelImage;
import dev.loomstudios.project.*;
import java.util.List;
import java.util.UUID;

/** Deterministic artwork for the opt-in capture world, never a production template. */
final class LoomCaptureFixtures {
    private LoomCaptureFixtures() { }
    static LoomProject moon(String name, int variant) {
        var blank = LoomProjectFactory.blank(name, 1790978400000L + variant * 1000L);
        int width = 256, height = 128;
        int[] base = new int[width*height], stars = new int[width*height], moon = new int[width*height];
        int accent = new int[]{0xFF24DBF0,0xFFAD68EE,0xFF65D0A9,0xFFFF6C73}[variant];
        for(CapeUvRegion face : new CapeUvRegion[]{CapeUvRegion.OUTSIDE,CapeUvRegion.INSIDE}) {
            for(int y=0;y<64;y++) for(int x=0;x<40;x++) {
                int idx = (face.textureY()*4+y)*width+face.textureX()*4+x;
                base[idx] = 0xFF0D183D + ((y/8)<<16);
                int edge = 3+(int)(2*Math.sin(y/7.0));
                if(x==edge || x==39-edge || y>59) base[idx] = ((y/8)%2==0) ? accent : 0xFF7938AF;
                if((x*17+y*29)%149==0 && x>6 && x<34) stars[idx] = accent;
                double circle = (x-20)*(x-20)+(y-24)*(y-24);
                double cut = (x-24)*(x-24)+(y-21)*(y-21);
                if(circle<81 && cut>=70) moon[idx] = accent;
                if((x==12 && y>=40 && y<=46) || (y==43 && x>=9 && x<=15)) stars[idx] = 0xFFDAEFFF;
            }
        }
        var layers = List.of(
                new LoomLayer(UUID.randomUUID(),"Night weave",true,1,BlendMode.NORMAL,false,base),
                new LoomLayer(UUID.randomUUID(),"Starlight",true,1,BlendMode.NORMAL,true,stars),
                new LoomLayer(UUID.randomUUID(),"Moon",true,1,BlendMode.NORMAL,true,moon));
        var project = blank.withCape(new LoomCanvas(width,height,layers));
        var face = source(project);
        return ProjectEdits.addElytraPaintLayer(project,"Moon wings",CapeToElytraConverter.convertOutsideFace(
                face.pixels(),face.width(),face.height(),project.elytra()));
    }
    static PixelImage source(LoomProject project) {
        int[] atlas = dev.loomstudios.client.render.LoomTextureCompiler.compile(project.cape(),0,false,false);
        int[] face = new int[40*64];
        for(int y=0;y<64;y++) for(int x=0;x<40;x++) face[y*40+x]=atlas[(y+4)*256+x+4];
        return new PixelImage(40,64,face);
    }
}
