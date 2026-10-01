package dev.loomstudios.client;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.render.DynamicCosmeticSpike;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class LoomStudiosClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LoomStudios.LOGGER.info("Loom Studios client initialization complete.");
        OptionalModSupport.logDetectedMods();

        ClientTickEvents.END_CLIENT_TICK.register(DynamicCosmeticSpike::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> DynamicCosmeticSpike.close());
    }
}
