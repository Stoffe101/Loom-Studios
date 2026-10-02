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
        LoomRuntimeSettings runtime
) {
    public static final int CURRENT_SCHEMA_VERSION = 2;
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

        if (name.isBlank() || name.length() > LoomProjectCodec.MAX_PROJECT_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid project name");
        }

        validateRuntimeCanvas("cape", cape);
        validateRuntimeCanvas("elytra", elytra);
    }

    private static void validateRuntimeCanvas(String label, LoomCanvas canvas) {
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
                runtime
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
                runtime
        );
    }

    public LoomProject withMetadata(LoomProjectMetadata nextMetadata) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                Objects.requireNonNull(nextMetadata, "nextMetadata"),
                cape,
                elytra,
                runtime
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
                runtime
        );
    }

    public LoomProject withRuntime(LoomRuntimeSettings nextRuntime) {
        return new LoomProject(
                schemaVersion,
                projectId,
                name,
                metadata,
                cape,
                elytra,
                Objects.requireNonNull(nextRuntime, "nextRuntime")
        );
    }

    public byte[] encode() {
        return LoomProjectCodec.encode(this);
    }

    public String hash() {
        return LoomProjectCodec.sha256(this.encode());
    }
}
