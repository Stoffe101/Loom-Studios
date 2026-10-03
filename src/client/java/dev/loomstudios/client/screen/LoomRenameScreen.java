package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class LoomRenameScreen extends Screen {
    @FunctionalInterface public interface Rename { void apply(String name) throws java.io.IOException; }
    private final Screen parent;private final String original;private final Rename action;
    private String fieldLabel="Design name",buttonLabel="Save name";
    public LoomRenameScreen(Screen parent,String title,String label,String button,String original,Rename action){this(parent,original,action);this.fieldLabel=label;this.buttonLabel=button;this.customTitle=title;}
    private String customTitle="Rename design";
    private EditBox name;private String error="";
    public LoomRenameScreen(Screen parent,String original,Rename action){super(Component.literal("Rename design"));this.parent=parent;this.original=original;this.action=action;}
    @Override protected void init(){int x=width/2-140,y=height/2-35;
        name=addRenderableWidget(new EditBox(font,x,y,280,22,Component.literal(fieldLabel)));name.setMaxLength(dev.loomstudios.project.LoomProjectCodec.MAX_PROJECT_NAME_CHARS);name.setValue(original);setInitialFocus(name);
        addRenderableWidget(new LoomButton(x,y+32,136,22,Component.literal("Cancel"),this::onClose));
        addRenderableWidget(new LoomButton(x+144,y+32,136,22,Component.literal(buttonLabel),()->{try{action.apply(name.getValue().strip());onClose();}catch(java.io.IOException|IllegalArgumentException e){error=e.getMessage();}}).setPrimary(true));}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,customTitle,LoomUiTheme.compact(width,height));LoomScreenChrome.panel(g,width/2-152,height/2-62,width/2+152,height/2+60);g.drawCenteredString(font,Component.literal(fieldLabel),width/2,height/2-53,LoomUiTheme.TEXT);super.render(g,mx,my,dt);g.drawCenteredString(font,Component.literal(font.plainSubstrByWidth(error,280)),width/2,height/2+31,0xFFFF718B);}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
