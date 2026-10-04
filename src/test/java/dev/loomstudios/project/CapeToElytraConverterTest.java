package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CapeToElytraConverterTest {
    @Test
    void conversionAspectFitsCapeOutsideAndMirrorsRightWing() {
        LoomProject project = LoomProjectFactory.blank("Convert", 1L);
        int[] source = new int[10 * 16];

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 10; x++) {
                source[y * 10 + x] =
                        0xFF000000 | (x << 16) | (y << 8);
            }
        }

        int[] output = CapeToElytraConverter.convertOutsideFace(
                source,
                10,
                16,
                project.elytra()
        );

        // 10x16 fits unchanged into a 10x20 wing with two transparent rows
        // above and below.
        int leftTop = ElytraWing.LEFT.atlasY(0, 1)
                * project.elytra().width()
                + ElytraWing.LEFT.atlasX(0, 1);
        assertEquals(0, output[leftTop]);

        int left = ElytraWing.LEFT.atlasY(2, 1)
                * project.elytra().width()
                + ElytraWing.LEFT.atlasX(0, 1);
        int right = ElytraWing.RIGHT.atlasY(2, 1)
                * project.elytra().width()
                + ElytraWing.RIGHT.atlasX(9, 1);

        assertEquals(source[0], output[left]);
        assertEquals(source[0], output[right]);
    }

    @Test
    void conversionPreservesAlphaAndScalesToFourXTarget() {
        LoomProject project = ProjectResizer.resizeElytra(
                LoomProjectFactory.blank("Convert 4x", 1L),
                CanvasResolution.ULTRA
        );
        int[] source = new int[]{0x4022D7E8};

        int[] output = CapeToElytraConverter.convertOutsideFace(
                source,
                1,
                1,
                project.elytra()
        );

        long visible = Arrays.stream(output)
                .filter(pixel -> ((pixel >>> 24) & 0xFF) != 0)
                .count();

        assertTrue(visible > 2);
        assertTrue(Arrays.stream(output)
                .anyMatch(pixel -> pixel == 0x4022D7E8));
    }

    @Test
    void invalidSourceDimensionsAreRejected() {
        LoomProject project = LoomProjectFactory.blank("Bad Convert", 1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> CapeToElytraConverter.convertOutsideFace(
                        new int[3],
                        2,
                        2,
                        project.elytra()
                )
        );
    }

    @Test
    void elytraWingNormalizedRectsTrackCanvasResolution() {
        LoomProject project = ProjectResizer.resizeElytra(
                LoomProjectFactory.blank("Rects", 1L),
                CanvasResolution.HIGH
        );

        NormalizedRect left =
                ElytraWing.LEFT.normalizedRect(project.elytra());
        NormalizedRect right =
                ElytraWing.RIGHT.normalizedRect(project.elytra());

        assertEquals(26.0 / 64.0, left.x(), 1.0E-9);
        assertEquals(2.0 / 64.0, right.x(), 1.0E-9);
        assertEquals(10.0 / 64.0, left.width(), 1.0E-9);
        assertEquals(20.0 / 32.0, left.height(), 1.0E-9);
    }
}
