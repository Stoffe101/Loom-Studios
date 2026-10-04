package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.*;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.image.*;
import dev.loomstudios.project.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/** Focused reference, frame, onion and reusable-stamp tabs for either semantic editor face. */
public final class LoomCreativeAssetsScreen extends LoomPointerScreen {
  private final Screen parent;
  private final boolean wing;
  private final UUID layerId;
  private final CapeUvRegion cape;
  private final ElytraWing selectedWing;
  private final ElytraSurface surface;
  private final int color;
  private final BitSet selection;
  private int tab, index, onion = 3, size = 9, turns;
  private boolean erase, playing, mirror, recolor = true;
  private float onionOpacity = .3f;
  private UUID referenceId, stampId;
  private List<CustomStamp> stamps = List.of();
  private EditBox name;
  private long localRevision;
  private int top, right, y;
  private String message = "Editor-only guides never appear in exports or equipped designs";
  private LoomImagePreviewWidget image, stampPreview;
  private int playClock;

  public LoomCreativeAssetsScreen(
      Screen parent,
      boolean wing,
      UUID layerId,
      CapeUvRegion cape,
      ElytraWing selectedWing,
      ElytraSurface surface,
      int color,
      BitSet selection) {
    super(Component.literal("Creative assets"));
    this.parent = parent;
    this.wing = wing;
    this.layerId = layerId;
    this.cape = cape == null ? CapeUvRegion.OUTSIDE : cape;
    this.selectedWing = selectedWing == null ? ElytraWing.LEFT : selectedWing;
    this.surface = surface == null ? ElytraSurface.OUTSIDE : surface;
    this.color = color;
    this.selection = selection == null ? null : (BitSet) selection.clone();
    try {
      stamps = EditorOverlayState.STORE.stamps();
    } catch (java.io.IOException e) {
      message = e.getMessage();
    }
    if (!stamps.isEmpty()) stampId = stamps.getFirst().id();
  }

  public LoomCreativeAssetsScreen openTab(int value) {
    tab = value;
    return this;
  }

  private AnimationChannel channel() {
    return wing ? AnimationChannel.ELYTRA : AnimationChannel.CAPE;
  }

  private LoomCanvas canvas() {
    return wing
        ? ClientProjectWorkspace.project().elytra()
        : ClientProjectWorkspace.project().cape();
  }

  private LoomLayer layer() {
    return canvas().layers().stream().filter(l -> l.id().equals(layerId)).findFirst().orElseThrow();
  }

  private int fw() {
    int s = canvas().width() / 64;
    return wing ? surface.width(s) : cape.width(s);
  }

  private int fh() {
    int s = canvas().width() / 64;
    return wing ? surface.height(s) : cape.height(s);
  }

  private int[] map() {
    var c = canvas();
    int s = c.width() / 64;
    int[] out = new int[fw() * fh()];
    for (int y = 0; y < fh(); y++)
      for (int x = 0; x < fw(); x++)
        out[y * fw() + x] =
            (wing ? surface.atlasY(y, s) : cape.atlasY(y, s)) * c.width()
                + (wing ? surface.atlasX(selectedWing, x, s) : cape.atlasX(x, s));
    return out;
  }

  private NormalizedRect target() {
    int[] m = map();
    return new NormalizedRect(
        (m[0] % canvas().width()) / (double) canvas().width(),
        (m[0] / canvas().width()) / (double) canvas().height(),
        fw() / (double) canvas().width(),
        fh() / (double) canvas().height());
  }

  private PixelPatch face() {
    int[] atlas = LayerRasterizer.rasterize(layer(), canvas().width(), canvas().height()),
        map = map(),
        out = new int[map.length];
    for (int i = 0; i < out.length; i++) out[i] = atlas[map[i]];
    return new PixelPatch(fw(), fh(), out);
  }

  private List<ReferenceImage> refs() {
    return EditorOverlayState.references(ClientProjectWorkspace.project().projectId());
  }

  private ReferenceImage reference() {
    return refs().stream()
        .filter(r -> r.id().equals(referenceId) && r.channel() == channel())
        .findFirst()
        .orElse(null);
  }

