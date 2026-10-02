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

Implemented in the Gradient/typed-layer UX pass:
- Gradient create/edit controls;
- Linear / Radial;
- editable color stops;
- angle / repeat / dither;
- move / bounded scale / mirror / reset;
- typed Paint / Image / Gradient row icons;
- direct row visibility and lock affordances.

Still required:
- broader shortcut/tooltips surface;
- final reference-image editor composition;
- local visual verification at all required GUI profiles.

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

Implemented UI refinement:
- typed layer row icons;
- selected-row accent;
- direct visibility affordance;
- direct persistent lock affordance;
- fuller Gradient authoring controls.

Remaining:
- richer thumbnails/context actions as the visual shell evolves;
- future effect/reference layer metadata;
- local visual verification.

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

**Status:** SEMANTIC EDITOR MVP CI GREEN / LOCAL VISUAL VERIFICATION PENDING.

Implemented:
- independent Elytra texture;
- high-resolution Elytra project canvas/resizer;
- semantic Left / Right 10x20 wing-front regions;
- unfolded two-wing canvas;
- Pencil / Eraser;
- linked mirrored wing editing;
- independent Separate Wings mode;
- scalable brush;
- 1x / 2x / 4x;
- reusable canvas-backed typed layer list;
- Add / Copy / Delete Paint layers;
- row visibility + persistent Lock;
- Undo / Redo;
- Save / Save + Equip;
- integrated Elytra 3D preview;
- project-authored thickness UI 25%–200%;
- old debug thickness override removed.

Implemented in the workflow-completion pass:
- cape-to-Elytra starting conversion;
- Elytra Smart Import / Image-layer target;
- linked import as two semantic wing Image layers;
- reopening/editing Elytra Image layers;
- layer reorder / opacity / rename / blend;
- shared Swatches parity.

Still planned:
- standing/open/gliding preview controls;
- animation timeline/effects;
- final reference polish.

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

## Home reference shell

**Status:** FIRST REFERENCE-ORIENTED SHELL CI GREEN / LOCAL VISUAL VERIFICATION PENDING.

Implemented:
- responsive three-column shell;
- icon action cards;
- real recent-project thumbnails;
- integrated selected-project 3D preview;
- Blank + Gradient templates;
- Edit Elytra and Smart Import routing;
- status/footer treatment.

Still planned:
- Loom Codes and Settings destinations;
- additional template packs;
- final decorative wood/cloth/metal fidelity.

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
