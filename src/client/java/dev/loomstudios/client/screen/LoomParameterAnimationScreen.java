package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.*;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.client.ui.premium.PremiumControls;
import dev.loomstudios.project.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;

/**
 * Independent lanes and curve workspace. Every row, graph and hit target is clipped to its panel.
 */
public final class LoomParameterAnimationScreen extends LoomPointerScreen {
  private final Screen parent;
  private final AnimationChannel channel;
  private UUID trackId;
  private AnimationParameter parameter;
  private int tick, scroll, startTick, zoom = 1;
  private boolean playing, refresh, previewMode;
  private double cursor;
  private final Set<AnimationKeyEditing.Address> selected = new HashSet<>();
  private List<AnimationKeyEditing.CopiedKey> clipboard = List.of();
  private final Set<UUID> collapsed = new HashSet<>();
  private String message =
      "Select a keyframe diamond, then edit its value or outgoing curve · Ctrl+C/V copy/paste";
  private int top, right, graphTop, graphHeight, timelineTop, timelineHeight;
  private Canvas widget;
  private int draggedHandle = -1, dragStart, dragDelta;
  private LoomAnimation dragAnimation;
  private Set<AnimationKeyEditing.Address> dragSelection = Set.of();
  private LoomPlayerPreviewWidget preview;
  private boolean additive;

  /** Wide displays can show curves and the actual cosmetic together, without hiding the timeline. */
  private boolean splitPreview() {
    return !previewMode && width >= 1100 && height >= 520;
  }

  private int curveRight() {
    return splitPreview() ? Math.max(240, right - Math.max(180, (right - 16) / 3)) - 12
        : right - 24;
  }

  private record Row(
      UUID layer, UUID track, AnimationParameter parameter, String label, boolean heading) {}

  public LoomParameterAnimationScreen(Screen parent, AnimationChannel channel, UUID id, int tick) {
    super(Component.literal("Parameter animation"));
    this.parent = parent;
    this.channel = channel;
    trackId = id;
    this.tick = tick;
    cursor = tick;
  }

  private LoomAnimation animation() {
    return ClientProjectWorkspace.project().animation();
  }

  private AnimationTrack track() {
    return animation().tracks().stream()
        .filter(t -> t.id().equals(trackId))
        .findFirst()
        .orElseThrow();
  }

  private List<AnimationKeyframe> keys() {
    return AnimationKeyEditing.keys(track(), parameter);
  }

  private void change(UnaryOperator<LoomAnimation> action) {
    try {
      ClientProjectWorkspace.apply(p -> p.withAnimation(action.apply(p.animation())));
      message =
          ClientProjectWorkspace.session().editError() == null
              ? "Animation updated · Save from studio to keep changes"
              : ClientProjectWorkspace.session().editError();
    } catch (IllegalArgumentException | IllegalStateException e) {
      message = e.getMessage();
    }
  }

  private void changeTrack(UnaryOperator<AnimationTrack> action) {
    change(a -> AnimationAuthoring.replaceTrack(a, action.apply(track())));
  }

  private void chooseLane(AnimationParameter p) {
    parameter = p;
    selected.clear();
    rebuildWidgets();
  }

  private LoomButton button(int x, int y, int w, String label, Runnable action) {
    var b = addRenderableWidget(new LoomButton(x, y, w, 22, Component.literal(label), action));
    b.setTooltip(Tooltip.create(Component.literal(label)));
    return b;
  }

