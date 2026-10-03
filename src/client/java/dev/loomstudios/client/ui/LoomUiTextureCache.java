package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/** Bounded render-thread cache for generated UI artwork. Never stores project state. */
public final class LoomUiTextureCache {
    private static final int LIMIT=32;
    private static final LinkedHashMap<String,Entry> entries=new LinkedHashMap<>(32,.75f,true);
    private static long sequence,uploads;
    private LoomUiTextureCache() {}
    public static void draw(GuiGraphics graphics,String key,int x,int y,int width,int height,
                            Supplier<NativeImage> pixels) {
        Entry entry=entries.get(key);
        if(entry==null) {
            NativeImage image=pixels.get();
            Identifier id=Identifier.fromNamespaceAndPath("loom-studios","ui/cache_"+sequence++);
            DynamicTexture texture=new DynamicTexture(()->"Loom UI "+key,image);
            Minecraft.getInstance().getTextureManager().register(id,texture);
            texture.upload();uploads++;
            entry=new Entry(id,image.getWidth(),image.getHeight());entries.put(key,entry);
            if(entries.size()>LIMIT) {
                var oldest=entries.entrySet().iterator();var evicted=oldest.next();oldest.remove();
                Minecraft.getInstance().getTextureManager().release(evicted.getValue().id());
            }
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED,entry.id(),x,y,0f,0f,width,height,
                entry.width(),entry.height(),entry.width(),entry.height());
    }
    public static long uploads() {return uploads;}
    public static int size() {return entries.size();}
    public static void clear() {
        var manager=Minecraft.getInstance().getTextureManager();
        entries.values().forEach(entry->manager.release(entry.id()));entries.clear();
    }
    private record Entry(Identifier id,int width,int height) {}
}
