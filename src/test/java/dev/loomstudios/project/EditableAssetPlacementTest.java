package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EditableAssetPlacementTest {
  private CustomStamp star() {
    return BuiltinStampPacks.all().stream()
        .filter(asset -> asset.name().equals("Star"))
        .findFirst()
        .orElseThrow();
  }

  @Test
  void placingCapeAssetCreatesIndependentTransformableSerializableLayer() {
    LoomProject baseline = LoomProjectFactory.blank("Editable stars", 1);
    var project =
        EditableAssetPlacement.cape(
            baseline, star(), CapeUvRegion.OUTSIDE, .5, .5, .6, 0xFF63E5FF, true);
    assertEquals(baseline.cape().layers().size() + 1, project.cape().layers().size());
    assertEquals(LayerKind.IMAGE, project.cape().layers().getLast().kind());
    var placed = project.cape().layers().getLast();
    assertEquals("Star", placed.name());
    assertNotEquals(baseline.hash(), project.hash());
    assertEquals(project, LoomProjectCodec.decode(project.encode()));
    assertEquals(project, LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(project)));

    int[] rendered = LayerRasterizer.rasterize(
        placed, project.cape().width(), project.cape().height());
    assertTrue(Arrays.stream(rendered).anyMatch(argb -> (argb & 0xFFFFFF) == 0x63E5FF
        && (argb >>> 24) != 0));
    assertEquals(0, rendered[10 * 64 + 38], "Cape placement cannot color unrelated UVs");

    var relocated = placed.imageData().withTransform(
        placed.imageData().transform().withCenter(
            placed.imageData().transform().centerX() + .01,
            placed.imageData().transform().centerY()));
    var moved = ProjectEdits.setCapeImageData(project, placed.id(), relocated);
    assertEquals(placed.id(), moved.cape().layers().getLast().id());
    assertNotEquals(placed.imageData(), moved.cape().layers().getLast().imageData());
    assertEquals(moved, LoomProjectCodec.decode(moved.encode()));
    assertEquals(baseline, LoomProjectCodec.decode(baseline.encode()));
  }

  @Test
  void elytraPlacedOnLeftInsideDoesNotLeakIntoRightWing() {
    var p = EditableAssetPlacement.elytra(
        LoomProjectFactory.blank("Independent wings", 1),
        star(), ElytraWing.LEFT, ElytraSurface.INSIDE, .5, .5, .65, 0xFFFFFFFF, false);
    var layer = p.elytra().layers().getLast();
    var raster = LayerRasterizer.rasterize(layer, p.elytra().width(), p.elytra().height());

    // Left wing inside starts at x=26; right wing inside starts at x=2.
    boolean leftVisible = false;
    for (int y = 2; y < 22; y++)
      for (int x = 26; x < 36; x++)
        leftVisible |= (raster[y * 64 + x] >>> 24) != 0;
    assertTrue(leftVisible);
    for (int y = 2; y < 22; y++)
      for (int x = 2; x < 12; x++)
        assertEquals(0, raster[y * 64 + x], "Asset crossed to the other Elytra wing");
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
  }

  @Test
  void explicitPixelConversionKeepsLayerIdentityAndExactCompositedPixels() {
    var p = EditableAssetPlacement.cape(
        LoomProjectFactory.blank("Pixel mode", 1),
        star(), CapeUvRegion.OUTSIDE, .5, .5, .5, 0xFFFFFFFF, false);
    var source = p.cape().layers().getLast();
    int[] before = LayerRasterizer.rasterize(source, 64, 32);
    var updated = EditableAssetPlacement.convertToPaint(
        p, AnimationChannel.CAPE, source.id());
    var converted = updated.cape().layers().getLast();
    assertEquals(source.id(), converted.id());
    assertEquals(source.name(), converted.name());
    assertEquals(LayerKind.PAINT, converted.kind());
    assertArrayEquals(before, converted.pixels());
    assertArrayEquals(before, LayerRasterizer.rasterize(converted, 64, 32));
    assertEquals(updated, LoomProjectCodec.decode(updated.encode()));
    assertEquals(LayerKind.IMAGE, p.cape().layers().getLast().kind());
  }

  @Test
  void invalidPlacementIsRejectedWithoutChangingExistingProject() {
    var project = LoomProjectFactory.blank("Bounds", 1);
    assertThrows(
        IllegalArgumentException.class,
        () -> EditableAssetPlacement.cape(
            project, star(), CapeUvRegion.OUTSIDE, 1.2, .5, .4, 0xFFFFFFFF, false));
    assertThrows(
        IllegalArgumentException.class,
        () -> EditableAssetPlacement.cape(
            project, star(), CapeUvRegion.OUTSIDE, .5, .5, 0, 0xFFFFFFFF, false));
    assertEquals(1, project.cape().layers().size());
  }
}
