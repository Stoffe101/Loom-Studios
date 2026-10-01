package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoomProjectCodecTest {
    private static final UUID PLAYER_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Test
    void blankProjectHasRealMetadataAndTransparentBaseLayers() {
        LoomProject project = LoomProjectFactory.blank("Blank Cape", 1234L);

        assertEquals("Blank Cape", project.name());
        assertEquals(1234L, project.metadata().createdAtEpochMillis());
        assertEquals(1234L, project.metadata().modifiedAtEpochMillis());
        assertEquals(1, project.cape().layers().size());
        assertEquals(1, project.elytra().layers().size());

        assertTrue(
                Arrays.stream(project.cape().layers().getFirst().pixels())
                        .allMatch(pixel -> pixel == 0)
        );
        assertTrue(
                Arrays.stream(project.elytra().layers().getFirst().pixels())
                        .allMatch(pixel -> pixel == 0)
        );
    }

    @Test
    void pixelEditReplacesOnlyRequestedCapePixel() {
        LoomProject project = LoomProjectFactory.blank("Paint Test", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject edited = ProjectEdits.setCapePixel(
                project,
                layer.id(),
                3,
                4,
                0xFFABCDEF
        );

        int changedIndex = 4 * LoomProject.TEXTURE_WIDTH + 3;
        assertEquals(
                0xFFABCDEF,
                edited.cape().layers().getFirst().pixelAt(changedIndex)
        );
        assertEquals(
                0,
                project.cape().layers().getFirst().pixelAt(changedIndex)
        );
        assertEquals(project.elytra(), edited.elytra());
    }

    @Test
    void projectRoundTripIsDeterministic() {
        LoomProject original = LoomProjectFactory.forPlayer(PLAYER_ID);

        byte[] firstEncoding = original.encode();
        LoomProject decoded = LoomProjectCodec.decode(firstEncoding);
        byte[] secondEncoding = decoded.encode();

        assertEquals(original, decoded);
        assertArrayEquals(firstEncoding, secondEncoding);
        assertEquals(
                LoomProjectCodec.sha256(firstEncoding),
                LoomProjectCodec.sha256(secondEncoding)
        );
    }

    @Test
    void layerPixelsAreDefensivelyCopied() {
        int[] pixels = new int[LoomProject.TEXTURE_WIDTH * LoomProject.TEXTURE_HEIGHT];
        pixels[0] = 0xFF123456;

        LoomLayer layer = new LoomLayer(
                UUID.randomUUID(),
                "Paint",
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                pixels
        );

        pixels[0] = 0;
        assertEquals(0xFF123456, layer.pixelAt(0));

        int[] copy = layer.pixels();
        copy[0] = 0;
        assertEquals(0xFF123456, layer.pixelAt(0));
    }

    @Test
    void trailingBytesAreRejected() {
        byte[] encoded = LoomProjectFactory.forPlayer(PLAYER_ID).encode();
        byte[] corrupted = Arrays.copyOf(encoded, encoded.length + 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> LoomProjectCodec.decode(corrupted)
        );
    }

    @Test
    void oversizedOrWrongSizedRuntimeCanvasIsRejected() {
        LoomCanvas wrong = new LoomCanvas(
                32,
                32,
                java.util.List.of()
        );

        LoomProject valid = LoomProjectFactory.forPlayer(PLAYER_ID);

        assertThrows(
                IllegalArgumentException.class,
                () -> new LoomProject(
                        LoomProject.CURRENT_SCHEMA_VERSION,
                        valid.projectId(),
                        valid.name(),
                        valid.metadata(),
                        wrong,
                        valid.elytra(),
                        valid.runtime()
                )
        );
    }

    @Test
    void unsupportedSchemaUsesExplicitMigrationGate() {
        byte[] encoded = LoomProjectFactory.forPlayer(PLAYER_ID).encode();
        encoded[7] = 2;

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> LoomProjectCodec.decode(encoded)
        );

        assertTrue(error.getMessage().contains("Unsupported Loom project schema"));
    }

    @Test
    void undoRedoAndBranchingWork() {
        LoomProject first = LoomProjectFactory.forPlayer(PLAYER_ID);
        ProjectHistory history = new ProjectHistory(first);

        LoomProject second = new LoomProject(
                first.schemaVersion(),
                first.projectId(),
                "Second",
                first.metadata(),
                first.cape(),
                first.elytra(),
                first.runtime()
        );

        LoomProject third = new LoomProject(
                first.schemaVersion(),
                first.projectId(),
                "Third",
                first.metadata(),
                first.cape(),
                first.elytra(),
                first.runtime()
        );

        history.apply(ignored -> second);
        assertTrue(history.canUndo());
        assertEquals(second, history.current());

        history.undo();
        assertEquals(first, history.current());
        assertTrue(history.canRedo());

        history.apply(ignored -> third);
        assertEquals(third, history.current());
        assertFalse(history.canRedo());
    }
}
