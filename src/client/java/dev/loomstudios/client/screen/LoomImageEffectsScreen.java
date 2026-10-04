package dev.loomstudios.client.screen;

import dev.loomstudios.client.palette.ColorPaletteLibrary;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.image.*;
import dev.loomstudios.palette.ColorPalette;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;

/** Non-destructive local processing with a before/after transparency preview. */
public final class LoomImageEffectsScreen extends LoomPointerScreen {
    private final Screen parent;
    private final PixelImage source;
    private final Consumer<ImageProcessingSettings> apply;
    private ImageProcessingSettings settings;
    private LoomImagePreviewWidget before, after;
    private long revision;
    private String message =
            "Click the original image to pick a background color · Apply keeps the original source";
    private boolean backgroundTab;
    private PixelImage cached;
    private long cachedRevision = -1;

    public LoomImageEffectsScreen(
            Screen parent,
            PixelImage source,
            ImageProcessingSettings settings,
            Consumer<ImageProcessingSettings> apply) {
        super(Component.literal("Image adjustments"));
        this.parent = parent;
        this.source = source;
        this.settings = settings;
        this.apply = apply;
    }

    private PixelImage processed() {
        if (cachedRevision != revision) {
            cached = ImageProcessingPipeline.apply(source, settings);
            cachedRevision = revision;
        }
        return cached;
    }

    private void touch() {
        revision++;
    }

