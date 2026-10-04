package dev.loomstudios.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AnimationAuthoring {
    private AnimationAuthoring() {
    }

    public static LoomAnimation addTrack(
            LoomAnimation animation,
            UUID layerId,
            AnimationChannel channel,
            AnimationEffectType effect
    ) {
        Objects.requireNonNull(animation, "animation");

        if (animation.tracks().size() >= LoomAnimation.MAX_TRACKS) {
            throw new IllegalStateException(
                    "Animation already has the maximum track count"
            );
        }

        AnimationTrack track = new AnimationTrack(
                UUID.randomUUID(),
                Objects.requireNonNull(layerId, "layerId"),
                Objects.requireNonNull(channel, "channel"),
                Objects.requireNonNull(effect, "effect"),
                true,
                1.0F,
                true,
                defaultKeyframes(effect, animation.durationTicks())
        );

        List<AnimationTrack> next =
                new ArrayList<>(animation.tracks());
        next.add(track.withParameters(EffectParameters.forAuthoring(effect)));
        return animation.withTracks(next);
    }

    public static LoomAnimation removeTracksForLayer(
            LoomAnimation animation,
            AnimationChannel channel,
            UUID layerId
    ) {
        Objects.requireNonNull(animation, "animation");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(layerId, "layerId");

        List<AnimationTrack> next =
                new ArrayList<>(animation.tracks());
        next.removeIf(track ->
                track.channel() == channel
                        && track.layerId().equals(layerId)
        );

        return next.size() == animation.tracks().size()
                ? animation
                : animation.withTracks(next);
    }

    public static LoomAnimation removeTrack(
            LoomAnimation animation,
            UUID trackId
    ) {
        List<AnimationTrack> next =
                new ArrayList<>(animation.tracks());
        boolean removed = next.removeIf(track ->
                track.id().equals(trackId)
        );

        if (!removed) {
            throw new IllegalArgumentException(
                    "Unknown animation track " + trackId
            );
        }

        return animation.withTracks(next);
    }

    public static LoomAnimation replaceTrack(
            LoomAnimation animation,
            AnimationTrack replacement
    ) {
        List<AnimationTrack> next =
                new ArrayList<>(animation.tracks());

        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).id().equals(replacement.id())) {
                next.set(i, replacement);
                return animation.withTracks(next);
            }
        }

        throw new IllegalArgumentException(
                "Unknown animation track " + replacement.id()
        );
    }

    public static AnimationTrack addOrReplaceKeyframe(
            AnimationTrack track,
            int tick,
            float value
    ) {
        List<AnimationKeyframe> next =
                new ArrayList<>(track.keyframes());

        boolean replaced = false;
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).tick() == tick) {
                next.set(i, new AnimationKeyframe(tick, value, next.get(i).easing()));
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            if (next.size() >= AnimationTrack.MAX_KEYFRAMES) {
                throw new IllegalStateException(
                        "Track already has the maximum keyframe count"
                );
            }
            next.add(new AnimationKeyframe(tick, value));
            next.sort(Comparator.comparingInt(
                    AnimationKeyframe::tick
            ));
        }

        return track.withKeyframes(next);
    }

    /** Drag collision merges the destination, preserving bounded sorted keyframes. */
    public static AnimationTrack moveKeyframe(AnimationTrack track,int oldTick,int newTick) {
        AnimationKeyframe source=track.keyframes().stream().filter(k->k.tick()==oldTick).findFirst().orElseThrow(()->new IllegalArgumentException("Missing keyframe"));
        if(oldTick==newTick)return track;
        List<AnimationKeyframe> next=new ArrayList<>(track.keyframes());
        next.removeIf(k->k.tick()==oldTick||k.tick()==newTick);
        next.add(new AnimationKeyframe(newTick,source.value(),source.easing()));next.sort(Comparator.comparingInt(AnimationKeyframe::tick));
        return track.withKeyframes(next);
    }

    public static AnimationTrack removeKeyframe(
            AnimationTrack track,
            int tick
    ) {
        if (track.keyframes().size() <= 1) {
            return track;
        }

        List<AnimationKeyframe> next =
                new ArrayList<>(track.keyframes());
        next.removeIf(keyframe -> keyframe.tick() == tick);

        return next.isEmpty()
                ? track
                : track.withKeyframes(next);
    }

    public static LoomAnimation changeDuration(
            LoomAnimation animation,
            int requestedTicks
    ) {
        int duration = Math.max(
                LoomAnimation.MIN_DURATION_TICKS,
                Math.min(
                        LoomAnimation.MAX_DURATION_TICKS,
                        requestedTicks
                )
        );

        List<AnimationTrack> tracks =
                new ArrayList<>(animation.tracks().size());

        for (AnimationTrack track : animation.tracks()) {
            List<AnimationKeyframe> frames =
                    new ArrayList<>(track.keyframes().size());

            for (AnimationKeyframe frame : track.keyframes()) {
                int tick = Math.min(frame.tick(), duration);
                if (!frames.isEmpty()
                        && frames.getLast().tick() == tick) {
                    frames.set(
                            frames.size() - 1,
                            new AnimationKeyframe(tick, frame.value(), frame.easing())
                    );
                } else {
                    frames.add(new AnimationKeyframe(
                            tick,
                            frame.value(), frame.easing()
                    ));
                }
            }

            tracks.add(track.withKeyframes(frames));
        }

        return new LoomAnimation(
                duration,
                animation.loop(),
                animation.playbackSpeed(),
                tracks
        );
    }

    private static List<AnimationKeyframe> defaultKeyframes(
            AnimationEffectType effect,
            int duration
    ) {
        int middle = duration / 2;

        return switch (effect) {
            case PULSE -> List.of(
                    new AnimationKeyframe(0, 0.55F),
                    new AnimationKeyframe(middle, 1.0F),
                    new AnimationKeyframe(duration, 0.55F)
            );
            case HUE_SHIFT, SCROLL, MOVING_GRADIENT -> List.of(
                    new AnimationKeyframe(0, 0.0F),
                    new AnimationKeyframe(duration, 1.0F)
            );
            case SPARKLE -> List.of(
                    new AnimationKeyframe(0, 0.25F),
                    new AnimationKeyframe(middle, 0.9F),
                    new AnimationKeyframe(duration, 0.25F)
            );
            case EMISSIVE_GLOW -> List.of(
                    new AnimationKeyframe(0, 0.4F),
                    new AnimationKeyframe(middle, 1.0F),
                    new AnimationKeyframe(duration, 0.4F)
            );
        };
    }
}
