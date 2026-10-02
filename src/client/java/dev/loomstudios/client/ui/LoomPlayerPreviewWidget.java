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
public final class LoomPlayerPreviewWidget extends AbstractWidget {
    public enum Mode {
        CAPE,
        ELYTRA
    }

    private final Supplier<LoomProject> projectSupplier;
    private Mode mode;
    private IntSupplier timelineTickSupplier;

    private float yaw = 180.0F;
    private float pitch;
    private float zoom = 1.0F;
    private boolean dragging;

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
        setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Drag to rotate · Wheel to zoom")));
    }

    public record ViewState(float yaw, float pitch, float zoom) { }
    public ViewState viewState() { return new ViewState(yaw, pitch, zoom); }
    public void restoreViewState(ViewState state) { if (state != null) { yaw = state.yaw; pitch = state.pitch; zoom = state.zoom; } }

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
        graphics.fill(getX(), getY(), getRight(), getBottom(), LoomUiTheme.BORDER);
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                LoomUiTheme.PANEL_INNER
        );

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("3D Preview"),
                getX() + 8,
                getY() + 7,
                LoomUiTheme.TEXT,
                false
        );

        int contentLeft = getX() + 4;
        int contentTop = getY() + 19;
        int contentRight = getRight() - 4;
        int contentBottom = getBottom() - (getHeight() > 140 ? 16 : 4);

        graphics.fill(
                contentLeft,
                contentTop,
                contentRight,
                contentBottom,
                0xFF14243A
        );

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

            if (renderState instanceof LivingEntityRenderState livingState) {
                livingState.bodyRot = 180.0F + yaw;
                livingState.yRot = yaw;
                if (livingState.pose != Pose.FALL_FLYING) {
                    livingState.xRot = -pitch;
                } else {
                    livingState.xRot = 0.0F;
                }

                livingState.boundingBoxWidth /= livingState.scale;
                livingState.boundingBoxHeight /= livingState.scale;
                livingState.scale = 1.0F;
            }

            Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
            Quaternionf xRotation = new Quaternionf().rotateX(
                    pitch * ((float)Math.PI / 180.0F)
            );
            rotation.mul(xRotation);

            Vector3f translation = new Vector3f(
                    0.0F,
                    renderState.boundingBoxHeight / 2.0F + 0.0625F,
                    0.0F
            );

            graphics.submitEntityRenderState(
                    renderState,
                    Math.max(
                            18.0F,
                            Math.min((contentBottom - contentTop) * 0.49F, (contentRight - contentLeft) * 0.46F) * zoom
                    ),
                    translation,
                    rotation,
                    xRotation,
                    contentLeft,
                    contentTop,
                    contentRight,
                    contentBottom
            );
        } else {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.literal("Select a saved design"),
                    getX() + getWidth() / 2,
                    getY() + getHeight() / 2,
                    LoomUiTheme.TEXT_MUTED
            );
        }

        if (getHeight() > 140) graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.literal("Drag to rotate  •  Wheel to zoom"),
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
                            renderer.createRenderState(entity, 1.0F);
                    renderState.lightCoords = 15728880;
                    renderState.shadowPieces.clear();
                    renderState.outlineColor = 0;
                    return renderState;
                };

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

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            dragging = true;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
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
        yaw = 180.0F;
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
