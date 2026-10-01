package dev.loomstudios.project;

/**
 * Conventional Minecraft Java 64x32 cape UV regions.
 *
 * <p>Coordinates are atlas pixels. OUTSIDE is the 10x16 face normally seen
 * from behind the player.</p>
 */
public enum CapeUvRegion {
    OUTSIDE("Outside / Back", 1, 1, 10, 16),
    INSIDE("Inside", 12, 1, 10, 16),
    LEFT_EDGE("Left Edge", 0, 1, 1, 16),
    RIGHT_EDGE("Right Edge", 11, 1, 1, 16),
    TOP("Top", 1, 0, 10, 1),
    BOTTOM("Bottom", 11, 0, 10, 1);

    private final String displayName;
    private final int textureX;
    private final int textureY;
    private final int width;
    private final int height;

    CapeUvRegion(
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

    public int textureX() {
        return textureX;
    }

    public int textureY() {
        return textureY;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int width(int scale) {
        return width * scale;
    }

    public int height(int scale) {
        return height * scale;
    }

    public int atlasX(int localX) {
        return textureX + localX;
    }

    public int atlasY(int localY) {
        return textureY + localY;
    }

    public int atlasX(int localX, int scale) {
        return textureX * scale + localX;
    }

    public int atlasY(int localY, int scale) {
        return textureY * scale + localY;
    }

    public CapeUvRegion next() {
        CapeUvRegion[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
