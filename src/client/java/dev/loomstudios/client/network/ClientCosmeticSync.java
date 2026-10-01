package dev.loomstudios.client.network;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.network.LoomNetworking;
import dev.loomstudios.network.ProofProject;
import dev.loomstudios.network.payload.EquippedStateS2CPayload;
import dev.loomstudios.network.payload.HelloC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobS2CPayload;
import dev.loomstudios.network.payload.ProjectNeededS2CPayload;
import dev.loomstudios.network.payload.ProjectRequestC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientCosmeticSync {
    private static final Map<String, ProofProject> PROJECTS = new HashMap<>();
    private static final Map<UUID, String> EQUIPPED = new HashMap<>();

    private static UUID localPlayerId;
    private static ProofProject localProject;
    private static String localProjectHash;
    private static boolean serverSupportsLoom;

    private ClientCosmeticSync() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ProjectNeededS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> sendLocalProjectIfRequested(payload.projectHash())));

        ClientPlayNetworking.registerGlobalReceiver(ProjectBlobS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> acceptProjectBlob(payload.projectHash(), payload.data())));

        ClientPlayNetworking.registerGlobalReceiver(EquippedStateS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> acceptEquippedState(payload)));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                client.execute(() -> onJoin(client)));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                client.execute(ClientCosmeticSync::resetSession));
    }

    private static void onJoin(Minecraft client) {
        resetSession();

        if (client.player == null) {
            return;
        }

        ensureLocalProject(client.player.getUUID());

        serverSupportsLoom = ClientPlayNetworking.canSend(HelloC2SPayload.ID);
        if (serverSupportsLoom) {
            ClientPlayNetworking.send(
                    new HelloC2SPayload(
                            LoomNetworking.PROTOCOL_VERSION,
                            localProjectHash
                    )
            );
            LoomStudios.LOGGER.info(
                    "SPIKE-05 hello sent with project {}",
                    shortHash(localProjectHash)
            );
        } else {
            LoomStudios.LOGGER.info(
                    "Server does not advertise Loom Studios networking; local-only cosmetics remain available."
            );
        }
    }

    public static void ensureLocalProject(UUID playerId) {
        if (localProject != null && playerId.equals(localPlayerId)) {
            return;
        }

        localPlayerId = playerId;
        localProject = ProofProject.forPlayer(playerId);
        localProjectHash = localProject.hash();
        PROJECTS.put(localProjectHash, localProject);
        EQUIPPED.put(playerId, localProjectHash);
    }

    public static ProofProject projectFor(UUID playerId) {
        if (playerId != null && playerId.equals(localPlayerId) && localProject != null) {
            return localProject;
        }

        String hash = EQUIPPED.get(playerId);
        return hash == null ? null : PROJECTS.get(hash);
    }

    public static String projectHashFor(UUID playerId) {
        if (playerId != null && playerId.equals(localPlayerId) && localProjectHash != null) {
            return localProjectHash;
        }

        return EQUIPPED.get(playerId);
    }

    public static boolean serverSupportsLoom() {
        return serverSupportsLoom;
    }

    private static void sendLocalProjectIfRequested(String hash) {
        if (!serverSupportsLoom
                || localProject == null
                || !localProjectHash.equals(hash)
                || !ClientPlayNetworking.canSend(ProjectBlobC2SPayload.ID)) {
            return;
        }

        ClientPlayNetworking.send(
                new ProjectBlobC2SPayload(localProjectHash, localProject.encode())
        );
    }

    private static void acceptProjectBlob(String hash, byte[] data) {
        if (!ProofProject.isValidHash(hash)
                || data.length == 0
                || data.length > ProofProject.MAX_BYTES
                || !hash.equals(ProofProject.sha256(data))) {
            LoomStudios.LOGGER.warn("Rejected invalid Loom project blob from server.");
            return;
        }

        try {
            PROJECTS.put(hash, ProofProject.decode(data));
            LoomStudios.LOGGER.info(
                    "Cached remote Loom project {}",
                    shortHash(hash)
            );
        } catch (IllegalArgumentException e) {
            LoomStudios.LOGGER.warn("Rejected malformed Loom project blob from server.", e);
        }
    }

    private static void acceptEquippedState(EquippedStateS2CPayload payload) {
        if (payload.projectHash().isEmpty()) {
            EQUIPPED.remove(payload.playerId());
            return;
        }

        if (!ProofProject.isValidHash(payload.projectHash())) {
            return;
        }

        EQUIPPED.put(payload.playerId(), payload.projectHash());

        if (!PROJECTS.containsKey(payload.projectHash())
                && ClientPlayNetworking.canSend(ProjectRequestC2SPayload.ID)) {
            ClientPlayNetworking.send(
                    new ProjectRequestC2SPayload(payload.projectHash())
            );
        }
    }

    private static void resetSession() {
        PROJECTS.clear();
        EQUIPPED.clear();
        serverSupportsLoom = false;

        if (localPlayerId != null && localProject != null && localProjectHash != null) {
            PROJECTS.put(localProjectHash, localProject);
            EQUIPPED.put(localPlayerId, localProjectHash);
        }
    }

    private static String shortHash(String hash) {
        return hash.substring(0, Math.min(12, hash.length()));
    }
}
