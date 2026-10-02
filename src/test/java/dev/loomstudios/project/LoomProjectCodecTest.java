package dev.loomstudios.project;

import dev.loomstudios.image.ImagePlacementMode;
import dev.loomstudios.image.ImageProcessingMode;
import dev.loomstudios.image.ImageProcessingSettings;
import dev.loomstudios.image.PixelImage;
import dev.loomstudios.palette.ColorPalette;
import dev.loomstudios.palette.ColorPaletteCodec;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
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
    void emissiveRuntimeMirrorStaysEnabledUntilFinalLayerIsDisabled() {
        LoomProject project = LoomProjectFactory.blank("Multi Emissive", 1L);
        UUID baseId = project.cape().layers().getFirst().id();

        LoomProject withSecond = ProjectEdits.addCapeLayer(project, "Second");
        UUID secondId = withSecond.cape().layers().getLast().id();

        LoomProject bothEnabled = ProjectEdits.setCapeLayerEmissive(
                ProjectEdits.setCapeLayerEmissive(withSecond, baseId, true),
                secondId,
                true
        );
        assertTrue(bothEnabled.runtime().emissiveEnabled());

        LoomProject oneRemaining = ProjectEdits.setCapeLayerEmissive(
                bothEnabled,
                baseId,
                false
        );
        assertTrue(oneRemaining.runtime().emissiveEnabled());

        LoomProject noneRemaining = ProjectEdits.setCapeLayerEmissive(
                oneRemaining,
                secondId,
                false
        );
        assertFalse(noneRemaining.runtime().emissiveEnabled());
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
    void pixelSelectionNormalizesDragEndpointsAndRejectsInvalidBounds() {
        PixelSelection selection = PixelSelection.between(7, 9, 2, 3);

        assertEquals(2, selection.minX());
        assertEquals(3, selection.minY());
        assertEquals(7, selection.maxX());
        assertEquals(9, selection.maxY());
        assertEquals(6, selection.width());
        assertEquals(7, selection.height());
        assertTrue(selection.contains(4, 5));
        assertFalse(selection.contains(8, 5));

        assertThrows(
                IllegalArgumentException.class,
                () -> new PixelSelection(-1, 0, 1, 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new PixelSelection(4, 4, 3, 5)
        );
    }

    @Test
    void selectionVerticalAndCombinedFlipAreDeterministic() {
        LoomProject project = LoomProjectFactory.blank("Selection Flip", 1L);
        LoomLayer layer = project.cape().layers().getFirst();

        int[][] colors = {
                {0xFFFF0000, 0xFF00FF00},
                {0xFF0000FF, 0xFFFFFF00}
        };

        LoomProject painted = project;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                painted = ProjectEdits.setCapeRegionPixel(
                        painted,
                        layer.id(),
                        CapeUvRegion.OUTSIDE,
                        1 + x,
                        1 + y,
                        colors[y][x]
                );
            }
        }

        PixelSelection selection = new PixelSelection(1, 1, 2, 2);

        LoomProject vertical = ProjectEdits.flipCapeRegionSelection(
                painted,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                selection,
                false,
                true
        );

        int topLeft = CapeUvRegion.OUTSIDE.atlasY(1)
                * vertical.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(1);
        int bottomRight = CapeUvRegion.OUTSIDE.atlasY(2)
                * vertical.cape().width()
                + CapeUvRegion.OUTSIDE.atlasX(2);

        assertEquals(
                0xFF0000FF,
                vertical.cape().layers().getFirst().pixelAt(topLeft)
        );
        assertEquals(
                0xFF00FF00,
                vertical.cape().layers().getFirst().pixelAt(bottomRight)
        );

        LoomProject combined = ProjectEdits.flipCapeRegionSelection(
                painted,
                layer.id(),
                CapeUvRegion.OUTSIDE,
                selection,
                true,
                true
        );

        assertEquals(
                0xFFFFFF00,
                combined.cape().layers().getFirst().pixelAt(topLeft)
        );
        assertEquals(
                0xFFFF0000,
                combined.cape().layers().getFirst().pixelAt(bottomRight)
        );
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
    void schemaV1SnapshotMigratesThroughTypedSchemaV3() {
        LoomProject current = LoomProjectFactory.blank("Legacy", 77L);
        byte[] legacy = LoomProjectCodec.encodeVersion1SnapshotForTest(
                current
        );

        LoomProject migrated = LoomProjectCodec.decode(legacy);

        assertEquals(3, migrated.schemaVersion());
        assertEquals(LayerKind.PAINT, migrated.cape().layers().getFirst().kind());
        assertFalse(migrated.cape().layers().getFirst().locked());
        assertArrayEquals(
                current.cape().layers().getFirst().pixels(),
                migrated.cape().layers().getFirst().pixels()
        );
    }

    @Test
    void schemaV3RoundTripPreservesImageGradientAndLockState() {
        LoomProject project = LoomProjectFactory.blank("Typed", 1L);
        PixelImage source = new PixelImage(
                2,
                2,
                new int[]{
                        0xFFFF0000,
                        0xFF00FF00,
                        0xFF0000FF,
                        0x80FFFFFF
                }
        );

        ImageLayerData imageData = ImageLayerData.placed(
                source,
                project.cape().width(),
                project.cape().height(),
                NormalizedRect.fullCanvas(),
                ImagePlacementMode.FIT
        ).withProcessing(
                ImageProcessingSettings.defaults()
                        .withMode(ImageProcessingMode.POSTERIZE)
                        .withPosterizeLevels(3)
        );

        LoomProject withImage = ProjectEdits.addCapeImageLayer(
                project,
                "Imported",
                imageData
        );
        UUID imageId = withImage.cape().layers().getLast().id();
        withImage = ProjectEdits.setCapeLayerLocked(
                withImage,
                imageId,
                true
        );

        GradientLayerData gradient =
                GradientLayerData.defaultLinear(
                        0xFFFF0000,
                        0xFF0000FF,
                        NormalizedRect.fullCanvas()
                ).withType(GradientType.RADIAL)
                        .withDither(true);

        LoomProject typed = ProjectEdits.addCapeGradientLayer(
                withImage,
                "Gradient",
                gradient
        );

        LoomProject decoded = LoomProjectCodec.decode(typed.encode());

        assertEquals(typed, decoded);
        assertEquals(3, decoded.schemaVersion());
        assertEquals(
                LayerKind.IMAGE,
                decoded.cape().layers().get(1).kind()
        );
        assertTrue(decoded.cape().layers().get(1).locked());
        assertEquals(
                LayerKind.GRADIENT,
                decoded.cape().layers().get(2).kind()
        );
    }

    @Test
    void imageLayerRasterizesThroughPersistentTransformAndProcessing() {
        PixelImage source = new PixelImage(
                2,
                1,
                new int[]{0xFFFF0000, 0xFF0000FF}
        );
        ImageLayerData data = ImageLayerData.placed(
                source,
                4,
                2,
                NormalizedRect.fullCanvas(),
                ImagePlacementMode.STRETCH
        );

        LoomLayer layer = LoomLayer.image(
                UUID.randomUUID(),
                "Image",
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                data
        );

        int[] raster = LayerRasterizer.rasterize(layer, 4, 2);

        assertEquals(0xFFFF0000, raster[0]);
        assertEquals(0xFF0000FF, raster[3]);
        assertEquals(0xFFFF0000, raster[4]);
        assertEquals(0xFF0000FF, raster[7]);
    }

    @Test
    void gradientLayerRasterizationIsDeterministic() {
        GradientLayerData data = GradientLayerData.defaultLinear(
                0xFF000000,
                0xFFFFFFFF,
                NormalizedRect.fullCanvas()
        );

        LoomLayer layer = LoomLayer.gradient(
                UUID.randomUUID(),
                "Gradient",
                true,
                1.0F,
                BlendMode.NORMAL,
                false,
                false,
                data
        );

        int[] first = LayerRasterizer.rasterize(layer, 8, 4);
        int[] second = LayerRasterizer.rasterize(layer, 8, 4);

        assertArrayEquals(first, second);
        assertNotEquals(first[0], first[7]);
    }

    @Test
    void typedLayersKeepNormalizedPlacementAcrossResolutionChanges() {
        LoomProject project = LoomProjectFactory.blank("Resize Typed", 1L);
        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0xFFFFFFFF}
        );

        ImageLayerData data = ImageLayerData.placed(
                source,
                project.cape().width(),
                project.cape().height(),
                NormalizedRect.fullCanvas(),
                ImagePlacementMode.FIT
        );

        LoomProject withImage = ProjectEdits.addCapeImageLayer(
                project,
                "Image",
                data
        );
        LoomLayer before = withImage.cape().layers().getLast();

        LoomProject resized = ProjectResizer.resizeCape(
                withImage,
                CanvasResolution.ULTRA
        );
        LoomLayer after = resized.cape().layers().getLast();

        assertEquals(LayerKind.IMAGE, after.kind());
        assertEquals(before.imageData(), after.imageData());
        assertEquals(256, resized.cape().width());
        assertEquals(128, resized.cape().height());
    }

    @Test
    void lockedAndTypedLayersRejectPaintMutation() {
        LoomProject project = LoomProjectFactory.blank("Lock", 1L);
        UUID baseId = project.cape().layers().getFirst().id();

        LoomProject locked = ProjectEdits.setCapeLayerLocked(
                project,
                baseId,
                true
        );

        assertThrows(
                IllegalStateException.class,
                () -> ProjectEdits.setCapePixel(
                        locked,
                        baseId,
                        1,
                        1,
                        0xFFFFFFFF
                )
        );

        PixelImage source = new PixelImage(
                1,
                1,
                new int[]{0xFFFFFFFF}
        );
        LoomProject imageProject = ProjectEdits.addCapeImageLayer(
                project,
                "Image",
                ImageLayerData.placed(
                        source,
                        project.cape().width(),
                        project.cape().height(),
                        NormalizedRect.fullCanvas(),
                        ImagePlacementMode.FIT
                )
        );
        UUID imageId = imageProject.cape().layers().getLast().id();

        assertThrows(
                IllegalStateException.class,
                () -> ProjectEdits.setCapePixel(
                        imageProject,
                        imageId,
                        1,
                        1,
                        0xFF000000
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
        encoded[7] = 4;

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
