## 2026-10-08 — Actions artifact-storage cleanup confirmed — DONE (CI infrastructure)

[Prune outdated workflow artifacts run #1](https://github.com/Stoffe101/Loom-Studios/actions/runs/37712798078) completed **success** on merged CI commit `6a1bf5361ebc368f2e98f9f19f8c563d702dd9d3`. The cleanup removed **875 older artifacts**, freeing approximately **13.8 GiB** of GitHub Actions storage, while keeping recent JAR/full-capture evidence by retention policy. Future Build and comparison workflows use shorter retention and curated original screenshots (full screenshot assertions still execute). This supersedes the historical "implementation pending" note below. Current Java/GUI functional acceptance remains a separate per-commit requirement.

## 2026-10-08 — M0 live-document accuracy correction — DOCS BRANCH ONLY

Historical implementation notes in SMART_IMPORT, LOOM_CODES and UI_COMPATIBILITY were clearly separated from current capabilities: multi-format JPEG/GIF/BMP/TIFF/WBMP import alongside PNG, 512px embedded source limit, schema-v5 portable animation contents, and 1×/2×/4×/6×/8× canvas performance obligations. New WebP/hosted services/animated-export features remain proposed. This is **documentation-only**, no Java, assets, CI pipeline or runtime change. Branch `docs/m0-current-docs-audit-2026-10-08` remains pending integration into `main` after the Animation UX PR26 acceptance; do not check off M0-06 until merged and reviewed.

## 2026-10-08 — Real 2D/3D asset preview and 29-artwork diversity pass — CI PENDING

On Asset Library PR28's feature branch: 17 original illustrated premium assets extended by **12 distinct original ARGB assets** (Ocean koi/wave/coral; Creatures owl/moth/fox; Seasons maple/snow-globe/rose; Heraldry compass/dragon/shield), bringing the Featured quality-first set to **29**. Existing monochrome/basic stamps remain in their own categories. The browser now adds a **live worn-cosmetic preview**: 2D artwork beside actual 3D player/cape/Elytra on comfortable/wide GUIs, and a single focused 2D↔3D toggle on compact GUI3. Preview reuses existing Minecraft player rendering, Loom NanoVG theme and actual unsaved project data; it neither equips nor uploads content.

Capture fixtures now generate actual **project-owned Image layers** on a Midnight Sky backdrop, for both the Cape and independent left/right Elytra outside faces, and assert the layers exist. Test matrix expands from 314 to **326 actual Minecraft screenshots**: 16 named Asset Library captures (4 GUI profiles × Cape 2D, Cape worn, Elytra 2D, Elytra worn). Focused CI builds a 4×4 review sheet and publishes each actual original PNG. Workflow concurrency cancels obsolete PR revisions instead of consuming runner/storage while main-branch verification remains uncancelled. Prior screenshot art fixture's old cyan crescent is now deliberately covered by a clean night-sky backdrop in the new gallery capture.

**Status:** work committed, **NOT accepted or merged** until latest Java21 tests, focused 16-image CI and full 326-image regression + 24 comparison suites pass, and original captures are inspected for rendering accuracy, correct wing UVs, text clipping, 3D appearance, overlapping widgets and realistic shader/performance limitations. Windows RTX5070 / optional mods / dedicated server remain untested.

## 2026-10-08 — Illustrated Asset Library quality pass — IN PROGRESS, CI/visual review pending

User review of real GUI2/3 screenshots rejected the monochrome 3–9px shapes as insufficiently polished. The first **quality-first replacement** on the Asset Library feature branch adds `PremiumAssetArtwork`: **17 original 28–48px multicolor ARGB pixel illustrations** (shaded crescent, distinctive stars/constellation/planet, clouds/storm/mist/aurora, fir/sakura/oak/mushrooms, crystal/feather/rune halo, heart/laurel). These become the Featured default category. The previous simple monochrome symbols stay available in All/category searches, never falsely marketed as high-detail.

The Asset Library grid now performs bounded nearest-neighbor thumbnail fitting instead of clipping larger art, shortens long labels, shows a modest featured marker, allocates more width and three columns on ultrawide, and chooses an initial placement size proportional to 1×–8× resolution and asset category. Real placement embeds the **full original multicolor pixels** in a project-owned Image layer, preserving exact source for later source-pixel edits, saves/portable codes and equip, with tint opt-in. New `PremiumAssetArtworkTest` checks actual palette richness, transparency, uniqueness, and roundtrip fidelity.

**Status:** feature-branch implementation unaccepted until new Java tests and actual Minecraft GUI captures are green and original screenshots visually reviewed. This is 17 detailed assets, not yet the 120–150 first catalog target nor a guarantee that every drawn image is visually final. At 1×, Minecraft's physical cape pixels remain a fundamental detail limit; recommend 4×/8× for complex art, but preview must be truthful. No schema/protocol change.

## 2026-10-08 — Editable Asset Library first slice — IN PROGRESS, awaiting CI/screenshots

Initial code on `feature/editable-asset-placement-core-2026-10-08` adds a **project-owned, individually editable Image layer** placement core `AssetPlacement`; original categorized `CreativeAssetCatalog` seed artwork (celestial, cloud/mist, trees/nature, fantasy, decorative categories); `LoomAssetLibraryScreen` with thumbnails, filtering/search, drag-thumbnail-to-design and click-to-arm/click-or-drag to place. The library opens from both Cape and Elytra editor Assets buttons. Art is copied into .loom source pixels, independent from local stamp-pack IDs. Image source editing, transform handles and selected-layer return are connected. The old reference/GIF/onion/custom-stamp tools remain behind the **Tools** button.

This is **stage-one architecture only**: one Image layer per placed asset within the existing 64-layer/channel budget. Large independent star collections and hundreds of curated icons are *future* work requiring compatible collection-object persistence. No schema/protocol revision is introduced here. Paint vs placing is separate. Tests added for save/code roundtrip, wing isolation, source pixel edits, bounds and catalog data. Four actual in-game Asset Library screenshots (GUI2/3 at 1920x1080 and 3440x1440) added to the capture workflow; expected captures increment to 314. **No test/screenshot result confirmed yet**, and this branch is not accepted or merged.

## 2026-10-08 — Active mission-board planning checkpoint — DOCUMENTATION ONLY

Created [ACTIVE_MISSIONS.md](ACTIVE_MISSIONS.md), a living, checkable product queue grounded in approved reference screens and the real 2D/worn Cape screenshots. Distinguishes accepted current baseline on main, open Animation UX PR26 and open Asset Library PR28 (29 original illustrated starter assets, focused real-client capture) from still-planned work. Orders stable UI hierarchy, animation usability and asset-art quality ahead of a future ElvUI-like layout editor. Includes exact splitters/numeric sizes before complex docking, 4 required GUI profiles, visible reset, actual Windows/mod compatibility, and schema/network safety.

**No Java, art, CI, runtime, test or schema change in this pass.** No open PR is newly verified or merged merely by creating a board. When a mission is implemented, update this canonical state with **exact SHA and CI evidence** and then check it off.

## 2026-10-08 — Actions artifact retention and compact visual evidence — IMPLEMENTATION PENDING CI

Repository storage was pressured by the 310-PNG main capture archive (~354 MB ZIP for the accepted runtime) and 24 per-suite comparison screenshot ZIPs. Maintenance branch adds `.github/workflows/artifact-prune.yml` to delete **aged (>24 h) artifacts** except the newest full `loom-editor-screenshots` and two newest `loom-studios-dev` JARs. The cleanup requires Actions write permission and runs when merged to main, weekly and on manual dispatch. Uploads now preserve 310 real-client capture/assertion coverage but publish at most 55 relevant full PNGs plus a JPEG review index; comparison suites retain three original PNGs per suite. Shorter retention and setup-java@v5 included.

**Status:** workflow changes require GitHub Actions execution to verify permissions, retained image selection and reclaimed storage. Do not label cleanup DONE until the actual prune job reports deletions and the follow-on Build/Comparison passes. No Java/runtime model change, no schema/protocol impact, no false claim of GUI polish from a storage change.

## 2026-10-08 — Future creative UX and asset-library research — PLANNING ONLY

A complete **proposed** improvement backlog was documented in [FUTURE_IMPROVEMENTS_ROADMAP.md](FUTURE_IMPROVEMENTS_ROADMAP.md), [CREATIVE_ASSET_LIBRARY_SPEC.md](CREATIVE_ASSET_LIBRARY_SPEC.md), [ANIMATION_UX_REDESIGN_SPEC.md](ANIMATION_UX_REDESIGN_SPEC.md) and [EDITOR_CREATIVE_UX_SPEC.md](EDITOR_CREATIVE_UX_SPEC.md). It covers the large themed stamp catalog (stars/clouds/trees/etc.), scatter, soft mist painting, layered recipes, animation Simple/Advanced redesign, frame filmstrip, adaptive panels, compositing, animated export and the previously audited improvements.

**This changes documentation only.** No new assets, runtime features, tests or schema changes were produced by this planning pass. The **latest verified implementation remains Animation 2.1 / schema5 / protocol3** in the immediately following accepted record, runtime `7288c5877fcc5b708ae1ef945096e8a119323b49`; the 2026-10-04 automated/Linux results still apply only to that runtime. Windows, optional mods/shaders and concurrent dedicated-server acceptance remain outstanding. Next implementation priorities remain real-hardware validation followed by progressive editor/animation UX and the creative asset-library work, subject to review.

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

# Loom Studios — Current State

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
