package dev.loomstudios.client.ui;

import dev.loomstudios.ui.LoomWorkspaceLayout.Rect;

/** Content rows fail fast when a page exceeds its allocated inspector. */
public final class LoomInspectorLayout {
    private final Rect bounds;
    private int cursor;
    public LoomInspectorLayout(Rect bounds) { this.bounds = bounds; cursor = bounds.top() + padding(); }
    public int padding() { return bounds.height() < 162 ? 4 : 6; }
    public int gap() { return bounds.height() < 162 ? 3 : 4; }
    public int controlHeight() { return bounds.height() < 162 ? 19 : 22; }
    public int stride() { return controlHeight() + gap(); }
    public Rect row(int height) {
        if (height == 22) height = controlHeight();
        Rect row = new Rect(bounds.left() + padding(), cursor, bounds.right() - padding(), cursor + height);
        if (row.bottom() > bounds.bottom() - 4) throw new IllegalStateException("Inspector page overflow");
        cursor += height + gap();
        return row;
    }
}
