package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class LoomHelpScreen extends Screen {
    private final Screen parent;
    private int page;
    public LoomHelpScreen(Screen parent){super(Component.literal("Studio help"));this.parent=parent;}
    @Override protected void init(){int y=height-52;addRenderableWidget(new LoomButton(width/2-150,y,96,22,Component.literal("Back"),this::onClose));addRenderableWidget(new LoomButton(width/2-48,y,96,22,Component.literal("Previous"),()->{page=Math.floorMod(page-1,3);}));addRenderableWidget(new LoomButton(width/2+54,y,96,22,Component.literal("Next"),()->{page=(page+1)%3;}));}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Studio help",LoomUiTheme.compact(width,height));
        String[][] tips={{"Drawing & selection","B/P: pencil · E: eraser · G: fill · I: eyedropper","L: line · R: rectangle · C: circle · S: select (Elytra)","Drag a shape; release to commit one undo step.","Select: arrows move · Ctrl+C/V copy/paste · Ctrl+R rotate","Ctrl+Z/Y: undo/redo · Ctrl+S: save · Ctrl+Shift+S: equip","Middle drag pans textures and 3D; wheel zooms.","Choose an unlocked Paint layer for pixel tools."},{"Animation & preview","Choose a layer and add an effect in the timeline.","Drag a keyframe diamond to change its time.","Use the effect-specific value slider to set a key.","Preset restores a useful default animation curve.","Play previews locally; Save + Equip publishes artwork.","Click the Elytra preview heading to cycle poses.","Expand preview for facing, poses and camera presets."},{"Design library & recovery","Single click previews · Double click opens editing.","Right click: Edit, Rename, Favorite, Duplicate, Delete.","Delete moves to Trash; restore keeps the project ID.","Search, Favorites and Latest/Name sorting filter cards.","Dirty edits checkpoint every 30 seconds by default.","Drafts recover unsaved work; they never equip it.","Settings and favorites stay local to your device."}};
        int x=width/2-225,y=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+20;for(String line:tips[page]){g.drawString(font,Component.literal(font.plainSubstrByWidth(line,450)),x,y,LoomUiTheme.TEXT,false);y+=23;}super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,"Keyboard and mouse workflows","Page "+(page+1)+" / 3");}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
