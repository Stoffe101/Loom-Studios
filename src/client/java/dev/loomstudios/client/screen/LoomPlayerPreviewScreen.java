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
    private float yaw = 25.0F;
    private float pitch = 0.0F;
    private float zoom = 1.0F;
    private boolean draggingPreview;

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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.min(360, this.width - 32);
        int panelHeight = Math.min(300, this.height - 32);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left, top, right, bottom, PANEL_COLOR);
        graphics.fill(left + 2, top + 2, right - 2, bottom - 2, INNER_COLOR);
        graphics.fill(left, top, right, top + 2, ACCENT_COLOR);

        graphics.drawCenteredString(
                this.font,
                TITLE,
                this.width / 2,
                top + 12,
                TEXT_COLOR
        );

        Component modeText = Component.literal(
                "Mode: " + (this.mode == PreviewMode.CAPE ? "Cape" : "Elytra")
        );
        graphics.drawCenteredString(
                this.font,
                modeText,
                this.width / 2,
                top + 28,
                ACCENT_COLOR
        );

        int previewLeft = left + 24;
        int previewTop = top + 46;
        int previewRight = right - 24;
        int previewBottom = bottom - 42;

        graphics.fill(
                previewLeft,
                previewTop,
                previewRight,
                previewBottom,
                0x8A02050A
        );

        if (this.minecraft.player != null) {
            renderPreviewEntity(
                    graphics,
                    previewLeft,
                    previewTop,
                    previewRight,
                    previewBottom,
                    Math.max(20.0F, (previewBottom - previewTop) * 0.42F * this.zoom),
                    this.minecraft.player
            );
        }

        graphics.drawCenteredString(
                this.font,
                Component.literal("Drag: rotate   Mouse wheel: zoom   C: Cape/Elytra   R: reset   Esc: close"),
                this.width / 2,
                bottom - 25,
                MUTED_TEXT_COLOR
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal("Preview state is render-only. Your real equipment is not changed."),
                this.width / 2,
                bottom - 13,
                MUTED_TEXT_COLOR
        );

        super.render(graphics, mouseX, mouseY, partialTick);
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

        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(
                this.pitch * ((float)Math.PI / 180.0F)
        );
        rotation.mul(xRotation);

        Vector3f translation = new Vector3f(
                0.0F,
                renderState.boundingBoxHeight / 2.0F + 0.0625F,
                0.0F
        );

        graphics.submitEntityRenderState(
                renderState,
                size,
                translation,
                rotation,
                xRotation,
                x0,
                y0,
                x1,
                y1
        );
    }

    private EntityRenderState extractRenderState(LivingEntity entity) {
        return PlayerCosmeticRenderer.withPreviewProject(
                this.minecraft,
                previewProjectSupplier.get(),
                () -> {
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
                }
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isInsidePreview(event.x(), event.y())) {
            this.draggingPreview = true;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.draggingPreview) {
            this.draggingPreview = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
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
            return true;
        }

        if (event.key() == 82) { // GLFW_KEY_R
            this.yaw = 25.0F;
            this.pitch = 0.0F;
            this.zoom = 1.0F;
            return true;
        }

        return super.keyPressed(event);
    }

    private boolean isInsidePreview(double mouseX, double mouseY) {
        int panelWidth = Math.min(360, this.width - 32);
        int panelHeight = Math.min(300, this.height - 32);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        int previewLeft = left + 24;
        int previewTop = top + 46;
        int previewRight = left + panelWidth - 24;
        int previewBottom = top + panelHeight - 42;

        return mouseX >= previewLeft
                && mouseX < previewRight
                && mouseY >= previewTop
                && mouseY < previewBottom;
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
