package dev.loomstudios.client.ui.premium;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.loomstudios.client.mixin.GuiGraphicsAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix3x2f;
import org.lwjgl.opengl.GL33C;

/**
 * GUI PIP integration adapted from NVGRenderer f4e8272 (Unlicense).
 * One canvas submission per screen, no timer-based stale-frame reuse.
 */
public final class PremiumGuiRenderer extends PictureInPictureRenderer<PremiumGuiRenderer.State> {
    private long paintedRevision=Long.MIN_VALUE;
    private static long paints;
    public static long paints() {return paints;}
    public PremiumGuiRenderer(MultiBufferSource.BufferSource buffer) { super(buffer); }
    public static void register() {
        SpecialGuiElementRegistry.register(context -> new PremiumGuiRenderer(context.vertexConsumers()));
    }
    public static void submit(GuiGraphics graphics,long revision, Runnable paint) {
        int w = graphics.guiWidth(), h = graphics.guiHeight();
        var matrix = new Matrix3x2f(graphics.pose());
        var bounds = new ScreenRectangle(0, 0, w, h).transformMaxBounds(matrix);
        ((GuiGraphicsAccessor) graphics).loom$getGuiRenderState().submitPicturesInPictureState(
                new State(w, h, matrix, bounds, revision,paint));
    }
    @Override protected boolean textureIsReadyToBlit(State state) { return state.revision()==paintedRevision; }
    @Override protected float getTranslateY(int height, int scale) { return height / 2f; }
    @Override public Class<State> getRenderStateClass() { return State.class; }
    @Override protected String getTextureLabel() { return "loom-premium-ui"; }
    @Override protected void renderToTexture(State state, PoseStack pose) {
        var color = RenderSystem.outputColorTextureOverride;
        var depth = RenderSystem.outputDepthTextureOverride;
        if (color == null || depth == null || !(RenderSystem.getDevice() instanceof GlDevice device)) return;
        if (!(color.texture() instanceof GlTexture texture) || !(depth.texture() instanceof GlTexture depthTexture)) return;
        int width = color.getWidth(0), height = color.getHeight(0);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, texture.getFbo(device.directStateAccess(), depthTexture));
        GlStateManager._viewport(0, 0, width, height);
        GL33C.glBindSampler(0, 0);
        float ratio = Minecraft.getInstance().getWindow().getGuiScale() > 0
                ? (float) Minecraft.getInstance().getWindow().getGuiScale() : 1;
        PremiumPaint.begin(width / ratio, height / ratio, ratio);
        try {
            PremiumPaint.transform(state.matrix());
            state.paint().run();
        } finally {
            PremiumPaint.end();
            paintedRevision=state.revision();paints++;
            GlStateManager._disableDepthTest();
            GlStateManager._disableCull();
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(770, 771, 1, 0);
        }
    }
    public record State(int width, int height, Matrix3x2f matrix, ScreenRectangle bounds,
                        long revision,Runnable paint) implements PictureInPictureRenderState {
        @Override public int x0() { return 0; }
        @Override public int y0() { return 0; }
        @Override public int x1() { return width; }
        @Override public int y1() { return height; }
        @Override public ScreenRectangle scissorArea() { return null; }
        @Override public float scale() { return 1f; }
    }
}
