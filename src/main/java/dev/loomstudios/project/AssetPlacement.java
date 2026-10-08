package dev.loomstudios.project;

import dev.loomstudios.image.ImagePlacementMode;
import dev.loomstudios.image.PixelImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Places project-owned, independently editable catalog artwork on one semantic face.
 *
 * <p>The catalog is only an authoring source: every placed asset embeds its pixels inside the
 * resulting IMAGE layer, so .loom, portable codes, exports and multiplayer do not require a
 * locally installed pack. This is the first-stage one-layer-per-object implementation; a future
 * bounded collection layer is needed for hundreds of separately selectable stars.
 */
public final class AssetPlacement {
  private AssetPlacement() {}

  public static LoomProject place(
      LoomProject project,
      AnimationChannel channel,
      String name,
      PixelPatch artwork,
      CapeUvRegion capeFace,
      ElytraWing wing,
      ElytraSurface surface,
      double localX,
      double localY,
      int longestSidePixels,
      boolean recolor,
      int color) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(channel, "channel");
    Objects.requireNonNull(artwork, "artwork");
    if (!Double.isFinite(localX) || !Double.isFinite(localY)
        || localX < 0 || localX > 1 || localY < 0 || localY > 1) {
      throw new IllegalArgumentException("Asset cursor must be inside the selected face");
    }
    if (longestSidePixels < 1 || longestSidePixels > 256) {
      throw new IllegalArgumentException("Asset size out of range");
    }

    var canvas = channel == AnimationChannel.CAPE ? project.cape() : project.elytra();
    int scale = CanvasResolution.fromCanvas(canvas).scale();
    final int faceLeft, faceTop, faceWidth, faceHeight;
    if (channel == AnimationChannel.CAPE) {
      CapeUvRegion face = Objects.requireNonNull(capeFace, "capeFace");
      faceLeft = face.atlasX(0, scale);
      faceTop = face.atlasY(0, scale);
      faceWidth = face.width(scale);
      faceHeight = face.height(scale);
    } else {
      ElytraSurface f = Objects.requireNonNull(surface, "surface");
      ElytraWing w = Objects.requireNonNull(wing, "wing");
      faceLeft = f.atlasX(w, 0, scale);
      faceTop = f.atlasY(0, scale);
      faceWidth = f.width(scale);
      faceHeight = f.height(scale);
    }

    // Respect the source aspect ratio, *then* fit inside the selected semantic face.
    double size = longestSidePixels / (double) Math.max(artwork.width(), artwork.height());
    double fit = Math.min(1.0, Math.min(faceWidth / (artwork.width() * size),
        faceHeight / (artwork.height() * size)));
    int w = Math.max(1, Math.min(faceWidth, (int) Math.round(artwork.width() * size * fit)));
    int h = Math.max(1, Math.min(faceHeight, (int) Math.round(artwork.height() * size * fit)));
    int x = faceLeft + Math.max(0, Math.min(faceWidth - w,
        (int) Math.round(localX * faceWidth - w / 2.0)));
    int y = faceTop + Math.max(0, Math.min(faceHeight - h,
        (int) Math.round(localY * faceHeight - h / 2.0)));

    int[] pixels = artwork.data();
    if (recolor) {
      for (int i = 0; i < pixels.length; i++) {
        pixels[i] = (pixels[i] & 0xFF000000) | (color & 0x00FFFFFF);
      }
    }
    var source = new PixelImage(artwork.width(), artwork.height(), pixels);
    var target = new NormalizedRect(x / (double) canvas.width(),
        y / (double) canvas.height(), w / (double) canvas.width(),
        h / (double) canvas.height());
    // The image's clip is the entire *selected face*, not the initial placement bounds.
    // Moving the object later must not crop it to its original position.
    var faceClip = new NormalizedRect(faceLeft / (double) canvas.width(),
        faceTop / (double) canvas.height(), faceWidth / (double) canvas.width(),
        faceHeight / (double) canvas.height());
    var placed = ImageLayerData.placed(source, canvas.width(), canvas.height(), target,
        ImagePlacementMode.STRETCH);
    var owned = new ImageLayerData(source, placed.sourceCrop(), placed.transform(), faceClip,
        placed.processing());
    return channel == AnimationChannel.CAPE
        ? ProjectEdits.addCapeImageLayer(project, name, owned)
        : ProjectEdits.addElytraImageLayer(project, name, owned);
  }

  /** Edits source pixels without flattening the placement transform or other layers. */
  public static LoomProject editPixel(
      LoomProject project, AnimationChannel channel, UUID layerId, int pixelX, int pixelY, int argb) {
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(channel, "channel");
    Objects.requireNonNull(layerId, "layerId");
    var canvas = channel == AnimationChannel.CAPE ? project.cape() : project.elytra();
    var layer = canvas.layers().stream().filter(l -> l.id().equals(layerId))
        .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown asset layer"));
    if (layer.kind() != LayerKind.IMAGE || layer.locked()) {
      throw new IllegalStateException("Choose an unlocked Image layer to edit its artwork");
    }
    var data = layer.imageData();
    if (!data.frames().isEmpty()) {
      throw new IllegalStateException("Use the frame editor to edit animated image pixels");
    }
    var original = data.source();
    if (pixelX < 0 || pixelY < 0 || pixelX >= original.width() || pixelY >= original.height()) {
      throw new IllegalArgumentException("Pixel is outside the asset artwork");
    }
    if (original.pixelAt(pixelX, pixelY) == argb) return project;
    int[] pixels = original.pixels();
    pixels[pixelY * original.width() + pixelX] = argb;
    var updated = new ImageLayerData(new PixelImage(original.width(), original.height(), pixels),
        data.sourceCrop(), data.transform(), data.clip(), data.processing());
    return channel == AnimationChannel.CAPE
        ? ProjectEdits.setCapeImageData(project, layerId, updated)
        : ProjectEdits.setElytraImageData(project, layerId, updated);
  }

  /** Independent transform edits retain the exact embedded artwork and current source-pixel edits. */
  public static LoomProject transform(
      LoomProject project, AnimationChannel channel, UUID layerId, LayerTransform next) {
    Objects.requireNonNull(next, "next");
    var canvas = channel == AnimationChannel.CAPE ? project.cape() : project.elytra();
    var layer = canvas.layers().stream().filter(l -> l.id().equals(layerId))
        .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown asset layer"));
    if (layer.kind() != LayerKind.IMAGE || layer.locked()) {
      throw new IllegalStateException("Choose an unlocked Image layer to move or resize");
    }
    ImageLayerData updated = layer.imageData().withTransform(next);
    return channel == AnimationChannel.CAPE
        ? ProjectEdits.setCapeImageData(project, layerId, updated)
        : ProjectEdits.setElytraImageData(project, layerId, updated);
  }
}
