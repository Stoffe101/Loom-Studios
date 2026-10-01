package dev.loomstudios.network;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.network.payload.EquippedStateS2CPayload;
import dev.loomstudios.network.payload.HelloC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobS2CPayload;
import dev.loomstudios.network.payload.ProjectNeededS2CPayload;
import dev.loomstudios.network.payload.ProjectRequestC2SPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SPIKE-05 server-authoritative cosmetic synchronization proof.
 */
public final class LoomNetworking {
    public static final int PROTOCOL_VERSION = 1;

    private static final Map<String, byte[]> PROJECTS = new HashMap<>();
    private static final Map<UUID, String> EQUIPPED = new HashMap<>();

    private LoomNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(HelloC2SPayload.ID, HelloC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProjectBlobC2SPayload.ID, ProjectBlobC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProjectRequestC2SPayload.ID, ProjectRequestC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ProjectNeededS2CPayload.ID, ProjectNeededS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ProjectBlobS2CPayload.ID, ProjectBlobS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(EquippedStateS2CPayload.ID, EquippedStateS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(HelloC2SPayload.ID, (payload, context) ->
                context.player().getServer().execute(() ->
                        handleHello(context.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(ProjectBlobC2SPayload.ID, (payload, context) ->
                context.player().getServer().execute(() ->
                        handleProjectUpload(context.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(ProjectRequestC2SPayload.ID, (payload, context) ->
                context.player().getServer().execute(() ->
                        handleProjectRequest(context.player(), payload)));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerId = handler.player.getUUID();
            if (EQUIPPED.remove(playerId) != null) {
                broadcast(server, new EquippedStateS2CPayload(playerId, ""));
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PROJECTS.clear();
            EQUIPPED.clear();
        });

        LoomStudios.LOGGER.info("SPIKE-05 networking payloads registered.");
    }

    private static void handleHello(ServerPlayer player, HelloC2SPayload payload) {
        if (payload.protocolVersion() != PROTOCOL_VERSION
                || !ProofProject.isValidHash(payload.projectHash())) {
            LoomStudios.LOGGER.warn(
                    "Rejected Loom hello from {}: protocol/hash invalid",
                    player.getGameProfile().name()
            );
            return;
        }

        syncExistingStatesTo(player);

        if (PROJECTS.containsKey(payload.projectHash())) {
            equip(player, payload.projectHash());
        } else if (ServerPlayNetworking.canSend(player, ProjectNeededS2CPayload.ID)) {
            ServerPlayNetworking.send(
                    player,
                    new ProjectNeededS2CPayload(payload.projectHash())
            );
        }
    }

    private static void handleProjectUpload(
            ServerPlayer player,
            ProjectBlobC2SPayload payload
    ) {
        if (!validateProjectBlob(payload.projectHash(), payload.data())) {
            LoomStudios.LOGGER.warn(
                    "Rejected invalid Loom project blob from {}",
                    player.getGameProfile().name()
            );
            return;
        }

        PROJECTS.put(payload.projectHash(), Arrays.copyOf(payload.data(), payload.data().length));
        equip(player, payload.projectHash());

        LoomStudios.LOGGER.info(
                "Cached Loom project {} from {} ({} bytes)",
                shortHash(payload.projectHash()),
                player.getGameProfile().name(),
                payload.data().length
        );
    }

    private static void handleProjectRequest(
            ServerPlayer player,
            ProjectRequestC2SPayload payload
    ) {
        if (!ProofProject.isValidHash(payload.projectHash())) {
            return;
        }

        byte[] data = PROJECTS.get(payload.projectHash());
        if (data != null && ServerPlayNetworking.canSend(player, ProjectBlobS2CPayload.ID)) {
            ServerPlayNetworking.send(
                    player,
                    new ProjectBlobS2CPayload(
                            payload.projectHash(),
                            Arrays.copyOf(data, data.length)
                    )
            );
        }
    }

    private static boolean validateProjectBlob(String hash, byte[] data) {
        if (!ProofProject.isValidHash(hash)
                || data.length == 0
                || data.length > ProofProject.MAX_BYTES
                || !hash.equals(ProofProject.sha256(data))) {
            return false;
        }

        try {
            ProofProject.decode(data);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static void equip(ServerPlayer player, String hash) {
        EQUIPPED.put(player.getUUID(), hash);
        broadcast(
                player.getServer(),
                new EquippedStateS2CPayload(player.getUUID(), hash)
        );
    }

    private static void syncExistingStatesTo(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, EquippedStateS2CPayload.ID)) {
            return;
        }

        EQUIPPED.forEach((playerId, hash) ->
                ServerPlayNetworking.send(
                        player,
                        new EquippedStateS2CPayload(playerId, hash)
                ));
    }

    private static void broadcast(MinecraftServer server, EquippedStateS2CPayload payload) {
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            if (ServerPlayNetworking.canSend(target, EquippedStateS2CPayload.ID)) {
                ServerPlayNetworking.send(target, payload);
            }
        }
    }

    private static String shortHash(String hash) {
        return hash.substring(0, Math.min(12, hash.length()));
    }
}
