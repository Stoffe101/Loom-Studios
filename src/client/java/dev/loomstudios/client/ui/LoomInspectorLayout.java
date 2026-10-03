package dev.loomstudios.client.ui;

import dev.loomstudios.ui.LoomWorkspaceLayout.Rect;

/** Content rows fail fast when a page exceeds its allocated inspector. */
public final class LoomInspectorLayout {
    private final Rect bounds;
    private int cursor;
    public LoomInspectorLayout(Rect bounds) { this.bounds = bounds; cursor = bounds.top() + 6; }
    public Rect row(int height) {
        Rect row = new Rect(bounds.left() + 6, cursor, bounds.right() - 6, cursor + height);
        if (row.bottom() > bounds.bottom() - 4) throw new IllegalStateException("Inspector page overflow");
        cursor += height + 4;
        return row;
    }
}
