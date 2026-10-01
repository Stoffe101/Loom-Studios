package dev.loomstudios.palette;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ColorPalette(
        UUID id,
        String name,
        List<Integer> colors
) {
    public static final int MAX_NAME_CHARS = 48;
    public static final int MAX_COLORS = 64;

    public ColorPalette {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(colors, "colors");

        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid palette name");
        }

        if (colors.isEmpty() || colors.size() > MAX_COLORS) {
            throw new IllegalArgumentException("Palette color count out of range");
        }

        name = trimmed;

        List<Integer> normalized = new ArrayList<>(colors.size());
        for (Integer color : colors) {
            if (color == null) {
                throw new IllegalArgumentException("Palette color cannot be null");
            }
            normalized.add(color);
        }
        colors = List.copyOf(normalized);
    }

    public ColorPalette withName(String nextName) {
        return new ColorPalette(id, nextName, colors);
    }

    public ColorPalette addColor(int argb) {
        if (colors.contains(argb)) {
            return this;
        }

        if (colors.size() >= MAX_COLORS) {
            throw new IllegalStateException("Palette already has 64 colors");
        }

        List<Integer> next = new ArrayList<>(colors);
        next.add(argb);
        return new ColorPalette(id, name, next);
    }

    public ColorPalette removeColorAt(int index) {
        if (index < 0 || index >= colors.size()) {
            return this;
        }

        if (colors.size() == 1) {
            return this;
        }

        List<Integer> next = new ArrayList<>(colors);
        next.remove(index);
        return new ColorPalette(id, name, next);
    }

    public ColorPalette copyWithNewId() {
        return new ColorPalette(UUID.randomUUID(), name, colors);
    }
}
