package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;

import dev.loomstudios.image.*;

import org.junit.jupiter.api.Test;

import java.util.*;

class AdvancedAuthoringTest {
    private LoomLayer paint(int... pixels) {
        return new LoomLayer(UUID.randomUUID(), "Paint", true, 1, BlendMode.NORMAL, false, pixels);
    }

    @Test
    void alphaLockRecolorsWithoutChangingAlpha() {
        var l = paint(0x80FF0000, 0);
        var edited = l.withAlphaLocked(true).withPixels(new int[] {0xFF0000FF, 0xFF22CC55});
        assertEquals(0x800000FF, edited.pixelAt(0));
        assertEquals(0, edited.pixelAt(1) >>> 24);
        assertEquals(0x80FF0000, l.pixelAt(0));
    }

    @Test
    void masksAndClippingComposeAgainstImmediatelyBelow() {
        var bottom = paint(0x80FF0000, 0);
        var middle =
                paint(0xFF0000FF, 0xFF0000FF)
                        .withClipToBelow(true)
                        .withMask(new byte[] {(byte) 128, (byte) 255});
        var c = new LoomCanvas(2, 1, List.of(bottom, middle));
        var pixels = TextureCompositor.compile(c, 0, false, false);
        assertEquals(0, pixels[1]);
        assertTrue((pixels[0] >>> 24) > 128);
        assertEquals(
                0,
                TextureCompositor.compile(new LoomCanvas(2, 1, List.of(middle)), 0, false, false)[
                        0]);
    }

    @Test
    void glowClippingUsesNonEmissiveBelow() {
        var bottom = paint(0xFFFFFFFF, 0);
        var top = paint(0xFFFF0000, 0xFFFF0000).withEmissive(true).withClipToBelow(true);
        int[] out =
                TextureCompositor.compile(
                        new LoomCanvas(2, 1, List.of(bottom, top)), 0, false, true);
        assertEquals(0xFFFF0000, out[0]);
        assertEquals(0, out[1]);
    }

