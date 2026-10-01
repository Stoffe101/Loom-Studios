package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public final class LoomButton extends AbstractButton {
    private final Runnable action;

    public LoomButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Runnable action
    ) {
        super(x, y, width, height, message);
        this.action = action;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (this.active) {
            action.run();
        }
    }

    @Override
    protected void renderContents(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int background = !this.active
                ? LoomUiTheme.BUTTON_DISABLED
                : (this.isHoveredOrFocused()
                        ? LoomUiTheme.BUTTON_HOVER
                        : LoomUiTheme.BUTTON);

        int border = this.isHoveredOrFocused() && this.active
                ? LoomUiTheme.ACCENT
                : LoomUiTheme.BORDER;

        graphics.fill(
                getX(),
                getY(),
                getRight(),
                getBottom(),
                border
        );
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                background
        );

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                this.active ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED
        );
    }
}
