package dev.loomstudios.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.function.IntConsumer;

public final class LoomColorPickerWidget extends AbstractWidget {
    private static final int[] PALETTE = {
            0xFFFFFFFF, 0xFF9AA7B3, 0xFF45515D, 0xFF8E5CFF,
            0xFFCC4CFF, 0xFF2864E8, 0xFF3DB7E8, 0xFF33E3D0,
            0xFF62E56A, 0xFF1E8D39, 0xFFFFBE2E, 0xFFFF7D27,
            0xFFFF5368, 0xFFC91539
    };

    private final IntConsumer onChanged;

    private int color;
    private float hue;
    private float saturation;
    private float brightness;
    private DragTarget dragTarget = DragTarget.NONE;

    public LoomColorPickerWidget(
            int x,
            int y,
            int width,
            int height,
            int initialColor,
            IntConsumer onChanged
    ) {
        super(x, y, width, height, Component.literal("Colors"));
        this.onChanged = onChanged;
        setColor(initialColor, false);
    }

    public int color() {
        return color;
    }

    public void setColor(int argb) {
        setColor(argb, false);
    }

    private void setColor(int argb, boolean notify) {
        this.color = argb;

        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];

        if (notify) {
            onChanged.accept(this.color);
        }
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
                LoomUiTheme.PANEL
        );

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Colors"),
                getX() + 8,
                getY() + 7,
                LoomUiTheme.TEXT,
                false
        );

        int svX = getX() + 8;
        int svY = getY() + 22;
        int svW = Math.max(64, Math.min(96, getWidth() / 2 - 18));
        int svH = Math.min(64, Math.max(32, getHeight() - 47));

        int hueX = svX + svW + 5;
        int hueW = 9;

        float displayedHue=hue;
        LoomUiTextureCache.draw(graphics,"sv/"+Float.floatToIntBits(displayedHue)+"/"+svW+"/"+svH,
                svX,svY,svW,svH,()-> {
                    var image=new com.mojang.blaze3d.platform.NativeImage(svW,svH,false);
                    for(int y=0;y<svH;y++)for(int x=0;x<svW;x++)
                        image.setPixel(x,y,0xFF000000|Color.HSBtoRGB(displayedHue,x/(float)Math.max(1,svW-1),1-y/(float)Math.max(1,svH-1)));
                    return image;
                });
        LoomUiTextureCache.draw(graphics,"hue/"+svH,hueX,svY,hueW,svH,()-> {
            var image=new com.mojang.blaze3d.platform.NativeImage(1,svH,false);
            for(int y=0;y<svH;y++)image.setPixel(0,y,0xFF000000|Color.HSBtoRGB(y/(float)Math.max(1,svH-1),1,1));
            return image;
        });

        int markerX = svX + Math.round(saturation * (svW - 1));
        int markerY = svY + Math.round((1.0F - brightness) * (svH - 1));
        graphics.fill(markerX - 2, markerY - 2, markerX + 3, markerY - 1, 0xFFFFFFFF);
        graphics.fill(markerX - 2, markerY + 2, markerX + 3, markerY + 3, 0xFFFFFFFF);
        graphics.fill(markerX - 2, markerY - 2, markerX - 1, markerY + 3, 0xFFFFFFFF);
        graphics.fill(markerX + 2, markerY - 2, markerX + 3, markerY + 3, 0xFFFFFFFF);

        int hueY = svY + Math.round(hue * (svH - 1));
        graphics.fill(hueX - 2, hueY, hueX + hueW + 2, hueY + 2, 0xFFFFFFFF);

        boolean compact = getHeight() < 135 || getWidth() < 270;
        int rightX = hueX + hueW + 8;
        int rightW = Math.max(42, getRight() - rightX - 8);

        renderChecker(
                graphics,
                rightX,
                svY,
                Math.min(26, rightW),
                18
        );
        graphics.fill(
                rightX,
                svY,
                rightX + Math.min(26, rightW),
                svY + 18,
                color
        );

        if (!compact) {
            graphics.fill(
                    rightX + 28,
                    svY,
                    rightX + rightW,
                    svY + 18,
                    LoomUiTheme.PANEL_INNER
            );
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(
                            String.format("#%06X", color & 0xFFFFFF)
                    ),
                    rightX + 31,
                    svY + 5,
                    LoomUiTheme.TEXT,
                    false
            );

            renderRgbSlider(
                    graphics,
                    rightX,
                    svY + 24,
                    rightW,
                    'R',
                    16
            );
            renderRgbSlider(
                    graphics,
                    rightX,
                    svY + 39,
                    rightW,
                    'G',
                    8
            );
            renderRgbSlider(
                    graphics,
                    rightX,
                    svY + 54,
                    rightW,
                    'B',
                    0
            );
            renderAlphaSlider(
                    graphics,
                    rightX,
                    svY + 69,
                    rightW
            );
        } else {
            String hex = String.format(
                    "#%06X",
                    color & 0xFFFFFF
            );
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(Minecraft.getInstance().font.plainSubstrByWidth(hex, rightW)),
                    rightX,
                    svY + 24,
                    LoomUiTheme.TEXT,
                    false
            );
        }

        int paletteY = svY + svH + (compact ? 6 : 24);
        int swatch = 16;
        int gap = 3;
        int columns = Math.max(1, Math.min(7, (getWidth() - 16) / (swatch + gap)));

        for (int i = 0; i < PALETTE.length; i++) {
            int row = i / columns;
            int col = i % columns;
            int x = getX() + 8 + col * (swatch + gap);
            int y = paletteY + row * (swatch + gap);
            if (y + swatch + 1 >= getBottom()) break;

            graphics.fill(x - 1, y - 1, x + swatch + 1, y + swatch + 1, LoomUiTheme.BORDER);
            graphics.fill(x, y, x + swatch, y + swatch, PALETTE[i]);
        }
    }

    private void renderRgbSlider(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            char label,
            int shift
    ) {
        int value = (color >>> shift) & 0xFF;
        int labelWidth = 12;
        int valueWidth = 24;
        int barX = x + labelWidth;
        int barW = Math.max(20, width - labelWidth - valueWidth - 2);

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(String.valueOf(label)),
                x,
                y + 3,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        for (int i = 0; i < barW; i++) {
            int channel = Math.round(i / (float)Math.max(1, barW - 1) * 255.0F);
            int sample = switch (shift) {
                case 16 -> (color & 0xFF00FFFF) | (channel << 16);
                case 8 -> (color & 0xFFFF00FF) | (channel << 8);
                default -> (color & 0xFFFFFF00) | channel;
            };
            graphics.fill(barX + i, y + 2, barX + i + 1, y + 11, sample | 0xFF000000);
        }

        int marker = barX + Math.round(value / 255.0F * Math.max(1, barW - 1));
        graphics.fill(marker - 1, y, marker + 1, y + 13, 0xFFFFFFFF);

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(Integer.toString(value)),
                barX + barW + 4,
                y + 3,
                LoomUiTheme.TEXT,
                false
        );
    }

    private void renderAlphaSlider(
            GuiGraphics graphics,
            int x,
            int y,
            int width
    ) {
        int alpha = (color >>> 24) & 0xFF;
        int labelWidth = 12;
        int valueWidth = 24;
        int barX = x + labelWidth;
        int barW = Math.max(20, width - labelWidth - valueWidth - 2);

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("A"),
                x,
                y + 3,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        for (int i = 0; i < barW; i++) {
            int a = Math.round(i / (float)Math.max(1, barW - 1) * 255.0F);
            int checker = ((i / 4) & 1) == 0 ? 0xFF38424A : 0xFF252C32;
            graphics.fill(barX + i, y + 2, barX + i + 1, y + 11, checker);
            graphics.fill(
                    barX + i,
                    y + 2,
                    barX + i + 1,
                    y + 11,
                    (a << 24) | (color & 0x00FFFFFF)
            );
        }

        int marker = barX + Math.round(alpha / 255.0F * Math.max(1, barW - 1));
        graphics.fill(marker - 1, y, marker + 1, y + 13, 0xFFFFFFFF);

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(Integer.toString(alpha)),
                barX + barW + 4,
                y + 3,
                LoomUiTheme.TEXT,
                false
        );
    }

    private static void renderChecker(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {
        int cell = 4;
        for (int py = 0; py < height; py += cell) {
            for (int px = 0; px < width; px += cell) {
                int checker = (((px / cell) + (py / cell)) & 1) == 0
                        ? 0xFF3B444C
                        : 0xFF252C32;
                graphics.fill(
                        x + px,
                        y + py,
                        Math.min(x + width, x + px + cell),
                        Math.min(y + height, y + py + cell),
                        checker
                );
            }
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        updateFromMouse(event.x(), event.y(), true);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        updateFromMouse(event.x(), event.y(), false);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        dragTarget = DragTarget.NONE;
    }

    private void updateFromMouse(double mouseX, double mouseY, boolean chooseTarget) {
        int svX = getX() + 8;
        int svY = getY() + 22;
        int svW = Math.max(64, Math.min(96, getWidth() / 2 - 18));
        int svH = Math.min(64, Math.max(32, getHeight() - 47));
        int hueX = svX + svW + 5;
        int hueW = 9;
        boolean compact = getHeight() < 135 || getWidth() < 270;
        int rightX = hueX + hueW + 8;
        int rightW = Math.max(42, getRight() - rightX - 8);
        int paletteY = svY + svH + (compact ? 6 : 24);

        if (chooseTarget) {
            if (inside(mouseX, mouseY, svX, svY, svW, svH)) {
                dragTarget = DragTarget.SV;
            } else if (inside(mouseX, mouseY, hueX, svY, hueW, svH)) {
                dragTarget = DragTarget.HUE;
            } else if (!compact
                    && inside(
                            mouseX,
                            mouseY,
                            rightX + 12,
                            svY + 24,
                            rightW - 38,
                            13
                    )) {
                dragTarget = DragTarget.RED;
            } else if (!compact
                    && inside(
                            mouseX,
                            mouseY,
                            rightX + 12,
                            svY + 39,
                            rightW - 38,
                            13
                    )) {
                dragTarget = DragTarget.GREEN;
            } else if (!compact
                    && inside(
                            mouseX,
                            mouseY,
                            rightX + 12,
                            svY + 54,
                            rightW - 38,
                            13
                    )) {
                dragTarget = DragTarget.BLUE;
            } else if (!compact
                    && inside(
                            mouseX,
                            mouseY,
                            rightX + 12,
                            svY + 69,
                            rightW - 38,
                            13
                    )) {
                dragTarget = DragTarget.ALPHA;
            } else if (selectPalette(
                    mouseX,
                    mouseY,
                    paletteY
            )) {
                return;
            } else {
                dragTarget = DragTarget.NONE;
            }
        }

        switch (dragTarget) {
            case SV -> {
                saturation = clamp01((float)((mouseX - svX) / Math.max(1.0, svW - 1.0)));
                brightness = 1.0F - clamp01(
                        (float)((mouseY - svY) / Math.max(1.0, svH - 1.0))
                );
                updateFromHsb();
            }
            case HUE -> {
                hue = clamp01((float)((mouseY - svY) / Math.max(1.0, svH - 1.0)));
                updateFromHsb();
            }
            case RED -> setChannel(16, channelFromMouse(mouseX, rightX, rightW));
            case GREEN -> setChannel(8, channelFromMouse(mouseX, rightX, rightW));
            case BLUE -> setChannel(0, channelFromMouse(mouseX, rightX, rightW));
            case ALPHA -> setAlpha(channelFromMouse(mouseX, rightX, rightW));
            case NONE -> {
            }
        }
    }

    private int channelFromMouse(double mouseX, int rightX, int rightW) {
        int barX = rightX + 12;
        int barW = Math.max(20, rightW - 38);
        float ratio = clamp01((float)((mouseX - barX) / Math.max(1.0, barW - 1.0)));
        return Math.round(ratio * 255.0F);
    }

    private void setChannel(int shift, int value) {
        int alpha = color & 0xFF000000;
        int rgb = color & 0x00FFFFFF;
        rgb &= ~(0xFF << shift);
        rgb |= value << shift;
        setColor(alpha | rgb, true);
    }

    private void setAlpha(int alpha) {
        setColor((alpha << 24) | (color & 0x00FFFFFF), true);
    }

    private void updateFromHsb() {
        int alpha = color & 0xFF000000;
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        this.color = alpha | (rgb & 0x00FFFFFF);
        onChanged.accept(color);
    }

    private boolean selectPalette(double mouseX, double mouseY, int paletteY) {
        int swatch = 16;
        int gap = 3;
        int columns = Math.max(1, Math.min(7, (getWidth() - 16) / (swatch + gap)));

        for (int i = 0; i < PALETTE.length; i++) {
            int row = i / columns;
            int col = i % columns;
            int x = getX() + 8 + col * (swatch + gap);
            int y = paletteY + row * (swatch + gap);
            if (y + swatch + 1 >= getBottom()) break;

            if (inside(mouseX, mouseY, x, y, swatch, swatch)) {
                setColor(PALETTE[i], true);
                dragTarget = DragTarget.NONE;
                return true;
            }
        }

        return false;
    }

    private static boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseY >= y
                && mouseX < x + width
                && mouseY < y + height;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    private enum DragTarget {
        NONE,
        SV,
        HUE,
        RED,
        GREEN,
        BLUE,
        ALPHA
    }
}
