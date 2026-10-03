package dev.loomstudios.client.ui.premium;

import org.joml.Matrix3x2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import static org.lwjgl.nanovg.NanoSVG.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;

/** Render-thread-only vector canvas. Resources belong to the context and are disposed together. */
public final class PremiumPaint {
    private static long vg;
    private static ByteBuffer fontBytes;
    private static long started;
    private static int paths;
    private static int warmup;
    private static final ArrayDeque<Long> times = new ArrayDeque<>();
    private static final HashMap<String,Integer> icons = new HashMap<>();
    private PremiumPaint() {}
    public static void begin(float width, float height, float ratio) {
        if (vg == 0) {
            vg = nvgCreate(NVG_ANTIALIAS);
            if (vg == 0) throw new IllegalStateException("NanoVG context creation failed");
            try (var input = PremiumPaint.class.getResourceAsStream("/assets/loom-studios/font/inter.ttf")) {
                if (input == null) throw new IOException("Missing bundled UI font");
                byte[] bytes = input.readAllBytes();
                fontBytes = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
                if (nvgCreateFontMem(vg, "inter", fontBytes, false) < 0) throw new IOException("Font registration failed");
            } catch (IOException error) { close(); throw new IllegalStateException(error); }
        }
        started = System.nanoTime(); paths = 0;
        nvgBeginFrame(vg, width, height, ratio);
        nvgFontFace(vg, "inter");
        nvgTextAlign(vg, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
    }
    public static void transform(Matrix3x2f m) { nvgTransform(vg,m.m00(),m.m01(),m.m10(),m.m11(),m.m20(),m.m21()); }
    public static void end() {
        nvgEndFrame(vg);
        if(warmup++<10)return;
        times.addLast(System.nanoTime() - started);
        if (times.size() > 240) times.removeFirst();
    }
    public static String metrics() {
        if (times.isEmpty()) return "Warming up";
        long[] sorted = times.stream().mapToLong(Long::longValue).toArray();
        Arrays.sort(sorted);
        return String.format(java.util.Locale.ROOT, "CPU submit p50 %.2f / p95 %.2f ms · %d paths",
                sorted[sorted.length/2]/1e6, sorted[Math.min(sorted.length-1,(int)(sorted.length*.95))]/1e6, paths);
    }
    public static void resetMetrics() {times.clear();warmup=0;}
    public static void close() {
        if (vg != 0) { nvgDelete(vg); vg = 0; }
        if (fontBytes != null) { MemoryUtil.memFree(fontBytes); fontBytes = null; }
        times.clear();
        icons.clear();
    }
    public static void icon(String name,float x,float y,float size,int tint) {
        int image=icons.computeIfAbsent(name,PremiumPaint::loadIcon);
        try(var stack=MemoryStack.stackPush()) {
            var paint=org.lwjgl.nanovg.NVGPaint.malloc(stack);
            nvgImagePattern(vg,x,y,size,size,0,image,1,paint);
            paint.innerColor().set(color(stack,tint));paint.outerColor().set(color(stack,tint));
            nvgBeginPath(vg);nvgRect(vg,x,y,size,size);nvgFillPaint(vg,paint);nvgFill(vg);paths++;
        }
    }
    private static int loadIcon(String name) {
        try(var input=PremiumPaint.class.getResourceAsStream("/assets/loom-studios/icons/"+name+".svg")) {
            if(input==null)throw new IOException("Missing icon "+name);
            String svg=new String(input.readAllBytes(),StandardCharsets.UTF_8).replace("currentColor","#e9f2fa");
            var parsed=nsvgParse(svg,"px",96);
            if(parsed==null)throw new IOException("Invalid SVG "+name);
            long raster=nsvgCreateRasterizer();
            if(raster==0){nsvgDelete(parsed);throw new IOException("Rasterizer creation failed");}
            var pixels=MemoryUtil.memAlloc(96*96*4);
            try {
                nsvgRasterize(raster,parsed,0,0,96/parsed.width(),pixels,96,96,96*4);
                int id=nvgCreateImageRGBA(vg,96,96,0,pixels);
                if(id==0)throw new IOException("Icon upload failed");
                return id;
            } finally {MemoryUtil.memFree(pixels);nsvgDeleteRasterizer(raster);nsvgDelete(parsed);}
        } catch(IOException error){throw new IllegalStateException(error);}
    }
    public static void box(float x,float y,float w,float h,float radius,int color) {
        try(var stack=MemoryStack.stackPush()) {
            nvgBeginPath(vg); nvgRoundedRect(vg,x,y,w,h,radius);
            nvgFillColor(vg,color(stack,color)); nvgFill(vg); paths++;
        }
    }
    public static void outline(float x,float y,float w,float h,float radius,int color) {
        try(var stack=MemoryStack.stackPush()) {
            nvgBeginPath(vg); nvgRoundedRect(vg,x+.5f,y+.5f,w-1,h-1,radius);
            nvgStrokeWidth(vg,1); nvgStrokeColor(vg,color(stack,color)); nvgStroke(vg); paths++;
        }
    }
    public static void text(String text,float x,float y,float size,int color) {
        try(var stack=MemoryStack.stackPush()) {
            nvgFontSize(vg,size); nvgFillColor(vg,color(stack,color)); nvgText(vg,x,y,text);
        }
    }
    public static void line(float x,float y,float x2,float y2,float weight,int color) {
        try(var stack=MemoryStack.stackPush()) {
            nvgBeginPath(vg);nvgMoveTo(vg,x,y);nvgLineTo(vg,x2,y2);nvgStrokeWidth(vg,weight);
            nvgLineCap(vg,NVG_ROUND);nvgStrokeColor(vg,color(stack,color));nvgStroke(vg);paths++;
        }
    }
    public static void circle(float x,float y,float radius,int color) {
        try(var stack=MemoryStack.stackPush()) {
            nvgBeginPath(vg);nvgCircle(vg,x,y,radius);nvgStrokeWidth(vg,1.6f);
            nvgStrokeColor(vg,color(stack,color));nvgStroke(vg);paths++;
        }
    }
    public static void clip(float x,float y,float w,float h,Runnable body) {
        nvgSave(vg); nvgIntersectScissor(vg,x,y,w,h);
        try { body.run(); } finally { nvgRestore(vg); }
    }
    private static NVGColor color(MemoryStack stack,int argb) {
        return NVGColor.malloc(stack).r((argb>>16&255)/255f).g((argb>>8&255)/255f)
                .b((argb&255)/255f).a((argb>>>24)/255f);
    }
}
