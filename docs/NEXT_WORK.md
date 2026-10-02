# Loom Studios — Next Work

## Current gate

The Smart Import + schema-v2 milestone is functionally implemented, merged and CI green on `main`.

Exact merged-main checkpoint:
- `6208086fd5c6f025376afd7cf8390829cda56dbd`
- GitHub Actions #121: **SUCCESS**

The remaining gate is local visual/runtime verification because the user is away from the development PC.

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

## Next CI-safe product work

Gradient authoring and typed-layer row UX are implemented in the current pass; local visual verification remains pending.

Priority order:

1. Home reference shell:
   - real Recent Project thumbnail cards;
   - selected-project/player preview;
   - Create New Cape hierarchy;
   - Import Image routed to Smart Import;
   - Edit Elytra placeholder/entry once editor exists;
   - Templates / Loom Codes / Settings hierarchy;
   - calmer 70/30 reference styling with icon-led actions;
2. Elytra semantic editor model:
   - unfolded left/right wings;
   - linked/mirrored vs independent;
   - cape-to-Elytra starting conversion;
   - thickness user control;
   - standing/open/gliding preview states;
3. animation authoring schema:
   - tracks;
   - keyframes;
   - procedural effects;
   - timeline;
4. project Loom Codes/sharing;
5. final reference-fidelity and compatibility hardening.

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
