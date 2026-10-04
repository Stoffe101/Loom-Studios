package dev.loomstudios.project;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Filesystem store for editable .loom projects.
 *
 * <p>The root is injected so the core can be tested independently from
 * Minecraft/Fabric. The client LocalProjectLibrary supplies the game folder.</p>
 */
public final class ProjectFileStore {
    private final Path root;
    private final boolean keepHistory;

    public ProjectFileStore(Path root) {
        this(root,true);
    }
    public ProjectFileStore(Path root,boolean keepHistory) {
        this.keepHistory=keepHistory;
        this.root = Objects.requireNonNull(root, "root")
                .toAbsolutePath()
                .normalize();
    }

    public Path root() {
        return root;
    }

    public Path pathFor(UUID projectId) {
        return root.resolve(projectId + ".loom");
    }

    public Path save(LoomProject project) throws IOException {
        Objects.requireNonNull(project, "project");
        Files.createDirectories(root);

        Path target = pathFor(project.projectId());
        if(keepHistory&&Files.isRegularFile(target,java.nio.file.LinkOption.NOFOLLOW_LINKS)){
            LoomProject previous=load(target);
            if(!previous.hash().equals(project.hash()) || LoomProjectCodec.peekSchemaVersion(Files.readAllBytes(target)) != LoomProject.CURRENT_SCHEMA_VERSION)new ProjectVersions(this).backupSource(target,previous.projectId());
        }
        Path temporary = root.resolve(project.projectId() + ".loom.tmp");
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

    public LoomProject load(Path path) throws IOException {
        Path normalized = requireInsideRoot(path);

        long size = Files.size(normalized);
        if (size <= 0L || size > LoomProjectCodec.MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Project file size out of range");
        }

        return LoomProjectCodec.decode(Files.readAllBytes(normalized));
    }

    public List<Path> list() throws IOException {
        Files.createDirectories(root);

        try (var stream = Files.list(root)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".loom"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    /** Reversible deletion. The UUID remains stable and restore never overwrites a design. */
    public Path trash(UUID id) throws IOException {
        Path source = pathFor(id);
        if (!Files.isRegularFile(source, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new IOException("Design is missing or is not a regular file");
        Path directory = root.resolve("trash");
        Files.createDirectories(directory);
        Path destination = directory.resolve(id + ".loom");
        Files.move(source, destination);
        return destination;
    }

    public Path restore(UUID id) throws IOException {
        Path source = root.resolve("trash").resolve(id + ".loom");
        if (!Files.isRegularFile(source, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new IOException("Trashed design is missing");
        Files.move(source, pathFor(id));
        return pathFor(id);
    }

    public LoomProject rename(UUID id, String name, long now) throws IOException {
        LoomProject project = load(pathFor(id));
        LoomProject renamed = project.withName(name.strip())
                .withMetadata(project.metadata().touch(now));
        save(renamed);
        return renamed;
    }

    private Path requireInsideRoot(Path path) {
        Path normalized = Objects.requireNonNull(path, "path")
                .toAbsolutePath()
                .normalize();

        if (!normalized.startsWith(root)) {
            throw new IllegalArgumentException(
                    "Project path is outside Loom Studios library"
            );
        }

        return normalized;
    }
}
