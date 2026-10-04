## Large-project encoding performance checkpoint — IN PROGRESS

Source829850eae3b0105ad0e69c30c4a005475c58fb3c: Build254 build passes151 tests; Comparison30 passes all20 jobs, including focused authoring Screen/GIF/input and3 MiB live networking. Full278 capture still running.

Added32 MiB/64-entry layer block LRU with immutable identity + canvas-dimension keys; budget counts retained artwork and packed bytes. Warm48-layer edits recompress only the changed layer, while raw expansion and envelope budgets still apply. A new regression checks reuse, changed artwork, resize and cache bounds. Set Gradle test heap to384 MiB to make the large-project memory check repeatable in official CI. Local compile first caught a duplicate variable name, corrected before publication; the temporary Java/tool cache was reset before the follow-up result could be recovered. New152-test official result remains pending. Updated README/current format/network/animation/import usage; next: exact-source official test/runtime acceptance, final evidence and merge.

## Compact import preview correction — IN PROGRESS

Full Build252 screenshot job at98d39898b406c2880495c8afce9ce46062a48bda failed at the635×320 logical window import drag assertion. New Processing rows had reduced the preview to32px including its24px header; its middle hit landed on the title. Compact Processing now routes brightness/contrast/saturation/background/tint/swatches into the dedicated modal instead of duplicating those rows. Restore the180px compact panel while keeping242px for full controls. Placement preview has usable drag space again. All20 Comparison28 jobs still pass; focused authoring assertions and exact revised-source full capture remain pending.

## 2026-10-04 — Runtime integration checkpoint

**IN PROGRESS**, source98d39898b406c2880495c8afce9ce46062a48bda. Official Build252 build passes151 tests. Comparison28 passes all20 jobs and178 actual captures. All40 new authoring originals decoded and four profile contact sheets reviewed against approved references01/04/05. Outer wing artwork now renders correctly; compact tabs, sizes, mask workspace, timeline ruler and8× canvases are aligned. Real3,149,342-byte C2S/S2C codec and integrated Fabric fragmentation/async validation/equip/client decode checks pass. Full278 capture suite is still running.

Added a focused opt-in verification class for actual stamp drag/single undo, Wand/Replace actions, mask flags/hide brush, parent return, background sample/Create Swatches and uploaded GIF frame changes on Cape and Elytra. Local client compilation passes; these new runtime assertions remain pending. No product changes in this checkpoint. Next: verify this harness, complete the full capture, review Processing regressions, record final evidence and merge.

## Current authoring-v4 pass

1. Official build at096bdddc passes; publish the corrected fixture, Processing bounds, physical wing face mapping and bounded asynchronous network paths, then verify that exact head.
2. Capture new surfaces, background/tint, typed parameters and 8× Cape/Elytra on all four required profiles. Exercise real controls, clipboard/history and wing geometry.
3. Verify large protocol-v2 payload codec/integrated server paths, cache eviction and malformed input. Record limits and remaining multiplayer soak coverage honestly.
4. Review captures against references 01/04/05, correct any bounds/overlaps, run official CI, update documentation to exact SHAs and merge the verified change.

# Loom Studios — Next Work

## 2026-10-03 — Dropdowns and cosmetic-only preview — DONE: Linux runtime and visual verification

