package dev.loomstudios.project;

import java.io.*;
import java.util.*;

final class LoomProjectV5Codec {
  static byte[] encode(LoomProject p) {
    try {
      byte[] base = LoomProjectV4Codec.encode(p);
      var bytes = new ByteArrayOutputStream();
      var out = new DataOutputStream(bytes);
      out.writeInt(0x4C4F4F4D);
      out.writeInt(5);
      out.writeInt(base.length);
      out.write(base);
      for (var t : p.animation().tracks()) {
        for (var k : t.keyframes()) writeCurve(out, k);
        out.writeByte(t.lanes().size());
        for (var l : t.lanes()) {
          out.writeByte(l.parameter().ordinal());
          out.writeInt(l.keys().size());
          for (var k : l.keys()) {
            out.writeInt(k.tick());
            out.writeFloat(k.value());
            out.writeByte(k.easing().ordinal());
            writeCurve(out, k);
          }
        }
      }
      for (var canvas : List.of(p.cape(), p.elytra()))
        for (var layer : canvas.layers())
          if (layer.kind() == LayerKind.IMAGE)
            out.writeBoolean(layer.imageData().editableAnimation());
      out.flush();
      byte[] data = bytes.toByteArray();
      if (data.length > LoomProjectCodec.MAX_SERIALIZED_BYTES)
        throw new IllegalArgumentException("Loom project exceeds serialized size limit");
      return data;
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot encode animation project", e);
    }
  }

  private static void writeCurve(DataOutputStream out, AnimationKeyframe k) throws IOException {
    var c = k.curve();
    out.writeFloat(c.x1());
    out.writeFloat(c.y1());
    out.writeFloat(c.x2());
    out.writeFloat(c.y2());
  }

  private static KeyframeCurve readCurve(DataInputStream in) throws IOException {
    return new KeyframeCurve(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
  }

  static LoomProject decode(byte[] data) {
    try (var in = new DataInputStream(new ByteArrayInputStream(data))) {
      LoomProjectCodec.requireHeader(in, 5);
      int length = in.readInt();
      if (length < 8 || length > data.length - 12)
        throw new IllegalArgumentException("Invalid nested project size");
      var p = LoomProjectV4Codec.decode(in.readNBytes(length));
      var tracks = new ArrayList<AnimationTrack>();
      int total = 0;
      for (var t : p.animation().tracks()) {
        var keys = new ArrayList<AnimationKeyframe>();
        for (var k : t.keyframes())
          keys.add(new AnimationKeyframe(k.tick(), k.value(), k.easing(), readCurve(in)));
        int count = in.readUnsignedByte();
        if (count > 16) throw new IllegalArgumentException("Lane count exceeded");
        var lanes = new ArrayList<ParameterLane>();
        for (int l = 0; l < count; l++) {
          int parameter = in.readUnsignedByte(), n = in.readInt();
          if (parameter >= AnimationParameter.values().length
              || n < 1
              || n > 128
              || (total += n) > 16384)
            throw new IllegalArgumentException("Lane metadata budget exceeded");
          var frames = new ArrayList<AnimationKeyframe>();
          for (int i = 0; i < n; i++) {
            int tick = in.readInt();
            float value = in.readFloat();
            int e = in.readUnsignedByte();
            if (e >= AnimationEasing.values().length)
              throw new IllegalArgumentException("Unknown easing");
            frames.add(
                new AnimationKeyframe(tick, value, AnimationEasing.values()[e], readCurve(in)));
          }
          lanes.add(new ParameterLane(AnimationParameter.values()[parameter], frames));
        }
        tracks.add(t.withKeyframes(keys).withLanes(lanes));
      }
      var cape = readEditable(in, p.cape());
      var elytra = readEditable(in, p.elytra());
      LoomProjectCodec.rejectTrailingBytes(in);
      return new LoomProject(
          5,
          p.projectId(),
          p.name(),
          p.metadata(),
          cape,
          elytra,
          p.runtime(),
          p.animation().withTracks(tracks));
    } catch (IOException e) {
      throw new IllegalArgumentException("Malformed schema5 project", e);
    }
  }

  private static LoomCanvas readEditable(DataInputStream in, LoomCanvas c) throws IOException {
    var layers = new ArrayList<LoomLayer>();
    for (var l : c.layers())
      layers.add(
          l.kind() == LayerKind.IMAGE
              ? l.withImageData(l.imageData().withEditableAnimation(in.readBoolean()))
              : l);
    return new LoomCanvas(c.width(), c.height(), layers);
  }
}
