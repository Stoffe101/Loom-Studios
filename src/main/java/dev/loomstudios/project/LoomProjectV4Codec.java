package dev.loomstudios.project;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Per-layer compression with exact expansion lengths and a cumulative decoded budget. */
final class LoomProjectV4Codec {
    static final int MAX_RAW_BYTES = 64 * 1024 * 1024;
    private static final int MAX_BLOCK_BYTES = 20 * 1024 * 1024;

    private LoomProjectV4Codec() {}

    static byte[] encode(LoomProject p) {
        try {
            var bytes = new ByteArrayOutputStream();
            var out =
                    new DataOutputStream(
                            new LimitedOutput(bytes, LoomProjectCodec.MAX_SERIALIZED_BYTES));
            LoomProjectCodec.writeHeaderAndCommon(out, 4, p);
            long[] budget = {0};
            writeCanvas(out, p.cape(), budget);
            writeCanvas(out, p.elytra(), budget);
            LoomProjectCodec.writeAnimation(out, p.animation());
            for (AnimationTrack t : p.animation().tracks()) {
                writeParameters(out, t.parameters());
                for (AnimationKeyframe k : t.keyframes()) out.writeByte(k.easing().ordinal());
            }
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot encode bounded Loom project", e);
        }
    }

    static LoomProject decode(byte[] bytes) {
        try (var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            LoomProjectCodec.requireHeader(in, 4);
            var c = LoomProjectCodec.readCommonProjectData(in);
            long[] budget = {0};
            var cape = readCanvas(in, budget);
            var elytra = readCanvas(in, budget);
            var animation = LoomProjectCodec.readAnimation(in);
            var tracks = new ArrayList<AnimationTrack>();
            for (var track : animation.tracks()) {
                var params = readParameters(in, track.effect());
                var keys = new ArrayList<AnimationKeyframe>();
                for (var k : track.keyframes()) {
                    int e = in.readUnsignedByte();
                    if (e >= AnimationEasing.values().length)
                        throw new IllegalArgumentException("Unknown easing");
                    keys.add(
                            new AnimationKeyframe(
                                    k.tick(), k.value(), AnimationEasing.values()[e]));
                }
                tracks.add(track.withKeyframes(keys).withParameters(params));
            }
            LoomProjectCodec.rejectTrailingBytes(in);
            return new LoomProject(
                    4,
                    c.projectId(),
                    c.name(),
                    c.metadata(),
                    cape,
                    elytra,
                    c.runtime(),
                    animation.withTracks(tracks));
        } catch (IOException | ArithmeticException e) {
            throw new IllegalArgumentException("Malformed schema-v4 project", e);
        }
    }

    private static void writeCanvas(DataOutputStream out, LoomCanvas canvas, long[] budget)
            throws IOException {
        out.writeInt(canvas.width());
        out.writeInt(canvas.height());
        out.writeInt(canvas.layers().size());
        for (var authored : canvas.layers()) {
            var layer =
                    authored.kind() == LayerKind.PAINT && authored.legacyWingUv()
                            ? authored.withPixels(authored.pixels())
                            : authored;
            var raw = new ByteArrayOutputStream();
            var block = new DataOutputStream(new LimitedOutput(raw, MAX_BLOCK_BYTES));
            LoomProjectCodec.writeCanvasV2(
                    block, new LoomCanvas(canvas.width(), canvas.height(), List.of(layer)));
            block.writeBoolean(layer.legacyWingUv());
            block.writeBoolean(layer.alphaLocked());
            block.writeBoolean(layer.clipToBelow());
            block.writeInt(layer.maskLength());
            block.write(layer.mask());
            if (layer.kind() == LayerKind.IMAGE) {
                var data = layer.imageData();
                var settings = data.processing();
                block.writeInt(settings.tintColor());
                block.writeFloat(settings.tintStrength());
                var bg = settings.background();
                block.writeBoolean(bg.enabled());
                block.writeInt(bg.color());
                block.writeInt(bg.tolerance());
                block.writeBoolean(bg.contiguous());
                block.writeInt(bg.seedX());
                block.writeInt(bg.seedY());
                block.writeInt(data.frames().size());
                for (int i = 0; i < data.frames().size(); i++) {
                    block.writeInt(data.frameTicks().get(i));
                    var frame = data.frames().get(i);
                    block.writeInt(frame.width());
                    block.writeInt(frame.height());
                    for (int color : frame.pixels()) block.writeInt(color);
                }
            }
            block.flush();
            budget[0] += raw.size();
            if (budget[0] > MAX_RAW_BYTES)
                throw new IllegalArgumentException("Project exceeds 64 MiB decoded artwork budget");
            byte[] source = raw.toByteArray();
            var compressed = new ByteArrayOutputStream();
            try (var zip = new DeflaterOutputStream(compressed)) {
                zip.write(source);
            }
            byte[] packed = compressed.toByteArray();
            boolean deflated = packed.length < source.length;
            byte[] stored = deflated ? packed : source;
            out.writeBoolean(deflated);
            out.writeInt(source.length);
            out.writeInt(stored.length);
            out.write(stored);
        }
    }

