package dev.loomstudios.project;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record LoomAnimation(
        int durationTicks,
        boolean loop,
        float playbackSpeed,
        List<AnimationTrack> tracks
) {
    public static final int MIN_DURATION_TICKS = 20;
    public static final int MAX_DURATION_TICKS = 7200;
    public static final int MAX_TRACKS = 64;
    public static final float MIN_KEYFRAME_VALUE = -4.0F;
    public static final float MAX_KEYFRAME_VALUE = 4.0F;

    public LoomAnimation {
        Objects.requireNonNull(tracks, "tracks");

        if (durationTicks < MIN_DURATION_TICKS
                || durationTicks > MAX_DURATION_TICKS) {
            throw new IllegalArgumentException(
                    "Animation duration out of range"
            );
        }
        if (!Float.isFinite(playbackSpeed)
                || playbackSpeed < 0.25F
                || playbackSpeed > 4.0F) {
            throw new IllegalArgumentException(
                    "Animation playback speed out of range"
            );
        }
        if (tracks.size() > MAX_TRACKS) {
            throw new IllegalArgumentException(
                    "Too many animation tracks"
            );
        }

    int totalLaneKeys =
        tracks.stream().flatMap(t -> t.lanes().stream()).mapToInt(l -> l.keys().size()).sum();
    if (totalLaneKeys > 16384) throw new IllegalArgumentException("Parameter key budget exceeded");

        java.util.HashSet<UUID> ids = new java.util.HashSet<>();
        for (AnimationTrack track : tracks) {
            Objects.requireNonNull(track, "track");
            if (!ids.add(track.id())) {
                throw new IllegalArgumentException(
                        "Duplicate animation track id"
                );
            }
      for (var lane : track.lanes())
        for (var key : lane.keys())
          if (key.tick() > durationTicks)
            throw new IllegalArgumentException("Lane key exceeds duration");
            for (AnimationKeyframe keyframe : track.keyframes()) {
                if (keyframe.tick() > durationTicks) {
                    throw new IllegalArgumentException(
                            "Animation keyframe exceeds timeline duration"
                    );
                }
            }
        }

        tracks = List.copyOf(tracks);
    }

    public static LoomAnimation empty() {
        return new LoomAnimation(80, true, 1.0F, List.of());
    }

    public boolean hasEnabledTracks(AnimationChannel channel) {
        return tracks.stream().anyMatch(track ->
                track.enabled() && track.channel() == channel
        );
    }

    public LoomAnimation withTracks(List<AnimationTrack> next) {
        return new LoomAnimation(
                durationTicks, loop, playbackSpeed, next
        );
    }

    public LoomAnimation withDurationTicks(int next) {
        return new LoomAnimation(
                next, loop, playbackSpeed, tracks
        );
    }

    public LoomAnimation withLoop(boolean next) {
        return new LoomAnimation(
                durationTicks, next, playbackSpeed, tracks
        );
    }

    public LoomAnimation withPlaybackSpeed(float next) {
        return new LoomAnimation(
                durationTicks, loop, next, tracks
        );
    }
}
