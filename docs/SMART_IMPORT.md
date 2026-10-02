# Loom Studios — Smart Import

**Status:** Functional implementation complete / exact-head CI and local visual verification pending  
**Reference target:** `Loom_Studios_03_Smart_Import.png`

## Product rule

Smart Import is a guided, non-destructive image workflow.

Imported artwork remains an editable Image layer. Loom Studios does not silently flatten the import into paint pixels.

The persistent Image layer keeps:
- bounded source ARGB pixels;
- source crop;
- normalized target transform;
- semantic target clip;
- processing mode/settings;
- optional palette;
- normal layer opacity/blend/emissive/visibility/lock metadata.

Runtime/editor textures are compiled products, not the editable source of truth.

## Implemented import flow

Smart Import is reachable from:
- Loom Home through **Import PNG with Smart Import**;
- Cape Editor through **Import PNG**;
- an existing Image layer through **Edit Image**.

The current screen provides:
- PNG file picker;
- Original preview;
- Processed preview;
- resulting Cape Texture preview;
- isolated 3D candidate-project preview;
- Apply as Image Layer;
- update/reopen flow for existing Image layers;
- Cancel without equipping or publishing the candidate project.

The candidate 3D preview uses the normal Loom render compiler through a scoped preview project. It does not mutate the equipped project or multiplayer state.

## PNG safety and persistence

Client PNG decoding is handled by `PngImportAdapter`.

Safety rules:
- current user-facing import accepts PNG only;
- compressed input is capped at 32 MiB;
- decoded processing images retain the common `PixelImage` safety limits:
  - maximum dimension 4096;
  - maximum 16,777,216 pixels;
- alpha is preserved;
- the extension is not sufficient by itself: NativeImage must successfully parse the content;
- source decoding happens on import, not per rendered frame;
- embedded project source is downscaled with nearest-neighbor sampling when either dimension exceeds 256;
- final candidate project encoding is validated before the workspace edit is accepted, so an import that would exceed the 1 MiB `.loom` limit is rejected before it creates an unsavable project.

The 256-pixel embedded-source bound is separate from the temporary 4096-pixel processing bound.

## Placement and transform model

The persistent target transform is normalized to the Loom canvas.

That makes Image-layer placement resolution-independent across 64x32, 128x64 and 256x128 project canvases.

### Placement modes

**Fit**
- contain source inside target;
- preserve aspect ratio;
- center result;
- transparent letterbox is allowed.

**Stretch**
- use full source;
- fill target;
- aspect ratio may change.

**Crop**
- take a centered source crop matching target aspect;
- preserve aspect ratio;
- fill target;
- does not change Minecraft's fixed cape-face dimensions.

**Center**
- keep source pixel scale relative to the target canvas;
- center it;
- clip overflow at the target semantic face.

### Interactive transforms

Smart Import exposes:
- Keep Aspect;
- Move Left / Right / Up / Down;
- free scale from 10% to 400% of the placed size;
- arbitrary-angle rotation in 15-degree editor steps;
- Mirror H;
- Mirror V.

The persistent `LayerTransform` itself stores arbitrary finite rotation rather than restricting the schema to 90-degree turns.

## Processing modes

The common deterministic pipeline now exposes:

### Direct
Color adjustments only, with optional final color reduction.

### Pixel Art
Uses nearest/pixel-preserving source data plus bounded color reduction. Default palette size is 16 when no explicit color limit is selected.

### Outline Only
Keeps visible edge pixels based on alpha/luminance discontinuities and clears non-edge interior pixels.

### Monochrome
Converts RGB to luminance grayscale while preserving alpha.

### Palette Limited
Maps visible source RGB to a constrained palette while retaining source alpha.

Smart Import can use:
- the currently selected persisted Loom Swatches palette; or
- an automatically extracted bounded palette when no explicit Swatches palette is attached.

### Posterize
Reduces each RGB channel to a configurable number of levels.

## Processing controls

Implemented:
- Brightness `-1..1`;
- Contrast `-1..1`;
- Saturation `-1..1`;
- Reduce Colors / palette-size limit;
- deterministic Floyd-Steinberg dithering;
- Posterize levels;
- selected Loom Swatches palette integration.

Processing settings remain serialized with the Image layer and can be reopened/edited after saving the project.

## Pure image-processing foundation

Reusable/testable common code includes:
- immutable bounded `PixelImage`;
- Fit / Stretch / Crop / Center geometry;
- mirror horizontal/vertical;
- rotate 90° clockwise/counter-clockwise;
- crop;
- nearest-neighbor resize;
- arbitrary persistent destination rotation through `LayerTransform`;
- Brightness / Contrast / Saturation;
- deterministic bounded color quantization;
- palette mapping;
- Floyd-Steinberg dithering;
- Posterize;
- Monochrome;
- Outline Only;
- processing-mode orchestration.

## Schema-v2 relationship

Schema v2 makes Smart Import a first-class project feature.

Image-layer payload:
- embedded source image;
- normalized source crop;
- normalized destination transform;
- normalized semantic clip;
- processing mode;
- Brightness / Contrast / Saturation;
- color limit;
- dither flag;
- Posterize levels;
- optional palette.

Image layers also use normal layer metadata:
- UUID;
- name;
- visible;
- opacity;
- stable blend mode;
- emissive;
- persistent lock.

Schema-v1 projects are decoded through the old paint-only layout and explicitly migrated to schema v2.

## Runtime/compiler behavior

`LayerRasterizer` converts Paint, Image and Gradient layers into canvas-sized temporary rasters.

`LoomTextureCompiler` then applies the existing shared:
- visibility;
- opacity;
- blend mode;
- emissive-only filtering;
- optional runtime hue animation.

Therefore imported Image layers automatically participate in:
- editor composite preview;
- project thumbnails;
- 3D preview;
- equipped runtime cape;
- multiplayer hash/blob transfer.

No special runtime-network format is required for Smart Import beyond the versioned `.loom` blob.

## Automated coverage

Existing processing tests cover:
- PixelImage validation/defensive ownership;
- placement geometry and clipping;
- mirror/crop/resize/quarter-turn transforms;
- adjustment bounds and alpha preservation;
- deterministic bounded quantization;
- palette mapping;
- Posterize;
- Monochrome;
- deterministic Floyd-Steinberg dithering and transparency barriers.

The current milestone adds coverage for:
- Outline Only;
- processing-mode stable IDs;
- Direct-mode stability;
- Pixel Art color bounds;
- Palette Limited alpha preservation;
- schema-v1 -> schema-v2 migration;
- schema-v2 Image-layer round trip;
- persistent lock state;
- typed Image-layer rasterization;
- resolution-independent typed-layer placement;
- rejection of paint mutation against locked/typed layers.

## Local visual/runtime verification still required

Because the current user is away from the development PC, do not call the screen visually DONE yet.

Verify later:
- file picker opens and returns correctly on Windows;
- PNG with and without alpha;
- large/odd/small PNGs;
- Original / Processed / Cape Texture preview layout;
- all placement modes;
- Keep Aspect;
- move/scale/rotate/mirror;
- each processing mode;
- Reduce Colors and Dither;
- selected Swatches palette;
- Apply new Image layer;
- reopen/edit an Image layer;
- 3D candidate preview;
- Undo/Redo after applying;
- all mandatory GUI profiles.

## Later enhancements outside this milestone

Still future:
- automatic background-removal workflow;
- dedicated tint control;
- Reference-only layer mode;
- transform handles directly on the preview;
- final reference-image decorative treatment;
- importing directly into the dedicated Elytra editor once that screen exists.
