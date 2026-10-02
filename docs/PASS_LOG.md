# Loom Studios — Pass Log

## 2026-10-02 — Canvas geometry and bounded workspace repair

**State: PARTIAL — clean build and editor screenshot verification PASS; full decorative reference acceptance pending.**

Base: `f49ddb2e9727e16d3e6438772534f278d0520a38`. Target: all five approved `docs/references/ui` workshop screens, with primary work focused on the two failed Cape/Elytra screenshots.

Changes: common integer canvas transform; identical live/committed selection bounds and no false hover handle; shared bounded compact/normal layout; slim workshop chrome; coherent icons; contextual tool rows; complete tab visibility; paged Layer/Gradient/Transform/Stops; opacity sliders; cached real layer thumbnails; cached two-wing composition with zoom/pan; adaptive timeline; explicit animation Keys/Playback; preserved canvas/preview view state; readable navigation and consistent Home routing; Elytra color alpha control. Smart Import retains its existing task tabs with the calmer shared surfaces.

Architecture: pure geometry/layout contracts and bounded inspector allocator, replacing editor-owned duplicated absolute stacks. No schema or network change. Intentional reference deviations: native Minecraft font, code-native pixel icons, and paged compact properties preserve real functionality in 640×360 rather than reproducing reference-only decoration.

