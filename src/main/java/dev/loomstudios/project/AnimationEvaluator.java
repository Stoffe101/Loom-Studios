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

    return valueAt( track.keyframes(), tick);
  }

  public static float valueAt(java.util.List<AnimationKeyframe> keyframes, int tick) {

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

            float progress = left.progress((tick - left.tick()) / (float)span);
            return left.value()
                    + (right.value() - left.value()) * progress;
        }

        return keyframes.getLast().value();
    }

  public static EffectParameters parametersAt(
      AnimationTrack track, LoomAnimation animation, int timelineTick) {
    int tick =
        track.loop()
            ? (int)
                Math.floorMod(
                    (long) Math.floor(Math.max(0, timelineTick) * track.speed()),
                    animation.durationTicks())
            : Math.min(
                animation.durationTicks(), (int) (Math.max(0, timelineTick) * track.speed()));
    EffectParameters p = track.parameters();
    // Evaluate paired opacity boundaries simultaneously to avoid order-dependent clipping.
    if (p instanceof EffectParameters.Pulse v) {
      float lo = v.minOpacity(), hi = v.maxOpacity();
      for (var lane : track.lanes()) {
        float n = lane.parameter().decode(valueAt(lane.keys(), tick));
        if (lane.parameter() == AnimationParameter.MIN_OPACITY) lo = n;
        else hi = n;
}
      return new EffectParameters.Pulse(Math.min(lo, hi), Math.max(lo, hi));
    }
    for (var lane : track.lanes())
      p = lane.parameter().set(p, lane.parameter().decode(valueAt(lane.keys(), tick)));
    return p;
  }
}
