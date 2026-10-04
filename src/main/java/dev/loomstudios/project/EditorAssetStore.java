package dev.loomstudios.project;

import dev.loomstudios.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Bounded local sidecars with atomic replacement. No project/equip codec calls this store. */
public final class EditorAssetStore {
  private final Path root;
  public static final int MAX_BYTES = 8 * 1024 * 1024;

  public EditorAssetStore(Path root) {
    this.root = root;
  }

  private static DataInputStream open(Path path) throws IOException {
    if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
      throw new IOException("Asset file is not regular");
    if (Files.size(path) > MAX_BYTES) throw new IOException("Asset file exceeds8 MiB");
    byte[] b;
    try (var input = Files.newInputStream(path)) {
      b = input.readNBytes(MAX_BYTES + 1);
    }
    if (b.length > MAX_BYTES) throw new IOException("Asset file exceeds8 MiB");
    return new DataInputStream(new ByteArrayInputStream(b));
  }

  private void write(Path path, java.util.function.Consumer<DataOutputStream> action)
      throws IOException {
    var bytes = new ByteArrayOutputStream();
    try (var out = new DataOutputStream(bytes)) {
      action.accept(out);
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
    if (bytes.size() > MAX_BYTES) throw new IOException("Assets exceed8 MiB");
    Files.createDirectories(root);
    Path tmp = Files.createTempFile(root, "asset-", ".tmp");
    try {
      Files.write(tmp, bytes.toByteArray());
      try {
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  private static PixelImage image(DataInputStream in, int max) throws IOException {
    int w = in.readInt(), h = in.readInt();
    if (w < 1 || h < 1 || w > max || h > max) throw new IOException("Invalid asset dimensions");
    int[] p = new int[w * h];
    for (int i = 0; i < p.length; i++) p[i] = in.readInt();
    return new PixelImage(w, h, p);
  }

  private static void image(DataOutputStream out, PixelImage p) throws IOException {
    out.writeInt(p.width());
    out.writeInt(p.height());
    for (int c : p.pixels()) out.writeInt(c);
  }

  private static void id(DataOutputStream out, UUID id) throws IOException {
    out.writeLong(id.getMostSignificantBits());
    out.writeLong(id.getLeastSignificantBits());
  }

  private static UUID id(DataInputStream in) throws IOException {
    return new UUID(in.readLong(), in.readLong());
  }

  public List<CustomStamp> stamps() throws IOException {
    Path file = root.resolve("stamps.bin");
    if (!Files.exists(file)) return BuiltinStampPacks.all();
    try (var in = open(file)) {
      if (in.readInt() != 0x4C535431) throw new IOException("Unknown stamp format");
      int n = in.readInt();
      if (n < 0 || n > 64) throw new IOException("Stamp count exceeded");
      var stamps = new ArrayList<CustomStamp>();
      var ids = new HashSet<UUID>();
      for (int i = 0; i < n; i++) {
        UUID id = id(in);
        if (!ids.add(id)) throw new IOException("Duplicate stamp");
        String name = in.readUTF();
        boolean favorite = in.readBoolean();
        var p = image(in, 128);
        stamps.add(
            new CustomStamp(id, name, new PixelPatch(p.width(), p.height(), p.pixels()), favorite));
      }
      if (in.available() != 0) throw new IOException("Trailing stamp data");
      return List.copyOf(stamps);
    } catch (IllegalArgumentException e) {
      throw new IOException("Malformed stamp asset", e);
    }
  }

  public void stamps(List<CustomStamp> stamps) throws IOException {
    if (stamps.size() > 64
        || stamps.stream().map(CustomStamp::id).distinct().count() != stamps.size())
      throw new IOException("At most64 unique stamps");
    write(
        root.resolve("stamps.bin"),
        out -> {
          try {
            out.writeInt(0x4C535431);
            out.writeInt(stamps.size());
            for (var s : stamps) {
              id(out, s.id());
              out.writeUTF(s.name());
              out.writeBoolean(s.favorite());
              image(out, new PixelImage(s.patch().width(), s.patch().height(), s.patch().data()));
            }
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  public List<ReferenceImage> references(UUID project) throws IOException {
    Path file = root.resolve(project + "-references.bin");
    if (!Files.exists(file)) return List.of();
    try (var in = open(file)) {
      if (in.readInt() != 0x4C524631) throw new IOException("Unknown reference format");
      int n = in.readInt();
      if (n < 0 || n > 8) throw new IOException("At most8 reference layers");
      var refs = new ArrayList<ReferenceImage>();
      var ids = new HashSet<UUID>();
      for (int i = 0; i < n; i++) {
        UUID id = id(in);
        if (!ids.add(id)) throw new IOException("Duplicate reference");
        String name = in.readUTF();
        int channel = in.readUnsignedByte();
        if (channel >= AnimationChannel.values().length)
          throw new IOException("Invalid reference channel");
        float opacity = in.readFloat();
        boolean visible = in.readBoolean(), locked = in.readBoolean(), above = in.readBoolean();
        var t =
            new LayerTransform(
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readBoolean(),
                in.readBoolean());
        var clip =
            new NormalizedRect(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
        var image =
            new ImageLayerData(
                image(in, 512),
                NormalizedRect.fullCanvas(),
                t,
                clip,
                ImageProcessingSettings.defaults());
        refs.add(
            new ReferenceImage(
                id,
                name,
                AnimationChannel.values()[channel],
                image,
                opacity,
                visible,
                locked,
                above));
      }
      if (in.available() != 0) throw new IOException("Trailing reference data");
      return List.copyOf(refs);
    } catch (IllegalArgumentException e) {
      throw new IOException("Malformed reference asset", e);
    }
  }

  public void references(UUID project, List<ReferenceImage> refs) throws IOException {
    if (refs.size() > 8 || refs.stream().map(ReferenceImage::id).distinct().count() != refs.size())
      throw new IOException("At most8 unique references");
    write(
        root.resolve(project + "-references.bin"),
        out -> {
          try {
            out.writeInt(0x4C524631);
            out.writeInt(refs.size());
            for (var r : refs) {
              id(out, r.id());
              out.writeUTF(r.name());
              out.writeByte(r.channel().ordinal());
              out.writeFloat(r.opacity());
              out.writeBoolean(r.visible());
              out.writeBoolean(r.locked());
              out.writeBoolean(r.above());
              var t = r.image().transform();
              out.writeDouble(t.centerX());
              out.writeDouble(t.centerY());
              out.writeDouble(t.width());
              out.writeDouble(t.height());
              out.writeDouble(t.rotationDegrees());
              out.writeBoolean(t.mirrorHorizontal());
              out.writeBoolean(t.mirrorVertical());
              var clip = r.image().clip();
              out.writeDouble(clip.x());
              out.writeDouble(clip.y());
              out.writeDouble(clip.width());
              out.writeDouble(clip.height());
              image(out, r.image().source());
            }
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }
}