  @Override
  protected void init() {
    top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8;
    right = width - 204;
    graphTop = top + 30;
    graphHeight = previewMode
        ? Math.max(96, Math.min(200, (height - graphTop - 64) / 2))
        : splitPreview()
            ? Math.max(110, Math.min(172, (height - graphTop - 64) / 3))
            : Math.max(64, Math.min(110, (height - graphTop - 64) / 3));
    timelineTop = graphTop + graphHeight + 8;
    timelineHeight = height - 28 - timelineTop;
    button(8, top, 60, "Back", this::onClose);
    button(
        72,
        top,
        60,
        playing ? "Pause" : "Play",
        () -> {
          playing = !playing;
          cursor = tick;
          rebuildWidgets();
        });
    button(
        136,
        top,
        54,
        "Undo",
        () -> {
          ClientProjectWorkspace.undo();
          rebuildWidgets();
        });
    button(
        194,
        top,
        40,
        "−",
        () -> {
          zoom = Math.max(1, zoom / 2);
          startTick = Math.min(startTick, animation().durationTicks() - span());
        });
    button(
        294,
        top,
        78,
        previewMode ? "Show curve" : splitPreview() ? "Focus preview" : "3D preview",
        () -> {
          previewMode = !previewMode;
          rebuildWidgets();
        });
    button(
        238,
        top,
        52,
        "+ Zoom",
        () -> {
          zoom = Math.min(16, zoom * 2);
          startTick =
              Math.max(0, Math.min(tick - span() / 2, animation().durationTicks() - span()));
        });
    int x = right + 8, y = top;
    LoomButton[] choose = {null};
    choose[0] =
        button(
            x,
            y,
            188,
            parameter == null ? "Lane: Effect amount" : parameter.label,
            () -> {
              var options = new ArrayList<LoomChoicePopup.Option<AnimationParameter>>();
              options.add(
                  new LoomChoicePopup.Option<>(null, "Effect amount", "Original primary scalar"));
              for (var p : AnimationParameter.forEffect(track().effect()))
                options.add(new LoomChoicePopup.Option<>(p, p.label, "Independent parameter lane"));
              showChoices(choose[0], "Parameter lane", options, parameter, this::chooseLane);
            });
    y += 26;
    button(
        x,
        y,
        92,
        "Add lane",
        () -> {
          if (parameter != null)
            changeTrack(
                t -> AnimationKeyEditing.addLane(t, parameter, animation().durationTicks()));
        });
    button(x + 96, y, 92, "+ Key", this::addKey);
    y += 26;
    button(x, y, 92, "Copy keys", this::copy);
    button(x + 96, y, 92, "Paste", this::paste);
    y += 26;
    button(
        x,
        y,
        92,
        "Delete keys",
        () -> {
          change(a -> AnimationKeyEditing.edit(a, selected, k -> k, true));
          selected.clear();
        });
    button(
        x + 96,
        y,
        92,
        "Select lane",
        () -> {
          selected.clear();
          for (var k : keys())
            selected.add(new AnimationKeyEditing.Address(trackId, parameter, k.tick()));
        });
    y += 26;
    addRenderableWidget(
        new LoomSlider(
            x,
            y,
            188,
            parameter == null ? "Amount" : "Key: " + parameter.label,
            () -> selectedKey().value(),
            v ->
                editSelected(
                    k -> new AnimationKeyframe(k.tick(), (float) v, k.easing(), k.curve()))));
    y += 26;
    LoomButton[] ease = {null};
    ease[0] =
        button(
            x,
            y,
            188,
            "Easing: " + selectedKey().easing().label(),
            () ->
                showChoices(
                    ease[0],
                    "Outgoing timing",
                    Arrays.stream(AnimationEasing.values())
                        .map(
                            e ->
                                new LoomChoicePopup.Option<>(
                                    e,
                                    e.label(),
                                    e == AnimationEasing.CUSTOM
                                        ? "Drag both handles in the curve graph"
                                        : "Transition following each selected key"))
                        .toList(),
                    selectedKey().easing(),
                    e -> {
                      editSelected(k -> new AnimationKeyframe(k.tick(), k.value(), e, k.curve()));
                      rebuildWidgets();
                    }));
    y += 26;
    button(
        x,
        y,
        188,
        "Reset curve",
        () ->
            editSelected(
                k ->
                    new AnimationKeyframe(
                        k.tick(), k.value(), AnimationEasing.CUSTOM, KeyframeCurve.DEFAULT)));
    y += 26;
    button(
        x,
        y,
        188,
        "Static settings…",
        () -> minecraft.setScreen(new LoomEffectParametersScreen(this, trackId, tick)));
    var previousPreview = preview == null ? null : preview.viewState();
    widget =
        addRenderableWidget(
            new Canvas(
                8,
                previewMode ? timelineTop : graphTop,
                right - 16,
                height - (previewMode ? timelineTop : graphTop) - 28));
    if (previewMode || splitPreview()) {
      int previewX = previewMode ? 8 : curveRight() + 12;
      int previewWidth = previewMode ? right - 16 : right - 8 - previewX;
      preview =
          addRenderableWidget(
              new LoomPlayerPreviewWidget(
                  previewX,
                  graphTop,
                  previewWidth,
                  graphHeight,
                  ClientProjectWorkspace::project,
                  channel == AnimationChannel.CAPE
                      ? LoomPlayerPreviewWidget.Mode.CAPE
                      : LoomPlayerPreviewWidget.Mode.ELYTRA));
      preview.restoreViewState(previousPreview);
      preview.setTimelineTickSupplier(() -> tick);
    } else {
      preview = null;
    }
  }

