package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.image.ImageTransforms;
import dev.loomstudios.image.PixelImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class LoomImagePreviewWidget extends AbstractWidget {
    private static final int HEADER_HEIGHT = 20;
    private static final int MAX_PREVIEW_DIMENSION = 512;

    private final Component title;
    private final Supplier<PixelImage> imageSupplier;
    private final LongSupplier revisionSupplier;
    private final Identifier textureId;

    private DynamicTexture texture;
    private NativeImage nativeImage;
    private long renderedRevision = Long.MIN_VALUE;
    public interface TransformController {
        dev.loomstudios.project.LayerTransform transform();
        void move(double dx,double dy);
        void scale(double delta);
        void rotate(double degrees);
    }
    private TransformController controller;
    private int handleMode;
    private double lastX,lastY;
    private int imageLeft,imageTop,drawWidth,drawHeight;
    public void setTransformController(TransformController controller){this.controller=controller;setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Drag artwork: move · Corner handle: scale · Top handle: rotate")));}
    private int sourceWidth;
    private int sourceHeight;

    public LoomImagePreviewWidget(
            int x,
            int y,
            int width,
            int height,
            Component title,
            Supplier<PixelImage> imageSupplier,
            LongSupplier revisionSupplier
    ) {
        super(x, y, width, height, title);
        this.title = Objects.requireNonNull(title, "title");
        this.imageSupplier = Objects.requireNonNull(
                imageSupplier,
                "imageSupplier"
        );
        this.revisionSupplier = Objects.requireNonNull(
                revisionSupplier,
                "revisionSupplier"
        );
        this.textureId = Identifier.fromNamespaceAndPath(
                LoomStudios.MOD_ID,
                "editor/import_preview_"
                        + UUID.randomUUID().toString().replace("-", "")
        );
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                getX(),
                getY(),
                getRight(),
                getBottom(),
                LoomUiTheme.BORDER
        );
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                LoomUiTheme.PANEL_INNER
        );

        PixelImage current = imageSupplier.get();
        String dimensions = current == null
                ? "No image"
                : current.width() + "×" + current.height();

        dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
                Minecraft.getInstance().font,
                title,
                getX() + 6,
                getY() + 6,
                LoomUiTheme.TEXT,
                false
        );

        int dimWidth = Minecraft.getInstance().font.width(dimensions);
        dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
                Minecraft.getInstance().font,
                Component.literal(dimensions),
                getRight() - dimWidth - 6,
                getY() + 6,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        int contentLeft = getX() + 6;
        int contentTop = getY() + HEADER_HEIGHT + 4;
        int contentRight = getRight() - 6;
        int contentBottom = getBottom() - 6;

        renderChecker(
                graphics,
                contentLeft,
                contentTop,
                contentRight,
                contentBottom
        );

        if (current == null) {
            return;
        }

        ensureTexture(current, revisionSupplier.getAsLong());

        int availableWidth = Math.max(1, contentRight - contentLeft);
        int availableHeight = Math.max(1, contentBottom - contentTop);
        double scale = Math.min(
                availableWidth / (double) sourceWidth,
                availableHeight / (double) sourceHeight
        );

        int drawWidth = Math.max(
                1,
                (int)Math.floor(sourceWidth * scale)
        );
        int drawHeight = Math.max(
                1,
                (int)Math.floor(sourceHeight * scale)
        );
        int left = contentLeft + (availableWidth - drawWidth) / 2;
        int top = contentTop + (availableHeight - drawHeight) / 2;

        imageLeft=left;imageTop=top;this.drawWidth=drawWidth;this.drawHeight=drawHeight;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                left,
                top,
                0.0F,
                0.0F,
                drawWidth,
                drawHeight,
                sourceWidth,
                sourceHeight,
                sourceWidth,
                sourceHeight
        );
        if(controller!=null&&controller.transform()!=null)renderHandles(graphics);
    }
    private double[][] handles(){
        var t=controller.transform();double cx=imageLeft+t.centerX()*drawWidth,cy=imageTop+t.centerY()*drawHeight;
        double hw=t.width()*drawWidth/2,hh=t.height()*drawHeight/2,angle=Math.toRadians(t.rotationDegrees()),c=Math.cos(angle),s=Math.sin(angle);
        double[][] points={{-hw,-hh},{hw,-hh},{hw,hh},{-hw,hh},{0,-hh-10}};
        for(var p:points){double x=p[0],y=p[1];p[0]=cx+x*c-y*s;p[1]=cy+x*s+y*c;}return points;
    }
    private void renderHandles(GuiGraphics g){
        g.enableScissor(getX()+1,getY()+20,getRight()-1,getBottom()-1);
        double[][] p=handles();for(int i=0;i<4;i++){line(g,p[i],p[(i+1)%4]);int x=(int)p[i][0],y=(int)p[i][1];g.fill(x-2,y-2,x+3,y+3,LoomUiTheme.ACCENT);}
        int x=(int)p[4][0],y=(int)p[4][1];g.fill(x-3,y-3,x+4,y+4,LoomUiTheme.ACCENT_ALT);g.disableScissor();
    }
    private void line(GuiGraphics g,double[] a,double[] b){int steps=Math.min(2048,Math.max(1,(int)Math.ceil(Math.max(Math.abs(b[0]-a[0]),Math.abs(b[1]-a[1])))));for(int i=0;i<=steps;i++){int x=(int)Math.round(a[0]+(b[0]-a[0])*i/steps),y=(int)Math.round(a[1]+(b[1]-a[1])*i/steps);g.fill(x,y,x+1,y+1,LoomUiTheme.ACCENT);}}
    @Override public void onClick(net.minecraft.client.input.MouseButtonEvent e,boolean doubleClick){if(e.button()!=0||controller==null||controller.transform()==null||e.y()<getY()+20)return;
        handleMode=1;var p=handles();for(int i=0;i<p.length;i++)if(Math.hypot(e.x()-p[i][0],e.y()-p[i][1])<=7)handleMode=i==4?3:2;lastX=e.x();lastY=e.y();}
    @Override protected void onDrag(net.minecraft.client.input.MouseButtonEvent e,double dx,double dy){if(handleMode==0||controller==null||controller.transform()==null)return;var t=controller.transform();
        double cx=imageLeft+t.centerX()*drawWidth,cy=imageTop+t.centerY()*drawHeight;
        if(handleMode==1)controller.move(dx/Math.max(1,drawWidth),dy/Math.max(1,drawHeight));
        else if(handleMode==2){double old=Math.hypot(lastX-cx,lastY-cy),next=Math.hypot(e.x()-cx,e.y()-cy);if(old>2)controller.scale((next-old)/old);}
        else {double old=Math.atan2(lastY-cy,lastX-cx),next=Math.atan2(e.y()-cy,e.x()-cx);double delta=Math.toDegrees(next-old);if(delta>180)delta-=360;if(delta< -180)delta+=360;controller.rotate(delta);}
        lastX=e.x();lastY=e.y();}
    @Override public void onRelease(net.minecraft.client.input.MouseButtonEvent e){handleMode=0;}


    private void ensureTexture(PixelImage source, long revision) {
        if (texture != null && renderedRevision == revision) {
            return;
        }

        PixelImage preview = fitPreview(source);

        if (texture != null) {
            Minecraft.getInstance()
                    .getTextureManager()
                    .release(textureId);
            texture = null;
            nativeImage = null;
        }

        sourceWidth = preview.width();
        sourceHeight = preview.height();
        nativeImage = new NativeImage(
                NativeImage.Format.RGBA,
                sourceWidth,
                sourceHeight,
                false
        );

        int[] pixels = preview.pixels();
        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                nativeImage.setPixel(
                        x,
                        y,
                        pixels[y * sourceWidth + x]
                );
            }
        }

        texture = new DynamicTexture(
                () -> "Loom Studios Smart Import preview",
                nativeImage
        );
        Minecraft.getInstance()
                .getTextureManager()
                .register(textureId, texture);
        texture.upload();
        renderedRevision = revision;
    }

    private static PixelImage fitPreview(PixelImage source) {
        if (source.width() <= MAX_PREVIEW_DIMENSION
                && source.height() <= MAX_PREVIEW_DIMENSION) {
            return source;
        }

        double scale = Math.min(
                MAX_PREVIEW_DIMENSION / (double)source.width(),
                MAX_PREVIEW_DIMENSION / (double)source.height()
        );

        return ImageTransforms.resizeNearest(
                source,
                Math.max(
                        1,
                        (int)Math.round(source.width() * scale)
                ),
                Math.max(
                        1,
                        (int)Math.round(source.height() * scale)
                )
        );
    }

    private static void renderChecker(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        int size = 8;
        for (int y = top; y < bottom; y += size) {
            for (int x = left; x < right; x += size) {
                boolean alternate =
                        (((x - left) / size) + ((y - top) / size)) % 2 != 0;
                graphics.fill(
                        x,
                        y,
                        Math.min(right, x + size),
                        Math.min(bottom, y + size),
                        alternate ? 0xFF3B4148 : 0xFF262C32
                );
            }
        }
    }

    public void close() {
        if (texture != null) {
            Minecraft.getInstance()
                    .getTextureManager()
                    .release(textureId);
            texture = null;
            nativeImage = null;
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
