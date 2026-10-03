package dev.loomstudios.client.ui;
import dev.loomstudios.project.LoomLayer;
import java.util.Locale;

public final class LoomToolGuidance {
    private LoomToolGuidance(){}
    public static String name(String tool){return tool.substring(0,1)+tool.substring(1).toLowerCase(Locale.ROOT);}
    public static String shortcut(String tool,boolean wing){return switch(tool){case "PENCIL"->"P";case "ERASER"->"E";case "FILL"->"G";case "EYEDROPPER"->"I";case "SELECT"->"S";case "LINE"->"L";case "RECTANGLE"->"R";case "CIRCLE"->"C";default->"";};}
    public static String reason(String tool,LoomLayer layer){if(tool.equals("EYEDROPPER"))return "";if(layer==null)return "select a layer";if(layer.locked())return "unlock this layer";if(!layer.editableAsPaint())return "choose a Paint layer";return "";}
    public static String status(String tool,LoomLayer layer,boolean wing,String fallback){String reason=reason(tool,layer);return name(tool)+(reason.isEmpty()?" ["+shortcut(tool,wing)+"] · "+fallback:": "+reason);}
    public static String tooltip(String tool,LoomLayer layer,boolean wing){String reason=reason(tool,layer);return name(tool)+" ("+shortcut(tool,wing)+")"+(reason.isEmpty()?" · "+switch(tool){case "SELECT"->"Drag to select; arrows move; Ctrl+C/V copy/paste";case "EYEDROPPER"->"Click to sample the visible artwork";case "FILL"->"Click a connected area to fill it";case "LINE","RECTANGLE","CIRCLE"->"Drag to preview; release to commit";default->"Drag to draw";}:" · Disabled: "+reason);}
}