  private CustomStamp stamp() {
    return stamps.stream().filter(s -> s.id().equals(stampId)).findFirst().orElse(null);
  }

  private ImageLayerData data() {
    return layer().kind() == LayerKind.IMAGE ? layer().imageData() : null;
  }

  private PixelImage cachedPreview;
  private long previewVersion=Long.MIN_VALUE;
  private PixelImage preview(){long version=31*ClientProjectWorkspace.revision()+localRevision+EditorOverlayState.revision();if(cachedPreview==null||previewVersion!=version){cachedPreview=buildPreview();previewVersion=version;}return cachedPreview;}
  private PixelImage buildPreview() {
    if (tab == 1 && data() != null && data().editableAnimation() && !data().frames().isEmpty()) {
      index = Math.min(index, data().frames().size() - 1);
      return EditableFrames.onion(data(), index, onion, onionOpacity);
    }
    int[] atlas = EditorOverlayState.composite(ClientProjectWorkspace.project(), channel()),
        map = map(),
        p = new int[map.length];
    for (int i = 0; i < p.length; i++) p[i] = atlas[map[i]];
    return new PixelImage(fw(), fh(), p);
  }

  private LoomButton button(int x, int y, int w, String label, Runnable action) {
    var b =
        addRenderableWidget(
            new LoomButton(
                x,
                y,
                w,
                22,
                Component.literal(label),
                () -> {
                  try {
                    action.run();
                  } catch (IllegalArgumentException | IllegalStateException e) {
                    message = e.getMessage();
                  }
                }));
    b.setTooltip(Tooltip.create(Component.literal(label)));
    return b;
  }

  private void row(String label, Runnable action) {
    button(right + 8, y, 188, label, action);
    y += 24;
  }

  private void pair(String a, Runnable aa, String b, Runnable bb) {
    button(right + 8, y, 92, a, aa);
    button(right + 104, y, 92, b, bb);
    y += 24;
  }

  private void slider(String label, DoubleSupplier value, DoubleConsumer set) {
    addRenderableWidget(new LoomSlider(right + 8, y, 188, label, value, set));
    y += 24;
  }

  @Override
  protected void init() {
    if (image != null) image.close();
    if (stampPreview != null) stampPreview.close();
    top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8;
    right = width - 204;
    button(8, top, 60, "Back", this::onClose);
    button(
        72,
        top,
        60,
        "Undo",
        () -> {
          ClientProjectWorkspace.undo();
          localRevision++;
        });
    button(
        136,
        top,
        60,
        "Redo",
        () -> {
          ClientProjectWorkspace.redo();
          localRevision++;
        });
    image =
        addRenderableWidget(
            new LoomImagePreviewWidget(
                8,
                top + 28,
                right - 16,
                height - top - 56,
                Component.literal(
                    tab == 1
                        ? "Editable frame · click/drag to draw"
                        : tab == 2
                            ? "Stamp canvas · click/drag to apply"
                            : "Editor canvas · local guides"),
                this::preview,
                () ->
                    ClientProjectWorkspace.revision()
                        + localRevision
                        + EditorOverlayState.revision()));
    image.setPixelAction((x, y) -> paint(x, y));
    String[] tabs = {"References", "Frames", "Stamps", "Onion skin"};
    for (int i = 0; i < 4; i++) {
      int v = i;
      button(
          right + 8 + (i % 2) * 96,
          top + (i / 2) * 24,
          92,
          tabs[i],
          () -> {
            tab = v;
            playing = false;
            localRevision++;
            rebuildWidgets();
          });
    }
    y = top + 52;
    switch (tab) {
      case 0 -> referencesUi();
      case 1 -> framesUi();
      case 2 -> stampsUi();
      default -> onionUi();
    }
  }

