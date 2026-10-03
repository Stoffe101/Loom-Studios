package dev.loomstudios.client.palette;
import dev.loomstudios.palette.*;
import dev.loomstudios.client.project.*;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.LoomProject;
import java.util.*;

public final class EditorColors {
    public static final UUID RECENT_ID=UUID.fromString("00000000-0000-4000-8000-000000000001");
    public static final UUID DESIGN_ID=UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static RecentColors recent;
    private static LoomProject cachedProject;
    private static List<Integer> cachedDesign=List.of();
    private EditorColors(){}
    private static RecentColors recent(){if(recent==null)recent=new RecentColors(LoomPreferences.get().choice("recentColors",""));return recent;}
    public static void use(int color){if(recent().use(color))try{LoomPreferences.get().set("recentColors",recent().encode());}catch(java.io.IOException e){LoomDiagnostics.record("Recent colors",e);}}
    public static List<Integer> design(){
        if(!ClientProjectWorkspace.isInitialized())return List.of();LoomProject p=ClientProjectWorkspace.project();
        if(p!=cachedProject){cachedDesign=DesignColors.collect(LoomTextureCompiler.compile(p.cape(),0,false,false),LoomTextureCompiler.compile(p.elytra(),0,false,false));cachedProject=p;}
        return cachedDesign;
    }
    public static List<ColorPalette> groups(){
        List<ColorPalette> out=new ArrayList<>();if(!recent().colors().isEmpty())out.add(new ColorPalette(RECENT_ID,"Recent colors",recent().colors()));
        if(!design().isEmpty())out.add(new ColorPalette(DESIGN_ID,"Design colors · click title to save",design()));out.addAll(ColorPaletteLibrary.palettes());return List.copyOf(out);
    }
    public static boolean virtual(ColorPalette p){return p.id().equals(RECENT_ID)||p.id().equals(DESIGN_ID);}
    public static ColorPalette saveDesign() throws java.io.IOException {
        if(design().isEmpty())throw new IllegalArgumentException("This design has no visible colors");
        String name=ClientProjectWorkspace.project().name();name=name.substring(0,Math.min(name.length(),ColorPalette.MAX_NAME_CHARS-7))+" colors";
        return ColorPaletteLibrary.save(new ColorPalette(UUID.randomUUID(),name,design()));
    }
}