  private AnimationKeyframe selectedKey() {
    for (var k : keys())
      if (selected.contains(new AnimationKeyEditing.Address(trackId, parameter, k.tick())))
        return k;
    return keys().isEmpty()
        ? new AnimationKeyframe(
            tick, parameter == null ? 0 : parameter.normalize(parameter.read(track().parameters())))
        : keys().getFirst();
  }

  private void editSelected(UnaryOperator<AnimationKeyframe> edit) {
    if (selected.isEmpty()) {
      message = "Select one or more key diamonds first";
      return;
    }
    change(a -> AnimationKeyEditing.edit(a, selected, edit, false));
  }

  private void addKey() {
    float value =
        keys().isEmpty()
            ? (parameter == null ? 0 : parameter.normalize(parameter.read(track().parameters())))
            : AnimationEvaluator.valueAt(keys(), tick);
    changeTrack(
        t -> {
          var map = new TreeMap<Integer, AnimationKeyframe>();
          for (var k : AnimationKeyEditing.keys(t, parameter)) map.put(k.tick(), k);
          map.put(tick, new AnimationKeyframe(tick, value));
          return AnimationKeyEditing.withKeys(t, parameter, List.copyOf(map.values()));
        });
    selected.clear();
    selected.add(new AnimationKeyEditing.Address(trackId, parameter, tick));
  }

  private void copy() {
    try {
      clipboard = AnimationKeyEditing.copy(animation(), selected);
      message = "Copied " + clipboard.size() + " keys; paste relative to playhead";
    } catch (IllegalArgumentException e) {
      message = e.getMessage();
    }
  }

  private void paste() {
    change(a -> AnimationKeyEditing.paste(a, clipboard, tick));
  }

  private int span() {
    return Math.max(1, animation().durationTicks() / zoom);
  }

  private int laneLeft() {
    return Math.min(right - 70, 8 + Math.max(90, Math.min(160, (right - 16) / 3)));
  }

  private int tickX(int t) {
    return laneLeft() + (t - startTick) * (right - 14 - laneLeft()) / span();
  }

  private int atX(double x) {
    return Math.max(
        0,
        Math.min(
            animation().durationTicks(),
            startTick + (int) Math.round((x - laneLeft()) * span() / (right - 14 - laneLeft()))));
  }

