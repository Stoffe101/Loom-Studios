package dev.loomstudios.client.screen;

import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.project.LoomDiagnostics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Explicit choices; failures stay in the dialog and never advance navigation. */
public final class LoomDecisionScreen extends Screen {
    @FunctionalInterface public interface Action { void run() throws Exception; }
    public record Choice(String label, Action action, boolean danger) { }
    private final Screen parent;
    private final String detail;
    private final List<Choice> choices;
    private String error="";
    public LoomDecisionScreen(Screen parent,String title,String detail,List<Choice> choices){super(Component.literal(title));this.parent=parent;this.detail=detail;this.choices=List.copyOf(choices);}
    @Override protected void init(){
        int x=(width-360)/2,y=Math.max(LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+42,height/2-32);
        for(int i=0;i<choices.size();i++){Choice c=choices.get(i);addRenderableWidget(new LoomButton(x,y+i*25,360,22,Component.literal(c.label()),()->choose(c)).setDanger(c.danger()));}
        addRenderableWidget(new LoomButton(x,y+choices.size()*25+5,360,22,Component.literal("Cancel"),this::onClose));
    }
    private void choose(Choice c){try{c.action().run();}catch(Exception e){error="Could not complete action. Your design is unchanged.";LoomDiagnostics.record("Decision action",e);}}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,title.getString(),LoomUiTheme.compact(width,height));
        int x=(width-380)/2,y=Math.max(LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+20,height/2-54);
        LoomScreenChrome.panel(g,x,y,x+380,y+choices.size()*25+88);
        g.drawCenteredString(font,Component.literal(font.plainSubstrByWidth(detail,360)),width/2,y+8,LoomUiTheme.TEXT);
        super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,error,"Choose before leaving");
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
