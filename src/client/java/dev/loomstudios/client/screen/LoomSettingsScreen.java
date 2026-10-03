package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.project.LoomPreferences;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class LoomSettingsScreen extends Screen {
    private final Screen parent;
    public LoomSettingsScreen(Screen parent){super(Component.literal("Editor settings"));this.parent=parent;}
    @Override protected void init(){int x=width/2-150,y=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+30;
        addToggle(x,y,"autosave","Recovery every 30 seconds",true);
        addToggle(x,y+28,"grid","Show grid by default",true);
        addToggle(x,y+56,"alphaGuide","Preview transparency guide",true);
        addToggle(x,y+84,"animatePreview","Animate live previews",true);
        addToggle(x,y+112,"shortcuts","Show shortcut hints",true);
        addRenderableWidget(new LoomButton(x,y+150,300,22,Component.literal("Back"),this::onClose));
        addRenderableWidget(new LoomButton(x,y+178,300,22,Component.literal("Shortcuts & workflow help"),()->minecraft.setScreen(new LoomHelpScreen(this))));}
    private void addToggle(int x,int y,String key,String label,boolean fallback){addRenderableWidget(new LoomButton(x,y,300,22,Component.literal(label+": "+(LoomPreferences.get().enabled(key,fallback)?"On":"Off")),()->{LoomPreferences.toggle(key,fallback);rebuildWidgets();}));}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Editor preferences",LoomUiTheme.compact(width,height));super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,"Preferences save automatically","Local settings");}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
