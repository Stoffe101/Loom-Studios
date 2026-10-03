package dev.loomstudios.client.project;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectFactory;
import dev.loomstudios.project.ProjectSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Sole owner of the local editable project session.
 *
 * <p>The editor session and the world/multiplayer equipped project are
 * deliberately separate. Unsaved edits can update the editor preview without
 * silently changing what other players see.</p>
 */
public final class ClientProjectWorkspace {
    private static final CopyOnWriteArrayList<Consumer<WorkspaceState>> LISTENERS =
            new CopyOnWriteArrayList<>();

    private static UUID playerId;
    private static ProjectSession session;

    private static LoomProject equippedProject;
    private static String equippedHash;

    private ClientProjectWorkspace() {
    }

    public static ProjectSession ensure(UUID localPlayerId) {
        Objects.requireNonNull(localPlayerId, "localPlayerId");

        if (session != null) {
            playerId = localPlayerId;
            return session;
        }

        playerId = localPlayerId;
        LoomProject development = LoomProjectFactory.forPlayer(localPlayerId);
        session = new ProjectSession(
                development,
                LocalProjectLibrary.store()
        );

        // Development bootstrap only: give the player a visible cosmetic even
        // before the real editor/library flow chooses a saved project.
        equippedProject = development;
        equippedHash = development.hash();

        LoomStudios.LOGGER.info(
                "Created Loom project workspace for {}",
                localPlayerId
        );
        notifyListeners();
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
        return session().currentHash();
    }

    public static long revision() {
        return session().revision();
    }

    public static boolean isDirty() {
        return session().isDirty();
    }

    public static LoomProject equippedProject() {
        if (equippedProject == null) {
            throw new IllegalStateException("No Loom project is equipped");
        }
        return equippedProject;
    }

    public static String equippedProjectHash() {
        if (equippedHash == null) {
            throw new IllegalStateException("No Loom project is equipped");
        }
        return equippedHash;
    }

    public static boolean isCurrentProjectEquipped() {
        return equippedProject != null && equippedProject == project();
    }

    public static void beginCompoundEdit() {
        session().beginCompoundEdit();
    }

    public static void endCompoundEdit() {
        session().endCompoundEdit();
        notifyListeners();
    }

    public static LoomProject apply(UnaryOperator<LoomProject> edit) {
        WorkspaceRecovery.editingStarted=true;
        LoomProject result = session().apply(edit);
        notifyListeners();
        return result;
    }

    public static LoomProject undo() {
        LoomProject result = session().undo();
        notifyListeners();
        return result;
    }

    public static LoomProject redo() {
        LoomProject result = session().redo();
        notifyListeners();
        return result;
    }

    public static Path save() throws IOException {
        Path saved = session().save();
        WorkspaceRecovery.saved(project());
        ProjectLibraryIndex.refresh();
        notifyListeners();
        return saved;
    }

    public static Path saveAndEquip() throws IOException {
        Path saved = save();
        equipCurrent();
        return saved;
    }

    public static void discardChanges() throws IOException {
        java.util.UUID id=project().projectId();
        ProjectSession restored=session;
        Path canonical=LocalProjectLibrary.store().pathFor(id);
        if(session.sourcePath()==null&&java.nio.file.Files.isRegularFile(canonical))restored=ProjectSession.load(canonical,LocalProjectLibrary.store());
        WorkspaceRecovery.discardDraft(id);
        session=restored;session.discardChanges();notifyListeners();
    }

    public static void equipCurrent() {
        if (session().isDirty() || session().sourcePath()==null) {
            throw new IllegalStateException(
                    "Save the Loom project before equipping it"
            );
        }

        equippedProject = project();
        equippedHash = equippedProject.hash();
        notifyListeners();
    }

    public static void createBlank(
            String name,
            long nowEpochMillis,
            UUID localPlayerId
    ) {
        Objects.requireNonNull(localPlayerId, "localPlayerId");
        WorkspaceRecovery.checkpoint();
        WorkspaceRecovery.editingStarted=true;
        playerId = localPlayerId;
        session = new ProjectSession(
                LoomProjectFactory.blank(name, nowEpochMillis),
                LocalProjectLibrary.store()
        );
        notifyListeners();
    }

    public static void open(Path path, UUID localPlayerId) throws IOException {
        Objects.requireNonNull(localPlayerId, "localPlayerId");
        WorkspaceRecovery.checkpoint();
        WorkspaceRecovery.editingStarted=true;
        session = ProjectSession.load(path, LocalProjectLibrary.store());
        playerId = localPlayerId;
        ProjectLibraryIndex.refresh();
        notifyListeners();
    }

    public static void replaceWith(LoomProject project,UUID localPlayerId) {
        WorkspaceRecovery.checkpoint();WorkspaceRecovery.editingStarted=true;session=new ProjectSession(project,LocalProjectLibrary.store());playerId=localPlayerId;notifyListeners();
    }
    public static void recover(Path path, UUID localPlayerId) throws IOException {
        WorkspaceRecovery.checkpoint();
        WorkspaceRecovery.editingStarted=true;
        session = new ProjectSession(WorkspaceRecovery.STORE.load(path),LocalProjectLibrary.store());
        playerId=localPlayerId; notifyListeners();
    }

    public static void bindPlayer(UUID localPlayerId) {
        Objects.requireNonNull(localPlayerId, "localPlayerId");
        playerId = localPlayerId;

        if (session == null) {
            ensure(localPlayerId);
        }
    }

    public static WorkspaceState state() {
        return new WorkspaceState(
                project(),
                revision(),
                isDirty(),
                session().sourcePath(),
                equippedProject,
                equippedHash
        );
    }

    public static void addListener(Consumer<WorkspaceState> listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));

        if (session != null) {
            listener.accept(state());
        }
    }

    public static void removeListener(Consumer<WorkspaceState> listener) {
        LISTENERS.remove(listener);
    }

    private static void notifyListeners() {
        if (session == null) {
            return;
        }

        WorkspaceState snapshot = state();
        for (Consumer<WorkspaceState> listener : LISTENERS) {
            listener.accept(snapshot);
        }
    }

    public static void resetForTestsAndShutdown() {
        playerId = null;
        session = null;
        equippedProject = null;
        equippedHash = null;
        LISTENERS.clear();
    }
}
