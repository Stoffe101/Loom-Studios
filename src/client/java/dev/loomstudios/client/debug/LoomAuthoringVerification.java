package dev.loomstudios.client.debug;

import dev.loomstudios.client.palette.ColorPaletteLibrary;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.render.RuntimeCosmeticCache;
import dev.loomstudios.client.screen.*;
import dev.loomstudios.client.ui.LoomImagePreviewWidget;
import dev.loomstudios.image.*;
import dev.loomstudios.project.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;

import java.lang.reflect.Field;
import java.util.*;

/** Opt-in capture assertions exercise real Screen event routing and uploaded GIF textures. */
final class LoomAuthoringVerification {
    private LoomAuthoringVerification() {}

    static void verify(Minecraft client) throws Exception {
        var project = LoomProjectFactory.blank("Authoring input", 1);
        var layer = project.cape().layers().getFirst();
        var pixels = layer.pixels();
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 10; x++) pixels[(y + 1) * 64 + x + 1] = 0xFF888888;
        project =
                project.withCape(project.cape().replaceLayer(layer.id(), layer.withPixels(pixels)));
        ClientProjectWorkspace.replaceWith(project, client.player.getUUID());
        var home = new LoomHomeScreen();
        var tools =
                new LoomSurfaceToolsScreen(
                        home, false, layer.id(), CapeUvRegion.OUTSIDE, null, null, 0xFF22D7E8);
        client.setScreen(tools);
        press(tools, "Brushes");
        gesture(tools, "image", 10, 16, 5, 8, true);
        if (Arrays.equals(
                        pixels,
                        ClientProjectWorkspace.project().cape().layers().getFirst().pixels())
                || ClientProjectWorkspace.session().isCompoundEditActive())
            throw new IllegalStateException("Stamp gesture failed to commit");
        ClientProjectWorkspace.undo();
        if (!Arrays.equals(
                pixels, ClientProjectWorkspace.project().cape().layers().getFirst().pixels()))
            throw new IllegalStateException("Stamp drag did not undo as one edit");
        press(tools, "Wand");
        gesture(tools, "image", 10, 16, 5, 8, false);
        if (((BitSet) get(tools, "selection")).isEmpty())
            throw new IllegalStateException("Wand Screen input selected nothing");
        press(tools, "Replace with selected color");
        if (ClientProjectWorkspace.project().cape().layers().getFirst().pixelAt(9 * 64 + 6)
                != 0xFF22D7E8)
            throw new IllegalStateException("Replace Color action did not recolor selection");
        press(tools, "Clear");
        press(tools, "Masks");
        press(tools, "Alpha lock: Off");
        press(tools, "Clip below: Off");
        var flags = ClientProjectWorkspace.project().cape().layers().getFirst();
        if (!flags.alphaLocked() || !flags.clipToBelow())
            throw new IllegalStateException("Mask flags did not persist through Screen input");
        press(tools, "Edit mask");
        press(tools, "Hide");
        gesture(tools, "image", 10, 16, 5, 8, false);
        if (ClientProjectWorkspace.project().cape().layers().getFirst().maskAt(9 * 64 + 6) != 0)
            throw new IllegalStateException("Mask painting did not hide the selected pixels");
        press(tools, "Back");
        if (client.screen != home)
            throw new IllegalStateException("Surface tools did not return to parent");

        var source =
                new PixelImage(2, 2, new int[] {0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFF000000});
        var effects =
                new LoomImageEffectsScreen(
                        home, source, ImageProcessingSettings.defaults(), value -> {});
        client.setScreen(effects);
        gesture(effects, "before", 2, 2, 0, 0, false);
        var settings = (ImageProcessingSettings) get(effects, "settings");
        var processed = ImageProcessingPipeline.apply(source, settings);
        if ((processed.pixelAt(0, 0) >>> 24) != 0 || (processed.pixelAt(1, 0) >>> 24) != 255)
            throw new IllegalStateException(
                    "Picked background did not remove only connected matching pixels");
        int paletteCount = ColorPaletteLibrary.palettes().size();
        press(effects, "Create Swatches from Image");
        if (ColorPaletteLibrary.palettes().size() != paletteCount + 1
                || !ColorPaletteLibrary.selected().orElseThrow().name().equals("Image colors")
                || ColorPaletteLibrary.selected().orElseThrow().colors().isEmpty())
            throw new IllegalStateException("Image swatches were not saved and selected");

        var gif = LoomProjectFactory.blank("GIF textures", 1);
        var red = new PixelImage(1, 1, new int[] {0xFFFF0000});
        var blue = new PixelImage(1, 1, new int[] {0xFF0000FF});
        var data =
                ImageLayerData.placed(
                                red,
                                64,
                                32,
                                new NormalizedRect(0, 0, 1, 1),
                                ImagePlacementMode.STRETCH)
                        .withFrames(List.of(red, blue), List.of(2, 3));
        gif = ProjectEdits.addCapeImageLayer(gif, "GIF cape", data);
        gif = ProjectEdits.addElytraImageLayer(gif, "GIF wings", data);
        String key = "authoring-gif-" + gif.hash();
        var bundle = RuntimeCosmeticCache.getOrCompile(client, key, gif, 0, false);
        var cape = (com.mojang.blaze3d.platform.NativeImage) get(bundle, "capeImage");
        var wings = (com.mojang.blaze3d.platform.NativeImage) get(bundle, "elytraImage");
        if (cape.getPixel(1, 1) != 0xFFFF0000 || wings.getPixel(14, 2) != 0xFFFF0000)
            throw new IllegalStateException("First GIF frame was not uploaded");
        RuntimeCosmeticCache.getOrCompile(client, key, gif, 2, false);
        if (cape.getPixel(1, 1) != 0xFF0000FF || wings.getPixel(14, 2) != 0xFF0000FF)
            throw new IllegalStateException("Next GIF frame was not uploaded on both channels");
        RuntimeCosmeticCache.release(client, key);
        System.out.println(
                "LOOM_AUTHORING_INPUT PASS: real stamp drag/single undo, wand/replace, mask"
                    + " flags/painting, parent return, picked background/swatches and Cape/Elytra"
                    + " GIF texture updates");
    }

    private static Object get(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static MouseButtonEvent mouse(double x, double y) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
    }

    private static void press(Screen screen, String text) {
        var button =
                screen.children().stream()
                        .filter(
                                c ->
                                        c instanceof AbstractWidget w
                                                && w.visible
                                                && w.active
                                                && w.getMessage().getString().equals(text))
                        .map(c -> (AbstractWidget) c)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Missing authoring button: " + text));
        screen.mouseClicked(mouse(button.getX() + 3, button.getY() + 3), false);
        screen.mouseReleased(mouse(button.getX() + 3, button.getY() + 3));
    }

    private static void gesture(
            Screen screen, String widget, int w, int h, int px, int py, boolean drag)
            throws Exception {
        var image = (LoomImagePreviewWidget) get(screen, widget);
        int left = image.getX() + 6, top = image.getY() + 24;
        // Deterministic fitted geometry before the next render; routes through the actual Screen.
        set(image, "imageLeft", left);
        set(image, "imageTop", top);
        set(image, "drawWidth", w * 2);
        set(image, "drawHeight", h * 2);
        double x = left + (px + .5) * 2, y = top + (py + .5) * 2;
        screen.mouseClicked(mouse(x, y), false);
        if (drag) screen.mouseDragged(mouse(x + 2, y), 2, 0);
        screen.mouseReleased(mouse(x + (drag ? 2 : 0), y));
    }
}
