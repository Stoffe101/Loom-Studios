# Loom Studios — Current State

**Checkpoint:** 2026-10-02  
**Target:** Minecraft Java Edition 1.21.11 / Fabric  
**Active product phase:** Phase 2–5 — editor productization

## Active workshop pass

2026-10-03: five-screen styling and acceptance is IN PROGRESS on draft PR #14. Shared timber/steel/lantern/pennant/wordmark artwork, scenic live previews and revised Smart Import are implemented; 42-screen profile capture/build verification is pending. Prior editor evidence is historical for this new styling pass. See the newest PASS_LOG entry.

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
- project-authored Elytra thickness controls from 25% to 200%;
- cape -> Elytra conversion and Elytra-target Smart Import;
- richer Elytra layer properties and shared Swatches;
- schema-v3 authored animation data;
- layer-targeted animation tracks + keyframes for Cape and Elytra;
- runtime Pulse / Scroll / Hue Shift / Moving Gradient / Sparkle / Emissive Glow effects;
- a reference-oriented Elytra animation timeline with play/pause, scrub, duration, loop, playback speed, track speed, effect selection and keyframe editing;
- isolated fixed-tick 3D preview for timeline scrubbing;
- a responsive shared reference UI shell;
- icon-led compact editor controls;
- contextual Cape inspector tabs for Layers / Color / Properties;
- contextual Elytra inspector tabs for Layers / Color / Properties / Animation;
- a compact timeline mode for 640x360 effective GUI layouts;
- tabbed Smart Import Placement / Processing controls with fixed Apply actions;
- first-class Share / Export navigation from Home, Cape and Elytra;
- a simplified Export / Import sharing workspace.

The editor workspace repair is **PARTIAL: clean build and editor screenshot verification PASS; full five-screen decorative reference fidelity and wider workflow acceptance pending**. The earlier refactor is historical evidence, not acceptance of this revision.

