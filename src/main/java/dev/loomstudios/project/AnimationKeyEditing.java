package dev.loomstudios.project;

import java.util.*;

/** Pure, atomic bounded selection/clipboard operations shared by timeline and tests. */
public final class AnimationKeyEditing {
  public record Address(UUID track, AnimationParameter parameter, int tick) {}

  public record CopiedKey(
      UUID track, AnimationParameter parameter, int offset, AnimationKeyframe key) {}

  public static List<AnimationKeyframe> keys(AnimationTrack t, AnimationParameter p) {
    return p == null
        ? t.keyframes()
        : t.lanes().stream()
            .filter(l -> l.parameter() == p)
            .findFirst()
            .map(ParameterLane::keys)
            .orElse(List.of());
  }

  public static AnimationTrack withKeys(
      AnimationTrack t, AnimationParameter p, List<AnimationKeyframe> keys) {
    if (p == null) return t.withKeyframes(keys);
    var lanes = new ArrayList<>(t.lanes());
    lanes.removeIf(l -> l.parameter() == p);
    if (!keys.isEmpty()) lanes.add(new ParameterLane(p, keys));
    return t.withLanes(lanes);
  }

  public static AnimationTrack addLane(AnimationTrack t, AnimationParameter p, int duration) {
    if (!keys(t, p).isEmpty()) return t;
    float n = p.normalize(p.read(t.parameters()));
    return withKeys(t, p, List.of(new AnimationKeyframe(0, n), new AnimationKeyframe(duration, n)));
  }

  public static List<CopiedKey> copy(LoomAnimation a, Set<Address> selected) {
    if (selected.size() > 512) throw new IllegalArgumentException("Select at most512 keys to copy");
    int start = selected.stream().mapToInt(Address::tick).min().orElse(0);
    var result = new ArrayList<CopiedKey>();
    for (var t : a.tracks())
      for (var address : selected)
        if (t.id().equals(address.track()))
          for (var k : keys(t, address.parameter()))
            if (k.tick() == address.tick())
              result.add(new CopiedKey(t.id(), address.parameter(), k.tick() - start, k));
    return List.copyOf(result);
  }

  public static LoomAnimation paste(LoomAnimation a, List<CopiedKey> clipboard, int tick) {
    if (clipboard.size() > 512) throw new IllegalArgumentException("Clipboard budget exceeded");
    for (var c : clipboard)
      if (tick + c.offset() > a.durationTicks() || tick + c.offset() < 0)
        throw new IllegalArgumentException(
            "Pasted keys exceed timeline; move cursor earlier or extend duration");
    var tracks = new ArrayList<>(a.tracks());
    for (int i = 0; i < tracks.size(); i++) {
      var t = tracks.get(i);
      for (var c : clipboard) {
        if (!c.track().equals(t.id())) continue;
        if (c.parameter() != null
            && !AnimationParameter.forEffect(t.effect()).contains(c.parameter()))
          throw new IllegalArgumentException("Clipboard effect no longer matches");
        var map = new TreeMap<Integer, AnimationKeyframe>();
        for (var k : keys(t, c.parameter())) map.put(k.tick(), k);
        map.put(tick + c.offset(), c.key().at(tick + c.offset()));
        t = withKeys(t, c.parameter(), List.copyOf(map.values()));
      }
      tracks.set(i, t);
    }
    return a.withTracks(tracks);
  }

  public static LoomAnimation edit(
      LoomAnimation a,
      Set<Address> selected,
      java.util.function.UnaryOperator<AnimationKeyframe> edit,
      boolean delete) {
    var tracks = new ArrayList<AnimationTrack>();
    for (var t : a.tracks()) {
      var parameters = new ArrayList<AnimationParameter>();
      parameters.add(null);
      for (var l : t.lanes()) parameters.add(l.parameter());
      for (var p : parameters) {
        var keys = new ArrayList<AnimationKeyframe>();
        for (var k : keys(t, p))
          if (selected.contains(new Address(t.id(), p, k.tick()))) {
            if (!delete) keys.add(edit.apply(k));
          } else keys.add(k);
        if (p == null && keys.isEmpty()) keys.add(t.keyframes().getFirst());
        t = withKeys(t, p, keys);
      }
      tracks.add(t);
    }
    return a.withTracks(tracks);
  }

  public static LoomAnimation move(LoomAnimation a, Set<Address> selected, int delta) {
    for (var address : selected)
      if (address.tick() + delta < 0 || address.tick() + delta > a.durationTicks())
        throw new IllegalArgumentException("Selected keys exceed timeline");
    var tracks = new ArrayList<AnimationTrack>();
    for (var t : a.tracks()) {
      var params = new ArrayList<AnimationParameter>();
      params.add(null);
      for (var lane : t.lanes()) params.add(lane.parameter());
      for (var p : params) {
        var old = keys(t, p);
        var map = new TreeMap<Integer, AnimationKeyframe>();
        for (var k : old)
          if (!selected.contains(new Address(t.id(), p, k.tick()))) map.put(k.tick(), k);
        for (var k : old)
          if (selected.contains(new Address(t.id(), p, k.tick())))
            map.put(k.tick() + delta, k.at(k.tick() + delta));
        t = withKeys(t, p, List.copyOf(map.values()));
      }
      tracks.add(t);
    }
    return a.withTracks(tracks);
  }

  private AnimationKeyEditing() {}
}
