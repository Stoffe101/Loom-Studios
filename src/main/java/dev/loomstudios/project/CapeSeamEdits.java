package dev.loomstudios.project;

import java.util.Arrays;

/** Unfolded outside plus its four physical neighbors; corner gaps never write artwork. */
public final class CapeSeamEdits {
    private CapeSeamEdits() {}

    public static int[] mapping(int atlasWidth) {
        int s = atlasWidth / 64, w = 12 * s, h = 18 * s;
        int[] map = new int[w * h];
        Arrays.fill(map, -1);
        place(map, w, atlasWidth, s, CapeUvRegion.OUTSIDE, s, s);
        place(map, w, atlasWidth, s, CapeUvRegion.LEFT_EDGE, 0, s);
        place(map, w, atlasWidth, s, CapeUvRegion.RIGHT_EDGE, 11 * s, s);
        place(map, w, atlasWidth, s, CapeUvRegion.TOP, s, 0);
        place(map, w, atlasWidth, s, CapeUvRegion.BOTTOM, s, 17 * s);
        return map;
    }

    private static void place(
            int[] map, int w, int atlasWidth, int s, CapeUvRegion face, int nx, int ny) {
        for (int y = 0; y < face.height(s); y++)
            for (int x = 0; x < face.width(s); x++)
                map[(ny + y) * w + nx + x] = face.atlasY(y, s) * atlasWidth + face.atlasX(x, s);
    }

    public static PixelPatch read(LoomCanvas canvas, LoomLayer layer) {
        int s = canvas.width() / 64;
        int[] map = mapping(canvas.width()),
                pixels = LayerRasterizer.rasterize(layer, canvas.width(), canvas.height()),
                out = new int[map.length];
        for (int i = 0; i < map.length; i++) if (map[i] >= 0) out[i] = pixels[map[i]];
        return new PixelPatch(12 * s, 18 * s, out);
    }

    public static LoomCanvas write(LoomCanvas canvas, LoomLayer layer, PixelPatch net) {
        if (!layer.editableAsPaint())
            throw new IllegalArgumentException("Select an unlocked Paint layer");
        int[] map = mapping(canvas.width()), p = layer.pixels(), data = net.data();
        if (data.length != map.length)
            throw new IllegalArgumentException("Invalid unfolded surface");
        for (int i = 0; i < map.length; i++) if (map[i] >= 0) p[map[i]] = data[i];
        return canvas.replaceLayer(layer.id(), layer.withPixels(p));
    }
}
