package dev.loomstudios.client.project;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectFactory;
import dev.loomstudios.project.ProjectSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Owns the one local editable project session used by rendering, networking
 * and the future editor.
 */
public final class ClientProjectWorkspace {
    private static UUID playerId;
    private static ProjectSession session;

    private ClientProjectWorkspace() {
    }

    public static ProjectSession ensure(UUID localPlayerId) {
        Objects.requireNonNull(localPlayerId, "localPlayerId");

        if (session != null) {
            playerId = localPlayerId;
            return session;
        }

        playerId = localPlayerId;
        session = new ProjectSession(
                LoomProjectFactory.forPlayer(localPlayerId),
                LocalProjectLibrary.store()
        );

        LoomStudios.LOGGER.info(
                "Created Loom project workspace for {}",
                localPlayerId
        );
        return session;
    }

    public static boolean isInitialized() {
        return session != null;
    }

    public static boolean isLocalPlayer(UUID candidate) {
        return playerId != null && playerId.equals(candidate);
    }

    public static ProjectSession session() {
        if (session == null) {
            throw new IllegalStateException("Loom project workspace is not initialized");
        }
        return session;
    }

    public static LoomProject project() {
        return session().project();
    }

    public static String projectHash() {
        return project().hash();
    }

    public static long revision() {
        return session().revision();
    }

    public static LoomProject apply(UnaryOperator<LoomProject> edit) {
        return session().apply(edit);
    }

    public static LoomProject undo() {
        return session().undo();
    }

    public static LoomProject redo() {
        return session().redo();
    }

    public static Path save() throws IOException {
        Path saved = session().save();
        ProjectLibraryIndex.refresh();
        return saved;
    }

    public static void createBlank(
            String name,
            long nowEpochMillis,
            UUID localPlayerId
    ) {
        Objects.requireNonNull(localPlayerId, "localPlayerId");
        playerId = localPlayerId;
        session = new ProjectSession(
                LoomProjectFactory.blank(name, nowEpochMillis),
                LocalProjectLibrary.store()
        );
    }

    public static void open(Path path, UUID localPlayerId) throws IOException {
        Objects.requireNonNull(localPlayerId, "localPlayerId");
        session = ProjectSession.load(path, LocalProjectLibrary.store());
        playerId = localPlayerId;
        ProjectLibraryIndex.refresh();
    }

    public static void bindPlayer(UUID localPlayerId) {
        Objects.requireNonNull(localPlayerId, "localPlayerId");
        playerId = localPlayerId;

        if (session == null) {
            session = new ProjectSession(
                    LoomProjectFactory.forPlayer(localPlayerId),
                    LocalProjectLibrary.store()
            );
        }
    }

    public static void resetForTestsAndShutdown() {
        playerId = null;
        session = null;
    }
}
