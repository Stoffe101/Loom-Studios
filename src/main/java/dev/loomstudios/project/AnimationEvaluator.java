package dev.loomstudios.project;

public final class AnimationEvaluator {
    private AnimationEvaluator() {
    }

    public static int timelineTick(
            LoomAnimation animation,
            long gameTime
    ) {
        long scaled = (long)Math.floor(
                Math.max(0L, gameTime) * animation.playbackSpeed()
        );

        if (animation.loop()) {
            return (int)Math.floorMod(
                    scaled,
                    (long)animation.durationTicks()
            );
        }

        return (int)Math.min(
                animation.durationTicks(),
                scaled
        );
    }

    public static float valueAt(
            AnimationTrack track,
            LoomAnimation animation,
            int timelineTick
    ) {
        double scaled = Math.max(0, timelineTick) * track.speed();
        int tick;

        if (track.loop()) {
            tick = (int)Math.floorMod(
                    (long)Math.floor(scaled),
                    (long)animation.durationTicks()
            );
        } else {
            tick = (int)Math.min(
                    animation.durationTicks(),
                    Math.floor(scaled)
            );
        }

        var keyframes = track.keyframes();

        if (tick <= keyframes.getFirst().tick()) {
            return keyframes.getFirst().value();
        }
        if (tick >= keyframes.getLast().tick()) {
            return keyframes.getLast().value();
        }

        for (int i = 1; i < keyframes.size(); i++) {
            AnimationKeyframe right = keyframes.get(i);
            if (tick > right.tick()) {
                continue;
            }

            AnimationKeyframe left = keyframes.get(i - 1);
            int span = right.tick() - left.tick();
            if (span <= 0) {
                return right.value();
            }

            float progress = left.easing().apply((tick - left.tick()) / (float)span);
            return left.value()
                    + (right.value() - left.value()) * progress;
        }

        return keyframes.getLast().value();
    }
}
