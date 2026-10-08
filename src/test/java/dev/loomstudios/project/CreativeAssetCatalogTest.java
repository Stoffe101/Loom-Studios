package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class CreativeAssetCatalogTest {
  @Test void catalogIsCategorizedUniqueAndHasUsefulVariety() {
    var all = CreativeAssetCatalog.all();
    assertTrue(all.size() >= 30, "Starter catalog should include varied artwork");
    assertEquals(all.size(), all.stream().map(a -> a.stamp().id()).distinct().count());
    for (var a : all) {
      assertFalse(a.stamp().name().isBlank());
      assertTrue(CreativeAssetCatalog.categories().contains(a.category()));
      assertTrue(a.stamp().patch().width() <= 128);
      assertTrue(a.stamp().patch().height() <= 128);
      assertTrue(Arrays.stream(a.stamp().patch().data()).anyMatch(v -> (v >>> 24) != 0),
          "Empty artwork: " + a.stamp().name());
    }
    assertTrue(CreativeAssetCatalog.search("moon", "All").size() >= 2);
    assertTrue(CreativeAssetCatalog.search("forest", "Nature").size() >= 3);
    assertTrue(CreativeAssetCatalog.search("mist", "Clouds & Mist").size() >= 2);
    assertTrue(CreativeAssetCatalog.search("crystal", "Fantasy").size() >= 1);
    assertEquals(all.size(), CreativeAssetCatalog.search("", "All").size());
  }

  @Test void assetCatalogNeverChangesExistingSixPatternStampPack() {
    assertEquals(6, BuiltinStampPacks.all().size());
    var a = CreativeAssetCatalog.search("pine", "Nature").getFirst().stamp();
    var b = CreativeAssetCatalog.search("pine", "Nature").getFirst().stamp();
    assertEquals(a.id(), b.id());
    assertArrayEquals(a.patch().data(), b.patch().data());
    assertEquals(List.of(), CreativeAssetCatalog.search("nonexistent_shape_279", "All"));
  }
}
