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

        graphics.fill(getX()+2,getY()+2,getRight()-2,getY()+3,primary || hot ? 0xAA44BED7 : 0xFF354358);
        if (primary) {
            graphics.fill(
                    getX() + 1,
                    getY() + 1,
                    getX() + 3,
                    getBottom() - 1,
                    LoomUiTheme.ACCENT
            );
        }

        boolean template = switch(icon) { case BLANK,GRADIENT,NATURE,SPACE,FANTASY,EMBLEM -> true; default -> false; };
        if (template && getHeight() >= 50) {
            int ch = Math.min(64,getHeight()-28), cw = ch*10/16;
            int cx = getX()+(getWidth()-cw)/2, cy = getY()+6;
            graphics.fill(cx-1,cy-1,cx+cw+1,cy+ch+1,LoomUiTheme.BORDER);
            for (int row=0;row<ch;row++) graphics.fill(cx,cy+row,cx+cw,cy+row+1,
                    icon == Icon.BLANK ? 0xFFCBD5E0 : LoomWorkshopArt.mix(LoomUiTheme.ACCENT,LoomUiTheme.ACCENT_ALT,row/(float)ch));
            if(!active) graphics.fill(cx,cy,cx+cw,cy+ch,0x99303B4D);
            String label = Minecraft.getInstance().font.plainSubstrByWidth(getMessage().getString(),getWidth()-8);
            graphics.drawCenteredString(Minecraft.getInstance().font,Component.literal(label),getX()+getWidth()/2,getBottom()-14,
                    active ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED);
            return;
        }

        boolean compact = getHeight() < 34 || getWidth() < 145;
        int iconSize = compact ? 13 : 18;
        int iconX = getX() + (compact ? 7 : 10);
        int iconY = getY() + (getHeight() - iconSize) / 2;
        drawIcon(graphics, iconX, iconY, icon, active, iconSize);

        int textX = getX() + (compact ? 26 : 38);
        int arrowReserve = 14;
        int available = Math.max(
                8,
                getRight() - arrowReserve - textX
        );

        String title = Minecraft.getInstance().font.plainSubstrByWidth(
                getMessage().getString(),
                available
        );

        int titleY = compact
                ? getY() + (getHeight() - 8) / 2
                : getY() + Math.max(5, getHeight() / 2 - 10);

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(title),
                textX,
                titleY,
                active ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                false
        );

        if (!compact && getHeight() >= 30) {
            String subtitleText = Minecraft.getInstance()
                    .font
                    .plainSubstrByWidth(
                            subtitle.getString(),
                            available
                    );
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(subtitleText),
                    textX,
                    titleY + 11,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        int arrowX = getRight() - 8;
        int arrowY = getY() + getHeight() / 2;
        int arrowColor = hot ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT_MUTED;
        graphics.fill(arrowX - 3, arrowY - 3, arrowX - 2, arrowY + 4, arrowColor);
        graphics.fill(arrowX - 2, arrowY - 2, arrowX - 1, arrowY + 3, arrowColor);
        graphics.fill(arrowX - 1, arrowY - 1, arrowX, arrowY + 2, arrowColor);
    }

    private static void drawIcon(
            GuiGraphics graphics,
            int x,
            int y,
            Icon icon,
            boolean enabled,
            int size
    ) {
        int main = enabled ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED;
        LoomButton.Icon mapped = switch (icon) {
            case CAPE, BLANK -> LoomButton.Icon.CAPE;
            case ELYTRA -> LoomButton.Icon.ELYTRA;
            case IMAGE -> LoomButton.Icon.IMAGE;
            case CODE -> LoomButton.Icon.SHARE;
            case SETTINGS -> LoomButton.Icon.SETTINGS;
            case GRADIENT -> LoomButton.Icon.GRADIENT;
            case FOLDER -> LoomButton.Icon.SAVE;
            case NATURE, SPACE, FANTASY, EMBLEM -> LoomButton.Icon.GRADIENT;
        };
        LoomButton.drawIcon(graphics, x, y, size, mapped, main);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
