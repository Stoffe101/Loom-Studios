package dev.loomstudios.project;

import dev.loomstudios.palette.ColorPalette;
import dev.loomstudios.palette.ColorPaletteCodec;
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
    void blankEditorProjectIsStaticByDefault() {
        LoomProject project = LoomProjectFactory.blank("Static", 1L);

        assertFalse(project.runtime().hueCycleEnabled());
        assertFalse(project.runtime().emissiveEnabled());
    }

    @Test
    void capeRegionEditMapsOutsideFaceToCorrectAtlasPixel() {
        LoomProject project = LoomProjectFactory.blank("Region Test", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject edited = ProjectEdits.setCapeRegionPixel(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                2,
                3,
                0xFF556677
        );

        int atlasX = CapeUvRegion.OUTSIDE.atlasX(2);
        int atlasY = CapeUvRegion.OUTSIDE.atlasY(3);
        int index = atlasY * LoomProject.TEXTURE_WIDTH + atlasX;

        assertEquals(
                0xFF556677,
                edited.cape().layers().getFirst().pixelAt(index)
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
    void capeCanResizeToFourTimesResolution() {
        LoomProject project = LoomProjectFactory.blank("Hi Res", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject painted = ProjectEdits.setCapeRegionPixel(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                2,
                3,
                0xFF123456
        );

        LoomProject resized = ProjectResizer.resizeCape(
                painted,
                CanvasResolution.ULTRA
        );

        assertEquals(256, resized.cape().width());
        assertEquals(128, resized.cape().height());
        assertEquals(CanvasResolution.ULTRA, CanvasResolution.fromCanvas(resized.cape()));

        int scale = CanvasResolution.ULTRA.scale();
        int atlasX = CapeUvRegion.OUTSIDE.atlasX(2 * scale, scale);
        int atlasY = CapeUvRegion.OUTSIDE.atlasY(3 * scale, scale);
        assertEquals(
                0xFF123456,
                resized.cape().layers().getFirst()
                        .pixelAt(atlasY * resized.cape().width() + atlasX)
        );
    }

    @Test
    void brushPaintsMultiplePixelsInRegion() {
        LoomProject project = LoomProjectFactory.blank("Brush", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject painted = ProjectEdits.paintCapeRegionBrush(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                5,
                8,
                3,
                0xFFABCDEF
        );

        long count = java.util.Arrays.stream(
                painted.cape().layers().getFirst().pixels()
        ).filter(pixel -> pixel == 0xFFABCDEF).count();

        assertTrue(count > 1);
    }

    @Test
    void paletteShareCodeRoundTrips() {
        ColorPalette palette = new ColorPalette(
                UUID.fromString("44444444-4444-4444-8444-444444444444"),
                "Sunset",
                java.util.List.of(
                        0xFFFF5368,
                        0xFFFFBE2E,
                        0xFF8E5CFF
                )
        );

        String code = ColorPaletteCodec.encodeShareCode(palette);
        ColorPalette decoded = ColorPaletteCodec.decodeShareCode(code);

        assertEquals(palette, decoded);
        assertTrue(code.startsWith(ColorPaletteCodec.SHARE_PREFIX));
    }

    @Test
    void paletteShareCodePreservesAlphaAndLegacyOpaqueColors() {
        ColorPalette palette = new ColorPalette(
                UUID.fromString("55555555-5555-4555-8555-555555555555"),
                "Alpha",
                java.util.List.of(
                        0x80112233,
                        0xFF445566
                )
        );

        String json = ColorPaletteCodec.encode(palette, false);
        assertTrue(json.contains("#80112233"));
        assertTrue(json.contains("#445566"));

        ColorPalette decoded = ColorPaletteCodec.decode(json);
        assertEquals(palette, decoded);

        ColorPalette legacy = ColorPaletteCodec.decode(
                "{\"format\":\"loom-studios-palette\","
                        + "\"version\":1,"
                        + "\"id\":\"66666666-6666-4666-8666-666666666666\","
                        + "\"name\":\"Legacy\","
                        + "\"colors\":[\"#ABCDEF\"]}"
        );
        assertEquals(0xFFABCDEF, legacy.colors().getFirst());
    }

    @Test
    void floodFillStaysInsideConnectedCapeRegion() {
        LoomProject project = LoomProjectFactory.blank("Fill", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject divider = project;
        for (int y = 0; y < CapeUvRegion.OUTSIDE.height(); y++) {
            divider = ProjectEdits.setCapeRegionPixel(
                    divider,
                    layer.id(),
                    CapeUvRegion.OUTSIDE,
                    5,
                    y,
                    0xFFFFFFFF
            );
        }

        LoomProject filled = ProjectEdits.floodFillCapeRegion(
                divider,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                2,
                2,
                0xFF112233
        );

        int leftX = CapeUvRegion.OUTSIDE.atlasX(2);
        int rightX = CapeUvRegion.OUTSIDE.atlasX(8);
        int y = CapeUvRegion.OUTSIDE.atlasY(2);

        assertEquals(
                0xFF112233,
                filled.cape().layers().getFirst()
                        .pixelAt(y * filled.cape().width() + leftX)
        );
        assertEquals(
                0,
                filled.cape().layers().getFirst()
                        .pixelAt(y * filled.cape().width() + rightX)
        );
    }

    @Test
    void lineToolPaintsBothEndpoints() {
        LoomProject project = LoomProjectFactory.blank("Line", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject painted = ProjectEdits.paintCapeRegionLine(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                1,
                1,
                8,
                12,
                1,
                0xFF44AAFF
        );

        int start = CapeUvRegion.OUTSIDE.atlasY(1)
                * painted.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(1);
        int end = CapeUvRegion.OUTSIDE.atlasY(12)
                * painted.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(8);

        assertEquals(
                0xFF44AAFF,
                painted.cape().layers().getFirst().pixelAt(start)
        );
        assertEquals(
                0xFF44AAFF,
                painted.cape().layers().getFirst().pixelAt(end)
        );
    }

    @Test
    void rectangleToolSupportsOutlineAndFilledModes() {
        LoomProject project = LoomProjectFactory.blank("Rectangle", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject outline = ProjectEdits.paintCapeRegionRectangle(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                2,
                2,
                6,
                6,
                1,
                0xFFFF00AA,
                false
        );

        int center = CapeUvRegion.OUTSIDE.atlasY(4)
                * outline.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(4);
        assertEquals(
                0,
                outline.cape().layers().getFirst().pixelAt(center)
        );

        LoomProject filled = ProjectEdits.paintCapeRegionRectangle(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                2,
                2,
                6,
                6,
                1,
                0xFFFF00AA,
                true
        );

        assertEquals(
                0xFFFF00AA,
                filled.cape().layers().getFirst().pixelAt(center)
        );
    }

    @Test
    void capeLayerOperationsPreserveEditableStackRules() {
        LoomProject project = LoomProjectFactory.blank("Layers", 1L);
        UUID baseId = project.cape().layers().getFirst().id();

        LoomProject added = ProjectEdits.addCapeLayer(project, "Paint");
        assertEquals(2, added.cape().layers().size());

        UUID paintId = added.cape().layers().getLast().id();
        LoomProject duplicated = ProjectEdits.duplicateCapeLayer(
                added,
                paintId
        );
        assertEquals(3, duplicated.cape().layers().size());
        assertNotEquals(
                paintId,
                duplicated.cape().layers().getLast().id()
        );

        LoomProject hidden = ProjectEdits.setCapeLayerVisible(
                duplicated,
                paintId,
                false
        );
        assertFalse(
                hidden.cape().layers().stream()
                        .filter(layer -> layer.id().equals(paintId))
                        .findFirst()
                        .orElseThrow()
                        .visible()
        );

        LoomProject faded = ProjectEdits.setCapeLayerOpacity(
                hidden,
                paintId,
                0.4F
        );
        assertEquals(
                0.4F,
                faded.cape().layers().stream()
                        .filter(layer -> layer.id().equals(paintId))
                        .findFirst()
                        .orElseThrow()
                        .opacity()
        );

        LoomProject removed = ProjectEdits.removeCapeLayer(
                faded,
                paintId
        );
        assertEquals(2, removed.cape().layers().size());

        LoomProject single = LoomProjectFactory.blank("Single", 1L);
        LoomProject stillSingle = ProjectEdits.removeCapeLayer(
                single,
                single.cape().layers().getFirst().id()
        );
        assertEquals(1, stillSingle.cape().layers().size());
        assertEquals(
                baseId,
                project.cape().layers().getFirst().id()
        );
    }

    @Test
    void layerRenameBlendAndEmissiveRoundTrip() {
        LoomProject project = LoomProjectFactory.blank("Layer Props", 1L);
        UUID layerId = project.cape().layers().getFirst().id();

        LoomProject edited = ProjectEdits.renameCapeLayer(
                project,
                layerId,
                "Glow"
        );
        edited = ProjectEdits.setCapeLayerBlendMode(
                edited,
                layerId,
                BlendMode.SCREEN
        );
        edited = ProjectEdits.setCapeLayerEmissive(
                edited,
                layerId,
                true
        );

        LoomProject decoded = LoomProjectCodec.decode(edited.encode());
        LoomLayer layer = decoded.cape().layers().getFirst();

        assertEquals("Glow", layer.name());
        assertEquals(BlendMode.SCREEN, layer.blendMode());
        assertTrue(layer.emissive());
    }

    @Test
    void emissiveLayerToggleKeepsRuntimeMasterInSync() {
        LoomProject project = LoomProjectFactory.blank("Emissive Toggle", 1L);
        UUID layerId = project.cape().layers().getFirst().id();

        assertFalse(project.runtime().emissiveEnabled());

        LoomProject enabled = ProjectEdits.setCapeLayerEmissive(
                project,
                layerId,
                true
        );
        assertTrue(enabled.cape().layers().getFirst().emissive());
        assertTrue(enabled.runtime().emissiveEnabled());

        LoomProject disabled = ProjectEdits.setCapeLayerEmissive(
                enabled,
                layerId,
                false
        );
        assertFalse(disabled.cape().layers().getFirst().emissive());
        assertFalse(disabled.runtime().emissiveEnabled());
    }

    @Test
    void blendModeOrdinalsPreserveSchemaV1Compatibility() {
        assertEquals(0, BlendMode.NORMAL.ordinal());
        assertEquals(1, BlendMode.ADD.ordinal());
        assertEquals(2, BlendMode.SCREEN.ordinal());
        assertEquals(3, BlendMode.MULTIPLY.ordinal());
        assertEquals(4, BlendMode.OVERLAY.ordinal());
    }

    @Test
    void selectionMoveAndFlipStayInsideSemanticFace() {
        LoomProject project = LoomProjectFactory.blank("Selection", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        LoomProject painted = ProjectEdits.setCapeRegionPixel(
                project,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                1,
                1,
                0xFFFF0000
        );
        painted = ProjectEdits.setCapeRegionPixel(
                painted,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                2,
                1,
                0xFF00FF00
        );

        PixelSelection selection = new PixelSelection(1, 1, 2, 1);

        LoomProject flipped = ProjectEdits.flipCapeRegionSelection(
                painted,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                selection,
                true,
                false
        );

        int left = CapeUvRegion.OUTSIDE.atlasY(1)
                * flipped.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(1);
        int right = CapeUvRegion.OUTSIDE.atlasY(1)
                * flipped.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(2);

        assertEquals(
                0xFF00FF00,
                flipped.cape().layers().getFirst().pixelAt(left)
        );
        assertEquals(
                0xFFFF0000,
                flipped.cape().layers().getFirst().pixelAt(right)
        );

        LoomProject moved = ProjectEdits.moveCapeRegionSelection(
                flipped,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                selection,
                2,
                3
        );

        assertEquals(
                0,
                moved.cape().layers().getFirst().pixelAt(left)
        );
        assertEquals(
                0,
                moved.cape().layers().getFirst().pixelAt(right)
        );

        int movedLeft = CapeUvRegion.OUTSIDE.atlasY(4)
                * moved.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(3);
        int movedRight = CapeUvRegion.OUTSIDE.atlasY(4)
                * moved.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(4);

        assertEquals(
                0xFF00FF00,
                moved.cape().layers().getFirst().pixelAt(movedLeft)
        );
        assertEquals(
                0xFFFF0000,
                moved.cape().layers().getFirst().pixelAt(movedRight)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ProjectEdits.flipCapeRegionSelection(
                        project,
                        layer.id(),
                        CapeUvRegion.OUTSIDE,
                        new PixelSelection(0, 0, 20, 20),
                        true,
                        false
                )
        );
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
