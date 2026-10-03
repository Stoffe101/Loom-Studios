package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class LoomButton extends AbstractButton {
    public enum Icon {
        NONE,
        HOME,
        CAPE,
        ELYTRA,
        PENCIL,
        ERASER,
        FILL,
        EYEDROPPER,
        SELECT,
        LINE,
        RECTANGLE,
        CIRCLE,
        CLOSE,
        ROTATE_LEFT,
        ROTATE_RIGHT,
        MOVE,
        FLIP_H,
        FLIP_V,
        UNDO,
        REDO,
        ZOOM_IN,
        ZOOM_OUT,
        PALETTE,
        LAYERS,
        PLUS,
        COPY,
        DELETE,
        UP,
        DOWN,
        MINUS,
        LEFT,
        RIGHT,
        GRID,
        IMAGE,
        GRADIENT,
        LOCK,
        EMISSIVE,
        SAVE,
        EQUIP,
        SHARE,
        EXPORT,
        SETTINGS,
        PLAY,
        PAUSE,
        LOOP,
        BACK,
        RESET
    }

    private final Runnable action;
    private Icon icon;
    private boolean iconOnly;
    private boolean selected;
    private boolean danger;
    private boolean primary;
    public LoomButton setPrimary(boolean primary) { this.primary = primary; return this; }

    public LoomButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Runnable action
    ) {
        this(
                x,
                y,
                width,
                height,
                message,
                Icon.NONE,
                false,
                action
        );
    }

    public LoomButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            Icon icon,
            boolean iconOnly,
            Runnable action
    ) {
        super(x, y, width, height, message);
        this.action = action;
        this.icon = icon == null ? Icon.NONE : icon;
        this.iconOnly = iconOnly;
        setTooltip(Tooltip.create(message));
    }

    public LoomButton setIcon(Icon icon) {
        this.icon = icon == null ? Icon.NONE : icon;
        return this;
    }

    public LoomButton setIconOnly(boolean iconOnly) {
        this.iconOnly = iconOnly;
        return this;
    }

    public LoomButton setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public boolean selected() {
        return selected;
    }

    public LoomButton setDanger(boolean danger) {
        this.danger = danger;
        return this;
    }

    @Override public void setMessage(Component message) {
        super.setMessage(message);
        setTooltip(Tooltip.create(message));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (this.active) {
            action.run();
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    @Override
    protected void renderContents(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        boolean effectiveIconOnly = iconOnly || (icon != Icon.NONE
                && Minecraft.getInstance().font.width(getMessage()) + 28 > getWidth());
        boolean hot = this.isHoveredOrFocused() && this.active;
        int background = !this.active
                ? LoomUiTheme.BUTTON_DISABLED
                : selected || primary
                        ? LoomUiTheme.BUTTON_SELECTED
                        : hot
                                ? LoomUiTheme.BUTTON_HOVER
                                : LoomUiTheme.BUTTON;

        int border = danger && this.active
                ? (hot ? 0xFFFF7180 : LoomUiTheme.DANGER)
                : (selected || primary || hot)
                        ? LoomUiTheme.ACCENT
                        : LoomUiTheme.BORDER_SOFT;

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

        int color = this.active
                ? LoomUiTheme.TEXT
                : LoomUiTheme.TEXT_MUTED;

        if (icon != Icon.NONE) {
            int iconSize = Math.max(
                    10,
                    Math.min(16, getHeight() - 2)
            );
            int iconX;
            if (effectiveIconOnly) {
                iconX = getX() + (getWidth() - iconSize) / 2;
            } else {
                iconX = getX() + 6;
            }
            int iconY = getY() + (getHeight() - iconSize) / 2;
            drawIcon(graphics, iconX, iconY, iconSize, icon, color);
        }

        if (!effectiveIconOnly) {
            int textLeft = icon == Icon.NONE
                    ? getX() + 4
                    : getX() + 23;
            int maxWidth = Math.max(4, getRight() - textLeft - 4);
            String clipped = Minecraft.getInstance()
                    .font
                    .plainSubstrByWidth(
                            getMessage().getString(),
                            maxWidth
                    );

            int textWidth = Minecraft.getInstance().font.width(clipped);
            int center = icon == Icon.NONE
                    ? getX() + getWidth() / 2
                    : textLeft + maxWidth / 2;
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(clipped),
                    center - textWidth / 2,
                    getY() + (getHeight() - 8) / 2,
                    color,
                    false
            );
        }

    }

    public static void drawIcon(
            GuiGraphics graphics,
            int x,
            int y,
            int size,
            Icon icon,
            int color
    ) {
        if (LoomIconSet.draw(graphics, x, y, size, icon, color)) return;
        int s = Math.max(8, size);
        int accent = color == LoomUiTheme.TEXT_MUTED
                ? LoomUiTheme.TEXT_MUTED
                : LoomUiTheme.ACCENT;
        int alt = color == LoomUiTheme.TEXT_MUTED
                ? 0xFF5E6C75
                : LoomUiTheme.ACCENT_ALT;

        switch (icon) {
            case HOME -> {
                graphics.fill(x + s / 2 - 1, y, x + s / 2 + 2, y + 3, color);
                graphics.fill(x + 2, y + 3, x + s - 2, y + 6, color);
                graphics.fill(x + 3, y + 6, x + s - 3, y + s - 1, color);
                graphics.fill(x + s / 2 - 1, y + s - 5, x + s / 2 + 2, y + s - 1, LoomUiTheme.PANEL_INNER);
            }
            case CAPE -> {
                graphics.fill(x + 4, y + 1, x + s - 4, y + 3, color);
                graphics.fill(x + 2, y + 3, x + s - 2, y + s - 3, accent);
                graphics.fill(x + 4, y + s - 3, x + s - 4, y + s, alt);
            }
            case ELYTRA -> {
                graphics.fill(x + s / 2 - 1, y + 1, x + s / 2 + 1, y + s - 1, color);
                graphics.fill(x, y + 3, x + s / 2 - 1, y + s - 3, accent);
                graphics.fill(x + s / 2 + 1, y + 3, x + s, y + s - 3, alt);
            }
            case PENCIL -> {
                for (int i = 1; i < s - 2; i++) {
                    graphics.fill(x + i, y + s - 1 - i, x + i + 2, y + s + 1 - i, color);
                }
                graphics.fill(x + s - 3, y, x + s, y + 3, accent);
            }
            case ERASER -> {
                graphics.fill(x + 2, y + 3, x + s - 1, y + s - 2, color);
                graphics.fill(x + 1, y + s - 5, x + s - 5, y + s - 1, alt);
            }
            case FILL -> {
                graphics.fill(x + 2, y + 3, x + s - 4, y + s - 4, color);
                graphics.fill(x + 4, y + 1, x + s - 2, y + 4, color);
                graphics.fill(x + s - 4, y + s - 4, x + s - 1, y + s - 1, accent);
            }
            case EYEDROPPER -> {
                graphics.fill(x + s - 5, y + 1, x + s - 1, y + 5, color);
                for (int i = 3; i < s - 2; i++) {
                    graphics.fill(x + i - 2, y + s - i, x + i, y + s - i + 2, color);
                }
                graphics.fill(x, y + s - 3, x + 3, y + s, accent);
            }
            case SELECT -> {
                graphics.fill(x + 1, y + 1, x + s - 1, y + 2, color);
                graphics.fill(x + 1, y + s - 2, x + s - 1, y + s - 1, color);
                graphics.fill(x + 1, y + 1, x + 2, y + s - 1, color);
                graphics.fill(x + s - 2, y + 1, x + s - 1, y + s - 1, color);
            }
            case LINE -> {
                for (int i = 1; i < s - 1; i++) {
                    graphics.fill(x + i, y + s - i, x + i + 1, y + s - i + 1, color);
                }
            }
            case RECTANGLE -> {
                graphics.fill(x + 1, y + 2, x + s - 1, y + 3, color);
                graphics.fill(x + 1, y + s - 3, x + s - 1, y + s - 2, color);
                graphics.fill(x + 1, y + 2, x + 2, y + s - 2, color);
                graphics.fill(x + s - 2, y + 2, x + s - 1, y + s - 2, color);
            }
            case MOVE -> {
                graphics.fill(x + s / 2, y, x + s / 2 + 1, y + s, color);
                graphics.fill(x, y + s / 2, x + s, y + s / 2 + 1, color);
                graphics.fill(x + s / 2 - 2, y, x + s / 2 + 3, y + 3, color);
                graphics.fill(x + s / 2 - 2, y + s - 3, x + s / 2 + 3, y + s, color);
                graphics.fill(x, y + s / 2 - 2, x + 3, y + s / 2 + 3, color);
                graphics.fill(x + s - 3, y + s / 2 - 2, x + s, y + s / 2 + 3, color);
            }
            case FLIP_H -> {
                graphics.fill(x + s / 2, y + 1, x + s / 2 + 1, y + s - 1, LoomUiTheme.TEXT_MUTED);
                graphics.fill(x + 1, y + 3, x + s / 2 - 2, y + s - 3, color);
                graphics.fill(x + s / 2 + 3, y + 3, x + s - 1, y + s - 3, color);
            }
            case FLIP_V -> {
                graphics.fill(x + 1, y + s / 2, x + s - 1, y + s / 2 + 1, LoomUiTheme.TEXT_MUTED);
                graphics.fill(x + 3, y + 1, x + s - 3, y + s / 2 - 2, color);
                graphics.fill(x + 3, y + s / 2 + 3, x + s - 3, y + s - 1, color);
            }
            case UNDO, REDO -> {
                boolean redo = icon == Icon.REDO;
                int left = redo ? x + s / 2 : x + 1;
                int right = redo ? x + s - 1 : x + s / 2;
                graphics.fill(left, y + 3, right, y + 5, color);
                graphics.fill(redo ? x + s - 4 : x + 1, y + 1, redo ? x + s - 1 : x + 4, y + 8, color);
                graphics.fill(x + 3, y + 5, x + s - 3, y + s - 2, color);
            }
            case ZOOM_IN, ZOOM_OUT -> {
                graphics.fill(x + 1, y + 1, x + s - 4, y + s - 4, color);
                graphics.fill(x + 3, y + 3, x + s - 6, y + s - 6, LoomUiTheme.PANEL_INNER);
                graphics.fill(x + s - 5, y + s - 5, x + s, y + s, color);
                int cy = y + (s - 4) / 2;
                graphics.fill(x + 4, cy, x + s - 8, cy + 1, color);
                if (icon == Icon.ZOOM_IN) {
                    int cx = x + (s - 4) / 2;
                    graphics.fill(cx, y + 4, cx + 1, y + s - 8, color);
                }
            }
            case PALETTE -> {
                graphics.fill(x, y, x + s / 2 - 1, y + s / 2 - 1, accent);
                graphics.fill(x + s / 2 + 1, y, x + s, y + s / 2 - 1, alt);
                graphics.fill(x, y + s / 2 + 1, x + s / 2 - 1, y + s, LoomUiTheme.GOLD);
                graphics.fill(x + s / 2 + 1, y + s / 2 + 1, x + s, y + s, LoomUiTheme.DANGER);
            }
            case LAYERS -> {
                graphics.fill(x + 2, y + 1, x + s - 2, y + 4, color);
                graphics.fill(x + 1, y + 5, x + s - 1, y + 8, accent);
                graphics.fill(x + 3, y + 9, x + s - 3, y + 12, alt);
            }
            case PLUS -> {
                graphics.fill(x + s / 2, y + 2, x + s / 2 + 2, y + s - 2, color);
                graphics.fill(x + 2, y + s / 2, x + s - 2, y + s / 2 + 2, color);
            }
            case COPY -> {
                graphics.fill(x + 1, y + 1, x + s - 4, y + s - 4, color);
                graphics.fill(x + 4, y + 4, x + s - 1, y + s - 1, accent);
                graphics.fill(x + 5, y + 5, x + s - 2, y + s - 2, LoomUiTheme.BUTTON);
            }
            case DELETE -> {
                graphics.fill(x + 2, y + 3, x + s - 2, y + s - 1, LoomUiTheme.DANGER);
                graphics.fill(x + 1, y + 1, x + s - 1, y + 3, color);
            }
            case UP, DOWN -> {
                boolean down = icon == Icon.DOWN;
                for (int i = 0; i < s / 2; i++) {
                    int yy = down ? y + s - 3 - i : y + 2 + i;
                    graphics.fill(x + s / 2 - i, yy, x + s / 2 + i + 1, yy + 1, color);
                }
            }
            case IMAGE -> {
                graphics.fill(x + 1, y + 1, x + s - 1, y + s - 1, color);
                graphics.fill(x + 3, y + 3, x + s - 3, y + s - 3, LoomUiTheme.PANEL_INNER);
                graphics.fill(x + 4, y + s - 6, x + s - 4, y + s - 3, accent);
                graphics.fill(x + s - 6, y + 4, x + s - 4, y + 6, color);
            }
            case GRADIENT -> {
                graphics.fill(x + 1, y + 1, x + s / 2, y + s - 1, accent);
                graphics.fill(x + s / 2, y + 1, x + s - 1, y + s - 1, alt);
            }
            case LOCK -> {
                graphics.fill(x + 3, y + 6, x + s - 3, y + s - 1, color);
                graphics.fill(x + 5, y + 2, x + s - 5, y + 7, color);
                graphics.fill(x + 6, y + 3, x + s - 6, y + 6, LoomUiTheme.PANEL_INNER);
            }
            case EMISSIVE -> {
                graphics.fill(x + s / 2 - 1, y, x + s / 2 + 2, y + s, LoomUiTheme.GOLD);
                graphics.fill(x, y + s / 2 - 1, x + s, y + s / 2 + 2, LoomUiTheme.GOLD);
                graphics.fill(x + 3, y + 3, x + s - 3, y + s - 3, accent);
            }
            case SAVE -> {
                graphics.fill(x + 1, y + 1, x + s - 1, y + s - 1, color);
                graphics.fill(x + 3, y + 2, x + s - 4, y + 5, LoomUiTheme.PANEL_INNER);
                graphics.fill(x + 4, y + s - 5, x + s - 4, y + s - 2, accent);
            }
            case EQUIP -> {
                graphics.fill(x + 2, y + 1, x + s - 2, y + s - 2, accent);
                graphics.fill(x + 4, y + 3, x + s - 4, y + s - 4, LoomUiTheme.PANEL_INNER);
                graphics.fill(x + s / 2 - 1, y + 4, x + s / 2 + 2, y + s - 5, alt);
            }
            case SHARE -> {
                graphics.fill(x + 1, y + s / 2 - 1, x + s - 1, y + s / 2 + 1, color);
                graphics.fill(x + s - 5, y + 2, x + s - 1, y + 6, accent);
                graphics.fill(x + s - 5, y + s - 6, x + s - 1, y + s - 2, alt);
            }
            case EXPORT -> {
                graphics.fill(x + 2, y + 6, x + s - 2, y + s - 1, color);
                graphics.fill(x + s / 2 - 1, y, x + s / 2 + 2, y + 8, accent);
                graphics.fill(x + s / 2 - 4, y + 3, x + s / 2 + 5, y + 5, accent);
            }
            case SETTINGS -> {
                graphics.fill(x + s / 2 - 2, y, x + s / 2 + 3, y + s, color);
                graphics.fill(x, y + s / 2 - 2, x + s, y + s / 2 + 3, color);
                graphics.fill(x + 3, y + 3, x + s - 3, y + s - 3, color);
                graphics.fill(x + 5, y + 5, x + s - 5, y + s - 5, LoomUiTheme.PANEL_INNER);
            }
            case PLAY -> {
                for (int i = 0; i < s - 2; i++) {
                    graphics.fill(x + 3, y + 1 + i, x + 4 + i / 2, y + 2 + i, color);
                }
            }
            case PAUSE -> {
                graphics.fill(x + 3, y + 2, x + 6, y + s - 2, color);
                graphics.fill(x + s - 6, y + 2, x + s - 3, y + s - 2, color);
            }
            case LOOP -> {
                graphics.fill(x + 2, y + 2, x + s - 3, y + 4, color);
                graphics.fill(x + s - 4, y + 2, x + s - 1, y + 7, accent);
                graphics.fill(x + 3, y + s - 4, x + s - 2, y + s - 2, color);
                graphics.fill(x + 1, y + s - 7, x + 4, y + s - 2, alt);
            }
            case BACK -> {
                graphics.fill(x + 1, y + s / 2 - 1, x + s - 1, y + s / 2 + 1, color);
                graphics.fill(x + 1, y + s / 2 - 1, x + 5, y + 3, color);
                graphics.fill(x + 1, y + s / 2, x + 5, y + s - 3, color);
            }
            case RESET -> {
                graphics.fill(x + 2, y + 2, x + s - 2, y + 4, color);
                graphics.fill(x + 2, y + 2, x + 4, y + s - 2, color);
                graphics.fill(x + 3, y + s - 4, x + s - 2, y + s - 2, color);
                graphics.fill(x + 1, y + 1, x + 5, y + 6, accent);
            }
            case MINUS -> graphics.fill(x + 2, y + s / 2 - 1, x + s - 2, y + s / 2 + 1, color);
            case LEFT, RIGHT -> {
                boolean right = icon == Icon.RIGHT;
                for (int i = 0; i < s / 2; i++) {
                    int xx = right ? x + s - 3 - i : x + 2 + i;
                    graphics.fill(xx, y + s / 2 - i, xx + 1, y + s / 2 + i + 1, color);
                }
            }
            case GRID -> {
                for (int i = 1; i < s; i += 4) {
                    graphics.fill(x + i, y + 1, x + i + 1, y + s - 1, color);
                    graphics.fill(x + 1, y + i, x + s - 1, y + i + 1, color);
                }
            }
            case NONE -> {
            }
        }
    }
}
