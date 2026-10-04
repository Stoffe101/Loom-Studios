package dev.loomstudios.client.mixin;

import net.minecraft.client.model.geom.ModelPart;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ModelPart.class)
public interface ModelPartCubesAccessor {
    @Accessor("cubes")
    List<ModelPart.Cube> loomStudios$getCubes();

    @Mutable
    @Accessor("cubes")
    void loomStudios$setCubes(List<ModelPart.Cube> cubes);
}
