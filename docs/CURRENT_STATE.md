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
- typed Paint / Image / Gradient layer editing;
- persistent layer locking and blend/emissive controls;
- Gradient authoring with type/stops/angle/repeat/dither plus move/scale/mirror/reset transforms;
- icon-led typed layer rows with direct visibility/lock affordances;
- selection/move/flip common-core transforms;
- first editor exposure of drag selection, nudge and flip controls.

The current Smart Import + schema-v2 merged baseline is **CI GREEN / LOCAL VISUAL VERIFICATION PENDING**.

Exact merged-main baseline entering the Gradient/typed-layer pass: `fec3d46e8b3e3eb40e24b6a602d2f2bae0da2cf6` — GitHub Actions #123 **SUCCESS**.

Gradient Editor + typed-layer UX implementation checkpoint: `29d43b67ae880679e3b8ae9c3093c7f5ad7c2613` — GitHub Actions #124 **SUCCESS**. Documentation-only head verification follows this implementation checkpoint.

The earlier Select/runtime-hardening checkpoint remains `831b1139a14944b643926f67c655db9b46b65c38` — GitHub Actions #78 **SUCCESS**.

Historical spike-by-spike detail belongs in `PASS_LOG.md`; this file intentionally describes only the current project state.

The refreshed five-screen Loom Studios UI reference set is approved and committed under `docs/references/ui/`. The current visual direction is canvas/task-first, icon-led and intentionally calmer than the earlier concepts: roughly 70% clean creative editor / 30% magical Minecraft workshop, with stronger decoration on Home/Sharing than on work screens.

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

Current schema: **v2**

Stored today:
- project UUID/name;
- created/modified timestamps;
- runtime settings;
- cape canvas;
- Elytra canvas;
- ordered typed layers.

Layer kinds:
- Paint;
- Image;
- Gradient.

Common layer fields:
- UUID;
- name;
- visible;
- opacity;
- stable blend-mode id;
- emissive flag;
- persistent lock state;
- stable layer-kind id.

Paint layers store ARGB pixels.

Image layers store:
- bounded embedded source ARGB image;
- normalized source crop;
- normalized destination transform;
- semantic clip;
- processing mode/settings;
- optional palette.

Gradient layers store:
- Linear/Radial type;
- ordered color stops;
- normalized transform;
- semantic clip;
- repeat;
- dither.

Supported canvas sizes:
- 64x32;
- 128x64;
- 256x128.

Current serialized project limit:
- **1 MiB**

Current embedded Image-layer source limit:
- **256 px max dimension** before project persistence.

Schema v1 projects are decoded through the old paint-only format and explicitly migrated to schema v2.

Schema v1 blend ordinals remain frozen compatibility data; schema v2 uses stable string identifiers for blend modes and layer kinds.

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
- New Paint;
- Import PNG / edit Image;
- New Gradient;
- Duplicate;
- Delete;
- reorder;
- visibility;
- opacity;
- rename;
- persistent Lock;
- Emissive toggle;
- Normal / Add-Glow / Screen / Multiply / Overlay;
- typed Paint / Image / Gradient row icons;
- selected-row accent;
- direct row visibility control;
- direct row lock control.

Gradient authoring currently exposes:
- Linear / Radial;
- ordered editable color stops;
- add/remove stop;
- stop position stepping;
- selected-stop color from the active Loom color;
- angle rotation;
- repeat;
- dither;
- layer-relative move;
- bounded uniform scale;
- horizontal / vertical mirror;
- reset to the semantic target clip.

Gradient move/scale/mirror/reset behavior is implemented through the pure common-core `GradientAuthoring` helper so UI behavior remains deterministic and unit-testable.

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

**Functional implementation is complete on the branch / local visual-runtime verification pending.**

Exact green checkpoint: `7a568f736f273328a3b55d1ea28587832cd45282` — GitHub Actions #113 **SUCCESS**.

Implemented:
- PNG import adapter with size/decode validation;
- Home-screen **Import PNG with Smart Import** entry;
- Cape Editor **Import PNG** entry;
- reopen/edit existing Image layers;
- Original preview;
- Processed preview;
- resulting Cape Texture preview;
- isolated 3D candidate-project preview;
- Fit / Stretch / Crop / Center;
- Keep Aspect;
- move;
- free scale;
- arbitrary persistent rotation with 15-degree editor steps;
- Mirror H / Mirror V;
- Brightness / Contrast / Saturation;
- Reduce Colors;
- Floyd-Steinberg Dither;
- Direct;
- Pixel Art;
- Outline Only;
- Monochrome;
- Palette Limited;
- Posterize;
- selected Loom Swatches palette integration;
- Apply as editable Image layer;
- schema-v2 persistence for source/transform/processing intent;
- Image-layer participation in normal opacity/blend/emissive/visibility/lock behavior;
- runtime/editor/project-thumbnail/multiplayer compilation through the shared typed-layer compiler.

Also implemented in schema v2:
- persistent layer lock;
- first-class editable Image layers;
- first-class editable Gradient layers;
- stable layer-kind/blend identifiers;
- explicit schema-v1 -> schema-v2 migration.

Still future for Smart Import:
- automatic background-removal workflow;
- tint control;
- reference-only layer mode;
- direct transform handles;
- final decorative fidelity pass;
- Elytra-target import once the Elytra editor exists.

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

1. locally verify Select/layers/emissive/Smart Import at the required GUI profiles when the development PC is available;
2. finish richer Gradient authoring UI in the Cape Editor;
3. move the Home screen toward the approved reference with real thumbnail cards and navigation hierarchy;
4. begin the dedicated Elytra editor foundation;
5. then animation authoring, Loom Codes/sharing and final reference-fidelity hardening.

See `NEXT_WORK.md` for the concrete queue.