    @Test
    void maskArraysAreImmutableAndSized() {
        byte[] mask = {(byte) 255, 0};
        var l = paint(1, 2).withMask(mask);
        mask[0] = 0;
        assertEquals(255, l.maskAt(0));
        l.mask()[0] = 0;
        assertEquals(255, l.maskAt(0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoomCanvas(2, 1, List.of(l.withMask(new byte[1]))));
    }

    @Test
    void wandKeepsDisconnectedMatchesSeparate() {
        var p =
                new PixelPatch(
                        3, 2, new int[] {0xFFFF0000, 0, 0xFFFF0000, 0xFFFF0000, 0, 0xFFFF0000});
        var local = ColorSelection.select(p, 0, 0, 0, true);
        var global = ColorSelection.select(p, 0, 0, 0, false);
        assertEquals(2, local.cardinality());
        assertEquals(4, global.cardinality());
        var replaced = ColorSelection.replace(p, local, 0xFF0000FF, true);
        assertEquals(0xFF0000FF, replaced.data()[0]);
        assertEquals(0xFFFF0000, replaced.data()[2]);
    }

    @Test
    void stampsRespectExactSelection() {
        var p = new PixelPatch(5, 5, new int[25]);
        var mask = new BitSet();
        mask.set(12);
        for (var stamp : BrushStamp.values()) {
            var out = stamp.apply(p, 2, 2, 5, 0xFFFFFFFF, mask);
            assertEquals(1, Arrays.stream(out.data()).filter(v -> v != 0).count());
        }
    }

    @Test
    void seamStrokeWritesAdjacentFacesAndNeverInside() {
        var p = LoomProjectFactory.blank("Seam", 1);
        var l = p.cape().layers().getFirst();
        var net = CapeSeamEdits.read(p.cape(), l);
        var stamped = BrushStamp.SQUARE.apply(net, 1, 8, 3, 0xFF22CCEE, null);
        var c = CapeSeamEdits.write(p.cape(), l, stamped);
        assertEquals(0xFF22CCEE, c.layers().getFirst().pixelAt(8 * 64));
        assertEquals(0xFF22CCEE, c.layers().getFirst().pixelAt(8 * 64 + 1));
        assertEquals(0, c.layers().getFirst().pixelAt(8 * 64 + 12));
    }

    @Test
    void allWingSurfacesAreDistinctAndLinkedCopiesOnlyTheirFace() {
        var p = LoomProjectFactory.blank("Faces", 1);
        UUID id = p.elytra().layers().getFirst().id();
        var used = new HashSet<Integer>();
        for (var face : ElytraSurface.values())
            for (var wing : ElytraWing.values())
                for (int y = 0; y < face.height(1); y++)
                    for (int x = 0; x < face.width(1); x++)
                        assertTrue(
                                used.add(face.atlasY(y, 1) * 64 + face.atlasX(wing, x, 1)),
                                "Overlapping faces");
        p =
                SurfaceEdits.wing(
                        p,
                        id,
                        ElytraWing.LEFT,
                        ElytraSurface.INSIDE,
                        true,
                        v -> BrushStamp.SQUARE.apply(v, 0, 0, 1, 0xFFAA33CC, null));
        var pixels = p.elytra().layers().getFirst();
        assertEquals(
                0xFFAA33CC,
                pixels.pixelAt(
                        ElytraSurface.INSIDE.atlasY(0, 1) * 64
                                + ElytraSurface.INSIDE.atlasX(ElytraWing.RIGHT, 9, 1)));
        assertEquals(
                0,
                pixels.pixelAt(ElytraWing.LEFT.atlasY(0, 1) * 64 + ElytraWing.LEFT.atlasX(0, 1)));
    }

    @Test
    void easingAndKeyMovesPreserveAuthoredParameters() {
        var p = LoomProjectFactory.blank("Ease", 1);
        var t =
                new AnimationTrack(
                        UUID.randomUUID(),
                        p.cape().layers().getFirst().id(),
                        AnimationChannel.CAPE,
                        AnimationEffectType.SCROLL,
                        true,
                        1,
                        false,
                        List.of(
                                new AnimationKeyframe(0, 0, AnimationEasing.STEP),
                                new AnimationKeyframe(20, 1)),
                        new EffectParameters.Scroll(0, 1, 2));
        assertEquals(0, AnimationEvaluator.valueAt(t, p.animation(), 10));
        var moved = AnimationAuthoring.moveKeyframe(t, 0, 2);
        assertEquals(AnimationEasing.STEP, moved.keyframes().getFirst().easing());
        assertEquals(t.parameters(), moved.parameters());
        assertEquals(.5f, AnimationEasing.SMOOTH.apply(.5f));
        assertEquals(.25f, AnimationEasing.EASE_IN.apply(.5f));
    }

    @Test
    void gifTimingChangesRenderedPixels() {
        var p = LoomProjectFactory.blank("GIF", 1);
        var red = new PixelImage(1, 1, new int[] {0xFFFF0000});
        var blue = new PixelImage(1, 1, new int[] {0xFF0000FF});
        var data =
                ImageLayerData.placed(
                                red,
                                64,
                                32,
                                new NormalizedRect(0, 0, 1, 1),
                                ImagePlacementMode.STRETCH)
                        .withFrames(List.of(red, blue), List.of(2, 3));
        p = ProjectEdits.addCapeImageLayer(p, "GIF", data);
        assertEquals(
                0xFFFF0000,
                TextureCompositor.compileAnimated(p, AnimationChannel.CAPE, 0, 0, false)[0]);
        assertEquals(
                0xFF0000FF,
                TextureCompositor.compileAnimated(p, AnimationChannel.CAPE, 2, 0, false)[0]);
        assertEquals(
                0xFFFF0000,
                TextureCompositor.compileAnimated(p, AnimationChannel.CAPE, 5, 0, false)[0]);
    }
}
