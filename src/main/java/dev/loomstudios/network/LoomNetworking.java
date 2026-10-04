package dev.loomstudios.network;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.network.payload.EquippedStateS2CPayload;
import dev.loomstudios.network.payload.HelloC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobC2SPayload;
import dev.loomstudios.network.payload.ProjectBlobS2CPayload;
import dev.loomstudios.network.payload.ProjectNeededS2CPayload;
import dev.loomstudios.network.payload.ProjectRequestC2SPayload;
import dev.loomstudios.project.LoomProjectCodec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** SPIKE-05 server-authoritative cosmetic synchronization proof. */
public final class LoomNetworking {
    public static final int PROTOCOL_VERSION = 3;

    private static final dev.loomstudios.project.BoundedCache<String, byte[]> PROJECTS =
            new dev.loomstudios.project.BoundedCache<>(
                    64L * 1024 * 1024, 64, bytes -> bytes.length);
    private static final Map<UUID, Long> LAST_UPLOAD = new HashMap<>();
    private static final Map<UUID, Long> LAST_DOWNLOAD = new HashMap<>();
    private static final java.util.Set<UUID> COMPATIBLE = new java.util.HashSet<>();
    private static final Map<UUID, String> EXPECTED = new HashMap<>();
    private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();
    private static final java.util.Set<UUID> UPLOADING = new java.util.HashSet<>();
    private static java.util.concurrent.ThreadPoolExecutor validator;

    private static final Map<UUID, String> EQUIPPED = new HashMap<>();