Verified implementation: `a2a9628afced367d4a178f944c5cce239d110fbf`, tested merge `ed4b04f900dc200eb43fa6074fecd5d6a832e084`; [Actions #178](https://github.com/Stoffe101/Loom-Studios/actions/runs/37068918058) passed both build and 22 fresh actual Minecraft captures. Draft PR #14 remains unmerged.

Current repair base: `f49ddb2e9727e16d3e6438772534f278d0520a38`. A unified integer pixel transform now owns cape texture, grid, input, live shape and committed selection bounds. Both editors use a shared bounded workspace layout, slim navigation, contextual controls, cached textures and paged inspectors. Elytra has Layers / Color / Properties / Animation; animation separates Keys and Playback. Empty timelines yield more canvas space. View state survives rebuilds. See the newest PASS_LOG entry for exact validation and remaining gaps.

A local test on 2026-10-02 confirmed the underlying editor/import/Elytra/animation/sharing features were broadly functional, but the pre-refactor interface failed the visual/usability gate:
- it did not resemble the approved references closely enough;
- persistent button walls exposed too many controls at once;
- Cape/Gradient surfaces required excessive vertical scrolling;
- several labels/controls overlapped or became difficult to parse;
- animation authoring was hard to understand;
- Export was technically present but not discoverable from the editors;
- 1920x1080 / GUI scale 3 was especially poor.

That feedback triggered the current full UI-shell refactor rather than incremental spacing patches.

Latest merged-main baseline entering the reference UI refactor: `e3e417ff21f231e8513186e499026682d4cfb785` — GitHub Actions #158 **SUCCESS**.

Current UI-refactor implementation checkpoint: `db90568ad8e47b342e3be36419381f4e2c37af66` — GitHub Actions #172 **SUCCESS**.

Pre-merge reference-UI validation checkpoint: `d6407fd5125836969d6dd5c40ecbb65c4069c1b6` — GitHub Actions #175 **SUCCESS**.

Current refactor includes:
- shared `LoomScreenChrome` and responsive theme primitives;
- rebuilt responsive Home;
- Cape contextual workspace with icon tool rail and inspector tabs;
- Elytra contextual workspace with dedicated Animation inspector;
- compact timeline geometry;
- Smart Import Placement / Processing tabs;
- Share / Export and Import workspaces;
- explicit top-level Share / Export routing from editors;
- inactive Settings removed from the Home primary action stack.

Latest merged-main baseline entering the animation pass: `2eca773a0358344491fd3280f535feb3515268a0` — GitHub Actions #139 **SUCCESS**.

Animation milestone exact green implementation head: `c3e5ca49e4ecccbeaca7ad064a828c63c7720692` — GitHub Actions #147 **SUCCESS**.

Key animation checkpoints:
- schema-v3 model/migration tests: `e4c5aaa1112992e11a172c6cbf3d62874c1c808e` — Actions #141 **SUCCESS**;
- authored runtime evaluator/compiler: `864e48534f5b1464e23089104096af2a4ee1d3d4` — Actions #142 **SUCCESS**;
- reference-integrity + scrubbed preview fix: `88e1b1c051545661f021262b39fc57c3eebf4844` — Actions #145 **SUCCESS**;
- timeline widget: `156afcfe9eb7a2cf5d38f38280bc35335e5dade8` — Actions #146 **SUCCESS**;
- integrated Elytra timeline authoring: `c3e5ca49e4ecccbeaca7ad064a828c63c7720692` — Actions #147 **SUCCESS**.

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

Current schema: **v3**

Stored today:
- project UUID/name;
- created/modified timestamps;
- runtime settings;
- cape canvas;
- Elytra canvas;
- ordered typed layers;
- project animation duration/loop/playback speed;
- layer-targeted animation tracks and ordered keyframes.

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

Schema v1 projects are decoded through the old paint-only format, migrated to schema v2 typed layers, then migrated to current schema v3 with a default empty animation timeline. Schema v2 projects migrate directly to v3.

Schema v1 blend ordinals remain frozen compatibility data; schema v2/v3 uses stable string identifiers for blend modes and layer kinds.

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

Additional Elytra workflow implemented in the current pass:
- cape Outside -> Elytra starting conversion as a new editable Paint layer;
- aspect-fit conversion preserves transparency and mirrors into the opposite semantic wing;
- Smart Import can target Elytra directly;
- linked Elytra import creates two editable Image layers, one per semantic wing;
- the opposite imported wing uses mirrored horizontal placement rather than flattening both wings into one UV rectangle;
- existing Elytra Image layers can be reopened in Smart Import;
- layer reorder;
- opacity;
- rename;
- blend mode;
- shared Swatches/palette window with the Cape Editor.

Still missing for the full reference target:
- standing/open/gliding preview-state controls;
- final reference-layout polish.

The semantic wing-link mode is editor state, not serialized project state.

## Animation/effects

Proven underneath:
- deterministic local animation timing;
- no frame streaming;
- separate emissive cape pass;
- emissive mask compilation.

Implemented authoring: schema-v3 tracks, keyframes, Pulse, Scroll, Hue Shift, Moving Gradient, Sparkle and Emissive Glow. See the Animation section below. This is a functional MVP; visual and shader compatibility acceptance remain separate.

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

Intentionally still unavailable:
- Settings product screen (removed from primary navigation);
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
- typed Image-layer persistence for source/transform/processing intent (introduced in schema v2 and preserved in current schema v3);
- Image-layer participation in normal opacity/blend/emissive/visibility/lock behavior;
- runtime/editor/project-thumbnail/multiplayer compilation through the shared typed-layer compiler.

Schema-v2 typed-layer data remains fully supported inside current schema v3:
- persistent layer lock;
- first-class editable Image layers;
- first-class editable Gradient layers;
- stable layer-kind/blend identifiers;
- explicit schema-v1 -> schema-v2 -> schema-v3 migration.

Smart Import now also supports Elytra:
- linked wing import creates two semantic editable Image layers;
- the opposite wing uses mirrored horizontal placement;
- existing Elytra Image layers can be reopened and edited.

Still future for Smart Import:
- automatic background-removal workflow;
- tint control;
- reference-only layer mode;
- direct transform handles;
- final decorative fidelity pass.

See `SMART_IMPORT.md`.

## Animation

**Functional schema/runtime/timeline MVP is CI green / local visual-runtime verification pending.**

Current schema v3 stores:
- project timeline duration;
- timeline loop;
- playback speed;
- bounded animation tracks;
- target layer UUID;
- Cape/Elytra channel;
- effect type;
- enabled state;
- per-track speed/loop;
- ordered keyframes with tick + scalar value.

Current authored effects:
- Pulse;
- Scroll;
- Hue Shift;
- Moving Gradient;
- Sparkle;
- Emissive Glow.

Runtime animation is evaluated locally from project data. No rendered frames are streamed over the network.

The Elytra editor now contains a compact reference-oriented timeline dock:
- play/pause;
- scrub;
- timeline loop;
- duration +/-;
- playback speed +/-;
- add/select/enable/delete track;
- cycle effect;
- add/remove keyframe;
- keyframe value +/-;
- per-track speed cycle;
- fixed-tick isolated 3D preview.

Layer deletion prunes tracks targeting that layer, and schema-v3 validation rejects orphan track references.

Still future:
- standing/open/gliding preview-state controls;
- richer per-effect property controls;
- direct draggable keyframes;
- Cape Editor timeline exposure;
- final visual/reference polish.

See `ANIMATION.md`.

## Loom Codes / sharing

**Local/offline sharing MVP is implemented and CI green / local visual-runtime verification pending.**

Exact green sharing checkpoints:
- portable-code core: `bff95cbd5c79ce59b417e97f49cb58917b97d307` — Actions #152 **SUCCESS**;
- functional sharing UI/file import: `709087febc06370df247bdb200d0b988e3f7ea79` — Actions #154 **SUCCESS**;
- canonical sharing docs: `a34bfddac35d29e175d9bcdce75dff3ac2969365` — Actions #156 **SUCCESS**.

Reference-05-oriented sharing now provides:
- Home -> Loom Codes navigation;
- deterministic short design fingerprint: `LS-XXXX-XXXX-XXXX`;
- self-contained versioned portable project code: `LSP1:`;
- clipboard Copy Portable Code;
- clipboard Paste + bounded decode;
- native `.loom` project-file import;
- imported-project preview before accepting;
- Cape / Elytra 3D preview toggle;
- Import to Library;
- Import + Open;
- safe fork-on-import with a fresh project UUID;
- export editable `.loom`;
- export portable-code text file;
- export Cape PNG;
- export Elytra PNG;
- dedicated `.minecraft/loom-studios/exports` output directory;
- explicit local/private visibility messaging.

The short `LS-...` value is currently a deterministic **design fingerprint**, not a remotely resolvable cloud code. Loom Studios does not pretend a hosted gallery/share resolver already exists.

The `LSP1:` code is the actual offline project-transfer format and contains a bounded compressed `.loom` project.

Still future:
- hosted short-code resolver/gallery if a backend is intentionally built;
- friends/server/public visibility service;
- clickable chat cards backed by real resolver semantics;
- Favorites/collections product model;
- final Reference 05 decorative polish.

Palette `LOOMPAL1:` codes remain a separate palette-only format.

See `LOOM_CODES.md`.
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

The previous build has now been locally tested. The feature behavior was broadly functional, but its UI failed the visual/usability gate described above. The **new reference UI refactor has not yet been locally re-tested** and must be checked at all four profiles, with 1920x1080 / GUI scale 3 treated as a release-critical compact profile.

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

The immediate gate is a local eyes-on pass of the **new UI architecture**, not another feature expansion.

Verify first:
- Home hierarchy and card density;
- Cape icon rail / context bar / Layers-Color-Properties inspectors;
- no primary Cape editor scrolling for normal authoring;
- Elytra Layers-Color-Animation inspector flow;
- animation track/keyframe workflow clarity;
- compact timeline at 640x360 effective size;
- Smart Import Placement / Processing tabs;
- fixed Apply / 3D Preview / Cancel actions;
- first-class Share / Export discoverability;
- Export Project / Cape PNG / Elytra PNG visibility;
- no text overlap or clipped controls at 1920x1080 GUI 3;
- Swatches floating window interaction over the new editor shell.

Feature-level runtime verification still remains for emissive/blend/shader behavior and the broader Iris/shader matrix.

Automated build/test verification is green through the current UI-refactor checkpoint.

## Immediate direction

1. locally re-test the new reference UI shell at all four required GUI profiles, starting with 1920x1080 / GUI scale 3;
2. fix any remaining overlap, density, hit-target or reference-fidelity defects found in that pass;
3. add Elytra preview-state polish for standing/open/gliding;
4. expose/refine animation authoring in the Cape Editor only if it can remain context-driven rather than increasing clutter;
5. decide whether Settings becomes a real product screen before restoring it to Home;
6. finish final compatibility/performance hardening.

See `NEXT_WORK.md` for the concrete queue.
