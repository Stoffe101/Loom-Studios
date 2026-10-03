package dev.loomstudios.client.screen;
import dev.loomstudios.client.project.*;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.project.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class LoomTemplatesScreen extends LoomPointerScreen {
    private final Screen parent;
    private final List<LoomProject> templates=new ArrayList<>();
    private final List<LoomProjectCard> cards=new ArrayList<>();
    private LoomProject selected;
    private int page;
    public LoomTemplatesScreen(Screen parent){super(Component.literal("Templates"));this.parent=parent;for(var kind:TemplateCatalog.Kind.values())templates.add(TemplateCatalog.create(kind,System.currentTimeMillis()));selected=templates.getFirst();}
    @Override protected void init(){for(var c:cards)c.close();cards.clear();int top=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height))+8,bottom=height-28,right=width-204;
        addRenderableWidget(new LoomButton(8,top,70,20,Component.literal("Back"),this::onClose));
        int cw=(right-14)/2,ch=(bottom-top-42)/3;
        for(int i=page*6;i<Math.min(templates.size(),(page+1)*6);i++){LoomProject p=templates.get(i);try{var d=new ProjectDescriptor(p.projectId(),p.name(),p.metadata().createdAtEpochMillis(),p.metadata().modifiedAtEpochMillis(),p.hash(),LocalProjectLibrary.store().pathFor(p.projectId()),ProjectThumbnailCache.ensure(p,p.hash()));int n=i%6;var c=new LoomProjectCard(8+(n%2)*(cw+6),top+27+(n/2)*(ch+4),cw,ch,d,Component.literal("Editable template"),()->selected==p,()->selected=p).setOpenAction(this::useSelected);cards.add(c);addRenderableWidget(c);}catch(java.io.IOException e){dev.loomstudios.LoomStudios.LOGGER.error("Cannot create template preview",e);}}
        addRenderableWidget(new LoomButton(84,top,70,20,Component.literal("Previous"),()->{page--;rebuildWidgets();}).setActive(page>0));
        addRenderableWidget(new LoomButton(158,top,70,20,Component.literal("Next"),()->{page++;rebuildWidgets();}).setActive((page+1)*6<templates.size()));
        addRenderableWidget(new LoomPlayerPreviewWidget(right+8,top,188,bottom-top-27,()->selected));
        addRenderableWidget(new LoomButton(right+8,bottom-22,188,22,Component.literal("Use template"),this::useSelected).setPrimary(true));
    }
    private void useSelected(){WorkspaceNavigation.request(this,()->{if(minecraft.player==null)return;LoomProject copy=LoomProjectCode.forkImported(selected,System.currentTimeMillis()).withName(selected.name());ClientProjectWorkspace.replaceWith(copy,minecraft.player.getUUID());minecraft.setScreen(new CapeEditorScreen(this));});}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Template collection",LoomUiTheme.compact(width,height));super.render(g,mx,my,dt);LoomScreenChrome.footer(g,width,height,"Select to preview · Double click to create",templates.size()+" templates · Page "+(page+1));}
    @Override public void removed(){for(var c:cards)c.close();cards.clear();super.removed();}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