    private static LoomCanvas readCanvas(DataInputStream in, long[] budget) throws IOException {
        int width = in.readInt(), height = in.readInt(), count = in.readInt();
        CanvasResolution.fromDimensions(width, height);
        if (count < 0 || count > LoomProjectCodec.MAX_LAYER_COUNT)
            throw new IllegalArgumentException("Layer count out of range");
        var layers = new ArrayList<LoomLayer>();
        for (int i = 0; i < count; i++) {
            boolean compressed = in.readBoolean();
            int raw = in.readInt(), stored = in.readInt();
            if (raw <= 0
                    || raw > MAX_BLOCK_BYTES
                    || stored <= 0
                    || stored > LoomProjectCodec.MAX_SERIALIZED_BYTES
                    || (!compressed && raw != stored)
                    || (budget[0] += raw) > MAX_RAW_BYTES)
                throw new IllegalArgumentException("Layer expansion budget exceeded");
            byte[] data = in.readNBytes(stored);
            if (data.length != stored) throw new EOFException();
            if (compressed) {
                var inflater = new Inflater();
                try {
                    inflater.setInput(data);
                    byte[] decoded = new byte[raw];
                    int n = 0;
                    while (n < raw) {
                        int got = inflater.inflate(decoded, n, raw - n);
                        if (got == 0) break;
                        n += got;
                    }
                    if (n != raw || !inflater.finished() || inflater.getRemaining() != 0)
                        throw new IllegalArgumentException("Invalid compressed layer length");
                    data = decoded;
                } catch (DataFormatException e) {
                    throw new IllegalArgumentException("Invalid compressed layer", e);
                } finally {
                    inflater.end();
                }
            }
            try (var block = new DataInputStream(new ByteArrayInputStream(data))) {
                var single = LoomProjectCodec.readCanvasV2(block);
                if (single.width() != width
                        || single.height() != height
                        || single.layers().size() != 1)
                    throw new IllegalArgumentException("Layer canvas mismatch");
                var layer =
                        single.layers()
                                .getFirst()
                                .withLegacyWingUv(block.readBoolean())
                                .withAlphaLocked(block.readBoolean())
                                .withClipToBelow(block.readBoolean());
                int mask = block.readInt();
                if (mask != 0 && mask != width * height)
                    throw new IllegalArgumentException("Mask size mismatch");
                byte[] m = block.readNBytes(mask);
                if (m.length != mask) throw new EOFException();
                layer = layer.withMask(m);
                if (layer.kind() == LayerKind.IMAGE) {
                    int tint = block.readInt();
                    float strength = block.readFloat();
                    var background =
                            new dev.loomstudios.image.BackgroundRemoval(
                                    block.readBoolean(),
                                    block.readInt(),
                                    block.readInt(),
                                    block.readBoolean(),
                                    block.readInt(),
                                    block.readInt());
                    layer =
                            layer.withImageData(
                                    layer.imageData()
                                            .withProcessing(
                                                    layer.imageData()
                                                            .processing()
                                                            .withTint(tint, strength)
                                                            .withBackground(background)));
                    int frames = block.readInt();
                    if (frames < 0 || frames > ImageLayerData.MAX_FRAMES)
                        throw new IllegalArgumentException("GIF frame limit exceeded");
                    var images = new ArrayList<dev.loomstudios.image.PixelImage>();
                    var ticks = new ArrayList<Integer>();
                    long pixels = 0;
                    for (int f = 0; f < frames; f++) {
                        ticks.add(block.readInt());
                        int w = block.readInt(), h = block.readInt();
                        if (w < 1
                                || h < 1
                                || w > 512
                                || h > 512
                                || (pixels += (long) w * h) > ImageLayerData.MAX_FRAME_PIXELS)
                            throw new IllegalArgumentException("GIF decoded budget exceeded");
                        int[] colors = new int[w * h];
                        for (int j = 0; j < colors.length; j++) colors[j] = block.readInt();
                        images.add(new dev.loomstudios.image.PixelImage(w, h, colors));
                    }
                    layer = layer.withImageData(layer.imageData().withFrames(images, ticks));
                }
                LoomProjectCodec.rejectTrailingBytes(block);
                layers.add(layer);
            }
        }
        return new LoomCanvas(width, height, layers);
    }

