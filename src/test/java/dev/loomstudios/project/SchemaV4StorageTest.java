package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;

import dev.loomstudios.image.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.*;

class SchemaV4StorageTest {
    @TempDir Path directory;

    private LoomProject large(int capeCount, int wingCount) {
        var p = LoomProjectFactory.blank("Large", 1);
        p = ProjectResizer.resizeCape(p, CanvasResolution.MAXIMUM);
        p = ProjectResizer.resizeElytra(p, CanvasResolution.MAXIMUM);
        while (p.cape().layers().size() < capeCount) p = ProjectEdits.addCapeLayer(p, "Detail");
        while (p.elytra().layers().size() < wingCount) p = ProjectEdits.addElytraLayer(p, "Detail");
        return p;
    }

    @Test
    void eightTimesAndMoreThanSevenLayersRoundTripAndShare() throws Exception {
        var p = large(32, 16);
        UUID id = p.cape().layers().getLast().id();
        p = ProjectEdits.paintCapeRegionBrush(p, id, CapeUvRegion.OUTSIDE, 42, 88, 8, 0xFFCC2288);
        assertEquals(512, p.cape().width());
        assertEquals(512, p.elytra().width());
        assertEquals(48, p.cape().layers().size() + p.elytra().layers().size());
        byte[] bytes = p.encode();
        assertTrue(bytes.length < 100_000);
        assertEquals(p, LoomProjectCodec.decode(bytes));
        assertArrayEquals(bytes, LoomProjectCodec.decode(bytes).encode());
        var store = new ProjectFileStore(directory);
        assertEquals(p, store.load(store.save(p)));
        assertEquals(p, LoomProjectCode.decodePortable(LoomProjectCode.encodePortable(p)));
        assertEquals(p.hash(), LoomProjectCodec.sha256(bytes));
    }

    @Test
    void largeEditReusesUnchangedBlocksWithoutStaleArtworkOrResize() {
        var p = large(32, 16);
        byte[] baseline = p.encode();
        long count = LoomProjectV4Codec.compressionCount();
        assertArrayEquals(baseline, p.encode());
        assertEquals(count, LoomProjectV4Codec.compressionCount());
        var id = p.cape().layers().getLast().id();
        var edited =
                ProjectEdits.paintCapeRegionBrush(
                        p, id, CapeUvRegion.OUTSIDE, 42, 88, 1, 0xFF22CC99);
        byte[] changed = edited.encode();
        assertEquals(count + 1, LoomProjectV4Codec.compressionCount());
        assertFalse(Arrays.equals(baseline, changed));
        assertEquals(edited, LoomProjectCodec.decode(changed));
        assertTrue(LoomProjectV4Codec.encodedCacheBytes() <= 32L * 1024 * 1024);
        var source = new PixelImage(1, 1, new int[] {0xFFAA3399});
        var small =
                ProjectEdits.addCapeImageLayer(
                        LoomProjectFactory.blank("Resize cache", 1),
                        "Image",
                        ImageLayerData.placed(
                                source,
                                64,
                                32,
                                new NormalizedRect(0, 0, 1, 1),
                                ImagePlacementMode.STRETCH));
        small.encode();
        var resized = ProjectResizer.resizeCape(small, CanvasResolution.MAXIMUM);
        assertEquals(resized, LoomProjectCodec.decode(resized.encode()));
    }

    @Test
    void allResolutionStepsKeepCapeWingAndMaskPixels() {
        var p = LoomProjectFactory.blank("Steps", 1);
        var layer = p.cape().layers().getFirst();
        byte[] mask = new byte[2048];
        Arrays.fill(mask, (byte) 255);
        mask[65] = 0;
        p = p.withCape(p.cape().replaceLayer(layer.id(), layer.withMask(mask)));
        for (var resolution : CanvasResolution.values()) {
            p = ProjectResizer.resizeCape(p, resolution);
            p = ProjectResizer.resizeElytra(p, resolution);
            assertEquals(resolution, CanvasResolution.fromCanvas(p.cape()));
            assertEquals(resolution, CanvasResolution.fromCanvas(p.elytra()));
            assertEquals(
                    p.cape().width() * p.cape().height(),
                    p.cape().layers().getFirst().maskLength());
            assertEquals(p, LoomProjectCodec.decode(p.encode()));
        }
        assertEquals(
                List.of(1, 2, 4, 6, 8),
                Arrays.stream(CanvasResolution.values()).map(CanvasResolution::scale).toList());
    }

    @Test
    void forgedCompressedExpansionIsRejectedBeforeAllocation() throws Exception {
        byte[] bytes = LoomProjectFactory.blank("Bomb", 1).encode();
        var in = new DataInputStream(new ByteArrayInputStream(bytes));
        LoomProjectCodec.requireHeader(in, 4);
        LoomProjectCodec.readCommonProjectData(in);
        in.readInt();
        in.readInt();
        in.readInt();
        in.readBoolean();
        int offset = bytes.length - in.available();
        java.nio.ByteBuffer.wrap(bytes).putInt(offset, Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> LoomProjectCodec.decode(bytes));
        assertThrows(
                IllegalArgumentException.class,
                () -> LoomProjectCodec.decode(Arrays.copyOf(bytes, bytes.length - 5)));
    }

