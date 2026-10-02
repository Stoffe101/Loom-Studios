package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Shared visual chrome for Loom Studios screens.
 */
public final class LoomScreenChrome {
    private LoomScreenChrome() {
    }

    public static void renderBackdrop(
            GuiGraphics graphics,
            int width,
            int height
    ) {
        graphics.fill(0, 0, width, height, LoomUiTheme.BACKDROP);
    }

    public static int headerHeight(boolean compact) {
        return compact ? 34 : 48;
    }

    public static int navHeight(boolean compact) {
        return compact ? 22 : 26;
    }

    public static void renderBrandHeader(
            GuiGraphics graphics,
            int width,
            String subtitle,
            boolean compact
    ) {
        int h = headerHeight(compact);
        int frame = compact ? 3 : 5;

        graphics.fill(
                0,
                0,
                width,
                h,
                LoomUiTheme.FRAME_WOOD
        );
        graphics.fill(
                frame,
                frame,
                width - frame,
                h - 3,
                LoomUiTheme.PANEL_INNER
        );
        graphics.fill(
                frame,
                h - 3,
                width - frame,
                h - 1,
                LoomUiTheme.ACCENT
        );

        int center = width / 2;
        int titleY = compact ? 7 : 9;
        String title = "Loom Studios";
        int titleWidth = Minecraft.getInstance().font.width(title);

        LoomButton.drawIcon(
                graphics,
                center - titleWidth / 2 - 18,
                compact ? 5 : 7,
                compact ? 11 : 14,
                LoomButton.Icon.CAPE,
                LoomUiTheme.ACCENT
        );

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.literal(title),
                center,
                titleY,
                LoomUiTheme.TEXT
        );

        if (!compact && subtitle != null && !subtitle.isBlank()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.literal(subtitle),
                    center,
                    25,
                    LoomUiTheme.TEXT_MUTED
            );
        } else if (compact && subtitle != null && !subtitle.isBlank()) {
            String clipped = Minecraft.getInstance().font.plainSubstrByWidth(
                    subtitle,
                    Math.max(40, width / 2)
            );
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.literal(clipped),
                    center,
                    19,
                    LoomUiTheme.TEXT_MUTED
            );
        }
    }

    public static void panel(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        graphics.fill(
                left,
                top,
                right,
                bottom,
                LoomUiTheme.BORDER
        );
        graphics.fill(
                left + 1,
                top + 1,
                right - 1,
                bottom - 1,
                LoomUiTheme.PANEL_INNER
        );
    }

    public static void panelHeader(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            String title
    ) {
        graphics.fill(
                left + 1,
                top + 1,
                right - 1,
                top + 19,
                LoomUiTheme.PANEL_HEADER
        );
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(title),
                left + 7,
                top + 6,
                LoomUiTheme.TEXT,
                false
        );
    }

    public static void footer(
            GuiGraphics graphics,
            int width,
            int height,
            String status,
            String rightText
    ) {
        int top = height - 18;
        graphics.fill(
                0,
                top,
                width,
                height,
                LoomUiTheme.PANEL_INNER
        );
        graphics.fill(
                0,
                top,
                width,
                top + 1,
                LoomUiTheme.BORDER_SOFT
        );

        if (status != null) {
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(status),
                    8,
                    top + 5,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }

        if (rightText != null) {
            int textWidth = Minecraft.getInstance().font.width(rightText);
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(rightText),
                    Math.max(8, width - textWidth - 8),
                    top + 5,
                    LoomUiTheme.ACCENT_ALT,
                    false
            );
        }
    }
}
