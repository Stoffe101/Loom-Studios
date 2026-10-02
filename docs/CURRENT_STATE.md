# Loom Studios — Current State

**Checkpoint:** 2026-10-02  
**Target:** Minecraft Java Edition 1.21.11 / Fabric  
**Active product phase:** Phase 2 — Cape Editor core

## Overall status

The Minecraft/Fabric feasibility phase is complete enough to support normal product development.

Loom Studios currently has:
- a versioned editable `.loom` project model;
- local save/load and bounded undo/redo;
- separate cape and Elytra project canvases;
- vanilla-first cape and Elytra rendering;
- live dynamic textures;
- content-addressed multiplayer synchronization;
- an isolated live 3D preview;
- 1x / 2x / 4x cape editing;
- a functional semantic-face Cape Editor;
- custom palettes/Swatches;
- paint-layer editing and blend modes;
- selection/move/flip common-core transforms;
- first editor exposure of drag selection, nudge and flip controls.

The current implementation slice is **CI GREEN / LOCAL VISUAL VERIFICATION PENDING**.

Exact green checkpoint: `831b1139a14944b643926f67c655db9b46b65c38` — GitHub Actions #78 **SUCCESS**.

Historical spike-by-spike detail belongs in `PASS_LOG.md`; this file intentionally describes only the current project state.

## Current development baseline

- Minecraft: **1.21.11**
- Java: **21**
- Fabric Loader compatibility baseline: **0.18.4**
- Fabric API: **0.141.1+1.21.11**
- Fabric Loom: **1.17.21**
- Gradle wrapper: **9.6.1**
- mappings: official Mojang mappings
- source layout: split common/client
- license: proprietary / All Rights Reserved
- CI: GitHub Actions Java 21 build on `main` and pull requests

Optional compatibility targets, not dependencies:
- Sodium
- Sodium Extra
- Iris
- shader packs
- 3D Skin Layers

## Current architecture

### Editable project ownership

`ClientProjectWorkspace` owns the local `ProjectSession`.

`ProjectSession` owns:
- the immutable current `LoomProject`;
- bounded undo/redo;
- revision;
- persisted source path;
- current content hash cache;
- dirty state.

Edits flow through immutable `ProjectEdits` transformations.

### Editing versus equipped state

Editing state and equipped state are intentionally separate.

- the editor may contain dirty unsaved changes;
- the 3D editor preview may render those dirty changes;
- world rendering uses the explicitly equipped snapshot;
- multiplayer advertises/uploads the explicitly equipped snapshot;
- Save does not implicitly equip;
- Save + Equip updates the equipped snapshot.

The current hardening slice corrects a regression where `ClientCosmeticSync` could read the editable local project instead of the equipped project.

### Runtime texture ownership

`RuntimeCosmeticCache` owns:
- compiled cape texture;
- compiled Elytra texture;
- compiled emissive cape texture;
- `NativeImage` resources;
- `DynamicTexture` resources;
- content-hash keyed runtime bundles.

`PlayerCosmeticRenderer` owns render-state patching and small render-facing state only.

### Multiplayer

The implemented protocol is content-addressed:

1. equipped project is encoded;
2. SHA-256 becomes the project identity;
3. client announces the equipped hash;
4. server requests the blob on cache miss;
5. server validates hash/schema/limits;
6. server stores the equipped UUID -> hash mapping;
7. remote clients request unknown hashes;
8. remote clients validate/cache/compile locally.

Rendered animation frames are never streamed.

## Project format

Current schema: **v1**

Stored today:
- project UUID/name;
- created/modified timestamps;
- runtime settings;
- cape canvas;
- Elytra canvas;
- ordered paint layers.

Paint-layer fields:
- UUID;
- name;
- visible;
- opacity;
- blend mode;
- emissive flag;
- ARGB pixels.

Supported canvas sizes:
- 64x32;
- 128x64;
- 256x128.

Current serialized project limit:
- **1 MiB**

Current blend modes:
- Normal;
- Add / Glow;
- Screen;
- Multiply;
- Overlay.

Schema-v1 blend ordinals are pinned compatibility data.

Persistent layer lock and new layer kinds are deliberately deferred to the coordinated schema-v2 expansion.

