package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;

import dev.loomstudios.image.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class Animation21Test {
  @TempDir Path directory;

  private LoomProject project() {
    var p = LoomProjectFactory.blank("Lanes", 1);
    return p.withAnimation(
        AnimationAuthoring.addTrack(
            p.animation(),
            p.cape().layers().getFirst().id(),
            AnimationChannel.CAPE,
            AnimationEffectType.SCROLL));
  }

  @Test
  void independentPropertiesEvaluateAndRoundTrip() {
    var p = project();
    var t =
        p.animation()
            .tracks()
            .getFirst()
            .withLanes(
                List.of(
                    new ParameterLane(
                        AnimationParameter.DIRECTION_X,
                        List.of(new AnimationKeyframe(0, 0), new AnimationKeyframe(80, 1))),
                    new ParameterLane(
                        AnimationParameter.DISTANCE,
                        List.of(new AnimationKeyframe(0, 0), new AnimationKeyframe(80, 1)))));
    var v = (EffectParameters.Scroll) AnimationEvaluator.parametersAt(t, p.animation(), 40);
    assertEquals(0, v.directionX(), .001);
    assertEquals(2, v.distance(), .001);
    p = p.withAnimation(p.animation().withTracks(List.of(t)));
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    assertEquals(p, LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(p)));
    assertArrayEquals(p.encode(), LoomProjectCodec.decode(p.encode()).encode());
  }

  @Test
  void curveUsesXInversionAndKeepsHandlesOnMoveDurationAndSave() {
    var curve = new KeyframeCurve(.15f, 0, .85f, 1);
    assertEquals(.5, curve.apply(.5f), .0001);
    var p = project();
    var t =
        p.animation()
            .tracks()
            .getFirst()
            .withKeyframes(
                List.of(
                    new AnimationKeyframe(0, 0, AnimationEasing.CUSTOM, curve),
                    new AnimationKeyframe(80, 1)));
    t = AnimationAuthoring.moveKeyframe(t, 0, 2);
    assertEquals(curve, t.keyframes().getFirst().curve());
    var a = AnimationAuthoring.changeDuration(p.animation().withTracks(List.of(t)), 40);
    assertEquals(curve, a.tracks().getFirst().keyframes().getFirst().curve());
    p = p.withAnimation(a);
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    assertThrows(IllegalArgumentException.class, () -> new KeyframeCurve(.9f, 0, .1f, 1));
  }

  @Test
  void clipboardMultipleLanesKeepsRelativeSpacingAndRejectsOverflowAtomically() {
    var p = project();
    var t =
        AnimationKeyEditing.addLane(
            p.animation().tracks().getFirst(), AnimationParameter.DIRECTION_Y, 80);
    var a = p.animation().withTracks(List.of(t));
    var selection =
        Set.of(
            new AnimationKeyEditing.Address(t.id(), null, 0),
            new AnimationKeyEditing.Address(t.id(), AnimationParameter.DIRECTION_Y, 0));
    var copied = AnimationKeyEditing.copy(a, selection);
    var pasted = AnimationKeyEditing.paste(a, copied, 20);
    assertEquals(3, pasted.tracks().getFirst().keyframes().size());
    assertEquals(3, pasted.tracks().getFirst().lanes().getFirst().keys().size());
    var all =
        Set.of(
            new AnimationKeyEditing.Address(t.id(), null, 0),
            new AnimationKeyEditing.Address(t.id(), null, 80));
    assertThrows(
        IllegalArgumentException.class,
        () -> AnimationKeyEditing.paste(a, AnimationKeyEditing.copy(a, all), 20));
    assertEquals(2, t.keyframes().size());
  }

  @Test
  void durationClampsLaneKeysAndEffectChangeRemovesInvalidLanes() {
    var p = project();
    var t =
        AnimationKeyEditing.addLane(
            p.animation().tracks().getFirst(), AnimationParameter.DISTANCE, 80);
    var a = AnimationAuthoring.changeDuration(p.animation().withTracks(List.of(t)), 20);
    assertEquals(20, a.tracks().getFirst().lanes().getFirst().keys().getLast().tick());
    assertTrue(t.withEffect(AnimationEffectType.PULSE).lanes().isEmpty());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            t.withLanes(
                List.of(
                    new ParameterLane(
                        AnimationParameter.FALLOFF, List.of(new AnimationKeyframe(0, 0))))));
  }

  @Test
  void framesDuplicateMoveDeletePaintTimingAndEditableFlagPersist() {
    var red = new PixelImage(1, 1, new int[] {0xFFFF0000});
    var blue = new PixelImage(1, 1, new int[] {0xFF0000FF});
    var d =
        ImageLayerData.placed(red, 64, 32, NormalizedRect.fullCanvas(), ImagePlacementMode.STRETCH)
            .withFrames(List.of(red, blue), List.of(2, 5))
            .withEditableAnimation(true);
    d = EditableFrames.duplicate(d, 0);
    assertEquals(3, d.frames().size());
    d = EditableFrames.move(d, 2, 0);
    assertEquals(blue, d.frames().getFirst());
    d = EditableFrames.delete(d, 1);
    d = EditableFrames.duration(d, 0, 9);
    d = EditableFrames.replace(d, 1, new PixelImage(1, 1, new int[] {0}));
    var p = ProjectEdits.addCapeImageLayer(LoomProjectFactory.blank("Frames", 1), "Frames", d);
    assertEquals(p, LoomProjectCodec.decode(p.encode()));
    assertTrue(
        LoomProjectCodec.decode(p.encode())
            .cape()
            .layers()
            .getLast()
            .imageData()
            .editableAnimation());
    assertEquals(9, d.frameTicks().getFirst());
    assertEquals(blue, d.frameAt(8));
    assertEquals(0, d.frameAt(9).pixelAt(0, 0));
  }

  @Test
  void conversionBakesOnlySelectedFaceWithoutTouchingOppositeWing() {
    var c = LoomProjectFactory.blank("Wings", 1).elytra();
    var red = new PixelImage(2, 2, new int[] {-65536, -65536, -65536, -65536});
    var d =
        ImageLayerData.placed(
                red,
                64,
                32,
                new NormalizedRect(38 / 64.0, 2 / 32.0, 10 / 64.0, 20 / 32.0),
                ImagePlacementMode.STRETCH)
            .withFrames(List.of(red, red), List.of(2, 2));
    var l = LoomLayer.image(UUID.randomUUID(), "GIF", true, 1, BlendMode.NORMAL, false, false, d);
    int[] map = new int[200];
    for (int y = 0; y < 20; y++)
      for (int x = 0; x < 10; x++) map[y * 10 + x] = (y + 2) * 64 + 38 + x;
    var converted = EditableFrames.convert(l, 64, 32, map, 10, 20, d.clip());
    var pixels = LayerRasterizer.rasterize(l.withImageData(converted), 64, 32);
    assertEquals(0, pixels[2 * 64 + 14]);
    assertEquals(-65536, pixels[2 * 64 + 38]);
    assertEquals(10, converted.source().width());
    assertTrue(converted.editableAnimation());
  }

  @Test
  void onionNeverChangesSourceAndTransparentPixelsShowColoredGhosts() {
    var empty = new PixelImage(1, 1, new int[] {0});
    var red = new PixelImage(1, 1, new int[] {-65536});
    var d =
        ImageLayerData.placed(
                empty, 64, 32, NormalizedRect.fullCanvas(), ImagePlacementMode.STRETCH)
            .withFrames(List.of(empty, red), List.of(2, 2));
    var ghost = EditableFrames.onion(d, 0, 3, .5f);
    assertTrue((ghost.pixelAt(0, 0) >>> 24) > 0);
    assertEquals(0, d.frames().getFirst().pixelAt(0, 0));
    assertEquals(empty, EditableFrames.onion(d, 0, 0, .5f));
  }

  @Test
  void editorAssetsStayOutsideProjectHashAndPersistFlags() {
    var store = new EditorAssetStore(directory);
    var p = project();
    String hash = p.hash();
    var image = new PixelImage(2, 1, new int[] {-1, 0});
    var ref =
        new ReferenceImage(
            UUID.randomUUID(),
            "Guide",
            AnimationChannel.CAPE,
            ImageLayerData.placed(
                image, 64, 32, NormalizedRect.fullCanvas(), ImagePlacementMode.FIT),
            .4f,
            true,
            true,
            false);
    assertDoesNotThrow(() -> store.references(p.projectId(), List.of(ref)));
    assertEquals(List.of(ref), assertDoesNotThrow(() -> store.references(p.projectId())));
    assertEquals(hash, p.hash());
    assertThrows(IllegalStateException.class, () -> ref.withImage(ref.image()));
    assertFalse(ref.above());
  }

  @Test
  void customSelectionStampTransparencyTransformsFavoritesAndPatternPackPersist() {
    var source = new PixelPatch(3, 2, new int[] {0xFFAA0000, 0, 0, 0, 0xFF00AA00, 0});
    var bits = new BitSet();
    bits.set(0);
    bits.set(4);
    var patch = CustomStamp.selection(source, bits);
    assertEquals(2, patch.width());
    var stamp = new CustomStamp(UUID.randomUUID(), "Custom", patch, true);
    var result = stamp.apply(new PixelPatch(5, 5, new int[25]), 2, 2, 2, 1, true, false, 0, null);
    assertTrue(Arrays.stream(result.data()).anyMatch(v -> v != 0));
    var store = new EditorAssetStore(directory);
    assertDoesNotThrow(() -> store.stamps(List.of(stamp)));
    assertEquals(stamp, assertDoesNotThrow(() -> store.stamps()).getFirst());
    assertEquals(6, BuiltinStampPacks.all().size());
    assertThrows(IllegalArgumentException.class, () -> CustomStamp.selection(source, new BitSet()));
  }

  @Test
  void malformedNestedLengthLaneKeysAndSidecarsAreRejected() throws Exception {
    byte[] bytes = project().encode();
    java.nio.ByteBuffer.wrap(bytes).putInt(8, Integer.MAX_VALUE);
    assertThrows(IllegalArgumentException.class, () -> LoomProjectCodec.decode(bytes));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ParameterLane(AnimationParameter.WIDTH, List.of(new AnimationKeyframe(0, -1))));
    Files.write(directory.resolve("stamps.bin"), new byte[] {0, 1, 2});
    assertThrows(java.io.IOException.class, () -> new EditorAssetStore(directory).stamps());
  }
}
