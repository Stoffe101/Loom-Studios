package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public final class LoomPaletteButton extends AbstractButton {
    private final Runnable action;

    public LoomPaletteButton(
            int x,
            int y,
            int width,
            int height,
            Runnable action
    ) {
        super(x, y, width, height, Component.literal("Palettes"));
        this.action = action;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (active) {
            action.run();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    protected void renderContents(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int background = !active
                ? LoomUiTheme.BUTTON_DISABLED
                : (isHoveredOrFocused()
                        ? LoomUiTheme.BUTTON_HOVER
                        : LoomUiTheme.BUTTON);
        int border = isHoveredOrFocused() && active
                ? LoomUiTheme.ACCENT
                : LoomUiTheme.BORDER;

        graphics.fill(getX(), getY(), getRight(), getBottom(), border);
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                background
        );

        int iconX = getX() + 8;
        int iconY = getY() + (getHeight() - 11) / 2;

        graphics.fill(iconX, iconY, iconX + 5, iconY + 5, 0xFF8E5CFF);
        graphics.fill(iconX + 6, iconY, iconX + 11, iconY + 5, 0xFF3DB7E8);
        graphics.fill(iconX, iconY + 6, iconX + 5, iconY + 11, 0xFFFFBE2E);
        graphics.fill(iconX + 6, iconY + 6, iconX + 11, iconY + 11, 0xFFFF5368);

        graphics.drawString(
                Minecraft.getInstance().font,
                getMessage(),
                iconX + 18,
                getY() + (getHeight() - 8) / 2,
                active ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                false
        );
    }
}
