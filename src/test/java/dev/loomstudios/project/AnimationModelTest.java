package dev.loomstudios.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AnimationModelTest {
    @Test
    void evaluatorInterpolatesBetweenKeyframes() {
        AnimationTrack track = new AnimationTrack(
                UUID.randomUUID(),
                UUID.randomUUID(),
                AnimationChannel.CAPE,
                AnimationEffectType.PULSE,
                true,
                1.0F,
                false,
                List.of(
                        new AnimationKeyframe(0, 0.0F),
                        new AnimationKeyframe(20, 1.0F)
                )
        );
        LoomAnimation animation = new LoomAnimation(
                40,
                false,
                1.0F,
                List.of(track)
        );

        assertEquals(
                0.5F,
                AnimationEvaluator.valueAt(track, animation, 10),
                1.0E-6F
        );
    }

    @Test
    void animationAuthoringAddsAndEditsTrack() {
        LoomProject project = LoomProjectFactory.blank("Anim", 1L);
        UUID layerId = project.cape().layers().getFirst().id();

        LoomAnimation animation = AnimationAuthoring.addTrack(
                project.animation(),
                layerId,
                AnimationChannel.CAPE,
                AnimationEffectType.HUE_SHIFT
        );

        assertEquals(1, animation.tracks().size());
        AnimationTrack track = animation.tracks().getFirst();
        assertEquals(AnimationEffectType.HUE_SHIFT, track.effect());

        AnimationTrack changed =
                AnimationAuthoring.addOrReplaceKeyframe(
                        track,
                        10,
                        0.25F
                );

        assertTrue(changed.keyframes().stream()
                .anyMatch(frame ->
                        frame.tick() == 10
                                && Math.abs(
                                        frame.value() - 0.25F
                                ) < 1.0E-6F
                ));
    }

    @Test
    void schemaV2SnapshotMigratesToV3WithEmptyTimeline() {
        LoomProject current = LoomProjectFactory.blank("Legacy V2", 1L);
        byte[] v2 = LoomProjectCodec.encodeVersion2SnapshotForTest(
                current
        );

        LoomProject migrated = LoomProjectCodec.decode(v2);

        assertEquals(5, migrated.schemaVersion());
        assertTrue(migrated.animation().tracks().isEmpty());
        assertEquals(80, migrated.animation().durationTicks());
    }

    @Test
    void schemaV3RoundTripPreservesAnimationTracks() {
        LoomProject project = LoomProjectFactory.blank("Timeline", 1L);
        UUID layerId = project.elytra().layers().getFirst().id();

        LoomAnimation animation = AnimationAuthoring.addTrack(
                project.animation(),
                layerId,
                AnimationChannel.ELYTRA,
                AnimationEffectType.SPARKLE
        );

        AnimationTrack source = animation.tracks().getFirst()
                .withSpeed(1.5F)
                .withLoop(false);
        source = AnimationAuthoring.addOrReplaceKeyframe(
                source,
                12,
                0.75F
        );
        animation = AnimationAuthoring.replaceTrack(
                animation,
                source
        );

        LoomProject animated = project.withAnimation(animation);
        LoomProject decoded = LoomProjectCodec.decode(
                animated.encode()
        );

        assertEquals(animated, decoded);
        assertEquals(5, decoded.schemaVersion());
        assertEquals(
                AnimationEffectType.SPARKLE,
                decoded.animation().tracks().getFirst().effect()
        );
        assertEquals(
                AnimationChannel.ELYTRA,
                decoded.animation().tracks().getFirst().channel()
        );
    }

    @Test
    void durationChangeClampsCollidingKeyframesDeterministically() {
        AnimationTrack track = new AnimationTrack(
                UUID.randomUUID(),
                UUID.randomUUID(),
                AnimationChannel.CAPE,
                AnimationEffectType.PULSE,
                true,
                1.0F,
                true,
                List.of(
                        new AnimationKeyframe(0, 0.5F),
                        new AnimationKeyframe(40, 1.0F),
                        new AnimationKeyframe(80, 0.5F)
                )
        );
        LoomAnimation animation = new LoomAnimation(
                80,
                true,
                1.0F,
                List.of(track)
        );

        LoomAnimation shortened =
                AnimationAuthoring.changeDuration(animation, 30);

        assertEquals(30, shortened.durationTicks());
        assertEquals(
                List.of(0, 30),
                shortened.tracks()
                        .getFirst()
                        .keyframes()
                        .stream()
                        .map(AnimationKeyframe::tick)
                        .toList()
        );
    }

    @Test
    void malformedTrackOrderingIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AnimationTrack(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        AnimationChannel.ELYTRA,
                        AnimationEffectType.SCROLL,
                        true,
                        1.0F,
                        true,
                        List.of(
                                new AnimationKeyframe(20, 1.0F),
                                new AnimationKeyframe(10, 0.0F)
                        )
                )
        );
    }
}
