# Loom Studios — Implementation Roadmap

## Current roadmap audit — 2026-10-03

The phase lists below are historical implementation checkpoints, not a fresh backlog. Current delivery through PR20 includes all drawing/selection/rotation tools, fourteen templates, library folders/tags/bulk/versions, grouping/multi-selection, editor preferences/help, standing/open/gliding previews, draggable keys, Cape/Elytra studios, guided recipes, local portable sharing and premium component migration. UI/runtime verified at all four profiles. This pass finishes direct effect/preset/rate choices and effect-specific scalar labels/help, plus cosmetic-only preview cleanup. Validated at source2107213: Build248 passes128 tests/238 captures; Comparison25 passes16 jobs/138 images. See DROPDOWN_PREVIEW_AUDIT.md.

Remaining release work is target-hardware frame-time/OS/modpack/shader/multiplayer acceptance. Future optional scope: richer schema-versioned effect parameters/reference layers/onion skin, image tint/background removal, and a real hosted resolver/gallery service. Those require their own data-model/service design; historical already-delivered pose/keyframe/import-handle tasks must not be reimplemented. No fake network service or undocumented format change is introduced.

## 2026-10-03 safety/usability milestone (DONE)

All six requested pre-test additions are delivered: explicit unsaved decisions, named recoverable deletion/Undo, tool guidance, recent/design palettes, GUI preview backgrounds and private copyable diagnostics. Exact source `5c6b68d`, Actions #204: 117 tests and 128 actual captures/runtime assertions pass; all images decoded and reviewed against the five reference screens. PR #17 merged at `c8a6b31d0f513a1ca04d47c16614ae77d8b21136`, identical tested tree. Next is manual hardware/OS/modpack/shader/multiplayer acceptance, not further scope expansion. No schema/protocol changes.

## 2026-10-03 authoring milestone (DONE)

Source implemented: design browser/actions/Trash, isolated draft autosave and recovery, favorites/search/sort, six layered templates, local editor preferences and help, semantic-face clipboard/rotation, Elytra drawing parity, Cape/Elytra animation studio with keyframe dragging and per-effect controls, preview camera presets/standing/open/gliding/facing, and direct Smart Import move/scale/rotation handles. Project schema-v3 and multiplayer protocol remain unchanged. Actions #200 at 9b22438db8c8aea42a10ee5800000c56415ac4e7 passes 111 tests, 100 real captures and new library/animation/import input assertions; screenshots reviewed. Hardware/modpack shader and multiplayer acceptance remains a separate release step.

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
- current schema-v3 project model;
- explicit schema-v1 -> v2 -> v3 migration;
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
- bounded contextual workspace with collection-only scrolling.

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

Delivered since this checkpoint: standing/open/gliding controls and animation timeline/effects. See current audit and canonical state for visual evidence.

## Phase 6 — Animation authoring
Status: **MVP IMPLEMENTED / LOCAL VISUAL-RUNTIME VERIFICATION PENDING**

Implemented:
- schema-v3 project animation;
- explicit v1 -> v2 -> v3 migration;
- bounded layer-targeted tracks/keyframes;
- Cape and Elytra channels;
- Pulse / Scroll / Hue Shift / Moving Gradient / Sparkle / Emissive Glow;
- deterministic local runtime evaluation;
- Cape + Elytra animated texture updates;
- authored emissive animation;
- compact Elytra timeline dock;
- play/pause and scrub;
- loop/once, duration and playback speed;
- per-track speed;
- add/select/enable/delete track;
- effect cycling;
- add/remove keyframe;
- scalar keyframe value editing;
- isolated fixed-tick 3D preview;
- layer-reference validation/pruning.

Delivered since this checkpoint: pose controls, draggable keyframes, Cape animation studio and guided recipes. This pass adds effect-specific scalar labels/help and direct effect dropdowns. Richer parameter payloads remain optional schema-versioned work.

## Phase 7 — Multiplayer library/sharing

**Status:** LOCAL/OFFLINE SHARING MVP IMPLEMENTED / HOSTED SERVICE NOT IMPLEMENTED / LOCAL VISUAL VERIFICATION PENDING.

Existing multiplayer infrastructure:
- server/client project validation;
- content-hash cache;
- remote-player sync.

Implemented product sharing:
- deterministic `LS-XXXX-XXXX-XXXX` design fingerprint;
- bounded self-contained `LSP1:` portable project codes;
- clipboard copy/paste;
- imported-project preview;
- `.loom` file import;
- fork-on-import identity safety;
- Import to Library / Import + Open;
- editable `.loom` export;
- portable-code text export;
- Cape/Elytra PNG export;
- Reference-05-oriented local/private sharing screen.

Intentionally not faked:
- hosted short-code resolver;
- public gallery;
- friends/public permission backend;
- clickable chat card service;
- Favorites/collections backend.

A future hosted service may use the design fingerprint or issue a separate resolver code, but offline import remains `LSP1:`.
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
