package dev.loomstudios.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.UnaryOperator;

/**
 * Owns one editable project session: history, dirty state and persistence.
 */
public final class ProjectSession {
    private final ProjectFileStore store;
    private final LongSupplier clock;

    private ProjectHistory history;
    private Path sourcePath;
    private String savedHash;
    private LoomProject savedProject;
    private long revision;

    private long cachedHashRevision = Long.MIN_VALUE;
    private String cachedHash;

    public ProjectSession(
            LoomProject project,
            ProjectFileStore store
    ) {
        this(project, store, System::currentTimeMillis);
    }

    ProjectSession(
            LoomProject project,
            ProjectFileStore store,
            LongSupplier clock
    ) {
        this.store = Objects.requireNonNull(store, "store");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.history = new ProjectHistory(
                Objects.requireNonNull(project, "project")
        );
    }

    public static ProjectSession load(
            Path path,
            ProjectFileStore store
    ) throws IOException {
        LoomProject project = store.load(path);
        ProjectSession session = new ProjectSession(project, store);
        session.sourcePath = path.toAbsolutePath().normalize();
        session.savedHash = project.hash();
        session.savedProject = project;
        session.cachedHashRevision = session.revision;
        session.cachedHash = session.savedHash;
        return session;
    }

    public LoomProject project() {
        return history.current();
    }

    public long revision() {
        return revision;
    }

    public Path sourcePath() {
        return sourcePath;
    }

    public boolean isDirty() {
        return savedProject != project();
    }

    public String currentHash() {
        if (cachedHash == null || cachedHashRevision != revision) {
            cachedHash = project().hash();
            cachedHashRevision = revision;
        }

        return cachedHash;
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    public void beginCompoundEdit() {
        history.beginCompoundEdit();
    }

    public void endCompoundEdit() {
        history.endCompoundEdit();
    }

    public boolean isCompoundEditActive() {
        return history.isCompoundEditActive();
    }

    public LoomProject apply(UnaryOperator<LoomProject> edit) {
        Objects.requireNonNull(edit, "edit");
        LoomProject before = project();
        LoomProject edited = Objects.requireNonNull(
                edit.apply(before),
                "edit result"
        );

        if (edited.equals(before)) {
            return before;
        }

        LoomProject touched = edited.withMetadata(
                edited.metadata().touch(clock.getAsLong())
        );

        LoomProject result = history.apply(ignored -> touched);
        revision++;
        cachedHash = null;
        return result;
    }

    public LoomProject undo() {
        LoomProject before = project();
        LoomProject result = history.undo();

        if (!result.equals(before)) {
            revision++;
            cachedHash = null;
        }

        return result;
    }

    public LoomProject redo() {
        LoomProject before = project();
        LoomProject result = history.redo();

        if (!result.equals(before)) {
            revision++;
            cachedHash = null;
        }

        return result;
    }

    public Path save() throws IOException {
        Path saved = store.save(project());
        this.sourcePath = saved;
        this.savedHash = currentHash();
        this.savedProject = project();
        return saved;
    }
}
