package dev.loomstudios.project;

public enum CanvasResolution {
    STANDARD(1, "1x"),
    HIGH(2, "2x"),
    ULTRA(4, "4x"),
    DETAIL(6, "6x"),
    MAXIMUM(8, "8x");

    private final int scale;
    private final String label;

    CanvasResolution(int scale, String label) {
        this.scale = scale;
        this.label = label;
    }

    public int scale() {
        return scale;
    }

    public String label() {
        return label;
    }

    public int width() {
        return LoomProject.TEXTURE_WIDTH * scale;
    }

    public int height() {
        return LoomProject.TEXTURE_HEIGHT * scale;
    }

    public static CanvasResolution fromCanvas(LoomCanvas canvas) {
        return fromDimensions(canvas.width(), canvas.height());
    }

    public static CanvasResolution fromDimensions(int width, int height) {
        for (CanvasResolution resolution : values()) {
            if (resolution.width() == width && resolution.height() == height) {
                return resolution;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported Loom canvas resolution " + width + "x" + height
        );
    }

    public CanvasResolution higher() {
        CanvasResolution[] values = values();
        return values[Math.min(values.length - 1, ordinal() + 1)];
    }

    public CanvasResolution lower() {
        CanvasResolution[] values = values();
        return values[Math.max(0, ordinal() - 1)];
    }
}
