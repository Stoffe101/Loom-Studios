package dev.loomstudios.ui;

import dev.loomstudios.project.PixelSelection;

/** Integer-snapped pixel boundaries shared by drawing, overlays and input. */
public record CanvasViewportTransform(
        int left, int top, int columns, int rows, int pixelScale,
        int clipLeft, int clipTop, int clipRight, int clipBottom
) {
    public CanvasViewportTransform {
        if (columns < 1 || rows < 1 || pixelScale < 1
                || clipRight <= clipLeft || clipBottom <= clipTop) {
            throw new IllegalArgumentException("Invalid canvas viewport");
        }
    }

    public static CanvasViewportTransform fit(
            int clipLeft, int clipTop, int clipRight, int clipBottom,
            int columns, int rows, float zoom, int panX, int panY
    ) {
        int width = clipRight - clipLeft;
        int height = clipBottom - clipTop;
        int fit = Math.max(1, Math.min(width / columns, height / rows));
        int cell = Math.max(1, Math.round(fit * zoom));
        int drawWidth = columns * cell;
        int drawHeight = rows * cell;
        int maxX = Math.max(0, (drawWidth - width + 1) / 2);
        int maxY = Math.max(0, (drawHeight - height + 1) / 2);
        return new CanvasViewportTransform(
                clipLeft + (width - drawWidth) / 2 + Math.max(-maxX, Math.min(maxX, panX)),
                clipTop + (height - drawHeight) / 2 + Math.max(-maxY, Math.min(maxY, panY)),
                columns, rows, cell, clipLeft, clipTop, clipRight, clipBottom
        );
    }

    public int drawWidth() { return columns * pixelScale; }
    public int drawHeight() { return rows * pixelScale; }
    public int screenX(int boundary) { return left + boundary * pixelScale; }
    public int screenY(int boundary) { return top + boundary * pixelScale; }

    /** Right and bottom are exclusive. The last cell cannot spill into its neighbour. */
    public Rect selection(PixelSelection selection) {
        return new Rect(screenX(selection.minX()), screenY(selection.minY()),
                screenX(selection.maxX() + 1), screenY(selection.maxY() + 1));
    }

    public int[] pixelAt(double x, double y) {
        if (x < clipLeft || y < clipTop || x >= clipRight || y >= clipBottom
                || x < left || y < top || x >= screenX(columns) || y >= screenY(rows)) {
            return null;
        }
        return new int[]{(int)((x - left) / pixelScale), (int)((y - top) / pixelScale)};
    }

    public record Rect(int left, int top, int right, int bottom) { }
}
