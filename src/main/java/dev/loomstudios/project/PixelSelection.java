package dev.loomstudios.project;

/**
 * Inclusive pixel selection inside one semantic editing surface.
 *
 * <p>The selection itself is editor/session state rather than project data.
 * ProjectEdits validates it against the active semantic region before applying
 * destructive pixel transforms.</p>
 */
public record PixelSelection(
        int minX,
        int minY,
        int maxX,
        int maxY
) {
    public PixelSelection {
        if (minX < 0 || minY < 0 || maxX < minX || maxY < minY) {
            throw new IllegalArgumentException("Invalid pixel selection");
        }
    }

    public static PixelSelection between(
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        return new PixelSelection(
                Math.min(startX, endX),
                Math.min(startY, endY),
                Math.max(startX, endX),
                Math.max(startY, endY)
        );
    }

    public int width() {
        return maxX - minX + 1;
    }

    public int height() {
        return maxY - minY + 1;
    }

    public boolean contains(int x, int y) {
        return x >= minX
                && x <= maxX
                && y >= minY
                && y <= maxY;
    }
}
