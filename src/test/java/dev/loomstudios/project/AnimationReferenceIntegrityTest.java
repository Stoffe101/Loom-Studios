package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnimationReferenceIntegrityTest {
    @Test
    void deletingElytraLayerPrunesItsAnimationTracks() {
        LoomProject project = LoomProjectFactory.blank("Refs", 1L);
        UUID first = project.elytra().layers().getFirst().id();

        LoomProject withSecond = ProjectEdits.addElytraLayer(
                project,
                "Second"
        );
        UUID second = withSecond.elytra().layers().getLast().id();

        LoomAnimation animation = AnimationAuthoring.addTrack(
                withSecond.animation(),
                second,
                AnimationChannel.ELYTRA,
                AnimationEffectType.PULSE
        );
        LoomProject animated = withSecond.withAnimation(animation);

        LoomProject deleted = ProjectEdits.removeElytraLayer(
                animated,
                second
        );

        assertEquals(1, deleted.elytra().layers().size());
        assertEquals(first, deleted.elytra().layers().getFirst().id());
        assertTrue(deleted.animation().tracks().isEmpty());
    }

    @Test
    void missingLayerReferenceIsRejectedByProjectValidation() {
        LoomProject project = LoomProjectFactory.blank("Broken", 1L);

        AnimationTrack orphan = new AnimationTrack(
                UUID.randomUUID(),
                UUID.randomUUID(),
                AnimationChannel.CAPE,
                AnimationEffectType.HUE_SHIFT,
                true,
                1.0F,
                true,
                List.of(
                        new AnimationKeyframe(0, 0.0F),
                        new AnimationKeyframe(80, 1.0F)
                )
        );

        LoomAnimation animation = new LoomAnimation(
                80,
                true,
                1.0F,
                List.of(orphan)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> project.withAnimation(animation)
        );
    }
}
