package dev.loomstudios.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Tiny versioned project used only by SPIKE-05/06 to prove the real transport,
 * cache and renderer architecture before the full .loom schema is implemented.
 */
public record ProofProject(
        int accentArgb,
        float elytraThickness,
        int animationPeriodTicks,
        boolean emissiveEnabled
) {
    public static final int FORMAT_VERSION = 1;
    public static final int MAX_BYTES = 256;

    public ProofProject {
        if (!Float.isFinite(elytraThickness)
                || elytraThickness < 0.25F
                || elytraThickness > 2.0F) {
            throw new IllegalArgumentException("Elytra thickness out of range");
        }

        if (animationPeriodTicks < 10 || animationPeriodTicks > 200) {
            throw new IllegalArgumentException("Animation period out of range");
        }
    }

    public static ProofProject forPlayer(UUID uuid) {
        int[] accents = {
                0xFF00DCE8,
                0xFFFF36C8,
                0xFF8E5CFF,
                0xFF61EA72,
                0xFFFFAD32,
                0xFF4A8DFF
        };

        int index = Math.floorMod(uuid.hashCode(), accents.length);
        return new ProofProject(accents[index], 1.0F, 40, true);
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeByte(FORMAT_VERSION);
                out.writeInt(this.accentArgb);
                out.writeFloat(this.elytraThickness);
                out.writeShort(this.animationPeriodTicks);
                out.writeBoolean(this.emissiveEnabled);
            }

            byte[] data = bytes.toByteArray();
            if (data.length > MAX_BYTES) {
                throw new IllegalStateException("Proof project exceeded byte limit");
            }
            return data;
        } catch (IOException impossible) {
            throw new IllegalStateException("Unexpected in-memory serialization error", impossible);
        }
    }

    public static ProofProject decode(byte[] data) {
        if (data.length == 0 || data.length > MAX_BYTES) {
            throw new IllegalArgumentException("Invalid proof project size");
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            int version = in.readUnsignedByte();
            if (version != FORMAT_VERSION) {
                throw new IllegalArgumentException("Unsupported proof project version " + version);
            }

            ProofProject project = new ProofProject(
                    in.readInt(),
                    in.readFloat(),
                    in.readUnsignedShort(),
                    in.readBoolean()
            );

            if (in.available() != 0) {
                throw new IllegalArgumentException("Trailing bytes in proof project");
            }

            return project;
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed proof project", e);
        }
    }

    public String hash() {
        return sha256(this.encode());
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
}