  private void referencesUi() {
    var current = reference();
    if (current == null) {
      referenceId =
          refs().stream()
              .filter(r -> r.channel() == channel())
              .map(ReferenceImage::id)
              .findFirst()
              .orElse(null);
      current = reference();
    }
    LoomButton[] choose = {null};
    choose[0] =
        button(
            right + 8,
            y,
            188,
            current == null ? "No reference selected" : current.name(),
            () ->
                showChoices(
                    choose[0],
                    "Reference layers",
                    refs().stream()
                        .filter(r -> r.channel() == channel())
                        .map(
                            r ->
                                new LoomChoicePopup.Option<>(
                                    r.id(),
                                    r.name(),
                                    r.above() ? "Above artwork" : "Below artwork"))
                        .toList(),
                    referenceId,
                    id -> {
                      referenceId = id;
                      rebuildWidgets();
                    }));
    y += 24;
    pair(
        "+ Image",
        this::importReference,
        "Delete",
        () -> {
          if (reference() == null) return;
          if (reference().locked())
            throw new IllegalStateException("Unlock reference before deleting");
          var list = new ArrayList<>(refs());
          list.removeIf(r -> r.id().equals(referenceId));
          saveReferences(list);
          referenceId = null;
          rebuildWidgets();
        });
    slider(
        "Guide opacity",
        () -> reference() == null ? .5 : reference().opacity(),
        v -> referenceState((float) v, null, null, null));
    pair(
        current == null || current.visible() ? "Hide guide" : "Show guide",
        () -> {
          if (reference() != null) referenceState(null, !reference().visible(), null, null);
          rebuildWidgets();
        },
        current != null && current.above() ? "Above art" : "Below art",
        () -> {
          if (reference() != null) referenceState(null, null, null, !reference().above());
          rebuildWidgets();
        });
    row(
        current != null && current.locked() ? "Locked · Unlock" : "Unlocked · Lock",
        () -> {
          if (reference() != null) referenceState(null, null, !reference().locked(), null);
          rebuildWidgets();
        });
    pair("Move left", () -> moveReference(-.01, 0), "Move right", () -> moveReference(.01, 0));
    pair("Move up", () -> moveReference(0, -.01), "Move down", () -> moveReference(0, .01));
    pair("Smaller", () -> scaleReference(.9), "Larger", () -> scaleReference(1.1));
    message =
        "Reference layers are local to this project · opacity/display work while locked · unlock to"
            + " move/delete";
  }

  private void importReference() {
    String path =
        TinyFileDialogs.tinyfd_openFileDialog(
            "Loom Studios · Reference image", "", null, "PNG, JPEG, GIF, BMP, TIFF, WBMP", false);
    if (path == null) return;
    try {
      var loaded = ImageImportReader.load(Path.of(path));
      String name = Path.of(path).getFileName().toString();
      if (name.length() > 96) name = name.substring(0, 96);
      var r =
          new ReferenceImage(
              UUID.randomUUID(),
              name,
              channel(),
              ImageLayerData.placed(
                  loaded.embedded(),
                  canvas().width(),
                  canvas().height(),
                  target(),
                  ImagePlacementMode.FIT),
              .5f,
              true,
              true,
              true);
      var list = new ArrayList<>(refs());
      list.add(r);
      saveReferences(list);
      referenceId = r.id();
      rebuildWidgets();
    } catch (java.io.IOException e) {
      message = e.getMessage();
    }
  }

