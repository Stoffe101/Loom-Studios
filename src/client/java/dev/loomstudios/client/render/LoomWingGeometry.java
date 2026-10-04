package dev.loomstudios.client.render;

import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;

/** Changes only the UV origin; attachment, mirrored mesh and deformation remain vanilla. */
public final class LoomWingGeometry {
    private LoomWingGeometry() {}

    public static ModelPart leftWing() {
        var mesh = new MeshDefinition();
        mesh.getRoot()
                .addOrReplaceChild(
                        "left_wing",
                        CubeListBuilder.create()
                                .texOffs(24, 0)
                                .addBox(-10, 0, 0, 10, 20, 2, new CubeDeformation(1)),
                        PartPose.offsetAndRotation(5, 0, 0, .2617994f, 0, -.2617994f));
        return LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("left_wing");
    }

    public static ModelPart rightWing() {
        var mesh = new MeshDefinition();
        mesh.getRoot()
                .addOrReplaceChild(
                        "right_wing",
                        CubeListBuilder.create()
                                .texOffs(0, 0)
                                .mirror()
                                .addBox(0, 0, 0, 10, 20, 2, new CubeDeformation(1)),
                        PartPose.offsetAndRotation(-5, 0, 0, .2617994f, 0, .2617994f));
        return LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("right_wing");
    }
}
