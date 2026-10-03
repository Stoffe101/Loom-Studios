package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.render.PlayerCosmeticRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import dev.loomstudios.project.LoomProject;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Reusable player-preview foundation for the Loom editor.
 *
 * <p>The final visual treatment will follow the approved reference screens;
 * this class owns the preview interaction/render mechanics.</p>
 */
public final class LoomPlayerPreviewScreen extends Screen {
    private static final Component TITLE = Component.literal("Loom Studios - Player Preview");
    private static final int PANEL_COLOR = 0xE6121720;
    private static final int INNER_COLOR = 0xB8060A10;
    private static final int ACCENT_COLOR = 0xFF20D9E8;
    private static final int TEXT_COLOR = 0xFFEAFBFF;
    private static final int MUTED_TEXT_COLOR = 0xFF9CB2BA;

    private final Screen parent;
    private final Supplier<LoomProject> previewProjectSupplier;

    private PreviewMode mode = PreviewMode.CAPE;
    private dev.loomstudios.client.render.LoomPreviewState.PreviewPose pose=dev.loomstudios.client.render.LoomPreviewState.PreviewPose.STANDING;
    private float facing;
    public void setView(float yaw,float pitch,float zoom,int panX,int panY,dev.loomstudios.client.render.LoomPreviewState.PreviewPose pose,float facing){this.yaw=yaw;this.pitch=pitch;this.zoom=zoom;this.panX=panX;this.panY=panY;this.pose=pose;this.facing=facing;}
    @Override protected void init(){
        int left=(width-panelWidth())/2+8,top=panelTop()+23,total=panelWidth()-16;
        String[] views={"Back","Front","Left","Right"};float[] angles={25,180,90,270};
        for(int i=0;i<4;i++){final float angle=angles[i];addRenderableWidget(new dev.loomstudios.client.ui.LoomButton(left+i*(total/4),top,total/4-3,19,Component.literal(views[i]),()->{yaw=angle;pitch=0;panX=0;panY=0;}));}
        top+=23;
        addRenderableWidget(new dev.loomstudios.client.ui.LoomButton(left,top,total/4-3,19,Component.literal(pose.label()),()->{pose=pose.next();rebuildWidgets();}));
        addRenderableWidget(new dev.loomstudios.client.ui.LoomButton(left+total/4,top,total/4-3,19,Component.literal("Facing "+Math.round(facing)+"°"),()->{facing=(facing+45)%360;rebuildWidgets();}));
        addRenderableWidget(new dev.loomstudios.client.ui.LoomButton(left+2*(total/4),top,total/4-3,19,Component.literal("Zoom "+Math.round(zoom*100)+"%"),()->{zoom=zoom>=1.6F?0.65F:zoom+0.2F;rebuildWidgets();}));
        addRenderableWidget(new dev.loomstudios.client.ui.LoomButton(left+3*(total/4),top,total/4-3,19,Component.literal(mode==PreviewMode.CAPE?"Cape":"Elytra"),()->{mode=mode==PreviewMode.CAPE?PreviewMode.ELYTRA:PreviewMode.CAPE;rebuildWidgets();}));
    }
    private float yaw = 25.0F;
    private float pitch = 0.0F;
    private float zoom = 1.0F;
    private boolean draggingPreview;
    private boolean panningPreview;
    private int panX,panY;
    private java.util.function.IntSupplier timelineTickSupplier;
    public void setTimelineTickSupplier(java.util.function.IntSupplier supplier) { timelineTickSupplier=supplier; }

    public LoomPlayerPreviewScreen() {
        this(null, ClientProjectWorkspace::project);
    }

    public LoomPlayerPreviewScreen(Screen parent) {
        this(parent, ClientProjectWorkspace::project);
    }

    public LoomPlayerPreviewScreen(
            Screen parent,
            Supplier<LoomProject> previewProjectSupplier
    ) {
        this(parent, previewProjectSupplier, false);
    }

    public LoomPlayerPreviewScreen(
            Screen parent,
            Supplier<LoomProject> previewProjectSupplier,
            boolean startInElytraMode
    ) {
        super(TITLE);
        this.parent = parent;
        this.previewProjectSupplier = Objects.requireNonNull(
                previewProjectSupplier,
                "previewProjectSupplier"
        );
        this.mode = startInElytraMode
                ? PreviewMode.ELYTRA
                : PreviewMode.CAPE;
    }