  private void saveReferences(List<ReferenceImage> value) {
    try {
      EditorOverlayState.references(ClientProjectWorkspace.project().projectId(), value);
      localRevision++;
    } catch (java.io.IOException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private void editReference(UnaryOperator<ReferenceImage> edit) {
    if (reference() == null) return;
    var list = new ArrayList<>(refs());
    for (int i = 0; i < list.size(); i++)
      if (list.get(i).id().equals(referenceId)) list.set(i, edit.apply(list.get(i)));
    saveReferences(list);
  }

  private void referenceState(Float opacity, Boolean visible, Boolean locked, Boolean above) {
    editReference(
        r ->
            r.withState(
                opacity == null ? r.opacity() : opacity,
                visible == null ? r.visible() : visible,
                locked == null ? r.locked() : locked,
                above == null ? r.above() : above));
  }

  private void moveReference(double dx, double dy) {
    editReference(
        r ->
            r.withImage(
                r.image()
                    .withTransform(
                        r.image()
                            .transform()
                            .withCenter(
                                r.image().transform().centerX() + dx,
                                r.image().transform().centerY() + dy))));
  }

  private void scaleReference(double n) {
    editReference(
        r ->
            r.withImage(
                r.image()
                    .withTransform(
                        r.image()
                            .transform()
                            .withSize(
                                r.image().transform().width() * n,
                                r.image().transform().height() * n))));
  }

  private void framesUi() {
    if (data() == null || data().frames().isEmpty()) {
      row("Select a GIF Image layer", () -> {});
      message = "Import a GIF, select its Image layer, then open Frames";
      return;
    }
    if (!data().editableAnimation()) {
      row(
          "Convert GIF to Editable Animation",
          () -> {
            editData(
                d ->
                    EditableFrames.convert(
                        layer(), canvas().width(), canvas().height(), map(), fw(), fh(), target()));
            index = 0;
            localRevision++;
            rebuildWidgets();
          });
      message = "Conversion bakes processing onto this face · Ctrl+Z restores original GIF";
      return;
    }
    index = Math.min(index, data().frames().size() - 1);
    LoomButton[] choose = {null};
    choose[0] =
        button(
            right + 8,
            y,
            188,
            "Frame " + (index + 1) + " / " + data().frames().size(),
            () ->
                showChoices(
                    choose[0],
                    "Animation frames",
                    java.util.stream.IntStream.range(0, data().frames().size())
                        .mapToObj(
                            i ->
                                new LoomChoicePopup.Option<>(
                                    i, "Frame " + (i + 1), data().frameTicks().get(i) + " ticks"))
                        .toList(),
                    index,
                    i -> {
                      index = i;
                      playing = false;
                      syncTime();
                      rebuildWidgets();
                    }));
    y += 24;
    pair("Previous", () -> selectFrame(-1), "Next", () -> selectFrame(1));
    pair(
        playing ? "Pause" : "Preview loop",
        () -> {
          playing = !playing;
          playClock = 0;
          rebuildWidgets();
        },
        erase ? "Eraser" : "Draw color",
        () -> {
          erase = !erase;
          rebuildWidgets();
        });
    slider(
        "Duration (ticks)",
        () -> (data().frameTicks().get(index) - 1) / 1199.0,
        v -> editData(d -> EditableFrames.duration(d, index, 1 + (int) Math.round(v * 1199))));
    pair(
        "Duplicate",
        () -> {
          editData(d -> EditableFrames.duplicate(d, index));
          if (ClientProjectWorkspace.session().editError() == null) index++;
          syncTime();
          rebuildWidgets();
        },
        "Delete",
        () -> {
          editData(d -> EditableFrames.delete(d, index));
          index = Math.min(index, data().frames().size() - 1);
          syncTime();
          rebuildWidgets();
        });
    pair("Move earlier", () -> moveFrame(-1), "Move later", () -> moveFrame(1));
    onionControls();
    message =
        "Frame painting uses selected color · Eraser clears · each drag is one Undo · pink"
            + " previous, cyan next";
  }

  private void selectFrame(int delta) {
    index = Math.floorMod(index + delta, data().frames().size());
    playing = false;
    syncTime();
    rebuildWidgets();
  }

  private void moveFrame(int delta) {
    int next = Math.max(0, Math.min(data().frames().size() - 1, index + delta));
    editData(d -> EditableFrames.move(d, index, next));
    index = next;
    syncTime();
    rebuildWidgets();
  }

  private void editData(UnaryOperator<ImageLayerData> edit) {
    if (layer().locked()) throw new IllegalStateException("Unlock this layer first");
    ClientProjectWorkspace.apply(
        p -> {
          var c = wing ? p.elytra() : p.cape();
          var l = c.layers().stream().filter(v -> v.id().equals(layerId)).findFirst().orElseThrow();
          var out = c.replaceLayer(layerId, l.withImageData(edit.apply(l.imageData())));
          return wing ? p.withElytra(out) : p.withCape(out);
        });
    if (ClientProjectWorkspace.session().editError() != null)
      message = ClientProjectWorkspace.session().editError();
    localRevision++;
  }

  private void syncTime() {
    if (data() == null || data().frames().isEmpty()) return;
    index = Math.max(0, Math.min(index, data().frames().size() - 1));
    int at = 0;
    for (int i = 0; i < index; i++) at += data().frameTicks().get(i);
    int total = data().frameTicks().stream().mapToInt(Integer::intValue).sum(),
        prev =
            index == 0
                ? total - data().frameTicks().getLast()
                : at - data().frameTicks().get(index - 1),
        next = (at + data().frameTicks().get(index)) % total;
    EditorOverlayState.time(at, prev, next);
    localRevision++;
  }

  private void onionControls() {
    LoomButton[] choose = {null};
    String[] labels = {"Off", "Previous", "Next", "Both"};
    choose[0] =
        button(
            right + 8,
            y,
            188,
            "Onion: " + labels[onion],
            () ->
                showChoices(
                    choose[0],
                    "Frame ghosts",
                    java.util.stream.IntStream.range(0, 4)
                        .mapToObj(
                            i ->
                                new LoomChoicePopup.Option<>(
                                    i, labels[i], "Pink previous · cyan next · editor-only"))
                        .toList(),
                    onion,
                    i -> {
                      onion = i;
                      EditorOverlayState.onion(onion, onionOpacity);
                      localRevision++;
                      rebuildWidgets();
                    }));
    y += 24;
    slider(
        "Ghost opacity",
        () -> onionOpacity,
        v -> {
          onionOpacity = (float) v;
          EditorOverlayState.onion(onion, onionOpacity);
          localRevision++;
        });
  }

  private void onionUi() {
    onionControls();
    slider(
        "Timeline position",
        () ->
            EditorOverlayState.tick()
                / (double) ClientProjectWorkspace.project().animation().durationTicks(),
        v -> {
          int t =
              (int) Math.round(v * ClientProjectWorkspace.project().animation().durationTicks());
          EditorOverlayState.time(
              t,
              Math.max(0, t - 1),
              Math.min(ClientProjectWorkspace.project().animation().durationTicks(), t + 1));
          localRevision++;
        });
    row(
        "Show current frame only",
        () -> {
          onion = 0;
          EditorOverlayState.onion(0, onionOpacity);
          rebuildWidgets();
        });
    message =
        "Pink = previous frame · cyan = next frame · ghosts are editor-only and never"
            + " exported/equipped";
  }

  private void stampsUi() {
    LoomButton[] choose = {null};
    var current = stamp();
    choose[0] =
        button(
            right + 8,
            y,
            188,
            current == null ? "Choose a stamp" : (current.favorite() ? "★ " : "") + current.name(),
            () ->
                showChoices(
                    choose[0],
                    "Reusable stamps",
                    stamps.stream()
                        .sorted(
                            Comparator.comparing(CustomStamp::favorite)
                                .reversed()
                                .thenComparing(CustomStamp::name))
                        .map(
                            s ->
                                new LoomChoicePopup.Option<>(
                                    s.id(),
                                    (s.favorite() ? "★ " : "") + s.name(),
                                    s.patch().width() + " × " + s.patch().height() + " px"))
                        .toList(),
                    stampId,
                    id -> {
                      stampId = id;
                      rebuildWidgets();
                    }));
    y += 24;
    name =
        addRenderableWidget(
            new EditBox(minecraft.font, right + 8, y, 188, 20, Component.literal("Stamp name")));
    name.setMaxLength(64);
    name.setValue("My stamp");
    y += 24;
    row(
        "Create Stamp from Selection",
        () -> {
          var patch = CustomStamp.selection(face(), selection);
          var stamp = new CustomStamp(UUID.randomUUID(), name.getValue(), patch, false);
          var list = new ArrayList<>(stamps);
          list.add(stamp);
          saveStamps(list);
          stampId = stamp.id();
          rebuildWidgets();
        });
    pair(
        current != null && current.favorite() ? "Unfavorite" : "Favorite",
        () -> {
          if (stamp() == null) return;
          var list =
              stamps.stream()
                  .map(s -> s.id().equals(stampId) ? s.withFavorite(!s.favorite()) : s)
                  .toList();
          saveStamps(list);
          rebuildWidgets();
        },
        "Delete stamp",
        () -> {
          var list = new ArrayList<>(stamps);
          list.removeIf(s -> s.id().equals(stampId));
          saveStamps(list);
          stampId = stamps.isEmpty() ? null : stamps.getFirst().id();
          rebuildWidgets();
        });
    pair(
        "Rotate " + turns * 90 + "°",
        () -> {
          turns = (turns + 1) % 4;
          rebuildWidgets();
        },
        mirror ? "Mirror: On" : "Mirror: Off",
        () -> {
          mirror = !mirror;
          rebuildWidgets();
        });
    slider(
        "Size",
        () -> (size - 1) / 127.0,
        v -> {
          size = 1 + (int) Math.round(v * 127);
          localRevision++;
        });
    row(
        recolor ? "Use selected color" : "Use stamp colors",
        () -> {
          recolor = !recolor;
          rebuildWidgets();
        });
    message =
        "Wand/Select artwork first to save a stamp · click canvas to stamp · favorites are listed"
            + " first";
  }

  private void saveStamps(List<CustomStamp> value) {
    try {
      EditorOverlayState.STORE.stamps(value);
      stamps = List.copyOf(value);
      localRevision++;
    } catch (java.io.IOException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private void paint(int x, int y) {
    try {
      if (tab == 1 && data() != null && data().editableAnimation() && !playing) {
        int fx = Math.min(data().source().width() - 1, x),
            fy = Math.min(data().source().height() - 1, y);
        int[] p = data().frames().get(index).pixels();
        if (x >= data().source().width() || y >= data().source().height()) return;
        p[fy * data().source().width() + fx] = erase ? 0 : color;
        editData(
            d ->
                EditableFrames.replace(
                    d, index, new PixelImage(d.source().width(), d.source().height(), p)));
      } else if (tab == 2 && stamp() != null) {
        if (!layer().editableAsPaint())
          throw new IllegalStateException("Select an unlocked Paint layer");
        var patch = stamp().apply(face(), x, y, size, turns, mirror, recolor, color, null);
        int[] pixels = layer().pixels(), map = map(), draw = patch.data();
        for (int i = 0; i < map.length; i++) pixels[map[i]] = draw[i];
        ClientProjectWorkspace.apply(
            p -> {
              var c = wing ? p.elytra() : p.cape();
              var out = c.replaceLayer(layerId, layer().withPixels(pixels));
              return wing ? p.withElytra(out) : p.withCape(out);
            });
        localRevision++;
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      message = e.getMessage();
    }
  }

  @Override
  public void tick() {
    if (playing && data() != null && data().editableAnimation()) {
      playClock++;
      if (playClock >= data().frameTicks().get(index)) {
        playClock = 0;
        index = (index + 1) % data().frames().size();
        syncTime();
      }
    }
  }

  @Override
  public boolean keyPressed(KeyEvent e) {
    if (hasChoices()) return super.keyPressed(e);
    if (e.hasControlDownWithQuirk() && (e.key() == 90 || e.key() == 89)) {
      if (e.key() == 90) ClientProjectWorkspace.undo();
      else ClientProjectWorkspace.redo();
      localRevision++;
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
        "Creative assets · " + channel().displayName(),
        LoomUiTheme.compact(width, height));
    super.render(g, mx, my, dt);
    LoomScreenChrome.footer(g, width, height, message, "Local guides · saved stamps");
  }

  @Override
  public void removed() {
    if (image != null) image.close();
    if (stampPreview != null) stampPreview.close();
    ClientProjectWorkspace.endCompoundEdit();
    super.removed();
  }

  @Override
  public void onClose() {
    playing = false;
    minecraft.setScreen(parent);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
