package dev.loomstudios.project;

/** Vanilla v1–v3 shared cuboid at tex(22,0) becomes two independent v4 cuboids. */
public final class LegacyWingUvs {
    private LegacyWingUvs() {}

    public static int[] migrate(int[] source, int width, int height) {
        int scale = width / 64;
        int[] out = source.clone();
        for (int y = 0; y < 22 * scale; y++) {
            System.arraycopy(source, y * width + 22 * scale, out, y * width, 24 * scale);
            System.arraycopy(
                    source, y * width + 22 * scale, out, y * width + 24 * scale, 24 * scale);
        }
        return out;
    }

    public static int widthForPixels(int pixels) {
        return 64 * (int) Math.round(Math.sqrt(pixels / 2048.0));
    }
}
