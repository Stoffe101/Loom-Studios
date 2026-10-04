package dev.loomstudios.client.project;

import dev.loomstudios.image.*;
import dev.loomstudios.project.*;
import java.io.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Only editor widgets call this; texture/equip/export paths never see reference or ghost pixels.
 */
public final class EditorOverlayState {
  public static final EditorAssetStore STORE =
      new EditorAssetStore(
          FabricLoader.getInstance().getConfigDir().resolve("loom-studios/editor-assets"));
  private static UUID project;
  private static List<ReferenceImage> references = List.of();
  private static int onion, tick, previousTick, nextTick = 1;
  private static float opacity = .3f;
  private static long revision;

  public static long revision() {
    return revision;
  }

  public static int onion() {
    return onion;
  }

  public static float opacity() {
    return opacity;
  }

  public static int tick() {
    return tick;
  }

  public static void onion(int mode, float alpha) {
    onion = mode;
    opacity = alpha;
    revision++;
  }

  public static void time(int value, int previous, int next) {
    tick = value;
    previousTick = previous;
    nextTick = next;
    revision++;
  }

  public static List<ReferenceImage> references(UUID id) {
    if (!id.equals(project)) {
      project = id;
      try {
        references = STORE.references(id);
      } catch (IOException e) {
        references = List.of();
        dev.loomstudios.LoomStudios.LOGGER.warn("Unable to read editor references", e);
      }
      onion = 0;
      tick = 0;
      previousTick = 0;
      nextTick = 1;
      revision++;
    }
    return references;
  }

  public static void references(UUID id, List<ReferenceImage> value) throws IOException {
    STORE.references(id, value);
    project = id;
    references = List.copyOf(value);
    revision++;
  }

  public static int[] composite(LoomProject p, AnimationChannel channel) {
    var refs = references(p.projectId());
    var canvas = channel == AnimationChannel.CAPE ? p.cape() : p.elytra();
    int w = canvas.width(), h = canvas.height();
    int[] art = TextureCompositor.compileAnimated(p, channel, tick, tick, false),
        out = new int[w * h];
    for (var r : refs)
      if (r.channel() == channel && r.visible() && !r.above())
        draw(out, r.raster(w, h), r.opacity());
    if (onion > 0) {
      int[]
          previous =
              (onion == 1 || onion == 3)
                  ? TextureCompositor.compileAnimated(p, channel, previousTick, previousTick, false)
                  : null,
          next =
              (onion == 2 || onion == 3)
                  ? TextureCompositor.compileAnimated(p, channel, nextTick, nextTick, false)
                  : null;
      for (int i = 0; i < out.length; i++) {
        if (previous != null)
          out[i] =
              EditableFrames.over(out[i], EditableFrames.tint(previous[i], 0xFFFF66AF, opacity));
        if (next != null)
          out[i] = EditableFrames.over(out[i], EditableFrames.tint(next[i], 0xFF45DDE8, opacity));
      }
    }
    draw(out, art, 1);
    for (var r : refs)
      if (r.channel() == channel && r.visible() && r.above())
        draw(out, r.raster(w, h), r.opacity());
    return out;
  }

  private static void draw(int[] out, int[] pixels, float opacity) {
    for (int i = 0; i < out.length; i++) {
      int c = pixels[i];
      c = Math.round((c >>> 24) * opacity) << 24 | (c & 0xFFFFFF);
      out[i] = EditableFrames.over(out[i], c);
    }
  }

  private EditorOverlayState() {}
}
