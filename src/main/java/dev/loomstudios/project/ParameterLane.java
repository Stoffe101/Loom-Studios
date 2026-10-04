package dev.loomstudios.project;

import java.util.*;

public record ParameterLane(AnimationParameter parameter, List<AnimationKeyframe> keys) {
  public ParameterLane {
    Objects.requireNonNull(parameter);
    keys = List.copyOf(keys);
    if (keys.isEmpty() || keys.size() > 128)
      throw new IllegalArgumentException("Lane key budget exceeded");
    int previous = -1;
    for (var k : keys) {
      if (k.tick() <= previous || k.value() < 0 || k.value() > 1)
        throw new IllegalArgumentException("Invalid normalized lane keys");
      previous = k.tick();
    }
  }

  public ParameterLane withKeys(List<AnimationKeyframe> value) {
    return new ParameterLane(parameter, value);
  }
}