Exact runtime source `21072136702cd2b2921c48ed20cab146b83c5dae`, accepted 2026-10-03 22:32 UTC. [Build248](https://github.com/Stoffe101/Loom-Studios/actions/runs/37157928246) passes both jobs: **128 tests, zero failures/errors/skips**, and **238 actual Minecraft captures**, all original PNGs downloaded and decoded locally. [Comparison25](https://github.com/Stoffe101/Loom-Studios/actions/runs/37157928161) passes all16 jobs and decodes138 screenshots, including20 new feedback views. MC1.21.11, Loader0.18.4, Fabric API0.141.1, Temurin21, Linux/Mesa/Xvfb; no optional mods/shaders.

Implemented: bounded direct effect/preset/rate dropdowns with descriptions, smooth font, hover/focus, narration, keyboard selection, wheel scrolling and Escape/outside cancellation; resize dismissal; effect-specific scalar labels/help; six-pixel Home card/Browse/Help gutters; held-item-free isolated previews and hidden-character attachment cleanup while retaining Cape/Elytra. Real inventory and project/export/network formats remain unchanged. Reviewed both feedback crops and all five approved references.

Visual acceptance: all four new profile contact sheets reviewed at1920×1080 and3440×1440 GUI2/3; original compact GUI3 dropdown confirms smooth text, row gutters and complete effect descriptions. Compact library/group/layer/menu/template and production import/export contact sheets reviewed for regressions. Full run passes dropdown bounds/cancel/keyboard choice, clean hands/armor, unchanged real inventory, existing painting/preview/input/animation/library/safety/import/export assertions and nine idle cache probes. Cache reuse is not hardware FPS evidence.

Corrected before acceptance: constructor-time Fabric event registration failed at69d509d; inherited reflection lookup failed atd2376f4; full-size review atd3140b5 found pixel-font fallback. Popup collection now occurs inside PremiumControls.finish before its snapshot because deferred tooltips can flush earlier than Fabric afterRender. See DROPDOWN_PREVIEW_AUDIT.md and DECISIONS.md. No known failing automated checks on accepted runtime source.

Artifacts: Build248 loom-editor-screenshots11286493023 and loom-studios-dev11286645374; Comparison25 loom-premium-* suites. Packaged classes verified; release JAR SHA256 `613af13bdebda86e5fb3bc1ea8967169f55467f9f9c61b429aa7f7562be8a3ba`. This acceptance commit changes documentation only; evidence belongs to the exact runtime SHA above.

Docs audit distinguishes delivered historical TODOs from optional richer effect schemas/onion skin/reference layers/image processing/hosted services. Next release work: ordinary-client frame-time and interaction testing on target hardware, repeated open/close/resource reload, Windows/macOS native loading, Sodium/Iris/shaders and live equipped multiplayer. Those checks remain manual.

## 2026-10-03 — Usability and animation follow-up — DONE: Linux runtime and visual verification

Exact runtime source `b447e7add4cf12ded65d477ad4f5c01bfc95c6fe`, accepted 2026-10-03 21:08 UTC. [Build #241](https://github.com/Stoffe101/Loom-Studios/actions/runs/37153029252) passes both jobs: **128 tests, zero failures/errors/skips**, and **218 actual Minecraft screenshots**. All original PNGs downloaded and decoded locally. [Comparison #19](https://github.com/Stoffe101/Loom-Studios/actions/runs/37153029222) passes all twelve jobs and decodes **118 screenshots**, including 28 feedback-specific usability views. MC1.21.11, Loader0.18.4, Fabric API0.141.1, Temurin Java21, Linux/Mesa/Xvfb; no Sodium/Iris/shaders.

Reviewed all seven new feedback screenshots, all five approved references and saved runtime artifact screenshots. Implemented proportional workshop decorations/sign and aligned 48/72px brand header; inset Home preview actions/View All and padded export code; shared context-menu gutters/hit targets; scissor-aware deferred smooth controls; player-head Toggle character (H) with isolated preview snapshots; seven guided editable animation presets/rates and bounded advanced controls; cached fullbright Elytra Glow masks/pass. Adaptive Settings rows correct a compact diagnostics-button overflow found by the full run. No project/export/network schema change.

Visual acceptance inspected usability profile contact sheets at 1920×1080 and3440×1440 GUI2/3, original compact swatches and advanced animation PNGs, production Home/import/export, dense layer/group/menu screens and final compact Settings/palette/unsaved/delete-undo captures. Four complete dense layer rows remain visible at1920×1080 GUI3. Header details preserve source proportions; compact previews reserve editing space rather than exactly copy reference proportions. Native text inputs/tooltips and authored pixel artwork intentionally retain Minecraft rendering.

Actual Screen assertions pass: character click/state isolation, cosmetic retention, canvas/preview/expanded middle pan, preset application/single undo, key dragging, menu gutters and animation bounds, animated Elytra emissive masks, imports/exports, library actions/recovery, unsaved safety, favorite ordering and bulk operations. Nine idle overlay cache reuse probes pass. Packaged JAR classes/mixins verified; SHA256 `12074823b5674e0ec48da93c6c3e2f067bd5363b61dec1fbfbd504253e1e7709`. Artifacts: loom-studios-dev and loom-editor-screenshots on Build241; loom-premium-* on Comparison19. Subsequent acceptance commit changes documentation only; evidence belongs to the runtime SHA above.

Corrected failures: final renderWidget override, compact inspector height, cache probe mistakenly applied to playing fixtures, generic ElytraModel API assumption, and compact Settings button bounds. Local Fabric Loom resolution failed; CI supplies authoritative build/runtime evidence. See USABILITY_ANIMATION_AUDIT.md and DECISIONS.md. No known failing automated checks on accepted runtime source.

Next concrete work: ordinary-client painting/scrubbing/zoom responsiveness and frame-time measurement on target Windows hardware; repeated open/close/resource reload; Sodium/Iris/shader combinations and equipped multiplayer designs. Cache assertions are not hardware FPS measurements. Broader hardware/optional-mod acceptance remains manual.

## Premium studio presentation — DONE: UI/layout and Linux client verification (2026-10-03)

Runtime source: `778c2eea7f50ae46240a46fe506be7ceb8f2e238`. [Build #229](https://github.com/Stoffe101/Loom-Studios/actions/runs/37142515469) passes both jobs: 125 tests, zero failures/errors/skips, and 190 actual Minecraft captures. [Comparison #8](https://github.com/Stoffe101/Loom-Studios/actions/runs/37142515428) passes all eight jobs: 90 decoded screenshots. Tested with Minecraft 1.21.11, Loader 0.18.4, Fabric API 0.141.1, Temurin Java 21, Linux/Xvfb/software Mesa; no Sodium/Iris/shaders.

Implemented: smooth Inter studio labels; 48 SVG icons (45 Lucide and three Loom-specific); rounded button/card hover, focus and selection; resource-backed timber/steel/lantern frame; portrait and wide courtyard scenes; six responsive Home actions and Help; readable compact navigation and layer percentages; native/vector occlusion; smooth timeline, project-menu and visibility/lock controls. Real pixel artwork, player/cape/elytra, native text fields, tooltips, input and narration retain Minecraft ownership. Other scripts retain native font fallback.

Verified profiles: 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3. Visual review inspected Home, Cape/Elytra, Smart Import, sharing, dense layers/groups, context menus, library organization/bulk/version screens and cool/cute templates across all four profiles. GUI 3 dense-layer assertions require four complete rows. Nine full-run idle cache assertions pass while submitted frames advance and paint/upload counts remain unchanged. Screen workflows pass: canvas/preview/expanded middle pan, palette close, alpha isolation, hash/skin reuse, import handles, project/portable/PNG import/export, animation drag/undo, library right-click/delete/Trash/double-click/recovery, unsaved safety, favorite ordering, bulk hide/undo and Equip.

Performance changes: one interaction-invalidated transparent NanoVG PIP surface, globally unique revisions, SVG/font resources loaded once, and a bounded 32-entry cache for template and color-picker textures. Continuous entity rendering remains separate. These submission/cache assertions establish reuse, not hardware FPS.

Reference targets: all five approved Home, Cape, Elytra, Smart Import and Sharing images. Intentional adaptations: existing project workflows remain available; compact profiles use abbreviated copy or icon-only controls with tooltips; template/user artwork stays crisp pixel art; the workshop scene is a generated resource texture rather than a world renderer. Provenance and licenses are in PREMIUM_ASSETS.md and packaged third-party notices.

Remaining manual verification: target hardware frame times and live editing responsiveness; Windows/macOS native loading; Sodium/Iris/shader combinations; resource reload and repeated screen/open-close memory use. Native input fields/tooltips and some ancillary task-page composition can receive a later visual polish pass. F9 is a labeled developer comparison with illustrative sample artwork, not an editor replacement.

Final corrections: GuiGraphicsOcclusionMixin flushes the studio surface at renderDeferredElements HEAD before native tooltips enter their higher stratum. afterRender remains a guarded fallback. Non-overlapping opaque canvas pixels return before allocating/copying the vector command list. The compact tooltip captures were inspected, alongside final-source Home/import/share/editor contact sheets at all four profiles.

Verification evidence is attached to the exact runtime source above. The subsequent acceptance commit changes documentation only. Build artifacts are available as loom-studios-dev; screenshot artifacts are loom-editor-screenshots and loom-premium-* on the linked runs. CI emits JPEG contact sheets for review and retains original PNGs as artifacts.

Primary tooltip/render-order research: https://docs.neoforged.net/primer/docs/1.21.9/ (renderDeferredElements rename), https://docs.neoforged.net/docs/1.21.8/gui/screens/ (deferred tooltip strata), and exact Minecraft 1.21.11 method descriptors checked before compilation. Linux/X11 missing narrator/cursor-shape and offline Realms messages are capture-environment limitations; rendering/input assertions still pass. They do not establish optional-mod or cross-platform acceptance.

## Historical implementation checkpoints

The following dated entries describe earlier checkpoints. The verified source and remaining work above supersede their presentation status and pending-verification statements.

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

## Prior release checklist (historical; current handoff above)

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
