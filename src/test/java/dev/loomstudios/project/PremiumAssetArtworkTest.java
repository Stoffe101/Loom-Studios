package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PremiumAssetArtworkTest {
  @Test void illustratedAssetsAreDistinctAndContainRealShadingAndTransparency() {
    var featured = PremiumAssetArtwork.all();
    assertTrue(featured.size() >= 15, "Quality-first artwork collection is incomplete");
    var titles = new HashSet<String>();
    var signatures = new HashSet<Integer>();
    for (var entry : featured) {
      var pixels = entry.pixels();
      assertTrue(titles.add(entry.name()), "Duplicate illustrated asset title");
      assertTrue(pixels.width() >= 28 && pixels.height() >= 24,
          "A tiny source glyph is not detailed premium artwork: " + entry.name());
      assertTrue(pixels.width() <= 128 && pixels.height() <= 128);
      var colors = new HashSet<Integer>();
      int visible = 0;
      int transparent = 0;
      for (int argb : pixels.data()) {
        if ((argb >>> 24) == 0) transparent++;
        else { colors.add(argb); visible++; }
      }
      assertTrue(visible >= 28, "Nearly empty: " + entry.name());
      assertTrue(transparent > 0, "Asset should not have a solid rectangular background");
      assertTrue(colors.size() >= 5,
          "Premium pixel art must have shading/palette depth: " + entry.name());
      assertTrue(signatures.add(Arrays.hashCode(pixels.data())),
          "Two catalog assets are identical despite different names: " + entry.name());
      assertTrue(CreativeAssetCatalog.search(entry.name(), "Featured").stream()
          .anyMatch(a -> a.stamp().name().equals(entry.name())));
    }
  }

  @Test void placingPremiumArtPreservesExactMulticolorPixelsInSavedProject() {
    var source = PremiumAssetArtwork.all().stream()
        .filter(e -> e.name().equals("Moonstone Crescent")).findFirst().orElseThrow().pixels();
    var p = AssetPlacement.place(LoomProjectFactory.blank("Moonlight", 8),
        AnimationChannel.CAPE, "Moonstone Crescent", source,
        CapeUvRegion.OUTSIDE, null, null, .5, .5, 44, false, 0);
    var image = p.cape().layers().getLast().imageData().source();
    assertArrayEquals(source.data(), image.pixels());
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    assertEquals(p, LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(p)));
    assertTrue(Arrays.stream(image.pixels()).distinct().count() >= 6);
  }

  @Test void monochromeClassicsRemainUsableButNotDefaultFeaturedArt() {
    assertTrue(CreativeAssetCatalog.search("Tiny Spark", "All").size() >= 1);
    assertTrue(CreativeAssetCatalog.search("Tiny Spark", "Featured").isEmpty());
    assertEquals(PremiumAssetArtwork.all().size(),
        CreativeAssetCatalog.search("", "Featured").size());
    assertEquals(6, BuiltinStampPacks.all().size());
  }
}
