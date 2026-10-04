package dev.loomstudios.project;

import dev.loomstudios.image.*;
import java.util.*;

/** Local guide image, deliberately outside LoomProject/export/network data. */
public record ReferenceImage(
    UUID id,
    String name,
    AnimationChannel channel,
    ImageLayerData image,
    float opacity,
    boolean visible,
    boolean locked,
    boolean above) {
  public ReferenceImage {
    Objects.requireNonNull(id);
    Objects.requireNonNull(channel);
    Objects.requireNonNull(image);
    if (name == null
        || name.isBlank()
        || name.length() > 96
        || !Float.isFinite(opacity)
        || opacity < 0
        || opacity > 1
        || !image.frames().isEmpty()) throw new IllegalArgumentException("Invalid reference image");
  }

  public ReferenceImage withState(float opacity, boolean visible, boolean locked, boolean above) {
    return new ReferenceImage(id, name, channel, image, opacity, visible, locked, above);
  }

  public ReferenceImage withImage(ImageLayerData value) {
    if (locked) throw new IllegalStateException("Unlock the reference first");
    return new ReferenceImage(id, name, channel, value, opacity, visible, locked, above);
  }

  public int[] raster(int width, int height) {
    var layer = LoomLayer.image(id, name, true, opacity, BlendMode.NORMAL, false, true, image);
    return LayerRasterizer.rasterize(layer, width, height);
  }
}
