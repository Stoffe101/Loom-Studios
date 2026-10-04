package dev.loomstudios.image;

import java.util.ArrayDeque;

/** Local, deterministic RGB color removal. Tolerance is the maximum channel distance. */
public record BackgroundRemoval(
        boolean enabled, int color, int tolerance, boolean contiguous, int seedX, int seedY) {
    public BackgroundRemoval {
        if (tolerance < 0
                || tolerance > 255
                || seedX < 0
                || seedY < 0
                || seedX >= 512
                || seedY >= 512)
            throw new IllegalArgumentException("Invalid background removal settings");
    }

    public static BackgroundRemoval none() {
        return new BackgroundRemoval(false, 0xFFFFFFFF, 24, true, 0, 0);
    }

    public PixelImage apply(PixelImage image) {
        if (!enabled) return image;
        int[] pixels = image.pixels();
        if (!contiguous) {
            for (int i = 0; i < pixels.length; i++)
                if (matches(pixels[i], color, tolerance)) pixels[i] &= 0xFFFFFF;
        } else {
            int x = Math.min(seedX, image.width() - 1),
                    y = Math.min(seedY, image.height() - 1),
                    start = y * image.width() + x;
            var todo = new ArrayDeque<Integer>();
            boolean[] seen = new boolean[pixels.length];
            todo.add(start);
            seen[start] = true;
            while (!todo.isEmpty()) {
                int at = todo.removeFirst();
                if (!matches(pixels[at], color, tolerance)) continue;
                pixels[at] &= 0xFFFFFF;
                int px = at % image.width(), py = at / image.width();
                int[] neighbors = {
                    px > 0 ? at - 1 : -1,
                    px + 1 < image.width() ? at + 1 : -1,
                    py > 0 ? at - image.width() : -1,
                    py + 1 < image.height() ? at + image.width() : -1
                };
                for (int next : neighbors)
                    if (next >= 0 && !seen[next]) {
                        seen[next] = true;
                        todo.add(next);
                    }
            }
        }
        return new PixelImage(image.width(), image.height(), pixels);
    }

    public static boolean matches(int a, int b, int tolerance) {
        return Math.abs((a >>> 16 & 255) - (b >>> 16 & 255)) <= tolerance
                && Math.abs((a >>> 8 & 255) - (b >>> 8 & 255)) <= tolerance
                && Math.abs((a & 255) - (b & 255)) <= tolerance;
    }
}
