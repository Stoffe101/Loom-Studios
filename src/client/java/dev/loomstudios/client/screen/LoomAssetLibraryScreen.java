package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.EditorOverlayState;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomImagePreviewWidget;
import dev.loomstudios.client.ui.LoomScreenChrome;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.client.ui.premium.PremiumText;
import dev.loomstudios.image.PixelImage;
import dev.loomstudios.project.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * First-stage editor-integrated asset browser: thumbnail → drag onto artwork or click to pick →
 * click/drag on artwork. Each result is a project-owned editable IMAGE layer, never a paint dab.
 *
 * <p>Existing reference/GIF/onion/custom-stamp tools remain available through Creative Tools.
 * The future collection-instance schema is intentionally not invented by this screen.
 */
public final class LoomAssetLibraryScreen extends LoomPointerScreen {
  private final Screen parent;
  private final boolean wing;
  private final CapeUvRegion face;
  private final ElytraWing selectedWing;
  private final ElytraSurface surface;
  private final int color;
  private UUID layerId;
  private CreativeAssetCatalog.Entry selectedAsset;
  private String category = "All", query = "", message = "Drag an asset onto the design, or click then place";
  private int size = 9, page;
  private boolean recolor, armed, draggingTile, placing, editPixels, erasePixels, draggingObject;
  private int startPixelX, startPixelY, lastX, lastY;
  private LoomImagePreviewWidget preview;
  private EditBox search;
  private LoomButton categoryButton, previous, next, modeButton, pixelButton;
  private final List<Tile> tiles = new ArrayList<>();
  private PixelImage cachedComposite;
  private long cachedRevision = Long.MIN_VALUE;

  public LoomAssetLibraryScreen(Screen parent, boolean wing, UUID layerId,
      CapeUvRegion face, ElytraWing selectedWing, ElytraSurface surface, int color) {
    super(Component.literal("Asset Library"));
    this.parent = parent;
    this.wing = wing;
    this.layerId = layerId;
    this.face = face == null ? CapeUvRegion.OUTSIDE : face;
    this.selectedWing = selectedWing == null ? ElytraWing.LEFT : selectedWing;
    this.surface = surface == null ? ElytraSurface.OUTSIDE : surface;
    this.color = color;
  }

  private AnimationChannel channel() {
    return wing ? AnimationChannel.ELYTRA : AnimationChannel.CAPE;
  }
  private LoomCanvas canvas() {
    return wing ? ClientProjectWorkspace.project().elytra() : ClientProjectWorkspace.project().cape();
  }
  private LoomLayer layer() {
    return canvas().layers().stream().filter(l -> l.id().equals(layerId))
        .findFirst().orElse(null);
  }
  private int scale() { return CanvasResolution.fromCanvas(canvas()).scale(); }
  private int faceW() { return wing ? surface.width(scale()) : face.width(scale()); }
  private int faceH() { return wing ? surface.height(scale()) : face.height(scale()); }
  private int faceLeft() { return wing ? surface.atlasX(selectedWing, 0, scale()) : face.atlasX(0, scale()); }
  private int faceTop() { return wing ? surface.atlasY(0, scale()) : face.atlasY(0, scale()); }

  private PixelImage image() {
    var layer = layer();
    if (editPixels && layer != null && layer.kind() == LayerKind.IMAGE)
      return layer.imageData().source();
    long revision = 31 * ClientProjectWorkspace.revision() + EditorOverlayState.revision();
    if (cachedComposite == null || revision != cachedRevision) {
      LoomCanvas c = canvas();
      int[] atlas = EditorOverlayState.composite(ClientProjectWorkspace.project(), channel());
      int[] image = new int[faceW() * faceH()];
      for (int y = 0; y < faceH(); y++)
        System.arraycopy(atlas, (faceTop() + y) * c.width() + faceLeft(),
            image, y * faceW(), faceW());
      cachedComposite = new PixelImage(faceW(), faceH(), image);
      cachedRevision = revision;
    }
    return cachedComposite;
  }

  private LayerTransform selectedTransform() {
    var l = layer();
    if (l == null || l.kind() != LayerKind.IMAGE) return null;
    var t = l.imageData().transform();
    var c = canvas();
    return new LayerTransform(
        (t.centerX() * c.width() - faceLeft()) / faceW(),
        (t.centerY() * c.height() - faceTop()) / faceH(),
        t.width() * c.width() / faceW(), t.height() * c.height() / faceH(),
        t.rotationDegrees(), t.mirrorHorizontal(), t.mirrorVertical());
  }

