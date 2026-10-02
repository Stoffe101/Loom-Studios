package dev.loomstudios.project;

import java.util.Objects;
import java.util.UUID;

public record LoomProject(
        int schemaVersion,
        UUID projectId,
        String name,
        LoomProjectMetadata metadata,
        LoomCanvas cape,
        LoomCanvas elytra,
        LoomRuntimeSettings runtime,
        LoomAnimation animation
) {
    public static final int CURRENT_SCHEMA_VERSION = 3;
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    public LoomProject {
        if (schemaVersion < 1 || schemaVersion > CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Unsupported Loom project schema " + schemaVersion
            );
        }

        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(cape, "cape");
        Objects.requireNonNull(elytra, "elytra");
        Objects.requireNonNull(runtime, "runtime");
        Objects.requireNonNull(animation, "animation");

        if (name.isBlank()
                || name.length()
                > LoomProjectCodec.MAX_PROJECT_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid project name");
        }

        validateRuntimeCanvas("cape", cape);
        validateRuntimeCanvas("elytra", elytra);
        validateAnimationReferences(animation, cape, elytra);
    }

    private static void validateAnimationReferences(
            LoomAnimation animation,
            LoomCanvas cape,
            LoomCanvas elytra
    ) {
        java.util.Set<UUID> capeIds = cape.layers().stream()
                .map(LoomLayer::id)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<UUID> elytraIds = elytra.layers().stream()
                .map(LoomLayer::id)
                .collect(java.util.stream.Collectors.toSet());

        for (AnimationTrack track : animation.tracks()) {
            boolean exists = switch (track.channel()) {
                case CAPE -> capeIds.contains(track.layerId());
                case ELYTRA -> elytraIds.contains(track.layerId());
            };

            if (!exists) {
                throw new IllegalArgumentException(
                        "Animation track references missing "
                                + track.channel().displayName()
                                + " layer "
                                + track.layerId()
                );
            }
        }
    }

    public LoomProject(
            int schemaVersion,
            UUID projectId,
            String name,
            LoomProjectMetadata metadata,
            LoomCanvas cape,
            LoomCanvas elytra,
            LoomRuntimeSettings runtime
    ) {
        this(
                schemaVersion,
                projectId,
                name,
                metadata,
                cape,
                elytra,
                runtime,
                LoomAnimation.empty()
        );
    }

    private static void validateRuntimeCanvas(
            String label,
            LoomCanvas canvas
    ) {
        try {
            CanvasResolution.fromCanvas(canvas);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    label
                            + " canvas must use a supported Loom resolution: "
                            + "64x32, 128x64, or 256x128",
                    e
            );
        }
    }

    public LoomProject withCape(LoomCanvas nextCape) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                metadata,
                Objects.requireNonNull(nextCape, "nextCape"),
                elytra,
                runtime,
                animation
        );
    }

    public LoomProject withElytra(LoomCanvas nextElytra) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                metadata,
                cape,
                Objects.requireNonNull(nextElytra, "nextElytra"),
                runtime,
                animation
        );
    }

    public LoomProject withMetadata(
            LoomProjectMetadata nextMetadata
    ) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                Objects.requireNonNull(nextMetadata, "nextMetadata"),
                cape,
                elytra,
                runtime,
                animation
        );
    }

    public LoomProject withName(String nextName) {
        return new LoomProject(
                schemaVersion,
                projectId,
                nextName,
                metadata,
                cape,
                elytra,
                runtime,
                animation
        );
    }

    public LoomProject withRuntime(
            LoomRuntimeSettings nextRuntime
    ) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                metadata,
                cape,
                elytra,
                Objects.requireNonNull(nextRuntime, "nextRuntime"),
                animation
        );
    }

    public LoomProject withAnimation(
            LoomAnimation nextAnimation
    ) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                metadata,
                cape,
                elytra,
                runtime,
                Objects.requireNonNull(
                        nextAnimation,
                        "nextAnimation"
                )
        );
    }

    public byte[] encode() {
        return LoomProjectCodec.encode(this);
    }

    public String hash() {
        return LoomProjectCodec.sha256(this.encode());
    }
}
