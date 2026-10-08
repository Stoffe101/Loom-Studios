package dev.loomstudios.project;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Small, original starter collection for the editable Asset Library.
 *
 * <p>These are authoring resources. Placing them copies their actual pixels into the saved
 * project through {@link AssetPlacement}, so no catalog asset identifier is transmitted.
 * The complete illustrated collection is a separate curated-art milestone.
 */
public final class CreativeAssetCatalog {
  public record Entry(String category, CustomStamp stamp, List<String> tags) {
    public Entry {
      category = Objects.requireNonNull(category);
      stamp = Objects.requireNonNull(stamp);
      tags = List.copyOf(tags);
    }
  }
  private static final List<Entry> ALL = build();

  public static List<Entry> all() { return ALL; }
  public static List<String> categories() {
    return List.of("All", "Celestial", "Clouds & Mist", "Nature", "Fantasy", "Decorations");
  }

  public static List<Entry> search(String query, String category) {
    String q = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
    String c = category == null || category.isBlank() ? "All" : category;
    return ALL.stream().filter(e -> c.equals("All") || c.equals(e.category()))
        .filter(e -> q.isEmpty() || e.stamp().name().toLowerCase(Locale.ROOT).contains(q)
            || e.category().toLowerCase(Locale.ROOT).contains(q)
            || e.tags().stream().anyMatch(t -> t.toLowerCase(Locale.ROOT).contains(q)))
        .toList();
  }

