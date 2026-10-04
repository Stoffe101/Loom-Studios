package dev.loomstudios.project;

import dev.loomstudios.image.BackgroundRemoval;

import java.util.*;

/** Exact per-pixel selection; disconnected color matches never become a bounding rectangle. */
public final class ColorSelection {
    private ColorSelection() {}

    public static BitSet select(PixelPatch image, int x, int y, int tolerance, boolean contiguous) {
        int w = image.width(), h = image.height();
        if (x < 0 || y < 0 || x >= w || y >= h)
            throw new IllegalArgumentException("Selection outside surface");
        int[] pixels = image.data();
        int target = pixels[y * w + x];
        BitSet result = new BitSet(pixels.length);
        if (!contiguous) {
            for (int i = 0; i < pixels.length; i++)
                if (match(pixels[i], target, tolerance)) result.set(i);
            return result;
        }
        BitSet visited = new BitSet(pixels.length);
        ArrayDeque<Integer> todo = new ArrayDeque<>();
        todo.add(y * w + x);
        visited.set(y * w + x);
        while (!todo.isEmpty()) {
            int i = todo.removeFirst();
            if (!match(pixels[i], target, tolerance)) continue;
            result.set(i);
            int px = i % w, py = i / w;
            int[] n = {
                px > 0 ? i - 1 : -1,
                px + 1 < w ? i + 1 : -1,
                py > 0 ? i - w : -1,
                py + 1 < h ? i + w : -1
            };
            for (int j : n)
                if (j >= 0 && !visited.get(j)) {
                    visited.set(j);
                    todo.add(j);
                }
        }
        return result;
    }

    private static boolean match(int a, int b, int tolerance) {
        return Math.abs((a >>> 24) - (b >>> 24)) <= tolerance
                && BackgroundRemoval.matches(a, b, tolerance);
    }

    public static PixelPatch replace(
            PixelPatch image, BitSet selection, int color, boolean preserveAlpha) {
        int[] p = image.data();
        for (int i = selection.nextSetBit(0);
                i >= 0 && i < p.length;
                i = selection.nextSetBit(i + 1))
            p[i] = preserveAlpha ? (p[i] & 0xFF000000) | (color & 0xFFFFFF) : color;
        return new PixelPatch(image.width(), image.height(), p);
    }
}
