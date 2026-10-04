package dev.loomstudios.project;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Phase-1 undo/redo foundation.
 *
 * <p>Edits are expressed as project transformations. For now history stores immutable project
 * snapshots; later high-volume tools may use specialized deltas without changing the editor-facing
 * API.
 */
public final class ProjectHistory {
    private static final int DEFAULT_LIMIT = 128;

    private static final long MEMORY_LIMIT = 96L * 1024 * 1024;
    private final int limit;
    private final Deque<LoomProject> undo = new ArrayDeque<>();
    private final Deque<LoomProject> redo = new ArrayDeque<>();
    private LoomProject current;

    private boolean compoundActive;
    private boolean compoundChanged;
    private LoomProject compoundBase;

    public ProjectHistory(LoomProject initial) {
        this(initial, DEFAULT_LIMIT);
    }

    public ProjectHistory(LoomProject initial, int limit) {
        this.current = Objects.requireNonNull(initial, "initial");
        if (limit <= 0) {
            throw new IllegalArgumentException("History limit must be positive");
        }
        this.limit = limit;
    }

    public LoomProject current() {
        return current;
    }

    public LoomProject apply(UnaryOperator<LoomProject> edit) {
        LoomProject next =
                Objects.requireNonNull(
                        Objects.requireNonNull(edit, "edit").apply(current), "edit result");

        if (next.equals(current)) {
            return current;
        }

        if (compoundActive) {
            current = next;
            compoundChanged = true;
            redo.clear();
            return current;
        }

        pushUndo(current);
        current = next;
        redo.clear();
        trimMemory();
        return current;
    }

    public void beginCompoundEdit() {
        if (compoundActive) {
            return;
        }

        compoundActive = true;
        compoundChanged = false;
        compoundBase = current;
    }

    public void endCompoundEdit() {
        if (!compoundActive) {
            return;
        }

        if (compoundChanged && compoundBase != null) {
            pushUndo(compoundBase);
        }

        compoundActive = false;
        compoundChanged = false;
        compoundBase = null;
        trimMemory();
    }

    public boolean isCompoundEditActive() {
        return compoundActive;
    }

    private void pushUndo(LoomProject project) {
        undo.addLast(project);
        while (undo.size() > limit) {
            undo.removeFirst();
        }
    }

    private void trimMemory() {
        while (!undo.isEmpty() && ProjectMemory.retainedBytes(undo, current) > MEMORY_LIMIT)
            undo.removeFirst();
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public LoomProject undo() {
        endCompoundEdit();

        if (!canUndo()) {
            return current;
        }

        redo.addLast(current);
        current = undo.removeLast();
        return current;
    }

    public LoomProject redo() {
        endCompoundEdit();

        if (!canRedo()) {
            return current;
        }

        undo.addLast(current);
        current = redo.removeLast();
        return current;
    }
}
