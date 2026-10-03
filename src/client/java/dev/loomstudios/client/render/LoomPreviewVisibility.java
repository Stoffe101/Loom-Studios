package dev.loomstudios.client.render;

/** Flag belongs to the deferred preview snapshot, never the live player. */
public interface LoomPreviewVisibility {
    boolean loom$characterHidden();
    void loom$characterHidden(boolean value);
}
