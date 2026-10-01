package dev.loomstudios.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ProjectSessionTest {
    private static final UUID PLAYER_ID =
            UUID.fromString("22222222-2222-4222-8222-222222222222");

    @TempDir
    Path tempDir;

    @Test
    void saveLoadAndDirtyLifecycleWork() throws Exception {
        ProjectFileStore store = new ProjectFileStore(tempDir);
        AtomicLong clock = new AtomicLong(1000L);
        LoomProject project = LoomProjectFactory.forPlayer(PLAYER_ID);

        ProjectSession session = new ProjectSession(
                project,
                store,
                clock::get
        );

        assertTrue(session.isDirty());

        Path saved = session.save();
        assertFalse(session.isDirty());
        assertEquals(store.pathFor(project.projectId()), saved);

        clock.set(2000L);
        session.apply(current -> current.withName("Edited"));

        assertTrue(session.isDirty());
        assertEquals(2000L, session.project().metadata().modifiedAtEpochMillis());
        assertTrue(session.canUndo());

        session.undo();
        assertFalse(session.isDirty());

        session.redo();
        assertTrue(session.isDirty());

        session.save();
        assertFalse(session.isDirty());

        ProjectSession loaded = ProjectSession.load(saved, store);
        assertFalse(loaded.isDirty());
        assertEquals(session.project(), loaded.project());
    }

    @Test
    void fileStoreRejectsOutsidePath() {
        ProjectFileStore store = new ProjectFileStore(tempDir.resolve("library"));
        Path outside = tempDir.resolve("outside.loom");

        assertThrows(
                IllegalArgumentException.class,
                () -> store.load(outside)
        );
    }

    @Test
    void listOnlyReturnsLoomProjects() throws Exception {
        ProjectFileStore store = new ProjectFileStore(tempDir);
        LoomProject first = LoomProjectFactory.forPlayer(PLAYER_ID);
        LoomProject second = LoomProjectFactory.forPlayer(
                UUID.fromString("33333333-3333-4333-8333-333333333333")
        );

        store.save(first);
        store.save(second);
        java.nio.file.Files.writeString(tempDir.resolve("notes.txt"), "ignore");

        assertEquals(2, store.list().size());
    }
}
