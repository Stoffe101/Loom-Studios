package dev.loomstudios.project;

import dev.loomstudios.image.ImageProcessingSettings;
import dev.loomstudios.image.PixelImage;
import java.util.Objects;
import java.util.UUID;

/**
 * Editable placement foundation for the proposed Asset Library.
 *
 * <p>Unlike a stamp brush, a placement creates a project-owned Image layer with a stable layer ID.
 * The built-in/custom source pixels are embedded in the project, so multiplayer and exports do not
 * depend on whether the receiving client has the same authoring library. The player can transform,
 * tint, fade, mask and animate that layer with existing project editing operations.
 *
 * <p>When the player explicitly chooses "Edit Pixels", the image can be converted to a Paint layer
 * in one undoable project edit while preserving the layer ID and its existing animation references.
 * This is an explicit conversion, not a promise that an Image layer is natively brush-editable.
 */
public final class EditableAssetPlacement {
  private EditableAssetPlacement() {}

  /** Place an independent asset on a semantic cape face. Coordinates are 0–1 within the face. */
  public static LoomProject cape(
      LoomProject project,
      CustomStamp asset,
      CapeUvRegion region,
      double localX,
      double localY,
      double relativeWidth,
      int tint,
      boolean recolor) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(region, "region");
    LoomCanvas canvas = project.cape();
    int scale = CanvasResolution.fromCanvas(canvas).scale();
    NormalizedRect face =
        new NormalizedRect(
            region.atlasX(0, scale) / (double) canvas.width(),
            region.atlasY(0, scale) / (double) canvas.height(),
            region.width(scale) / (double) canvas.width(),
            region.height(scale) / (double) canvas.height());
    return ProjectEdits.addCapeImageLayer(
        project, asset.name(), data(canvas, asset, face, localX, localY, relativeWidth, tint, recolor));
  }

  /** Place an independent asset on exactly one selected Elytra wing and surface. */
  public static LoomProject elytra(
      LoomProject project,
      CustomStamp asset,
      ElytraWing wing,
      ElytraSurface surface,
      double localX,
      double localY,
      double relativeWidth,
      int tint,
      boolean recolor) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(wing, "wing");
    Objects.requireNonNull(surface, "surface");
    LoomCanvas canvas = project.elytra();
    return ProjectEdits.addElytraImageLayer(
        project,
        asset.name(),
        data(canvas, asset, surface.normalizedRect(canvas, wing),
            localX, localY, relativeWidth, tint, recolor));
  }

  /**
   * Explicitly enter pixel-editing mode for a previously placed asset.
   *
   * <p>Maintains the layer UUID and visual result, preserving existing effect-track ownership.
   * The caller should wrap this in a single project session edit so Undo can restore the original
   * transformable Image layer. No automatic conversion occurs on placement.
   */
  public static LoomProject convertToPaint(
      LoomProject project, AnimationChannel channel, UUID layerId) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(channel, "channel");
    Objects.requireNonNull(layerId, "layerId");
    LoomCanvas canvas = channel == AnimationChannel.CAPE ? project.cape() : project.elytra();
    LoomLayer source = canvas.layers().stream()
        .filter(layer -> layer.id().equals(layerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown asset layer"));
    if (source.locked()) throw new IllegalStateException("Unlock the asset before editing pixels");
    if (source.kind() != LayerKind.IMAGE)
      throw new IllegalStateException("Only Image assets can be converted to pixel editing");
    int[] pixels = LayerRasterizer.rasterize(source, canvas.width(), canvas.height());
    LoomLayer paint = LoomLayer.paint(
            source.id(), source.name(), source.visible(), source.opacity(),
            source.blendMode(), source.emissive(), source.locked(), pixels)
        .withAlphaLocked(source.alphaLocked())
        .withClipToBelow(source.clipToBelow())
        .withMask(source.mask());
    LoomCanvas updated = canvas.replaceLayer(layerId, paint);
    return channel == AnimationChannel.CAPE
        ? project.withCape(updated)
        : project.withElytra(updated);
  }

  private static ImageLayerData data(
      LoomCanvas canvas,
      CustomStamp asset,
      NormalizedRect face,
      double localX,
      double localY,
      double relativeWidth,
      int tint,
      boolean recolor) {
    Objects.requireNonNull(canvas, "canvas");
    Objects.requireNonNull(asset, "asset");
    Objects.requireNonNull(face, "face");
    if (!Double.isFinite(localX) || !Double.isFinite(localY)
        || localX < 0 || localX > 1 || localY < 0 || localY > 1)
      throw new IllegalArgumentException("Asset placement must be within the selected face");
    if (!Double.isFinite(relativeWidth) || relativeWidth <= 0 || relativeWidth > 1)
      throw new IllegalArgumentException("Asset width must be within the selected face");

    PixelPatch patch = asset.patch();
    int[] pixels = patch.data();
    if (recolor) {
      for (int i = 0; i < pixels.length; i++)
        if ((pixels[i] >>> 24) != 0)
          pixels[i] = (pixels[i] & 0xFF000000) | (tint & 0x00FFFFFF);
    }
    PixelImage source = new PixelImage(patch.width(), patch.height(), pixels);
    double width = face.width() * relativeWidth;
    double height = width * patch.height() / patch.width()
        * canvas.width() / (double) canvas.height();
    var transform = new LayerTransform(
        face.x() + localX * face.width(),
        face.y() + localY * face.height(),
        width,
        height,
        0,
        false,
        false);
    return new ImageLayerData(
        source,
        NormalizedRect.fullCanvas(),
        transform,
        face,
        ImageProcessingSettings.defaults());
  }
}
