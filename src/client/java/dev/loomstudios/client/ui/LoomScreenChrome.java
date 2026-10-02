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

    public static void workSurface(GuiGraphics g, int l, int t, int r, int b) {
        g.fill(l, t, r, b, 0xFF111E30);
        for (int x = l; x < r; x += 12) g.fill(x, t, x + 1, b, 0x182E4968);
        for (int y = t; y < b; y += 12) g.fill(l, y, r, y + 1, 0x182E4968);
    }

    public static void renderEditorHeader(GuiGraphics g, int width, String title, boolean compact) {
        int h = compact ? 22 : 26;
        g.fill(0, 0, width, h, LoomUiTheme.PANEL_HEADER);
        // Slim timber bezel and steel end caps; workshop identity without stealing workspace.
        g.fill(0, 0, width, 3, LoomUiTheme.FRAME_WOOD);
        for (int x = 8; x < width; x += 48) g.fill(x, 1, Math.min(x + 25, width), 2, LoomUiTheme.FRAME_WOOD_LIGHT);
        g.fill(0, 3, 4, h, LoomUiTheme.FRAME_METAL);
        g.fill(width - 4, 3, width, h, LoomUiTheme.FRAME_METAL);
        LoomButton.drawIcon(g, 12, 7, 10, LoomButton.Icon.CAPE, LoomUiTheme.ACCENT);
        var font = Minecraft.getInstance().font;
        g.drawString(font, Component.literal("Loom Studios"), 28, 8, LoomUiTheme.ACCENT, false);
        g.drawString(font, Component.literal(font.plainSubstrByWidth(title, width - 150)), 126, 8, LoomUiTheme.TEXT_MUTED, false);
        g.fill(8, h - 1, width - 8, h, LoomUiTheme.BORDER_SOFT);
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

        graphics.fill(frame, frame, width - frame, frame + 1, LoomUiTheme.FRAME_WOOD_LIGHT);
        for (int x : new int[]{frame, width - frame - 7}) {
            graphics.fill(x, frame, x + 7, h - 3, LoomUiTheme.FRAME_METAL);
            graphics.fill(x + 2, frame + 3, x + 4, frame + 5, LoomUiTheme.GOLD);
        }
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
