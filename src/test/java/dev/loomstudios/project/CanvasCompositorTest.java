package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CanvasCompositorTest {
    @Test
    void invisibleLayersDoNotAffectComposite() {
        LoomCanvas canvas = new LoomCanvas(
                1,
                1,
                List.of(
                        LoomLayer.paint(
                                UUID.randomUUID(),
                                "Base",
                                true,
                                1.0F,
                                BlendMode.NORMAL,
                                false,
                                false,
                                new int[]{0xFFFF0000}
                        ),
                        LoomLayer.paint(
                                UUID.randomUUID(),
                                "Hidden",
                                false,
                                1.0F,
                                BlendMode.NORMAL,
                                false,
                                false,
                                new int[]{0xFF0000FF}
                        )
                )
        );

        assertArrayEquals(
                new int[]{0xFFFF0000},
                CanvasCompositor.compile(canvas)
        );
    }

    @Test
    void opacityAndNormalAlphaCompositionAreDeterministic() {
        LoomCanvas canvas = new LoomCanvas(
                1,
                1,
                List.of(
                        LoomLayer.paint(
                                UUID.randomUUID(),
                                "Base",
                                true,
                                1.0F,
                                BlendMode.NORMAL,
                                false,
                                false,
                                new int[]{0xFF000000}
                        ),
                        LoomLayer.paint(
                                UUID.randomUUID(),
                                "Top",
                                true,
                                0.5F,
                                BlendMode.NORMAL,
                                false,
                                false,
                                new int[]{0xFFFFFFFF}
                        )
                )
        );

        int first = CanvasCompositor.compile(canvas)[0];
        int second = CanvasCompositor.compile(canvas)[0];

        assertEquals(first, second);
        assertEquals(0xFF808080, first);
    }

    @Test
    void emissiveOnlySkipsNonEmissiveLayers() {
        LoomCanvas canvas = new LoomCanvas(
                1,
                1,
                List.of(
                        LoomLayer.paint(
                                UUID.randomUUID(),
                                "Base",
                                true,
                                1.0F,
                                BlendMode.NORMAL,
                                false,
                                false,
                                new int[]{0xFFFF0000}
                        ),
                        LoomLayer.paint(
                                UUID.randomUUID(),
                                "Glow",
                                true,
                                1.0F,
                                BlendMode.NORMAL,
                                true,
                                false,
                                new int[]{0xFF00FFFF}
                        )
                )
        );

        assertArrayEquals(
                new int[]{0xFF00FFFF},
                CanvasCompositor.compile(
                        canvas,
                        true,
                        java.util.function.IntUnaryOperator.identity()
                )
        );
    }
}
