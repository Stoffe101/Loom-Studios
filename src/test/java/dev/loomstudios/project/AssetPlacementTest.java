package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssetPlacementTest {
  private static final PixelPatch STAR = new PixelPatch(3, 3, new int[] {
      0, 0x80FFEABB, 0,
      0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
      0, 0xFFFFEABB, 0
  });

  @Test
  void capeAssetIsIndependentEmbeddedImageLayerAndSurvivesSharing() {
    var original = LoomProjectFactory.blank("Night", 1);
    var p = AssetPlacement.place(original, AnimationChannel.CAPE, "Crescent Star", STAR,
        CapeUvRegion.OUTSIDE, null, null, .5, .5, 5, false, 0);
    assertEquals(original.cape().layers().size() + 1, p.cape().layers().size());
    var asset = p.cape().layers().getLast();
    assertEquals("Crescent Star", asset.name());
    assertEquals(LayerKind.IMAGE, asset.kind());
    assertEquals(0x80FFEABB, asset.imageData().source().pixelAt(1, 0));
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    assertEquals(p, LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(p)));
    assertEquals(original.cape().layers().getFirst(), p.cape().layers().getFirst());
    int[] result = LayerRasterizer.rasterize(asset, 64, 32);
    // Only the outside Cape region may contain placed asset pixels.
    for (int y = 0; y < 32; y++)
      for (int x = 0; x < 64; x++)
        if ((result[y * 64 + x] >>> 24) != 0) {
          assertTrue(x >= 1 && x < 11 && y >= 1 && y < 17, "Bleed at " + x + "," + y);
        }
  }

  @Test
  void placementOnLeftElytraFaceNeverBleedsToRightWing() {
    var p = AssetPlacement.place(LoomProjectFactory.blank("Tree", 1),
        AnimationChannel.ELYTRA, "Pine Tree", STAR,
        null, ElytraWing.LEFT, ElytraSurface.OUTSIDE, .8, .6, 8, true, 0xFF115522);
    var layer = p.elytra().layers().getLast();
    assertEquals(LayerKind.IMAGE, layer.kind());
    assertEquals(0x80115522, layer.imageData().source().pixelAt(1, 0));
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    int[] pixels = LayerRasterizer.rasterize(layer, p.elytra().width(), p.elytra().height());
    int scale = CanvasResolution.fromCanvas(p.elytra()).scale();
    boolean any = false;
    for (int y = 0; y < p.elytra().height(); y++)
      for (int x = 0; x < p.elytra().width(); x++)
        if ((pixels[y * p.elytra().width() + x] >>> 24) != 0) {
          any = true;
          assertTrue(x >= 38 * scale && x < 48 * scale, "Wrong wing X " + x);
          assertTrue(y >= 2 * scale && y < 22 * scale, "Wrong wing Y " + y);
        }
    assertTrue(any, "A placed asset must be visible");
  }

  @Test
  void sourcePixelEditingAndTransformRemainIndependentAndSerializable() {
    var p = AssetPlacement.place(LoomProjectFactory.blank("Editable", 2),
        AnimationChannel.CAPE, "Little Star", STAR,
        CapeUvRegion.OUTSIDE, null, null, .5, .5, 10, false, 0);
    UUID id = p.cape().layers().getLast().id();
    var before = p.cape().layers().getLast().imageData().transform();
    p = AssetPlacement.editPixel(p, AnimationChannel.CAPE, id, 1, 1, 0xFF11AAFF);
    assertEquals(0xFF11AAFF, p.cape().layers().getLast().imageData().source().pixelAt(1, 1));
    assertEquals(before, p.cape().layers().getLast().imageData().transform());
    var moved = before.withCenter(before.centerX() + .01, before.centerY() + .01)
        .withRotation(45);
    p = AssetPlacement.transform(p, AnimationChannel.CAPE, id, moved);
    assertEquals(moved, p.cape().layers().getLast().imageData().transform());
    assertEquals(0xFF11AAFF, p.cape().layers().getLast().imageData().source().pixelAt(1, 1));
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    assertThrows(IllegalArgumentException.class,
        () -> AssetPlacement.editPixel(LoomProjectFactory.blank("Empty",1),
            AnimationChannel.CAPE, id, 0, 0, 0));
  }

  @Test
  void invalidPositionAndSourcePixelBoundsRejectWithoutMutatingProject() {
    var p = LoomProjectFactory.blank("Safe", 1);
    assertThrows(IllegalArgumentException.class, () -> AssetPlacement.place(p,
        AnimationChannel.CAPE, "Oops", STAR,
        CapeUvRegion.OUTSIDE, null, null, Double.NaN, .5, 3, false, 0));
    assertThrows(IllegalArgumentException.class, () -> AssetPlacement.place(p,
        AnimationChannel.CAPE, "Oops", STAR,
        CapeUvRegion.OUTSIDE, null, null, -.01, .5, 3, false, 0));
    assertThrows(IllegalArgumentException.class, () -> AssetPlacement.place(p,
        AnimationChannel.CAPE, "Oops", STAR,
        CapeUvRegion.OUTSIDE, null, null, .5, .5, 0, false, 0));
    var added = AssetPlacement.place(p, AnimationChannel.CAPE, "Star", STAR,
        CapeUvRegion.OUTSIDE, null, null, .5, .5, 3, false, 0);
    UUID id = added.cape().layers().getLast().id();
    assertThrows(IllegalArgumentException.class,
        () -> AssetPlacement.editPixel(added, AnimationChannel.CAPE, id, 100, 0, -1));
    assertEquals(p.cape().layers().size(), 1);
    assertEquals(0xFFFFFFFF, added.cape().layers().getLast().imageData().source().pixelAt(1,1));
  }
}
