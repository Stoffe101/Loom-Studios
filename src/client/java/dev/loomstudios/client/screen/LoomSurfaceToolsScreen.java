package dev.loomstudios.client.screen;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.ui.*;
import dev.loomstudios.image.PixelImage;
import dev.loomstudios.project.*;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.UnaryOperator;

/** Focused color selection, stamp and mask workspace shared by both editors. */
public final class LoomSurfaceToolsScreen extends LoomPointerScreen {
    private final Screen parent;
    private final boolean wing;
    private final UUID id;
    private final CapeUvRegion capeFace;
    private final ElytraWing selectedWing;
    private final ElytraSurface wingFace;
    private final int color;
    private BitSet selection;
    private int tolerance = 16, brushSize = 5;
    private boolean contiguous = true, seams, maskEdit;
    private int mode;
    private BrushStamp stamp = BrushStamp.CIRCLE;
    private String message =
            "Click artwork to select connected color · Replace uses the editor’s selected color";
    private LoomImagePreviewWidget image;
    private long revision;
    private int right, top;

    public LoomSurfaceToolsScreen(
            Screen parent,
            boolean wing,
            UUID id,
            CapeUvRegion face,
            ElytraWing selectedWing,
            ElytraSurface wingFace,
            int color) {
        super(Component.literal("Surface tools"));
        this.parent = parent;
        this.wing = wing;
        this.id = id;
        this.capeFace = face == null ? CapeUvRegion.OUTSIDE : face;
        this.selectedWing = selectedWing == null ? ElytraWing.LEFT : selectedWing;
        this.wingFace = wingFace == null ? ElytraSurface.OUTSIDE : wingFace;
        this.color = color;
    }

    private LoomCanvas canvas() {
        return wing
                ? ClientProjectWorkspace.project().elytra()
                : ClientProjectWorkspace.project().cape();
    }

    private LoomLayer layer() {
        return canvas().layers().stream().filter(l -> l.id().equals(id)).findFirst().orElseThrow();
    }