  private void editTransform(java.util.function.UnaryOperator<LayerTransform> action) {
    var l = layer();
    if (l == null || l.kind() != LayerKind.IMAGE || l.locked()) return;
    try {
      var now = l.imageData().transform();
      var next = action.apply(now);
      ClientProjectWorkspace.apply(p -> AssetPlacement.transform(p, channel(), layerId, next));
    } catch (IllegalArgumentException | IllegalStateException e) { message = e.getMessage(); }
  }

  private void showPreviewController() {
    if (editPixels) {
      preview.setPixelAction((x,y) -> {
        try {
          ClientProjectWorkspace.apply(p ->
              AssetPlacement.editPixel(p, channel(), layerId, x, y,
                  erasePixels ? 0 : (color | 0xFF000000)));
        } catch (IllegalArgumentException | IllegalStateException e) { message = e.getMessage(); }
      });
    } else if (!armed && layer() != null && layer().kind() == LayerKind.IMAGE) {
      preview.setTransformController(new LoomImagePreviewWidget.TransformController() {
        public LayerTransform transform() { return selectedTransform(); }
        public void move(double dx, double dy) {
          var c=canvas();
          editTransform(t -> t.withCenter(
              t.centerX() + dx * faceW() / c.width(),
              t.centerY() + dy * faceH() / c.height()));
        }
        public void scale(double delta) {
          editTransform(t -> t.withSize(
              Math.max(.0001, t.width() * (1 + delta)),
              Math.max(.0001, t.height() * (1 + delta))));
        }
        public void rotate(double degrees) {
          editTransform(t -> t.withRotation(t.rotationDegrees() + degrees));
        }
      });
    }
  }

  @Override protected void init() {
    if (preview != null) preview.close();
    tiles.clear();
    int top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8;
    int sideWidth = Math.max(182, Math.min(236, width / 3));
    int right = width - sideWidth - 8;
    addRenderableWidget(new LoomButton(8,top,52,20,Component.literal("Back"),this::onClose));
    addRenderableWidget(new LoomButton(64,top,Math.max(100,right-140),20,
        Component.literal(wing ? "Elytra · " + surface.label() : "Cape · " + face.displayName()),
        () -> {})).active=false;
    addRenderableWidget(new LoomButton(right-76,top,72,20,Component.literal("Done"),this::onClose));
    preview = addRenderableWidget(new LoomImagePreviewWidget(8,top+25,right-12,
        height-top-82,
        Component.literal(editPixels ? "Edit Asset Pixels" : "Your Design · Drop assets here"),
        this::image,
        () -> 37 * ClientProjectWorkspace.revision()
            + 11 * EditorOverlayState.revision() + (editPixels ? 1 : 0)));
    showPreviewController();

    int x=right+4, w=width-x-8;
    categoryButton=addRenderableWidget(new LoomButton(x,top,w,20,
        Component.literal("Category: "+category),() -> showChoices(categoryButton, "Choose asset category",
            CreativeAssetCatalog.categories().stream()
                .map(v -> new dev.loomstudios.client.ui.LoomChoicePopup.Option<>(v,v,
                    v.equals("All")?"Browse everything":"Browse "+v)).toList(),
            category,c -> {category=c;page=0;updateTiles();categoryButton.setMessage(Component.literal("Category: "+category));})));
    categoryButton.setIcon(LoomButton.Icon.DOWN);
    search=addRenderableWidget(new EditBox(font,x,top+24,w,18,Component.literal("Find artwork")));
    search.setHint(Component.literal("Search stars, cloud, trees…"));
    search.setValue(query);
    search.setResponder(v -> {query=v;page=0;updateTiles();});
    int gridTop=top+47, gridBottom=height-73;
    int tileWidth=(w-6)/2, tileHeight=52;
    int rows=Math.max(1,(gridBottom-gridTop)/tileHeight);
    for(int i=0;i<rows*2;i++){
      var tile=addRenderableWidget(new Tile(x+(i%2)*(tileWidth+6),gridTop+(i/2)*tileHeight,
          tileWidth, tileHeight-3));
      tiles.add(tile);
    }
    int bottom=height-72, step=(w-8)/3;
    previous=addRenderableWidget(new LoomButton(x,bottom,step,20,Component.literal("◀ Prev"),
        () -> {page=Math.max(0,page-1);updateTiles();}));
    next=addRenderableWidget(new LoomButton(x+step+4,bottom,step,20,
        Component.literal("Next ▶"),() -> {page++;updateTiles();}));
    addRenderableWidget(new LoomButton(x+2*(step+4),bottom,w-2*(step+4),20,
        Component.literal("Tools"),() -> minecraft.setScreen(
            new LoomCreativeAssetsScreen(this,wing,layerId,face,selectedWing,surface,color,null))));
    addRenderableWidget(new LoomButton(x,bottom+23,step,20,Component.literal("Smaller"),
        () -> {size=Math.max(1,size-1);}));
    addRenderableWidget(new LoomButton(x+step+4,bottom+23,step,20,
        Component.literal("Larger"),() -> {size=Math.min(256,size+1);}));
    modeButton=addRenderableWidget(new LoomButton(x+2*(step+4),bottom+23,
        w-2*(step+4),20,Component.literal(recolor?"Tint ON":"Tint OFF"),
        () -> {recolor=!recolor;modeButton.setMessage(Component.literal(recolor?"Tint ON":"Tint OFF"));}));
    pixelButton=addRenderableWidget(new LoomButton(8,height-51,112,20,
        Component.literal(editPixels?"Return to Design":"Edit Selected Pixels"),
        () -> {var l=layer();if(l==null||l.kind()!=LayerKind.IMAGE){message="Place or select an Image layer first";return;}
          if(!l.imageData().frames().isEmpty()){message="Animated images use the Frame Editor";return;}
          editPixels=!editPixels;armed=false;rebuildWidgets();}));
    pixelButton.active=layer()!=null&&layer().kind()==LayerKind.IMAGE;
    if(editPixels) addRenderableWidget(new LoomButton(124,height-51,66,20,
        Component.literal(erasePixels?"Eraser":"Pencil"),
        () -> {erasePixels=!erasePixels;rebuildWidgets();}));
    updateTiles();
  }

