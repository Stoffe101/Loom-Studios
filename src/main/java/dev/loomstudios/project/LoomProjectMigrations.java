package dev.loomstudios.project;

/** Single entry point for loading versioned .loom data. */
public final class LoomProjectMigrations {
    private LoomProjectMigrations() {}

    public static LoomProject decodeAndMigrate(byte[] data) {
        int schemaVersion = LoomProjectCodec.peekSchemaVersion(data);

        LoomProject project =
                switch (schemaVersion) {
                    case 1 -> migrateV2ToV3(migrateV1ToV2(LoomProjectCodec.decodeVersion1(data)));
                    case 2 -> migrateV2ToV3(LoomProjectCodec.decodeVersion2(data));
                    case 3 -> LoomProjectCodec.decodeVersion3(data);
                    case 4 -> LoomProjectV4Codec.decode(data);
          case 5 -> LoomProjectV5Codec.decode(data);
                    default ->
                            throw new IllegalArgumentException(
                                    "Unsupported Loom project schema " + schemaVersion);
                };
        return project.schemaVersion() == 5
                ? project
                : new LoomProject(
            5,
                        project.projectId(),
                        project.name(),
                        project.metadata(),
                        project.cape(),
            project.schemaVersion() < 4 ?
                        migrateElytraAtlas(project.elytra()) : project.elytra(),
                        project.runtime(),
                        project.animation());
    }

    private static LoomProject migrateV1ToV2(LoomProject legacy) {
        if (legacy.schemaVersion() != 1) {
            throw new IllegalArgumentException("Expected schema-v1 project for migration");
        }

        return new LoomProject(
                2,
                legacy.projectId(),
                legacy.name(),
                legacy.metadata(),
                legacy.cape(),
                legacy.elytra(),
                legacy.runtime(),
                LoomAnimation.empty());
    }

    private static LoomProject migrateV2ToV3(LoomProject legacy) {
        if (legacy.schemaVersion() != 2) {
            throw new IllegalArgumentException("Expected schema-v2 project for migration");
        }

        return new LoomProject(
                3,
                legacy.projectId(),
                legacy.name(),
                legacy.metadata(),
                legacy.cape(),
                legacy.elytra(),
                legacy.runtime(),
                LoomAnimation.empty());
    }

    private static LoomCanvas migrateElytraAtlas(LoomCanvas canvas) {
        return new LoomCanvas(
                canvas.width(),
                canvas.height(),
                canvas.layers().stream().map(layer -> layer.withLegacyWingUv(true)).toList());
    }
}