    private int[] mapping() {
        var c = canvas();
        int s = c.width() / 64;
        if (seams && !wing) return CapeSeamEdits.mapping(c.width());
        int w = wing ? wingFace.width(s) : capeFace.width(s),
                h = wing ? wingFace.height(s) : capeFace.height(s);
        int[] map = new int[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                map[y * w + x] =
                        (wing ? wingFace.atlasY(y, s) : capeFace.atlasY(y, s)) * c.width()
                                + (wing
                                        ? wingFace.atlasX(selectedWing, x, s)
                                        : capeFace.atlasX(x, s));
        return map;
    }

    private PixelPatch patch() {
        var c = canvas();
        int s = c.width() / 64;
        int[] map = mapping(),
                raster = LayerRasterizer.rasterize(layer(), c.width(), c.height()),
                out = new int[map.length];
        for (int i = 0; i < map.length; i++) if (map[i] >= 0) out[i] = raster[map[i]];
        return new PixelPatch(
                seams && !wing ? 12 * s : wing ? wingFace.width(s) : capeFace.width(s),
                seams && !wing ? 18 * s : wing ? wingFace.height(s) : capeFace.height(s),
                out);
    }

    private PixelImage preview() {
        var p = patch();
        int[] pixels = p.data(), map = mapping();
        for (int i = 0; i < pixels.length; i++) {
            if (maskEdit && map[i] >= 0) {
                int value = layer().maskAt(map[i]);
                pixels[i] = 0xFF000000 | value << 16 | value << 8 | value;
            } else if (selection != null && selection.get(i)) pixels[i] = 0xFF55DDE0;
        }
        return new PixelImage(p.width(), p.height(), pixels);
    }

    @Override
    protected void init() {
        if (image != null) image.close();
        top = LoomScreenChrome.headerHeight(LoomUiTheme.compact(width, height)) + 8;
        right = width - 204;
        button(8, top, 64, "Back", this::onClose);
        button(
                76,
                top,
                80,
                "Undo",
                () -> {
                    ClientProjectWorkspace.undo();
                    revision++;
                });
        button(
                160,
                top,
                80,
                "Redo",
                () -> {
                    ClientProjectWorkspace.redo();
                    revision++;
                });
        image =
                addRenderableWidget(
                        new LoomImagePreviewWidget(
                                8,
                                top + 28,
                                right - 16,
                                height - top - 58,
                                Component.literal(
                                        seams
                                                ? "Unfolded cape · edges are adjacent"
                                                : maskEdit
                                                        ? "Mask · white reveals / black hides"
                                                        : "Click artwork · exact pixel selection"),
                                this::preview,
                                () -> ClientProjectWorkspace.revision() + revision));
        image.setPixelAction(this::click);
        int x = right + 4, y = top;
        String[] labels = {"Wand / color", "Brush stamps", "Layer masks"};
        for (int i = 0; i < 3; i++) {
            int m = i;
            button(
                    x + i * 64,
                    y,
                    61,
                    labels[i],
                    () -> {
                        mode = m;
                        maskEdit = false;
                        rebuildWidgets();
                    });
        }
        y += 28;
        if (mode == 0) {
            button(
                    x,
                    y,
                    192,
                    contiguous ? "Connected color" : "All matching color",
                    () -> {
                        contiguous = !contiguous;
                        revision++;
                        rebuildWidgets();
                    });
            y += 26;
            addRenderableWidget(
                    new LoomSlider(
                            x,
                            y,
                            192,
                            "Tolerance",
                            () -> tolerance / 255.0,
                            v -> {
                                tolerance = (int) Math.round(v * 255);
                            }));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    "Replace with selected color",
                    () ->
                            change(
                                    p ->
                                            ColorSelection.replace(
                                                    p,
                                                    selection == null ? new BitSet() : selection,
                                                    color,
                                                    true)));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    "Delete selected pixels",
                    () ->
                            change(
                                    p ->
                                            ColorSelection.replace(
                                                    p,
                                                    selection == null ? new BitSet() : selection,
                                                    0,
                                                    false)));
            y += 26;
            button(
                    x,
                    y,
                    92,
                    "Clear",
                    () -> {
                        selection = null;
                        revision++;
                    });
            button(
                    x + 98,
                    y,
                    94,
                    "Invert",
                    () -> {
                        if (selection == null) selection = new BitSet();
                        selection.flip(0, patch().data().length);
                        revision++;
                    });
        } else if (mode == 1) {
            LoomButton[] b = {null};
            b[0] =
                    button(
                            x,
                            y,
                            192,
                            "Stamp: " + stamp.label(),
                            () ->
                                    showChoices(
                                            b[0],
                                            "Brush stamp",
                                            Arrays.stream(BrushStamp.values())
                                                    .map(
                                                            v ->
                                                                    new LoomChoicePopup.Option<>(
                                                                            v,
                                                                            v.label(),
                                                                            "Stamp inside the"
                                                                                + " current color"
                                                                                + " selection"))
                                                    .toList(),
                                            stamp,
                                            v -> {
                                                stamp = v;
                                                rebuildWidgets();
                                            }));
            y += 26;
            addRenderableWidget(
                    new LoomSlider(
                            x,
                            y,
                            192,
                            "Size",
                            () -> (brushSize - 1) / 31.0,
                            v -> {
                                brushSize = 1 + (int) Math.round(v * 31);
                            }));
            y += 26;
            if (!wing)
                button(
                        x,
                        y,
                        192,
                        seams ? "Seam editing: On" : "Seam editing: Off",
                        () -> {
                            seams = !seams;
                            selection = null;
                            revision++;
                            rebuildWidgets();
                        });
            message =
                    "Click or drag to stamp · Selection limits painting · Seam mode unfolds all"
                        + " four outside edges";
        } else {
            button(
                    x,
                    y,
                    192,
                    layer().alphaLocked() ? "Alpha lock: On" : "Alpha lock: Off",
                    () -> flags(l -> l.withAlphaLocked(!l.alphaLocked())));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    layer().clipToBelow() ? "Clip below: On" : "Clip below: Off",
                    () -> flags(l -> l.withClipToBelow(!l.clipToBelow())));
            y += 26;
            button(
                    x,
                    y,
                    192,
                    maskEdit ? "Edit mask: On" : "Edit mask",
                    () -> {
                        if (layer().maskLength() == 0)
                            flags(
                                    l -> {
                                        byte[] mask =
                                                new byte[canvas().width() * canvas().height()];
                                        Arrays.fill(mask, (byte) 255);
                                        return l.withMask(mask);
                                    });
                        maskEdit = !maskEdit;
                        revision++;
                        rebuildWidgets();
                    });
            y += 26;
            button(
                    x,
                    y,
                    92,
                    "Reveal",
                    () -> {
                        colorMode = 255;
                        message = "Click or drag: reveal mask pixels";
                    });
            button(
                    x + 98,
                    y,
                    94,
                    "Hide",
                    () -> {
                        colorMode = 0;
                        message = "Click or drag: hide mask pixels";
                    });
            y += 26;
            button(
                    x,
                    y,
                    92,
                    "Invert mask",
                    () ->
                            flags(
                                    l -> {
                                        byte[] mask = l.mask();
                                        for (int i = 0; i < mask.length; i++)
                                            mask[i] = (byte) (255 - (mask[i] & 255));
                                        return l.withMask(mask);
                                    }));
            button(
                    x + 98,
                    y,
                    94,
                    "Remove mask",
                    () -> {
                        flags(l -> l.withMask(new byte[0]));
                        maskEdit = false;
                        revision++;
                        rebuildWidgets();
                    });
            message =
                    "Alpha lock preserves transparency · Clip uses the layer immediately below ·"
                        + " White mask reveals, black hides";
        }
    }

    private int colorMode;

    private LoomButton button(int x, int y, int w, String label, Runnable action) {
        var b = addRenderableWidget(new LoomButton(x, y, w, 22, Component.literal(label), action));
        b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(label)));
        return b;
    }

    private void flags(UnaryOperator<LoomLayer> edit) {
        ClientProjectWorkspace.apply(
                p -> {
                    var c = wing ? p.elytra() : p.cape();
                    var next = c.replaceLayer(id, edit.apply(layer()));
                    return wing ? p.withElytra(next) : p.withCape(next);
                });
        revision++;
        rebuildWidgets();
    }

    private void change(UnaryOperator<PixelPatch> edit) {
        if (!layer().editableAsPaint()) {
            message = "Select an unlocked Paint layer for pixel edits";
            return;
        }
        PixelPatch next = edit.apply(patch());
        int[] data = next.data(), map = mapping();
        ClientProjectWorkspace.apply(
                p -> {
                    var c = wing ? p.elytra() : p.cape();
                    var l =
                            c.layers().stream()
                                    .filter(v -> v.id().equals(id))
                                    .findFirst()
                                    .orElseThrow();
                    int[] pixels = l.pixels();
                    for (int i = 0; i < map.length; i++) if (map[i] >= 0) pixels[map[i]] = data[i];
                    var edited = c.replaceLayer(id, l.withPixels(pixels));
                    return wing ? p.withElytra(edited) : p.withCape(edited);
                });
        revision++;
    }

    private void click(int x, int y) {
        if (layer().locked()) {
            message = "Unlock this layer before editing";
            return;
        }
        var p = patch();
        if (mode == 0) {
            selection = ColorSelection.select(p, x, y, tolerance, contiguous);
            message = selection.cardinality() + " pixels selected · Replace preserves alpha";
            revision++;
        } else if (mode == 1) change(v -> stamp.apply(v, x, y, brushSize, color, selection));
        else if (maskEdit) {
            int[] map = mapping();
            byte[] mask = layer().mask();
            if (mask.length == 0) return;
            var surface = new PixelPatch(p.width(), p.height(), new int[map.length]);
            int[] hit =
                    BrushStamp.CIRCLE.apply(surface, x, y, brushSize, 0xFFFFFFFF, selection).data();
            for (int i = 0; i < hit.length; i++)
                if (hit[i] != 0 && map[i] >= 0) mask[map[i]] = (byte) colorMode;
            ClientProjectWorkspace.apply(
                    project -> {
                        var c = wing ? project.elytra() : project.cape();
                        var next = c.replaceLayer(id, layer().withMask(mask));
                        return wing ? project.withElytra(next) : project.withCape(next);
                    });
            revision++;
        }
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent e) {
        if (e.hasControlDownWithQuirk() && e.key() == 90) {
            ClientProjectWorkspace.undo();
            revision++;
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    public void render(GuiGraphics g, int x, int y, float dt) {
        LoomScreenChrome.renderBackdrop(g, width, height);
        LoomScreenChrome.renderBrandHeader(
                g, width, "Surface tools · " + layer().name(), LoomUiTheme.compact(width, height));
        super.render(g, x, y, dt);
        String error = ClientProjectWorkspace.session().editError();
        LoomScreenChrome.footer(
                g,
                width,
                height,
                font.plainSubstrByWidth(error == null ? message : error, width - 70),
                "Local");
    }

    @Override
    public void removed() {
        if (image != null) image.close();
        ClientProjectWorkspace.endCompoundEdit();
        super.removed();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
