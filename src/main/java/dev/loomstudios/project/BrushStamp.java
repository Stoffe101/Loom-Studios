package dev.loomstudios.project;

import java.util.BitSet;

/** Small predictable stamp presets, applied only inside the current exact selection. */
public enum BrushStamp {
    SQUARE("Square"),
    CIRCLE("Circle"),
    CROSS("Cross"),
    DIAMOND("Diamond"),
    STAR("Star");
    private final String label;

    BrushStamp(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public PixelPatch apply(
            PixelPatch source, int cx, int cy, int size, int color, BitSet selection) {
        int[] data = source.data();
        int half = size / 2;
        double radius = Math.max(.5, size / 2.0);
        for (int dy = -half; dy < size - half; dy++)
            for (int dx = -half; dx < size - half; dx++) {
                int x = cx + dx, y = cy + dy;
                if (x < 0 || y < 0 || x >= source.width() || y >= source.height()) continue;
                boolean hit =
                        switch (this) {
                            case SQUARE -> true;
                            case CIRCLE -> dx * dx + dy * dy <= radius * radius;
                            case CROSS -> dx == 0 || dy == 0;
                            case DIAMOND -> Math.abs(dx) + Math.abs(dy) <= radius;
                            case STAR -> dx == 0 || dy == 0 || Math.abs(dx) == Math.abs(dy);
                        };
                int i = y * source.width() + x;
                if (hit && (selection == null || selection.get(i))) data[i] = color;
            }
        return new PixelPatch(source.width(), source.height(), data);
    }
}
