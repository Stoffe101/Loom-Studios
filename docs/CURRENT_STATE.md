# Loom Studios — Current State

Prototype follow-up verification is pending:32-entry cached template/picker textures, correct selected-tool/tinted icons, per-profile warm CPU samples and actual-client idle-upload reuse assertions at Home/color-picker captures. Initial source71d3d02 build/125 tests passed; the full capture job is still running. Next validate the follow-up source and inspect the eight renderer comparisons before choosing the production renderer.

## NanoVG prototype — IN PROGRESS (2026-10-03)

Implemented an isolated F9 renderer comparison with smooth Inter typography, cached Lucide SVG textures, rounded panels, compact layouts, normal widget input/narration, actual 3D preview interleaving and CPU submission counters. Added LWJGL NanoVG module/native packaging and third-party notices; no Kotlin runtime is needed. Eight smooth/native captures extend the regression suite to190. Build/runtime/image verification is pending; local existing Fabric Loom resolution failed. The main UI overhaul is not done. [Prototype scope and verification](PREMIUM_PROTOTYPE.md).

Next: compile and capture on GitHub Actions, resolve integration failures, compare all four profiles, then choose the renderer and migrate production screens. Hardware FPS/shaders remain unverified.

2026-10-03 follow-up research: NanoVG through NVGRenderer explicitly targets Fabric1.21.11 and is now a rendering-spike candidate for smooth shapes/text/SVGs; Kotlin/native packaging and actual Loom compatibility remain untested. Next compare a small NanoVG prototype with cached Fabric assets before adoption; owo-ui layout evaluation is separate. YACL is settings-oriented and ImGui is developer-tool-oriented. No dependencies/code added or runtime tests run. See PREMIUM_UI_AUDIT.md for primary sources and requirements.

## Premium UI/performance audit — research complete; overhaul TODO (2026-10-03)

Inspected the recovered Actions #217 archive: all182 PNGs decoded, eleven contact sheets reviewed and selected GUI3 originals inspected; latest150225 feedback and all five references viewed individually. The HTTP503 archive-access blocker is resolved. Visual quality is still below the references and performance is unmeasured. No rendering code or dependency changed in this pass. [Audit, primary sources and implementation sequence](PREMIUM_UI_AUDIT.md).

Next: instrument frame-time/render costs, cache template/picker/scenery rendering, then build an asset/icon/font-based Home/Cape vertical slice and responsive640×360 editor. Existing125 tests are prior evidence only; no new game/FPS run this pass. Optional mods, hardware and multiplayer acceptance remain outstanding.

## Library / layer polish — implementation verified; visual acceptance PARTIAL (2026-10-03)

