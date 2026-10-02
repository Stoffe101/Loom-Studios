package dev.loomstudios.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Self-contained Loom Studios project transfer code.
 *
 * <p>LSP1 is intentionally local/offline: the code contains the compressed
 * project itself and does not require a resolver service. The shorter LS
 * design ID is a deterministic fingerprint only and is not presented as a
 * remotely resolvable code until a real service exists.</p>
 */
public final class LoomProjectCode {
    public static final String PORTABLE_PREFIX = "LSP1:";
    private static final String DESIGN_ALPHABET =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    public static final int MAX_COMPRESSED_BYTES =
            LoomProjectCodec.MAX_SERIALIZED_BYTES + 64 * 1024;
    public static final int MAX_PORTABLE_CODE_CHARS =
            PORTABLE_PREFIX.length()
                    + ((MAX_COMPRESSED_BYTES + 2) / 3) * 4;

    private LoomProjectCode() {
    }

    public static String encodePortable(LoomProject project) {
        Objects.requireNonNull(project, "project");
        byte[] raw = project.encode();
        byte[] compressed = deflate(raw);

        if (compressed.length > MAX_COMPRESSED_BYTES) {
            throw new IllegalArgumentException(
                    "Compressed Loom project exceeds portable-code limit"
            );
        }

        return PORTABLE_PREFIX
                + Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(compressed);
    }

    public static LoomProject decodePortable(String code) {
        String normalized = Objects.requireNonNull(
                code,
                "code"
        ).trim();

        if (!normalized.startsWith(PORTABLE_PREFIX)) {
            throw new IllegalArgumentException(
                    "Unsupported Loom portable-code prefix"
            );
        }
        if (normalized.length() > MAX_PORTABLE_CODE_CHARS) {
            throw new IllegalArgumentException(
                    "Loom portable code exceeds size limit"
            );
        }

        String body = normalized.substring(PORTABLE_PREFIX.length());
        if (body.isBlank()) {
            throw new IllegalArgumentException(
                    "Loom portable code is empty"
            );
        }

        final byte[] compressed;
        try {
            compressed = Base64.getUrlDecoder().decode(body);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Malformed Loom portable-code Base64",
                    e
            );
        }

        if (compressed.length <= 0
                || compressed.length > MAX_COMPRESSED_BYTES) {
            throw new IllegalArgumentException(
                    "Compressed Loom portable payload is out of range"
            );
        }

        byte[] raw = inflateBounded(compressed);
        return LoomProjectCodec.decode(raw);
    }

    public static boolean looksPortable(String value) {
        return value != null
                && value.trim().startsWith(PORTABLE_PREFIX);
    }

    /**
     * Human-friendly deterministic project fingerprint.
     *
     * <p>This is deliberately not called a remotely resolvable share code in
     * the implementation. A future service can choose to register this ID or
     * issue another short code, but offline import uses LSP1.</p>
     */
    public static String designId(LoomProject project) {
        Objects.requireNonNull(project, "project");

        byte[] digest = HexFormat.of().parseHex(project.hash());
        long bits = 0L;
        for (int i = 0; i < 8; i++) {
            bits = (bits << 8) | (digest[i] & 0xFFL);
        }

        char[] chars = new char[12];
        for (int i = chars.length - 1; i >= 0; i--) {
            chars[i] = DESIGN_ALPHABET.charAt((int)(bits & 31L));
            bits >>>= 5;
        }

        return "LS-"
                + new String(chars, 0, 4)
                + "-"
                + new String(chars, 4, 4)
                + "-"
                + new String(chars, 8, 4);
    }

    /**
     * Imported projects become new local projects so receiving a design
     * cannot overwrite an existing library entry that happens to share the
     * sender's project UUID.
     */
    public static LoomProject forkImported(
            LoomProject source,
            long nowEpochMillis
    ) {
        Objects.requireNonNull(source, "source");

        String suffix = " (Imported)";
        int maximumBase = Math.max(
                1,
                LoomProjectCodec.MAX_PROJECT_NAME_CHARS - suffix.length()
        );
        String baseName = source.name().length() > maximumBase
                ? source.name().substring(0, maximumBase)
                : source.name();

        return new LoomProject(
                LoomProject.CURRENT_SCHEMA_VERSION,
                UUID.randomUUID(),
                baseName + suffix,
                LoomProjectMetadata.now(nowEpochMillis),
                source.cape(),
                source.elytra(),
                source.runtime(),
                source.animation()
        );
    }

    private static byte[] deflate(byte[] raw) {
        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream(Math.min(raw.length, 64 * 1024));
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

        try {
            try (DeflaterOutputStream out =
                         new DeflaterOutputStream(bytes, deflater)) {
                out.write(raw);
            }
        } catch (IOException impossible) {
            throw new IllegalStateException(
                    "Unexpected in-memory portable-code compression failure",
                    impossible
            );
        } finally {
            deflater.end();
        }

        return bytes.toByteArray();
    }

    private static byte[] inflateBounded(byte[] compressed) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();

        try {
            try (InflaterInputStream in = new InflaterInputStream(
                    new ByteArrayInputStream(compressed),
                    inflater
            )) {
                byte[] buffer = new byte[8192];
                int total = 0;
                int read;

                while ((read = in.read(buffer)) != -1) {
                    total = Math.addExact(total, read);
                    if (total > LoomProjectCodec.MAX_SERIALIZED_BYTES) {
                        throw new IllegalArgumentException(
                                "Inflated Loom portable payload exceeds project limit"
                        );
                    }
                    output.write(buffer, 0, read);
                }
            }
        } catch (IOException | ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Malformed Loom portable payload",
                    e
            );
        } finally {
            inflater.end();
        }

        byte[] raw = output.toByteArray();
        if (raw.length == 0) {
            throw new IllegalArgumentException(
                    "Inflated Loom portable payload is empty"
            );
        }
        return raw;
    }
}