  private List<Row> rows() {
    var rows = new ArrayList<Row>();
    var c =
        channel == AnimationChannel.CAPE
            ? ClientProjectWorkspace.project().cape()
            : ClientProjectWorkspace.project().elytra();
    for (var l : c.layers()) {
      var tracks =
          animation().tracks().stream()
              .filter(t -> t.channel() == channel && t.layerId().equals(l.id()))
              .toList();
      if (tracks.isEmpty()) continue;
      rows.add(
          new Row(l.id(), null, null, (collapsed.contains(l.id()) ? "▸ " : "▾ ") + l.name(), true));
      if (collapsed.contains(l.id())) continue;
      for (var t : tracks) {
        rows.add(new Row(l.id(), t.id(), null, t.effect().displayName() + " · Amount", false));
        for (var lane : t.lanes())
          rows.add(new Row(l.id(), t.id(), lane.parameter(), "  " + lane.parameter().label, false));
      }
    }
    return rows;
  }

  private void label(GuiGraphics g, String s, int x, int y, int w, int color) {
    PremiumControls.label(g, s, x, y, Math.max(1, w), 9, color, false);
  }

  private final class Canvas extends AbstractWidget {
    Canvas(int x, int y, int w, int h) {
      super(x, y, w, h, Component.literal("Curve and parameter lanes"));
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mx, int my, float dt) {
      if (!previewMode) {
        LoomScreenChrome.panel(g, 8, graphTop, right - 8, graphTop + graphHeight);
        int l = 24, r = curveRight(), t = graphTop + 22, b = graphTop + graphHeight - 10;
        label(
            g,
            "Selected key → next · " + selectedKey().easing().label() + " easing",
            16,
            graphTop + 6,
            curveRight() - 24,
            LoomUiTheme.TEXT_MUTED);
        g.enableScissor(l, t, r + 1, b + 1);
        for (int i = 0; i <= 4; i++) {
          int x = l + (r - l) * i / 4, y = t + (b - t) * i / 4;
          g.fill(x, t, x + 1, b, 0x442F435A);
          g.fill(l, y, r, y + 1, 0x442F435A);
        }
        var key = selectedKey();
        for (int i = 0; i < r - l; i++) {
          float progress = i / (float) (r - l), value = key.progress(progress);
          int yy = b - Math.round(value * (b - t));
          g.fill(l + i, yy, l + i + 2, yy + 2, LoomUiTheme.ACCENT);
        }
        var c = key.curve();
        for (float[] h : new float[][] {{c.x1(), c.y1()}, {c.x2(), c.y2()}}) {
          int xx = l + Math.round(h[0] * (r - l)), yy = b - Math.round(h[1] * (b - t));
          g.fill(xx - 3, yy - 3, xx + 4, yy + 4, LoomUiTheme.ACCENT_ALT);
        }
        g.disableScissor();
      }
      LoomScreenChrome.panel(g, 8, timelineTop, right - 8, LoomParameterAnimationScreen.this.height - 28);
      label(
          g,
          "Time " + String.format(java.util.Locale.ROOT, "%.2fs", tick / 20f) + " · " + zoom + "×",
          16,
          timelineTop + 6,
          laneLeft() - 24,
          LoomUiTheme.TEXT_MUTED);
      for (int i = 0; i <= 4; i++) {
        int at = startTick + span() * i / 4, x = tickX(at);
        label(
            g,
            String.format(java.util.Locale.ROOT, "%.1fs", at / 20f),
            Math.min(right - 52, x),
            timelineTop + 6,
            40,
            LoomUiTheme.TEXT_MUTED);
      }
      var rows = rows();
      int count = Math.max(1, (timelineHeight - 26) / 20);
      scroll = Math.max(0, Math.min(scroll, Math.max(0, rows.size() - count)));
      g.enableScissor(10, timelineTop + 24, right - 10, LoomParameterAnimationScreen.this.height - 30);
      for (int i = 0; i < count && i + scroll < rows.size(); i++) {
        var row = rows.get(i + scroll);
        int y = timelineTop + 24 + i * 20;
        g.fill(
            10,
            y,
            right - 10,
            y + 19,
            row.heading()
                ? 0xFF24384A
                : Objects.equals(row.track(), trackId) && row.parameter() == parameter
                    ? 0xFF263E55
                    : 0xFF172432);
        label(
            g,
            row.label(),
            16,
            y + 5,
            laneLeft() - 24,
            row.heading() ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT);
        if (row.heading()) continue;
        var tr =
            animation().tracks().stream()
                .filter(a -> a.id().equals(row.track()))
                .findFirst()
                .orElseThrow();
        g.enableScissor(laneLeft(), timelineTop + 24, right - 12, LoomParameterAnimationScreen.this.height - 30);
        for (var k : AnimationKeyEditing.keys(tr, row.parameter())) {
          int x = tickX(k.tick());
          boolean sel =
              selected.contains(
                  new AnimationKeyEditing.Address(tr.id(), row.parameter(), k.tick()));
          g.fill(x - 3, y + 6, x + 4, y + 13, sel ? LoomUiTheme.ACCENT_ALT : LoomUiTheme.ACCENT);
        }
        // Popping the per-lane clip restores the outer timeline clip. Do not push it again.
        g.disableScissor();
      }
      g.fill(tickX(tick), timelineTop + 22, tickX(tick) + 1, LoomParameterAnimationScreen.this.height - 30, LoomUiTheme.ACCENT);
      g.disableScissor();
    }