    @Test
    void incompressibleOverflowLeavesHistoryAndArtworkIntact() {
        var p = large(15, 1);
        var random = new Random(81);
        var layers = new ArrayList<LoomLayer>();
        for (var layer : p.cape().layers()) {
            int[] pixels = new int[p.cape().width() * p.cape().height()];
            for (int i = 0; i < pixels.length; i++) pixels[i] = random.nextInt();
            layers.add(layer.withPixels(pixels));
        }
        p = p.withCape(new LoomCanvas(512, 256, layers));
        assertDoesNotThrow(p::encode);
        var session = new ProjectSession(p, new ProjectFileStore(directory));
        var before = p;
        session.apply(
                v -> {
                    var list = new ArrayList<>(v.cape().layers());
                    list.add(list.getFirst().duplicate(UUID.randomUUID(), "Overflow"));
                    return v.withCape(new LoomCanvas(512, 256, list));
                });
        assertSame(before, session.project());
        assertFalse(session.canUndo());
        assertNotNull(session.editError());
    }

    @Test
    void cacheEvictsByBothMemoryAndEntryCount() {
        var cache = new BoundedCache<String, byte[]>(10, 2, v -> v.length);
        cache.put("a", new byte[4]);
        cache.put("b", new byte[4]);
        cache.get("a");
        cache.put("c", new byte[4]);
        assertNull(cache.get("b"));
        assertEquals(8, cache.retainedBytes());
        cache.put("d", new byte[9]);
        assertEquals(1, cache.size());
        assertNull(cache.get("a"));
        assertThrows(IllegalArgumentException.class, () -> cache.put("e", new byte[11]));
    }

    @Test
    void typedEffectsMasksEasingAndGifFramesPersist() {
        var p = LoomProjectFactory.blank("Typed", 1);
        var l = p.cape().layers().getFirst();
        byte[] mask = new byte[2048];
        Arrays.fill(mask, (byte) 128);
        p =
                p.withCape(
                        p.cape()
                                .replaceLayer(
                                        l.id(),
                                        l.withAlphaLocked(true)
                                                .withClipToBelow(true)
                                                .withMask(mask)));
        var image = new PixelImage(2, 1, new int[] {0xFFFF0000, 0xFF0000FF});
        var data =
                ImageLayerData.placed(
                                image,
                                64,
                                32,
                                new NormalizedRect(0, 0, 1, 1),
                                ImagePlacementMode.FIT)
                        .withFrames(List.of(image, image), List.of(2, 3))
                        .withProcessing(
                                ImageProcessingSettings.defaults()
                                        .withTint(0xFF55AAFF, .5f)
                                        .withBackground(
                                                new BackgroundRemoval(
                                                        true, 0xFFFFFFFF, 16, false, 0, 0)));
        p = ProjectEdits.addCapeImageLayer(p, "GIF", data);
        var tracks = new ArrayList<AnimationTrack>();
        for (var type : AnimationEffectType.values())
            tracks.add(
                    new AnimationTrack(
                            UUID.randomUUID(),
                            l.id(),
                            AnimationChannel.CAPE,
                            type,
                            true,
                            1,
                            true,
                            List.of(
                                    new AnimationKeyframe(0, 0, AnimationEasing.SMOOTH),
                                    new AnimationKeyframe(20, 1, AnimationEasing.STEP)),
                            EffectParameters.forAuthoring(type)));
        p = p.withAnimation(p.animation().withTracks(tracks));
        assertEquals(p, LoomProjectCodec.decode(p.encode()));
    }

    @Test
    void legacyMigrationPreservesTypedPayloadAndAnimation() {
        var p = LoomProjectFactory.blank("Old", 1);
        var data =
                ImageLayerData.placed(
                        new PixelImage(1, 1, new int[] {0xFF22CC99}),
                        64,
                        32,
                        new NormalizedRect(24 / 64.0, 2 / 32.0, 10 / 64.0, 20 / 32.0),
                        ImagePlacementMode.STRETCH);
        p = ProjectEdits.addElytraImageLayer(p, "Old image", data);
        UUID id = p.elytra().layers().getLast().id();
        p =
                p.withAnimation(
                        AnimationAuthoring.addTrack(
                                p.animation(),
                                id,
                                AnimationChannel.ELYTRA,
                                AnimationEffectType.MOVING_GRADIENT));
        p =
                p.withAnimation(
                        p.animation()
                                .withTracks(
                                        p.animation().tracks().stream()
                                                .map(
                                                        t ->
                                                                t.withParameters(
                                                                        EffectParameters.defaults(
                                                                                t.effect())))
                                                .toList()));
        byte[] old = LoomProjectCodec.encodeVersion3SnapshotForTest(p);
        var migrated = LoomProjectCodec.decode(old);
        assertEquals(4, migrated.schemaVersion());
        var typed = migrated.elytra().layers().getLast();
        assertEquals(data, typed.imageData());
        assertTrue(typed.legacyWingUv());
        int[] pixels = LayerRasterizer.rasterize(typed, 64, 32);
        assertEquals(0xFF22CC99, pixels[2 * 64 + 26]);
        assertEquals(0xFF22CC99, pixels[2 * 64 + 2]);
        assertEquals(p.animation(), migrated.animation());
        assertEquals(migrated, LoomProjectCodec.decode(migrated.encode()));
    }
}
