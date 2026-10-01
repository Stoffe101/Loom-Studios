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
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    public LoomProject {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
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
        if (canvas.width() != TEXTURE_WIDTH || canvas.height() != TEXTURE_HEIGHT) {
            throw new IllegalArgumentException(
                    label + " canvas must be "
                            + TEXTURE_WIDTH
                            + "x"
                            + TEXTURE_HEIGHT
                            + " in schema v1"
            );
        }
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

    public byte[] encode() {
        return LoomProjectCodec.encode(this);
    }

    public String hash() {
        return LoomProjectCodec.sha256(this.encode());
    }
}
