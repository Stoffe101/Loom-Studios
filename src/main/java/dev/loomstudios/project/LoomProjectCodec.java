package dev.loomstudios.project;

import dev.loomstudios.image.ImageProcessingMode;
import dev.loomstudios.image.ImageProcessingSettings;
import dev.loomstudios.image.PixelImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Deterministic bounded binary codec for versioned .loom projects. */
public final class LoomProjectCodec {
    private static final int MAGIC = 0x4C4F4F4D; // LOOM

    public static final int MAX_SERIALIZED_BYTES = 8 * 1024 * 1024;
    public static final int MAX_NETWORK_PAYLOAD_BYTES =
            MAX_SERIALIZED_BYTES + 512;
    public static final int MAX_CANVAS_DIMENSION = 512;
    public static final int MAX_EMBEDDED_IMAGE_DIMENSION = 512;
    public static final int MAX_LAYER_COUNT = 64;
    public static final int MAX_PROJECT_NAME_CHARS = 96;
    public static final int MAX_LAYER_NAME_CHARS = 96;
    public static final int MAX_IMAGE_PALETTE_COLORS = 256;
    public static final int MAX_GRADIENT_STOPS = 16;
    public static final int MAX_ANIMATION_TRACKS = LoomAnimation.MAX_TRACKS;
    public static final int MAX_ANIMATION_KEYFRAMES =
            AnimationTrack.MAX_KEYFRAMES;
    private static final int MAX_STRING_BYTES = 512;

    private LoomProjectCodec() {
    }

    public static byte[] encode(LoomProject project) {
        return switch (project.schemaVersion()) {
            case 1 -> encodeVersion1(project);
            case 2 -> encodeVersion2(project);
            case 3 -> encodeVersion3(project);
            case 4 -> {
        if (java.util.stream.Stream.concat(
                    project.cape().layers().stream(), project.elytra().layers().stream())
                .anyMatch(l -> l.kind() == LayerKind.IMAGE && l.imageData().editableAnimation())
            || project.animation().tracks().stream()
                .anyMatch(
                    t ->
                        !t.lanes().isEmpty()
                            || t.keyframes().stream()
                                .anyMatch(k -> k.easing() == AnimationEasing.CUSTOM)))
          yield LoomProjectV5Codec.encode(project);
        yield LoomProjectV4Codec.encode(project);
      }
      case 5 -> LoomProjectV5Codec.encode(project);
            default -> throw new IllegalArgumentException(
                    "Unsupported Loom project schema "
                            + project.schemaVersion()
            );
        };
    }

    static byte[] encodeVersion3SnapshotForTest(LoomProject project) { return encodeVersion3(project); }

    public static LoomProject decode(byte[] data) {
        return LoomProjectMigrations.decodeAndMigrate(data);
    }

