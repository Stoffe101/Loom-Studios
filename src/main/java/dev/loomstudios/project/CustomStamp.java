package dev.loomstudios.project;

import java.util.*;

/** Immutable colored stamp. Transparent pixels leave artwork untouched. */
public record CustomStamp(UUID id, String name, PixelPatch patch, boolean favorite) {
  public CustomStamp {
    Objects.requireNonNull(id);
    if (name == null
        || name.isBlank()
        || name.length() > 64
        || patch.width() > 128
        || patch.height() > 128)
      throw new IllegalArgumentException("Stamp must be at most128px with a1–64 character name");
  }

  public CustomStamp withFavorite(boolean v) {
    return new CustomStamp(id, name, patch, v);
  }

  public static PixelPatch selection(PixelPatch source, BitSet selected) {
    if (selected == null || selected.isEmpty())
      throw new IllegalArgumentException("Select artwork with Wand first");
    int left = source.width(), top = source.height(), right = 0, bottom = 0;
    for (int i = selected.nextSetBit(0);
        i >= 0 && i < source.width() * source.height();
        i = selected.nextSetBit(i + 1)) {
      left = Math.min(left, i % source.width());
      right = Math.max(right, i % source.width());
      top = Math.min(top, i / source.width());
      bottom = Math.max(bottom, i / source.width());
    }
    if (left > right) throw new IllegalArgumentException("Selection outside canvas");
    int[] pixels = new int[(right - left + 1) * (bottom - top + 1)], original = source.data();
    for (int y = top; y <= bottom; y++)
      for (int x = left; x <= right; x++)
        if (selected.get(y * source.width() + x))
          pixels[(y - top) * (right - left + 1) + x - left] = original[y * source.width() + x];
    return new PixelPatch(right - left + 1, bottom - top + 1, pixels);
  }

  public PixelPatch apply(
      PixelPatch source,
      int cx,
      int cy,
      int size,
      int turns,
      boolean mirror,
      boolean recolor,
      int color,
      BitSet selected) {
    PixelPatch p = patch;
    for (int i = 0; i < Math.floorMod(turns, 4); i++) p = p.rotate();
    if (mirror) p = p.flip(true);
    int w = Math.max(1, size), h = Math.max(1, Math.round(size * p.height() / (float) p.width()));
    int[] src = p.data(), dst = source.data();
    for (int y = 0; y < h; y++)
      for (int x = 0; x < w; x++) {
        int tx = cx - w / 2 + x, ty = cy - h / 2 + y;
        if (tx < 0 || ty < 0 || tx >= source.width() || ty >= source.height()) continue;
        int i = ty * source.width() + tx;
        if (selected != null && !selected.get(i)) continue;
        int pixel =
            src[
                Math.min(p.height() - 1, y * p.height() / h) * p.width()
                    + Math.min(p.width() - 1, x * p.width() / w)];
        if ((pixel >>> 24) == 0) continue;
        if (recolor) pixel = (pixel & 0xFF000000) | (color & 0xFFFFFF);
        dst[i] = EditableFrames.over(dst[i], pixel);
      }
    return new PixelPatch(source.width(), source.height(), dst);
  }
}
