package dev.loomstudios.client.network;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.network.LoomNetworking;
import dev.loomstudios.network.payload.EquippedStateS2CPayload;
import dev.loomstudios.network.payload.HelloC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobS2CPayload;
import dev.loomstudios.network.payload.ProjectNeededS2CPayload;
import dev.loomstudios.network.payload.ProjectRequestC2SPayload;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectCodec;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientCosmeticSync {
    private static final Map<String, LoomProject> PROJECTS = new HashMap<>();
    private static final Map<UUID, String> EQUIPPED = new HashMap<>();

    private static UUID localPlayerId;
    private static String announcedLocalHash;
    private static boolean serverSupportsLoom;
    private static boolean helloPending;

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
        helloPending = true;
        tick(client);
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        ensureLocalProject(client.player.getUUID());

        String currentHash = ClientProjectWorkspace.projectHash();
        boolean hashChanged = !currentHash.equals(announcedLocalHash);

        if (!helloPending && !hashChanged) {
            return;
        }

        serverSupportsLoom = ClientPlayNetworking.canSend(HelloC2SPayload.ID);
        helloPending = false;

        if (serverSupportsLoom) {
            ClientPlayNetworking.send(
                    new HelloC2SPayload(
                            LoomNetworking.PROTOCOL_VERSION,
                            currentHash
                    )
            );
            announcedLocalHash = currentHash;

            LoomStudios.LOGGER.info(
                    "Loom project hello sent: {}",
                    shortHash(currentHash)
            );
        } else {
            announcedLocalHash = currentHash;
            LoomStudios.LOGGER.info(
                    "Server does not advertise Loom Studios networking; local-only cosmetics remain available."
            );
        }
    }

    public static void ensureLocalProject(UUID playerId) {
        if (localPlayerId != null && playerId.equals(localPlayerId)) {
            ClientProjectWorkspace.ensure(playerId);
            return;
        }

        localPlayerId = playerId;
        ClientProjectWorkspace.ensure(playerId);
    }

    public static LoomProject projectFor(UUID playerId) {
        if (playerId != null
                && playerId.equals(localPlayerId)
                && ClientProjectWorkspace.isInitialized()) {
            return ClientProjectWorkspace.project();
        }

        String hash = EQUIPPED.get(playerId);
        return hash == null ? null : PROJECTS.get(hash);
    }

    public static String projectHashFor(UUID playerId) {
        if (playerId != null
                && playerId.equals(localPlayerId)
                && ClientProjectWorkspace.isInitialized()) {
            return ClientProjectWorkspace.projectHash();
        }

        return EQUIPPED.get(playerId);
    }

    public static boolean serverSupportsLoom() {
        return serverSupportsLoom;
    }

    private static void sendLocalProjectIfRequested(String hash) {
        if (!serverSupportsLoom
                || !ClientProjectWorkspace.isInitialized()
                || !ClientProjectWorkspace.projectHash().equals(hash)
                || !ClientPlayNetworking.canSend(ProjectBlobC2SPayload.ID)) {
            return;
        }

        LoomProject project = ClientProjectWorkspace.project();
        ClientPlayNetworking.send(
                new ProjectBlobC2SPayload(hash, project.encode())
        );
    }

    private static void acceptProjectBlob(String hash, byte[] data) {
        if (!LoomProjectCodec.isValidHash(hash)
                || data.length == 0
                || data.length > LoomProjectCodec.MAX_SERIALIZED_BYTES
                || !hash.equals(LoomProjectCodec.sha256(data))) {
            LoomStudios.LOGGER.warn("Rejected invalid Loom project blob from server.");
            return;
        }

        try {
            PROJECTS.put(hash, LoomProjectCodec.decode(data));
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

        if (!LoomProjectCodec.isValidHash(payload.projectHash())) {
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
        helloPending = false;

        announcedLocalHash = null;
    }

    private static String shortHash(String hash) {
        return hash.substring(0, Math.min(12, hash.length()));
    }
}
