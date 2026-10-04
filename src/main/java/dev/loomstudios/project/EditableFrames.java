package dev.loomstudios.project;

import dev.loomstudios.image.*;
import java.util.*;

/** Bakes processed GIF frames to one semantic face; frame edits retain project history. */
public final class EditableFrames {
  public static ImageLayerData convert(
      LoomLayer layer,
      int canvasWidth,
      int canvasHeight,
      int[] mapping,
      int faceWidth,
      int faceHeight,
      NormalizedRect target) {
    if (layer.kind() != LayerKind.IMAGE || layer.imageData().frames().isEmpty())
      throw new IllegalArgumentException("Select an animated Image layer");
    var frames = new ArrayList<PixelImage>();
    int tick = 0;
    for (int i = 0; i < layer.imageData().frames().size(); i++) {
      int[] atlas = LayerRasterizer.rasterize(layer, canvasWidth, canvasHeight, tick),
          pixels = new int[mapping.length];
      for (int j = 0; j < pixels.length; j++) if (mapping[j] >= 0) pixels[j] = atlas[mapping[j]];
      frames.add(new PixelImage(faceWidth, faceHeight, pixels));
      tick += layer.imageData().frameTicks().get(i);
    }
    return ImageLayerData.placed(
            frames.getFirst(), canvasWidth, canvasHeight, target, ImagePlacementMode.STRETCH)
        .withFrames(frames, layer.imageData().frameTicks())
        .withEditableAnimation(true);
  }

  public static ImageLayerData replace(ImageLayerData data, int index, PixelImage image) {
    var frames = new ArrayList<>(data.frames());
    frames.set(index, image);
    return data.withFrames(frames, data.frameTicks());
  }

  public static ImageLayerData duration(ImageLayerData data, int index, int ticks) {
    var times = new ArrayList<>(data.frameTicks());
    times.set(index, ticks);
    return data.withFrames(data.frames(), times);
  }

  public static ImageLayerData duplicate(ImageLayerData data, int index) {
    if (data.frames().size() >= ImageLayerData.MAX_FRAMES)
      throw new IllegalArgumentException("Maximum64 frames");
    var frames = new ArrayList<>(data.frames());
    var times = new ArrayList<>(data.frameTicks());
    frames.add(index + 1, frames.get(index));
    times.add(index + 1, times.get(index));
    return data.withFrames(frames, times);
  }

  public static ImageLayerData delete(ImageLayerData data, int index) {
    if (data.frames().size() <= 1) throw new IllegalArgumentException("Keep at least one frame");
    var frames = new ArrayList<>(data.frames());
    var times = new ArrayList<>(data.frameTicks());
    frames.remove(index);
    times.remove(index);
    return data.withFrames(frames, times);
  }

  public static ImageLayerData move(ImageLayerData data, int index, int destination) {
    if (destination < 0 || destination >= data.frames().size()) return data;
    var frames = new ArrayList<>(data.frames());
    var times = new ArrayList<>(data.frameTicks());
    var f = frames.remove(index);
    var t = times.remove(index);
    frames.add(destination, f);
    times.add(destination, t);
    return data.withFrames(frames, times);
  }

  public static PixelImage onion(ImageLayerData data, int index, int mode, float opacity) {
    return onion(
        data.frames().get(index),
        index > 0 ? data.frames().get(index - 1) : data.frames().getLast(),
        index + 1 < data.frames().size() ? data.frames().get(index + 1) : data.frames().getFirst(),
        mode,
        opacity);
  }

  public static PixelImage onion(
      PixelImage current, PixelImage previous, PixelImage next, int mode, float opacity) {
    int[] p = current.pixels(), prev = previous.pixels(), nxt = next.pixels();
    for (int i = 0; i < p.length; i++) {
      int ghost = 0;
      if (mode == 1 || mode == 3) ghost = tint(prev[i], 0xFFFF66AF, opacity);
      if (mode == 2 || mode == 3) ghost = over(ghost, tint(nxt[i], 0xFF45DDE8, opacity));
      p[i] = over(ghost, p[i]);
    }
    return new PixelImage(current.width(), current.height(), p);
  }

  public static int tint(int color, int tint, float opacity) {
    return Math.round((color >>> 24) * Math.max(0, Math.min(1, opacity))) << 24 | (tint & 0xFFFFFF);
  }

  public static int over(int below, int above) {
    float a = (above >>> 24) / 255f, b = (below >>> 24) / 255f, o = a + b * (1 - a);
    if (o == 0) return 0;
    int c = Math.round(o * 255) << 24;
    for (int shift : new int[] {0, 8, 16})
      c |=
          Math.round((((above >>> shift) & 255) * a + ((below >>> shift) & 255) * b * (1 - a)) / o)
              << shift;
    return c;
  }

  private EditableFrames() {}
}
