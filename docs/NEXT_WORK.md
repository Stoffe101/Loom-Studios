## 2026-10-08 — Approved priority update: Inspector first, then M2 multi-layer presets — NEXT

1. Complete premium Inspector PR: native Opacity slider (1% precision, Undo one drag), big **Animate Asset / Edit Animation** action preserving exact UUID/channel, grouped Position/Transform controls and responsive GUI2/GUI3 hitboxes. Confirm real screenshot artifacts.
2. Verify/merge PR #32's resizable Cape/Elytra inspector handles. Provide an obvious **Resize Panels** label/help, numeric width in **logical GUI pixels**, saved per-layout sizes, Reset to default, and clamping on scale/resolution changes. The fully free ElvUI-like panel layout mode remains future M5 work.
3. Fix/accept PR #33's prototype Simple visual preset gallery/isolated Try and keep Advanced lanes/curve edit unchanged. Mark anything red/unfinished as pending.
4. **M2-06:** Ctrl+click selects multiple layers in editor and Animation, with selected count and visual highlights; one preset may be tried/applied atomically to the selected layers, one Undo; existing custom tracks never get silently overwritten. Test invalid/locked/channel mismatch/track limits and at least three assets.
5. Continue M1/M2 themes: adaptive canvas/preview, direct Reference handles, frame filmstrip with native blank animation, mask/selection visualization, contextual curve and tooltip clarity at compact and ultrawide. Deprioritize mass artwork expansion until these workflows feel premium.

**CI:** Run full screenshots with actual populated Cape and Elytra editing; inspect, share actual original captures inline after successful builds, don't infer green from code-only changes.

## 2026-10-08 — High-priority Inspector "Animate Asset" flow — PLANNED / NEXT DESIGN INTEGRATION

**Priority update:** make **M1-04A / M2-00** the next mainline *user workflow* after the selected-asset inspector is stable, ahead of optional layout polish, mass catalog expansion and unrelated animation capabilities. From a selected independently editable Cape/Elytra Image layer, show **Animate Asset** (or **Edit Animation** if tracks already exist) beside Edit Pixels; launch the existing Simple/Advanced Animation Studio with that layer's persistent UUID, channel, exact target/wing and return context. Offer visual effects, slider-based Speed/Strength, pure **Try**, explicit **Apply/Cancel**, and return to the **same selected asset**.

