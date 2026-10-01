package dev.loomstudios.client;

import dev.loomstudios.LoomStudios;
import net.fabricmc.api.ClientModInitializer;

public final class LoomStudiosClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LoomStudios.LOGGER.info("Loom Studios client initialization complete.");
        OptionalModSupport.logDetectedMods();
    }
}
