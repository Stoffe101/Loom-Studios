package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.project.*;
import dev.loomstudios.project.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.io.IOException;
public final class LoomOrganizeScreen extends Screen {
    private final Screen parent;private final List<ProjectDescriptor> designs;private EditBox folder,tags;private String message="Folder names and comma-separated tags; empty values clear them";
    public LoomOrganizeScreen(Screen parent,List<ProjectDescriptor> designs){super(Component.literal("Organize designs"));this.parent=parent;this.designs=List.copyOf(designs);}
    @Override protected void init(){int x=width/2-160,y=Math.max(70,height/2-80);var org=new LibraryOrganization(LoomPreferences.get());
        folder=addRenderableWidget(new EditBox(font,x,y+15,320,20,Component.literal("Folder")));folder.setMaxLength(48);folder.setValue(designs.size()==1?org.folder(designs.getFirst().projectId()):"");
        tags=addRenderableWidget(new EditBox(font,x,y+54,320,20,Component.literal("Tags")));tags.setMaxLength(300);tags.setValue(designs.size()==1?String.join(",",org.tags(designs.getFirst().projectId())):"");
        addRenderableWidget(new LoomButton(x,y+84,156,22,Component.literal("Cancel"),this::onClose));addRenderableWidget(new LoomButton(x+164,y+84,156,22,Component.literal("Apply to "+designs.size()),()->{try{org.organize(designs.stream().map(ProjectDescriptor::projectId).toList(),folder.getValue(),tags.getValue());onClose();}catch(IOException|IllegalArgumentException e){message=e.getMessage();}}).setPrimary(true));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Folder / tags",LoomUiTheme.compact(width,height));int x=width/2-168,y=Math.max(70,height/2-80);LoomScreenChrome.panel(g,x,y-9,x+336,y+120);dev.loomstudios.client.ui.premium.PremiumText.drawString(g,font,"Folder",x+8,y+2,LoomUiTheme.TEXT,false);dev.loomstudios.client.ui.premium.PremiumText.drawString(g,font,"Tags",x+8,y+41,LoomUiTheme.TEXT,false);super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,font.plainSubstrByWidth(message,width-160),designs.size()+" selected");}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