## Cape Editor — implemented

### Canvas/editing
- semantic cape-face editing instead of raw-atlas-first editing;
- default Outside / Back face;
- Inside / left edge / right edge / top / bottom face switching;
- 1x / 2x / 4x project resolution;
- revision-cached `DynamicTexture` editor preview;
- checker transparency;
- pixel/grid coordinate diagnostics;
- zoom 100% to 800%;
- mouse-wheel zoom;
- middle-mouse panning;
- scissored canvas viewport.

### Paint tools
- Pencil;
- Eraser;
- Fill;
- Eyedropper;
- Line;
- Rectangle outline;
- Rectangle filled;
- brush size;
- transient brush-radius preview;
- symmetry Off / Horizontal / Vertical / Both.

### Selection/transforms
Common-core:
- `PixelSelection`;
- normalized drag bounds;
- semantic-face validation;
- Move;
- Flip Horizontal;
- Flip Vertical;
- combined flip;
- clipping protection against unrelated UV faces.

Editor exposure in the current slice:
- Select tool;
- drag-to-select rectangle;
- persistent selection outline;
- Move Left / Right / Up / Down;
- arrow-key nudge while Select is active;
- Flip H / Flip V;
- Clear Selection;
- UI nudge clamps the selection inside the active semantic face;
- changing semantic face or project resolution clears the temporary selection;
- Undo/Redo clear temporary selection state to avoid stale moved coordinates.

Selection itself remains editor state and is not serialized into `.loom`.

### Color
- HSV saturation/value picker;
- hue strip;
- editable Hex;
- editable R/G/B/A;
- alpha slider;
- semi-transparent paint;
- starter swatches;
- persistent custom named palettes;
- grouped Swatches dock;
- palette import/export;
- `LOOMPAL1:` palette share codes.

### Layers
- New;
- Duplicate;
- Delete;
- reorder;
- visibility;
- opacity;
- rename;
- Emissive toggle;
- Normal / Add-Glow / Screen / Multiply / Overlay.

Layer edits participate in normal ProjectSession history.

Per-layer emissive flags are the render-time authority. The schema-v1 runtime emissive master is synchronized automatically as a compatibility mirror, including multi-layer cases. The old SPIKE-06 synthetic shimmer has been removed from real project output.

### Project actions
- Undo / Redo;
- Save;
- Save + Equip;
- unsaved 3D Preview;
- Back.

The right tool rail uses Minecraft `ScrollableLayout` and is expected to remain usable on compact GUI profiles.

## 3D preview

Implemented:
- real local player model/skin;
- isolated extracted render state;
- dirty editor project preview;
- Cape / Elytra preview switching;
- drag rotation;
- wheel zoom;
- real world equipment remains unchanged.

Still planned:
- independent player-facing versus viewer-orbit controls;
- neutral/default head behavior;
- visible zoom/orbit controls;
- standing/open/gliding Elytra states;
- final integrated reference-layout preview panel.

## Elytra

Rendering foundation is implemented:
- dedicated Loom Elytra texture;
- independent cape/Elytra channels;
- vanilla gliding/wing animation;
- high-resolution project canvas support;
- calibrated visual thickness baseline;
- temporary development thickness presets;
- preview Cape/Elytra switching.

The dedicated Elytra editor is not implemented yet.

## Animation/effects

Proven underneath:
- deterministic local animation timing;
- no frame streaming;
- separate emissive cape pass;
- emissive mask compilation.

Not implemented as an authoring product:
- timeline;
- tracks;
- keyframes;
- Pulse;
- Scroll;
- Hue Shift;
- Moving Gradient;
- Sparkle;
- authored Emissive Glow parameters.

## Home / project library

Implemented:
- Loom home screen;
- Create New Cape;
- open selected saved project;
- local Recent Projects index;
- selection persistence;
- generated project thumbnail PNG cache.

Still missing:
- actual thumbnail-card UI;
- reference-style project hub hierarchy;
- Edit Elytra;
- Import Image;
- Loom Codes;
- Templates;
- Settings;
- integrated selected-project/player preview.

## Smart Import

**User-facing workspace not started / pure processing foundation CI green.**

