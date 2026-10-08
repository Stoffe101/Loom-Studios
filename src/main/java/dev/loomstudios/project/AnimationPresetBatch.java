package dev.loomstudios.project;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * One transaction for an effect on several selected layers.
 *
 * <p>Never replaces existing Advanced lanes or keyframes. Each target receives
 * one independent new track using the already accepted Simple-preset evaluator.
 * Call this exactly once inside a ClientProjectWorkspace.apply(...) transaction
 * for a single Undo step; preview it by evaluating the returned project without
 * committing. All targets/budgets are checked before any edits are composed.
 */
public final class AnimationPresetBatch {
    private AnimationPresetBatch() {}

    public static LoomProject compose(LoomProject source, AnimationChannel channel,
            List<UUID> layerIds, AnimationPreset preset, float rate, boolean loop) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(layerIds, "layerIds");
        Objects.requireNonNull(preset, "preset");
        if (layerIds.isEmpty())
            throw new IllegalArgumentException("Select at least one layer to animate");
        if (!Float.isFinite(rate) || rate <= 0 || rate > 20)
            throw new IllegalArgumentException("Invalid animation rate");
        if (source.animation().tracks().size() + layerIds.size() > LoomAnimation.MAX_TRACKS)
            throw new IllegalStateException("Too many tracks; select fewer layers or delete tracks");
        var canvas = channel==AnimationChannel.CAPE ? source.cape() : source.elytra();
        var seen = new HashSet<UUID>();
        for (UUID id:layerIds) {
            if (id==null || !seen.add(id))
                throw new IllegalArgumentException("Duplicate or missing layer in animation selection");
            LoomLayer target = canvas.layers().stream().filter(l->l.id().equals(id))
                .findFirst().orElseThrow(()->new IllegalArgumentException(
                    "Selected layer does not belong to this Cape/Elytra channel"));
            if (target.locked())
                throw new IllegalStateException("Unlock "+target.name()+" before animating");
        }
        LoomProject draft=source;
        for(UUID id:layerIds) {
            // Null track ID means "add" rather than replace existing edits.
            // Previous custom tracks stay intact, even for the same layer.
            draft=AnimationPresetDraft.compose(draft,id,channel,null,preset,rate,loop);
        }
        return draft;
    }
}
