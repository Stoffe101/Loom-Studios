package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.project.LoomPreferences;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class LoomSettingsScreen extends Screen {
    private final Screen parent;
    public LoomSettingsScreen(Screen parent){super(Component.literal("Editor settings"));this.parent=parent;}
    private String message="Preferences save automatically";
    @Override protected void init(){boolean compact=LoomUiTheme.compact(width,height);int x=width/2-150,y=LoomScreenChrome.headerHeight(compact)+(compact?8:16);int step=compact?24:26;
        addToggle(x,y,"autosave","Recovery every 30 seconds",true);
        addToggle(x,y+step,"grid","Show grid by default",true);
        addToggle(x,y+step*2,"alphaGuide","Preview transparency guide",true);
        addToggle(x,y+step*3,"animatePreview","Animate live previews",true);
        addToggle(x,y+step*4,"shortcuts","Show shortcut hints",true);
        addRenderableWidget(new LoomButton(x,y+step*5,300,22,Component.literal("Preview: "+LoomPreviewBackground.current().label()),()->{LoomPreviewBackground.cycle();rebuildWidgets();}));
        addRenderableWidget(new LoomButton(x,y+step*6,300,22,Component.literal("Back"),this::onClose));
        addRenderableWidget(new LoomButton(x,y+step*7,300,22,Component.literal("Shortcuts & workflow help"),()->minecraft.setScreen(new LoomHelpScreen(this))));
        addRenderableWidget(new LoomButton(x,y+step*8,300,22,Component.literal("Copy diagnostics"),()->{try{dev.loomstudios.client.project.LoomDiagnostics.copy(minecraft);message="Diagnostics copied · no account or design data";}catch(Exception e){message="Clipboard unavailable; no report was sent";dev.loomstudios.client.project.LoomDiagnostics.record("Copy diagnostics",e);}}));}
    private void addToggle(int x,int y,String key,String label,boolean fallback){addRenderableWidget(new LoomButton(x,y,300,22,Component.literal(label+": "+(LoomPreferences.get().enabled(key,fallback)?"On":"Off")),()->{LoomPreferences.toggle(key,fallback);rebuildWidgets();}));}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Editor preferences",LoomUiTheme.compact(width,height));super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,message,"Local settings");}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