    private int panelWidth() { return Math.min(740,width-32); }
    private int panelTop() { return dev.loomstudios.client.ui.LoomScreenChrome.headerHeight(dev.loomstudios.client.ui.LoomUiTheme.compact(width,height))+8; }
    private int panelBottom() { return height-36; }
    @Override
    public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        var chrome=dev.loomstudios.client.ui.LoomUiTheme.compact(width,height);
        dev.loomstudios.client.ui.LoomScreenChrome.renderBackdrop(graphics,width,height);
        dev.loomstudios.client.ui.LoomScreenChrome.renderBrandHeader(graphics,width,"Player preview",chrome);
        int left=(width-panelWidth())/2,right=left+panelWidth(),top=panelTop(),bottom=panelBottom();
        dev.loomstudios.client.ui.LoomScreenChrome.panel(graphics,left,top,right,bottom);
        dev.loomstudios.client.ui.LoomScreenChrome.panelHeader(graphics,left,top,right,
                (mode==PreviewMode.CAPE?"Cape":"Elytra")+" · Alpha guide");
        int x0=left+8,y0=top+72,x1=right-8,y1=bottom-38;
        dev.loomstudios.client.ui.LoomWorkshopArt.previewScene(graphics,x0,y0,x1,y1);
        if(minecraft.player!=null)renderPreviewEntity(graphics,x0,y0,x1,y1,
                Math.max(20,Math.min((y1-y0)*0.45F,(x1-x0)*0.35F)*zoom),minecraft.player);
        graphics.drawCenteredString(font,Component.literal(font.plainSubstrByWidth(
                "Drag: rotate · Middle: pan · Wheel: zoom · C: mode · R: reset · Esc: close",panelWidth()-16)),width/2,bottom-26,MUTED_TEXT_COLOR);
        graphics.drawCenteredString(font,Component.literal(font.plainSubstrByWidth(
                "Checkerboard = transparency. Exports keep authored pixels.",panelWidth()-16)),width/2,bottom-12,MUTED_TEXT_COLOR);
        dev.loomstudios.client.ui.LoomScreenChrome.footer(graphics,width,height,"3D preview",mode==PreviewMode.CAPE?"Cape":"Elytra");
        super.render(graphics,mouseX,mouseY,partialTick);
    }

    private void renderPreviewEntity(
            GuiGraphics graphics,
            int x0,
            int y0,
            int x1,
            int y1,
            float size,
            LivingEntity entity
    ) {
        EntityRenderState renderState = extractRenderState(entity);

        if (renderState instanceof AvatarRenderState avatarState) {
            if (this.mode == PreviewMode.ELYTRA) {
                avatarState.chestEquipment = new ItemStack(Items.ELYTRA);
                avatarState.showCape = false;
            } else {
                avatarState.chestEquipment = ItemStack.EMPTY;
                avatarState.showCape = true;
            }
        }

        dev.loomstudios.client.render.LoomPreviewState.orient(renderState,this.yaw);
        dev.loomstudios.client.render.LoomPreviewState.pose(renderState,mode==PreviewMode.ELYTRA?pose:dev.loomstudios.client.render.LoomPreviewState.PreviewPose.STANDING);

        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI)
                    .rotateY((float)Math.PI + (this.yaw+facing) * ((float)Math.PI / 180.0F));
        Quaternionf xRotation = new Quaternionf().rotateX(
                this.pitch * ((float)Math.PI / 180.0F)
        );
        rotation.mul(xRotation);

        Vector3f translation = new Vector3f(
                0.0F,
                renderState.boundingBoxHeight / 2.0F + 0.0625F,
                0.0F
        );

        graphics.enableScissor(x0,y0,x1,y1);
        graphics.submitEntityRenderState(
                renderState,
                size,
                translation,
                rotation,
                xRotation,
                x0+panX,
                y0+panY,
                x1+panX,
                y1+panY
        );
        graphics.disableScissor();
    }

    private EntityRenderState extractRenderState(LivingEntity entity) {
        Supplier<EntityRenderState> snapshot=() -> dev.loomstudios.client.render.LoomPreviewState.extract(entity);
        if(!dev.loomstudios.client.project.LoomPreferences.get().enabled("animatePreview",true))return PlayerCosmeticRenderer.withPreviewProjectAtTick(minecraft,previewProjectSupplier.get(),0,snapshot);
        return timelineTickSupplier==null
                ? PlayerCosmeticRenderer.withPreviewProject(minecraft,previewProjectSupplier.get(),snapshot)
                : PlayerCosmeticRenderer.withPreviewProjectAtTick(minecraft,previewProjectSupplier.get(),timelineTickSupplier.getAsInt(),snapshot);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(event.button()==2 && isInsidePreview(event.x(),event.y())) { panningPreview=true;return true; }
        if (event.button() == 0 && isInsidePreview(event.x(), event.y())) {
            this.draggingPreview = true;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if(event.button()==2 && panningPreview) { panningPreview=false;return true; }
        if (event.button() == 0 && this.draggingPreview) {
            this.draggingPreview = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if(event.button()==2 && panningPreview) {
            panX=Mth.clamp(panX+(int)Math.round(dx),-width/2,width/2);
            panY=Mth.clamp(panY+(int)Math.round(dy),-height/2,height/2);return true;
        }
        if (event.button() == 0 && this.draggingPreview) {
            this.yaw = wrapDegrees(this.yaw - (float)dx * 1.25F);
            this.pitch = Mth.clamp(this.pitch + (float)dy * 0.8F, -35.0F, 35.0F);
            return true;
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (isInsidePreview(mouseX, mouseY)) {
            this.zoom = Mth.clamp(
                    this.zoom + (float)scrollY * 0.08F,
                    0.65F,
                    1.65F
            );
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 67) { // GLFW_KEY_C
            this.mode = this.mode == PreviewMode.CAPE
                    ? PreviewMode.ELYTRA
                    : PreviewMode.CAPE;
            rebuildWidgets();return true;
        }

        if (event.key() == 82) { // GLFW_KEY_R
            panX=0;panY=0;
            this.yaw = 25.0F;
            this.pitch = 0.0F;
            this.zoom = 1.0F;facing=0;pose=dev.loomstudios.client.render.LoomPreviewState.PreviewPose.STANDING;
            rebuildWidgets();return true;
        }

        return super.keyPressed(event);
    }

    private boolean isInsidePreview(double x,double y) {
        int left=(width-panelWidth())/2;
        return x>=left+8 && x<left+panelWidth()-8 && y>=panelTop()+72 && y<panelBottom()-38;
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        return wrapped < 0.0F ? wrapped + 360.0F : wrapped;
    }

    @Override
    public void onClose() {
        if (this.parent != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public void removed() {
        PlayerCosmeticRenderer.clearPreviewProject(this.minecraft);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private enum PreviewMode {
        CAPE,
        ELYTRA
    }
}
