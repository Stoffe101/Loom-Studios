package dev.loomstudios.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.render.DynamicCosmeticSpike;
import dev.loomstudios.client.screen.LoomPreviewSpikeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class LoomStudiosClient implements ClientModInitializer {
    private static KeyMapping cycleElytraThickness;
    private static KeyMapping openPreview;

    @Override
    public void onInitializeClient() {
        LoomStudios.LOGGER.info("Loom Studios client initialization complete.");
        OptionalModSupport.logDetectedMods();

        KeyMapping.Category debugCategory = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "debug")
        );

        cycleElytraThickness = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.loom-studios.cycle_elytra_thickness",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                debugCategory
        ));

        openPreview = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.loom-studios.open_preview",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                debugCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cycleElytraThickness.consumeClick()) {
                DynamicCosmeticSpike.cycleElytraThickness(client);
            }

            while (openPreview.consumeClick()) {
                if (client.player != null && client.level != null) {
                    client.setScreen(new LoomPreviewSpikeScreen());
                }
            }

            DynamicCosmeticSpike.tick(client);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> DynamicCosmeticSpike.close());
    }
}