    @Override
    protected void init() {
        if (before != null) before.close();
        if (after != null) after.close();
        int top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8,
                right = width - 204,
                leftWidth = right - 16,
                previewW = (leftWidth - 6) / 2;
        button(8, top, 64, "Back", this::onClose);
        button(
                        76,
                        top,
                        130,
                        "Apply adjustments",
                        () -> {
                            apply.accept(settings);
                            minecraft.setScreen(parent);
                        })
                .setPrimary(true);
        before =
                addRenderableWidget(
                        new LoomImagePreviewWidget(
                                8,
                                top + 28,
                                previewW,
                                height - top - 58,
                                Component.literal("Original · pick background"),
                                () -> source,
                                () -> 0));
        before.setPixelAction(
                (x, y) -> {
                    int color = source.pixelAt(x, y);
                    var bg = settings.background();
                    settings =
                            settings.withBackground(
                                    new BackgroundRemoval(
                                            true, color, bg.tolerance(), bg.contiguous(), x, y));
                    touch();
                    message =
                            "Background #"
                                    + String.format("%06X", color & 0xFFFFFF)
                                    + " · checkerboard shows removed pixels";
                });
        after =
                addRenderableWidget(
                        new LoomImagePreviewWidget(
                                14 + previewW,
                                top + 28,
                                leftWidth - previewW - 6,
                                height - top - 58,
                                Component.literal("Transparency preview"),
                                this::processed,
                                () -> revision));
        int x = right + 4, y = top;
        button(
                x,
                y,
                92,
                "Adjust",
                () -> {
                    backgroundTab = false;
                    rebuildWidgets();
                });
        button(
                x + 98,
                y,
                94,
                "Remove BG",
                () -> {
                    backgroundTab = true;
                    rebuildWidgets();
                });
        y += 28;
        if (backgroundTab) {
            button(
                    x,
                    y,
                    192,
                    settings.background().enabled() ? "Removal: On" : "Removal: Off",
                    () -> {
                        var b = settings.background();
                        settings =
                                settings.withBackground(
                                        new BackgroundRemoval(
                                                !b.enabled(),
                                                b.color(),
                                                b.tolerance(),
                                                b.contiguous(),
                                                b.seedX(),
                                                b.seedY()));
                        touch();
                        rebuildWidgets();
                    });
            y += 26;
            button(
                    x,
                    y,
                    192,
                    settings.background().contiguous()
                            ? "Connected from picked pixel"
                            : "All matching background",
                    () -> {
                        var b = settings.background();
                        settings =
                                settings.withBackground(
                                        new BackgroundRemoval(
                                                b.enabled(),
                                                b.color(),
                                                b.tolerance(),
                                                !b.contiguous(),
                                                b.seedX(),
                                                b.seedY()));
                        touch();
                        rebuildWidgets();
                    });
            y += 26;
            addRenderableWidget(
                    new LoomSlider(
                            x,
                            y,
                            192,
                            "Color tolerance",
                            () -> settings.background().tolerance() / 255.0,
                            v -> {
                                var b = settings.background();
                                settings =
                                        settings.withBackground(
                                                new BackgroundRemoval(
                                                        b.enabled(),
                                                        b.color(),
                                                        (int) Math.round(v * 255),
                                                        b.contiguous(),
                                                        b.seedX(),
                                                        b.seedY()));
                                touch();
                            }));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    "Reset background removal",
                    () -> {
                        settings = settings.withBackground(BackgroundRemoval.none());
                        touch();
                        rebuildWidgets();
                    });
        } else {
            addRenderableWidget(
                    new LoomSlider(
                                    x,
                                    y,
                                    192,
                                    "Brightness",
                                    () -> (settings.brightness() + 1) / 2.0,
                                    v -> {
                                        settings =
                                                settings.withAdjustments(
                                                        (float) (v * 2 - 1),
                                                        settings.contrast(),
                                                        settings.saturation());
                                        touch();
                                    })
                            .format(v -> String.format(Locale.ROOT, "%+.0f%%", (v * 2 - 1) * 100)));
            y += 26;
            addRenderableWidget(
                    new LoomSlider(
                                    x,
                                    y,
                                    192,
                                    "Contrast",
                                    () -> (settings.contrast() + 1) / 2.0,
                                    v -> {
                                        settings =
                                                settings.withAdjustments(
                                                        settings.brightness(),
                                                        (float) (v * 2 - 1),
                                                        settings.saturation());
                                        touch();
                                    })
                            .format(v -> String.format(Locale.ROOT, "%+.0f%%", (v * 2 - 1) * 100)));
            y += 26;
            addRenderableWidget(
                    new LoomSlider(
                                    x,
                                    y,
                                    192,
                                    "Saturation",
                                    () -> (settings.saturation() + 1) / 2.0,
                                    v -> {
                                        settings =
                                                settings.withAdjustments(
                                                        settings.brightness(),
                                                        settings.contrast(),
                                                        (float) (v * 2 - 1));
                                        touch();
                                    })
                            .format(v -> String.format(Locale.ROOT, "%+.0f%%", (v * 2 - 1) * 100)));
            y += 26;
            addRenderableWidget(
                    new LoomSlider(
                            x,
                            y,
                            192,
                            "Tint strength",
                            () -> settings.tintStrength(),
                            v -> {
                                settings = settings.withTint(settings.tintColor(), (float) v);
                                touch();
                            }));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    "Tint color…",
                    () ->
                            minecraft.setScreen(
                                    new LoomRenameScreen(
                                            this,
                                            "Tint color",
                                            "Hex RGB, for example 80DFFF",
                                            "Apply",
                                            String.format("%06X", settings.tintColor() & 0xFFFFFF),
                                            value -> {
                                                try {
                                                    if (!value.replace("#", "")
                                                            .matches("[0-9a-fA-F]{6}"))
                                                        throw new NumberFormatException();
                                                    settings =
                                                            settings.withTint(
                                                                    0xFF000000
                                                                            | Integer.parseInt(
                                                                                    value.replace(
                                                                                            "#",
                                                                                            ""),
                                                                                    16),
                                                                    settings.tintStrength() == 0
                                                                            ? 1
                                                                            : settings
                                                                                    .tintStrength());
                                                    touch();
                                                } catch (NumberFormatException e) {
                                                    message = "Enter six hexadecimal digits";
                                                }
                                            })));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    "Create Swatches from Image",
                    () -> {
                        try {
                            var colors = ImageColorReduction.extractPalette(processed(), 16);
                            if (colors.isEmpty()) {
                                message = "No opaque colors to extract";
                                return;
                            }
                            ColorPaletteLibrary.save(
                                    new ColorPalette(UUID.randomUUID(), "Image colors", colors));
                            message =
                                    "Created and selected Image colors · "
                                            + colors.size()
                                            + " swatches";
                        } catch (java.io.IOException | IllegalArgumentException e) {
                            message = e.getMessage();
                        }
                    });
        }
    }

    private LoomButton button(int x, int y, int w, String label, Runnable action) {
        var b = addRenderableWidget(new LoomButton(x, y, w, 22, Component.literal(label), action));
        b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(label)));
        return b;
    }

    @Override
    public void render(GuiGraphics g, int x, int y, float dt) {
        LoomScreenChrome.renderBackdrop(g, width, height);
        LoomScreenChrome.renderBrandHeader(
                g,
                width,
                "Image adjustments · local & reversible",
                LoomUiTheme.compact(width, height));
        super.render(g, x, y, dt);
        LoomScreenChrome.footer(
                g, width, height, font.plainSubstrByWidth(message, width - 80), "Preview");
    }

    @Override
    public void removed() {
        if (before != null) before.close();
        if (after != null) after.close();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
