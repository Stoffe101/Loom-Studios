package dev.loomstudios.project;

/**
 * Single entry point for loading versioned .loom data.
 *
 * <p>Schema v1 is the first pre-release format, so there are no historical
 * migrations yet. New schema versions must be added here explicitly instead
 * of silently changing the meaning of old bytes.</p>
 */
public final class LoomProjectMigrations {
    private LoomProjectMigrations() {
    }

    public static LoomProject decodeAndMigrate(byte[] data) {
        int schemaVersion = LoomProjectCodec.peekSchemaVersion(data);

        return switch (schemaVersion) {
            case 1 -> LoomProjectCodec.decodeVersion1(data);
            default -> throw new IllegalArgumentException(
                    "Unsupported Loom project schema " + schemaVersion
            );
        };
    }
}
