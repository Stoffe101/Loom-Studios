package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.render.LoomTextureCompiler;
import dev.loomstudios.project.CanvasResolution;
import dev.loomstudios.project.ElytraWing;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.ui.CanvasViewportTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Two semantic wing views, sharing the same pixel-boundary transform as Cape. */
public final class LoomElytraCanvasWidget extends AbstractWidget implements LoomMiddlePanTarget {
    @FunctionalInterface public interface PixelAction { void apply(ElytraWing wing, int x, int y); }
    public interface StrokeLifecycle { void begin(); void end(); }
    private final Supplier<LoomProject> projectSupplier;
    private final LongSupplier revisionSupplier;
    private final PixelAction pixelAction;
    private final StrokeLifecycle strokeLifecycle;
    private final Identifier[] textureIds = new Identifier[2];
    private final DynamicTexture[] textures = new DynamicTexture[2];
    private final NativeImage[] images = new NativeImage[2];
    private long renderedRevision = Long.MIN_VALUE;
    private boolean strokeActive, panning;
    private int zoomIndex, panX, panY;
    private java.util.function.Supplier<dev.loomstudios.project.PixelDrawing.Tool> tool=()->dev.loomstudios.project.PixelDrawing.Tool.PENCIL;
    private java.util.function.IntSupplier color=()->0xFF22D7E8,brush=()->1;
    private java.util.function.BooleanSupplier filled=()->false;
    public interface Gesture {void finish(ElytraWing wing,int x0,int y0,int x1,int y1);}
    private Gesture gesture;
    private ElytraWing gestureWing;
    private int startX,startY,endX,endY;
    private dev.loomstudios.project.PixelSelection selection;
    private ElytraWing selectionWing;
    public void setDrawing(java.util.function.Supplier<dev.loomstudios.project.PixelDrawing.Tool> tool,java.util.function.IntSupplier color,java.util.function.IntSupplier brush,java.util.function.BooleanSupplier filled,Gesture gesture){this.tool=tool;this.color=color;this.brush=brush;this.filled=filled;this.gesture=gesture;}
    public void setSelection(ElytraWing wing,dev.loomstudios.project.PixelSelection selection){selectionWing=wing;this.selection=selection;}
    private boolean delayed(){return switch(tool.get()){case LINE,RECTANGLE,CIRCLE,SELECT->true;default->false;};}

    private static final float[] ZOOMS = {1, 1.25F, 1.5F, 2, 3, 4, 6, 8};
    public record ViewState(int zoomIndex, int panX, int panY) { }
    public ViewState viewState() { return new ViewState(zoomIndex, panX, panY); }
    public void restoreViewState(ViewState state) { if (state != null) { zoomIndex = state.zoomIndex; panX = state.panX; panY = state.panY; } }