    @Override
    public void onClick(MouseButtonEvent e, boolean twice) {
      if (e.y() < graphTop + graphHeight) {
        if (e.x() > curveRight()) return;
        var k = selectedKey();
        int l = 24, r = curveRight(), t = graphTop + 22, b = graphTop + graphHeight - 10;
        var c = k.curve();
        double d1 = Math.hypot(e.x() - (l + c.x1() * (r - l)), e.y() - (b - c.y1() * (b - t))),
            d2 = Math.hypot(e.x() - (l + c.x2() * (r - l)), e.y() - (b - c.y2() * (b - t)));
        if (Math.min(d1, d2) < 12 && !selected.isEmpty()) {
          draggedHandle = d1 < d2 ? 0 : 1;
          ClientProjectWorkspace.beginCompoundEdit();
        }
        return;
      }
      if (e.y() < timelineTop + 24) {
        tick = atX(e.x());
        playing = false;
        return;
      }
      var rows = rows();
      int i = scroll + (int) (e.y() - timelineTop - 24) / 20;
      if (i < 0 || i >= rows.size()) return;
      var row = rows.get(i);
      if (row.heading()) {
        if (!collapsed.add(row.layer())) collapsed.remove(row.layer());
        return;
      }
      trackId = row.track();
      parameter = row.parameter();
      var tr = track();
      AnimationKeyframe hit = null;
      for (var k : AnimationKeyEditing.keys(tr, parameter))
        if (Math.abs(tickX(k.tick()) - e.x()) <= 7) {
          hit = k;
          break;
        }
      if (!additive) selected.clear();
      if (hit != null) {
        var a = new AnimationKeyEditing.Address(trackId, parameter, hit.tick());
        if (additive && !selected.add(a)) selected.remove(a);
        else selected.add(a);
        tick = hit.tick();
        if (!selected.isEmpty()) {
          dragAnimation = animation();
          dragSelection = Set.copyOf(selected);
          dragStart = atX(e.x());
          dragDelta = 0;
          ClientProjectWorkspace.beginCompoundEdit();
        }
      } else tick = atX(e.x());
      playing = false;
      refresh = true;
    }

