package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ElytraWingEditingTest {
    @Test
    void linkedWingPixelMirrorsAcrossTheOppositeWing() {
        LoomProject project = LoomProjectFactory.blank("Elytra", 1L);
        UUID layerId = project.elytra().layers().getFirst().id();

        LoomProject edited = ProjectEdits.setElytraWingPixel(
                project,
                layerId,
                ElytraWing.LEFT,
                1,
                2,
                0xFF22D7E8,
                true
        );

        LoomLayer layer = edited.elytra().layers().getFirst();
        int left = ElytraWing.LEFT.atlasY(2, 1)
                * edited.elytra().width()
                + ElytraWing.LEFT.atlasX(1, 1);
        int rightX = ElytraWing.RIGHT.mirroredLocalX(1, 1);
        int right = ElytraWing.RIGHT.atlasY(2, 1)
                * edited.elytra().width()
                + ElytraWing.RIGHT.atlasX(rightX, 1);

        assertEquals(0xFF22D7E8, layer.pixelAt(left));
        assertEquals(0xFF22D7E8, layer.pixelAt(right));
    }

    @Test
    void separateWingEditDoesNotTouchOppositeWing() {
        LoomProject project = LoomProjectFactory.blank("Elytra", 1L);
        UUID layerId = project.elytra().layers().getFirst().id();

        LoomProject edited = ProjectEdits.setElytraWingPixel(
                project,
                layerId,
                ElytraWing.RIGHT,
                2,
                5,
                0xFF9B4DFF,
                false
        );

        LoomLayer layer = edited.elytra().layers().getFirst();
        int right = ElytraWing.RIGHT.atlasY(5, 1)
                * edited.elytra().width()
                + ElytraWing.RIGHT.atlasX(2, 1);
        int leftX = ElytraWing.LEFT.mirroredLocalX(2, 1);
        int left = ElytraWing.LEFT.atlasY(5, 1)
                * edited.elytra().width()
                + ElytraWing.LEFT.atlasX(leftX, 1);

        assertEquals(0xFF9B4DFF, layer.pixelAt(right));
        assertEquals(0, layer.pixelAt(left));
    }

    @Test
    void linkedMirrorUsesScaledSemanticWidthAtFourX() {
        LoomProject project = ProjectResizer.resizeElytra(
                LoomProjectFactory.blank("Elytra 4x", 1L),
                CanvasResolution.ULTRA
        );
        UUID layerId = project.elytra().layers().getFirst().id();
        int scale = CanvasResolution.ULTRA.scale();

        LoomProject edited = ProjectEdits.setElytraWingPixel(
                project,
                layerId,
                ElytraWing.LEFT,
                3,
                7,
                0xFFFFFFFF,
                true
        );

        int mirrored = ElytraWing.RIGHT.mirroredLocalX(3, scale);
        LoomLayer layer = edited.elytra().layers().getFirst();

        int right = ElytraWing.RIGHT.atlasY(7, scale)
                * edited.elytra().width()
                + ElytraWing.RIGHT.atlasX(mirrored, scale);

        assertEquals(ElytraWing.RIGHT.width(scale) - 1 - 3, mirrored);
        assertEquals(0xFFFFFFFF, layer.pixelAt(right));
    }

    @Test
    void lockedElytraPaintLayerRejectsWingEditing() {
        LoomProject project = LoomProjectFactory.blank("Locked Elytra", 1L);
        UUID layerId = project.elytra().layers().getFirst().id();
        LoomProject locked = ProjectEdits.setElytraLayerLocked(
                project,
                layerId,
                true
        );

        assertThrows(
                IllegalStateException.class,
                () -> ProjectEdits.paintElytraWingBrush(
                        locked,
                        layerId,
                        ElytraWing.LEFT,
                        4,
                        8,
                        3,
                        0xFFFFFFFF,
                        true
                )
        );
    }

    @Test
    void brushPaintsBothLinkedWings() {
        LoomProject project = LoomProjectFactory.blank("Brush Elytra", 1L);
        UUID layerId = project.elytra().layers().getFirst().id();

        LoomProject edited = ProjectEdits.paintElytraWingBrush(
                project,
                layerId,
                ElytraWing.LEFT,
                5,
                10,
                3,
                0xFFFFCC22,
                true
        );

        LoomLayer layer = edited.elytra().layers().getFirst();
        long painted = java.util.Arrays.stream(layer.pixels())
                .filter(pixel -> pixel == 0xFFFFCC22)
                .count();

        assertTrue(painted >= 2);
    }
    @Test
    void capeConversionCreatesNewEditableLayerWithoutChangingExistingElytra() {
        LoomProject project = LoomProjectFactory.blank("Convert", 1L);
        UUID capeLayerId = project.cape().layers().getFirst().id();

        LoomProject painted = ProjectEdits.setCapeRegionPixel(
                project,
                capeLayerId,
                CapeUvRegion.OUTSIDE,
                2,
                3,
                0xFF22D7E8
        );

        int beforeCount = painted.elytra().layers().size();
        LoomLayer existing = painted.elytra().layers().getFirst();

        LoomProject converted =
                ProjectEdits.addCapeConversionElytraLayer(painted);

        assertEquals(
                beforeCount + 1,
                converted.elytra().layers().size()
        );
        assertEquals(
                existing,
                converted.elytra().layers().getFirst()
        );
        assertEquals(
                LayerKind.PAINT,
                converted.elytra().layers().getLast().kind()
        );
        assertTrue(
                converted.elytra().layers().getLast().name()
                        .startsWith("Cape Conversion")
        );
    }

    @Test
    void capeConversionMirrorsVisibleCompositeIntoBothWings() {
        LoomProject project = LoomProjectFactory.blank("Convert Mirror", 1L);
        UUID capeLayerId = project.cape().layers().getFirst().id();

        LoomProject painted = ProjectEdits.setCapeRegionPixel(
                project,
                capeLayerId,
                CapeUvRegion.OUTSIDE,
                0,
                0,
                0xFFFF3366
        );

        LoomProject converted =
                ProjectEdits.addCapeConversionElytraLayer(painted);
        LoomLayer layer = converted.elytra().layers().getLast();

        int left = ElytraWing.LEFT.atlasY(0, 1)
                * converted.elytra().width()
                + ElytraWing.LEFT.atlasX(0, 1);
        int mirroredX = ElytraWing.RIGHT.mirroredLocalX(0, 1);
        int right = ElytraWing.RIGHT.atlasY(0, 1)
                * converted.elytra().width()
                + ElytraWing.RIGHT.atlasX(mirroredX, 1);

        assertEquals(0xFFFF3366, layer.pixelAt(left));
        assertEquals(0xFFFF3366, layer.pixelAt(right));
    }

    @Test
    void capeConversionUsesVisibleLayerComposite() {
        LoomProject project = LoomProjectFactory.blank("Composite", 1L);
        UUID baseId = project.cape().layers().getFirst().id();

        LoomProject base = ProjectEdits.setCapeRegionPixel(
                project,
                baseId,
                CapeUvRegion.OUTSIDE,
                0,
                0,
                0xFFFF0000
        );
        LoomProject withTop = ProjectEdits.addCapeLayer(base, "Top");
        UUID topId = withTop.cape().layers().getLast().id();
        LoomProject paintedTop = ProjectEdits.setCapeRegionPixel(
                withTop,
                topId,
                CapeUvRegion.OUTSIDE,
                0,
                0,
                0xFF0000FF
        );

        LoomProject hiddenTop = ProjectEdits.setCapeLayerVisible(
                paintedTop,
                topId,
                false
        );
        LoomProject converted =
                ProjectEdits.addCapeConversionElytraLayer(hiddenTop);

        LoomLayer layer = converted.elytra().layers().getLast();
        int left = ElytraWing.LEFT.atlasY(0, 1)
                * converted.elytra().width()
                + ElytraWing.LEFT.atlasX(0, 1);

        assertEquals(0xFFFF0000, layer.pixelAt(left));
    }

    @Test
    void capeConversionHandlesDifferentCapeAndElytraResolutions() {
        LoomProject project = LoomProjectFactory.blank("Mixed Scale", 1L);
        UUID capeLayerId = project.cape().layers().getFirst().id();
        LoomProject painted = project;

        for (int y = 0; y < CapeUvRegion.OUTSIDE.height(); y++) {
            for (int x = 0; x < CapeUvRegion.OUTSIDE.width(); x++) {
                painted = ProjectEdits.setCapeRegionPixel(
                        painted,
                        capeLayerId,
                        CapeUvRegion.OUTSIDE,
                        x,
                        y,
                        0xFF44CC88
                );
            }
        }

        LoomProject highElytra = ProjectResizer.resizeElytra(
                painted,
                CanvasResolution.ULTRA
        );
        LoomProject converted =
                ProjectEdits.addCapeConversionElytraLayer(highElytra);

        LoomLayer layer = converted.elytra().layers().getLast();
        int scale = CanvasResolution.ULTRA.scale();
        int left = ElytraWing.LEFT.atlasY(
                ElytraWing.LEFT.height(scale) - 1,
                scale
        ) * converted.elytra().width()
                + ElytraWing.LEFT.atlasX(
                        ElytraWing.LEFT.width(scale) - 1,
                        scale
                );

        assertEquals(0xFF44CC88, layer.pixelAt(left));
    }

    @Test
    void elytraLayerPropertiesAndOrderingAreEditable() {
        LoomProject project = LoomProjectFactory.blank("Layer Props", 1L);
        LoomProject withSecond = ProjectEdits.addElytraLayer(
                project,
                "Detail"
        );
        UUID firstId = withSecond.elytra().layers().getFirst().id();
        UUID detailId = withSecond.elytra().layers().getLast().id();

        LoomProject edited = ProjectEdits.renameElytraLayer(
                withSecond,
                detailId,
                "Glow Detail"
        );
        edited = ProjectEdits.setElytraLayerOpacity(
                edited,
                detailId,
                0.4F
        );
        edited = ProjectEdits.setElytraLayerBlendMode(
                edited,
                detailId,
                BlendMode.SCREEN
        );
        edited = ProjectEdits.setElytraLayerVisible(
                edited,
                detailId,
                false
        );
        edited = ProjectEdits.moveElytraLayer(
                edited,
                detailId,
                -1
        );

        LoomLayer detail = edited.elytra().layers().getFirst();
        assertEquals(detailId, detail.id());
        assertEquals("Glow Detail", detail.name());
        assertEquals(0.4F, detail.opacity());
        assertEquals(BlendMode.SCREEN, detail.blendMode());
        assertFalse(detail.visible());
        assertEquals(
                firstId,
                edited.elytra().layers().getLast().id()
        );
    }

}
