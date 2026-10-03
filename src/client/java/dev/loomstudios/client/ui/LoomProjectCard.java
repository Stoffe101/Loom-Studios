package dev.loomstudios.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ProjectDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * Recent-project card with a lazily loaded cached thumbnail.
 */
public final class LoomProjectCard extends AbstractButton {
    private static final int FOOTER_HEIGHT = 28;

    private final ProjectDescriptor descriptor;
    private final Component ageText;
    private final BooleanSupplier selectedSupplier;
    private final Runnable action;
    private final Identifier textureId;

    private Runnable openAction;
    private java.util.function.BiConsumer<Boolean,Boolean> selectionAction;
    public LoomProjectCard setSelectionAction(java.util.function.BiConsumer<Boolean,Boolean> action){selectionAction=action;return this;}
    private boolean decorated;
    public LoomProjectCard setDecorated(boolean value){decorated=value;return this;}
    private java.util.function.BiConsumer<Double,Double> contextAction;
    public LoomProjectCard setOpenAction(Runnable action) { openAction=action;return this; }
    public LoomProjectCard setContextAction(java.util.function.BiConsumer<Double,Double> action) { contextAction=action;return this; }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick) {
        if(event.button()==1 && isMouseOver(event.x(),event.y()) && contextAction!=null){contextAction.accept(event.x(),event.y());return true;}
        return super.mouseClicked(event,doubleClick);
    }
    @Override public void onClick(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick) {
        if(selectionAction!=null)selectionAction.accept(event.hasControlDown(),event.hasShiftDown());else action.run();if(doubleClick&&openAction!=null)openAction.run();
    }

    private DynamicTexture texture;
    private NativeImage image;
    private boolean textureAttempted;

    public LoomProjectCard(
            int x,
            int y,
            int width,
            int height,
            ProjectDescriptor descriptor,
            Component ageText,
            BooleanSupplier selectedSupplier,
            Runnable action
    ) {
        super(x, y, width, height, Component.literal(descriptor.name()));
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.ageText = Objects.requireNonNull(ageText, "ageText");
        this.selectedSupplier = Objects.requireNonNull(
                selectedSupplier,
                "selectedSupplier"
        );
        this.action = Objects.requireNonNull(action, "action");
        this.textureId = Identifier.fromNamespaceAndPath(
                LoomStudios.MOD_ID,
                "home/project_"
                        + UUID.randomUUID().toString().replace("-", "")
        );
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void renderContents(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        boolean selected = selectedSupplier.getAsBoolean();
        boolean hot = isHoveredOrFocused();
        int border = selected || hot
                ? LoomUiTheme.ACCENT
                : LoomUiTheme.BORDER;

        graphics.fill(getX(), getY(), getRight(), getBottom(), border);
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                LoomUiTheme.PANEL_INNER
        );

        int imageLeft = getX() + 4;
        int imageTop = getY() + 4;
        int imageRight = getRight() - 4;
        int imageBottom = getBottom() - FOOTER_HEIGHT;

        if(decorated){LoomWorkshopArt.previewScene(graphics,imageLeft,imageTop,imageRight,imageBottom);}else renderChecker(graphics, imageLeft, imageTop, imageRight, imageBottom);
        ensureTexture();

        if (texture != null && image != null) {
            int atlasScale = image.getWidth() / 64;
            int sourceWidth = 10 * atlasScale;
            int sourceHeight = 16 * atlasScale;
            int availableWidth = Math.max(1, imageRight - imageLeft);
            int availableHeight = Math.max(1, imageBottom - imageTop);
            double scale = Math.min(
                    availableWidth / (double)sourceWidth,
                    availableHeight / (double)sourceHeight
            );
            int drawWidth = Math.max(1, (int)Math.floor(sourceWidth * scale));
            int drawHeight = Math.max(1, (int)Math.floor(sourceHeight * scale));
            int left = imageLeft + (availableWidth - drawWidth) / 2;
            int top = imageTop + (availableHeight - drawHeight) / 2;
            if(decorated){graphics.fill(left+2,top+2,left+drawWidth+3,top+drawHeight+3,0x66304054);graphics.fill(left-1,top-1,left+drawWidth+1,top+drawHeight+1,0xFF1E3447);}

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    textureId,
                    left,
                    top,
                    (float)atlasScale,
                    (float)atlasScale,
                    drawWidth,
                    drawHeight,
                    sourceWidth,
                    sourceHeight,
                    image.getWidth(),
                    image.getHeight()
            );
        }

        if(decorated){String badge=dev.loomstudios.client.project.ClientProjectWorkspace.isInitialized()&&dev.loomstudios.client.project.ClientProjectWorkspace.project().projectId().equals(descriptor.projectId())&&dev.loomstudios.client.project.ClientProjectWorkspace.isCurrentProjectEquipped()?"Equipped":dev.loomstudios.client.project.LoomPreferences.get().favorite(descriptor.projectId())?"Favorite":"Saved";int bw=Minecraft.getInstance().font.width(badge)+8;graphics.fill(imageLeft+3,imageTop+3,imageLeft+3+bw,imageTop+15,0xDF152639);dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,Minecraft.getInstance().font,badge,imageLeft+7,imageTop+5,LoomUiTheme.ACCENT,false);}

        String title = Minecraft.getInstance().font.plainSubstrByWidth(
                descriptor.name(),
                Math.max(20, getWidth() - 10)
        );
        dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
                Minecraft.getInstance().font,
                Component.literal(title),
                getX() + 5,
                getBottom() - 23,
                selected ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                false
        );
        dev.loomstudios.client.ui.premium.PremiumText.drawString(graphics,
                Minecraft.getInstance().font,
                Component.literal(Minecraft.getInstance().font.plainSubstrByWidth(ageText.getString(),Math.max(20,getWidth()-10))),
                getX() + 5,
                getBottom() - 12,
                LoomUiTheme.TEXT_MUTED,
                false
        );

    }

    private void ensureTexture() {
        if (textureAttempted) {
            return;
        }
        textureAttempted = true;

        if (!Files.isRegularFile(descriptor.thumbnailPath())) {
            return;
        }

        try (InputStream stream = Files.newInputStream(
                descriptor.thumbnailPath()
        )) {
            image = NativeImage.read(stream);
            texture = new DynamicTexture(
                    () -> "Loom Studios project thumbnail",
                    image
            );
            Minecraft.getInstance()
                    .getTextureManager()
                    .register(textureId, texture);
            texture.upload();
        } catch (IOException | IllegalArgumentException e) {
            LoomStudios.LOGGER.warn(
                    "Failed to load Loom project thumbnail {}",
                    descriptor.thumbnailPath(),
                    e
            );
            image = null;
            texture = null;
        }
    }

    private static void renderChecker(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        int size = 6;
        for (int y = top; y < bottom; y += size) {
            for (int x = left; x < right; x += size) {
                boolean alternate =
                        (((x - left) / size) + ((y - top) / size)) % 2 != 0;
                graphics.fill(
                        x,
                        y,
                        Math.min(right, x + size),
                        Math.min(bottom, y + size),
                        alternate ? 0xFF303943 : 0xFF212831
                );
            }
        }
    }

    public void close() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(textureId);
            texture = null;
            image = null;
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