    private LoomNetworking() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(HelloC2SPayload.ID, HelloC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S()
                .registerLarge(
                        ProjectBlobC2SPayload.ID,
                        ProjectBlobC2SPayload.CODEC,
                        LoomProjectCodec.MAX_NETWORK_PAYLOAD_BYTES);
        PayloadTypeRegistry.playC2S()
                .register(ProjectRequestC2SPayload.ID, ProjectRequestC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C()
                .register(ProjectNeededS2CPayload.ID, ProjectNeededS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C()
                .registerLarge(
                        ProjectBlobS2CPayload.ID,
                        ProjectBlobS2CPayload.CODEC,
                        LoomProjectCodec.MAX_NETWORK_PAYLOAD_BYTES);
        PayloadTypeRegistry.playS2C()
                .register(EquippedStateS2CPayload.ID, EquippedStateS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                HelloC2SPayload.ID,
                (payload, context) ->
                        context.player()
                                .level()
                                .getServer()
                                .execute(() -> handleHello(context.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                ProjectBlobC2SPayload.ID,
                (payload, context) ->
                        context.player()
                                .level()
                                .getServer()
                                .execute(() -> handleProjectUpload(context.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                ProjectRequestC2SPayload.ID,
                (payload, context) ->
                        context.player()
                                .level()
                                .getServer()
                                .execute(() -> handleProjectRequest(context.player(), payload)));

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    UUID playerId = handler.player.getUUID();
                    LAST_UPLOAD.remove(playerId);
                    LAST_DOWNLOAD.remove(playerId);
                    COMPATIBLE.remove(playerId);
                    EXPECTED.remove(playerId);
                    LAST_REQUEST.remove(playerId);
                    UPLOADING.remove(playerId);
                    if (EQUIPPED.remove(playerId) != null) {
                        broadcast(server, new EquippedStateS2CPayload(playerId, ""));
                    }
                });

        ServerLifecycleEvents.SERVER_STARTING.register(
                server -> {
                    validator =
                            new java.util.concurrent.ThreadPoolExecutor(
                                    1,
                                    1,
                                    30,
                                    java.util.concurrent.TimeUnit.SECONDS,
                                    new java.util.concurrent.ArrayBlockingQueue<>(4),
                                    task -> {
                                        Thread thread = new Thread(task, "loom-project-validator");
                                        thread.setDaemon(true);
                                        return thread;
                                    },
                                    new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());
                });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    long now = System.nanoTime();
                    for (var entry : EXPECTED.entrySet()) {
                        UUID id = entry.getKey();
                        var player = server.getPlayerList().getPlayer(id);
                        if (player == null
                                || UPLOADING.contains(id)
                                || now - LAST_REQUEST.getOrDefault(id, 0L) < 1_000_000_000L
                                || now - LAST_UPLOAD.getOrDefault(id, 0L) < 1_000_000_000L)
                            continue;
                        if (ServerPlayNetworking.canSend(player, ProjectNeededS2CPayload.ID)) {
                            LAST_REQUEST.put(id, now);
                            ServerPlayNetworking.send(
                                    player, new ProjectNeededS2CPayload(entry.getValue()));
                        }
                    }
                });
        ServerLifecycleEvents.SERVER_STOPPED.register(
                server -> {
                    if (validator != null) {
                        validator.shutdownNow();
                        validator = null;
                    }
                    PROJECTS.clear();
                    EQUIPPED.clear();
                    LAST_UPLOAD.clear();
                    LAST_DOWNLOAD.clear();
                    COMPATIBLE.clear();
                    EXPECTED.clear();
                    LAST_REQUEST.clear();
                    UPLOADING.clear();
                });

        LoomStudios.LOGGER.info("SPIKE-05 networking payloads registered.");
    }

    private static void handleHello(ServerPlayer player, HelloC2SPayload payload) {
        if (payload.protocolVersion() != PROTOCOL_VERSION
                || !LoomProjectCodec.isValidHash(payload.projectHash())) {
            LoomStudios.LOGGER.warn(
                    "Rejected Loom hello from {}: protocol/hash invalid",
                    player.getGameProfile().name());
            return;
        }

        COMPATIBLE.add(player.getUUID());
        syncExistingStatesTo(player);

        if (PROJECTS.containsKey(payload.projectHash())) {
            equip(player, payload.projectHash());
            EXPECTED.remove(player.getUUID());
        } else {
            EXPECTED.put(player.getUUID(), payload.projectHash());
        }
    }

    private static void handleProjectUpload(ServerPlayer player, ProjectBlobC2SPayload payload) {
        UUID id = player.getUUID();
        long now = System.nanoTime();
        if (!COMPATIBLE.contains(id)
                || !payload.projectHash().equals(EXPECTED.get(id))
                || UPLOADING.contains(id)
                || now - LAST_UPLOAD.getOrDefault(id, 0L) < 1_000_000_000L
                || validator == null) return;
        UPLOADING.add(id);
        LAST_UPLOAD.put(id, now);
        var server = player.level().getServer();
        var worker = validator;
        try {
            worker.execute(
                    () -> {
                        boolean valid = validateProjectBlob(payload.projectHash(), payload.data());
                        if (worker.isShutdown()) return;
                        server.execute(
                                () -> {
                                    UPLOADING.remove(id);
                                    if (server.getPlayerList().getPlayer(id) != player
                                            || !COMPATIBLE.contains(id)
                                            || !payload.projectHash().equals(EXPECTED.get(id)))
                                        return;
                                    if (!valid) {
                                        EXPECTED.remove(id);
                                        LoomStudios.LOGGER.warn(
                                                "Rejected invalid Loom project from {}",
                                                player.getGameProfile().name());
                                        return;
                                    }
                                    PROJECTS.put(
                                            payload.projectHash(),
                                            Arrays.copyOf(payload.data(), payload.data().length));
                                    EXPECTED.remove(id);
                                    equip(player, payload.projectHash());
                                    LoomStudios.LOGGER.info(
                                            "Cached Loom project {} from {} ({} bytes)",
                                            shortHash(payload.projectHash()),
                                            player.getGameProfile().name(),
                                            payload.data().length);
                                });
                    });
        } catch (java.util.concurrent.RejectedExecutionException busy) {
            UPLOADING.remove(id);
        }
    }

    private static void handleProjectRequest(
            ServerPlayer player, ProjectRequestC2SPayload payload) {
        if (!COMPATIBLE.contains(player.getUUID())
                || !LoomProjectCodec.isValidHash(payload.projectHash())) {
            return;
        }

        long now = System.nanoTime();
        UUID id = player.getUUID();
        if (now - LAST_DOWNLOAD.getOrDefault(id, 0L) < 250_000_000L) return;
        LAST_DOWNLOAD.put(id, now);
        byte[] data = PROJECTS.get(payload.projectHash());
        if (data == null) {
            // Recover an evicted equipped design from its still-connected owner.
            EQUIPPED.forEach(
                    (owner, hash) -> {
                        if (hash.equals(payload.projectHash()) && COMPATIBLE.contains(owner))
                            EXPECTED.putIfAbsent(owner, hash);
                    });
        }
        if (data != null && ServerPlayNetworking.canSend(player, ProjectBlobS2CPayload.ID)) {
            ServerPlayNetworking.send(
                    player,
                    new ProjectBlobS2CPayload(
                            payload.projectHash(), Arrays.copyOf(data, data.length)));
        }
    }

    private static boolean validateProjectBlob(String hash, byte[] data) {
        if (!LoomProjectCodec.isValidHash(hash)
                || data.length == 0
                || data.length > LoomProjectCodec.MAX_SERIALIZED_BYTES
                || !hash.equals(LoomProjectCodec.sha256(data))) {
            return false;
        }

        try {
            LoomProjectCodec.decode(data);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static void equip(ServerPlayer player, String hash) {
        EQUIPPED.put(player.getUUID(), hash);
        broadcast(player.level().getServer(), new EquippedStateS2CPayload(player.getUUID(), hash));
    }

    private static void syncExistingStatesTo(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, EquippedStateS2CPayload.ID)) {
            return;
        }

        EQUIPPED.forEach(
                (playerId, hash) ->
                        ServerPlayNetworking.send(
                                player, new EquippedStateS2CPayload(playerId, hash)));
    }

    private static void broadcast(MinecraftServer server, EquippedStateS2CPayload payload) {
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            if (COMPATIBLE.contains(target.getUUID())
                    && ServerPlayNetworking.canSend(target, EquippedStateS2CPayload.ID)) {
                ServerPlayNetworking.send(target, payload);
            }
        }
    }

    private static String shortHash(String hash) {
        return hash.substring(0, Math.min(12, hash.length()));
    }
}
