# Loom Studios — Current State

**Checkpoint:** 2026-10-02  
**Target:** Minecraft Java Edition 1.21.11 / Fabric  
**Active product phase:** Phase 2–5 — editor productization

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
- first editor exposure of drag selection, nudge and flip controls;
- reference-oriented Home dashboard with real recent-project thumbnail cards;
- integrated selected-project 3D preview on Home;
- functional Blank and Gradient Home templates;
- semantic unfolded Elytra wing editor;
- linked-mirror and independent-wing painting;
- Elytra Paint-layer stack with direct visibility/lock affordances;
- project-authored Elytra thickness controls from 25% to 200%.

The current editor-productization branch is **CI GREEN / LOCAL VISUAL VERIFICATION PENDING**.

Latest merged-main baseline entering this pass: `213bd4e9d5d5f5f9449fbd8c3a3fce74cf71e6ec` — GitHub Actions #126 **SUCCESS**.

Current pass exact green implementation checkpoints:
- Home reference shell: `0cc3b219fcda2c8b4fb619db900545890e40764e` — GitHub Actions #127 **SUCCESS**;
- semantic Elytra wing editor: `fb21521181dae6139122b8e0e4891e4083627203` — GitHub Actions #128 **SUCCESS**;
- reusable Elytra layer stack: `af767ce8c12a55c725d5b14fc2602872515b5564` — GitHub Actions #129 **SUCCESS**;
- project-authored Elytra thickness: `e46960484bd4d8ef7ef308805a0ad449ba67c643` — GitHub Actions #130 **SUCCESS**.

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

Rendering foundation:
- dedicated Loom Elytra texture;
- independent cape/Elytra channels;
- vanilla gliding/wing animation;
- high-resolution project canvas support;
- calibrated visual thickness baseline;
- preview Cape/Elytra switching.

Semantic Elytra Editor implemented in the current pass:
- unfolded semantic Left / Right 10x20 wing-front regions;
- 1x / 2x / 4x editing;
- Pencil / Eraser;
- scalable brush;
- linked mirror mode;
- separate-wing mode;
- linked edits mirror local X into the opposite vanilla UV wing;
- persistent Paint-layer lock enforcement;
- reusable canvas-backed typed layer list;
- Add / Copy / Delete Elytra Paint layers;
- per-row visibility;
- per-row persistent Lock;
- Undo / Redo;
- Save / Save + Equip;
- integrated draggable/zoomable Elytra 3D preview;
- project-authored Elytra thickness 25%–200%;
- old debug thickness preset override retired so saved/equipped project thickness is authoritative.

Still missing for the full reference target:
- cape-to-Elytra starting conversion;
- Elytra-target Smart Import / Image-layer authoring;
- richer Elytra layer property UI (reorder, opacity, rename, blend);
- full Swatches parity with Cape Editor;
- standing/open/gliding preview-state controls;
- animation timeline/tracks/effects;
- final reference-layout polish.

The semantic wing-link mode is editor state, not serialized project state.

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
- responsive reference-oriented three-column Home shell;
- icon-led action cards;
- Create New Cape;
- functional Edit Elytra entry;
- Load selected saved design;
- Import Image -> Smart Import;
- local Recent Projects index;
- selection persistence;
- generated project thumbnail PNG cache;
- real recent-project thumbnail cards;
- selected-project 3D player/cape preview;
- draggable preview rotation + wheel zoom + Reset View;
- functional Blank template;
- functional Gradient template;
- template/category cards;
- project count/status footer;
- unreadable-project warning surface.

Intentionally still placeholder/inactive:
- project Loom Codes;
- Settings product screen;
- Nature / Space / Fantasy / Emblems template packs.

Home visual/runtime verification at the four mandatory GUI profiles is still pending.

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

The approved optimized WebP reference set is committed under `docs/references/ui/`; full-resolution PNG masters live in the ChatGPT Project Library.

## Required UI profiles

Every meaningful editor-layout change must eventually be checked at:

- 1920x1080 / GUI scale 2;
- 1920x1080 / GUI scale 3;
- 3440x1440 / GUI scale 2;
- 3440x1440 / GUI scale 3.

Because the current user is away from the development PC, the latest Cape/Gradient controls, Home dashboard and Elytra Editor are **not visually/runtime verified yet**.

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

1. locally verify Cape/Gradient/Smart Import/Home/Elytra at the required GUI profiles when the development PC is available;
2. finish remaining Elytra product workflow: cape-to-Elytra conversion, richer layer properties/import parity and preview states;
3. design/implement animation authoring schema + timeline;
4. implement project Loom Codes/sharing;
5. finish reference-fidelity and compatibility hardening.

See `NEXT_WORK.md` for the concrete queue.
