package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradientAuthoringTest {
    private static GradientLayerData gradient() {
        return GradientLayerData.defaultLinear(
                0xFF112233,
                0xFFCCDDEE,
                new NormalizedRect(0.25, 0.20, 0.40, 0.50)
        );
    }

    @Test
    void translateUsesLayerRelativeFivePercentSteps() {
        GradientLayerData original = gradient();
        LayerTransform before = original.transform();

        GradientLayerData moved = GradientAuthoring.translate(
                original,
                1,
                -1
        );

        assertEquals(
                before.centerX() + before.width() * 0.05,
                moved.transform().centerX(),
                1.0E-9
        );
        assertEquals(
                before.centerY() - before.height() * 0.05,
                moved.transform().centerY(),
                1.0E-9
        );
        assertEquals(before.width(), moved.transform().width());
        assertEquals(before.height(), moved.transform().height());
    }

    @Test
    void scalePreservesAspectAndReportsPercentAgainstClip() {
        GradientLayerData scaled = GradientAuthoring.scale(
                gradient(),
                1.25
        );

        assertEquals(0.50, scaled.transform().width(), 1.0E-9);
        assertEquals(0.625, scaled.transform().height(), 1.0E-9);
        assertEquals(125, GradientAuthoring.scalePercent(scaled));
    }

    @Test
    void mirrorTogglesAreIndependent() {
        GradientLayerData horizontal =
                GradientAuthoring.toggleMirrorHorizontal(gradient());
        assertTrue(horizontal.transform().mirrorHorizontal());
        assertFalse(horizontal.transform().mirrorVertical());

        GradientLayerData both =
                GradientAuthoring.toggleMirrorVertical(horizontal);
        assertTrue(both.transform().mirrorHorizontal());
        assertTrue(both.transform().mirrorVertical());

        GradientLayerData verticalOnly =
                GradientAuthoring.toggleMirrorHorizontal(both);
        assertFalse(verticalOnly.transform().mirrorHorizontal());
        assertTrue(verticalOnly.transform().mirrorVertical());
    }

    @Test
    void resetRestoresClipPlacementAndClearsRotationAndMirrors() {
        GradientLayerData edited = GradientAuthoring.scale(
                GradientAuthoring.translate(
                        GradientAuthoring.toggleMirrorHorizontal(
                                gradient().withTransform(
                                        gradient().transform()
                                                .withRotation(135.0)
                                )
                        ),
                        2,
                        -3
                ),
                1.5
        );

        GradientLayerData reset =
                GradientAuthoring.resetTransform(edited);
        NormalizedRect clip = reset.clip();

        assertEquals(clip.centerX(), reset.transform().centerX(), 1.0E-9);
        assertEquals(clip.centerY(), reset.transform().centerY(), 1.0E-9);
        assertEquals(clip.width(), reset.transform().width(), 1.0E-9);
        assertEquals(clip.height(), reset.transform().height(), 1.0E-9);
        assertEquals(0.0, reset.transform().rotationDegrees(), 1.0E-9);
        assertFalse(reset.transform().mirrorHorizontal());
        assertFalse(reset.transform().mirrorVertical());
        assertEquals(100, GradientAuthoring.scalePercent(reset));
    }

    @Test
    void invalidScaleFactorsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> GradientAuthoring.scale(gradient(), 0.0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> GradientAuthoring.scale(
                        gradient(),
                        Double.NaN
                )
        );
    }
}
