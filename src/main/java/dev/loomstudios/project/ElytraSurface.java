package dev.loomstudios.project;

/** Full box UVs: each wing owns a distinct 24×22 block in the 64×32 atlas. */
public enum ElytraSurface {
    OUTSIDE("Outside", 2, 2, 10, 20),
    INSIDE("Inside", 14, 2, 10, 20),
    LEFT_EDGE("Left edge", 0, 2, 2, 20),
    RIGHT_EDGE("Right edge", 12, 2, 2, 20),
    TOP("Top", 2, 0, 10, 2),
    BOTTOM("Bottom", 12, 0, 10, 2);
    private final String label;
    private final int x, y, w, h;

    ElytraSurface(String label, int x, int y, int w, int h) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String label() {
        return label;
    }

    public int width(int scale) {
        return w * scale;
    }

    public int height(int scale) {
        return h * scale;
    }

    public int atlasX(ElytraWing wing, int local, int scale) {
        return (wing == ElytraWing.LEFT ? 24 + x : x) * scale + local;
    }

    public int atlasY(int local, int scale) {
        return y * scale + local;
    }

    public NormalizedRect normalizedRect(LoomCanvas canvas, ElytraWing wing) {
        int s = canvas.width() / 64;
        return new NormalizedRect(
                atlasX(wing, 0, s) / (double) canvas.width(),
                atlasY(0, s) / (double) canvas.height(),
                width(s) / (double) canvas.width(),
                height(s) / (double) canvas.height());
    }
}