  private static List<Entry> build() {
    var a = new ArrayList<Entry>();
    // Celestial: deliberately distinct silhouettes, not just rotated duplicates.
    add(a,"Celestial","Tiny Spark","star twinkle",
        ".#.","###",".#.");
    add(a,"Celestial","Cross Star","star four point",
        "...#...","...#...","..###..","#######","..###..","...#...","...#...");
    add(a,"Celestial","Diamond Star","star eight point",
        "...#...","..###..",".#####.","#######",".#####.","..###..","...#...");
    add(a,"Celestial","Glimmer Star","star spark sparkle",
        "....#....","....#....","....#....","...###...","#########","...###...","....#....","....#....","....#....");
    add(a,"Celestial","Starlight Trio","star cluster sky",
        ".#........","###.......",".#.....#..","......###.",".......#..","..........","....#.....","...###....","....#.....");
    add(a,"Celestial","Crescent Moon","moon night lunar",
        "...####...",".#######..","###...#...","##........","##........","##........","###...#...",".#######..","...####...");
    add(a,"Celestial","Full Moon","moon planet lunar",
        "..#####..",".#######.","#########","#########","#########","#########","#########",".#######.","..#####..");
    add(a,"Celestial","Half Moon","moon lunar phase",
        "..###....",".#####...","######...","######...","######...","######...",".#####...","..###....");
    add(a,"Celestial","Shooting Star","meteor comet tail",
        "........#.",".......###","......###.",".....###..","....##....","...#......","..#.......",".#........");
    add(a,"Celestial","Ringed Planet","planet orbit saturn",
        ".....###.....","...#######...",".###########.","#############","...#######...", "....#####....",".....###.....");

    // Clouds and atmosphere, useful at different resolutions and for soft opacity.
    add(a,"Clouds & Mist","Puffy Cloud","cloud fluffy sky",
        "...####......",".########....","###########..","############.","#############",".###########.");
    add(a,"Clouds & Mist","High Clouds","cirrus cloud horizon",
        "...######.......","###########.....","..###########...","....###########",".......#######.");
    add(a,"Clouds & Mist","Cloud Bank","fog cloudbank sky",
        "........#####..........","...####.########.......","###################....","########################","########################");
    add(a,"Clouds & Mist","Thin Mist","fog smoke haze veil",
        "...+++++++.......","++++++++++++....","..++++++++++++++","....++++++++++++",".......++++.....");
    add(a,"Clouds & Mist","Rolling Fog","mist haze atmosphere",
        "...+++++...........",".+++++++++........","+++++++++++++++...","...++++++++++++++","......++++++++...");
    add(a,"Clouds & Mist","Storm Cloud","storm rain thunder",
        "..#######....",".###########.","##############","##############","..#..#..#..#.","...#..#..#...");
    add(a,"Clouds & Mist","Wind Curl","wind breeze sky",
        "....#######.","..##.......#","###.........",".....######.","...##.......","###########.");

    // Distinct tree silhouettes and building blocks for layered nature scenes.
    add(a,"Nature","Pine Tree","fir spruce forest evergreen",
        "....#....","...###...","..#####..","...###...","..#####..",".#######.","#########","....#....","....#....");
    add(a,"Nature","Tall Spruce","pine forest conifer",
        ".....#.....","....###....","...#####...","....###....","...#####...","..#######..","...#####...",".#########.","###########",".....#.....",".....#.....");
    add(a,"Nature","Oak Tree","tree round foliage nature",
        "...####...","..######..",".########.","##########","##########",".########.","..######..","....##....","....##....");
    add(a,"Nature","Willow Tree","tree hanging forest branches",
        "...#####....",".#########..","###########.","##.######.##","#..######..#","...##.##...","...##.##...","...##.##...");
    add(a,"Nature","Bare Winter Tree","tree branch winter",
        "#......#......#",".#....#...#..#","..#..#...#..#.","...##...#..#..","....#..#..#...","....#######...","......#......","......#......","......#......");
    add(a,"Nature","Little Leaf","leaf plant forest",
        ".....#..","...###..","..#####.",".######.","#######.","..####..","...##...","...#....");
    add(a,"Nature","Fern Frond","fern leaves forest plant",
        ".....#.....","..#..##....","...###..#..",".#..###....","..####..#..","#..###.....","...##......","....#......","....#......");
    add(a,"Nature","Mountain Ridge","mountains nature horizon",
        "........#.........",".......###........","......#####.......","...#..######......","..###.#######..#.","##################");
    add(a,"Nature","Daisy Flower","flower nature spring",
        "...#...","..###..","#.###.#","#######","#.###.#","..###..","...#...","...#...");
    add(a,"Nature","Butterfly","butterfly animal nature",
        "##...##","###.###",".#####.","..###..",".#####.","##...##");

    // Fantasy and decorations useful for combinations and emblem compositions.
    add(a,"Fantasy","Arcane Rune","rune magic glyph",
        "#......#",".#....#.","..#..#..","...##...","...##...","..#..#..",".#....#.","#......#");
    add(a,"Fantasy","Crystal Shard","crystal fantasy magic",
        "...#...","..###..",".#####.","#######","#######",".#####.","..###..","...#...");
    add(a,"Fantasy","Magic Spark","spell magic particle",
        "...#...",".#.#.#.","..###..","#######","..###..",".#.#.#.","...#...");
    add(a,"Fantasy","Snowflake","snow winter ice",
        "#..#..#",".#.#.#.","..###..","#######","..###..",".#.#.#.","#..#..#");
    add(a,"Fantasy","Fire Rune","flame fire magic",
        "...##....","..###....","..####...",".##.###..","####.####","#########",".#######.");
    add(a,"Fantasy","Water Drop","water rain ocean",
        "...#...","..###..","..###..",".#####.","#######","#######",".#####.","..###..");
    add(a,"Decorations","Pixel Heart","love heart",
        ".##.##.","#######","#######",".#####.","..###..","...#...");
    add(a,"Decorations","Tiny Heart","heart cute",
        ".#.#.","#####",".###.","..#..");
    add(a,"Decorations","Diamond Emblem","diamond geometry border",
        "...#...","..###..",".#####.","#######",".#####.","..###..","...#...");
    add(a,"Decorations","Crown","crown royal emblem",
        "#...#...#","##.###.##","#########",".#######.",".#######.");
    add(a,"Decorations","Chevron","trim geometric border",
        "#.......#",".#.....#.","..#...#..","...#.#...","....#....");

    return List.copyOf(a);
  }

  private static void add(List<Entry> entries, String category, String name, String keywords,
      String... rows) {
    int width = 0;
    for (String row : rows) width = Math.max(width, row.length());
    int[] data = new int[width * rows.length];
    for (int y = 0; y < rows.length; y++)
      for (int x = 0; x < rows[y].length(); x++) {
        data[y * width + x] = switch (rows[y].charAt(x)) {
          case '#' -> 0xFFF6F8FF;
          case '+' -> 0x77DBE6FF;
          default -> 0;
        };
      }
    var patch = new PixelPatch(width, rows.length, data);
    var stamp = new CustomStamp(
        UUID.nameUUIDFromBytes(("loom-creative-" + category + "-" + name)
            .getBytes(StandardCharsets.UTF_8)), name, patch, false);
    entries.add(new Entry(category, stamp, List.of(keywords.split(" "))));
  }

  private CreativeAssetCatalog() {}
}
