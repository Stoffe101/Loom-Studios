package dev.loomstudios.client.debug;

import dev.loomstudios.client.project.*;
import dev.loomstudios.client.screen.*;
import dev.loomstudios.image.*;
import dev.loomstudios.project.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.*;

/** Actual Screen checks for the four new authoring workflows. Runs only from opt-in capture. */
final class LoomCreativeVerification {
  static void verify(Minecraft client) throws Exception {
    var p = LoomProjectFactory.blank("Creative input", 1);
    var layer = p.cape().layers().getFirst();
    int[] pixels = layer.pixels();
    for (int y = 0; y < 16; y++)
      for (int x = 0; x < 10; x++) pixels[(y + 1) * 64 + x + 1] = 0xFF777777;
    p = p.withCape(p.cape().replaceLayer(layer.id(), layer.withPixels(pixels)));
    ClientProjectWorkspace.replaceWith(p, client.player.getUUID());
    var home = new LoomHomeScreen();
    var bits = new BitSet();
    bits.set(0, 160);
    var stamps =
        new LoomCreativeAssetsScreen(
                home, false, layer.id(), CapeUvRegion.OUTSIDE, null, null, 0xFF22D7E8, bits)
            .openTab(2);
    client.setScreen(stamps);
    LoomAuthoringVerification.press(stamps, "Create Stamp from Selection");
    LoomAuthoringVerification.press(stamps, "Favorite");
    var saved = EditorOverlayState.STORE.stamps();
    if (saved.stream().noneMatch(s -> s.name().equals("My stamp") && s.favorite()))
      throw new IllegalStateException("Custom stamp/favorite not saved");
    LoomAuthoringVerification.gesture(stamps, "image", 10, 16, 5, 8, true);
    if (Arrays.equals(pixels, ClientProjectWorkspace.project().cape().layers().getFirst().pixels()))
      throw new IllegalStateException("Custom stamp input did nothing");
    ClientProjectWorkspace.undo();
    if (!Arrays.equals(
        pixels, ClientProjectWorkspace.project().cape().layers().getFirst().pixels()))
      throw new IllegalStateException("Custom stamp gesture did not undo once");
    String hash = ClientProjectWorkspace.project().hash();
    var source = new PixelImage(1, 1, new int[] {0xFFFFFFFF});
    var guide =
        new ReferenceImage(
            UUID.randomUUID(),
            "Reference input",
            AnimationChannel.CAPE,
            ImageLayerData.placed(
                source,
                64,
                32,
                new NormalizedRect(1 / 64.0, 1 / 32.0, 10 / 64.0, 16 / 32.0),
                ImagePlacementMode.STRETCH),
            .5f,
            true,
            true,
            true);
    EditorOverlayState.references(p.projectId(), List.of(guide));
    var refs =
        new LoomCreativeAssetsScreen(
            home, false, layer.id(), CapeUvRegion.OUTSIDE, null, null, 0xFF22D7E8, null);
    client.setScreen(refs);
    LoomAuthoringVerification.press(refs, "Hide guide");
    if (EditorOverlayState.references(p.projectId()).getFirst().visible()
        || !hash.equals(ClientProjectWorkspace.project().hash()))
      throw new IllegalStateException("Reference affected exported artwork or visibility failed");
    LoomAuthoringVerification.press(refs, "Locked · Unlock");
    LoomAuthoringVerification.press(refs, "Move left");
    if (EditorOverlayState.references(p.projectId())
        .getFirst()
        .image()
        .transform()
        .equals(guide.image().transform()))
      throw new IllegalStateException("Reference transform control did not move");
    var red = new PixelImage(1, 1, new int[] {0xFFFF0000});
    var blue = new PixelImage(1, 1, new int[] {0xFF0000FF});
    var data =
        ImageLayerData.placed(
                red,
                64,
                32,
                new NormalizedRect(1 / 64.0, 1 / 32.0, 10 / 64.0, 16 / 32.0),
                ImagePlacementMode.STRETCH)
            .withFrames(List.of(red, blue), List.of(2, 3));
    ClientProjectWorkspace.apply(project -> ProjectEdits.addCapeImageLayer(project, "GIF", data));
    UUID gif = ClientProjectWorkspace.project().cape().layers().getLast().id();
    var frames =
        new LoomCreativeAssetsScreen(
                home, false, gif, CapeUvRegion.OUTSIDE, null, null, 0xFF22D7E8, null)
            .openTab(1);
    client.setScreen(frames);
    LoomAuthoringVerification.press(frames, "Convert GIF to Editable Animation");
    LoomAuthoringVerification.press(frames, "Duplicate");
    if (ClientProjectWorkspace.project().cape().layers().getLast().imageData().frames().size() != 3)
      throw new IllegalStateException("Frame duplication failed");
    LoomAuthoringVerification.press(frames, "Move later");
    LoomAuthoringVerification.press(frames, "Delete");
    var before = ClientProjectWorkspace.project();
    LoomAuthoringVerification.gesture(frames, "image", 10, 16, 5, 8, true);
    var edited = ClientProjectWorkspace.project().cape().layers().getLast().imageData();
    if (edited.frames().get(1).pixelAt(5, 8) != 0xFF22D7E8)
      throw new IllegalStateException("Editable frame drawing failed");
    ClientProjectWorkspace.undo();
    if (!before.equals(ClientProjectWorkspace.project()))
      throw new IllegalStateException("Frame gesture did not undo once");
    ClientProjectWorkspace.apply(
        project ->
            project.withAnimation(
                AnimationAuthoring.addTrack(
                    project.animation(),
                    layer.id(),
                    AnimationChannel.CAPE,
                    AnimationEffectType.SCROLL)));
    var initialTrack = ClientProjectWorkspace.project().animation().tracks().getLast();
    ClientProjectWorkspace.apply(
        project ->
            project.withAnimation(
                AnimationAuthoring.replaceTrack(
                    project.animation(),
                    AnimationKeyEditing.addLane(initialTrack, AnimationParameter.DISTANCE, 80))));
    var track = ClientProjectWorkspace.project().animation().tracks().getLast();
    var lanes = new LoomParameterAnimationScreen(home, AnimationChannel.CAPE, track.id(), 20);
    client.setScreen(lanes);
    int left = callInt(lanes, "laneLeft"),
        top = (int) LoomAuthoringVerification.get(lanes, "timelineTop");
    var original = ClientProjectWorkspace.project().animation();
    lanes.mouseClicked(mouse(left, top + 24 + 20 + 10), false);
    lanes.mouseDragged(mouse(left + 20, top + 24 + 20 + 10), 20, 0);
    lanes.mouseReleased(mouse(left + 20, top + 24 + 20 + 10));
    if (original.equals(ClientProjectWorkspace.project().animation()))
      throw new IllegalStateException("Parameter workspace key drag failed");
    ClientProjectWorkspace.undo();
    if (!original.equals(ClientProjectWorkspace.project().animation()))
      throw new IllegalStateException("Lane key drag did not undo once");
    client.setScreen(lanes);
    LoomAuthoringVerification.press(lanes, "Select lane");
    LoomAuthoringVerification.press(lanes, "Copy keys");
    var clipboard = (List<?>) LoomAuthoringVerification.get(lanes, "clipboard");
    if (clipboard.size() < 2) throw new IllegalStateException("Multiple keys were not copied");
    EditorOverlayState.references(p.projectId(), List.of());
    EditorOverlayState.onion(0, .3f);
    EditorOverlayState.time(0, 0, 1);
    System.out.println(
        "LOOM_CREATIVE_INPUT PASS: persisted custom stamps/favorites, one-Undo stamp and frame"
            + " drags, editor-only guide visibility/unlock/move, GIF"
            + " conversion/duplicate/reorder/delete, lane drag/undo and multi-key clipboard");
  }

  private static int callInt(Object target, String name) throws Exception {
    var m = target.getClass().getDeclaredMethod(name);
    m.setAccessible(true);
    return (int) m.invoke(target);
  }

  private static MouseButtonEvent mouse(double x, double y) {
    return new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
  }

  private LoomCreativeVerification() {}
}