  private List<CreativeAssetCatalog.Entry> filtered() {
    return CreativeAssetCatalog.search(query,category);
  }
  private void updateTiles() {
    var list=filtered();
    int perPage=Math.max(1,tiles.size());
    page=Math.max(0,Math.min(page,Math.max(0,(list.size()-1)/perPage)));
    for(int i=0;i<tiles.size();i++) {
      int index=page*perPage+i;
      tiles.get(i).entry=index<list.size()?list.get(index):null;
      tiles.get(i).visible=tiles.get(i).entry!=null;
    }
    if(previous!=null)previous.active=page>0;
    if(next!=null)next.active=(page+1)*perPage<list.size();
  }
  private void pick(CreativeAssetCatalog.Entry entry) {
    selectedAsset=entry;
    armed=true;
    editPixels=false;
    message="Selected "+entry.stamp().name()+" · click or drag onto the design";
  }
  private void placeAt(double mx,double my,int longest) {
    if(selectedAsset==null||preview==null)return;
    int[] pos=preview.imagePixelAt(mx,my);
    if(pos==null){message="Move onto the design to place the asset";return;}
    int aw=faceW(),ah=faceH();
    try{
      var p=ClientProjectWorkspace.apply(old ->
          AssetPlacement.place(old,channel(),selectedAsset.stamp().name(),
              selectedAsset.stamp().patch(),face,selectedWing,surface,
              (pos[0]+.5)/aw,(pos[1]+.5)/ah,longest,recolor,color));
      var c=wing?p.elytra():p.cape();
      layerId=c.layers().getLast().id();
      armed=false;
      message=selectedAsset.stamp().name()+" placed as editable layer · drag handles to modify";
      cachedComposite=null;
      rebuildWidgets();
    }catch(IllegalArgumentException|IllegalStateException e){message=e.getMessage();}
  }
  @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice) {
    if(hasChoices())return super.mouseClicked(e,twice);
    if(armed&&!editPixels&&preview!=null&&e.button()==0&&preview.imagePixelAt(e.x(),e.y())!=null){
      int[] pos=preview.imagePixelAt(e.x(),e.y());
      startPixelX=pos[0];startPixelY=pos[1];placing=true;return true;
    }
    if(!armed && !editPixels && preview!=null && preview.imagePixelAt(e.x(),e.y())!=null
        && layer()!=null&&layer().kind()==LayerKind.IMAGE && e.button()==0) {
      draggingObject=true;ClientProjectWorkspace.beginCompoundEdit();
    }
    return super.mouseClicked(e,twice);
  }
  @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy) {
    if(draggingTile||placing)return true;
    return super.mouseDragged(e,dx,dy);
  }
  @Override public boolean mouseReleased(MouseButtonEvent e) {
    if(draggingTile){draggingTile=false;placeAt(e.x(),e.y(),size);return true;}
    if(placing){
      placing=false;
      int[] p=preview.imagePixelAt(e.x(),e.y());
      int requested=size;
      if(p!=null)requested=Math.max(size,Math.max(Math.abs(p[0]-startPixelX),Math.abs(p[1]-startPixelY))*2);
      placeAt(e.x(),e.y(),Math.min(256,requested));
      return true;
    }
    boolean result=super.mouseReleased(e);
    if(draggingObject){draggingObject=false;ClientProjectWorkspace.endCompoundEdit();}
    return result;
  }
  @Override public boolean keyPressed(KeyEvent e) {
    if(hasChoices())return super.keyPressed(e);
    if(e.key()==256&&(armed||editPixels)){armed=false;editPixels=false;rebuildWidgets();return true;}
    return super.keyPressed(e);
  }
  @Override public void render(GuiGraphics g,int mx,int my,float dt) {
    LoomScreenChrome.renderBackdrop(g,width,height);
    LoomScreenChrome.renderBrandHeader(g,width,"Asset Library",LoomUiTheme.compact(width,height));
    super.render(g,mx,my,dt);
    if(!editPixels&&selectedAsset!=null&&(armed||draggingTile||placing)&&preview!=null){
      int[] p=preview.imagePixelAt(mx,my);
      if(p!=null)preview.renderAssetGhost(g,selectedAsset.stamp().patch(),p[0],p[1],size,recolor,color);
    }
    LoomScreenChrome.footer(g,width,height,message,selectedAsset==null?"Select an asset":
        selectedAsset.stamp().name()+" · "+size+" px");
    renderChoices(g,mx,my);
  }
  @Override public void removed() {
    if(preview!=null)preview.close();
    ClientProjectWorkspace.endCompoundEdit();
    super.removed();
  }
  @Override public void onClose() {
    if(parent instanceof CapeEditorScreen c)c.focusAssetLayer(layerId);
    if(parent instanceof ElytraEditorScreen e)e.focusAssetLayer(layerId);
    minecraft.setScreen(parent);
  }
  @Override public boolean isPauseScreen(){return false;}
  @Override public boolean isInGameUi(){return true;}

  private final class Tile extends AbstractWidget {
    private CreativeAssetCatalog.Entry entry;
    Tile(int x,int y,int w,int h){super(x,y,w,h,Component.literal("Asset"));}
    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){
      if(entry==null)return;
      boolean selected=selectedAsset!=null&&selectedAsset.stamp().id().equals(entry.stamp().id());
      int outline=selected?LoomUiTheme.ACCENT:isMouseOver(mx,my)?LoomUiTheme.ACCENT_ALT:LoomUiTheme.BORDER;
      g.fill(getX(),getY(),getRight(),getBottom(),outline);
      g.fill(getX()+1,getY()+1,getRight()-1,getBottom()-1,LoomUiTheme.PANEL_INNER);
      var patch=entry.stamp().patch();
      int[] raw=patch.data();
      int s=Math.max(1,Math.min(3,Math.min(27/patch.width(),24/patch.height())));
      int x0=getX()+getWidth()/2-patch.width()*s/2, y0=getY()+3;
      for(int y=0;y<patch.height();y++)for(int x=0;x<patch.width();x++){
        int c=raw[y*patch.width()+x];if((c>>>24)!=0)
          g.fill(x0+x*s,y0+y*s,x0+(x+1)*s,y0+(y+1)*s,c);
      }
      PremiumText.drawString(g,font,Component.literal(entry.stamp().name()),
          getX()+3,getBottom()-13,LoomUiTheme.TEXT,false);
    }
    @Override public void onClick(MouseButtonEvent e,boolean twice){
      if(entry==null)return;
      pick(entry);
      draggingTile=e.button()==0;
    }
    @Override protected void onDrag(MouseButtonEvent e,double dx,double dy) {}
    @Override protected void updateWidgetNarration(NarrationElementOutput o){
      o.add(NarratedElementType.TITLE,Component.literal(
          entry==null?"Empty":entry.stamp().name()+" · drag onto design or click then place"));
    }
  }
}
