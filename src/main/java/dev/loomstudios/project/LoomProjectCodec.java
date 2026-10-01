package dev.loomstudios.project;

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

/**
 * Deterministic bounded binary codec for .loom schema v1.
 */
public final class LoomProjectCodec {
    private static final int MAGIC = 0x4C4F4F4D; // LOOM

    public static final int MAX_SERIALIZED_BYTES = 256 * 1024;
    public static final int MAX_CANVAS_DIMENSION = 256;
    public static final int MAX_LAYER_COUNT = 64;
    public static final int MAX_PROJECT_NAME_CHARS = 96;
    public static final int MAX_LAYER_NAME_CHARS = 96;
    private static final int MAX_STRING_BYTES = 512;

    private LoomProjectCodec() {
    }

    public static byte[] encode(LoomProject project) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeInt(MAGIC);
                out.writeInt(project.schemaVersion());
                writeUuid(out, project.projectId());
                writeString(out, project.name());
                out.writeLong(project.metadata().createdAtEpochMillis());
                out.writeLong(project.metadata().modifiedAtEpochMillis());

                LoomRuntimeSettings runtime = project.runtime();
                out.writeFloat(runtime.elytraThickness());
                out.writeInt(runtime.animationPeriodTicks());
                out.writeBoolean(runtime.hueCycleEnabled());
                out.writeBoolean(runtime.emissiveEnabled());

                writeCanvas(out, project.cape());
                writeCanvas(out, project.elytra());
            }

            byte[] result = bytes.toByteArray();
            if (result.length > MAX_SERIALIZED_BYTES) {
                throw new IllegalArgumentException("Loom project exceeds serialized size limit");
            }

            return result;
        } catch (IOException impossible) {
            throw new IllegalStateException("Unexpected in-memory serialization failure", impossible);
        }
    }

    public static LoomProject decode(byte[] data) {
        return LoomProjectMigrations.decodeAndMigrate(data);
    }

    static int peekSchemaVersion(byte[] data) {
        validateEnvelopeSize(data);

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            if (in.readInt() != MAGIC) {
                throw new IllegalArgumentException("Invalid Loom project magic");
            }
            return in.readInt();
        } catch (EOFException e) {
            throw new IllegalArgumentException("Truncated Loom project", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed Loom project", e);
        }
    }

    static LoomProject decodeVersion1(byte[] data) {
        validateEnvelopeSize(data);

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            if (in.readInt() != MAGIC) {
                throw new IllegalArgumentException("Invalid Loom project magic");
            }

            int schemaVersion = in.readInt();
            if (schemaVersion != 1) {
                throw new IllegalArgumentException(
                        "Expected Loom project schema 1 but found " + schemaVersion
                );
            }

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

            LoomCanvas cape = readCanvas(in);
            LoomCanvas elytra = readCanvas(in);

            if (in.available() != 0) {
                throw new IllegalArgumentException("Trailing bytes in Loom project");
            }

            return new LoomProject(
                    schemaVersion,
                    projectId,
                    name,
                    metadata,
                    cape,
                    elytra,
                    runtime
            );
        } catch (EOFException e) {
            throw new IllegalArgumentException("Truncated Loom project", e);
        } catch (IOException | ArithmeticException e) {
            throw new IllegalArgumentException("Malformed Loom project", e);
        }
    }

    private static void validateEnvelopeSize(byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_SERIALIZED_BYTES) {
            throw new IllegalArgumentException("Invalid Loom project byte size");
        }
    }

    public static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(data)
            );
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    public static boolean isValidHash(String hash) {
        if (hash == null || hash.length() != 64) {
            return false;
        }

        for (int i = 0; i < hash.length(); i++) {
            char c = hash.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }

        return true;
    }

    private static void writeCanvas(DataOutputStream out, LoomCanvas canvas)
            throws IOException {
        out.writeInt(canvas.width());
        out.writeInt(canvas.height());
        out.writeInt(canvas.layers().size());

        for (LoomLayer layer : canvas.layers()) {
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

    private static LoomCanvas readCanvas(DataInputStream in) throws IOException {
        int width = in.readInt();
        int height = in.readInt();

        if (width <= 0
                || height <= 0
                || width > MAX_CANVAS_DIMENSION
                || height > MAX_CANVAS_DIMENSION) {
            throw new IllegalArgumentException("Canvas dimensions out of range");
        }

        int expectedPixels = Math.multiplyExact(width, height);
        int layerCount = in.readInt();

        if (layerCount < 0 || layerCount > MAX_LAYER_COUNT) {
            throw new IllegalArgumentException("Layer count out of range");
        }

        List<LoomLayer> layers = new ArrayList<>(layerCount);

        for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
            UUID id = readUuid(in);
            String name = readString(in, MAX_LAYER_NAME_CHARS);
            boolean visible = in.readBoolean();
            float opacity = in.readFloat();
            int blendOrdinal = in.readUnsignedByte();
            boolean emissive = in.readBoolean();
            int pixelCount = in.readInt();

            if (blendOrdinal >= BlendMode.values().length) {
                throw new IllegalArgumentException("Unknown blend mode");
            }

            if (pixelCount != expectedPixels) {
                throw new IllegalArgumentException("Layer pixel count mismatch");
            }

            int[] pixels = new int[pixelCount];
            for (int i = 0; i < pixelCount; i++) {
                pixels[i] = in.readInt();
            }

            layers.add(new LoomLayer(
                    id,
                    name,
                    visible,
                    opacity,
                    BlendMode.values()[blendOrdinal],
                    emissive,
                    pixels
            ));
        }

        return new LoomCanvas(width, height, layers);
    }

    private static void writeUuid(DataOutputStream out, UUID uuid)
            throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    private static UUID readUuid(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }

    private static void writeString(DataOutputStream out, String value)
            throws IOException {
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        if (utf8.length > MAX_STRING_BYTES) {
            throw new IllegalArgumentException("String exceeds Loom project limit");
        }

        out.writeInt(utf8.length);
        out.write(utf8);
    }

    private static String readString(DataInputStream in, int maxChars)
            throws IOException {
        int byteLength = in.readInt();
        if (byteLength < 0 || byteLength > MAX_STRING_BYTES) {
            throw new IllegalArgumentException("String byte length out of range");
        }

        byte[] utf8 = in.readNBytes(byteLength);
        if (utf8.length != byteLength) {
            throw new EOFException("Truncated UTF-8 string");
        }

        String value = new String(utf8, StandardCharsets.UTF_8);
        if (value.isBlank() || value.length() > maxChars) {
            throw new IllegalArgumentException("String character length out of range");
        }

        return value;
    }
}
