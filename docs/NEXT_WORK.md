# Loom Studios — Next Work

## Current gate

The workspace repair follows the failed cape/elytra screenshots from 2026-10-02 and all five approved workshop references. The clean build and 22 actual editor screenshots have passed and been inspected. Overall reference acceptance remains PARTIAL: the current editor is clearer but still simpler than the decorative workshop references.

Current changes: unified canvas pixel boundaries, bounded shared layout, slim navigation with explicit Save + Equip, contextual tool rows, paged layer/gradient inspectors, cached two-wing canvas, adaptive timeline, real layer thumbnails, draggable collection scrollbars, preserved viewport state and coherent icon/theme primitives.

Run the development capture flow with `./gradlew runClient -PuiCapture` in the isolated `run-ui-capture` directory. It creates a flat test world, checks visible widget bounds and captures Cape/Elytra at the four mandatory profiles plus compact Color/Properties/Animation/Playback pages. It is disabled in production and ordinary development launches.

Verified source `a2a9628afced367d4a178f944c5cce239d110fbf` passed Actions #178 (tested merge `ed4b04f900dc200eb43fa6074fecd5d6a832e084`). Next concrete gate: complete the decorative workshop styling and review Home/Smart Import/Share against their references. Keep shader, optional-mod, multiplayer and full manual workflow checks pending unless performed. The older checkpoint SHAs below are historical, not proof for this repair.

## Runtime verification queue

### Reference UI refactor smoke pass
Run this before the deeper feature checklist:
- 1920x1080 / GUI scale 3 first;
- Home has no text overlap and primary actions are readable;
- Cape Editor shows canvas + tool rail + one inspector context without a giant vertical control scroll;
- Color tab is reachable without covering Layers;
- Gradient Properties only appears when relevant;
- Elytra Editor keeps canvas, timeline and 3D preview simultaneously understandable;
- Animation tab explains the flow: select layer -> + Track -> scrub -> Add Key -> Value/Effect;
- compact timeline rows do not overlap header/footer;
- Smart Import switches Placement / Processing without scrolling through both at once;
- Smart Import Apply remains visible regardless of tab;
- Share / Export is reachable from both editors;
- Export Project / Cape PNG / Elytra PNG are immediately visible in Export;
- Import controls are hidden while Export is active and vice versa;
- no inactive Settings placeholder on Home;
- no primary UI text overlap at any required GUI profile.

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
- save/reopen current schema-v3 project;
- v1 project load -> v2 -> v3 migration;
- v2 project load -> v3 migration;
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
- no dead Settings card occupies primary navigation; future template packs remain secondary.

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

### Animation timeline
- schema-v1 file migrates through v2 to v3 with an empty timeline;
- schema-v2 file migrates to v3 without losing typed layers;
- schema-v3 save/reopen preserves tracks/keyframes;
- Add Track targets the selected Elytra layer;
- track enable/disable;
- effect cycling;
- Add/Remove Keyframe;
- keyframe scalar Value +/-;
- track speed cycle;
- timeline duration +/-;
- project playback speed +/-;
- timeline Loop/Once;
- Play/Pause and scrub;
- animation row scrolling with many tracks;
- layer deletion removes its tracks;
- Undo/Redo around animation edits;
- fixed-tick 3D preview matches the scrubbed timeline position;
- preview scrubbing does not equip/publish the dirty project;
- Pulse / Scroll / Hue Shift / Moving Gradient / Sparkle / Emissive Glow visually;
- Elytra animation output at 1x / 2x / 4x;
- compact timeline at all four mandatory GUI profiles.
### Share / Export / Loom Codes
- Home Share / Export card opens the sharing screen;
- short `LS-XXXX-XXXX-XXXX` fingerprint is stable for unchanged content;
- Copy Design ID;
- Copy Portable Code;
- Paste Portable Code from clipboard;
- malformed/wrong-prefix portable code rejection;
- `.loom` file picker import;
- preview imported project before accepting;
- toggle Cape / Elytra preview;
- Import to Library forks to a new project UUID;
- Import + Open enters the imported project;
- imported animation/layers remain intact;
- Export Project writes editable `.loom`;
- Save Portable Code writes `.txt`;
- Export Cape PNG;
- Export Elytra PNG;
- repeated exports receive unique filenames rather than overwriting;
- exports stay under `.minecraft/loom-studios/exports`;
- local/private wording does not imply a hosted backend;
- layout remains usable at all four mandatory GUI profiles.
## Next CI-safe product work

Do **not** start another broad feature milestone before the new shell passes local UI verification.

Priority order:

1. local reference-UI re-test and defect fixes;
2. Elytra preview-state polish:
   - standing;
   - open;
   - gliding;
3. Cape animation exposure/refinement only if the contextual UX remains clean;
4. Settings/product-preferences decision and implementation if retained;
5. final compatibility/performance matrix.

Loom Codes local/offline sharing is implemented. A hosted short-code/gallery/friends/public service remains optional future work and must not be represented as active until a real backend exists.

## Schema-v3 status

Implemented:
- explicit v1 -> v2 -> v3 migration;
- all schema-v2 typed layer data unchanged;
- Paint / Image / Gradient payloads;
- persistent layer lock;
- normalized transforms;
- embedded bounded image source;
- persistent processing settings;
- project animation duration / loop / playback speed;
- bounded animation tracks;
- Cape/Elytra channel;
- stable effect id;
- target layer UUID;
- per-track enabled/speed/loop;
- ordered keyframes.

Current animation bounds:
- up to 64 tracks per project;
- up to 128 keyframes per track;
- 20..7200 tick project duration;
- 0.25x..4.0x project playback speed;
- 0.1x..8.0x track speed;
- bounded scalar keyframe values.

Future schema changes still require explicit versioning/migration for:
- richer effect-specific parameter payloads;
- reference/effect layer metadata;
- external/content-addressed assets if ever needed;
- portable project sharing.
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
