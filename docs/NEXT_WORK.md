# Loom Studios — Next Work

## Current gate

Home + semantic Elytra Editor productization is merged, and the Elytra workflow-completion implementation is CI green.

Latest merged-main baseline entering the workflow-completion pass:
- `52576d191f451b49436a97f66b4501b2bc85eb13`
- GitHub Actions #133: **SUCCESS**

Current Elytra workflow-completion source checkpoint:
- `cc55df2ba4b52907595a1d7d7cb7c48849059692`
- GitHub Actions #137: **SUCCESS**

Current pass source checkpoints:
- `0cc3b219fcda2c8b4fb619db900545890e40764e` — Home shell — Actions #127 **SUCCESS**;
- `fb21521181dae6139122b8e0e4891e4083627203` — semantic Elytra Editor — Actions #128 **SUCCESS**;
- `af767ce8c12a55c725d5b14fc2602872515b5564` — Elytra layer stack — Actions #129 **SUCCESS**;
- `e46960484bd4d8ef7ef308805a0ad449ba67c643` — project-authored thickness — Actions #130 **SUCCESS**.

The remaining gate for visual DONE status is local runtime verification because the user is away from the development PC.

## Runtime verification queue

### Select / transforms
- drag selection at 1x / 2x / 4x;
- persistent outline alignment;
- Move Left / Right / Up / Down;
- arrow-key nudge;
- Flip H / Flip V;
- correct behavior after zoom/pan;
- selection clears on face/resolution/history changes;
- tool-rail layout at all required GUI profiles.

### Layers / effects
- New Paint / New Gradient / Import Image;
- Duplicate / Delete;
- visibility;
- reorder;
- opacity;
- rename;
- persistent Lock;
- direct row lock toggle;
- typed Paint / Image / Gradient icons;
- selected-row accent;
- Emissive;
- Normal / Add-Glow / Screen / Multiply / Overlay;
- Undo/Redo;
- 3D Preview compositing;
- emissive visual output with shaders OFF/ON.

### Gradient authoring
- Linear / Radial;
- start/end and additional stop colors;
- add/remove stops;
- stop position stepping;
- angle +/-;
- move left/up/down/right;
- scale +/-;
- Mirror H / Mirror V;
- reset transform;
- repeat;
- dither;
- locked Gradient disables authoring controls;
- all controls remain usable at required GUI profiles.

### Smart Import
- file picker opens/returns correctly on Windows;
- PNGs with/without alpha;
- large/odd/small PNGs;
- Original / Processed / Cape Texture preview layout;
- 3D candidate preview;
- Fit / Stretch / Crop / Center;
- Keep Aspect;
- move / scale / rotate / mirror;
- Direct / Pixel Art / Outline / Monochrome / Palette Limited / Posterize;
- Brightness / Contrast / Saturation;
- Reduce Colors;
- Dither;
- selected Swatches palette;
- Apply new Image layer;
- reopen/edit Image layer;
- save/reopen schema-v2 project;
- v1 project load -> v2 migration;
- Undo/Redo after applying;
- no equipped/network mutation until Save + Equip.

### Swatches / color
- compact 1920x1080 GUI 3 footprint;
- no Saved palettes overlap;
- Edit / Done collapse;
- multiple palette groups;
- move + Pin;
- editable Hex/R/G/B/A;
- alpha slider;
- semi-transparent paint;
- alpha-aware palette import/export.

### Home dashboard
- three-column layout at all four GUI profiles;
- no action-card text clipping;
- real project thumbnail load/release;
- selected-card accent;
- selected project updates 3D preview;
- drag/zoom/reset preview interaction;
- Blank template;
- Gradient template;
- Import Image -> Smart Import;
- Edit Elytra routing;
- empty-library state;
- unreadable-project warning;
- disabled placeholder cards remain visibly secondary.

### Elytra Editor
- Left/Right wing orientation matches rendered Elytra;
- linked mirror paints expected opposite visual wing;
- Separate Wings edits only the clicked wing;
- Pencil / Eraser at 1x / 2x / 4x;
- brush size;
- layer Add / Copy / Delete;
- row visibility;
- direct row Lock;
- reorder;
- opacity;
- rename;
- blend mode;
- locked Paint/Image layer rejects edits;
- Undo / Redo;
- Save / Save + Equip;
- Depth 25% through 200%;
- saved/equipped depth matches preview;
- old debug V-key thickness override is gone;
- 3D Elytra preview drag/zoom/reset;
- Cape -> Wings conversion;
- linked Smart Import creates two editable wing Image layers;
- existing Elytra Image layer reopens in Smart Import;
- shared Swatches/palette window;
- compact layout at all mandatory GUI profiles.

## Next CI-safe product work

Home reference shell and the semantic Elytra Editor MVP are implemented in this pass; local visual verification remains pending.

Priority order:

1. animation authoring schema + UI:
   - tracks;
   - keyframes;
   - procedural effects;
   - timeline;
   - play / pause / loop / speed;
2. Elytra preview-state polish:
   - standing;
   - open;
   - gliding;
3. project Loom Codes/sharing;
4. final Cape/Home/Smart Import/Elytra reference-fidelity hardening;
5. final compatibility/performance matrix.

## Schema-v2 status

Implemented:
- explicit v1 -> v2 migration;
- stable layer-kind identifiers;
- stable schema-v2 blend identifiers;
- Paint layer payload;
- Image layer payload;
- Gradient layer payload;
- persistent layer lock;
- normalized transforms;
- embedded bounded image source;
- persistent processing settings.

Future schema work should extend v2 deliberately for:
- animation targeting;
- effect/reference layer metadata;
- external/content-addressed assets if ever needed;
- portable project sharing.

Do not introduce new byte-layout meaning without explicit migration/versioning.

## Crop clarification

Minecraft cape/Elytra UV dimensions remain fixed.

Smart Import Crop means a centered source crop that fills the fixed target area. It never resizes the semantic Minecraft cape face itself.

## Reference-image priority

The refreshed five-screen reference set under `docs/references/ui/` is the active visual contract.

Implementation should follow the refreshed direction rather than reproduce screenshots literally: roughly 70% clean modern editor / 30% magical Minecraft workshop, icon-led controls, restrained glow, fewer nested borders, showcase-oriented Home/Sharing, and calmer canvas-first work screens.

The current Smart Import implementation is functionally complete but still needs local visual comparison against `Loom_Studios_03_Smart_Import.webp` before final visual DONE status.

See `REFERENCE_FIDELITY_ROADMAP.md`.

## Required UI profiles

- 1920x1080 GUI x2;
- 1920x1080 GUI x3;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.

## Documentation cleanup rule

When a task becomes implemented:
- remove it from active TODO lists;
- move verification-only work into the runtime verification queue;
- keep historical implementation detail in `PASS_LOG.md`;
- keep `CURRENT_STATE.md` focused on what is true now;
- explicitly mark superseded decisions in `DECISIONS.md` rather than leaving contradictory active guidance.
