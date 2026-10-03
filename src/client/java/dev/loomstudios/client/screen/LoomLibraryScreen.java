package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.*;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.project.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Bounded thumbnail browser: only the collection pages, not the workshop shell. */
public final class LoomLibraryScreen extends LoomPointerScreen {
    private final Screen parent;
    private final List<LoomProjectCard> cards=new ArrayList<>();
    private final Map<LoomProjectCard,ProjectDescriptor> descriptors=new HashMap<>();
    private List<ProjectDescriptor> entries=List.of();
    private EditBox search;
    private LoomPlayerPreviewWidget preview;
    private LoomProject previewProject;
    private ProjectDescriptor selected, menu;
    private UUID initialMenu;
    private int tab,page,left,right,top,bottom,menuX,menuY;
    private boolean favorites,nameSort;
    private String query="",message="Single click: preview · Double click: edit · Right click: actions";
    private LoomButton previous,next;
    public LoomLibraryScreen(Screen parent) { super(Component.literal("Design library"));this.parent=parent; }
    public LoomLibraryScreen(Screen parent,UUID menuId) { this(parent);initialMenu=menuId; }
    @Override protected void init() {
        WorkspaceRecovery.checkpoint(); closeCards();
        int header=LoomScreenChrome.headerHeight(LoomUiTheme.compact(width,height));
        top=header+8;bottom=height-28;left=8;right=Math.max(260,width-184);
        addRenderableWidget(new LoomButton(8,top,54,20,Component.literal("Back"),this::onClose));
        String[] tabs={"Designs","Drafts","Trash"};
        for(int i=0;i<3;i++){final int t=i;addRenderableWidget(new LoomButton(66+i*65,top,61,20,Component.literal(tabs[i]),()->{tab=t;page=0;menu=null;refresh();}).setSelected(tab==t));}
        addRenderableWidget(new LoomButton(width-176,top,168,20,Component.literal("Editor settings"),()->minecraft.setScreen(new LoomSettingsScreen(this))));
        search=addRenderableWidget(new EditBox(font,left,top+26,Math.max(100,(right-left)/2),20,Component.literal("Search designs")));
        search.setMaxLength(80);search.setValue(query);search.setHint(Component.literal("Search designs…"));
        search.setResponder(value->{query=value;page=0;buildCards();});
        addRenderableWidget(new LoomButton(search.getRight()+4,top+26,76,20,Component.literal("Favorites"),()->{favorites=!favorites;page=0;rebuild();}).setSelected(favorites));
        addRenderableWidget(new LoomButton(search.getRight()+84,top+26,Math.max(66,right-search.getRight()-84),20,Component.literal(nameSort?"Name A–Z":"Latest first"),()->{nameSort=!nameSort;page=0;rebuild();}));
        previous=addRenderableWidget(new LoomButton(left,bottom-22,56,20,Component.literal("Previous"),()->{page--;buildCards();}));
        next=addRenderableWidget(new LoomButton(right-56,bottom-22,56,20,Component.literal("Next"),()->{page++;buildCards();}));
        preview=addRenderableWidget(new LoomPlayerPreviewWidget(width-176,top+26,168,Math.max(90,bottom-top-52),()->previewProject));
        addRenderableWidget(new LoomButton(width-176,bottom-22,80,20,Component.literal("Edit"),()->editSelected()));
        addRenderableWidget(new LoomButton(width-92,bottom-22,84,20,Component.literal("Actions"),()->openMenu(selected,right-135,top+53)));
        refresh();
        if(initialMenu!=null){ProjectLibraryIndex.find(initialMenu).ifPresent(d->{select(d);openMenu(d,right-140,top+53);});initialMenu=null;}
    }
    private void rebuild(){rebuildWidgets();}
    private void refresh() {
        ProjectLibraryIndex.refresh();
        if(tab==0)entries=ProjectLibraryIndex.entries();
        else {
            List<ProjectDescriptor> list=new ArrayList<>();
            ProjectFileStore store=tab==1?WorkspaceRecovery.STORE:new ProjectFileStore(LocalProjectLibrary.root().resolve("trash"));
            try {List<Path> paths=new ArrayList<>(store.list());if(tab==2)paths.addAll(new ProjectFileStore(WorkspaceRecovery.STORE.root().resolve("trash")).list());
            for(Path path:paths)try {
                LoomProject p=new ProjectFileStore(path.getParent()).load(path);
                list.add(new ProjectDescriptor(p.projectId(),p.name(),p.metadata().createdAtEpochMillis(),p.metadata().modifiedAtEpochMillis(),p.hash(),path,ProjectThumbnailCache.ensure(p,p.hash())));
            }catch(IOException|IllegalArgumentException e){message="Some unreadable files were skipped";}}
            catch(IOException e){message="Could not read library: "+e.getMessage();}
            entries=List.copyOf(list);
        }
        if(selected!=null)selected=entries.stream().filter(d->d.projectId().equals(selected.projectId())).findFirst().orElse(null);
        if(selected==null&&!entries.isEmpty())selected=entries.getFirst();
        if(selected!=null)select(selected);else previewProject=null;
        buildCards();
    }
    private List<ProjectDescriptor> filtered() {
        var stream=entries.stream().filter(d->d.name().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                .filter(d->!favorites||LoomPreferences.get().favorite(d.projectId()));
        return stream.sorted(nameSort?Comparator.comparing(ProjectDescriptor::name,String.CASE_INSENSITIVE_ORDER):Comparator.comparingLong(ProjectDescriptor::modifiedAtEpochMillis).reversed()).toList();
    }
    private void buildCards() {
        for(var card:cards){removeWidget(card);card.close();}cards.clear();descriptors.clear();
        if(next==null)return;
        List<ProjectDescriptor> shown=filtered();
        int columns=Math.max(1,(right-left)/132),rows=Math.max(1,(bottom-top-80)/125),capacity=columns*rows;
        int pages=Math.max(1,(shown.size()+capacity-1)/capacity);page=Math.max(0,Math.min(page,pages-1));
        previous.active=page>0;next.active=page+1<pages;
        int cw=(right-left-6*(columns-1))/columns,ch=Math.max(60,(bottom-top-80-6*(rows-1))/rows);
        for(int i=page*capacity;i<Math.min(shown.size(),(page+1)*capacity);i++){
            ProjectDescriptor d=shown.get(i);int n=i-page*capacity;
            LoomProjectCard card=new LoomProjectCard(left+n%columns*(cw+6),top+53+n/columns*(ch+6),cw,ch,d,
                    Component.literal((LoomPreferences.get().favorite(d.projectId())?"★ ":"")+(tab==1?"Unsaved draft":tab==2?"In Trash":LoomHomeScreen.formatAge(d.modifiedAtEpochMillis()))),
                    ()->selected!=null&&selected.projectId().equals(d.projectId()),()->select(d));
            card.setOpenAction(()->{select(d);editSelected();});
            card.setContextAction((x,y)->{select(d);openMenu(d,x,y);});
            cards.add(card);descriptors.put(card,d);addRenderableWidget(card);
        }
    }
    private LoomProject load(ProjectDescriptor d) throws IOException {
        return new ProjectFileStore(d.projectPath().getParent()).load(d.projectPath());
    }
    private void select(ProjectDescriptor d) {
        selected=d;
        try{previewProject=load(d);if(tab==0)ProjectLibraryIndex.select(d.projectId());}
        catch(IOException|IllegalArgumentException e){previewProject=null;message="Could not open design: "+e.getMessage();}
    }
    private void restore(ProjectDescriptor d) throws IOException {
        boolean draft=d.projectPath().getParent().equals(WorkspaceRecovery.STORE.root().resolve("trash"));
        (draft?WorkspaceRecovery.STORE:LocalProjectLibrary.store()).restore(d.projectId());tab=draft?1:0;
    }
    private void editSelected() {
        if(selected==null||minecraft.player==null)return;
        try {
            if(tab==2){restore(selected);refresh();}
            if(tab==1)ClientProjectWorkspace.recover(selected.projectPath(),minecraft.player.getUUID());
            else ClientProjectWorkspace.open(selected.projectPath(),minecraft.player.getUUID());
            minecraft.setScreen(new CapeEditorScreen(this));
        }catch(IOException|IllegalArgumentException e){message="Could not edit design: "+e.getMessage();}
    }
    private void openMenu(ProjectDescriptor d,double x,double y) {
        if(d==null)return;menu=d;
        menuX=Math.max(8,Math.min(width-150,(int)x));menuY=Math.max(top,Math.min(bottom-146,(int)y));
    }
    private String[] actions() {return tab==2?new String[]{"Restore","Rename…","Favorite","Duplicate","Close"}:
            new String[]{tab==1?"Recover & edit":"Edit","Rename…",LoomPreferences.get().favorite(menu.projectId())?"Unfavorite":"Favorite","Duplicate",tab==1?"Discard to Trash":"Delete to Trash","Close"};}
    private void act(int index) {
        ProjectDescriptor d=menu;menu=null;if(d==null)return;select(d);
        try {
            switch(index){
                case 0 -> {if(tab==2){restore(d);message="Design restored";refresh();}else editSelected();}
                case 1 -> minecraft.setScreen(new LoomRenameScreen(this,d.name(),name->{
                    LoomProject p=load(d).withName(name).withMetadata(load(d).metadata().touch(System.currentTimeMillis()));
                    new ProjectFileStore(d.projectPath().getParent()).save(p);
                    if(ClientProjectWorkspace.isInitialized()&&ClientProjectWorkspace.project().projectId().equals(d.projectId()))ClientProjectWorkspace.apply(current->current.withName(name));
                    message="Renamed design";
                }));
                case 2 -> {LoomPreferences.get().toggleFavorite(d.projectId());refresh();}
                case 3 -> {LoomProject p=load(d);LoomProject copy=new LoomProject(p.schemaVersion(),UUID.randomUUID(),p.name().substring(0,Math.min(p.name().length(),LoomProjectCodec.MAX_PROJECT_NAME_CHARS-5))+" copy",LoomProjectMetadata.now(System.currentTimeMillis()),p.cape(),p.elytra(),p.runtime(),p.animation());LocalProjectLibrary.save(copy);message="Created independent copy";refresh();}
                case 4 -> {if(tab==2)return;
                    if(tab==1){WorkspaceRecovery.STORE.trash(d.projectId());message="Draft moved to draft Trash";}
                    else {LocalProjectLibrary.store().trash(d.projectId());message="Moved to Trash · select Trash to restore";}
                    refresh();
                }
                default -> { }
            }
        }catch(IOException|IllegalArgumentException e){message="Action failed: "+e.getMessage();}
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick) {
        if(menu!=null){String[] actions=actions();boolean inside=event.x()>=menuX&&event.x()<menuX+142&&event.y()>=menuY&&event.y()<menuY+actions.length*23;
            if(inside&&event.button()==0){act((int)(event.y()-menuY)/23);return true;}menu=null;return true;}
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean keyPressed(KeyEvent event){if(menu!=null&&event.key()==256){menu=null;return true;}return super.keyPressed(event);}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        LoomScreenChrome.renderBackdrop(g,width,height);LoomScreenChrome.renderBrandHeader(g,width,"Design library",LoomUiTheme.compact(width,height));
        LoomScreenChrome.panel(g,left-1,top+51,right+1,bottom-25);
        super.render(g,mx,my,delta);
        if(cards.isEmpty())g.drawCenteredString(font,Component.literal(query.isEmpty()?"No designs here yet":"No matching designs"),(left+right)/2,top+85,LoomUiTheme.TEXT_MUTED);
        g.drawCenteredString(font,Component.literal("Page "+(page+1)+" · "+filtered().size()+" designs"),(left+right)/2,bottom-16,LoomUiTheme.TEXT_MUTED);
        LoomScreenChrome.footer(g,width,height,font.plainSubstrByWidth(message,width-160),tab==1?"Recovery drafts":tab==2?"Trash":"Local library");
        if(menu!=null){String[] a=actions();LoomScreenChrome.panel(g,menuX,menuY,menuX+142,menuY+a.length*23);
            for(int i=0;i<a.length;i++){int y=menuY+i*23;if(mx>=menuX&&mx<menuX+142&&my>=y&&my<y+23)g.fill(menuX+1,y+1,menuX+141,y+22,LoomUiTheme.PANEL_INNER);
                g.drawString(font,a[i],menuX+9,y+8,i==4&&tab!=2?0xFFFF718B:LoomUiTheme.TEXT,false);}}
    }
    private void closeCards(){for(var card:cards)card.close();cards.clear();descriptors.clear();}
    @Override public void removed(){closeCards();super.removed();}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
