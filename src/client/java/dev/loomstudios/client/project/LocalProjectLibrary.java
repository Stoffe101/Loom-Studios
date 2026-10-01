package dev.loomstudios.client.project;

import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectCodec;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

/**
 * Local .loom project library foundation.
 */
public final class LocalProjectLibrary {
    private static final Path ROOT = FabricLoader.getInstance()
            .getGameDir()
            .resolve("loom-studios")
            .resolve("projects");

    private LocalProjectLibrary() {
    }

    public static Path root() {
        return ROOT;
    }

    public static Path save(LoomProject project) throws IOException {
        Files.createDirectories(ROOT);

        Path target = ROOT.resolve(project.projectId() + ".loom");
        Path temporary = ROOT.resolve(project.projectId() + ".loom.tmp");
        Files.write(temporary, project.encode());

        try {
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(
                    temporary,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return target;
    }

    public static LoomProject load(Path path) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        Path root = ROOT.toAbsolutePath().normalize();

        if (!normalized.startsWith(root)) {
            throw new IllegalArgumentException("Project path is outside Loom Studios library");
        }

        long size = Files.size(normalized);
        if (size <= 0 || size > LoomProjectCodec.MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Project file size out of range");
        }

        return LoomProjectCodec.decode(Files.readAllBytes(normalized));
    }

    public static List<Path> list() throws IOException {
        Files.createDirectories(ROOT);

        try (var stream = Files.list(ROOT)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".loom"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }
}
