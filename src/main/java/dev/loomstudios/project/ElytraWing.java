package dev.loomstudios.project;

/**
 * Independent outside faces for Loom schema v4 wing geometry.
 *
 * <p>The two 10x20 wing faces live in a 64x32 atlas. RIGHT is
 * horizontally mirrored in the vanilla UV layout, so linked authoring mirrors
 * local X when copying between wings.</p>
 */
public enum ElytraWing {
    LEFT("Left Wing", 26, 2, 10, 20),
    RIGHT("Right Wing", 2, 2, 10, 20);

    private final String displayName;
    private final int textureX;
    private final int textureY;
    private final int width;
    private final int height;

    ElytraWing(
            String displayName,
            int textureX,
            int textureY,
            int width,
            int height
    ) {
        this.displayName = displayName;
        this.textureX = textureX;
        this.textureY = textureY;
        this.width = width;
        this.height = height;
    }

    public String displayName() {
        return displayName;
    }

    public int width(int scale) {
        return width * scale;
    }

    public int height(int scale) {
        return height * scale;
    }

    public int atlasX(int localX, int scale) {
        return textureX * scale + localX;
    }

    public int atlasY(int localY, int scale) {
        return textureY * scale + localY;
    }

    public int mirroredLocalX(int localX, int scale) {
        return width(scale) - 1 - localX;
    }

    public NormalizedRect normalizedRect(LoomCanvas canvas) {
        int scale = CanvasResolution.fromCanvas(canvas).scale();

        return new NormalizedRect(
                atlasX(0, scale) / (double)canvas.width(),
                atlasY(0, scale) / (double)canvas.height(),
                width(scale) / (double)canvas.width(),
                height(scale) / (double)canvas.height()
        );
    }

    public ElytraWing opposite() {
        return this == LEFT ? RIGHT : LEFT;
    }
}
