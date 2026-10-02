package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Reference-oriented icon action card used by Loom showcase/navigation screens.
 */
public final class LoomActionCard extends AbstractButton {
    public enum Icon {
        CAPE,
        ELYTRA,
        FOLDER,
        IMAGE,
        CODE,
        SETTINGS,
        BLANK,
        GRADIENT,
        NATURE,
        SPACE,
        FANTASY,
        EMBLEM
    }

    private final Component subtitle;
    private final Icon icon;
    private final Runnable action;
    private final boolean primary;

    public LoomActionCard(
            int x,
            int y,
            int width,
            int height,
            Component title,
            Component subtitle,
            Icon icon,
            boolean primary,
            Runnable action
    ) {
        super(x, y, width, height, title);
        this.subtitle = Objects.requireNonNull(subtitle, "subtitle");
        this.icon = Objects.requireNonNull(icon, "icon");
        this.primary = primary;
        this.action = Objects.requireNonNull(action, "action");
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (active) {
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
        boolean hot = this.active && this.isHoveredOrFocused();
        int border = primary || hot
                ? LoomUiTheme.ACCENT
                : LoomUiTheme.BORDER;
        int background = !active
                ? LoomUiTheme.BUTTON_DISABLED
                : hot
                        ? LoomUiTheme.BUTTON_HOVER
                        : LoomUiTheme.BUTTON;

        graphics.fill(getX(), getY(), getRight(), getBottom(), border);
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                background
        );

        if (primary) {
            graphics.fill(
                    getX() + 1,
                    getY() + 1,
                    getX() + 3,
                    getBottom() - 1,
                    LoomUiTheme.ACCENT
            );
        }

        int iconX = getX() + 10;
        int iconY = getY() + Math.max(5, (getHeight() - 18) / 2);
        drawIcon(graphics, iconX, iconY, icon, active);

        int textX = getX() + 38;
        int titleY = getY() + Math.max(5, getHeight() / 2 - 10);
        int subtitleY = titleY + 11;

        graphics.drawString(
                Minecraft.getInstance().font,
                getMessage(),
                textX,
                titleY,
                active ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                false
        );
        graphics.drawString(
                Minecraft.getInstance().font,
                subtitle,
                textX,
                subtitleY,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        int arrowX = getRight() - 10;
        int arrowY = getY() + getHeight() / 2;
        int arrowColor = hot ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT_MUTED;
        graphics.fill(arrowX - 4, arrowY - 3, arrowX - 3, arrowY + 4, arrowColor);
        graphics.fill(arrowX - 3, arrowY - 2, arrowX - 2, arrowY + 3, arrowColor);
        graphics.fill(arrowX - 2, arrowY - 1, arrowX - 1, arrowY + 2, arrowColor);
    }

    private static void drawIcon(
            GuiGraphics graphics,
            int x,
            int y,
            Icon icon,
            boolean enabled
    ) {
        int main = enabled ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED;
        int accent = enabled ? LoomUiTheme.ACCENT : 0xFF4B5961;
        int alt = enabled ? LoomUiTheme.ACCENT_ALT : 0xFF3F4850;

        switch (icon) {
            case CAPE -> {
                graphics.fill(x + 6, y, x + 12, y + 3, main);
                graphics.fill(x + 4, y + 3, x + 14, y + 14, accent);
                graphics.fill(x + 6, y + 14, x + 12, y + 18, alt);
            }
            case ELYTRA -> {
                graphics.fill(x + 8, y + 2, x + 10, y + 17, main);
                graphics.fill(x, y + 3, x + 8, y + 13, accent);
                graphics.fill(x + 10, y + 3, x + 18, y + 13, alt);
                graphics.fill(x + 2, y + 13, x + 7, y + 17, accent);
                graphics.fill(x + 11, y + 13, x + 16, y + 17, alt);
            }
            case FOLDER -> {
                graphics.fill(x + 1, y + 5, x + 17, y + 16, main);
                graphics.fill(x + 3, y + 2, x + 9, y + 6, main);
                graphics.fill(x + 2, y + 8, x + 16, y + 15, 0xFFB99655);
            }
            case IMAGE -> {
                graphics.fill(x + 1, y + 1, x + 17, y + 17, main);
                graphics.fill(x + 3, y + 3, x + 15, y + 15, LoomUiTheme.PANEL_INNER);
                graphics.fill(x + 4, y + 11, x + 9, y + 15, accent);
                graphics.fill(x + 8, y + 8, x + 14, y + 15, alt);
                graphics.fill(x + 11, y + 5, x + 13, y + 7, main);
            }
            case CODE -> {
                graphics.fill(x + 1, y + 8, x + 6, y + 10, accent);
                graphics.fill(x + 3, y + 5, x + 5, y + 13, accent);
                graphics.fill(x + 12, y + 8, x + 17, y + 10, alt);
                graphics.fill(x + 13, y + 5, x + 15, y + 13, alt);
                graphics.fill(x + 8, y + 3, x + 10, y + 15, main);
            }
            case SETTINGS -> {
                graphics.fill(x + 7, y + 1, x + 11, y + 17, main);
                graphics.fill(x + 1, y + 7, x + 17, y + 11, main);
                graphics.fill(x + 4, y + 4, x + 14, y + 14, main);
                graphics.fill(x + 7, y + 7, x + 11, y + 11, LoomUiTheme.PANEL_INNER);
            }
            case BLANK -> {
                graphics.fill(x + 4, y + 1, x + 14, y + 17, main);
                graphics.fill(x + 6, y + 3, x + 12, y + 15, 0xFFE2E8EC);
            }
            case GRADIENT -> {
                graphics.fill(x + 2, y + 2, x + 9, y + 16, accent);
                graphics.fill(x + 9, y + 2, x + 16, y + 16, alt);
            }
            case NATURE -> {
                graphics.fill(x + 8, y + 2, x + 10, y + 17, main);
                graphics.fill(x + 2, y + 5, x + 9, y + 11, 0xFF55C878);
                graphics.fill(x + 10, y + 4, x + 16, y + 10, 0xFF55C878);
            }
            case SPACE -> {
                graphics.fill(x + 7, y + 4, x + 13, y + 10, accent);
                graphics.fill(x + 9, y + 2, x + 11, y + 12, accent);
                graphics.fill(x + 2, y + 12, x + 4, y + 14, main);
                graphics.fill(x + 14, y + 3, x + 16, y + 5, alt);
            }
            case FANTASY -> {
                graphics.fill(x + 8, y + 1, x + 10, y + 17, alt);
                graphics.fill(x + 2, y + 8, x + 16, y + 10, alt);
                graphics.fill(x + 5, y + 5, x + 13, y + 13, accent);
            }
            case EMBLEM -> {
                graphics.fill(x + 4, y + 2, x + 14, y + 15, main);
                graphics.fill(x + 6, y + 4, x + 12, y + 12, accent);
                graphics.fill(x + 8, y + 6, x + 10, y + 16, alt);
            }
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