    public LoomElytraCanvasWidget(int x, int y, int width, int height,
            Supplier<LoomProject> projectSupplier, LongSupplier revisionSupplier,
            PixelAction pixelAction, StrokeLifecycle lifecycle) {
        super(x, y, width, height, Component.literal("Elytra canvas"));
        this.projectSupplier = projectSupplier; this.revisionSupplier = revisionSupplier;
        this.pixelAction = pixelAction; strokeLifecycle = lifecycle;
        String id = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 2; i++) textureIds[i] = Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "editor/wings_" + id + "_" + i);
    }

    private CanvasViewportTransform pairTransform() {
        int scale = CanvasResolution.fromCanvas(projectSupplier.get().elytra()).scale();
        // Four logical texture cells form the gap, so fit/zoom/pan apply to the pair as one object.
        return CanvasViewportTransform.fit(getX() + 8, getY() + 24, getRight() - 8, getBottom() - 8,
                ElytraWing.LEFT.width(scale) * 2 + 4 * scale, ElytraWing.LEFT.height(scale), ZOOMS[zoomIndex], panX, panY);
    }
    private CanvasViewportTransform wingTransform(ElytraWing wing) {
        var pair = pairTransform();
        int scale = CanvasResolution.fromCanvas(projectSupplier.get().elytra()).scale();
        int left = wing == ElytraWing.LEFT ? pair.left() : pair.screenX(ElytraWing.LEFT.width(scale) + 4 * scale);
        return new CanvasViewportTransform(left, pair.top(), wing.width(scale), wing.height(scale), pair.pixelScale(),
                pair.clipLeft(), pair.clipTop(), pair.clipRight(), pair.clipBottom());
    }

    @Override protected void renderWidget(GuiGraphics g, int mx, int my, float tick) {
        LoomScreenChrome.panel(g, getX(), getY(), getRight(), getBottom());
        var font = Minecraft.getInstance().font;
        g.drawString(font, Component.literal("Wing textures"), getX() + 8, getY() + 7, LoomUiTheme.TEXT_MUTED, false);
        String zoom = Math.round(ZOOMS[zoomIndex] * 100) + "% · Wheel zoom / middle drag";
        if (getWidth() > 380 && dev.loomstudios.client.project.LoomPreferences.get().enabled("shortcuts",true)) g.drawString(font, Component.literal(zoom), getRight() - font.width(zoom) - 8, getY() + 7, LoomUiTheme.TEXT_FAINT, false);
        ensureTextures();
        var pair = pairTransform();
        LoomScreenChrome.workSurface(g, pair.clipLeft(), pair.clipTop(), pair.clipRight(), pair.clipBottom());
        g.enableScissor(pair.clipLeft(), pair.clipTop(), pair.clipRight(), pair.clipBottom());
        int axis = pair.left() + pair.drawWidth() / 2;
        for (int y = pair.clipTop(); y < pair.clipBottom(); y += 6) g.fill(axis, y, axis + 1, Math.min(y + 2, pair.clipBottom()), 0x66546E8E);
        for (ElytraWing wing : ElytraWing.values()) {
            var t = wingTransform(wing); int index = wing == ElytraWing.LEFT ? 0 : 1;
            g.blit(RenderPipelines.GUI_TEXTURED, textureIds[index], t.left(), t.top(), 0, 0,
                    t.drawWidth(), t.drawHeight(), t.columns(), t.rows(), t.columns(), t.rows());
            if (t.pixelScale() >= 5 && dev.loomstudios.client.project.LoomPreferences.get().enabled("grid",true)) {
                for (int x = 0; x <= t.columns(); x++) g.fill(t.screenX(x), t.top(), t.screenX(x) + 1, t.screenY(t.rows()), 0x28000000);
                for (int y = 0; y <= t.rows(); y++) g.fill(t.left(), t.screenY(y), t.screenX(t.columns()), t.screenY(y) + 1, 0x28000000);
            }
            if(selection!=null&&wing==selectionWing)drawBounds(g,t,selection.minX(),selection.minY(),selection.maxX(),selection.maxY());
            if(strokeActive&&delayed()&&gestureWing==wing){
                if(tool.get()==dev.loomstudios.project.PixelDrawing.Tool.SELECT)drawBounds(g,t,Math.min(startX,endX),Math.min(startY,endY),Math.max(startX,endX),Math.max(startY,endY));
                else {var mask=dev.loomstudios.project.PixelDrawing.shape(new dev.loomstudios.project.PixelPatch(t.columns(),t.rows(),new int[t.columns()*t.rows()]),tool.get(),startX,startY,endX,endY,brush.getAsInt(),color.getAsInt()|0xFF000000,filled.getAsBoolean());int[] p=mask.data();for(int y=0;y<t.rows();y++)for(int x=0;x<t.columns();x++)if(p[y*t.columns()+x]!=0)g.fill(t.screenX(x),t.screenY(y),t.screenX(x+1),t.screenY(y+1),p[y*t.columns()+x]);}
            }
            int[] pixel = t.pixelAt(mx, my);
            if (pixel != null) {
                int x = t.screenX(pixel[0]), y = t.screenY(pixel[1]), s = t.pixelScale();
                g.fill(x,y,x+s,y+1,LoomUiTheme.ACCENT); g.fill(x,y+s-1,x+s,y+s,LoomUiTheme.ACCENT);
                g.fill(x,y,x+1,y+s,LoomUiTheme.ACCENT); g.fill(x+s-1,y,x+s,y+s,LoomUiTheme.ACCENT);
            }
        }
        g.disableScissor();
    }

    private void ensureTextures() {
        var project = projectSupplier.get(); int scale = CanvasResolution.fromCanvas(project.elytra()).scale();
        int width = ElytraWing.LEFT.width(scale), height = ElytraWing.LEFT.height(scale);
        boolean resized = images[0] == null || images[0].getWidth() != width || images[0].getHeight() != height;
        if (resized) {
            releaseTextures();
            for (int i = 0; i < 2; i++) {
                images[i] = new NativeImage(NativeImage.Format.RGBA, width, height, false);
                textures[i] = new DynamicTexture(() -> "Loom wing preview", images[i]);
                Minecraft.getInstance().getTextureManager().register(textureIds[i], textures[i]);
            }
            renderedRevision = Long.MIN_VALUE;
        }
        if (renderedRevision == revisionSupplier.getAsLong()) return;
        int[] pixels = LoomTextureCompiler.compile(project.elytra(), 0, false, false);
        for (ElytraWing wing : ElytraWing.values()) {
            int index = wing == ElytraWing.LEFT ? 0 : 1;
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                int color = pixels[wing.atlasY(y, scale) * project.elytra().width() + wing.atlasX(x, scale)];
                int checker = ((x + y) & 1) == 0 ? 0xFF303E51 : 0xFF233044;
                int alpha = color >>> 24;
                int result = 0xFF000000;
                for (int shift : new int[]{0,8,16}) result |= ((((color >>> shift) & 255) * alpha + ((checker >>> shift) & 255) * (255 - alpha)) / 255) << shift;
                images[index].setPixel(x, y, result);
            }
            textures[index].upload();
        }
        renderedRevision = revisionSupplier.getAsLong();
    }
    private void drawBounds(GuiGraphics g,CanvasViewportTransform t,int x0,int y0,int x1,int y1){int l=t.screenX(x0),top=t.screenY(y0),r=t.screenX(x1+1),b=t.screenY(y1+1);g.fill(l,top,r,top+1,LoomUiTheme.ACCENT);g.fill(l,b-1,r,b,LoomUiTheme.ACCENT);g.fill(l,top,l+1,b,LoomUiTheme.ACCENT);g.fill(r-1,top,r,b,LoomUiTheme.ACCENT);}
    private void applyAt(double x, double y) {
        for (ElytraWing wing : ElytraWing.values()) {
            int[] pixel = wingTransform(wing).pixelAt(x, y);
            if (pixel != null) {
                if(delayed()){
                    if(gestureWing==null){gestureWing=wing;startX=pixel[0];startY=pixel[1];}
                    if(wing==gestureWing){endX=pixel[0];endY=pixel[1];}
                } else pixelAction.apply(wing,pixel[0],pixel[1]);
                return;
            }
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 2 && isMouseOver(event.x(), event.y())) { panning = true; return true; }
        return super.mouseClicked(event, doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (panning && event.button() == 2) { panX += (int)Math.round(dx); panY += (int)Math.round(dy);
            var t = pairTransform();
            panX = t.left() - (t.clipLeft() + (t.clipRight() - t.clipLeft() - t.drawWidth()) / 2);
            panY = t.top() - (t.clipTop() + (t.clipBottom() - t.clipTop() - t.drawHeight()) / 2);
            return true; }
        return super.mouseDragged(event, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 2 && panning) { panning = false; return true; }
        return super.mouseReleased(event);
    }
    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) {
        if (!isMouseOver(x,y) || sy == 0) return false;
        zoomIndex = Math.max(0, Math.min(ZOOMS.length - 1, zoomIndex + (sy > 0 ? 1 : -1)));
        if (zoomIndex == 0) { panX = 0; panY = 0; }
        return true;
    }
    @Override public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return;
        if (!strokeActive) { strokeActive = true;gestureWing=null; strokeLifecycle.begin(); }
        applyAt(event.x(), event.y());
    }
    @Override protected void onDrag(MouseButtonEvent event, double dx, double dy) { if (event.button() == 0) applyAt(event.x(), event.y()); }
    @Override public void onRelease(MouseButtonEvent event) { if (strokeActive) {
        if(delayed()&&gestureWing!=null&&gesture!=null)gesture.finish(gestureWing,startX,startY,endX,endY);
        strokeActive=false;gestureWing=null;strokeLifecycle.end();
    } }
    private void releaseTextures() {
        for (int i = 0; i < 2; i++) if (textures[i] != null) { Minecraft.getInstance().getTextureManager().release(textureIds[i]); textures[i] = null; images[i] = null; }
    }
    public void close() { if (strokeActive) { strokeActive = false; strokeLifecycle.end(); } releaseTextures(); }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) { output.add(NarratedElementType.TITLE, getMessage()); }
}
