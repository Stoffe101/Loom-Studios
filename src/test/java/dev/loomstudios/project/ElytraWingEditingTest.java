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
}
