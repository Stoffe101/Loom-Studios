package dev.loomstudios.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.network.ClientCosmeticSync;
import dev.loomstudios.client.project.ProjectLibraryIndex;
import dev.loomstudios.client.render.PlayerCosmeticRenderer;
import dev.loomstudios.client.render.LoomCapeGlowLayer;
import dev.loomstudios.client.screen.LoomHomeScreen;
import dev.loomstudios.client.screen.LoomPlayerPreviewScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class LoomStudiosClient implements ClientModInitializer {
    private static KeyMapping cycleElytraThickness;
    private static KeyMapping openPreview;
    private static KeyMapping openStudio;
    private static KeyMapping toggleEmissive;

    @Override
    public void onInitializeClient() {
        LoomStudios.LOGGER.info("Loom Studios client initialization complete.");
        OptionalModSupport.logDetectedMods();
        ClientCosmeticSync.register();
        ProjectLibraryIndex.refresh();

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, renderer, helper, context) -> {
                    if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
                        helper.register(
                                new LoomCapeGlowLayer(
                                        avatarRenderer,
                                        context.getModelSet(),
                                        context.getEquipmentAssets()
                                )
                        );
                    }
                }
        );

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

        openStudio = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.loom-studios.open_studio",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                debugCategory
        ));

        toggleEmissive = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.loom-studios.toggle_emissive",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                debugCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cycleElytraThickness.consumeClick()) {
                PlayerCosmeticRenderer.cycleElytraThickness(client);
            }

            while (openPreview.consumeClick()) {
                if (client.player != null && client.level != null) {
                    client.setScreen(new LoomPlayerPreviewScreen());
                }
            }

            while (openStudio.consumeClick()) {
                if (client.player != null && client.level != null) {
                    client.setScreen(new LoomHomeScreen());
                }
            }

            while (toggleEmissive.consumeClick()) {
                PlayerCosmeticRenderer.toggleEmissivePass(client);
            }

            ClientCosmeticSync.tick(client);
            PlayerCosmeticRenderer.tick(client);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(PlayerCosmeticRenderer::close);
    }
}
