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
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
import java.util.function.Supplier;

/**
 * Reusable in-screen player preview for Loom showcase/editor layouts.
 */
public final class LoomPlayerPreviewWidget extends AbstractWidget {
    public enum Mode {
        CAPE,
        ELYTRA
    }

    public enum ElytraPose {
        STANDING,
        OPEN,
        GLIDING
    }

    private static final float ELYTRA_STANDING_X = 0.2617994F;
    private static final float ELYTRA_STANDING_Z = -0.2617994F;
    private static final float ELYTRA_OPEN_X = 0.34906584F;
    private static final float ELYTRA_OPEN_Z = -(float)Math.PI / 2.0F;

    private final Supplier<LoomProject> projectSupplier;
    private Mode mode;
    private ElytraPose elytraPose = ElytraPose.STANDING;

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
    }

    public void setMode(Mode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public void setElytraPose(ElytraPose pose) {
        this.elytraPose = Objects.requireNonNull(pose, "pose");
    }

    public ElytraPose elytraPose() {
        return elytraPose;
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

        int contentLeft = getX() + 6;
        int contentTop = getY() + 22;
        int contentRight = getRight() - 6;
        int contentBottom = getBottom() - 18;

        graphics.fill(
                contentLeft,
                contentTop,
                contentRight,
                contentBottom,
                0xFF080E15
        );

        LoomProject project = projectSupplier.get();
        LivingEntity player = Minecraft.getInstance().player;

        if (project != null && player != null) {
            EntityRenderState renderState = extractRenderState(
                    player,
                    project
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

            if (mode == Mode.ELYTRA
                    && renderState instanceof HumanoidRenderState humanoidState) {
                applyElytraPose(humanoidState);
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
                            (contentBottom - contentTop) * 0.42F * zoom
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

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.literal("Drag to rotate  •  Wheel to zoom"),
                getX() + getWidth() / 2,
                getBottom() - 12,
                LoomUiTheme.TEXT_MUTED
        );
    }

    private void applyElytraPose(HumanoidRenderState state) {
        state.isCrouching = false;
        state.elytraRotY = 0.0F;

        switch (elytraPose) {
            case STANDING -> {
                state.pose = Pose.STANDING;
                state.isFallFlying = false;
                state.elytraRotX = ELYTRA_STANDING_X;
                state.elytraRotZ = ELYTRA_STANDING_Z;
            }
            case OPEN -> {
                // Editor-only inspection pose: upright body, fully spread wings.
                state.pose = Pose.STANDING;
                state.isFallFlying = false;
                state.elytraRotX = ELYTRA_OPEN_X;
                state.elytraRotZ = ELYTRA_OPEN_Z;
            }
            case GLIDING -> {
                state.pose = Pose.FALL_FLYING;
                state.isFallFlying = true;
                state.elytraRotX = ELYTRA_OPEN_X;
                state.elytraRotZ = ELYTRA_OPEN_Z;
            }
        }
    }

    private static EntityRenderState extractRenderState(
            LivingEntity entity,
            LoomProject project
    ) {
        return PlayerCosmeticRenderer.withPreviewProject(
                Minecraft.getInstance(),
                project,
                () -> {
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
                }
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
