package dev.loomstudios.client.project;

import dev.loomstudios.project.LoomProject;

import java.nio.file.Path;

public record WorkspaceState(
        LoomProject project,
        long revision,
        boolean dirty,
        Path sourcePath,
        LoomProject equippedProject,
        String equippedHash
) {
}
