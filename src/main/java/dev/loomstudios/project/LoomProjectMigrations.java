package dev.loomstudios.project;

/**
 * Single entry point for loading versioned .loom data.
 */
public final class LoomProjectMigrations {
    private LoomProjectMigrations() {
    }

    public static LoomProject decodeAndMigrate(byte[] data) {
        int schemaVersion = LoomProjectCodec.peekSchemaVersion(data);

        return switch (schemaVersion) {
            case 1 -> migrateV1ToV2(
                    LoomProjectCodec.decodeVersion1(data)
            );
            case 2 -> LoomProjectCodec.decodeVersion2(data);
            default -> throw new IllegalArgumentException(
                    "Unsupported Loom project schema " + schemaVersion
            );
        };
    }

    private static LoomProject migrateV1ToV2(LoomProject legacy) {
        if (legacy.schemaVersion() != 1) {
            throw new IllegalArgumentException(
                    "Expected schema-v1 project for migration"
            );
        }

        return new LoomProject(
                2,
                legacy.projectId(),
                legacy.name(),
                legacy.metadata(),
                legacy.cape(),
                legacy.elytra(),
                legacy.runtime()
        );
    }
}
