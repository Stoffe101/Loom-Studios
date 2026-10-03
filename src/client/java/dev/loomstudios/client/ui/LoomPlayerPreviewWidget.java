package dev.loomstudios.client.ui;

import dev.loomstudios.client.render.PlayerCosmeticRenderer;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Reusable in-screen player preview for Loom showcase/editor layouts.
 */
public final class LoomPlayerPreviewWidget extends AbstractWidget implements LoomMiddlePanTarget {
    public enum Mode {
        CAPE,
        ELYTRA
    }

    private final Supplier<LoomProject> projectSupplier;
    private Mode mode;
    private IntSupplier timelineTickSupplier;

    private dev.loomstudios.client.render.LoomPreviewState.PreviewPose pose=dev.loomstudios.client.render.LoomPreviewState.PreviewPose.STANDING;
    private float facing;
    public void setPose(dev.loomstudios.client.render.LoomPreviewState.PreviewPose pose){this.pose=pose;}
    public dev.loomstudios.client.render.LoomPreviewState.PreviewPose previewPose(){return pose;}
    private float yaw = 25.0F;
    private float pitch;
    private float zoom = 1.0F;
    private boolean dragging;
    private boolean panning;
    private int panX,panY;

    public LoomPlayerPreviewWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier
    ) {
        this(x, y, width, height, projectSupplier, Mode.CAPE);
    }

    public LoomPlayerPreviewWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            Mode mode
    ) {
        super(x, y, width, height, Component.literal("3D Preview"));
        this.projectSupplier = Objects.requireNonNull(
                projectSupplier,
                "projectSupplier"
        );
        this.mode = Objects.requireNonNull(mode, "mode");
        setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Checkerboard = transparency (preview only). Double-click or top-right: full preview. Drag: rotate · Middle drag: pan · Wheel: zoom")));
    }

    public record ViewState(float yaw, float pitch, float zoom,int panX,int panY) {
        public ViewState(float yaw,float pitch,float zoom) { this(yaw,pitch,zoom,0,0); }
    }
    public ViewState viewState() { return new ViewState(yaw, pitch, zoom,panX,panY); }
    public void restoreViewState(ViewState state) { if (state != null) { yaw = state.yaw; pitch = state.pitch; zoom = state.zoom; panX=state.panX;panY=state.panY; } }

    public void setTimelineTickSupplier(
            IntSupplier timelineTickSupplier
    ) {
        this.timelineTickSupplier = timelineTickSupplier;
    }

    public void setMode(Mode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        LoomScreenChrome.panel(graphics,getX(),getY(),getRight(),getBottom());
        LoomScreenChrome.panelHeader(graphics,getX(),getY(),getRight(),"3D · "+(mode==Mode.ELYTRA?pose.label():"Cape"));
        // Small, native expand affordance stays inside the preview header.
        int ex=getRight()-18, ey=getY()+4;
        graphics.fill(ex,ey+5,ex+1,ey+11,LoomUiTheme.TEXT_MUTED);
        graphics.fill(ex,ey+10,ex+7,ey+11,LoomUiTheme.TEXT_MUTED);
        graphics.fill(ex+5,ey,ex+11,ey+1,LoomUiTheme.ACCENT);
        graphics.fill(ex+10,ey,ex+11,ey+6,LoomUiTheme.ACCENT);
        for(int i=0;i<7;i++) graphics.fill(ex+4+i,ey+6-i,ex+5+i,ey+7-i,LoomUiTheme.ACCENT);
        int contentLeft = getX() + 4;
        int contentTop = getY() + 19;
        int contentRight = getRight() - 4;
        int contentBottom = getBottom() - (getHeight() > 140 ? 16 : 4);

        LoomWorkshopArt.previewScene(graphics,contentLeft,contentTop,contentRight,contentBottom);

        LoomProject project = projectSupplier.get();
        LivingEntity player = Minecraft.getInstance().player;

        if (project != null && player != null) {
            EntityRenderState renderState = extractRenderState(
                    player,
                    project,
                    timelineTickSupplier == null
                            ? null
                            : timelineTickSupplier.getAsInt()
            );

            if (renderState instanceof AvatarRenderState avatarState) {
                if (mode == Mode.ELYTRA) {
                    avatarState.chestEquipment = new ItemStack(Items.ELYTRA);
                    avatarState.showCape = false;
                } else {
                    avatarState.chestEquipment = ItemStack.EMPTY;
                    avatarState.showCape = true;
                }
            }

            dev.loomstudios.client.render.LoomPreviewState.orient(renderState,yaw);
            dev.loomstudios.client.render.LoomPreviewState.pose(renderState,mode==Mode.ELYTRA?pose:dev.loomstudios.client.render.LoomPreviewState.PreviewPose.STANDING);

            Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI)
                    .rotateY((float)Math.PI + (yaw+facing) * ((float)Math.PI / 180.0F));
            Quaternionf xRotation = new Quaternionf().rotateX(
                    pitch * ((float)Math.PI / 180.0F)
            );
            rotation.mul(xRotation);

            Vector3f translation = new Vector3f(
                    0.0F,
                    renderState.boundingBoxHeight / 2.0F + 0.0625F,
                    0.0F
            );

            graphics.enableScissor(contentLeft,contentTop,contentRight,contentBottom);
            graphics.submitEntityRenderState(
                    renderState,
                    Math.max(
                            18.0F,
                            Math.min((contentBottom - contentTop) * 0.49F, (contentRight - contentLeft) * 0.46F) * zoom
                    ),
                    translation,
                    rotation,
                    xRotation,
                    contentLeft+panX,
                    contentTop+panY,
                    contentRight+panX,
                    contentBottom+panY
            );
            graphics.disableScissor();
        } else {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.literal("Select a saved design"),
                    getX() + getWidth() / 2,
                    getY() + getHeight() / 2,
                    LoomUiTheme.TEXT_MUTED
            );
        }

        if (getHeight() > 140 && dev.loomstudios.client.project.LoomPreferences.get().enabled("shortcuts",true)) graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.literal(Minecraft.getInstance().font.plainSubstrByWidth(
                        getWidth() < 190 ? "Drag · Scroll to zoom" : "Alpha guide · Drag / wheel",getWidth()-12)),
                getX() + getWidth() / 2,
                getBottom() - 12,
                LoomUiTheme.TEXT_MUTED
        );
    }

    private static EntityRenderState extractRenderState(
            LivingEntity entity,
            LoomProject project,
            Integer timelineTick
    ) {
        Supplier<EntityRenderState> action = () -> {
                    EntityRenderDispatcher dispatcher =
                            Minecraft.getInstance().getEntityRenderDispatcher();
                    EntityRenderer<? super LivingEntity, ?> renderer =
                            dispatcher.getRenderer((Entity)entity);

                    EntityRenderState renderState =
                            dev.loomstudios.client.render.LoomPreviewState.extract(entity);
                    renderState.lightCoords = 15728880;
                    renderState.shadowPieces.clear();
                    renderState.outlineColor = 0;
                    return renderState;
                };

        if(!dev.loomstudios.client.project.LoomPreferences.get().enabled("animatePreview",true))timelineTick=0;
        return timelineTick == null
                ? PlayerCosmeticRenderer.withPreviewProject(
                        Minecraft.getInstance(),
                        project,
                        action
                )
                : PlayerCosmeticRenderer.withPreviewProjectAtTick(
                        Minecraft.getInstance(),
                        project,
                        timelineTick,
                        action
                );
    }

    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick) {
        if(event.button()==2 && isMouseOver(event.x(),event.y())) { panning=true;return true; }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) {
        if(event.button()==2 && panning) {
            panX=Mth.clamp(panX+(int)Math.round(dx),-getWidth()/2,getWidth()/2);
            panY=Mth.clamp(panY+(int)Math.round(dy),-getHeight()/2,getHeight()/2);return true;
        }
        return super.mouseDragged(event,dx,dy);
    }
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            if(doubleClick || (event.x()>=getRight()-22 && event.y()<getY()+19)) {
                var preview=new dev.loomstudios.client.screen.LoomPlayerPreviewScreen(
                        Minecraft.getInstance().screen,projectSupplier,mode==Mode.ELYTRA);
                preview.setTimelineTickSupplier(timelineTickSupplier);
                preview.setView(yaw,pitch,zoom,panX,panY,pose,facing);
                Minecraft.getInstance().setScreen(preview); return;
            }
            if(event.y()<getY()+19&&mode==Mode.ELYTRA){pose=pose.next();return;}
            dragging = true;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button()==2 && panning) { panning=false;return true; }
        if (event.button() == 0 && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (event.button() == 0 && dragging) {
            yaw = wrapDegrees(yaw - (float)dx * 1.25F);
            pitch = Mth.clamp(pitch + (float)dy * 0.8F, -35.0F, 35.0F);
        }
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        zoom = Mth.clamp(
                zoom + (float)scrollY * 0.08F,
                0.65F,
                1.65F
        );
        return true;
    }

    public void resetView() {
        panX=0;panY=0;
        yaw = 25.0F;
        pitch = 0.0F;
        zoom = 1.0F;
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        return wrapped < 0.0F ? wrapped + 360.0F : wrapped;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