    private static void writeParameters(DataOutputStream out, EffectParameters p)
            throws IOException {
        switch (p) {
            case EffectParameters.Pulse v -> {
                out.writeFloat(v.minOpacity());
                out.writeFloat(v.maxOpacity());
            }
            case EffectParameters.Scroll v -> {
                out.writeFloat(v.directionX());
                out.writeFloat(v.directionY());
                out.writeFloat(v.distance());
            }
            case EffectParameters.Gradient v -> {
                out.writeFloat(v.angle());
                out.writeFloat(v.width());
                out.writeInt(v.firstColor());
                out.writeInt(v.secondColor());
                out.writeFloat(v.offset());
                out.writeBoolean(v.legacyScroll());
            }
            case EffectParameters.Sparkle v -> {
                out.writeFloat(v.density());
                out.writeInt(v.seed());
                out.writeInt(v.size());
                out.writeFloat(v.brightness());
            }
            case EffectParameters.Glow v -> {
                out.writeFloat(v.intensity());
                out.writeFloat(v.falloff());
            }
            case EffectParameters.Hue v -> out.writeFloat(v.cycles());
        }
    }

    private static EffectParameters readParameters(DataInputStream in, AnimationEffectType effect)
            throws IOException {
        return switch (effect) {
            case PULSE -> new EffectParameters.Pulse(in.readFloat(), in.readFloat());
            case SCROLL ->
                    new EffectParameters.Scroll(in.readFloat(), in.readFloat(), in.readFloat());
            case MOVING_GRADIENT ->
                    new EffectParameters.Gradient(
                            in.readFloat(),
                            in.readFloat(),
                            in.readInt(),
                            in.readInt(),
                            in.readFloat(),
                            in.readBoolean());
            case SPARKLE ->
                    new EffectParameters.Sparkle(
                            in.readFloat(), in.readInt(), in.readInt(), in.readFloat());
            case EMISSIVE_GLOW -> new EffectParameters.Glow(in.readFloat(), in.readFloat());
            case HUE_SHIFT -> new EffectParameters.Hue(in.readFloat());
        };
    }

    private static final class LimitedOutput extends FilterOutputStream {
        private final int max;
        private int size;

        LimitedOutput(OutputStream out, int max) {
            super(out);
            this.max = max;
        }

        private void reserve(int n) {
            if (n < 0 || (long) size + n > max)
                throw new IllegalArgumentException("Project exceeds bounded serialization budget");
            size += n;
        }

        @Override
        public void write(int b) throws IOException {
            reserve(1);
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            reserve(len);
            out.write(b, off, len);
        }
    }
}
