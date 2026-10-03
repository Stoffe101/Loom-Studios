package dev.loomstudios.client.screen;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.project.*;
import dev.loomstudios.project.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.io.IOException;
public final class LoomBulkScreen extends Screen {
    private final Screen parent;private final List<ProjectDescriptor> designs;private final boolean trash;private String message="Selected designs: ";
    public LoomBulkScreen(Screen parent,List<ProjectDescriptor> designs,boolean trash){super(Component.literal("Bulk actions"));this.parent=parent;this.designs=List.copyOf(designs);this.trash=trash;message+=designs.size();}
    @Override protected void init(){int x=width/2-180,y=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+35;
        String[] labels={"Folder / tags","Favorite selected","Unfavorite selected","Back up selected",trash?"Restore selected":"Move selected to Trash","Back"};
        for(int i=0;i<labels.length;i++){final int action=i;addRenderableWidget(new LoomButton(x,y+i*25,360,22,Component.literal(labels[i]),()->{if(action==0)minecraft.setScreen(new LoomOrganizeScreen(this,designs));else if(action==5)onClose();else if(action==4)minecraft.setScreen(new LoomDecisionScreen(this,trash?"Restore selected?":"Delete selected designs?",designs.size()+" designs · "+(trash?"existing files are never overwritten":"recoverable from Trash"),List.of(new LoomDecisionScreen.Choice(labels[4],()->run(action),!trash))));else run(action);}).setDanger(i==4&&!trash));}
    }
    private void run(int action){int done=0,failed=0;for(var d:designs)try{switch(action){case 1,2->{boolean want=action==1;if(LoomPreferences.get().favorite(d.projectId())!=want)LoomPreferences.get().toggleFavorite(d.projectId());}case 3->new ProjectVersions(new ProjectFileStore(d.projectPath().getParent())).backup(new ProjectFileStore(d.projectPath().getParent()).load(d.projectPath()));case 4->{if(trash)new ProjectFileStore(d.projectPath().getParent().getParent()).restore(d.projectId());else new ProjectFileStore(d.projectPath().getParent()).trash(d.projectId());}default->{}}done++;}catch(IOException|IllegalArgumentException e){failed++;LoomDiagnostics.record("Bulk library action",e);}message=done+" completed · "+failed+" failed";ProjectLibraryIndex.refresh();minecraft.setScreen(this);}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Bulk actions · "+designs.size()+" selected",LoomUiTheme.compact(width,height));super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,message,"Local library");}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
