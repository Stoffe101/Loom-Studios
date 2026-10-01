package dev.loomstudios.client.palette;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.palette.ColorPalette;
import dev.loomstudios.palette.ColorPaletteCodec;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ColorPaletteLibrary {
    private static final Path ROOT = FabricLoader.getInstance()
            .getGameDir()
            .resolve("loom-studios")
            .resolve("palettes");
    private static final Path EXPORTS = ROOT.resolve("exports");
    private static final Path IMPORTS = ROOT.resolve("imports");

    private static List<ColorPalette> palettes = List.of();
    private static UUID selectedId;

    private ColorPaletteLibrary() {
    }

    public static void refresh() {
        List<ColorPalette> next = new ArrayList<>();

        try {
            Files.createDirectories(ROOT);
            Files.createDirectories(EXPORTS);
            Files.createDirectories(IMPORTS);

            try (var stream = Files.list(ROOT)) {
                for (Path path : stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName()
                                .toString()
                                .endsWith(".loompalette"))
                        .toList()) {
                    try {
                        String json = Files.readString(
                                path,
                                StandardCharsets.UTF_8
                        );
                        next.add(ColorPaletteCodec.decode(json));
                    } catch (RuntimeException | IOException e) {
                        LoomStudios.LOGGER.warn(
                                "Skipping unreadable Loom palette {}",
                                path,
                                e
                        );
                    }
                }
            }
        } catch (IOException e) {
            LoomStudios.LOGGER.warn(
                    "Failed to scan Loom Studios palette library",
                    e
            );
        }

        next.sort(
                Comparator.comparing(
                        ColorPalette::name,
                        String.CASE_INSENSITIVE_ORDER
                )
        );
        palettes = List.copyOf(next);

        if (selectedId != null && find(selectedId).isEmpty()) {
            selectedId = null;
        }
        if (selectedId == null && !palettes.isEmpty()) {
            selectedId = palettes.getFirst().id();
        }
    }

    public static List<ColorPalette> palettes() {
        return palettes;
    }

    public static Optional<ColorPalette> selected() {
        return selectedId == null
                ? Optional.empty()
                : find(selectedId);
    }

    public static Optional<ColorPalette> find(UUID id) {
        return palettes.stream()
                .filter(palette -> palette.id().equals(id))
                .findFirst();
    }

    public static void select(UUID id) {
        if (find(id).isEmpty()) {
            throw new IllegalArgumentException("Unknown Loom palette");
        }
        selectedId = id;
    }

    public static ColorPalette create(String name, int initialColor)
            throws IOException {
        ColorPalette palette = new ColorPalette(
                UUID.randomUUID(),
                normalizedName(name),
                List.of(initialColor)
        );
        write(palette);
        refresh();
        selectedId = palette.id();
        return find(palette.id()).orElse(palette);
    }

    public static ColorPalette save(ColorPalette palette) throws IOException {
        write(palette);
        refresh();
        selectedId = palette.id();
        return find(palette.id()).orElse(palette);
    }

    public static ColorPalette renameSelected(String name) throws IOException {
        ColorPalette current = selected().orElseThrow(
                () -> new IllegalStateException("No palette selected")
        );
        return save(current.withName(normalizedName(name)));
    }

    public static ColorPalette addColorToSelected(int color) throws IOException {
        ColorPalette current = selected().orElseThrow(
                () -> new IllegalStateException("No palette selected")
        );
        return save(current.addColor(color));
    }

    public static ColorPalette removeColorFromSelected(int index)
            throws IOException {
        ColorPalette current = selected().orElseThrow(
                () -> new IllegalStateException("No palette selected")
        );
        return save(current.removeColorAt(index));
    }

    public static void deleteSelected() throws IOException {
        ColorPalette current = selected().orElseThrow(
                () -> new IllegalStateException("No palette selected")
        );

        Files.deleteIfExists(ROOT.resolve(current.id() + ".loompalette"));
        selectedId = null;
        refresh();
    }

    public static ExportResult exportSelected() throws IOException {
        ColorPalette palette = selected().orElseThrow(
                () -> new IllegalStateException("No palette selected")
        );

        Files.createDirectories(EXPORTS);

        String fileName = sanitize(palette.name())
                + "-"
                + palette.id().toString().substring(0, 8)
                + ".loompalette";
        Path path = EXPORTS.resolve(fileName);

        Files.writeString(
                path,
                ColorPaletteCodec.encode(palette, true),
                StandardCharsets.UTF_8
        );

        return new ExportResult(
                path,
                ColorPaletteCodec.encodeShareCode(palette)
        );
    }

    public static ColorPalette importShareCode(String code) throws IOException {
        ColorPalette imported = ColorPaletteCodec.decodeShareCode(code)
                .copyWithNewId();
        write(imported);
        refresh();
        selectedId = imported.id();
        return find(imported.id()).orElse(imported);
    }

    public static int importInbox() throws IOException {
        Files.createDirectories(IMPORTS);
        int importedCount = 0;

        try (var stream = Files.list(IMPORTS)) {
            for (Path path : stream
                    .filter(Files::isRegularFile)
                    .filter(candidate -> candidate.getFileName()
                            .toString()
                            .endsWith(".loompalette"))
                    .toList()) {
                try {
                    ColorPalette imported = ColorPaletteCodec.decode(
                            Files.readString(path, StandardCharsets.UTF_8)
                    ).copyWithNewId();

                    write(imported);
                    selectedId = imported.id();
                    importedCount++;
                } catch (RuntimeException e) {
                    LoomStudios.LOGGER.warn(
                            "Skipping invalid palette import {}",
                            path,
                            e
                    );
                }
            }
        }

        refresh();
        return importedCount;
    }

    public static Path importFolder() {
        return IMPORTS;
    }

    public static Path exportFolder() {
        return EXPORTS;
    }

    private static void write(ColorPalette palette) throws IOException {
        Files.createDirectories(ROOT);

        Path target = ROOT.resolve(palette.id() + ".loompalette");
        Path temporary = ROOT.resolve(palette.id() + ".loompalette.tmp");

        Files.writeString(
                temporary,
                ColorPaletteCodec.encode(palette, true),
                StandardCharsets.UTF_8
        );

        try {
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static String normalizedName(String name) {
        String trimmed = name == null ? "" : name.trim();
        return trimmed.isEmpty() ? "Untitled Palette" : trimmed;
    }

    private static String sanitize(String name) {
        String sanitized = name
                .replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");

        return sanitized.isBlank() ? "palette" : sanitized;
    }

    public record ExportResult(Path path, String shareCode) {
    }
}
