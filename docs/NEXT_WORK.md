# Loom Studios — Next Work

2026-10-03 milestone verification: commit `5aa2ad043b40f0d06c9392650212dc0c8dd275db` passed build and the existing 68 real Minecraft/Mesa captures (Actions #196). Expanded new-screen capture and real library interaction assertions are now being added; this milestone remains IN PROGRESS until those checks pass and screenshots are reviewed.


## Library and authoring milestone — IN PROGRESS (2026-10-03)

User authorized the complete follow-up feature milestone plus Browse All, right-click Edit/Rename/Delete and double-click editing. All five attached reference originals were viewed individually again. Home reference 03 drives card browsing/actions; Elytra reference 01 drives drawing/animation parity; Cape 05 and Import 04 drive contextual transforms. Preserve workshop framing and bounded GUI3 controls.

Current branch: `codex/library-authoring-milestone`, base `a2996e47be8f1ed4a5bb0766d01027185eb9f3d7`. Implemented source pending verification: paged library with search/latest/name sorting, favorites, duplicate/rename, reversible Trash/restore; separate 30-second recovery drafts and Settings; standing/open/gliding preview controls; shared Cape/Elytra animation workspace and draggable keyframes; Elytra drawing tools and both-channel internal pixel copy/paste/rotation. None of this milestone is marked DONE until build/runtime/visual checks pass.

First CI checkpoint `638a5b552b4047e8fae616ab6fd54edd029ae8d7`, Actions #195, failed compilation: three Cape clipboard feedback references used an absent statusMessage field. Corrected to the existing player notification method. Preview pose fields and the other new classes compiled. Templates and direct import handles are now implemented source, still awaiting exact-SHA checks.

Next in this active pass: complete useful template packs and import handles/processing conveniences, add runtime interaction/layout evidence, run exact-SHA CI and release compatibility checks, correct failures and update documentation. Real hardware shader/FPS and two-user multiplayer results remain separately identified until actually exercised.

## User-feedback pass — DONE (2026-10-03)

All nine feedback screenshots and five approved references were inspected. Implemented clearer native icons/labels, one selected outline, consistent panel/frame clearance, Circle/ellipse outline/filled mode, palette close/Escape, functional windowed GUI3 down to 600×320 logical pixels, rear-facing visible Cape/Elytra previews with a labeled preview-only transparency guide, expand/full preview, and Screen-level middle pan on both canvases and all five inline/expanded 3D previews. Immutable hash and separate skin-patch caches remove repeated preview work; fixed-tick animation updates only changed animated channels. Circle follows paint-layer/lock eligibility.

Verified source `b7f7212388858109b96f3e8fc8a7175bc3ef8c45`: [Actions #191](https://github.com/Stoffe101/Loom-Studios/actions/runs/37090332364) passed Java21 build and **103 tests (0 failures/errors/skips)** plus **68 fresh actual Minecraft captures** at 2026-10-03 02:40 UTC. Environment: Minecraft 1.21.11, Fabric Loader 0.18.4, Fabric API 0.141.1+1.21.11, Temurin21, software Mesa/Xvfb. All screenshots decode and are reviewed in contact sheets, with critical previews/compact controls inspected at full size. [Verification index](verification/editor-feedback/README.md) and [fourteen-image analysis](verification/editor-feedback/ANALYSIS.md). Runtime assertions cover canvas/3D/expanded middle pan, palette close, locked Circle, independent snapshots, twenty interleaved world/preview frames with hash/skin reuse, both-channel alpha isolation, and export/import equivalence.

Delivery: **merged to main** through [PR #15](https://github.com/Stoffe101/Loom-Studios/pull/15), merge `4618352e4a38dd8d2faf5ee464eecc7cca56d543`, 2026-10-03 02:55 UTC. Pinned PR head `f62a6e9b62366eef6bf02d2e875b4aac2915328f` passed both jobs in [Actions #192](https://github.com/Stoffe101/Loom-Studios/actions/runs/37091132764): 103 tests and all 68 captures/input/preview/workflow checks. Main merge tree equals that tested head; source matches #191. Main push [Actions #193](https://github.com/Stoffe101/Loom-Studios/actions/runs/37091440293) was in progress at handoff and is not claimed passed. This integration note is a documentation-only follow-up. Compiled mod SHA-256: `d80bde04724ff827987a699c97ad1aa47b5fdec37d3248224e40e71d2ecbcbea`. Next: interactive testing on the user's hardware/modpack, including optional Sodium/Iris/shaders and multiplayer. Those integrations and absolute FPS are not claimed by the Mesa smoke run. The prior workshop acceptance below is historical and superseded by this feedback pass.

## Completed workshop pass

**DONE — requested five-screen workshop styling and four-profile alignment acceptance.** All five approved references were visually inspected. Shared timber/steel framing, lanterns, stitched pennants, cyan/violet branding, navy panels, moonlit live previews and parchment footer are implemented. All 42 captures were decoded and inspected; visible controls pass window/footer bounds and pairwise nonoverlap checks. Native Minecraft fonts, code-native pixel art and compact paged inspectors are intentional adaptations.

Verified source `58491bc8855f854f2f1902e32f1720cb3e5e3274`, tested merge `3de85d41980e1d28d78e429749147031a41766d1`: [Actions #182](https://github.com/Stoffe101/Loom-Studios/actions/runs/37073091623) passed build/tests and all 42 fresh actual Minecraft captures on 2026-10-02 at 22:36 UTC. Screenshots: [verification index](verification/editor-workspace/README.md). [PR #14](https://github.com/Stoffe101/Loom-Studios/pull/14) was merged into `main` on 2026-10-03 at `1e2bc6d2e800e4217d9243e21e61f00a44966a2a` after the user explicitly authorized the merge. Verified PR head `356d063028eb7eef392fff3d1d8e7adc9d4d4c83` passed both jobs in [Actions #183](https://github.com/Stoffe101/Loom-Studios/actions/runs/37074274210); all 42 captures and export/import workflow markers passed again.

## Next concrete work

Test the integrated feedback build from main: first windowed 1920×1080 GUI3, then the four full-screen resolution/scale profiles. Exercise Circle outline/filled/undo and locked/gradient-layer eligibility, middle pan on texture and 3D views, palette close/pin/drag, alpha-guide versus exported transparency, expanded preview, and normal Save/Equip/import/export workflows. Check the actual modpack/shaders/FPS and multiplayer separately.

Reproduce the automated smoke run with `./gradlew runClient -PuiCapture`: 68 real screenshots, frame/footer bounds and nonoverlap (excluding intentional floating palettes), project/portable/PNG exports, import candidate/apply equivalence, routed input and preview isolation/cache assertions. Packaged/ordinary launches never run this harness.

## Full in-game test from main

Update your checkout with `git switch main` then `git pull --ff-only origin main`; launch the normal Gradle/IntelliJ client (`./gradlew runClient`, without `-PuiCapture`). A packaged main build is available through the GitHub Actions `loom-studios-dev` artifact.

- Home and all work screens at 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3.
- Cape Paint/Erase/Fill/Select, move/flip, zoom/pan/grid, 1x/2x/4x, Layers/Color/Gradient properties, undo/redo, Save and Save + Equip.
- Elytra Linked/Separate wings, thickness, layers, timeline/effects/keyframes, playback and equipped preview.
- Smart Import file picker, Placement/Processing adjustments, candidate preview, Apply, save/reopen and Image layer editing.
- Share editable/portable/PNG exports, clipboard/file imports, preview and import-to-library/open.
- Sodium/Sodium Extra/Iris/3D Skin Layers, shaders off/on, and a second multiplayer client.

## Runtime verification queue

The five-screen reference smoke pass is complete at 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3. Retained evidence includes compact Color/Properties/Gradient/Transform/Stops/Animation/Playback, long collections, one-pixel live/released selection and below-minimum guidance. The following queue is for deeper manual release testing.

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