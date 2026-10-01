package dev.loomstudios;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoomStudios implements ModInitializer {
    public static final String MOD_ID = "loom-studios";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loom Studios common initialization complete.");
    }
}