Merged through [PR #18](https://github.com/Stoffe101/Loom-Studios/pull/18) at `7f9800e399e487ae59e5ba0f3b2c24fec6e697fb`, 2026-10-03 13:36 UTC. Merge tree equals the exact tested source tree `01d530366a0f25ff89265eef27ec286c0d545d2f`. Implemented: hover menus with action icons and Equip, favorite-first Designs, Home thumbnails matching catalog artwork/creation, eight new cool/cute motifs (14 total), scenic recent cards with status badges, atomic local folders/tags with search/filter, Ctrl/Shift selection and bulk actions, twenty checksum-verified saved-design snapshots with pre-restore backups, accurate transient save/equip feedback, bounded library artwork reuse, compact eighteen-pixel layer rows, and Cape/Elytra managers with named groups and batch selection/visibility/locking/duplication/reordering/deletion. Oversized batches fail before editing document/history. No portable project or network schema change.

[Actions #217](https://github.com/Stoffe101/Loom-Studios/actions/runs/37125589956), exact source above: both checks pass, Java21/Minecraft1.21.11 build, **125 tests, zero failures/errors/skips**, and **182 real Minecraft/Mesa captures** with visible-widget bounds/nonoverlap at 1920×1080 and 3440×1440 GUI2/GUI3 plus compact task screens. GUI3 dense-layer captures assert at least four complete rows. Actual Screen inline menu/Escape, Home/catalog artwork equivalence, favorite ordering, Ctrl bulk selection, batch hide/undo and Equip pass with prior preview/input/import/animation/library/safety regressions.

All five feedback screenshots (121206, 121353, 121438, 121637, 122234) and five approved references were viewed. Earlier actual Home/template/library and compact dense-layer screenshots were inspected. **The final screenshot archive could not be downloaded/inspected locally because executor/file tools failed with HTTP 503 (environment_status_unavailable). Final image-by-image visual acceptance remains PARTIAL.** The tested revision was merged under the existing authorization for in-game testing; this documentation-only follow-up does not claim subsequent main checks passed. [Evidence, environment and acceptance checklist](verification/library-polish/README.md).

Next: finish final archive inspection once file access recovers, then ordinary-client usability and hardware frame-time testing. Folder/tag/group metadata is local and excluded from exports/backups; groups organize membership without compositing or animation changes. Recovery autosave opts out of automatic version history. Optional Sodium/Sodium Extra/Iris/3D Skin Layers, shaders and live multiplayer remain unverified. No known failing checks on the merged source.

## Editor safety/usability — DONE (2026-10-03)

All six requested additions are implemented and verified: unsaved Save/Keep draft/Discard/Cancel, named delete confirmation + immediate Undo, selected-tool/shortcut/disabled-reason guidance, persistent recent colors and saved design palettes, Scenic/Light/Dark/Checker GUI preview backgrounds, and opt-in privacy-filtered diagnostics. All five original references were viewed again; existing workshop styling and compact layouts are retained. No schema/network change.

Exact source `5c6b68d82e1b878e48872a3ddb881e26056232ae`, [Actions #204](https://github.com/Stoffe101/Loom-Studios/actions/runs/37097790670), passes Java21 build, **117 tests (zero failures/errors/skips)** and **128 actual Minecraft/Mesa captures**. All PNGs decode and were visually reviewed, including all four mandatory profiles and compact 635×320 logical screens. Real Screen unsaved choices/delete Cancel/Undo, palette persistence, background isolation and filtered diagnostics assertions pass alongside existing canvas/3D/animation/import/library/export regressions. [Evidence and manual checklist](verification/editor-safety/README.md).

Merged through [PR #17](https://github.com/Stoffe101/Loom-Studios/pull/17) at `c8a6b31d0f513a1ca04d47c16614ae77d8b21136`, 2026-10-03 04:59 UTC. Merge tree equals the tested source tree. This follow-up records only documentation/evidence; subsequent main push checks may still be running and are not claimed passed. Packaged classes/dependencies verified, JAR SHA-256 `2b48032b4713d1e35c04a7137b183a9dfccc0e929591d646e9c64bf6d9bb25bd`.

Next: ordinary-client/OS clipboard and filesystem-failure acceptance, actual hardware FPS, optional Sodium/Sodium Extra/Iris/3D Skin Layers, shaders off/on and live two-client multiplayer. Software Mesa does not verify those integrations; broader release acceptance remains PARTIAL. No known failing automated checks in this pass.

## Library and authoring milestone — DONE (2026-10-03)

Merged to main through [PR #16](https://github.com/Stoffe101/Loom-Studios/pull/16), merge `ece5ec7a69e727390d61f22f631c7a59dac0e09f`, 2026-10-03 04:14 UTC. Merge tree equals the exact Actions #200 tested tree. This subsequent commit records documentation/evidence only; main push checks may still be running and are not claimed passed.

Implemented and verified source `9b22438db8c8aea42a10ee5800000c56415ac4e7`, [Actions #200](https://github.com/Stoffe101/Loom-Studios/actions/runs/37095425772): Java21 build, **111 tests (zero failures/errors/skips)** and **100 actual Minecraft/Mesa screenshots**. Covers 1920×1080 and 3440×1440 at GUI2/GUI3, plus compact 635×320 logical library/action screens and the earlier windowed editor checks. Screenshots decode and were visually reviewed against all five approved references; visible controls pass bounds/nonoverlap checks. [Evidence and test handoff](verification/library-authoring/README.md).

Features: Browse All with single-click preview/double-click selection and editing; right-click Edit/Rename/Favorite/Duplicate/Delete to recoverable Trash; Designs/Drafts/Trash tabs; search, latest/name sort, favorites and card pagination. Thirty-second autosave/navigation/exit draft checkpoints are separate from saved/equipped designs; explicit Save clears recovery. Six original layered templates, editor preferences/help, Cape/Elytra pixel copy/paste/flip/rotate, Elytra pencil/eraser/fill/eyedropper/select/line/rectangle/circle parity, shared animation studio with per-effect controls/presets and draggable keys, camera presets/standing/open/gliding/facing, and direct import move/scale/rotate handles are included. No project schema/network change or hosted service.

Actual input/workflow assertions pass: Screen right-click, delete, Trash restore, double-click editor selection, isolated recovery, explicit save cleanup, animation-key drag/single-step undo, import artwork drag/candidate changes; earlier canvas/3D pan, palette close, locked tools, cache reuse/alpha isolation and editable/portable/PNG/import equivalence remain green. Gliding centering was corrected after visual review. Native Minecraft fonts, code-native artwork and task screens are intentional reference adaptations.

Next: interactive hardware/modpack testing of the implemented features, optional Sodium/Iris/Sodium Extra/3D Skin Layers, shaders off/on and live two-client multiplayer. Software Mesa does not establish hardware FPS or those integrations. There are no known failing automated checks in this milestone. Broader release acceptance remains PARTIAL until those external scenarios are exercised.

## User-feedback pass — DONE (2026-10-03)

All nine feedback screenshots and five approved references were inspected. Implemented clearer native icons/labels, one selected outline, consistent panel/frame clearance, Circle/ellipse outline/filled mode, palette close/Escape, functional windowed GUI3 down to 600×320 logical pixels, rear-facing visible Cape/Elytra previews with a labeled preview-only transparency guide, expand/full preview, and Screen-level middle pan on both canvases and all five inline/expanded 3D previews. Immutable hash and separate skin-patch caches remove repeated preview work; fixed-tick animation updates only changed animated channels. Circle follows paint-layer/lock eligibility.

Verified source `b7f7212388858109b96f3e8fc8a7175bc3ef8c45`: [Actions #191](https://github.com/Stoffe101/Loom-Studios/actions/runs/37090332364) passed Java21 build and **103 tests (0 failures/errors/skips)** plus **68 fresh actual Minecraft captures** at 2026-10-03 02:40 UTC. Environment: Minecraft 1.21.11, Fabric Loader 0.18.4, Fabric API 0.141.1+1.21.11, Temurin21, software Mesa/Xvfb. All screenshots decode and are reviewed in contact sheets, with critical previews/compact controls inspected at full size. [Verification index](verification/editor-feedback/README.md) and [fourteen-image analysis](verification/editor-feedback/ANALYSIS.md). Runtime assertions cover canvas/3D/expanded middle pan, palette close, locked Circle, independent snapshots, twenty interleaved world/preview frames with hash/skin reuse, both-channel alpha isolation, and export/import equivalence.

Delivery: **merged to main** through [PR #15](https://github.com/Stoffe101/Loom-Studios/pull/15), merge `4618352e4a38dd8d2faf5ee464eecc7cca56d543`, 2026-10-03 02:55 UTC. Pinned PR head `f62a6e9b62366eef6bf02d2e875b4aac2915328f` passed both jobs in [Actions #192](https://github.com/Stoffe101/Loom-Studios/actions/runs/37091132764): 103 tests and all 68 captures/input/preview/workflow checks. Main merge tree equals that tested head; source matches #191. Main push [Actions #193](https://github.com/Stoffe101/Loom-Studios/actions/runs/37091440293) was in progress at handoff and is not claimed passed. This integration note is a documentation-only follow-up. Compiled mod SHA-256: `d80bde04724ff827987a699c97ad1aa47b5fdec37d3248224e40e71d2ecbcbea`. Next: interactive testing on the user's hardware/modpack, including optional Sodium/Iris/shaders and multiplayer. Those integrations and absolute FPS are not claimed by the Mesa smoke run. The prior workshop acceptance below is historical and superseded by this feedback pass.

## Completed workshop pass

**DONE — requested five-screen workshop styling and four-profile alignment acceptance.** All five approved references were visually inspected. Shared timber/steel framing, lanterns, stitched pennants, cyan/violet branding, navy panels, moonlit live previews and parchment footer are implemented. All 42 captures were decoded and inspected; visible controls pass window/footer bounds and pairwise nonoverlap checks. Native Minecraft fonts, code-native pixel art and compact paged inspectors are intentional adaptations.

Verified source `58491bc8855f854f2f1902e32f1720cb3e5e3274`, tested merge `3de85d41980e1d28d78e429749147031a41766d1`: [Actions #182](https://github.com/Stoffe101/Loom-Studios/actions/runs/37073091623) passed build/tests and all 42 fresh actual Minecraft captures on 2026-10-02 at 22:36 UTC. Screenshots: [verification index](verification/editor-workspace/README.md). [PR #14](https://github.com/Stoffe101/Loom-Studios/pull/14) was merged into `main` on 2026-10-03 at `1e2bc6d2e800e4217d9243e21e61f00a44966a2a` after the user explicitly authorized the merge. Verified PR head `356d063028eb7eef392fff3d1d8e7adc9d4d4c83` passed both jobs in [Actions #183](https://github.com/Stoffe101/Loom-Studios/actions/runs/37074274210); all 42 captures and export/import workflow markers passed again.

## Prior workshop main test handoff (historical)

**DONE — the verified UI implementation and screenshot evidence are on `main`.** Merge parent/head were checked before merging; the expected PR head was pinned in the merge request. No additional source/schema/network changes were made for this handoff. The documentation follow-up records integration and replaces the previous review-only next step. Main push CI runs the same clean build and 42-capture workflow; current results are attached to each exact main commit in GitHub Actions. Next: the user performs the full interactive/optional-mod/shader/multiplayer test pass from `main`.

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

The editor workspace repair and subsequent workshop styling are **DONE for the requested UI scope**, verified by Actions #182 above. Cape/Elytra share integer pixel boundaries, bounded workspace regions, contextual controls, cached textures, paged inspectors and adaptive timelines. Home, Smart Import and Share have been inspected against their approved references at all four profiles. Project format and network protocol are unchanged. Optional-mod/shader/multiplayer compatibility and exhaustive manual feature checks remain separate release work.

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

Historical initial UI-refactor implementation checkpoint: `db90568ad8e47b342e3be36419381f4e2c37af66` — GitHub Actions #172 **SUCCESS**.

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
- separate future preview-state UX enhancements; workshop style/profile acceptance is complete.

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

Home visual/runtime verification at all four mandatory GUI profiles passed in Actions #182; see the completed workshop pass above.

## Smart Import

**Functional implementation and four-profile visual verification PASS; both import pages also pass candidate/apply pixel checks in Actions #182.**

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

**Functional schema/runtime/timeline MVP is CI green; compact Animation/Playback and long-track captures pass in Actions #182. Exhaustive interactive animation checks remain separate.**

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

**Local/offline sharing MVP is CI green; Export/Import pass four-profile visual checks, project/portable round-trips and both PNG exports in Actions #182. OS file-picker/clipboard interaction remains a manual release check.**

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
