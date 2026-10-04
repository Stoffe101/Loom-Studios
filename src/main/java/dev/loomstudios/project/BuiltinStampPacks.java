package dev.loomstudios.project;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class BuiltinStampPacks {
  public static List<CustomStamp> all() {
    var result = new ArrayList<CustomStamp>();
    add(
        result,
        "Star",
        "....#....",
        "....#....",
        "...###...",
        "#########",
        "..#####..",
        "...###...",
        "..#...#..",
        ".#.....#.");
    add(result, "Heart", ".##.##.", "#######", "#######", ".#####.", "..###..", "...#...");
    add(result, "Rune", "#...#", ".#.#.", "..#..", ".#.#.", "#.#.#", "..#..", "..#..");
    add(
        result, "Flame", "...#...", "..##...", "..###..", ".#.###.", "####.##", "#######",
        ".#####.");
    add(result, "Scale", "..###..", ".#...#.", "#.....#", ".#...#.", "..###..");
    add(result, "Cloud", "..###....", ".#####...", "########.", "#########", ".#######.");
    return List.copyOf(result);
  }

  private static void add(List<CustomStamp> out, String name, String... rows) {
    int w = Arrays.stream(rows).mapToInt(String::length).max().orElseThrow();
    int[] data = new int[w * rows.length];
    for (int y = 0; y < rows.length; y++)
      for (int x = 0; x < rows[y].length(); x++)
        if (rows[y].charAt(x) == '#') data[y * w + x] = 0xFFFFFFFF;
    out.add(
        new CustomStamp(
            UUID.nameUUIDFromBytes(("loom-pattern-" + name).getBytes(StandardCharsets.UTF_8)),
            name,
            new PixelPatch(w, rows.length, data),
            false));
  }

  private BuiltinStampPacks() {}
}
