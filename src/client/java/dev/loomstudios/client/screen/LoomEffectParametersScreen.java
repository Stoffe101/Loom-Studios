package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.project.*;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.*;

/** Typed parameter inspector and per-key outgoing easing, with a live cosmetic preview. */
public final class LoomEffectParametersScreen extends LoomPointerScreen {
    private final Screen parent;
    private final UUID id;
    private final int tick;
    private LoomPlayerPreviewWidget preview;
    private String message =
            "Changes are live · Easing controls the transition after the selected key · Ctrl+Z"
                    + " undoes";
    private int x, y;

    public LoomEffectParametersScreen(Screen parent, UUID id, int tick) {
        super(Component.literal("Effect parameters"));
        this.parent = parent;
        this.id = id;
        this.tick = tick;
    }

    private AnimationTrack track() {
        return ClientProjectWorkspace.project().animation().tracks().stream()
                .filter(t -> t.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private EffectParameters parameters() {
        return track().parameters();
    }

    private void change(UnaryOperator<EffectParameters> edit) {
        ClientProjectWorkspace.apply(
                p ->
                        p.withAnimation(
                                AnimationAuthoring.replaceTrack(
                                        p.animation(),
                                        track().withParameters(edit.apply(parameters())))));
    }

    @Override
    protected void init() {
        int top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8,
                right = width - 226;
        x = right + 8;
        y = top;
        var state = preview == null ? null : preview.viewState();
        button(8, top, 64, "Back", this::onClose);
        button(
                76,
                top,
                80,
                "Undo",
                () -> {
                    ClientProjectWorkspace.undo();
                    rebuildWidgets();
                });
        preview =
                addRenderableWidget(
                        new LoomPlayerPreviewWidget(
                                8,
                                top + 28,
                                right - 16,
                                height - top - 58,
                                ClientProjectWorkspace::project,
                                track().channel() == AnimationChannel.CAPE
                                        ? LoomPlayerPreviewWidget.Mode.CAPE
                                        : LoomPlayerPreviewWidget.Mode.ELYTRA));
        preview.restoreViewState(state);
        preview.setTimelineTickSupplier(
                () -> (int) (minecraft.level == null ? tick : minecraft.level.getGameTime()));
        button(
                x,
                y,
                210,
                "Reset " + track().effect().displayName(),
                () -> {
                    change(p -> EffectParameters.forAuthoring(track().effect()));
                    rebuildWidgets();
                });
        y += 26;
        switch (parameters()) {
            case EffectParameters.Pulse v -> {
                slider(
                        "Min opacity",
                        0,
                        1,
                        () -> ((EffectParameters.Pulse) parameters()).minOpacity(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Pulse) p;
                                            return new EffectParameters.Pulse(
                                                    Math.min(n, a.maxOpacity()), a.maxOpacity());
                                        }));
                slider(
                        "Max opacity",
                        0,
                        1,
                        () -> ((EffectParameters.Pulse) parameters()).maxOpacity(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Pulse) p;
                                            return new EffectParameters.Pulse(
                                                    a.minOpacity(), Math.max(n, a.minOpacity()));
                                        }));
            }
            case EffectParameters.Scroll v -> {
                slider(
                        "Direction X",
                        -1,
                        1,
                        () -> ((EffectParameters.Scroll) parameters()).directionX(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Scroll) p;
                                            return new EffectParameters.Scroll(
                                                    n, a.directionY(), a.distance());
                                        }));
                slider(
                        "Direction Y",
                        -1,
                        1,
                        () -> ((EffectParameters.Scroll) parameters()).directionY(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Scroll) p;
                                            return new EffectParameters.Scroll(
                                                    a.directionX(), n, a.distance());
                                        }));
                slider(
                        "Distance (canvases)",
                        0,
                        4,
                        () -> ((EffectParameters.Scroll) parameters()).distance(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Scroll) p;
                                            return new EffectParameters.Scroll(
                                                    a.directionX(), a.directionY(), n);
                                        }));
            }
            case EffectParameters.Gradient v -> {
                slider(
                        "Angle (degrees)",
                        -180,
                        180,
                        () -> ((EffectParameters.Gradient) parameters()).angle(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Gradient) p;
                                            return new EffectParameters.Gradient(
                                                    n,
                                                    a.width(),
                                                    a.firstColor(),
                                                    a.secondColor(),
                                                    a.offset(),
                                                    false);
                                        }));
                slider(
                        "Band width",
                        .05f,
                        4,
                        () -> ((EffectParameters.Gradient) parameters()).width(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Gradient) p;
                                            return new EffectParameters.Gradient(
                                                    a.angle(),
                                                    n,
                                                    a.firstColor(),
                                                    a.secondColor(),
                                                    a.offset(),
                                                    false);
                                        }));
                slider(
                        "Offset",
                        -4,
                        4,
                        () -> ((EffectParameters.Gradient) parameters()).offset(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Gradient) p;
                                            return new EffectParameters.Gradient(
                                                    a.angle(),
                                                    a.width(),
                                                    a.firstColor(),
                                                    a.secondColor(),
                                                    n,
                                                    false);
                                        }));
                button(x, y, 102, "Color A…", () -> pickColor(true));
                button(x + 108, y, 102, "Color B…", () -> pickColor(false));
                y += 26;
            }
            case EffectParameters.Sparkle v -> {
                slider(
                        "Density",
                        0,
                        1,
                        () -> ((EffectParameters.Sparkle) parameters()).density(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Sparkle) p;
                                            return new EffectParameters.Sparkle(
                                                    n, a.seed(), a.size(), a.brightness());
                                        }));
                slider(
                        "Size (pixels)",
                        1,
                        8,
                        () -> ((EffectParameters.Sparkle) parameters()).size(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Sparkle) p;
                                            return new EffectParameters.Sparkle(
                                                    a.density(),
                                                    a.seed(),
                                                    Math.round(n),
                                                    a.brightness());
                                        }));
                slider(
                        "Brightness",
                        0,
                        4,
                        () -> ((EffectParameters.Sparkle) parameters()).brightness(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Sparkle) p;
                                            return new EffectParameters.Sparkle(
                                                    a.density(), a.seed(), a.size(), n);
                                        }));
                button(
                        x,
                        y,
                        210,
                        "Seed: " + v.seed() + "…",
                        () ->
                                minecraft.setScreen(
                                        new LoomRenameScreen(
                                                this,
                                                "Sparkle seed",
                                                "Integer seed",
                                                "Apply",
                                                Integer.toString(v.seed()),
                                                value -> {
                                                    try {
                                                        int seed = Integer.parseInt(value.trim());
                                                        change(
                                                                p -> {
                                                                    var a =
                                                                            (EffectParameters
                                                                                            .Sparkle)
                                                                                    p;
                                                                    return new EffectParameters
                                                                            .Sparkle(
                                                                            a.density(),
                                                                            seed,
                                                                            a.size(),
                                                                            a.brightness());
                                                                });
                                                    } catch (NumberFormatException e) {
                                                        message = "Seed must be an integer";
                                                    }
                                                })));
                y += 26;
            }
            case EffectParameters.Glow v -> {
                slider(
                        "Intensity",
                        0,
                        4,
                        () -> ((EffectParameters.Glow) parameters()).intensity(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Glow) p;
                                            return new EffectParameters.Glow(n, a.falloff());
                                        }));
                slider(
                        "Halo falloff",
                        0,
                        1,
                        () -> ((EffectParameters.Glow) parameters()).falloff(),
                        n ->
                                change(
                                        p -> {
                                            var a = (EffectParameters.Glow) p;
                                            return new EffectParameters.Glow(a.intensity(), n);
                                        }));
            }
            case EffectParameters.Hue v ->
                    slider(
                            "Hue cycles",
                            -4,
                            4,
                            () -> ((EffectParameters.Hue) parameters()).cycles(),
                            n -> change(p -> new EffectParameters.Hue(n)));
        }
        var key =
                track().keyframes().stream()
                        .min(Comparator.comparingInt(k -> Math.abs(k.tick() - tick)))
                        .orElseThrow();
        LoomButton[] easing = {null};
        easing[0] =
                button(
                        x,
                        y,
                        210,
                        "Easing: " + key.easing().label(),
                        () ->
                                showChoices(
                                        easing[0],
                                        "Outgoing key easing",
                                        Arrays.stream(AnimationEasing.values())
                                                .map(
                                                        e ->
                                                                new LoomChoicePopup.Option<>(
                                                                        e,
                                                                        e.label(),
                                                                        "Transition after the key"
                                                                                + " at "
                                                                                + key.tick()
                                                                                + " ticks"))
                                                .toList(),
                                        key.easing(),
                                        e -> {
                                            var keys =
                                                    track().keyframes().stream()
                                                            .map(
                                                                    k ->
                                                                            k.tick() == key.tick()
                                                                                    ? new AnimationKeyframe(
                                                                                            k
                                                                                                    .tick(),
                                                                                            k
                                                                                                    .value(),
                                                                                            e)
                                                                                    : k)
                                                            .toList();
                                            ClientProjectWorkspace.apply(
                                                    p ->
                                                            p.withAnimation(
                                                                    AnimationAuthoring.replaceTrack(
                                                                            p.animation(),
                                                                            track().withKeyframes(
                                                                                            keys))));
                                            rebuildWidgets();
                                        }));
    }

    private void slider(
            String label, float min, float max, DoubleSupplier read, Consumer<Float> edit) {
        addRenderableWidget(
                new LoomSlider(
                                x,
                                y,
                                210,
                                label,
                                () -> (read.getAsDouble() - min) / (max - min),
                                v -> edit.accept((float) (min + v * (max - min))))
                        .format(v -> String.format(Locale.ROOT, "%.2f", min + v * (max - min))));
        y += 26;
    }

    private LoomButton button(int x, int y, int width, String label, Runnable action) {
        return addRenderableWidget(
                new LoomButton(x, y, width, 22, Component.literal(label), action));
    }

    private void pickColor(boolean first) {
        var p = (EffectParameters.Gradient) parameters();
        int color = first ? p.firstColor() : p.secondColor();
        minecraft.setScreen(
                new LoomRenameScreen(
                        this,
                        "Gradient color",
                        "Hex RGB",
                        "Apply",
                        String.format("%06X", color & 0xFFFFFF),
                        value -> {
                            try {
                                String hex = value.replace("#", "").trim();
                                if (!hex.matches("[0-9a-fA-F]{6}"))
                                    throw new NumberFormatException();
                                int next = 0xFF000000 | Integer.parseInt(hex, 16);
                                change(
                                        current -> {
                                            var a = (EffectParameters.Gradient) current;
                                            return new EffectParameters.Gradient(
                                                    a.angle(),
                                                    a.width(),
                                                    first ? next : a.firstColor(),
                                                    first ? a.secondColor() : next,
                                                    a.offset(),
                                                    false);
                                        });
                            } catch (NumberFormatException e) {
                                message = "Enter six hexadecimal digits";
                            }
                        }));
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent e) {
        if (e.hasControlDownWithQuirk() && e.key() == 90) {
            ClientProjectWorkspace.undo();
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
        LoomScreenChrome.renderBackdrop(g, width, height);
        LoomScreenChrome.renderBrandHeader(
                g,
                width,
                track().effect().displayName() + " parameters",
                LoomUiTheme.compact(width, height));
        super.render(g, mx, my, dt);
        LoomScreenChrome.footer(
                g, width, height, font.plainSubstrByWidth(message, width - 80), "Live");
    }

    @Override
    public void removed() {
        ClientProjectWorkspace.endCompoundEdit();
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