    @Override
    protected void onDrag(MouseButtonEvent e, double dx, double dy) {
      if (draggedHandle < 0) {
        if (dragAnimation != null) {
          int delta = atX(e.x()) - dragStart;
          try {
            var next = AnimationKeyEditing.move(dragAnimation, dragSelection, delta);
            ClientProjectWorkspace.apply(p -> p.withAnimation(next));
            if (ClientProjectWorkspace.session().editError() == null) {
              dragDelta = delta;
              selected.clear();
              for (var a : dragSelection)
                selected.add(
                    new AnimationKeyEditing.Address(a.track(), a.parameter(), a.tick() + delta));
            }
          } catch (IllegalArgumentException ignored) {
          }
        }
        return;
      }
      int l = 24, r = right - 24, t = graphTop + 22, b = graphTop + graphHeight - 10;
      float x = Math.max(0, Math.min(1, (float) (e.x() - l) / (r - l))),
          y = Math.max(0, Math.min(1, (float) (b - e.y()) / (b - t)));
      editSelected(
          k -> {
            var c = k.curve();
            var next =
                draggedHandle == 0
                    ? new KeyframeCurve(Math.min(x, c.x2()), y, c.x2(), c.y2())
                    : new KeyframeCurve(c.x1(), c.y1(), Math.max(x, c.x1()), y);
            return new AnimationKeyframe(k.tick(), k.value(), AnimationEasing.CUSTOM, next);
          });
    }

    @Override
    public void onRelease(MouseButtonEvent e) {
      if (draggedHandle >= 0 || dragAnimation != null) {
        draggedHandle = -1;
        dragAnimation = null;
        ClientProjectWorkspace.endCompoundEdit();
        refresh = true;
      }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
      if (!isMouseOver(x, y)) return false;
      if (additive)
        startTick =
            Math.max(
                0,
                Math.min(
                    animation().durationTicks() - span(),
                    startTick - (int) (dy * Math.max(1, span() / 10))));
      else scroll = Math.max(0, scroll - (int) dy);
      return true;
    }

    @Override
    protected void updateWidgetNarration(
        net.minecraft.client.gui.narration.NarrationElementOutput out) {
      out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, getMessage());
    }
  }

  @Override
  public boolean keyPressed(KeyEvent e) {
    if (hasChoices()) return super.keyPressed(e);
    if (e.key() == 340 || e.key() == 344 || e.key() == 341 || e.key() == 345) additive = true;
    if (e.hasControlDownWithQuirk()) {
      switch (e.key()) {
        case 67 -> copy();
        case 86 -> paste();
        case 90 -> {
          ClientProjectWorkspace.undo();
          rebuildWidgets();
        }
        case 89 -> {
          ClientProjectWorkspace.redo();
          rebuildWidgets();
        }
        default -> {
          return super.keyPressed(e);
        }
      }
      return true;
    }
    if (e.key() == 261) {
      change(a -> AnimationKeyEditing.edit(a, selected, k -> k, true));
      selected.clear();
      return true;
    }
    return super.keyPressed(e);
  }

  @Override
  public boolean keyReleased(KeyEvent e) {
    if (e.key() == 340 || e.key() == 344 || e.key() == 341 || e.key() == 345) additive = false;
    return super.keyReleased(e);
  }

  @Override
  public void tick() {
    if (refresh && !ClientProjectWorkspace.session().isCompoundEditActive()) {
      refresh = false;
      rebuildWidgets();
    }
    if (playing) {
      cursor += animation().playbackSpeed();
      if (cursor > animation().durationTicks()) {
        if (animation().loop()) cursor %= animation().durationTicks();
        else {
          cursor = animation().durationTicks();
          playing = false;
        }
      }
      tick = (int) cursor;
      if (tick < startTick || tick > startTick + span())
        startTick = Math.max(0, Math.min(tick, animation().durationTicks() - span()));
    }
  }

  @Override
  public void render(GuiGraphics g, int mx, int my, float dt) {
    LoomScreenChrome.renderBackdrop(g, width, height);
    LoomScreenChrome.renderBrandHeader(
        g, width, "Advanced Animation", LoomUiTheme.compact(width, height));
    super.render(g, mx, my, dt);
    LoomScreenChrome.footer(g, width, height, message, selected.size() + " selected");
    renderChoices(g, mx, my);
  }

  @Override
  public void removed() {
    ClientProjectWorkspace.endCompoundEdit();
    super.removed();
  }

  @Override
  public void onClose() {
    minecraft.setScreen(parent);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