    static int peekSchemaVersion(byte[] data) {
        validateEnvelopeSize(data);

        try (DataInputStream in =
                     new DataInputStream(new ByteArrayInputStream(data))) {
            if (in.readInt() != MAGIC) {
                throw new IllegalArgumentException(
                        "Invalid Loom project magic"
                );
            }
            return in.readInt();
        } catch (EOFException e) {
            throw new IllegalArgumentException(
                    "Truncated Loom project",
                    e
            );
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Malformed Loom project",
                    e
            );
        }
    }

    static LoomProject decodeVersion1(byte[] data) {
        validateEnvelopeSize(data);

        try (DataInputStream in =
                     new DataInputStream(new ByteArrayInputStream(data))) {
            requireHeader(in, 1);
            CommonProjectData common = readCommonProjectData(in);
            LoomCanvas cape = readCanvasV1(in);
            LoomCanvas elytra = readCanvasV1(in);
            rejectTrailingBytes(in);

            return new LoomProject(
                    1,
                    common.projectId(),
                    common.name(),
                    common.metadata(),
                    cape,
                    elytra,
                    common.runtime()
            );
        } catch (EOFException e) {
            throw new IllegalArgumentException(
                    "Truncated Loom project",
                    e
            );
        } catch (IOException | ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Malformed Loom project",
                    e
            );
        }
    }

    static LoomProject decodeVersion2(byte[] data) {
        validateEnvelopeSize(data);

        try (DataInputStream in =
                     new DataInputStream(new ByteArrayInputStream(data))) {
            requireHeader(in, 2);
            CommonProjectData common = readCommonProjectData(in);
            LoomCanvas cape = readCanvasV2(in);
            LoomCanvas elytra = readCanvasV2(in);
            rejectTrailingBytes(in);

            return new LoomProject(
                    2,
                    common.projectId(),
                    common.name(),
                    common.metadata(),
                    cape,
                    elytra,
                    common.runtime()
            );
        } catch (EOFException e) {
            throw new IllegalArgumentException(
                    "Truncated Loom project",
                    e
            );
        } catch (IOException | ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Malformed Loom project",
                    e
            );
        }
    }

    static LoomProject decodeVersion3(byte[] data) {
        validateEnvelopeSize(data);

        try (DataInputStream in =
                     new DataInputStream(new ByteArrayInputStream(data))) {
            requireHeader(in, 3);
            CommonProjectData common = readCommonProjectData(in);
            LoomCanvas cape = readCanvasV2(in);
            LoomCanvas elytra = readCanvasV2(in);
            LoomAnimation animation = readAnimation(in);
            rejectTrailingBytes(in);

            return new LoomProject(
                    3,
                    common.projectId(),
                    common.name(),
                    common.metadata(),
                    cape,
                    elytra,
                    common.runtime(),
                    animation
            );
        } catch (EOFException e) {
            throw new IllegalArgumentException(
                    "Truncated Loom project",
                    e
            );
        } catch (IOException | ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Malformed Loom project",
                    e
            );
        }
    }

    static byte[] encodeVersion1SnapshotForTest(LoomProject project) {
        return encodeVersion1(project);
    }

    static byte[] encodeVersion2SnapshotForTest(LoomProject project) {
        return encodeVersion2(project);
    }

    private static byte[] encodeVersion1(LoomProject project) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try (DataOutputStream out = new DataOutputStream(bytes)) {
                writeHeaderAndCommon(out, 1, project);
                writeCanvasV1(out, project.cape());
                writeCanvasV1(out, project.elytra());
            }

            return validateEncodedSize(bytes.toByteArray());
        } catch (IOException impossible) {
            throw new IllegalStateException(
                    "Unexpected in-memory serialization failure",
                    impossible
            );
        }
    }

    private static byte[] encodeVersion2(LoomProject project) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try (DataOutputStream out = new DataOutputStream(bytes)) {
                writeHeaderAndCommon(out, 2, project);
                writeCanvasV2(out, project.cape());
                writeCanvasV2(out, project.elytra());
            }

            return validateEncodedSize(bytes.toByteArray());
        } catch (IOException impossible) {
            throw new IllegalStateException(
                    "Unexpected in-memory serialization failure",
                    impossible
            );
        }
    }

    private static byte[] encodeVersion3(LoomProject project) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try (DataOutputStream out = new DataOutputStream(bytes)) {
                writeHeaderAndCommon(out, 3, project);
                writeCanvasV2(out, project.cape());
                writeCanvasV2(out, project.elytra());
                writeAnimation(out, project.animation());
            }

            return validateEncodedSize(bytes.toByteArray());
        } catch (IOException impossible) {
            throw new IllegalStateException(
                    "Unexpected in-memory serialization failure",
                    impossible
            );
        }
    }

    private static byte[] validateEncodedSize(byte[] result) {
        if (result.length > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException(
                    "Loom project exceeds serialized size limit"
            );
        }
        return result;
    }

    static void writeHeaderAndCommon(
            DataOutputStream out,
            int schemaVersion,
            LoomProject project
    ) throws IOException {
        out.writeInt(MAGIC);
        out.writeInt(schemaVersion);
        writeUuid(out, project.projectId());
        writeString(out, project.name());
        out.writeLong(project.metadata().createdAtEpochMillis());
        out.writeLong(project.metadata().modifiedAtEpochMillis());

        LoomRuntimeSettings runtime = project.runtime();
        out.writeFloat(runtime.elytraThickness());
        out.writeInt(runtime.animationPeriodTicks());
        out.writeBoolean(runtime.hueCycleEnabled());
        out.writeBoolean(runtime.emissiveEnabled());
    }

    static CommonProjectData readCommonProjectData(
            DataInputStream in
    ) throws IOException {
        UUID projectId = readUuid(in);
        String name = readString(in, MAX_PROJECT_NAME_CHARS);
        LoomProjectMetadata metadata = new LoomProjectMetadata(
                in.readLong(),
                in.readLong()
        );
        LoomRuntimeSettings runtime = new LoomRuntimeSettings(
                in.readFloat(),
                in.readInt(),
                in.readBoolean(),
                in.readBoolean()
        );

        return new CommonProjectData(
                projectId,
                name,
                metadata,
                runtime
        );
    }

    static void requireHeader(
            DataInputStream in,
            int expectedSchema
    ) throws IOException {
        if (in.readInt() != MAGIC) {
            throw new IllegalArgumentException(
                    "Invalid Loom project magic"
            );
        }

        int actual = in.readInt();
        if (actual != expectedSchema) {
            throw new IllegalArgumentException(
                    "Expected Loom project schema "
                            + expectedSchema
                            + " but found "
                            + actual
            );
        }
    }

    private static void writeCanvasV1(
            DataOutputStream out,
            LoomCanvas canvas
    ) throws IOException {
        out.writeInt(canvas.width());
        out.writeInt(canvas.height());
        out.writeInt(canvas.layers().size());

        for (LoomLayer layer : canvas.layers()) {
            if (layer.kind() != LayerKind.PAINT) {
                throw new IllegalArgumentException(
                        "Schema v1 supports paint layers only"
                );
            }

            writeUuid(out, layer.id());
            writeString(out, layer.name());
            out.writeBoolean(layer.visible());
            out.writeFloat(layer.opacity());
            out.writeByte(layer.blendMode().ordinal());
            out.writeBoolean(layer.emissive());
            out.writeInt(layer.pixelCount());

            for (int i = 0; i < layer.pixelCount(); i++) {
                out.writeInt(layer.pixelAt(i));
            }
        }
    }

    private static LoomCanvas readCanvasV1(DataInputStream in)
            throws IOException {
        CanvasHeader header = readCanvasHeader(in);
        List<LoomLayer> layers =
                new ArrayList<>(header.layerCount());

        for (int layerIndex = 0;
             layerIndex < header.layerCount();
             layerIndex++) {
            UUID id = readUuid(in);
            String name = readString(in, MAX_LAYER_NAME_CHARS);
            boolean visible = in.readBoolean();
            float opacity = in.readFloat();
            int blendOrdinal = in.readUnsignedByte();
            boolean emissive = in.readBoolean();
            int pixelCount = in.readInt();

            if (blendOrdinal >= BlendMode.values().length) {
                throw new IllegalArgumentException(
                        "Unknown blend mode"
                );
            }

            if (pixelCount != header.expectedPixels()) {
                throw new IllegalArgumentException(
                        "Layer pixel count mismatch"
                );
            }

            layers.add(LoomLayer.paint(
                    id,
                    name,
                    visible,
                    opacity,
                    BlendMode.values()[blendOrdinal],
                    emissive,
                    false,
                    readPixels(in, pixelCount)
            ));
        }

        return new LoomCanvas(
                header.width(),
                header.height(),
                layers
        );
    }

    static void writeCanvasV2(
            DataOutputStream out,
            LoomCanvas canvas
    ) throws IOException {
        out.writeInt(canvas.width());
        out.writeInt(canvas.height());
        out.writeInt(canvas.layers().size());

        for (LoomLayer layer : canvas.layers()) {
            writeUuid(out, layer.id());
            writeString(out, layer.name());
            out.writeBoolean(layer.visible());
            out.writeFloat(layer.opacity());
            writeString(out, layer.blendMode().id());
            out.writeBoolean(layer.emissive());
            out.writeBoolean(layer.locked());
            writeString(out, layer.kind().id());

            switch (layer.kind()) {
                case PAINT -> writePaintPayload(out, layer);
                case IMAGE -> writeImagePayload(
                        out,
                        layer.imageData()
                );
                case GRADIENT -> writeGradientPayload(
                        out,
                        layer.gradientData()
                );
            }
        }
    }

    static LoomCanvas readCanvasV2(DataInputStream in)
            throws IOException {
        CanvasHeader header = readCanvasHeader(in);
        List<LoomLayer> layers =
                new ArrayList<>(header.layerCount());

        for (int layerIndex = 0;
             layerIndex < header.layerCount();
             layerIndex++) {
            UUID id = readUuid(in);
            String name = readString(in, MAX_LAYER_NAME_CHARS);
            boolean visible = in.readBoolean();
            float opacity = in.readFloat();
            BlendMode blendMode = BlendMode.fromId(
                    readString(in, 32)
            );
            boolean emissive = in.readBoolean();
            boolean locked = in.readBoolean();
            LayerKind kind = LayerKind.fromId(
                    readString(in, 32)
            );

            LoomLayer layer = switch (kind) {
                case PAINT -> LoomLayer.paint(
                        id,
                        name,
                        visible,
                        opacity,
                        blendMode,
                        emissive,
                        locked,
                        readPaintPayload(
                                in,
                                header.expectedPixels()
                        )
                );
                case IMAGE -> LoomLayer.image(
                        id,
                        name,
                        visible,
                        opacity,
                        blendMode,
                        emissive,
                        locked,
                        readImagePayload(in)
                );
                case GRADIENT -> LoomLayer.gradient(
                        id,
                        name,
                        visible,
                        opacity,
                        blendMode,
                        emissive,
                        locked,
                        readGradientPayload(in)
                );
            };

            layers.add(layer);
        }

        return new LoomCanvas(
                header.width(),
                header.height(),
                layers
        );
    }

    private static void writePaintPayload(
            DataOutputStream out,
            LoomLayer layer
    ) throws IOException {
        out.writeInt(layer.pixelCount());
        for (int i = 0; i < layer.pixelCount(); i++) {
            out.writeInt(layer.pixelAt(i));
        }
    }

    private static int[] readPaintPayload(
            DataInputStream in,
            int expectedPixels
    ) throws IOException {
        int count = in.readInt();
        if (count != expectedPixels) {
            throw new IllegalArgumentException(
                    "Paint layer pixel count mismatch"
            );
        }
        return readPixels(in, count);
    }

    private static void writeImagePayload(
            DataOutputStream out,
            ImageLayerData data
    ) throws IOException {
        PixelImage source = data.source();
        validateEmbeddedImage(source.width(), source.height());

        out.writeInt(source.width());
        out.writeInt(source.height());
        out.writeInt(source.width() * source.height());
        for (int argb : source.pixels()) {
            out.writeInt(argb);
        }

        writeRect(out, data.sourceCrop());
        writeTransform(out, data.transform());
        writeRect(out, data.clip());
        writeProcessing(out, data.processing());
    }

    private static ImageLayerData readImagePayload(
            DataInputStream in
    ) throws IOException {
        int width = in.readInt();
        int height = in.readInt();
        validateEmbeddedImage(width, height);

        int expected = Math.multiplyExact(width, height);
        int pixelCount = in.readInt();
        if (pixelCount != expected) {
            throw new IllegalArgumentException(
                    "Image source pixel count mismatch"
            );
        }

        PixelImage source = new PixelImage(
                width,
                height,
                readPixels(in, pixelCount)
        );

        return new ImageLayerData(
                source,
                readRect(in),
                readTransform(in),
                readRect(in),
                readProcessing(in)
        );
    }

    private static void writeProcessing(
            DataOutputStream out,
            ImageProcessingSettings settings
    ) throws IOException {
        writeString(out, settings.mode().id());
        out.writeFloat(settings.brightness());
        out.writeFloat(settings.contrast());
        out.writeFloat(settings.saturation());
        out.writeInt(settings.colorLimit());
        out.writeBoolean(settings.dither());
        out.writeInt(settings.posterizeLevels());
        out.writeInt(settings.palette().size());

        for (int argb : settings.palette()) {
            out.writeInt(argb);
        }
    }

    private static ImageProcessingSettings readProcessing(
            DataInputStream in
    ) throws IOException {
        ImageProcessingMode mode = ImageProcessingMode.fromId(
                readString(in, 48)
        );
        float brightness = in.readFloat();
        float contrast = in.readFloat();
        float saturation = in.readFloat();
        int colorLimit = in.readInt();
        boolean dither = in.readBoolean();
        int posterizeLevels = in.readInt();
        int paletteCount = in.readInt();

        if (paletteCount < 0
                || paletteCount > MAX_IMAGE_PALETTE_COLORS) {
            throw new IllegalArgumentException(
                    "Image palette size out of range"
            );
        }

        List<Integer> palette = new ArrayList<>(paletteCount);
        for (int i = 0; i < paletteCount; i++) {
            palette.add(in.readInt());
        }

        return new ImageProcessingSettings(
                mode,
                brightness,
                contrast,
                saturation,
                colorLimit,
                dither,
                posterizeLevels,
                palette
        );
    }

    private static void writeGradientPayload(
            DataOutputStream out,
            GradientLayerData data
    ) throws IOException {
        writeString(out, data.type().id());
        writeTransform(out, data.transform());
        writeRect(out, data.clip());
        out.writeBoolean(data.repeat());
        out.writeBoolean(data.dither());
        out.writeInt(data.stops().size());

        for (GradientStop stop : data.stops()) {
            out.writeDouble(stop.position());
            out.writeInt(stop.argb());
        }
    }

    private static GradientLayerData readGradientPayload(
            DataInputStream in
    ) throws IOException {
        GradientType type = GradientType.fromId(
                readString(in, 32)
        );
        LayerTransform transform = readTransform(in);
        NormalizedRect clip = readRect(in);
        boolean repeat = in.readBoolean();
        boolean dither = in.readBoolean();
        int stopCount = in.readInt();

        if (stopCount < 2 || stopCount > MAX_GRADIENT_STOPS) {
            throw new IllegalArgumentException(
                    "Gradient stop count out of range"
            );
        }

        List<GradientStop> stops =
                new ArrayList<>(stopCount);

        for (int i = 0; i < stopCount; i++) {
            stops.add(new GradientStop(
                    in.readDouble(),
                    in.readInt()
            ));
        }

        return new GradientLayerData(
                type,
                stops,
                transform,
                clip,
                repeat,
                dither
        );
    }

    static void writeAnimation(
            DataOutputStream out,
            LoomAnimation animation
    ) throws IOException {
        out.writeInt(animation.durationTicks());
        out.writeBoolean(animation.loop());
        out.writeFloat(animation.playbackSpeed());
        out.writeInt(animation.tracks().size());

        for (AnimationTrack track : animation.tracks()) {
            writeUuid(out, track.id());
            writeUuid(out, track.layerId());
            writeString(out, track.channel().id());
            writeString(out, track.effect().id());
            out.writeBoolean(track.enabled());
            out.writeFloat(track.speed());
            out.writeBoolean(track.loop());
            out.writeInt(track.keyframes().size());

            for (AnimationKeyframe keyframe : track.keyframes()) {
                out.writeInt(keyframe.tick());
                out.writeFloat(keyframe.value());
            }
        }
    }

    static LoomAnimation readAnimation(
            DataInputStream in
    ) throws IOException {
        int duration = in.readInt();
        boolean loop = in.readBoolean();
        float playbackSpeed = in.readFloat();
        int trackCount = in.readInt();

        if (trackCount < 0 || trackCount > MAX_ANIMATION_TRACKS) {
            throw new IllegalArgumentException(
                    "Animation track count out of range"
            );
        }

        List<AnimationTrack> tracks =
                new ArrayList<>(trackCount);

        for (int trackIndex = 0;
             trackIndex < trackCount;
             trackIndex++) {
            UUID id = readUuid(in);
            UUID layerId = readUuid(in);
            AnimationChannel channel = AnimationChannel.fromId(
                    readString(in, 32)
            );
            AnimationEffectType effect =
                    AnimationEffectType.fromId(
                            readString(in, 48)
                    );
            boolean enabled = in.readBoolean();
            float speed = in.readFloat();
            boolean trackLoop = in.readBoolean();
            int keyframeCount = in.readInt();

            if (keyframeCount < 1
                    || keyframeCount > MAX_ANIMATION_KEYFRAMES) {
                throw new IllegalArgumentException(
                        "Animation keyframe count out of range"
                );
            }

            List<AnimationKeyframe> keyframes =
                    new ArrayList<>(keyframeCount);
            for (int i = 0; i < keyframeCount; i++) {
                keyframes.add(new AnimationKeyframe(
                        in.readInt(),
                        in.readFloat()
                ));
            }

            tracks.add(new AnimationTrack(
                    id,
                    layerId,
                    channel,
                    effect,
                    enabled,
                    speed,
                    trackLoop,
                    keyframes
            ));
        }

        return new LoomAnimation(
                duration,
                loop,
                playbackSpeed,
                tracks
        );
    }

    private static void writeTransform(
            DataOutputStream out,
            LayerTransform transform
    ) throws IOException {
        out.writeDouble(transform.centerX());
        out.writeDouble(transform.centerY());
        out.writeDouble(transform.width());
        out.writeDouble(transform.height());
        out.writeDouble(transform.rotationDegrees());
        out.writeBoolean(transform.mirrorHorizontal());
        out.writeBoolean(transform.mirrorVertical());
    }

    private static LayerTransform readTransform(
            DataInputStream in
    ) throws IOException {
        return new LayerTransform(
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readBoolean(),
                in.readBoolean()
        );
    }

    private static void writeRect(
            DataOutputStream out,
            NormalizedRect rect
    ) throws IOException {
        out.writeDouble(rect.x());
        out.writeDouble(rect.y());
        out.writeDouble(rect.width());
        out.writeDouble(rect.height());
    }

    private static NormalizedRect readRect(
            DataInputStream in
    ) throws IOException {
        return new NormalizedRect(
                in.readDouble(),
                in.readDouble(),
                in.readDouble(),
                in.readDouble()
        );
    }

    private static CanvasHeader readCanvasHeader(
            DataInputStream in
    ) throws IOException {
        int width = in.readInt();
        int height = in.readInt();

        if (width <= 0
                || height <= 0
                || width > MAX_CANVAS_DIMENSION
                || height > MAX_CANVAS_DIMENSION) {
            throw new IllegalArgumentException(
                    "Canvas dimensions out of range"
            );
        }

        int expectedPixels = Math.multiplyExact(width, height);
        int layerCount = in.readInt();

        if (layerCount < 0 || layerCount > MAX_LAYER_COUNT) {
            throw new IllegalArgumentException(
                    "Layer count out of range"
            );
        }

        return new CanvasHeader(
                width,
                height,
                layerCount,
                expectedPixels
        );
    }

    private static int[] readPixels(
            DataInputStream in,
            int count
    ) throws IOException {
        int[] pixels = new int[count];
        for (int i = 0; i < count; i++) {
            pixels[i] = in.readInt();
        }
        return pixels;
    }

    private static void validateEmbeddedImage(
            int width,
            int height
    ) {
        if (width <= 0
                || height <= 0
                || width > MAX_EMBEDDED_IMAGE_DIMENSION
                || height > MAX_EMBEDDED_IMAGE_DIMENSION) {
            throw new IllegalArgumentException(
                    "Embedded image dimensions out of range"
            );
        }
    }

    static void rejectTrailingBytes(
            DataInputStream in
    ) throws IOException {
        if (in.available() != 0) {
            throw new IllegalArgumentException(
                    "Trailing bytes in Loom project"
            );
        }
    }

    private static void validateEnvelopeSize(byte[] data) {
        if (data == null
                || data.length == 0
                || data.length > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid Loom project byte size"
            );
        }
    }

    public static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(data)
            );
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(
                    "SHA-256 unavailable",
                    impossible
            );
        }
    }

    public static boolean isValidHash(String hash) {
        if (hash == null || hash.length() != 64) {
            return false;
        }

        for (int i = 0; i < hash.length(); i++) {
            char c = hash.charAt(i);
            if (!((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }

        return true;
    }

    private static void writeUuid(
            DataOutputStream out,
            UUID uuid
    ) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    private static UUID readUuid(
            DataInputStream in
    ) throws IOException {
        return new UUID(
                in.readLong(),
                in.readLong()
        );
    }

    private static void writeString(
            DataOutputStream out,
            String value
    ) throws IOException {
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);

        if (utf8.length > MAX_STRING_BYTES) {
            throw new IllegalArgumentException(
                    "String exceeds Loom project limit"
            );
        }

        out.writeInt(utf8.length);
        out.write(utf8);
    }

    private static String readString(
            DataInputStream in,
            int maxChars
    ) throws IOException {
        int byteLength = in.readInt();

        if (byteLength < 0 || byteLength > MAX_STRING_BYTES) {
            throw new IllegalArgumentException(
                    "String byte length out of range"
            );
        }

        byte[] utf8 = in.readNBytes(byteLength);
        if (utf8.length != byteLength) {
            throw new EOFException(
                    "Truncated UTF-8 string"
            );
        }

        String value = new String(
                utf8,
                StandardCharsets.UTF_8
        );

        if (value.isBlank() || value.length() > maxChars) {
            throw new IllegalArgumentException(
                    "String character length out of range"
            );
        }

        return value;
    }

    private record CanvasHeader(
            int width,
            int height,
            int layerCount,
            int expectedPixels
    ) {
    }

    record CommonProjectData(
            UUID projectId,
            String name,
            LoomProjectMetadata metadata,
            LoomRuntimeSettings runtime
    ) {
    }
}
