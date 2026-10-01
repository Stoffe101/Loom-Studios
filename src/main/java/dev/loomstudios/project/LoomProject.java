package dev.loomstudios.project;

import java.util.Objects;
import java.util.UUID;

public record LoomProject(
        int schemaVersion,
        UUID projectId,
        String name,
        LoomCanvas cape,
        LoomCanvas elytra,
        LoomRuntimeSettings runtime
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public LoomProject {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Unsupported Loom project schema " + schemaVersion
            );
        }

        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(cape, "cape");
        Objects.requireNonNull(elytra, "elytra");
        Objects.requireNonNull(runtime, "runtime");

        if (name.isBlank() || name.length() > LoomProjectCodec.MAX_PROJECT_NAME_CHARS) {
            throw new IllegalArgumentException("Invalid project name");
        }
    }

    public byte[] encode() {
        return LoomProjectCodec.encode(this);
    }

    public String hash() {
        return LoomProjectCodec.sha256(this.encode());
    }
}