Tests: clean [Actions #178](https://github.com/Stoffe101/Loom-Studios/actions/runs/37068918058) passed both jobs for source `a2a9628afced367d4a178f944c5cce239d110fbf`, tested merge `ed4b04f900dc200eb43fa6074fecd5d6a832e084`, Java 21.0.12+1 on Ubuntu/Mesa with the unmodified Loom plugin. All 22 regenerated PNGs were downloaded; compact Stops and minimum-window guidance are readable, and the single-pixel live/released outline region is identical at GUI 3 / 200% zoom. The committed-selection PNG is valid.

Earlier local tests: full Gradle 9.6.1 `build` passed on Java 21.0.9 / MC 1.21.11 / Loader 0.18.4 / API 0.141.1+1.21.11 before final polish. New pixel transform and profile bounds tests passed. The earlier build passed all 99 tests. Actual Minecraft screenshots were inspected for Cape/Elytra at every required profile and compact Color/Properties/Gradient/Transform/Stops/Animation/Playback pages plus long layer/track collections. Live/committed single-pixel bounds were equal in the earlier capture run (88×88 physical pixels at 200%); one committed PNG was truncated during the environment failure and was removed. This is superseded by the successful clean CI capture above. The container's unsupported Unix sockets required a local cached Loom platform-probe workaround; the repository itself contains no platform patch. Clean CI above independently passed without that workaround.

Risks/gaps: editor captures passed; full Home/Import/Share reference acceptance remains pending; optional-mod/shader/multiplayer compatibility not rechecked. Next: complete decorative workshop styling and review the wider five-screen reference target.


## 2026-10-02 — Reference UI architecture refactor

**Status: IMPLEMENTATION CI PASS / LOCAL RE-VERIFICATION PENDING**

Trigger:
- local testing confirmed the feature set broadly worked;
- UI/reference fidelity and usability did not pass;
- screenshots showed button walls, excessive scrolling, overlapping/dense text, unclear animation authoring, buried Export, and a particularly poor 1920x1080 GUI-scale-3 layout.

Merged-main baseline entering the pass:
- `e3e417ff21f231e8513186e499026682d4cfb785` — GitHub Actions #158 **SUCCESS**.

Important refactor checkpoints:
- `2900a0c4ec709ba50123dbf494a7f9ab72817855` — responsive UI primitives;
- `a79680c01157c226dd19c3a0bb3c53ed1f1bf811` — responsive Home;
- `482d43ff4e44690e4dd5d685ce9f42bd9fa2ab39` — compact color picker;
- `1615b683ec9bdc4bbc1fd15aac333d4ffe199e64` / `9dbcd21e466a4164f9c96f90395c31af68f83abf` — Cape contextual workspace;
- `0a3c579c5f4809518454dbb31c209d569985935a` — simplified timeline foundation;
- `38b0efada9ebb85e6140e8bad1e325be6c757163` — Elytra layout + Animation inspector — Actions #166 **SUCCESS**;
- `ff1670f138bedf13f05e4ea199981b73834dfdc0` — explicit Export / Import workspace — Actions #167 **SUCCESS**;
- `007ae2121d835b85aee8ed01259257c735d46288` — Smart Import tabs — Actions #168 **SUCCESS**;
- `8b338dfd530f41e4b283058b2d078e9beb6b41fb` — compact timeline geometry — Actions #169 **SUCCESS**;
- `db90568ad8e47b342e3be36419381f4e2c37af66` — corrected combined UI head — Actions #172 **SUCCESS**.
- `d6407fd5125836969d6dd5c40ecbb65c4069c1b6` — final pre-merge UI/docs cleanup validation — Actions #175 **SUCCESS**.

Implemented:
- shared `LoomScreenChrome`;
- expanded reusable icon button language;
- compact breakpoint for <=700x420;
- responsive Home with dead Settings removed;
- explicit Share / Export naming;
- Cape icon rail + canvas toolbar + context bar;
- Cape Layers / Color / Properties inspector;
- Elytra icon rail + Layers / Color / Animation inspector;
- simplified animation workflow and compact timeline;
- Smart Import Placement / Processing tabs;
- persistent Apply / Preview actions;
- explicit Export / Import sharing workspaces.

Expected red runs:
- #164 was caused by accidentally pruning Elytra helper methods during the first layout rewrite; methods were restored and subsequent heads are green;
- #170/#171 were caused only by an incompatible direct-tooltip API experiment; it was removed and #172 is green.

The new shell still requires local visual/runtime re-verification, especially at 1920x1080 GUI scale 3.

---
## 2026-10-02 — Loom Codes local/offline sharing MVP

**Status: CI PASS / LOCAL VISUAL-RUNTIME VERIFICATION PENDING**

Merged-main baseline entering the pass:
- `ccc8fe3ca8c05a7048745daf03c26d7f7940beac` — GitHub Actions #151 **SUCCESS**.

Green checkpoints so far:
- `bff95cbd5c79ce59b417e97f49cb58917b97d307` — portable-code common core — Actions #152 **SUCCESS**;
- `709087febc06370df247bdb200d0b988e3f7ea79` — functional sharing screen + mapped clipboard + project-file import — Actions #154 **SUCCESS**;
- `a34bfddac35d29e175d9bcdce75dff3ac2969365` — canonical sharing documentation head — Actions #156 **SUCCESS**.

Implemented common core:
- `LSP1:` self-contained compressed portable project code;
- bounded compression/decompression;
- normal Loom codec validation after decode;
- deterministic `LS-XXXX-XXXX-XXXX` design fingerprint;
- fork-on-import identity protection.

Implemented Reference-05-oriented client workflow:
- Home Loom Codes destination;
- Copy Design ID;
- Copy Portable Code;
- Save Portable Code;
- Paste/decode Portable Code;
- native `.loom` import dialog;
- imported-design preview before acceptance;
- Cape/Elytra preview toggle;
- Import to Library;
- Import + Open;
- editable `.loom` export;
- Cape PNG export;
- Elytra PNG export;
- unique export filenames;
- explicit local/private service wording.

The first sharing UI compile run #153 failed only because the initial implementation reached through Minecraft's Window wrapper using a non-existent native-handle accessor. It was corrected to use the mapped Minecraft keyboardHandler clipboard API; exact corrected head #154 is green.

No hosted resolver/gallery/friends/public backend is claimed or implemented.

Local visual/runtime verification remains pending for clipboard behavior, file picker, exports, imported project opening, preview correctness and Reference 05 layout at all mandatory GUI profiles.

---
## 2026-10-02 — Schema v3 + animation timeline MVP

**Status: CI PASS / LOCAL VISUAL-RUNTIME VERIFICATION PENDING**

Latest merged-main baseline entering the pass:
- `2eca773a0358344491fd3280f535feb3515268a0` — GitHub Actions #139 **SUCCESS**.

Exact green checkpoints:
- `e4c5aaa1112992e11a172c6cbf3d62874c1c808e` — schema-v3 model/migration tests — Actions #141 **SUCCESS**;
- `864e48534f5b1464e23089104096af2a4ee1d3d4` — authored animation runtime — Actions #142 **SUCCESS**;
- `88e1b1c051545661f021262b39fc57c3eebf4844` — reference integrity + fixed-tick preview correction — Actions #145 **SUCCESS**;
- `156afcfe9eb7a2cf5d38f38280bc35335e5dade8` — compact timeline widget — Actions #146 **SUCCESS**;
- `c3e5ca49e4ecccbeaca7ad064a828c63c7720692` — integrated Elytra timeline authoring — Actions #147 **SUCCESS**.

Schema v3 adds project duration/loop/playback speed plus bounded layer-targeted tracks and ordered tick/value keyframes while preserving schema-v2 typed layers.

Current authored effects: Pulse, Scroll, Hue Shift, Moving Gradient, Sparkle, Emissive Glow.

The Elytra timeline now supports Play/Pause, scrub, Loop/Once, duration, project playback speed, Add Track, enable/select/delete track, effect cycle, Add/Remove Keyframe, keyframe Value +/- and per-track speed cycle.

Timeline preview can render a fixed authored tick through a stable preview-only runtime bundle. Scrubbing updates preview textures in place and never equips/publishes the dirty project.

Expected intermediate red runs:
- #140 exposed old schema-v2 test assumptions;
- #143/#144 exposed animation-layer deletion ordering during strict reference validation;
- corrected exact heads are green above.

Local visual/runtime verification remains required for timeline density, hit targets, effect output and scrubbed-preview fidelity at all mandatory GUI profiles.

---
## 2026-10-02 — Elytra workflow completion

**Status: CI PASS / LOCAL VISUAL-RUNTIME VERIFICATION PENDING**

Exact green implementation checkpoint:
- `cc55df2ba4b52907595a1d7d7cb7c48849059692` — GitHub Actions #137 **SUCCESS**.

Added cape-to-Elytra conversion:
- compiles the current cape Outside face;
- aspect-fits it into the semantic wing face;
- mirrors the converted artwork into the opposite wing;
- preserves alpha;
- creates a new editable Elytra Paint layer rather than flattening/replacing the existing stack.

Added Elytra Smart Import:
- Smart Import can now target Elytra;
- linked import creates two semantic Image layers, left + right;
- the right Image layer mirrors the left transform horizontally;
- candidate texture/3D preview uses the Elytra canvas;
- existing Elytra Image layers can be reopened and edited through Smart Import;
- import continues to validate final project encode bounds before committing.

Expanded Elytra layer authoring:
- reorder;
- opacity;
- rename;
- blend mode;
- Edit Image action;
- shared Swatches/palette window from the Cape workflow.

Automated coverage added for:
- conversion geometry/mirroring;
- alpha preservation;
- 4x conversion;
- invalid conversion source dimensions;
- normalized semantic wing rectangles;
- Elytra Image add/update;
- Elytra layer property codec round-trip.

No schema or network protocol version changed.

Local visual/runtime verification remains pending for the new layer/property controls, Swatches, conversion result orientation and Elytra Smart Import at the four required GUI profiles.

---

## 2026-10-02 — Home reference shell + semantic Elytra Editor

**Status: CI PASS / LOCAL VISUAL-RUNTIME VERIFICATION PENDING**

Exact green implementation checkpoints:
- `0cc3b219fcda2c8b4fb619db900545890e40764e` — Home shell — GitHub Actions #127 **SUCCESS**;
- `fb21521181dae6139122b8e0e4891e4083627203` — semantic Elytra wing editor — GitHub Actions #128 **SUCCESS**;
- `af767ce8c12a55c725d5b14fc2602872515b5564` — reusable Elytra layer stack — GitHub Actions #129 **SUCCESS**;
- `e46960484bd4d8ef7ef308805a0ad449ba67c643` — project-authored Elytra thickness — GitHub Actions #130 **SUCCESS**.

Home dashboard:
- replaced the temporary centered launcher with a responsive three-column reference shell;
- added reusable icon action cards;
- real Recent Project thumbnail cards from the generated cache;
- selected-project 3D cape preview with drag/zoom/reset;
- Create New Cape / Load / Import Image / Edit Elytra routing;
- functional Blank template;
- functional Gradient template;
- category placeholders for later template packs;
- status/project-count footer;
- unreadable-project warning state.

Semantic Elytra Editor:
- added common-core `ElytraWing` semantic mapping for the two 10x20 front faces;
- unfolded Left / Right wing canvas;
- Pencil / Eraser;
- brush sizing;
- 1x / 2x / 4x;
- Linked Mirror and Separate Wings;
- linked edits mirror local X into the opposite wing;
- normal compound-edit Undo history;
- integrated Elytra 3D preview;
- Save / Save + Equip.

Elytra Layers:
- generalized `LoomLayerListWidget` from Cape-project-specific to generic canvas-backed;
- reused typed icons/visibility/lock behavior in Elytra;
- Add / Copy / Delete Paint layers;
- lock enforcement through normal ProjectEdits.

Thickness:
- added `LoomRuntimeSettings.withElytraThickness`;
- added editor Depth controls from 25% to 200%;
- removed the old development V-key thickness preset override;
- saved/equipped project thickness is now the single runtime authority.

Automated tests added for:
- linked wing pixel mirroring;
- separate wing isolation;
- 4x semantic mirror mapping;
- lock rejection;
- linked brush output.

No project schema or network protocol version changed.

Local visual/runtime verification is pending because the user is away from the development PC.

---

## 2026-10-02 — Gradient Editor + typed-layer UX

**Status: CI PASS / LOCAL VISUAL VERIFICATION PENDING**

Exact green implementation checkpoint: `29d43b67ae880679e3b8ae9c3093c7f5ad7c2613`  
GitHub Actions #124: **SUCCESS**

Refined the schema-v2 Gradient editing surface instead of duplicating the already-present first-pass controls.

Gradient authoring now exposes:
- Linear / Radial;
- editable ordered stops;
- start/end current-color assignment;
- add/remove stop;
- stop-position stepping;
- angle +/-;
- repeat;
- dither;
- layer-relative Move Left / Up / Down / Right;
- bounded uniform Scale - / +;
- Mirror H / Mirror V;
- reset transform to the active semantic clip.

Added pure common-core `GradientAuthoring` so move/scale/mirror/reset behavior is deterministic and unit-testable rather than screen-local.

Typed layer list UX now provides:
- primitive icon identity for Paint / Image / Gradient;
- selected-row cyan accent;
- direct visibility affordance;
- direct persistent lock affordance;
- quieter names without textual `[P]` / `[I]` / `[G]` / `[L]` prefixes;
- opacity remains visible per row.

Automated coverage added for:
- layer-relative Gradient translation;
- aspect-preserving Gradient scale and scale percentage;
- independent H/V mirror toggles;
- reset-to-clip behavior;
- invalid scale rejection.

No schema/network format changed.

Local visual verification remains pending for layout, icon readability, direct row hit targets and all new Gradient controls across the four required GUI profiles.

---

## 2026-10-02 — Refreshed approved UI reference set

**Status: DESIGN/DOCUMENTATION PASS — NO RUNTIME SOURCE CHANGE**

Baseline entering this pass:
- merged main: `6208086fd5c6f025376afd7cf8390829cda56dbd`;
- GitHub Actions #121: **SUCCESS**.

Approved and archived the refreshed five-screen Loom Studios reference set:
- Home;
- Cape Editor;
- Smart Import;
- Elytra + Animation Editor;
- Loom Codes / Sharing.

Repository:
- optimized 1280x720 WebP copies live under `docs/references/ui/`.

Project Library:
- full-resolution 1672x941 PNG masters live under `/Loom Studios/UI References/`.

Visual direction now explicitly favors:
- roughly 70% clean modern creative editor / 30% magical Minecraft workshop;
- stronger showcase framing on Home/Sharing;
- calmer canvas-first work mode on Cape/Smart Import/Elytra;
- icon-led tools/buttons/layer types;
- restrained cyan/violet glow for active/selected/primary states;
- fewer nested borders and repeated decorative elements.

The refreshed set supersedes the earlier rough concepts. It remains a design target rather than a pixel-perfect screenshot contract.

No gameplay/runtime behavior changed in this pass. Local visual verification against the implementation remains pending.

---

## 2026-10-02 — Schema v2 + functional Smart Import

**Status: CI PASS / LOCAL VISUAL-RUNTIME VERIFICATION PENDING**

Exact green implementation checkpoint: `7a568f736f273328a3b55d1ea28587832cd45282`  
GitHub Actions #113: **SUCCESS**

Implemented schema v2:
- explicit schema-v1 -> schema-v2 migration;
- stable layer-kind IDs;
- stable schema-v2 blend IDs;
- Paint / Image / Gradient layers;
- persistent layer lock;
- normalized typed-layer transforms;
- bounded embedded Image sources;
- persistent Image processing settings;
- Gradient transform/stop/repeat/dither data;
- shared typed-layer runtime compilation.

Implemented Smart Import:
- PNG file picker/decoder adapter with bounded decode/persistence rules;
- Home + Cape Editor import entry points;
- Original / Processed / Cape Texture previews;
- isolated 3D candidate-project preview;
- Fit / Stretch / Crop / Center;
- Keep Aspect;
- move / scale / arbitrary persistent rotation;
- Mirror H / Mirror V;
- Direct / Pixel Art / Outline Only / Monochrome / Palette Limited / Posterize;
- Brightness / Contrast / Saturation;
- Reduce Colors;
- Floyd-Steinberg Dither;
- Loom Swatches palette integration;
- Apply as editable Image layer;
- reopen/edit existing Image layers.

Runtime/compiler:
- typed Image/Gradient layers compile through the same visibility/opacity/blend/emissive path as Paint layers;
- typed-layer placement survives 1x/2x/4x backing-resolution changes;
- locked/non-Paint layers reject paint-tool pixel mutation;
- candidate import preview does not cross the equip/network boundary.

Automated coverage includes:
- processing mode behavior and stable IDs;
- Outline Only;
- Pixel Art color bounds;
- Palette Limited alpha preservation;
- v1 -> v2 migration;
- typed-layer codec round trip;
- persistent lock;
- Image rasterization;
- Gradient rasterization;
- typed-layer resolution stability;
- paint mutation rejection for locked/typed layers.

Local visual/runtime verification is intentionally pending because the user is away from the development PC.

---



## 2026-10-02 — Smart Import color reduction + dithering foundation

**Status: CI PASS**

Exact green checkpoint: `7d4efbe75744453ba03142eb61d97c917f79c7bc`  
GitHub Actions #85: **SUCCESS**

Initial exact head `62d6a4a85322b067a11c79ccec1415b74cd08ab1` failed GitHub Actions #84 in `:compileJava`.

Cause:
- Java generic inference treated the median-cut comparator lambda parameter as `Object` in a chained `Comparator.comparingInt(...).thenComparingInt(...)` expression.

Correction:
- explicitly type the comparator lambda parameter as `ColorPoint`;
- no quantization/dithering behavior changed.

Added deterministic pure-core processing:
- bounded Reduce Colors palette extraction;
- 5-bit/channel fixed histogram to avoid unbounded unique-color memory;
- weighted color-box splitting;
- nearest-palette RGB mapping with source alpha preservation;
- Palette Limited mapping primitive;
- Posterize;
- Monochrome;
- Floyd-Steinberg dithering;
- dithered Reduce Colors.

Transparency behavior:
- fully transparent source pixels remain byte-for-byte unchanged;
- Floyd-Steinberg error is not diffused into or through fully transparent pixels;
- semi-transparent pixels preserve their source alpha.

Automated tests cover determinism, color-count bounds, alpha preservation, palette mapping, posterization, monochrome, transparency barriers, invalid limits and dithered color containment.

No local visual verification is required for this pure common-core slice.

---


## 2026-10-02 — Smart Import transform + color-adjustment foundation

**Status: CI PASS**

Exact verified checkpoint: `c7b3c4107543f42a30c214e7aa41942d59281249`  
GitHub Actions #81: **SUCCESS**

Added a schema-independent, pure common-core image-processing foundation:
- immutable bounded `PixelImage`;
- 4096 max dimension / 16,777,216 max pixels;
- Fit / Stretch / centered Crop / Center placement math;
- transparent placement rendering;
- mirror horizontal / vertical;
- 90-degree clockwise/counter-clockwise rotation;
- rectangular crop;
- nearest-neighbor resize;
- Brightness / Contrast / Saturation with normalized -1..1 controls;
- alpha preservation.

Automated tests cover placement geometry, raster output, clipping, transform orientation, bounds/defensive ownership and color-adjustment behavior.

Architecture rule:
- these rasters are preview/compiler values, not a shortcut for flattening imported art into schema v1;
- the future schema-v2 Image layer should preserve source/transform/processing intent.

No visual/runtime claims are required for this pure-core slice.

See `SMART_IMPORT.md`.

---


## 2026-10-02 — Equipped-state hardening + emissive cleanup + Select UI

**Status: CI PASS / LOCAL VISUAL VERIFICATION PENDING**

Exact verified checkpoint: `831b1139a14944b643926f67c655db9b46b65c38`  
GitHub Actions #78: **SUCCESS**

Source-only selection-history checkpoint `01834249405e621691aba2fc9f13525d5ef7f786` also passed GitHub Actions #76.

Runtime/state hardening:
- local multiplayer publication now reads the explicitly equipped project/hash rather than the dirty editor session;
- local world rendering therefore follows the same equipped snapshot boundary documented by ADR-035;
- unsaved editor changes remain available to the scoped 3D preview only.

Emissive cleanup:
- layer Emissive toggles now synchronize the schema-v1 runtime emissive master;
- enabling the first emissive cape layer activates the runtime pass;
- disabling the final emissive cape layer disables it again;
- removed the hard-coded SPIKE-06 shimmer stripe from real project emissive output;
- added automated emissive master/layer synchronization coverage.

Selection UI:
- Select tool added to Cape Editor;
- drag creates a normalized `PixelSelection`;
- persistent selection outline;
- Clear Selection;
- Move Left / Right / Up / Down;
- arrow-key nudge while Select is active;
- Flip H / Flip V;
- UI nudge clamps the whole selection inside the active semantic cape face;
- face or resolution changes clear temporary selection state;
- selection remains editor state and is not serialized.

Automated selection coverage expanded:
- drag endpoint normalization;
- invalid direct selection rejection;
- vertical flip;
- combined horizontal+vertical flip;
- existing horizontal flip/move/bounds coverage retained.

Documentation cleanup:
- replaced stale chronological `CURRENT_STATE.md` with a current snapshot;
- refreshed `NEXT_WORK.md`;
- corrected stale Loom/Gradle pins;
- corrected schema-v1 size/blend documentation.

Local visual/runtime claims are intentionally deferred because the user is away from the development PC.

---


## 2026-10-02 — Selection / transform core foundation

**Status: IMPLEMENTED / CI RUNNING OR GREEN AT CHECKPOINT**

Implemented common, testable transform primitives before exposing the visual editor tool:
- `PixelSelection`;
- semantic-face move;
- horizontal/vertical flip;
- bounds validation;
- deterministic overlapping move behavior.

Automated coverage verifies:
- horizontal flip order;
- move clears the source;
- moved pixels land at expected destination coordinates;
- out-of-face selections are rejected.

This deliberately builds the transform engine before the final reference-image toolbar interaction.

---

## 2026-10-02 — Layer effects + reference-fidelity roadmap

**Result: IMPLEMENTATION CI PASS / LOCAL VISUAL TEST PENDING**

Green SHAs:
- `27a40a72c1b7fb0ffb17162a250a58f9d63cef74` — run #65
- `86fcc37183e5150359fe04d83508c940b964e73b` — run #66
- `4bad39c53e81d09bb49f8540bf72f5db5e2b2a1c` — run #67

Added:
- layer rename;
- Emissive On/Off;
- Normal / Add-Glow / Screen / Multiply / Overlay;
- compiler support for those blend modes;
- schema-v1 persistence tests;
- canonical per-reference-screen status/roadmap.

No local runtime/visual claims are made for this slice yet.

---

## 2026-10-02 — Compact Swatches + alpha/numeric color + symmetry + Layers foundation

**Status: IMPLEMENTED / FINAL CI + LOCAL UX VERIFICATION PENDING**

Screenshot-driven Swatches corrections:
- compact footprint for 1920x1080 GUI scale 3;
- compact mode defaults to collapsed management controls;
- Edit/Done reveals or hides New/Rename/Delete/Add/Import/Export;
- fixed Saved palettes text overlap by deriving the swatch viewport from the true controls bottom.

Additional milestones completed in the same pass:
- alpha slider and transparent-color painting;
- editable Hex/R/G/B/A fields;
- alpha-aware palette persistence/share codes with legacy compatibility;
- Horizontal / Vertical / Both symmetry;
- first functional Layers panel;
- layer visibility, add, duplicate, delete, reorder and opacity;
- automated palette-alpha and layer-stack coverage.

---

## 2026-10-02 — Swatches dock + temporary brush preview + Line/Rectangle

**Status: IMPLEMENTED / CI FINALIZATION PENDING**

Local feedback entering the pass:
- custom palette persistence/import/export works;
- requested palette UX is Photoshop/Illustrator-style grouped swatches;
- permanent brush-radius halo is too visually noisy;
- 1920x1080 GUI scale 3 and 4x performance remain locally good.

Implemented:
- all named palettes shown as stacked swatch groups;
- multiple palette groups visible simultaneously;
- palette group header selection;
- up to 64 swatches per palette;
- selected palette deletion;
- temporary brush-radius preview only after Brush size changes;
- normal hover reverts to a single-pixel target outline;
- Line tool with live preview;
- Rectangle tool with live preview;
- outline/filled Rectangle mode;
- automated Line/Rectangle paint tests.

---

## 2026-10-02 — Zoom, brush-radius preview, palette-input fix, Fill/Eyedropper

**Result: CI PASS / LOCAL UX TEST REQUIRED**

Exact SHA: `05bb23149368e0fc75b205b43c11357af76f960c`  
GitHub Actions #48: **SUCCESS**

Runtime feedback entering the pass:
- 4x performance fix: confirmed good locally;
- 1920x1080 GUI scale 3: confirmed good locally;
- palette window: visible but interaction broken.

Fixes/features:
- topmost palette overlay now gets first input/focus routing;
- zoom 100%-800%, toolbar + mouse wheel;
- MMB pan while zoomed;
- canvas scissoring while zoomed;
- circular brush/eraser radius cursor;
- Fill;
- Eyedropper;
- editor keyboard shortcuts;
- flood-fill automated test.

---

## 2026-10-01 — Performance/layout + custom palettes CI checkpoint

**Result: CI PASS / LOCAL UX TEST REQUIRED**

Performance/responsive exact SHA:
`8e7ece34ba23b457d7f8761f37c47149aa79e483`  
GitHub Actions #44: **SUCCESS**

Palette exact SHA:
`4d29faeb0766cc5f97a4c486ae66026d87c2fb1f`  
GitHub Actions #46: **SUCCESS**

The 1920×1080 GUI scale 2/3 and 3440×1440 GUI scale 2/3 profiles are now a permanent documented UI compatibility contract.

---

## 2026-10-01 — Palette window narration type correction

Initial palette SHA `98746b076edc48bd7f4625bdd655d82802cfca1c` failed client compilation because the composite palette window exposed child narratables with the event-listener interface instead of Minecraft's NarratableEntry interface.

Corrected without changing palette behavior.

---

## 2026-10-01 — Custom color palettes implementation

**Status: IMPLEMENTED / CI PENDING**

Implemented:
- persistent named custom palettes;
- movable/pinnable floating palette side window;
- palette switching and color application;
- Add Current Color;
- right-click color removal;
- rename/save;
- `.loompalette` export;
- clipboard share codes (`LOOMPAL1:`);
- clipboard/inbox import;
- palette icon button in the scrollable right tool rail;
- palette share-code automated round-trip test.

---

## 2026-10-01 — High-resolution editor performance + responsive tool rail

**Status: IMPLEMENTED / CI PENDING**

Runtime feedback showed severe lag at 2x/4x and vertical overflow at 1920x1080 GUI scale 3.

Performance corrections:
- editor face rendering now uses one revision-cached DynamicTexture instead of thousands of per-pixel GUI rectangles every frame;
- only the active semantic face is compiled for the editor preview;
- grid rendering is reduced to row/column lines;
- ProjectSession dirty checks use immutable snapshot identity instead of serializing/hash-checking the whole project each frame;
- current project hashes are revision-cached;
- 3D preview reuses the workspace's cached hash;
- brush drags are grouped as one history/undo entry instead of one retained snapshot per pixel event.

Responsive layout:
- right editor rail now uses Minecraft's ScrollableLayout;
- tool content receives a real scrollbar when vertical space is insufficient;
- 1920x1080 GUI scale 2/3 and 3440x1440 GUI scale 2/3 are explicit layout targets;
- resolution/brush changes no longer rebuild the whole screen.

---

## 2026-10-01 — High-resolution/color-control green build

**Result: CI PASS / LOCAL VISUAL TEST REQUIRED**

Exact SHA: `f8b477568628a45120c5a31dcc54c80633effe06`  
GitHub Actions run #42: **SUCCESS**

Verified by CI:
- high-resolution canvas model/resampling;
- large-payload networking registration;
- scale-aware cape UV mapping;
- dynamic-resolution runtime texture compilation;
- brush-size logic;
- color-picker client code;
- automated project tests;
- remapped artifact upload.

---

## 2026-10-01 — High-resolution face UV follow-up

The first high-resolution implementation compiled successfully, then static review caught two scale-path issues before local handoff:
- face rendering/readout still used 1x atlas coordinates in a few places;
- `setCapeRegionPixel` retained an old 1x bounds guard before its scale-aware check.

Both are corrected so 2x/4x face editing and display use the same scaled UV mapping.

---

## 2026-10-01 — High-resolution canvas + brush/color control implementation

**Status: IMPLEMENTED / CI + LOCAL VISUAL TEST PENDING**

Implemented:
- 1x / 2x / 4x canvas resolution model;
- nearest-neighbor layer resampling with undo support;
- scaled semantic cape UV regions;
- dynamic-resolution runtime cape/Elytra NativeImages;
- Fabric large-payload project blob registration;
- 1 MiB project bound;
- brush size;
- circular multi-pixel brush;
- HSV-style color square + hue bar;
- RGB sliders with numeric values;
- hex display;
- palette swatches.

The resolution core supports both cape and Elytra; the current UI exposes it in the Cape Editor and the Elytra Editor will reuse the same control.

---

## 2026-10-01 — Cape face-first/static-default green build

**Result: CI PASS / LOCAL UX TEST REQUIRED**

Exact SHA: `475355119f491a631de8a1b5311e5c0e9f1bbdb4`  
GitHub Actions run #39: **SUCCESS**

Green scope:
- editor-created projects are static by default;
- semantic cape-face UV model;
- Outside/Back 10x16 primary canvas;
- face switching;
- pixel hover highlight;
- local + atlas coordinate readout;
- region-aware immutable edits;
- automated mapping/default tests.

---

## 2026-10-01 — Cape paint clarity + static default correction

**Runtime feedback:** functional paint/save/render path works, but raw-atlas editing was confusing and blank-project colors animated unexpectedly.

Implemented:
- editor-created projects default to no hue animation and no emissive effect;
- conventional 64x32 cape UV regions modeled explicitly;
- focused semantic face editor replaces raw full-atlas canvas in CapeEditorScreen;
- default view is 10x16 Outside / Back;
- face cycling for inside/edges/top/bottom;
- large grid, hover highlight and coordinate/UV readout;
- region-local edits map safely into the underlying atlas;
- automated tests cover static editor defaults and Outside-face UV mapping.

CI + second local UX check required.

---

## 2026-10-01 — Phase-2 first interactive editor green build

**Result: CI PASS / LOCAL RUNTIME TEST REQUIRED**

Exact SHA: `c94e5024d5adc7b4bcfa7da77d653986c1cd5a33`  
GitHub Actions run #37: **SUCCESS**

Green scope:
- Loom Studios home screen;
- Recent Projects/open flow;
- blank project creation;
- LoomButton + LoomCanvasWidget;
- Pencil/Eraser;
- starter palette;
- Undo/Redo;
- Save + Save/Equip;
- unsaved ProjectSession 3D preview;
- editor/world equipped-state separation;
- ProjectEdits common model;
- automated project-core tests.

The editor is now ready for first local hands-on testing.

---

## 2026-10-01 — Phase-2 LoomButton narration compile correction

Initial Phase-2 editor SHA `b002d1fdea0e00bdac8bfce8c341ee7e9e906598` reached client compilation and failed because the custom LoomButton inherited AbstractButton but had not implemented the required narration callback.

Correction:
- implement `updateWidgetNarration`;
- reuse Minecraft's default button narration text.

This also keeps the reusable control on the right accessibility path instead of bypassing narration.

---

## 2026-10-01 — Phase-2 first interactive Cape Editor slice

**Status: IMPLEMENTED / CI + LOCAL TEST PENDING**

Implemented:
- Loom Studios home screen;
- Recent Projects selection/open;
- blank cape creation;
- reusable LoomButton;
- reusable 64x32 LoomCanvasWidget;
- pencil/eraser;
- starter palette;
- undo/redo;
- save;
- save+equip;
- unsaved live 3D preview;
- immutable ProjectEdits pixel mutation helper;
- L dev key opens the studio.

This is the first pass where a user can actually paint a Loom project in-game.

---

## 2026-10-01 — Unsaved preview + production preview/library cleanup

**Status: IMPLEMENTED / CI PENDING**

Implemented:
- LoomPlayerPreviewScreen production naming;
- scoped preview-project override during local player render-state extraction;
- dirty editor project can render in preview while world/multiplayer retain equipped project;
- preview GPU bundle eviction on replacement/close;
- preview Elytra thickness comes from preview project;
- Recent Projects selection state;
- stale content-hash thumbnail pruning.

This is the final ownership bridge required before the real Phase-2 editor canvas can mutate ProjectSession live.

---

## 2026-10-01 — Editor/equipped state separation

**Status: IMPLEMENTED / CI PENDING**

Implemented:
- dirty ProjectSession is now distinct from saved/equipped world state;
- multiplayer publishes only the equipped project hash;
- in-world renderer consumes only the equipped project;
- unsaved edits can remain local to the editor;
- equip requires the current session to be saved/clean;
- save-and-equip convenience path added;
- WorkspaceState + listener registration added for future widgets/status bars.

This aligns the implementation with the original architecture rule that unsaved preview edits must not silently mutate multiplayer equipped state.

---

## 2026-10-01 — Workspace refactor compile correction

Initial workspace-refactor SHA `b62392d0e80acdc2afdc4a326c2320d290b10752` failed client compilation because the lifecycle callback still invoked `PlayerCosmeticRenderer.close()` with no argument after the renderer close method was changed to accept the Minecraft client.

Correction:
- register `PlayerCosmeticRenderer::close` directly with `CLIENT_STOPPING`.

No runtime architecture change.

---

## 2026-10-01 — Workspace retention + editor-created project foundation

**Status: IMPLEMENTED / CI PENDING**

Follow-up hardening before Phase 2:
- opening a saved project now survives the next player-bind tick instead of being replaced by the development factory project;
- blank editable projects can now be created with real metadata and transparent cape/Elytra base layers;
- local runtime GPU bundles are explicitly released when the live project content hash changes;
- local patched-skin cache is invalidated at the same time;
- automated test covers blank-project metadata/base-layer construction.

This prevents a future paint stroke from leaking one GPU texture bundle per project revision.

---

## 2026-10-01 — Phase-1 live workspace/runtime cache/library implementation

**Status: IMPLEMENTED / CI PENDING**

Changes:
- one ClientProjectWorkspace owns the local ProjectSession;
- ClientCosmeticSync no longer owns a duplicate local LoomProject;
- changed workspace hashes automatically trigger a new HELLO/cache-miss publication;
- RuntimeCosmeticCache owns GPU textures and animation uploads;
- PlayerCosmeticRenderer owns player skin patching/debug render controls;
- DynamicCosmeticSpike removed from the active architecture;
- Recent Projects descriptor/index added;
- compiled cape thumbnails cached as PNG files;
- library index skips broken files instead of poisoning the whole library.

Next: exact-SHA CI and then one local runtime regression.

---

## 2026-10-01 — Phase-1 session/persistence green checkpoint

**Result: PASS**

Exact SHA: `3de9c581c212ac390f699841dc844c6478d3927e`  
GitHub Actions run #29: **SUCCESS**

Verified:
- common/client compilation;
- schema-v1 metadata serialization;
- explicit migration gate;
- ProjectFileStore save/load/path containment tests;
- ProjectSession dirty/save/load/undo/redo tests;
- remapped artifact build/upload.

Phase 1 can now move to the client workspace/runtime-cache ownership slice.

---

## 2026-10-01 — Elytra visual calibration accepted + Phase-1 session slice

**Elytra result: PASS**

The calibrated 100% Loom Elytra thickness was locally verified and accepted as the default.

**Project-core implementation staged:**
- created/modified project metadata;
- explicit schema migration dispatch;
- root-bounded reusable ProjectFileStore;
- ProjectSession owns undo/redo, revision, dirty state, save/load state;
- save uses temporary file + atomic replace where supported;
- LocalProjectLibrary delegates to the tested pure file-store core;
- new tests cover migration rejection, save/load, dirty transitions and path containment.

CI verification required before this Phase-1 slice is marked green.

---

## 2026-10-01 — Phase-1 project core green checkpoint

**Result: PASS**

Exact SHA: `66695ffc66458c675654746c473809e9a13f1488`  
GitHub Actions run #27: **SUCCESS**

Passed:
- Java 21 / Minecraft 1.21.11 build;
- common and client compilation;
- JUnit project-core test suite;
- remap JAR/sources;
- artifact upload.

The old ProofProject spike type is removed after this green replacement. LoomProject is now the real network/runtime project object.

Elytra visual calibration still needs local eyes-on verification because CI cannot judge appearance.

---

## 2026-10-01 — Gradle 9 JUnit launcher correction

**Initial test-wiring SHA:** `58f25c610a8f27b002def89cfac0dfc51ae8d9cc`  
**GitHub Actions run #26:** FAILED in `:test`

Production/common/client compilation all passed. The failure was test-runner wiring only: Gradle 9 requires the JUnit Platform launcher to be present explicitly on the test runtime classpath.

Correction:
- JUnit BOM 5.10.2;
- JUnit Jupiter test implementation;
- explicit `junit-platform-launcher` test runtime.

No Loom runtime/project behavior changed in this correction.

---

## 2026-10-01 — Phase-1 project-core CI + invariant hardening

Exact SHA `fe734caa608bf1e3524222dd467f606d8a5dd6cc` passed GitHub Actions run #25.

Follow-up correctness hardening:
- LoomLayer now has deep pixel-array equality/hash semantics;
- schema-v1 runtime cape/Elytra canvases are explicitly constrained to 64x32;
- JUnit 5 project-core tests added to the normal Gradle `build` lifecycle;
- tests cover deterministic encode/decode/hash, defensive pixel ownership, trailing-byte rejection, runtime canvas dimensions, and undo/redo branching.

---

## 2026-10-01 — SPIKE-05 runtime PASS + Phase-1 project-core bootstrap

**Runtime result from two-client test: PASS for the primary synchronization path**

Observed:
- two different development players/projects visible together;
- per-player project colors differ;
- both remote/local cosmetics continue cycling color;
- no main-path synchronization issue reported.

**Implementation now staged:**
- Elytra 100% visual baseline calibrated to 0.5 raw model Z scale for Loom cosmetics;
- darker edge UV treatment to reduce the boxy/fat appearance;
- real schema-v1 LoomProject model;
- cape/Elytra canvases and immutable paint layers;
- bounded deterministic .loom binary codec;
- SHA-256 hashing/validation on real project bytes;
- multiplayer proof migrated from ProofProject to LoomProject;
- layer-to-texture compiler;
- local project library file plumbing;
- undo/redo project history foundation.

CI and local visual/runtime verification are required before this pass is marked fully green.

---

## 2026-10-01 — SPIKE-05/06 latest green implementation checkpoint

**Result: CI PASS / MANUAL RUNTIME PASS REQUIRED**

Exact SHA: `20451c816ecf9f8f806968c2663f462d161e659c`  
GitHub Actions run #23: **SUCCESS**

Green scope:
- content-addressed multiplayer proof protocol;
- server-side project validation/cache/equipped state;
- client-side cache-miss retrieval and remote compilation;
- remote-player cape/Elytra render-state application;
- deterministic local animation;
- generated emissive cape mask;
- Fabric-registered emissive player feature layer;
- vanilla chest-armor offset and wing-suppression parity;
- Client A and Client B dev profiles;
- Windows-safe nested optional-mod extraction for concurrent clients.

Completion gate remaining:
- actual two-client LAN synchronization;
- visible remote cosmetics on both sides;
- emissive ON/OFF visual proof;
- Iris/shader smoke test.

---

## 2026-10-01 — Multi-client Windows dev-runtime hardening

**Status: IMPLEMENTED / CI RETEST REQUIRED**

Before the SPIKE-05 two-client test, the optional nested-library extraction was changed to be concurrency-friendly.

Previously Gradle deleted and recreated `build/dev-mods-nested/` during every configuration. On Windows, launching Client B while Client A still had an extracted TRansition/TRender JAR open could cause a file-lock failure.

Now:
- current embedded-library outputs are tracked explicitly;
- existing matching-size files are reused;
- the directory is not deleted during normal configuration;
- only outputs belonging to currently installed dev mods are added to `modLocalRuntime`;
- stale files are therefore ignored even if they remain under build/ until the next clean.

This is development-harness-only and does not affect shipped Loom Studios artifacts.

---

## 2026-10-01 — SPIKE-06 vanilla cape alignment parity

**Status: IMPLEMENTED / CI RETEST REQUIRED**

The emissive feature pass now mirrors vanilla CapeLayer's chest-equipment behavior:
- suppress glow when the equipped chest asset provides a WINGS layer;
- apply the same armor offset when the chest asset provides a HUMANOID layer.

Reason: the second glow model must stay pixel-aligned with the vanilla base cape under armor and non-vanilla wing equipment, not merely when the chest slot is empty.

---

## 2026-10-01 — SPIKE-05/06 first green compile checkpoint

**Result: CI PASS / RUNTIME VERIFICATION PENDING**

Exact SHA `f33c8adf3baa7cfdd7f6a4d1e7fd28e15c0a357c`  
GitHub Actions run #20: **SUCCESS**

Verified by CI:
- common networking payloads compile;
- server cache/validation code compiles;
- client synchronization/cache code compiles;
- remote-capable dynamic cosmetic renderer compiles;
- Fabric player emissive feature layer compiles;
- Client A / Client B run configurations configure successfully;
- packaged development artifact uploads successfully.

Follow-up robustness improvement:
- client hello is now allowed to wait until the local player/level are actually available after JOIN instead of silently giving up if JOIN fires slightly early.

Manual two-client and shader/emissive verification is still required before SPIKE-05/06 are marked DONE.

---

## 2026-10-01 — SPIKE-05 Mojang-mapping server accessor correction

**Status: FIX APPLIED / CI RETEST REQUIRED**

Exact implementation SHA `9312856fc1449895b3b973371a1a0b566ed85a2f` reached `compileJava` and failed on four calls to `ServerPlayer#getServer()`.

Minecraft 1.21.11 Mojang mappings keep the ServerPlayer server field private and expose the server through the player's ServerLevel instead.

Correction:
- `context.player().getServer()` -> `context.player().level().getServer()`
- `player.getServer()` -> `player.level().getServer()`

No protocol or architecture change was required.

---

## 2026-10-01 — SPIKE-05 + SPIKE-06 implementation

**Status: IMPLEMENTED / CI + RUNTIME VERIFICATION PENDING**

SPIKE-05:
- SHA-256 content-addressed proof project;
- server cache-miss upload;
- project validation;
- equipped-state broadcast;
- remote cache-miss download;
- remote player cosmetic rendering;
- graceful no-server-mod behavior via `ClientPlayNetworking.canSend`;
- separate Client A / Client B IntelliJ run profiles.

SPIKE-06:
- deterministic client-side animation using synchronized world time;
- no animation-frame packets;
- generated per-project emissive mask;
- additional Fabric-registered player render layer;
- vanilla base CapeLayer retained;
- fullbright/translucent-emissive second cape pass;
- G toggles effect locally for visual comparison.

Runtime verification remains required before either spike is DONE.

---

## 2026-10-01 — SPIKE-04 local runtime verification

**Result: PASS**

Exact implementation SHA: `faca3953ef07c9a2755bd0619963a59712cae548`  
GitHub Actions run #17: **SUCCESS**

User screenshots verified:
- local player renders correctly inside the custom GUI;
- Cape mode works;
- Elytra mode works;
- preview-only switching does not require changing actual world equipment;
- drag rotation works;
- zoom works;
- Loom cape/Elytra cosmetics use the same gameplay render path.

Deferred minor issue:
- head orientation follows the preview rotation and feels too "look-at-camera" for a design inspection tool.

Planned bundled refinement:
- separate body-facing control from camera/orbit control;
- optionally lock head orientation or expose an explicit head/facing control;
- preserve full 360-degree model inspection.

This is polish, not an architectural failure. SPIKE-04 is DONE.

Next: SPIKE-05 multiplayer synchronization.

---

## 2026-10-01 — SPIKE-04 live preview implementation

**Status: IMPLEMENTED / CI + LOCAL TEST PENDING**

Implemented:
- custom in-world Screen;
- P debug key to open;
- real local-player render-state extraction;
- preview-only chest equipment override;
- Cape/Elytra preview toggle without mutating actual equipment;
- drag rotation;
- wheel zoom;
- reset control;
- same Loom runtime cosmetic textures as gameplay;
- non-pausing in-world behavior.

This screen is architectural scaffolding only. Final Loom Studios UI work will replace its visual treatment after the foundation spikes.

---

## 2026-10-01 — SPIKE-03 local runtime verification

**Result: PASS**

Exact implementation SHA: `5d8fce628228fc570750c60734a7bedf1497701a`  
GitHub Actions run #15: **SUCCESS**

User verified in-world:
- dedicated Elytra texture works;
- cape animation remains independent;
- vanilla gliding/wing animation works;
- V-key thickness presets function correctly;
- 100% restores vanilla thickness;
- thinner/thicker render-only geometry values work;
- no observed conflict with the optional development mod stack.

Product decision confirmed:
- Elytra editor will later expose Thickness as a real control;
- 100% is the default;
- Reset to Vanilla is required;
- debug V-key cycling is temporary development tooling.

SPIKE-03 is DONE.

Next: SPIKE-04 live GUI player preview.

---

## 2026-10-01 — SPIKE-03 geometry-thickness proof implementation

**Status: IMPLEMENTED / CI + LOCAL TEST PENDING**

Changes:
- restored opaque Elytra edge/top/bottom UV coverage so default 100% appears volumetric like vanilla;
- added an ElytraModel mixin that changes only wing local Z scale;
- default is exactly 1.0 (100% vanilla thickness);
- temporary V key cycles 100%, 75%, 50%, 25%, 150%;
- non-Loom Elytras are explicitly reset to zScale 1.0 because vanilla model instances are reused;
- thickness is visual only and does not affect hitboxes/gameplay.

Goal: prove that Loom Studios can expose thickness customization without replacing vanilla pose/animation behavior.

---

## 2026-10-01 — Elytra thickness design correction

**Result: DECISION ACCEPTED**

Runtime screenshots showed the transparent edge-face UV experiment made the dedicated Elytra texture look effectively paper-thin.

Decision:
- remove edge-face transparency as the default visual strategy;
- preserve normal vanilla Elytra appearance at the default setting;
- separate artwork from geometry;
- add future user-controlled Elytra thickness as a render-only model setting.

The intended implementation will scale wing depth rather than hiding side-face textures.

---

## 2026-10-01 — SPIKE-02 local runtime verification

**Result: PASS**

User runtime screenshots confirm:
- cape palette/highlight changes live;
- vanilla cape movement continues to work;
- no restart or reconnect is required;
- optional development stack remains stable.

Additional discovery:
- equipping an Elytra caused Minecraft to use the animated cape texture for the wings;
- source inspection confirmed vanilla `WingsLayer.getPlayerElytraTexture` prefers `skin.elytra()`, then falls back to visible `skin.cape()`.

This proves both the dynamic-texture pipeline and the requirement for independent cape/Elytra texture channels.

---

## 2026-10-01 — SPIKE-03 dedicated Elytra implementation

**Status: IMPLEMENTED / VERIFICATION PENDING**

Implemented a separate runtime Elytra texture and supplied it through the Elytra field of `PlayerSkin.Patch`.

The test texture also leaves Elytra edge-face UV strips transparent as an experiment to reduce visible boxiness without changing vanilla geometry.

Source inspection documented that vanilla 1.21.11 Elytra geometry is a 10x20x2 box per wing with CubeDeformation(1.0F), explaining the user's observation that it looks visually thick.

Next: CI + local in-world Elytra verification.

---

## 2026-10-01 — SPIKE-02 dynamic cape implementation

**Status: IMPLEMENTED / CI + LOCAL RUNTIME VERIFICATION PENDING**

Implemented:
- runtime-created 64x32 NativeImage;
- registered DynamicTexture under a stable Loom Studios Identifier;
- cape render-state now points at the dynamic texture;
- in-place redraw + `DynamicTexture.upload()` every 40 client ticks;
- no texture re-registration per update;
- shutdown cleanup via texture-manager release;
- obvious four-phase palette/highlight animation for visual proof.

The full editor will later replace this procedural generator with project/layer compositing.

---

## 2026-10-01 — SPIKE-01 local runtime verification

**Result: PASS**

The local Windows IntelliJ/Gradle development client was launched successfully with the optional test stack present.

Verified in-world:
- LS test cape appears on the local player;
- vanilla cape movement/geometry works correctly;
- no obvious clipping or rendering failure;
- Sodium, Sodium Extra, Iris, and 3D Skin Layers coexist in the test client;
- screenshot evidence captured by the user.

SPIKE-01 static cape rendering is complete.

Next: SPIKE-02 dynamic runtime texture updates.

---

## 2026-10-01 — 3D Skin Layers dev-runtime crash diagnosis

**Result: FIX IMPLEMENTED / LOCAL RETEST REQUIRED**

The first full optional-mod `runClient` reached Fabric Loader and loaded Loom Studios, Sodium, Sodium Extra, Iris, and 3D Skin Layers.

Crash cause:
- 3D Skin Layers failed during its client entrypoint;
- missing class: `dev.tr7zw.transition.loader.ModLoaderUtil`;
- the supplied 3D Skin Layers JAR contains TRansition and TRender under `META-INF/jars/`;
- Loom's remapped local-runtime copy did not expose those embedded libraries as separate runtime mods in the development namespace.

This is a **development harness issue**, not evidence that Loom Studios' cape mixin conflicts with 3D Skin Layers.

Fix:
- inspect optional dev-mod JARs during Gradle configuration;
- extract embedded `META-INF/jars/*.jar` libraries into `build/dev-mods-nested/`;
- add both top-level and extracted JARs to `modLocalRuntime` so Loom remaps every library into the named dev namespace;
- keep all generated/external JARs out of the shipped Loom Studios artifact.

Sodium Extra also reported that Reese's Sodium Options is recommended but missing. That warning is non-fatal and unrelated to this crash.

Next: local `runClient` retest with the same four optional compatibility mods.

---

## 2026-10-01 — Local IntelliJ test feedback

**Status: FIX APPLIED / CLIENT CRASH UNDER INVESTIGATION**

User local results:
- `runClient` reached the Java client process but exited with Windows status `0xFFFFFFFF`;
- Gradle's outer error does not identify the Minecraft/Fabric cause, so `run/logs/latest.log` / crash report is required;
- `listDevMods` failed solely because the helper task was incompatible with Gradle configuration cache.

Correction:
- disable Gradle configuration cache for this project;
- this matches the development-first IntelliJ/Loom workflow and removes needless friction from helper/run tasks;
- add exact log/crash-report troubleshooting commands to GETTING_STARTED.md.

The client crash itself is not yet attributed to Loom Studios or an optional compatibility mod.

---

## 2026-10-01 — Official Gradle launcher scripts

**Result: CI PASS**

Exact SHA: `c321be1f4d0713d29826a4fd773b614c3b26a226`

GitHub Actions run #7:
- wrapper info: PASS
- full build: PASS
- artifact upload: PASS

Replaced the temporary minimal wrapper launcher scripts with the full official `gradlew` and `gradlew.bat` scripts from Fabric's 1.21.11 example project. The committed wrapper JAR remains the official Gradle wrapper binary.

Reason: Windows/IntelliJ is the primary local development workflow, so wrapper launching should handle quoting, JAVA_HOME, and platform edge cases exactly as the standard Fabric project does.

---

## 2026-10-01 — SPIKE-01 compile checkpoint

**Result: CI PASS / RUNTIME VISUAL TEST PENDING**

Exact SHA: `c7386f0ed3a46bfb51c7ae8614162deb03ee4fd4`

GitHub Actions run #5:
- Java 21 setup: PASS
- Gradle wrapper: PASS
- project build: PASS
- SPIKE-01 client mixin compilation: PASS
- resources/mixin JSON/test cape packaging: PASS
- artifact upload: PASS

Implemented:
- local-player-only AvatarRenderer render-state injection;
- cape-only `PlayerSkin.Patch`;
- vanilla `CapeLayer` remains responsible for motion/geometry;
- cyan/magenta 64x32 LS test cape;
- patched-skin cache;
- client-only mixin configuration.

Still required before SPIKE-01 is DONE:
- local runClient visual confirmation;
- normal vanilla cape movement;
- optional Sodium/Iris/Sodium Extra/3D Skin Layers smoke tests.

---

## 2026-10-01 — First green SPIKE-00 CI baseline

**Result: CI PASS**

Exact SHA: `a620de1a16334657b7e33f1606c800a673580214`

GitHub Actions run #4 completed successfully and uploaded the Loom Studios development artifact. This proves the Java 21 + Minecraft 1.21.11 + Loader 0.18.4 dependency baseline + Fabric API 0.141.1 + Loom 1.17.21 + Gradle 9.6.1 build combination.

Local dev-client and dedicated-server launches remain required before SPIKE-00 is fully DONE.

---

## 2026-10-01 — SPIKE-00 JAR configuration-cache correction

**Status: IN PROGRESS**

Exact SHA `680876b000cd6f50c079d6bb6abc9a83b48cc2d4`:

- Gradle wrapper: PASS
- Loom 1.17.21 on Java 21: PASS
- Minecraft/Fabric dependency setup: PASS
- `compileJava`: PASS
- `processResources`: PASS after prior correction
- `jar`: FAIL configuration-cache validation

Cause: the LICENSE rename closure read `project.base.archivesName` at task execution time.

Correction: capture the archive name during configuration and use only that captured value in the JAR task closure.

Next: rerun exact-SHA CI.

---

## 2026-10-01 — SPIKE-00 Gradle configuration-cache correction

**Status: IN PROGRESS**

Corrected SHA `6f442183229d9931457e4df47023751e18a32811`:

- Loom 1.17.21 resolved and ran successfully on Java 21.
- Minecraft/Fabric dependency setup reached compilation.
- `compileJava` completed.
- Build failed in `processResources` because the Groovy closure referenced `project.version` at execution time while Gradle configuration cache is enabled.

Correction:

- capture the mod version during configuration;
- pass the captured value into `processResources`;
- keep configuration cache enabled instead of papering over the issue.

Next: rerun exact-SHA CI.

---

## 2026-10-01 — SPIKE-00 toolchain correction

**Status: IN PROGRESS**

Bootstrap SHA `400d82c798db6a62a750ba2236481b22897c612e`:

- Gradle wrapper itself: PASS
- Java runtime: Java 21 PASS
- Build: FAIL during Loom plugin resolution
- Cause: current Loom 1.18.2 requires Java 25 to run Gradle

Correction:

- pin Fabric Loom to `1.17.21`
- pin Gradle distribution to `9.6.1`
- retain Java 21
- retain Minecraft 1.21.11
- retain Fabric Loader 0.18.4 baseline

Reason: Loom 1.17 already contains the modern property-based run configuration API and `preferGradleTask`, while avoiding Loom 1.18's Java 25 build-runtime requirement.

Next: CI verification on the corrected exact SHA.

---

## 2026-10-01 — SPIKE-00 bootstrap implementation

**Status: IN PROGRESS**

Implemented:

- real Fabric Gradle scaffold
- Minecraft 1.21.11 / Java 21
- Loader 0.18.4 baseline
- Fabric API 0.141.1+1.21.11 initial pin
- Mojang mappings
- split common/client sources
- Gradle wrapper
- IntelliJ/Loom client and server run profiles
- client run prefers Gradle runClient
- local optional compatibility mods through dev-mods/ + modLocalRuntime
- common/client entrypoints
- detection logging for Sodium, Sodium Extra, Iris, and 3D Skin Layers
- GitHub Actions build
- compatibility docs corrected: optional supported integrations, not dependencies

Verification pending:
- corrected CI
- local client/server launches
- optional test stack runtime

Next: finish SPIKE-00, then static cape.

---

## 2026-10-01 — Technical foundation research

**Result: PASS**

No blocker found for the planned core architecture.
