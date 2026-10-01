package dev.loomstudios.palette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public final class ColorPaletteCodec {
    public static final int VERSION = 1;
    public static final String FORMAT = "loom-studios-palette";
    public static final String SHARE_PREFIX = "LOOMPAL1:";

    private static final Gson PRETTY_GSON =
            new GsonBuilder().setPrettyPrinting().create();
    private static final Gson COMPACT_GSON = new Gson();

    private ColorPaletteCodec() {
    }

    public static String encode(ColorPalette palette, boolean pretty) {
        JsonObject root = new JsonObject();
        root.addProperty("format", FORMAT);
        root.addProperty("version", VERSION);
        root.addProperty("id", palette.id().toString());
        root.addProperty("name", palette.name());

        JsonArray colors = new JsonArray();
        for (int color : palette.colors()) {
            colors.add(String.format("#%06X", color & 0x00FFFFFF));
        }
        root.add("colors", colors);

        return (pretty ? PRETTY_GSON : COMPACT_GSON).toJson(root);
    }

    public static ColorPalette decode(String json) {
        JsonElement parsed = JsonParser.parseString(json);
        if (!parsed.isJsonObject()) {
            throw new IllegalArgumentException("Palette payload is not an object");
        }

        JsonObject root = parsed.getAsJsonObject();

        if (!root.has("format")
                || !FORMAT.equals(root.get("format").getAsString())) {
            throw new IllegalArgumentException("Unknown palette format");
        }

        if (!root.has("version")
                || root.get("version").getAsInt() != VERSION) {
            throw new IllegalArgumentException("Unsupported palette version");
        }

        UUID id = UUID.fromString(root.get("id").getAsString());
        String name = root.get("name").getAsString();

        JsonArray colorsJson = root.getAsJsonArray("colors");
        List<Integer> colors = new ArrayList<>(colorsJson.size());

        for (JsonElement element : colorsJson) {
            String value = element.getAsString().trim();
            if (!value.matches("#[0-9A-Fa-f]{6}")) {
                throw new IllegalArgumentException(
                        "Invalid palette color " + value
                );
            }

            colors.add(
                    0xFF000000
                            | Integer.parseInt(value.substring(1), 16)
            );
        }

        return new ColorPalette(id, name, colors);
    }

    public static String encodeShareCode(ColorPalette palette) {
        byte[] json = encode(palette, false).getBytes(StandardCharsets.UTF_8);
        return SHARE_PREFIX
                + Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    }

    public static ColorPalette decodeShareCode(String code) {
        if (code == null || !code.startsWith(SHARE_PREFIX)) {
            throw new IllegalArgumentException("Not a Loom palette share code");
        }

        byte[] json = Base64.getUrlDecoder().decode(
                code.substring(SHARE_PREFIX.length()).trim()
        );

        if (json.length > 16 * 1024) {
            throw new IllegalArgumentException("Palette share code is too large");
        }

        return decode(new String(json, StandardCharsets.UTF_8));
    }
}
