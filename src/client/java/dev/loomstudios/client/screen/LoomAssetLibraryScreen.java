package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.EditorOverlayState;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomImagePreviewWidget;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
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
  private String category = "Featured", query = "", message = "Choose artwork · drag or click to place";
  private int size = 9, page;
  private boolean recolor, armed, draggingTile, placing, editPixels, erasePixels, draggingObject, wornMode;
  private int startPixelX, startPixelY, lastX, lastY;
  private LoomImagePreviewWidget preview;
  private LoomPlayerPreviewWidget wornPreview;
  private LoomPlayerPreviewWidget.ViewState lastWornView;
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
    // A 1x Cape cannot represent a 48px painterly sprite. At 4x/8x
    // start with a proportionally larger placement so source detail survives.
    size = Math.min(256, 7 * CanvasResolution.fromCanvas(canvas()).scale());
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

  private void nudgeSelectedOpacity(float delta) {
    var selected=layer();
    if(selected==null||selected.locked())return;
    float value=Math.max(0,Math.min(1,Math.round((selected.opacity()+delta)*10)/10f));
    ClientProjectWorkspace.apply(p -> wing
        ? ProjectEdits.setElytraLayerOpacity(p,layerId,value)
        : ProjectEdits.setCapeLayerOpacity(p,layerId,value));
    message=selected.name()+" opacity "+Math.round(value*100)+"%";
  }

  private boolean splitWornPreview() {
    return width >= 1050 && height >= 450 && !editPixels && !wornMode;
  }

  /** Also used by the Minecraft screenshot fixtures to capture the equipped-looking view. */
  public void showWornPreview(boolean value) {
    wornMode=value;
    armed=false;
    editPixels=false;
    draggingTile=false;
    placing=false;
    rebuildWidgets();
  }

  private void showPreviewController() {
    if(preview==null)return;
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
    if (wornPreview != null) lastWornView=wornPreview.viewState();
    preview=null;
    wornPreview=null;
    tiles.clear();
    int top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8;
    // Let the illustrated catalog breathe on ultrawide without starving the
    // canvas at GUI3. This is not a uniformly scaled mobile layout.
    int sideWidth = width >= 1300 ? Math.min(350, width / 4)
        : Math.max(182, Math.min(244, width / 3));
    int right = width - sideWidth - 8;
    addRenderableWidget(new LoomButton(8,top,52,20,Component.literal("Back"),this::onClose));
    int viewX=right-165;
    addRenderableWidget(new LoomButton(64,top,Math.max(100,viewX-68),20,
        Component.literal(wing ? "Elytra · " + surface.label() : "Cape · " + face.displayName()),
        () -> {})).active=false;
    var view=addRenderableWidget(new LoomButton(viewX,top,85,20,
        Component.literal(wornMode ? (width>=1050 ? "2D + 3D" : "2D Design") : splitWornPreview() ? "Focus 3D" : "3D Worn"),
        () -> showWornPreview(!wornMode)));
    view.active=true;
    view.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
        wornMode ? "Return to the drawing surface and the side-by-side preview"
            : splitWornPreview() ? "Focus on the full-size 3D worn cosmetic"
                : "Inspect the actual cape or Elytra on your character")));
    addRenderableWidget(new LoomButton(right-76,top,72,20,Component.literal("Done"),this::onClose));

    // On comfortable/wide displays the artwork and actual 3D cosmetic are both
    // visible, as in approved reference02/04. Compact GUI3 focuses on one at
    // a time so we do not crush two previews into unusable postage stamps.
    int previewW=right-12;
    boolean split=splitWornPreview();
    int artW=split ? Math.max(220,(int)(previewW*.64)) : previewW;
    if(!wornMode || split || editPixels) {
      preview=addRenderableWidget(new LoomImagePreviewWidget(8,top+25,artW,
          height-top-82,
          Component.literal(editPixels ? "Edit Asset Pixels" : "Your Design · Drop assets here"),
          this::image,
          () -> 37 * ClientProjectWorkspace.revision()
              + 11 * EditorOverlayState.revision() + (editPixels ? 1 : 0)));
      showPreviewController();
      // Guidance belongs in the footer/inspector, not in a full-canvas tooltip.
      preview.setTooltip(null);
    }
    if ((wornMode || split) && !editPixels) {
      int wornX=split ? artW+12 : 8;
      int wornW=split ? Math.max(140,previewW-artW-4) : previewW;
      wornPreview=addRenderableWidget(new LoomPlayerPreviewWidget(
          wornX,top+25,wornW,height-top-82,
          ClientProjectWorkspace::project,
          wing ? LoomPlayerPreviewWidget.Mode.ELYTRA : LoomPlayerPreviewWidget.Mode.CAPE));
      wornPreview.restoreViewState(lastWornView == null
          ? new LoomPlayerPreviewWidget.ViewState(25.0F,0.0F,1.15F)
          : lastWornView);
      // Show the decorated outside wing surfaces when first inspecting the
      // artwork. Gliding is still available by clicking the 3D preview header,
      // but its nearly horizontal wings obscure the painted outer faces.
      if(wing && lastWornView==null)wornPreview.setPose(
          dev.loomstudios.client.render.LoomPreviewState.PreviewPose.STANDING);
    }

    int x=right+4, w=width-x-8;
    // Assets and Edit are clear separate tasks, using the same canvas and 3D
    // view. One contextual inspector prevents a giant always-visible tool wall.
    int tabW=(w-4)/2;
    var browseTab=addRenderableWidget(new LoomButton(x,top,tabW,20,
        Component.literal(propertiesTab?"Assets":"● Assets"),
        () -> {propertiesTab=false;rebuildWidgets();}));
    var editTab=addRenderableWidget(new LoomButton(x+tabW+4,top,w-tabW-4,20,
        Component.literal(propertiesTab?"● Edit":"Edit"),
        () -> {propertiesTab=true;armed=false;rebuildWidgets();}));
    editTab.active=layer()!=null&&layer().kind()==LayerKind.IMAGE;
    categoryButton=null;search=null;previous=null;next=null;modeButton=null;
    if(propertiesTab) {
      buildAssetProperties(x,top+25,w);
    } else {
      categoryButton=addRenderableWidget(new LoomButton(x,top+24,w,20,
          Component.literal("Category: "+category),() -> showChoices(categoryButton, "Choose asset category",
              CreativeAssetCatalog.categories().stream()
                  .map(v -> new dev.loomstudios.client.ui.LoomChoicePopup.Option<>(v,v,
                      v.equals("All")?"Browse everything":"Browse "+v)).toList(),
              category,c -> {category=c;page=0;updateTiles();
                categoryButton.setMessage(Component.literal("Category: "+category));})));
      categoryButton.setIcon(LoomButton.Icon.DOWN);
      search=addRenderableWidget(new EditBox(font,x,top+47,w,18,Component.literal("Find artwork")));
      search.setHint(Component.literal("Search stars, cloud, trees…"));
      search.setValue(query);
      search.setResponder(v -> {query=v;page=0;updateTiles();});
      int gridTop=top+70, gridBottom=height-73;
      int columns=w>=300?3:2;
      int tileWidth=(w-(columns-1)*6)/columns;
      int tileHeight=height>=510?72:height>=420?61:52;
      int rows=Math.max(1,(gridBottom-gridTop)/tileHeight);
      for(int i=0;i<rows*columns;i++){
        var tile=addRenderableWidget(new Tile(
            x+(i%columns)*(tileWidth+6),gridTop+(i/columns)*tileHeight,
            tileWidth, tileHeight-3));
        tiles.add(tile);
      }
      int bottom=height-72, step=(w-8)/3;
      previous=addRenderableWidget(new LoomButton(x,bottom,step,20,Component.literal("◀ Prev"),
          () -> {page=Math.max(0,page-1);updateTiles();}));
      next=addRenderableWidget(new LoomButton(x+step+4,bottom,step,20,
          Component.literal("Next ▶"),() -> {page++;updateTiles();}));
      addRenderableWidget(new LoomButton(x+2*(step+4),bottom,w-2*(step+4),20,
          Component.literal("More Tools"),() -> minecraft.setScreen(
              new LoomCreativeAssetsScreen(this,wing,layerId,face,selectedWing,surface,color,null))));
      addRenderableWidget(new LoomButton(x,bottom+23,step,20,Component.literal("Smaller"),
          () -> {size=Math.max(1,size-1);}));
      addRenderableWidget(new LoomButton(x+step+4,bottom+23,step,20,
          Component.literal("Larger"),() -> {size=Math.min(256,size+1);}));
      modeButton=addRenderableWidget(new LoomButton(x+2*(step+4),bottom+23,
          w-2*(step+4),20,Component.literal(recolor?"Tint ON":"Tint OFF"),
          () -> {recolor=!recolor;modeButton.setMessage(Component.literal(recolor?"Tint ON":"Tint OFF"));}));
    }
    pixelButton=addRenderableWidget(new LoomButton(8,height-51,112,20,
        Component.literal(editPixels?"Return to Design":"Edit Selected Pixels"),
        () -> {var l=layer();if(l==null||l.kind()!=LayerKind.IMAGE){message="Place or select an Image layer first";return;}
          if(!l.imageData().frames().isEmpty()){message="Animated images use the Frame Editor";return;}
          editPixels=!editPixels;armed=false;rebuildWidgets();}));
    pixelButton.active=layer()!=null&&layer().kind()==LayerKind.IMAGE;
    if(editPixels) addRenderableWidget(new LoomButton(124,height-51,66,20,
        Component.literal(erasePixels?"Eraser":"Pencil"),
        () -> {erasePixels=!erasePixels;rebuildWidgets();}));
    else if(!propertiesTab&&layer()!=null&&layer().kind()==LayerKind.IMAGE) {
      // A minimal *contextual* property strip. Do not add permanent chrome
      // while nothing is selected; transform handles stay on the artwork.
      int controls=Math.max(0,right-12-124), propertyStep=Math.min(86,(controls-8)/3);
      if(propertyStep>=52){
        var less=addRenderableWidget(new LoomButton(124,height-51,propertyStep,20,
            Component.literal("Opacity −"),()->nudgeSelectedOpacity(-.1f)));
        var more=addRenderableWidget(new LoomButton(128+propertyStep,height-51,propertyStep,20,
            Component.literal("Opacity +"),()->nudgeSelectedOpacity(.1f)));
        var rotate=addRenderableWidget(new LoomButton(132+2*propertyStep,height-51,propertyStep,20,
            Component.literal("Rotate ↻"),()->editTransform(t->
                t.withRotation(t.rotationDegrees()+45))));
        boolean editable=!layer().locked();
        less.active=editable;more.active=editable;rotate.active=editable;
      }
    }
    updateTiles();
  }

  private void buildAssetProperties(int x,int y,int w) {
    var l=layer();
    if(l==null||l.kind()!=LayerKind.IMAGE) {
      var hint=addRenderableWidget(new LoomButton(x,y,w,20,
          Component.literal("Select an editable layer"),()->{}));
      hint.active=false;
      return;
    }
    var header=addRenderableWidget(new LoomButton(x,y,w,20,
        Component.literal(l.name()),()->{}));
    header.active=false;
    header.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
        Component.literal(l.name()+" · "+Math.round(l.opacity()*100)+"% opacity")));
    int half=(w-4)/2;
    boolean canEdit=!l.locked();
    y+=27;
    var minus=addRenderableWidget(new LoomButton(x,y,half,20,Component.literal("Opacity −10%"),
        ()->nudgeSelectedOpacity(-.1f)));
    var plus=addRenderableWidget(new LoomButton(x+half+4,y,w-half-4,20,Component.literal("Opacity +10%"),
        ()->nudgeSelectedOpacity(.1f)));
    y+=26;
    var left=addRenderableWidget(new LoomButton(x,y,half,20,Component.literal("◀ Move 1px"),
        ()->nudgeAsset(-1,0)));
    var rightButton=addRenderableWidget(new LoomButton(x+half+4,y,w-half-4,20,
        Component.literal("Move 1px ▶"),()->nudgeAsset(1,0)));
    y+=26;
    var up=addRenderableWidget(new LoomButton(x,y,half,20,Component.literal("▲ Up 1px"),
        ()->nudgeAsset(0,-1)));
    var down=addRenderableWidget(new LoomButton(x+half+4,y,w-half-4,20,
        Component.literal("▼ Down 1px"),()->nudgeAsset(0,1)));
    y+=26;
    var smaller=addRenderableWidget(new LoomButton(x,y,half,20,Component.literal("Size −1px"),
        ()->resizeAsset(-1)));
    var bigger=addRenderableWidget(new LoomButton(x+half+4,y,w-half-4,20,Component.literal("Size +1px"),
        ()->resizeAsset(1)));
    y+=26;
    var ccw=addRenderableWidget(new LoomButton(x,y,half,20,Component.literal("Rotate −15°"),
        ()->editTransform(t->t.withRotation(t.rotationDegrees()-15))));
    var cw=addRenderableWidget(new LoomButton(x+half+4,y,w-half-4,20,
        Component.literal("Rotate +15°"),
        ()->editTransform(t->t.withRotation(t.rotationDegrees()+15))));
    y+=26;
    var flipH=addRenderableWidget(new LoomButton(x,y,half,20,Component.literal("Flip H"),
        ()->editTransform(t->t.withMirrors(!t.mirrorHorizontal(),t.mirrorVertical()))));
    var flipV=addRenderableWidget(new LoomButton(x+half+4,y,w-half-4,20,Component.literal("Flip V"),
        ()->editTransform(t->t.withMirrors(t.mirrorHorizontal(),!t.mirrorVertical()))));
    for(var control:new LoomButton[]{minus,plus,left,rightButton,up,down,smaller,bigger,ccw,cw,flipH,flipV})
      control.active=canEdit;
  }

  private void nudgeAsset(int dx,int dy) {
    var c=canvas();
    editTransform(t->t.withCenter(t.centerX()+dx/(double)c.width(),
        t.centerY()+dy/(double)c.height()));
  }

  private void resizeAsset(int pixelDelta) {
    var c=canvas();
    editTransform(t->{
      double current=t.width()*c.width();
      double adjusted=Math.max(1.0,current+pixelDelta);
      double ratio=adjusted/current;
      return t.withSize(t.width()*ratio,t.height()*ratio);
    });
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
      if(tiles.get(i).entry!=null){
        var entry=tiles.get(i).entry;
        var art=entry.stamp().patch();
        tiles.get(i).setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal(entry.stamp().name()+" · "+entry.category()
                +" · "+art.width()+"×"+art.height()+" source pixels")));
      }
    }
    if(previous!=null)previous.active=page>0;
    if(next!=null)next.active=(page+1)*perPage<list.size();
  }
  private void pick(CreativeAssetCatalog.Entry entry) {
    selectedAsset=entry;
    armed=true;
    editPixels=false;
    if (preview != null) {
      // Selecting a new asset disables transform/pixel gestures on the previous object.
      // Only the placement ghost handles pointer input until this asset is committed.
      preview.setPixelAction(null);
      preview.setTransformController(null);
    }
    // Large cloudbanks and trees should fill a composition; individual stars
    // should not cover an entire cape by default. The controls remain adjustable.
    int resolution=scale();
    String family=entry.category();
    int base=family.equals("Clouds & Mist") ? 9
        : family.equals("Nature") ? 7
        : entry.stamp().name().contains("Star") ? 4 : 6;
    size=Math.max(3, Math.min(256, base*resolution));
    message="Selected "+entry.stamp().name()+" · click or drag onto the design"
        +(resolution<4 && entry.tags().contains("featured") ? " · finer detail at 4×/8×" : "");
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
    // Compact footer has less room than the left/status text might imply;
    // keep the guidance usable instead of letting it run underneath the center slogan.
    var activeLayer = layer();
    String status = activeLayer != null
        ? activeLayer.name()+" · "+Math.round(activeLayer.opacity()*100f)+"% opacity"
        : selectedAsset == null ? "Browse assets" : selectedAsset.stamp().name()+" · "+size+" px";
    String footerMessage=LoomUiTheme.compact(width,height)
        ? armed ? "Click canvas to place"
            : activeLayer != null ? "Selected asset · edit / move / rotate" : "Choose an asset"
        : armed ? "Click or drag on the canvas to place "+selectedAsset.stamp().name()
            : activeLayer != null ? "Selected: "+activeLayer.name()+" · drag handles to edit" : message;
    LoomScreenChrome.footer(g,width,height,footerMessage,status);
    renderChoices(g,mx,my);
  }
  @Override public void removed() {
    if(preview!=null)preview.close();
    ClientProjectWorkspace.endCompoundEdit();
    super.removed();
  }
  @Override public void onClose() {
    if(wornPreview!=null)lastWornView=wornPreview.viewState();
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
      // Correct nearest-neighbour fit for both a 3px icon and a 48px
      // original illustration. The previous thumbnail loop drew 48x48
      // artwork beyond a 49px-high tile, clipping most of the asset.
      int maxW=Math.max(1,getWidth()-8), maxH=Math.max(1,getHeight()-20);
      double fit=Math.min(3.0, Math.min(maxW/(double)patch.width(),
          maxH/(double)patch.height()));
      int sw=Math.max(1,(int)Math.floor(patch.width()*fit));
      int sh=Math.max(1,(int)Math.floor(patch.height()*fit));
      int x0=getX()+(getWidth()-sw)/2;
      int y0=getY()+2+Math.max(0,(maxH-sh)/2);
      // Adjacent equal-color pixels share a fill draw call. A packed 3-column,
      // 7-row gallery should not enqueue tens of thousands of draw calls/frame.
      for(int py=0;py<sh;py++) {
        int srcY=Math.min(patch.height()-1,(int)((py+.5)/fit));
        int px=0;
        while(px<sw){
          int srcX=Math.min(patch.width()-1,(int)((px+.5)/fit));
          int c=raw[srcY*patch.width()+srcX],end=px+1;
          if((c>>>24)==0){px=end;continue;}
          while(end<sw){
            int nx=Math.min(patch.width()-1,(int)((end+.5)/fit));
            if(raw[srcY*patch.width()+nx]!=c)break;
            end++;
          }
          g.fill(x0+px,y0+py,x0+end,y0+py+1,c);
          px=end;
        }
      }
      if(entry.tags().contains("featured")) {
        // Tiny secondary cyan indicator: this is actual multi-color artwork.
        g.fill(getRight()-7,getY()+3,getRight()-4,getY()+6,LoomUiTheme.ACCENT);
      }
      String name=entry.stamp().name();
      int maxText=Math.max(8,getWidth()-8);
      if(font.width(name)>maxText)
        name=font.plainSubstrByWidth(name,Math.max(4,maxText-font.width("…")))+"…";
      PremiumText.drawString(g,font,Component.literal(name),
          getX()+4,getBottom()-13,LoomUiTheme.TEXT,false);
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
