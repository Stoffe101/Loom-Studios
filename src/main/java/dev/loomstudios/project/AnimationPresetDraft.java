package dev.loomstudios.project;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Builds a read-only preview of a simple animation recipe using the exact same
 * transformation as Apply. No editing session, textures, or equipped cosmetics
 * are changed until a caller commits the returned project in one Undo step.
 */
public final class AnimationPresetDraft {
    private AnimationPresetDraft() {}

    public static LoomProject compose(LoomProject source, UUID layerId,
            AnimationChannel channel, UUID selectedTrackId,
            AnimationPreset preset, float cyclesPerSecond) {
        return compose(source,layerId,channel,selectedTrackId,preset,cyclesPerSecond,
                source.animation().loop());
    }

    /** Preview-specific loop preference becomes project state only through explicit Apply. */
    public static LoomProject compose(LoomProject source, UUID layerId,
            AnimationChannel channel, UUID selectedTrackId,
            AnimationPreset preset, float cyclesPerSecond, boolean loop) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(layerId, "layerId");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(preset, "preset");
        var canvas = channel == AnimationChannel.CAPE ? source.cape() : source.elytra();
        if (canvas.layers().stream().noneMatch(l -> l.id().equals(layerId)))
            throw new IllegalArgumentException("Choose a valid layer before previewing an animation");
        var animation = source.animation();
        int period = Math.round(20 / cyclesPerSecond);
        if (period <= 0) throw new IllegalArgumentException("Choose a valid animation rate");
        int duration = Math.min(LoomAnimation.MAX_DURATION_TICKS,
                ((animation.durationTicks() + period - 1) / period) * period);
        var recipe = preset.create(layerId, channel, duration, cyclesPerSecond);
        var current = animation.tracks().stream()
                .filter(t -> t.channel() == channel && t.layerId().equals(layerId)
                        && t.id().equals(selectedTrackId))
                .findFirst().orElse(null);
        // A Simple recipe cannot faithfully represent multi-parameter
        // authoring. Never silently destroy an Advanced track's custom lanes.
        if (current != null && !current.lanes().isEmpty())
            throw new IllegalStateException(
                    "Customized animation: use Advanced to edit this track");
        if (current == null && animation.tracks().size() >= LoomAnimation.MAX_TRACKS)
            throw new IllegalStateException("Delete an existing track before adding another");
        var next = AnimationAuthoring.changeDuration(animation, duration);
        if (current != null) {
            // Preserve the selected track's stable identity; replacement is an
            // explicit Apply operation, not an implicit Simple/Advanced switch.
            var replacement = new AnimationTrack(current.id(), layerId, channel,
                    recipe.effect(), true, recipe.speed(), true,
                    recipe.keyframes(), recipe.parameters());
            next = AnimationAuthoring.replaceTrack(next, replacement);
        } else {
            var tracks = new ArrayList<>(next.tracks());
            tracks.add(recipe);
            next = next.withTracks(tracks);
        }
        return source.withAnimation(next.withLoop(loop));
    }
}