**Deliverable sequence:** (1) add and verify the prominent Inspector action and safe layer/wing handoff; (2) show Simple effect cards, clear controls and real worn 3D Try preview; (3) preserve and target existing Advanced tracks without silent replacement; (4) explicit one-Undo Apply and Cancel/back; (5) capture in-game Inspector / Simple Animation / worn results for Cape and Elytra at 1920×1080/3440×1440 GUI2/3, including keyboard and locked-layer cases. Reuse animation 2.1/schema5, premium theme components, and current project-owned Image layers. **No code implementation is claimed by this documentation update.** Detailed contracts: [Editor spec](EDITOR_CREATIVE_UX_SPEC.md#2b-priority-animate-asset-directly-from-the-inspector) and [Animation UX spec](ANIMATION_UX_REDESIGN_SPEC.md).

## 2026-10-08 — Animation screenshot review findings (not yet fixed)

The accepted standalone Animation UX screenshot artifact from [Build d7e4aee2](https://github.com/Stoffe101/Loom-Studios/actions/runs/37719395555) confirms the dual curve/3D layout does not overlap. However, **3440×1440 GUI2** leaves substantial unused empty space beneath the right-side 3D preview while the timeline remains constrained to the left; **1920×1080 GUI3** clips the long footer hint mid-sentence. Track keys and control legends remain low-discoverability. These are genuine observed UX defects for M1-02/M2-02, not a failure of the narrow M0 overlap fix. Proposed correction: shorter compact context tips and responsive timeline/preview allocation with movable or resizeable panel boundaries. Keep animation engine data and user keyframes untouched.

## 2026-10-08 — M1 Edit Inspector four-profile capture extension — CI PENDING

Added UI capture stages 326–329, increasing full expected real Minecraft screenshots from 326 to **330**, to specifically render the selected Cape asset's Browse/Edit inspector at all four physical/GUI-scale profiles. These complement the existing 16 2D/worn Cape/Elytra screenshots, so UI tests no longer claim an unobserved contextual panel works. The capture fixture calls `showEditInspector()` on a genuine project-owned Image layer and the Build workflow asserts 330 captures. This is a testing-only extension with no data/protocol behavior change. **No capture or CI result exists yet for this branch**.

## 2026-10-08 — M1 Asset Browse / Edit contextual workflow — IMPLEMENTED ON BRANCH, CI PENDING

The Asset Library now separates **Assets** (search/category/thumbnail and click/drag to place) from **Edit** (selected layer controls). Placing a new asset opens the Edit tab directly. The contextual controls nudge by 1 actual atlas pixel, resize uniformly by 1 pixel, adjust opacity in 10% steps, rotate 15°, and flip H/V, with locked layers disabled. This sits beside the existing 2D+3D preview without changing schema-v5 or equipped-state semantics. `LoomAssetLibraryScreen` and the existing `LayerTransform`/`AssetPlacement` APIs are reused.

**Pending acceptance:** screenshots at all required GUI2/GUI3 profiles, test actual hitboxes/correct selected object/Undo/save, and confirm the inspector never overlaps Save/Back or collection scrolling. This is **not merged or verified**, and future numeric position textboxes/dockable layout remain distinct later work.

## 2026-10-08 — M1 first UI follow-up — PENDING M0

After accepting/merging PR26 and PR28 into main, integrate the M1 preview-usability prototype, run the full 326-image capture and 16 focused Asset Library captures at GUI2/3 with both Cape and Elytra. Verify the new backdrop icon never overlaps "3D · Pose", visibility or fullscreen icon at compact widths; confirms focus/hover/hitboxes, avoids screen-wide tooltip obstructions, uses neutral backdrop consistently with local preferences and shows selected layer name/opacities. Follow with canvas-first panel organization, intelligent inspector and numeric layout sizing. This branch is **not DONE** until tests/screenshots pass.

## 2026-10-08 — M0 accepted; active next work = M1 core editor UI/UX

M0-01 through M0-06 are complete on main (see [ACTIVE_MISSIONS.md](ACTIVE_MISSIONS.md) and CURRENT_STATE for merged SHAs/CI proof). No more broad unrelated subsystem work is needed to claim M0 done.

**Next implementation order:** (1) integrate the staged M1 preview/asset inspector branch onto verified main, using the already existing Loom premium UI infrastructure; (2) run Java21 tests and focused 20-image Cape/Elytra/selected Edit inspector screenshots, then all 330 actual-client captures and premium comparisons; (3) inspect 1920×1080 GUI3 and 3440×1440 GUI2 with real populated artwork and reject overlapping tooltips, clipped labels, offscreen controls; (4) continue Animation Simple/Advanced redesign, adaptive canvas/preview layout, frame filmstrip and reference drag controls; (5) return to art curation/scatter/soft mist once daily authoring workflows are intuitive. Exact pixel/drag resizing of arbitrary panels and full ElvUI-style docking are separately staged M5 features, after working default layouts.

**Known visual UX debt:** Animation 3440 GUI2 leaves unused right-side vertical space under the 3D preview, while 1920 GUI3 truncates lengthy footer guidance; the M0 curve/widget overlap is fixed, but the overall experience still needs M1/M2 work. Windows optional-mod and dedicated-server acceptance remains M7.

## 2026-10-08 — Studio UX 3.0 first slice — CI / visual acceptance pending

1. Run Java21 Minecraft 1.21.11 build and existing 164 tests on `feature/animation-ux-first-pass-2026-10-08`.
2. Capture all 310 editor screenshots. **Individually inspect** the new Animation studio, parameter lanes, curve graph and live preview (1920×1080 GUI2/3 and 3440×1440 GUI2/3); test the wide split preview doesn't steal or misplace curve handles and compact layout remains unchanged.
3. Exercise new layer dropdown, failed/no-track Parameters button, Simple/Advanced switches and Space playback; save/undo/preview isolation.
4. After acceptance/merge, start editable Asset Library work in independently tested slices: editor Assets button, thumbnail browser, drag/click-to-place as **independent editable layers by default**, then original curated packs, starfield collection architecture, scatter, mist/recipes.
5. Capture/share actual artifact screenshots with user for visual feedback. Do not merge layout failures merely because screenshots decode.

## 2026-10-08 — Asset diversity, worn previews and CI visual gate — NEXT / PENDING

1. Validate Java21, Minecraft1.21.11, full unit tests after 29 original detailed assets and live 3D Asset Library preview changes.
2. Run **focused 16-capture** job at 1920×1080 GUI2/3 and 3440×1440 GUI2/3: Cape 2D and worn, Elytra 2D and worn. Confirm each screenshot **actually shows the multicolor artwork on the canvas/cosmetic**, not merely catalog thumbnails, and previews reflect project-owned layers. Inspect both wing faces and clipping independently.
3. Run all 326 standard screenshots and premium comparison suites on latest SHA. Verify responsive dual-pane layout, focused mobile-like GUI3, selected-asset toolbar, frame-rate/cache, scissor and no overlapping controls. Document any failed test without calling work DONE.
4. Once all four categories are visually approved, provide representative original PNGs directly in chat and ask user for art-direction feedback. A passing Gradle build alone is not a visual acceptance.
5. Future work: curated pack expansion, browsing/favorites, deterministic starfield collections/scatter/mist and editable recipes; do not ship 120–150 low-quality near-identical icons for a count target.

## 2026-10-08 — Illustrated asset quality gate — TESTING NEXT

- Check CI Build Java21 and `PremiumAssetArtworkTest` for every new 28–48px source image: >5 palette values, transparency, no duplicate rasters, exact embedded multicolor save/portable roundtrip.
- Run all 314 real-client captures and focused Asset Library four-profile workflow on the **latest** SHA (do not cite an earlier passing image run as evidence for new artwork).
- Extract actual original PNGs and review at 1920×1080 GUI3/GUI2 and 3440×1440 GUI3/GUI2; evaluate star, moon, cloud, tree thumbnails, long names, centered canvas, visible headers, controls, no overlap, clipped text or unsafely small targets. Curated art may need more iterations, especially small-screen legibility.
- Test placing multiple illustrated assets as editable Image layers, moving/rotating/painting on source, preserving alpha/crop/face and color over saves/equip/network. Test at 1×, 4× and 8×; do not falsely promise true soft detail at 1×.
- Present new actual screenshots to user for art-direction acceptance. Only then consider expanding to 120–150 distinctive, original licensed assets, editable collections and scatter.

## 2026-10-08 — Asset Library implementation first slice — CI and visual review pending

1. Build Java21 Minecraft1.21.11 / Fabric and run full tests including `AssetPlacementTest`, `CreativeAssetCatalogTest`; inspect compile and schema5 round-trip.
2. Run all **314** real Minecraft captures; individually inspect `asset-library-cape-*` at four profiles against approved reference02 and visual collection focus. Review dense GUI3 for grid clipping/preview proportions.
3. Exercise user journey: open Assets from Cape/Elytra, select star/cloud/tree, drag tile over artwork **or** click then drag/place, check one newly selected editable IMAGE layer, move/rotate/recolor/opacity through existing layer UI, edit source pixels, Undo/Redo, save/reopen and portable code. Verify no outside-face/other-wing bleed.
4. Review UI state changes after browsing and returning to editors, no lost draft/false equip/scale drift or texture cache leak. If any failures, fix before merging.
5. M2 follow-up: expand original quality-controlled art catalog, asset collections with individually addressable children for many stars, scalable thumbnail atlas, scatter mode and editable theme recipes. This is not part of the stage-one deliverable.
6. Share actual screenshot artifact images with user for look/feel feedback before broader expansion.

## 2026-10-08 — Active mission queue / visual review — PLANNING ONLY

The current ordered check-off board is [ACTIVE_MISSIONS.md](ACTIVE_MISSIONS.md). Immediate emphasis: (1) verify/accept PR28's real worn Cape/Elytra + 2D results and all latest CI, investigate the extra cyan crescent; (2) verify/accept PR26 animation changes without merging unchecked state; (3) fix obstructive tooltips/weak canvas & cosmetic focus at GUI3 and ultrawide; (4) contextual edit properties and Simple Animation; (5) curate art quality before growing the catalog.

**ElvUI-inspired editable layout:** accepted for roadmap consideration, **not implemented or next in line**. Stage splitters and numeric sizes after stable workspace semantics; later Edit Layout unlock/snap/dock, named profiles and recoverable reset. No code or functionality is claimed by this documentation entry.

## 2026-10-08 — Actions artifact storage — TODO verification after merge

On first main merge, inspect the **Prune outdated workflow artifacts** summary and verify GitHub Actions actually removed older archives; if Actions write is denied, report the blocker and remove artifacts from GitHub's UI or adjust repository Actions token permissions with user approval. Verify Build still runs all 310 real client captures, uploads at most 55 selected original screenshot PNGs + contact sheet, and comparison runs retain review PNGs. Confirm space is sufficient for subsequent UX development CI. Maintain current last accepted capture artifacts until a verified newer capture is available.

## 2026-10-08 — Proposed next implementation tracks — TODO / pending scope review

A **documentation-only** proposal now organizes all user-requested and repo-audited improvements: [Master Roadmap](FUTURE_IMPROVEMENTS_ROADMAP.md), [Creative Asset Library](CREATIVE_ASSET_LIBRARY_SPEC.md), [Animation UX](ANIMATION_UX_REDESIGN_SPEC.md), [Editor UX/Compositing](EDITOR_CREATIVE_UX_SPEC.md). Nothing from these documents should be described as newly delivered.

**Immediate next steps, in priority order:**

1. **M0 hardware verification:** install current schema5 client/server, back up existing projects, run ordinary Windows 1.21.11 client on target hardware, stress 8× GIF/layer/animation projects; test Sodium/Iris/shaders/resource reloads and dedicated multiplayer. Record actual performance and issues.
2. **M1 Studio UX 3.0 planning and implementation (after confirmation):** four-profile screenshots, compact and ultrawide composition, animation Simple/Advanced workspace, visual key/curve interactions, frame filmstrip and reference drag handles. Preserve current v5 project semantics. User review should precede a giant merge.
3. **M2 Creative Asset Library (after scope review):** build 120–150 original curated starter assets with thumbnails/categories/search and an **Assets** button in Cape/Elytra. Support **drag asset from library onto canvas OR click asset then click/drag to place**. **Default to one newly selected, independently editable layer/object**, with transform/color/opacity/actual pixel edit. Retain current-layer stamping only as an explicit alternative. Plan a bounded editable collection-object model so many stars do not exceed the current 64-layer/channel limit; do not claim it exists before schema/compatibility design. Add scatter and editable Misty Night recipe. Expand toward 300–400 meaningfully distinct assets.
4. **M3 atmospheric brush/compositing**, then **M4 animation export/import/library refinement**; **M5 direct 3D painting** is an isolated feasibility spike. Each new persistent typed layer/format requires migration/compatibility approval first.
5. **DOC-01 cleanup:** move superseded historical statements out of live-status sections of SMART_IMPORT, LOOM_CODES, UI_COMPATIBILITY and old checkpoints; keep PASS_LOG as history and CURRENT_STATE accurate.

**Acceptance rule:** follow [DOCUMENTATION_RULES.md](DOCUMENTATION_RULES.md) after each actual pass; test real screens and hardware, do not count the planning documents as implemented. The earlier exact-source Animation 2.1 acceptance follows below.

## Animation 2.1 / creative authoring — DONE: ready for user testing (2026-10-04)

Accepted runtime source `7288c5877fcc5b708ae1ef945096e8a119323b49`. [Build263](https://github.com/Stoffe101/Loom-Studios/actions/runs/37201892306) passes both jobs: **164 tests, zero failures/errors/skips**, **310 actual Minecraft captures**. [Comparison38](https://github.com/Stoffe101/Loom-Studios/actions/runs/37201892320) passes all **24 suites /210 captures**. Java21, Minecraft1.21.11, Fabric Loader0.18.4, Fabric API0.141.1, Linux/Mesa/Xvfb without optional mods. All32 new creative screenshots were extracted and visually reviewed at1920×1080 and3440×1440, GUI2/GUI3. Timeline rows fit, the live preview is visible and enlarged, custom easing renders, reference/frame/stamp panels have inset controls. Existing full-suite visible-widget bounds/nonoverlap checks pass. Reference targets01/04/05 were reinspected: workshop frame, inset panels, cyan/purple selection. Focused tabs intentionally keep compact controls out of the main drawing area.

Implemented: independent parameter lanes/multiple animated properties, multi-key selection/copy/paste/drag with Undo, bounded Bezier easing editor, timeline zoom/pan and collapsible layer tracks; editor-only references with opacity/lock/above-below/transform; previous/next/both onion ghosts; GIF-to-editable frames with duration/duplicate/delete/reorder/draw/loop; persistent selection-created stamps, favorites, rotation/mirroring/size and six pattern packs. Fixed widget-height shadowing and unbalanced nested scissors, null lane selection, invisible modal choices, portrait stamp scaling, idle creative preview compilation, playback frame labels and actual tick/pixel values.

Real Screen checks pass parameter-key dragging/Undo, curve-handle dragging/Undo, multi-key clipboard, custom stamp painting/persistence/favorites, reference hide/unlock/move with unchanged exported hash, GIF conversion/duplication/reordering/deletion and frame painting/Undo. Schema5/protocol3 integrated transfer passes a3,149,356-byte project through >1MiB fragmentation, validation, equip broadcast, client decode. Existing1–4 project migration and bounded layer/frame storage retain prior tests. New references/stamps remain local sidecars, deliberately absent from project exports/equip/network.

Delivery: [PR23](https://github.com/Stoffe101/Loom-Studios/pull/23). The acceptance/documentation commit changes only docs/README, not this tested runtime. Testing JAR SHA256 `8f99f466df34b014fee15bea0c09931a0bf4ed1f7dc8f4184d5ba22077bb5641`. [Usage](CREATIVE_AUTHORING.md) · [user test route](ANIMATION21_TESTING.md). Later main push workflows are separate and are not implied by this exact-source result.

Next: user-machine acceptance on Windows/RTX5070,8× GIF/many-layer responsiveness, Sodium/Iris/3D Skin Layers/shaders, resource reloads and concurrent dedicated-server transfer. Back up projects before testing: schema5 saves cannot be opened by older releases; both multiplayer peers require protocol3. No known failing automated checks or remaining implementation blocker in this scope.

## Animation 2.1 visual correction — IN PROGRESS (2026-10-04)

Checkpoint d10d62bc40c7bd075f1649fd11e3d2ce42b4b15d passed official Build259 (164 tests,310 captures) and Comparison34 (24 suites,210 captures). Visual review nevertheless found clipped timeline rows and an empty preview at compact GUI3: inner Canvas.height shadowed Screen.height, producing truncated/inverted scissors. Corrected all timeline panel/scissor/playhead bounds to use the screen height. Retained 12px ruler padding, fixed null primary-lane choices, bounded portrait stamp scaling, cached idle creative previews, updated loop frame labels and displayed actual ticks/pixels. Real Screen verification now drags parameter-lane keys and curve handles. Follow-up visual review showed the preview remained blank: each parameter row pushed the outer timeline clip again after popping its lane clip, leaking nested scissors into following widgets. Removed the redundant push to restore a balanced stack. Also connected modal choice rendering in both new screens so open lane/frame/stamp/easing menus remain visible above their panels. The preview is now visible in all corrected captures; increased its embedded height for useful cosmetic inspection while keeping a scrollable timeline below. Curve fixture now demonstrates Custom Bezier easing. New exact-SHA CI and four-profile visual acceptance pending; do not merge yet. Approved references01/04/05 guide inset panels and cyan/purple selection; no new dependencies.

## Animation2.1 core checkpoint — IN PROGRESS (2026-10-04)

164 core tests pass, zero failures/errors/skips, standalone Java21.0.12.1 headless JUnit with384 MiB heap. Includes independent lane evaluation/save/code, Bezier inversion/preservation, cross-lane clipboard, duration/effect migration, frame editing/isolation/timing, onion source immutability, local references/hash exclusion and saved stamp transforms/favorites. Corrected two old schema-number assumptions and PixelPatch array equality found by the first run.

Focused lane/curve/3D preview and reference/frame/stamp/onion screens now connected to both editors. Added32 new actual Minecraft captures across the four profiles (310 total) and real Screen verification. Client build/UI acceptance still pending: local Gradle cannot resolve Fabric Loom through this environment's proxy; official unmodified CI remains authoritative. Do not mark DONE or merge before that evidence.

## Animation 2.1 / creative assets — IN PROGRESS (2026-10-04)

New branch codex/animation21 from main63960f31d6117065eddcf5adb3c55f319173bb75. Isolated worktree leaves two pre-existing modified schema4/test files untouched. Reviewed references01/04/05: cyan/purple selection, inset grouped panels, compact controls and clear labels.

Implementing independent normalized parameter lanes, selected-key clipboard/curve editor/zoom and collapsible layer groups; bounded schema5 metadata preserves compressed schema4 artwork. Existing1–4 load; both peers need protocol3. Image frame editing reuses existing bounded compressed frames. Editor-only references and reusable stamps use bounded local sidecars and never enter exports/equipped hashes.

State: model work underway, no new build/runtime acceptance yet. New controls will use focused tabs rather than extending compact editor rails. Remaining: lane UI, references/onion/frame editor/custom stamps, large-project compatibility tests and four-profile captures, documentation acceptance and publication.

## 2026-10-04 — Schema-v4 authoring — DONE: Linux runtime and visual verification

Accepted runtime source `2fe83e46df4e4da4be7896ae1ec74bc0f4bf77a7`, final checks completed 02:01 UTC. [Build256](https://github.com/Stoffe101/Loom-Studios/actions/runs/37168936038) passes both jobs: **154 tests, zero failures/errors/skips**, with the official unmodified Fabric Loom toolchain and **384 MiB test heap**, plus **278 actual Minecraft screenshots**. [Comparison32](https://github.com/Stoffe101/Loom-Studios/actions/runs/37168935938) passes all20 jobs and178 screenshots. Minecraft1.21.11, Loader0.18.4, Fabric API0.141.1, Temurin21, Linux/Mesa/Xvfb; no optional mods/shaders.

Implemented all17 requested authoring areas in AUTHORING_V4.md: bounded compressed layer storage before 1/2/4/6/8× Cape/Elytra; safe budget rejection instead of the seven-layer crash; PNG/JPEG/GIF/BMP/TIFF/WBMP import; GIF playback; sampled local background removal, tint/brightness/contrast/saturation and image swatches; independent Elytra UV faces; optional Animate entry/return; typed effect parameters and five easing curves; Alpha Lock/clipping/editable masks; exact Wand/Replace selection; five stamps; unfolded cape seam editing; readable ruler/legend. Original legacy files and version checksums are preserved. Immutable compression, renderer/raster/history and multiplayer caches are bounded; upload validation/download decode use bounded background workers.

Verification includes48-layer8× save/load/Loom Code, malformed expansion and entropy rollback, cache reuse/resize/budget, legacy history/tampering, all image decoders/GIF disposal, masks/effects/wing isolation, real Screen stamp drag with single undo, Wand/Replace/mask controls, image sampling/swatches, GIF native-texture changes on both channels, animation return/key dragging and all prior safety/library/import/preview checks. A3,149,338-byte live integrated-server transfer passes native C2S/S2C fragmentation, asynchronous validation, equip broadcast and client decode.

Visual evidence: all seven supplied problem/reference images inspected; all40 authoring originals decoded and four display-profile sheets reviewed, plus20 production import/Home/export captures including the final compact Processing layout. Targets are references01/04/05; focused dialogs intentionally preserve compact editing space. Required1920×1080/3440×1440 GUI2/3 are captured and bounds-tested. The final278-capture artifact11289874252 passed CI; its archive downloaded, but the local execution transport disconnected before final extraction/review. Final cache and backup changes after the reviewed UI source do not change rendering. Do not claim every final PNG was individually reviewed.

Build artifact11290202752 is the verified release JAR; SHA256 `5657a148176cfedf092a02eed5d6af5b27a3574ad00c66c52a289d154faff1ad`. Packaged authoring classes and wing accessor mixin were checked. This acceptance update changes documentation only; runtime evidence belongs to the exact SHA above.

Corrections retained in checkpoint history: capture fixture overwrite, physical Outside/Inside UV mapping, compact635×320 import drag/header collision, a local duplicate variable and original-byte legacy-history hash handling. No known failing automated checks on the accepted source. Both client and server must update to schema4/protocol2; older releases cannot read newly saved projects. Keyframes animate the primary scalar; other typed settings configure the effect. WebP/HEIC/AVIF are unsupported.

Next work: ordinary-client frame times and repeated open/close/resource reload on target hardware; Windows/macOS native loading; Sodium/Iris/shaders; dedicated-server concurrent multiplayer soak; finish extracting/reviewing the final capture archive when workspace access recovers. Cache assertions and integrated transfer tests are not hardware FPS or dedicated-server load evidence.

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
