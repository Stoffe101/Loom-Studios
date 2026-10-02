# Loom Studios — Smart Import

**Status:** Processing/transform foundation in progress  
**Reference target:** `Loom_Studios_03_Smart_Import.png`

## Product rule

Smart Import is a guided, non-destructive image workflow.

The final project representation should preserve:
- original imported asset or bounded source data;
- placement/transform parameters;
- processing parameters;
- layer opacity/tint;
- later animation/effect metadata where applicable.

The pure image-processing core may rasterize temporary previews and compiled output, but the eventual Image layer must not silently replace editable intent with one flattened paint layer.

## Current pure-core model

### PixelImage

`PixelImage` is an immutable bounded ARGB raster value used by processing code and automated tests.

Current safety limits:
- maximum dimension: 4096 pixels;
- maximum total pixels: 16,777,216;
- exact pixel-count validation;
- defensive pixel-array ownership.

These are processing limits, not Loom cape/Elytra canvas sizes.

The editable Loom canvas remains 64x32 / 128x64 / 256x128.

### Placement modes

The approved first placement modes now have deterministic common-core semantics.

#### Fit

- preserve source aspect ratio;
- scale until the whole source fits inside the target;
- center the result;
- uncovered target pixels remain transparent.

#### Stretch

- use the full source;
- fill the full target;
- aspect ratio may change.

#### Crop

- preserve aspect ratio;
- take a centered source crop matching the target aspect ratio;
- scale that crop to fill the complete target.

This is the Smart Import meaning of Crop.

It does **not** resize or change Minecraft's fixed semantic cape-face dimensions.

#### Center

- do not scale;
- center source pixels in the target;
- clip portions that lie outside the target;
- preserve transparent target pixels where the source does not cover them.

## Implemented raster transforms

Pure/testable operations:
- mirror horizontal;
- mirror vertical;
- rotate 90 degrees clockwise;
- rotate 90 degrees counter-clockwise;
- rectangular crop;
- nearest-neighbor resize;
- render an `ImagePlacement` into a transparent target;
- Fit / Stretch / Crop / Center convenience placement.

Nearest-neighbor resizing is intentionally available because pixel-art import must not be forced through smoothing.

Later preview/compiler paths may add other sampling strategies when appropriate.

## Implemented color adjustments

Current deterministic controls:
- Brightness;
- Contrast;
- Saturation.

Each uses a normalized `-1..1` range.

Rules:
- alpha is preserved;
- RGB output is clamped to 0..255;
- zero adjustment is pixel-stable;
- saturation -1 produces luminance grayscale.

The eventual UI may present friendlier slider labels/percentages while mapping to this normalized core.

## Not implemented yet

### Processing
- Reduce Colors / palette quantization;
- Dither;
- Posterize;
- Monochrome mode;
- Outline Only;
- Pixel-art mode orchestration;
- Palette Limited mode;
- transparency/background removal workflow;
- tint.

### Transform
- arbitrary-angle rotation;
- free scale;
- explicit position offsets after initial placement;
- Keep Aspect Ratio interactive constraint;
- transform handles.

### Data model
- schema-v2 Image layer;
- imported asset storage/content addressing;
- persistent transform parameters;
- persistent processing parameters;
- migration fixtures.

### UI
- PNG picker/import source;
- original preview;
- processed preview;
- resulting cape/Elytra texture preview;
- integrated 3D preview;
- Apply Import;
- Import as New Layer;
- Use as Reference Layer;
- Cancel.

## PNG decode safety gate

Before PNG loading is exposed:
- inspect dimensions before allocating large processing buffers where the decoder permits;
- reject unsupported/oversized images cleanly;
- enforce decompressed pixel bounds;
- preserve alpha;
- never trust extension alone;
- avoid per-frame decoding.

## Schema relationship

This processing package is deliberately independent from schema v1.

Schema v2 should reference the transform/placement concepts without forcing preview rasters into project persistence.

Persistent layer lock, Image layers, Gradient layers and stable layer-kind identifiers should still be introduced together as the coordinated schema expansion described in `PROJECT_FORMAT.md` and `NEXT_WORK.md`.

## Automated coverage

Current tests cover:
- PixelImage defensive ownership;
- invalid dimensions/pixel counts;
- mirror horizontal/vertical;
- clockwise/counter-clockwise quarter rotation;
- crop bounds;
- nearest-neighbor resize;
- Fit geometry;
- Stretch geometry;
- centered Crop geometry;
- Center geometry with negative destination offsets;
- transparent Fit letterboxing;
- centered Crop raster output;
- oversized Center clipping;
- invalid custom source placement rejection;
- zero color-adjustment stability;
- brightness alpha preservation;
- full desaturation;
- adjustment range validation.

## Next implementation slice

Recommended order:
1. exact-SHA CI for the current transform/adjustment foundation;
2. palette quantization;
3. deterministic dithering;
4. Posterize / Monochrome / Palette Limited processing primitives;
5. schema-v2 Image layer design;
6. PNG decoding/import adapter;
7. Smart Import UI/reference composition after the data model is stable.
