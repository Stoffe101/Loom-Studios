package dev.loomstudios.client;

import dev.loomstudios.LoomStudios;
import net.fabricmc.loader.api.FabricLoader;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OptionalModSupport {
    private static final Map<String, String> OPTIONAL_MODS = new LinkedHashMap<>();

    static {
        OPTIONAL_MODS.put("sodium", "Sodium");
        OPTIONAL_MODS.put("sodium-extra", "Sodium Extra");
        OPTIONAL_MODS.put("iris", "Iris");
        OPTIONAL_MODS.put("skinlayers3d", "3D Skin Layers");
    }

    private OptionalModSupport() {
    }

    public static boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static void logDetectedMods() {
        OPTIONAL_MODS.forEach((id, name) ->
                LoomStudios.LOGGER.info(
                        "Optional compatibility mod {} ({}): {}",
                        name,
                        id,
                        isLoaded(id) ? "detected" : "not installed"
                ));
    }
}
