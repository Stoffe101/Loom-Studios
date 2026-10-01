package dev.loomstudios.client.project;

import java.nio.file.Path;
import java.util.UUID;

public record ProjectDescriptor(
        UUID projectId,
        String name,
        long createdAtEpochMillis,
        long modifiedAtEpochMillis,
        String contentHash,
        Path projectPath,
        Path thumbnailPath
) {
}
