package dev.loomstudios.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoomProjectCodeTest {
    @Test
    void portableCodeRoundTripsSchemaV3Project() {
        LoomProject project = LoomProjectFactory.blank("Portable", 1L);
        var layerId = project.elytra().layers().getFirst().id();

        LoomAnimation animation = AnimationAuthoring.addTrack(
                project.animation(),
                layerId,
                AnimationChannel.ELYTRA,
                AnimationEffectType.SPARKLE
        );
        project = project.withAnimation(animation);

        String code = LoomProjectCode.encodePortable(project);
        LoomProject decoded = LoomProjectCode.decodePortable(code);

        assertTrue(code.startsWith("LSP1:"));
        assertEquals(project, decoded);
    }

    @Test
    void designIdIsStableReadableAndShort() {
        LoomProject project = LoomProjectFactory.blank("ID", 1L);

        String first = LoomProjectCode.designId(project);
        String second = LoomProjectCode.designId(project);

        assertEquals(first, second);
        assertTrue(first.matches(
                "LS-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}"
                        + "-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}"
                        + "-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}"
        ));
    }

    @Test
    void importedProjectGetsNewIdentityWithoutFlatteningData() {
        LoomProject source = LoomProjectFactory.blank("Shared Design", 5L);
        LoomProject imported = LoomProjectCode.forkImported(
                source,
                100L
        );

        assertNotEquals(source.projectId(), imported.projectId());
        assertEquals("Shared Design (Imported)", imported.name());
        assertEquals(source.cape(), imported.cape());
        assertEquals(source.elytra(), imported.elytra());
        assertEquals(source.runtime(), imported.runtime());
        assertEquals(source.animation(), imported.animation());
        assertEquals(100L, imported.metadata().createdAtEpochMillis());
    }

    @Test
    void wrongPrefixAndMalformedPayloadAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> LoomProjectCode.decodePortable("LS-ABCD-EFGH-IJKL")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> LoomProjectCode.decodePortable("LSP1:not_base64%%%")
        );
    }

    @Test
    void portableCodeDetectionIsWhitespaceTolerant() {
        assertTrue(LoomProjectCode.looksPortable("  LSP1:abc  "));
        assertFalse(LoomProjectCode.looksPortable("LS-ABCD-EFGH-JKLM"));
        assertFalse(LoomProjectCode.looksPortable(null));
    }
}
