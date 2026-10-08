package dev.loomstudios.client.ui;

import dev.loomstudios.client.project.LoomDiagnostics;
import dev.loomstudios.client.project.LoomPreferences;
import dev.loomstudios.ui.LoomWorkspaceLayout;
import net.minecraft.client.gui.GuiGraphics;
import java.io.IOException;

/**
 * Shared conservative inspector-divider behavior for Cape and Elytra editors.
 *
 * <p>Coordinates/sizes are logical Minecraft GUI pixels. A drag shows a preview
 * divider, and commits only on mouse release so we do not rebuild thousands of
 * pixel textures or lose the active stroke on every mouse move. Profiles are
 * stored locally, independent of .loom authoring or multiplayer state.
 */
public final class LoomInspectorResize {
    private LoomInspectorResize() {}
    private static String key(int width,int height) {
        return "workspace.inspectorWidth." + (width <= 700 || height <= 420 ? "compact" : "wide");
    }
    public static int preference(int width,int height) {
        try { return Integer.parseInt(LoomPreferences.get().choice(key(width,height),"0")); }
        catch (NumberFormatException e) { return 0; }
    }
    public static int requestedAtX(int windowWidth,int windowHeight,double mouseX) {
        return LoomWorkspaceLayout.safeInspectorWidth(windowWidth,windowHeight,
                windowWidth-8-(int)Math.round(mouseX));
    }
    public static boolean onDivider(double mx,double my,int divider,int top,int bottom) {
        return mx>=divider-5 && mx<=divider+5 && my>=top+6 && my<bottom-6;
    }
    public static void persist(int width,int height,int value) {
        try { LoomPreferences.get().set(key(width,height),
                Integer.toString(LoomWorkspaceLayout.safeInspectorWidth(width,height,value))); }
        catch(IOException e) { LoomDiagnostics.record("Persist inspector panel width",e); }
    }
    public static void reset(int width,int height) {
        try { LoomPreferences.get().set(key(width,height),"0"); }
        catch(IOException e) { LoomDiagnostics.record("Reset inspector panel width",e); }
    }
    public static void render(GuiGraphics g,int divider,int top,int bottom,boolean highlighted,
                              boolean dragging,int pendingRightPanelLeft) {
        int x=dragging?pendingRightPanelLeft-3:divider-3;
        int color=highlighted?0xFF70E3EC:0xFF617991;
        int mid=(top+bottom)/2;
        g.fill(x,mid-15,x+5,mid+15,0xFF122334);
        g.fill(x+1,mid-13,x+3,mid+13,color);
        for(int y=mid-7;y<=mid+7;y+=7)
            g.fill(x-1,y,x+5,y+1,color);
    }
}
