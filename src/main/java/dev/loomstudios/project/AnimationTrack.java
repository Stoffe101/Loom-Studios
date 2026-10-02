package dev.loomstudios.project;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record AnimationTrack(
        UUID id,
        UUID layerId,
        AnimationChannel channel,
        AnimationEffectType effect,
        boolean enabled,
        float speed,
        boolean loop,
        List<AnimationKeyframe> keyframes
) {
    public static final int MAX_KEYFRAMES = 128;

    public AnimationTrack {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(layerId, "layerId");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(keyframes, "keyframes");

        if (!Float.isFinite(speed) || speed < 0.1F || speed > 8.0F) {
            throw new IllegalArgumentException(
                    "Animation track speed out of range"
            );
        }
        if (keyframes.isEmpty() || keyframes.size() > MAX_KEYFRAMES) {
            throw new IllegalArgumentException(
                    "Animation keyframe count out of range"
            );
        }

        int previousTick = -1;
        for (AnimationKeyframe keyframe : keyframes) {
            Objects.requireNonNull(keyframe, "keyframe");
            if (keyframe.tick() <= previousTick) {
                throw new IllegalArgumentException(
                        "Animation keyframes must be strictly ordered"
                );
            }
            previousTick = keyframe.tick();
        }

        keyframes = List.copyOf(keyframes);
    }

    public AnimationTrack withEffect(AnimationEffectType next) {
        return new AnimationTrack(
                id, layerId, channel,
                Objects.requireNonNull(next, "next"),
                enabled, speed, loop, keyframes
        );
    }

    public AnimationTrack withEnabled(boolean next) {
        return new AnimationTrack(
                id, layerId, channel, effect,
                next, speed, loop, keyframes
        );
    }

    public AnimationTrack withSpeed(float next) {
        return new AnimationTrack(
                id, layerId, channel, effect,
                enabled, next, loop, keyframes
        );
    }

    public AnimationTrack withLoop(boolean next) {
        return new AnimationTrack(
                id, layerId, channel, effect,
                enabled, speed, next, keyframes
        );
    }

    public AnimationTrack withKeyframes(List<AnimationKeyframe> next) {
        return new AnimationTrack(
                id, layerId, channel, effect,
                enabled, speed, loop, next
        );
    }
}
