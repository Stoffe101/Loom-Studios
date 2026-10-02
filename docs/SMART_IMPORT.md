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

## Implemented color reduction / import modes

### Reduce Colors

A deterministic bounded median-cut style quantizer now:
- bins source RGB into a fixed 5-bit-per-channel histogram;
- weights bins by source pixel frequency;
- recursively splits color boxes by their widest channel/range;
- produces at most 256 palette colors;
- maps source RGB to the nearest generated palette color;
- preserves the source alpha channel;
- leaves already-within-limit artwork pixel-stable.

This avoids an unbounded unique-color map for large source images.

### Palette Limited

`mapToPalette(...)` maps visible source pixels to the nearest supplied palette RGB while preserving source alpha.

Palette size is bounded to 1..256 colors.

This is the processing primitive that the eventual Palette Limited Smart Import mode will use with Loom Swatches/custom palettes.

### Dither

Deterministic Floyd-Steinberg error diffusion is implemented.

Rules:
- RGB error only;
- source alpha is preserved;
- fully transparent pixels remain byte-for-byte untouched;
- error is not diffused into or through fully transparent pixels;
- output colors remain limited to the supplied/generated palette;
- dithered Reduce Colors reuses the same bounded palette extractor.

### Posterize

Posterize supports 2..256 levels per RGB channel and preserves alpha.

### Monochrome

Monochrome uses luminance grayscale and preserves alpha.

## Not implemented yet

### Processing
- Outline Only;
- Pixel-art mode orchestration/presets;
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
- keep crop/placement arithmetic overflow-safe even for hostile coordinates;
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
- overflow-safe crop/source-rectangle bounds checks;
- extreme off-screen destination clipping without integer wraparound;
- zero color-adjustment stability;
- brightness alpha preservation;
- full desaturation;
- adjustment range validation;
- Reduce Colors maximum-color enforcement;
- deterministic palette extraction/reduction;
- alpha preservation during reduction;
- Palette Limited nearest-RGB mapping;
- Posterize level validation/output;
- Monochrome luminance behavior;
- deterministic Floyd-Steinberg output;
- dithering palette containment;
- transparent-pixel error-diffusion barriers;
- dithered Reduce Colors maximum-color enforcement.

## Next implementation slice

Recommended order:
1. exact-SHA CI for the quantization/dithering slice;
2. Outline Only / edge-processing primitive;
3. Pixel-art processing preset/orchestration design;
4. schema-v2 Image layer design;
5. PNG decoding/import adapter;
6. Smart Import UI/reference composition after the data model is stable.
