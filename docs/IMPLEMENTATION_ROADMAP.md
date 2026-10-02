# Loom Studios — Implementation Roadmap

## Phase 0 — Foundation spikes

**Status:** PRIMARY PATHS PROVEN.

Implemented/proven:
- toolchain;
- static/dynamic cape;
- Elytra;
- GUI preview;
- multiplayer sync;
- animation/emissive proof.

Remaining release-hardening item:
- broader final Iris/shader matrix.

## Phase 1 — Project core

**Status:** SCHEMA-V2 FOUNDATION GREEN.

Implemented:
- schema-v2 project model;
- explicit schema-v1 -> v2 migration;
- cape + Elytra canvases;
- typed Paint / Image / Gradient layers;
- persistent layer lock;
- stable schema-v2 blend/kind identifiers;
- normalized typed-layer transforms;
- serialization/hash;
- local project library;
- runtime texture cache;
- project thumbnails;
- ProjectSession;
- dirty-state/save/load;
- undo/redo;
- multiplayer transfer/cache.

## Phase 2 — Cape editor core

**Status:** ACTIVE / MAJOR FUNCTIONAL CORE IMPLEMENTED.

Implemented:
- home/start bootstrap;
- semantic cape face canvas;
- Pencil / Eraser;
- Fill / Eyedropper;
- Line;
- Rectangle outline/filled;
- 1x / 2x / 4x;
- zoom/pan;
- brush size + transient footprint preview;
- alpha-aware HSV/RGB/Hex controls;
- custom grouped Swatches;
- symmetry;
- undo/redo;
- live unsaved 3D preview;
- save/equip;
- responsive scrollable tool rail.

Implemented in the current selection slice:
- drag selection;
- persistent selection outline;
- Move/nudge;
- Flip Horizontal / Vertical.

Still required:
- richer Gradient authoring controls in the Cape Editor;
- richer typed-layer rows/icons;
- shortcut/tooltips surface;
- final reference-image editor composition.

Note: the fixed Minecraft cape UV face itself is not resized by a destructive crop operation. Crop belongs to imported-image placement inside the fixed target surface.

## Phase 3 — Layer system

**Status:** TYPED LAYER FOUNDATION IMPLEMENTED.

Implemented:
- add;
- delete;
- duplicate;
- reorder;
- visibility;
- opacity;
- rename;
- persistent lock;
- emissive flag;
- Normal;
- Add / Glow;
- Screen;
- Multiply;
- Overlay;
- Paint layers;
- non-destructive Image layers;
- editable Gradient layers;
- shared typed-layer runtime compilation.

Remaining:
- richer layer row/icon UI;
- richer Gradient authoring controls;
- future effect/reference layer metadata.

## Phase 4 — Smart Import

**Status:** FUNCTIONAL IMPLEMENTATION GREEN / LOCAL VISUAL VERIFICATION PENDING.

Implemented:
- PNG import adapter;
- Fit / Stretch / centered Crop / Center;
- Keep Aspect;
- move / free scale / arbitrary persistent rotation;
- mirror H/V;
- crop + nearest-neighbor resize;
- brightness/contrast/saturation;
- bounded immutable ARGB processing image;
- Reduce Colors;
- Palette Limited mapping;
- Floyd-Steinberg dithering;
- Posterize;
- Monochrome;
- Outline Only;
- Direct;
- Pixel Art orchestration;
- Original / Processed / Cape Texture previews;
- isolated 3D candidate preview;
- Apply as editable Image layer;
- reopen/edit Image layer;
- schema-v2 persistence of source/transform/processing intent.

Later enhancements:
- background removal workflow;
- tint;
- reference-only mode;
- direct transform handles;
- Elytra-target import once the Elytra editor exists;
- final reference-image visual polish.

## Phase 5 — Elytra editor

**Status:** RENDERING FOUNDATION PROVEN / EDITOR NOT STARTED.

Already available underneath:
- independent Elytra texture;
- calibrated thickness rendering;
- high-resolution Elytra project canvas/resizer;
- preview Cape/Elytra switching.

Planned:
- semantic unfolded-wing canvas;
- linked/mirrored wings;
- independent wings;
- cape-to-Elytra starting conversion;
- thickness UI;
- standing/open/gliding preview controls.

## Phase 6 — Animation/effects

**Status:** RUNTIME PROOF EXISTS / AUTHORING MODEL NOT STARTED.

Planned:
- timeline;
- tracks/keyframes;
- pulse;
- scroll;
- hue shift;
- moving gradient;
- sparkle;
- emissive/additive effects;
- performance controls.

Gate:
- layer/effect schema design.

## Phase 7 — Multiplayer library/sharing

**Status:** MULTIPLAYER CONTENT-HASH SYNC IMPLEMENTED / PRODUCT SHARING UI NOT STARTED.

Implemented:
- server/client project validation;
- content-hash cache;
- remote-player sync.

Planned:
- short Loom Codes;
- portable codes;
- private clickable chat result;
- Copy / Import / Preview / Favorite;
- permissions/visibility.

## Phase 8 — Reference-image fidelity

Reference fidelity is no longer treated as something to remember only at the very end.

Every phase should preserve the hierarchy and UX direction of the approved references while functional pieces are built.

A final dedicated polish pass will then unify:
- Home;
- Cape Editor;
- Smart Import;
- Elytra + Animation Editor;
- Loom Codes / Share.

See `REFERENCE_FIDELITY_ROADMAP.md`.

## Phase 9 — Release hardening

- full CI matrix;
- clean dedicated server;
- Sodium/Iris/shader matrix;
- project migrations;
- malformed input tests;
- performance profiling;
- packaging;
- release documentation.