Exact verified Smart Import core checkpoint: `c7b3c4107543f42a30c214e7aa41942d59281249` — GitHub Actions #81 **SUCCESS**.

Implemented pure-core prerequisites:
- immutable bounded `PixelImage`;
- Fit / Stretch / centered Crop / Center placement math;
- transparent placement rendering;
- mirror horizontal / vertical;
- 90-degree clockwise/counter-clockwise rotation;
- rectangular crop;
- nearest-neighbor resize;
- Brightness / Contrast / Saturation with preserved alpha;
- deterministic bounded Reduce Colors palette extraction;
- Palette Limited nearest-color mapping;
- Floyd-Steinberg dithering;
- Posterize;
- Monochrome;
- automated transform/placement/adjustment/quantization/dithering tests.

Already available elsewhere:
- high-resolution Loom canvases;
- alpha;
- layers;
- palettes;
- live preview;
- selection/move/flip primitives;
- versioned project storage.

Still required:
- Outline Only / edge processing;
- Pixel-art processing orchestration/presets;
- arbitrary/free image transforms;
- PNG decode/import adapter;
- schema-v2 Image layer representation;
- original/processed/texture/3D Smart Import UI.

See `SMART_IMPORT.md`.

## Loom Codes / sharing

Implemented related infrastructure:
- SHA-256 project identity;
- project cache-miss network transfer;
- local `.loom` files;
- palette-only `LOOMPAL1:` codes.

Not implemented:
- project short Loom Codes;
- portable project code;
- private clickable chat result;
- Copy / Import / Preview / Favorite project UX;
- final export/share screen.

Palette codes are not project Loom Codes.

## Cape Loom block

The final Cape Loom workstation/block flow is not implemented yet.

Development access currently uses the temporary Loom Studios key binding.

## Reference fidelity

The five approved references remain active product constraints:

1. Home / Start Screen
2. Cape Editor
3. Smart Import
4. Elytra + Animation Editor
5. Loom Codes / Sharing

Current implementation intentionally prioritizes functional architecture before final decorative polish, but screen hierarchy, density, dark slate surfaces, cyan/violet accents, grouped tools, Swatches/Layers roles and live-preview intent must continue tracking the references.

The binary reference PNGs are still not committed under `docs/references/ui/`.

## Required UI profiles

Every meaningful editor-layout change must eventually be checked at:

- 1920x1080 / GUI scale 2;
- 1920x1080 / GUI scale 3;
- 3440x1440 / GUI scale 2;
- 3440x1440 / GUI scale 3.

Because the current user is away from the development PC, newly added selection controls and latest layer/emissive behavior are **not visually/runtime verified yet**.

## Automated coverage currently includes

- deterministic project encode/decode/hash;
- schema rejection;
- canvas bounds;
- defensive pixel ownership;
- save/load/dirty lifecycle;
- path containment;
- high-resolution resize;
- palette code round-trip;
- palette alpha compatibility;
- Fill;
- Line;
- Rectangle;
- layer stack operations;
- layer rename/blend/emissive persistence;
- blend ordinal compatibility;
- PixelSelection normalization/validation;
- selection horizontal/vertical/combined flip;
- selection move behavior;
- emissive-layer/runtime-master synchronization.

## Current verification gaps

Still requires local eyes-on/runtime testing:
- latest Select tool layout and interaction;
- selection outline at all required GUI profiles;
- nudge/flip interaction under zoom/pan;
- compact Swatches after the newest tool-rail growth;
- latest Layers controls;
- emissive layer toggle in real rendering;
- blend modes visually;
- shader-on/off emissive behavior;
- broader final Iris/shader matrix.

Automated build/test verification is green. The remaining gate for the newest editor controls is local visual/runtime verification when the development PC is available.

## Immediate direction

1. locally verify the current selection/layer/emissive slice when the development PC is available;
2. continue CI-safe work on reusable transform/import foundations meanwhile;
3. design schema v2 before adding persistent layer lock/image/gradient/effect layer kinds;
4. move into Smart Import, Elytra editor, animation authoring and project sharing in the documented order.

See `NEXT_WORK.md` for the concrete queue.
