package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.project.*;
import dev.loomstudios.project.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.nio.file.Path;
public final class LoomVersionsScreen extends Screen {
    private final Screen parent;private final ProjectDescriptor design;private final ProjectVersions versions;private List<Path> files=List.of();private int page;private String message="Select a version to restore; current saved artwork is backed up first";
    public LoomVersionsScreen(Screen parent,ProjectDescriptor design){super(Component.literal("Project versions"));this.parent=parent;this.design=design;versions=new ProjectVersions(new ProjectFileStore(design.projectPath().getParent()));}
    @Override protected void init(){int x=width/2-190,y=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+32;try{files=versions.list(design.projectId());}catch(java.io.IOException e){message="Could not read versions";}
        addRenderableWidget(new LoomButton(x,y,100,20,Component.literal("Back"),this::onClose));addRenderableWidget(new LoomButton(x+108,y,272,20,Component.literal("Create backup now"),()->{try{versions.backup(new ProjectFileStore(design.projectPath().getParent()).load(design.projectPath()));message="Backup created";rebuildWidgets();}catch(java.io.IOException|IllegalArgumentException e){message="Backup failed";}}));
        int capacity=Math.max(1,(height-y-90)/25);page=Math.min(page,Math.max(0,(files.size()-1)/capacity));for(int i=page*capacity;i<Math.min(files.size(),(page+1)*capacity);i++){Path p=files.get(i);String name=p.getFileName().toString();long time=Long.parseLong(name.substring(0,name.indexOf('-')));String label=java.time.Instant.ofEpochMilli(time).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().toString().replace('T',' ')+" · "+name.substring(name.indexOf('-')+1,name.indexOf('-')+9);addRenderableWidget(new LoomButton(x,y+28+(i%capacity)*25,380,22,Component.literal(label),()->minecraft.setScreen(new LoomDecisionScreen(this,"Restore version?",design.name()+" · current save is backed up",List.of(new LoomDecisionScreen.Choice("Restore saved version",()->WorkspaceNavigation.request(this,()->{try{versions.restore(design.projectId(),p);if(ClientProjectWorkspace.isInitialized()&&ClientProjectWorkspace.project().projectId().equals(design.projectId())&&minecraft.player!=null)ClientProjectWorkspace.open(design.projectPath(),minecraft.player.getUUID());ProjectLibraryIndex.refresh();message="Saved version restored";minecraft.setScreen(this);}catch(java.io.IOException|IllegalArgumentException e){message="Restore failed; current save was preserved";minecraft.setScreen(this);}}),false))))));}
        addRenderableWidget(new LoomButton(x,height-56,100,20,Component.literal("Previous"),()->{page=Math.max(0,page-1);rebuildWidgets();}).setActive(page>0));addRenderableWidget(new LoomButton(x+280,height-56,100,20,Component.literal("Next"),()->{page++;rebuildWidgets();}).setActive((page+1)*capacity<files.size()));
    }
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Backup / version history",LoomUiTheme.compact(width,height));super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,font.plainSubstrByWidth(message,width-160),files.size()+" / "+ProjectVersions.LIMIT+" versions");}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
