package dev.loomstudios.project;

import dev.loomstudios.image.ImagePlacementMode;
import dev.loomstudios.image.PixelImage;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ElytraTypedLayerWorkflowTest {
    @Test
    void editableImageLayerCanBeAddedAndUpdatedOnElytra() {
        LoomProject project = LoomProjectFactory.blank("Image Wings", 1L);
        PixelImage source = new PixelImage(
                2,
                2,
                new int[]{
                        0xFFFF0000, 0xFF00FF00,
                        0xFF0000FF, 0xFFFFFFFF
                }
        );

        ImageLayerData data = ImageLayerData.placed(
                source,
                project.elytra().width(),
                project.elytra().height(),
                ElytraWing.LEFT.normalizedRect(project.elytra()),
                ImagePlacementMode.FIT
        );

        LoomProject added = ProjectEdits.addElytraImageLayer(
                project,
                "Imported Left",
                data
        );
        LoomLayer layer = added.elytra().layers().getLast();

        assertEquals(LayerKind.IMAGE, layer.kind());

        ImageLayerData updated = data.withTransform(
                data.transform().withRotation(45.0)
        );
        LoomProject changed = ProjectEdits.setElytraImageData(
                added,
                layer.id(),
                updated
        );

        assertEquals(
                45.0,
                changed.elytra()
                        .layers()
                        .getLast()
                        .imageData()
                        .transform()
                        .rotationDegrees(),
                1.0E-9
        );
    }

    @Test
    void elytraLayerPropertiesRoundTripThroughProjectCodec() {
        LoomProject project = LoomProjectFactory.blank("Props", 1L);
        UUID layerId = project.elytra().layers().getFirst().id();

        LoomProject changed = ProjectEdits.renameElytraLayer(
                project,
                layerId,
                "Wing Base"
        );
        changed = ProjectEdits.setElytraLayerOpacity(
                changed,
                layerId,
                0.6F
        );
        changed = ProjectEdits.setElytraLayerBlendMode(
                changed,
                layerId,
                BlendMode.SCREEN
        );

        LoomProject decoded = LoomProjectCodec.decode(changed.encode());
        LoomLayer layer = decoded.elytra().layers().getFirst();

        assertEquals("Wing Base", layer.name());
        assertEquals(0.6F, layer.opacity(), 1.0E-6F);
        assertEquals(BlendMode.SCREEN, layer.blendMode());
    }
}
