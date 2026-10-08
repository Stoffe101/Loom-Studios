# Loom Studios — Active Mission Board

> **Living checklist** · Updated 2026-10-08 · Track work here as it becomes verified, not merely when it is coded.
>
> This is the **operational queue**, not another wishlist. See [full product roadmap](FUTURE_IMPROVEMENTS_ROADMAP.md), [animation UX specification](ANIMATION_UX_REDESIGN_SPEC.md), [asset library specification](CREATIVE_ASSET_LIBRARY_SPEC.md), and [editor/painting specification](EDITOR_CREATIVE_UX_SPEC.md) for detailed designs. The newest verified runtime remains defined in [CURRENT_STATE.md](CURRENT_STATE.md).

## Status legend and rules

- `[x]` means **DONE and evidence verified** at an identifiable commit/workflow, or a **documentation/infrastructure task** verifiably completed. It does **not** imply that a similarly named prototype PR has been merged.
- `[ ]` is **not complete**. Write **IN PROGRESS**, **BLOCKED** or **TODO** next to it. A green compile is not enough to mark a GUI task done.
- Label clearly **MAIN** vs **OPEN PR** vs **PLANNED**. Never quietly convert a branch prototype into an implemented-main claim.
- Every mission has an exit gate: working feature, real screenshots and interaction tests when relevant, legacy saved data compatibility, and updates to CURRENT_STATE + NEXT_WORK + PASS_LOG. For architecture decisions, also update DECISIONS.
- Screenshots must be original **actual Minecraft client captures** at **1920×1080 GUI2/3** and **3440×1440 GUI2/3**, not screenshots from a generated design mockup. New authoring tasks need realistic **artwork on a cape and Elytra**, not just a library of pretty thumbnails.
- **Quality over item count:** if 50 coherent original assets beat 300 filler shapes, prioritize the 50. Do not mark the full asset catalog done based on numbers alone.

## Snapshot (2026-10-08)

**MAIN:** accepted Animation 2.1 runtime/schema5/protocol3; full planning docs merged; artifact-cleanup/storage changes merged and cleanup confirmed.

**MERGED PR #26 — [Animation Studio first UX slice](https://github.com/Stoffe101/Loom-Studios/pull/26):** commit `0ce5420c6ee0d0dde99c4eee5472791772523df4`; real layer chooser, clearer Simple/Advanced labels and responsive curve+preview with corrected non-overlapping bounds. Standalone [Build](https://github.com/Stoffe101/Loom-Studios/actions/runs/37719395555) and integrated [Build](https://github.com/Stoffe101/Loom-Studios/actions/runs/37719876153) both succeeded. Does NOT complete the full animation redesign.

**MERGED PR #28 — [Editable Asset Library](https://github.com/Stoffe101/Loom-Studios/pull/28):** commit `c6ac027374c68eea4165f643ab10294ad4ea3886`; verified source `90b6dec2390c485c5059459e6a39bcfbff3bca53` passed Java tests, all 326 real Minecraft capture checks, 16 focused Cape/Elytra 2D+worn captures, and all 24 comparison suites. Contains 29 original illustrated assets and separately editable placed Image layers. Broader art curation/collections/atmospheric work remain open.

### Mission 0 — Protect the foundation (baseline and CI)

- [x] **M0-01** Merge complete product-improvements roadmap and detailed Animation/Asset/Editor specs into `main` (PR #24).
- [x] **M0-02** Reclaim full GitHub Actions artifact storage safely; preserve newest evidence/JARs and shorten retention (PR #25 and successful cleanup workflow).
- [x] **M0-03** Existing Animation 2.1 engine accepted: independent parameter lanes, multi-key moves, easing, onion skin, editable imported frames, references and basic stamps. This is **engine capability**, not good final UX.
- [x] **M0-04 · DONE** PR #26 merged `0ce5420c`; exact integrated source `81526b9e` passed Java21 and all **326** actual Minecraft screenshots in [Build](https://github.com/Stoffe101/Loom-Studios/actions/runs/37719876153), plus standalone animation 310-capture [Build](https://github.com/Stoffe101/Loom-Studios/actions/runs/37719395555). Further UX polish is M1/M2.
- [x] **M0-05 · DONE** PR #28 merged into main `c6ac0273`. Exact verified source `90b6dec2`: [Build](https://github.com/Stoffe101/Loom-Studios/actions/runs/37717980898) Java tests/326 captures; [focused 16 views](https://github.com/Stoffe101/Loom-Studios/actions/runs/37717980878); [24 comparison suites](https://github.com/Stoffe101/Loom-Studios/actions/runs/37717980879). Merge reconciliation changed documentation only.
- [x] **M0-06 · DONE** Current import formats/512px limit, schema-v5 portable animation, and 1×–8× compatibility corrected in canonical live docs (PR #30), historical notes preserved.

**Exit:** `main` is a reliable, CI-verified base; no unmerged PR is presented as production-ready.

### M1 progress checkpoint (2026-10-08)

- **Merged in main:** PR #31 delivered the contextual Assets/Edit tab, per-pixel movement, opacity, rotation, flips, improved 2D/worn preview focus and contextual tooltips; build + screenshot + comparison checks passed. These are **partial progress on M1-01/02/03/04**, not the complete mission.
- **Separate recovery PR #32 under test:** responsive inspector sizing and bounded drag handles across Cape/Elytra, the workspace-layout helper and focused four-profile visual review. Do not claim delivered until files are visible on main after merge.
- **Not yet done:** numeric inspector/property field editing, full shared primary-editor tool hierarchy, robust reference/mask modes, all panel-size recovery cases and remaining GUI3 clipping. General ElvUI-style Edit Layout remains M5.
- **M2 work is staged independently:** visual recipe cards, read-only Try mode and undisturbed Advanced tracks are an M2-01 increment, not the completed animation mission.

### Mission 1 — Fix the editor's basic ergonomics (**highest product priority**)

- [ ] **M1-01 · IN PROGRESS (PR #32, visual review pending)** Organize one clear tool rail, one context inspector, and distinct **Browse → Place → Edit** states; eliminate ambiguous text/button walls.
- [ ] **M1-02 · TODO** Fix clipped/overlapping text, giant tooltips over headers, missing focus/hover/selected states, nested/unnecessary scrolling, tiny targets and inconsistent Escape/Cancel.
- [ ] **M1-03 · TODO** Make Cape and Elytra 2D canvas the primary artwork area, with **cosmetic-focused 3D preview** nearby or as a compact focus mode. Provide neutral/light-checker inspection background as an alternative to the scenic courtyard; default zoom should highlight the cosmetic rather than scenery.
- [ ] **M1-04 · IN PROGRESS (sliders and precise placement in PR #32, remaining inspector features pending)** Selected asset/layer Inspector: move, numeric X/Y/size/rotation, mirror, tint/palette, opacity/blend, lock, mask, duplicate, delete, animate and **real Edit Pixels** entry. Keep frequent controls visible while hiding rare ones contextually.
- [ ] **M1-05 · TODO** Make reference-image handles and mask/selection overlays understandable (direct drag/resize/rotate, Fit/Fill/Reset, mask/art side-by-side modes), reusing current transform/selection systems.
- [ ] **M1-06 · IN PROGRESS (eight resized-editor profiles in PR #32 pending)** Define layout profiles for compact 1920×1080 GUI3 and comfortable ultrawide 3440×1440 GUI2, with no feature silently inaccessible at other required profiles.

**Exit:** novice can create, select, move, recolor and save a Cape/Elytra asset without reading documentation or fighting scrolling; advanced tools remain discoverable. Human-reviewed screenshot captures at four profiles show readable workspace hierarchy.

### Mission 2 — Animation people can actually understand (**highest feature UX priority**)

- [ ] **M2-01 · TODO** Build real **Simple Animation**: visual effect cards with previews, understandable Speed/Strength/Direction/Loop controls, Try/Apply/Cancel and a five-minute first animation path.
- [ ] **M2-02 · TODO** Full **Advanced Animation** with one legible resizable timeline, collapsed parameter lanes, colored keyframes, clear playhead/ruler, selected target, seconds/ticks, keyboard operations and contextual help.
- [ ] **M2-03 · TODO** Rework Bézier editor: named easing thumbnails, value axes/units, live cosmetic preview, key→next-key context, predictable drag/hitboxes and Undo; keep current rich underlying engine.
- [ ] **M2-04 · TODO** **Frame filmstrip** with thumbnails, drag reorder, duration labels, new blank editable frame animation (not dependent on GIF import), onion opacity and playback.
- [ ] **M2-05 · TODO** Preserve all Animation 2.1 keys, tracks and values while switching Simple/Advanced and workspace layouts; no hidden flattening or loss of advanced customization.

**Exit:** a beginner animates twinkling stars in five minutes; an expert edits multiple keyframes/curves; both results survive save/reload/equip and remain readable at GUI3.

### Mission 3 — Make an excellent Asset Library, not a gallery of placeholders

- [x] **M3-01 · DONE (first-stage layer model)** PR #28 implements draggable/click-to-place project-owned Image layers with pixel editing, Cape/wing-face isolation, roundtrip and screenshots. A separate multi-object collection is still M3-05, and Windows/server acceptance remains M7.
- [ ] **M3-02 · IN PROGRESS in PR #28** Curate **29 starter illustrated assets** across celestial, sky, nature, ocean, creatures, seasons, heraldry, fantasy and decoration. Visually inspect them on both the 2D canvas **and the worn 3D cape/Elytra**, not only in thumbnails; fix ugly ones before adding more.
- [ ] **M3-03 · TODO** Grow to ~50 genuinely distinct **approved, high-quality** assets, with size/alpha/contrast standards and well-named categories, search, favorites/recent and original/provenance metadata.
- [ ] **M3-04 · TODO** Complete first broad catalog target only after artwork passes quality gates (proposed 120–150; eventually 300–400 when actually justified). Include trees, flowers, animals, ornamental borders, stars/moons, mist/clouds, emblems, weather and texture kits.
- [ ] **M3-05 · TODO** Implement bounded **Asset Collection** with individually selectable editable stars/objects inside a single organized layer so 50 stars do not consume the existing 64-layers/channel cap. Requires a separately approved migration/format/network design and exact Undo/render tests.
- [ ] **M3-06 · TODO** Provide seeded **Scatter Brush**, smart spacing/jitter/density, transform handles, snap/alignment and optional stamp-paint mode; normal placement must remain independently editable by default.
- [ ] **M3-07 · TODO** Deliver editable, layered theme recipes: **Misty Night**, Enchanted Forest, Aurora, Emberfall and others; include design, color, gradient, cloud/mist and optional animation, all independently editable.

**Exit:** original artwork looks great when worn, not merely in the browser. A player makes a layered starry cape with no manual star drawing; reopening, sharing and editing work.

### Mission 4 — Add atmosphere and richer creative authoring

- [ ] **M4-01 · TODO** Proper brush **Opacity vs Flow**, hardness/soft edges, airbrush accumulation, transparent mist/cloud tools and selectable mask fades; accurate behavior at 1× through 8×.
- [ ] **M4-02 · TODO** Deterministic atmospheric procedural textures/effects: fog, sparkle, snow, leaves, rain, textured fabric and seeded light effects, with bounded caches.
- [ ] **M4-03 · TODO** **True compositing layer groups** (not only organizational folders) plus opacity/mask/blend and group animation; perform schema/persistence safety design first.
- [ ] **M4-04 · TODO** Non-destructive **adjustment layers**, editable text/emblem/pattern layers, gradient map and layers that can be animated where supported.
- [ ] **M4-05 · TODO** Improve Smart Import presets/before-after and research bounded WebP decoding separately.

**Exit:** misty, glowing, layered results are predictable, undoable, performant and faithful across 3D preview, equip, export and multiplayer.

### Mission 5 — Make the UI user-customizable (ElvUI-inspired, staged)

This is a **good direction, not the very next task**. The default layout must first work well. Implement progressively rather than shipping an empty "unlock everything" system.

- [ ] **M5-01 · TODO / early alongside M1** Provide a few professionally designed workspace presets: **Painting / Animation / Asset Placement / Preview**, with saved local preferences.
- [ ] **M5-02 · PARTIAL (PR #32 divider; numeric layout widths and timeline docking still TODO)** Drag **splitters** to resize left Layers, central canvas, right Inspector/Assets and bottom Timeline. Offer precise numeric width/height controls with a clear distinction between **Minecraft GUI units** and physical monitor pixels; show live dimensions, min/max bounds and Reset.
- [ ] **M5-03 · TODO** Add an **Edit Layout** mode inspired by ElvUI: move and dock panels, anchor/snap grid, align/distribute, resize handles, panel visibility, keyboard nudges, lock/unlock, Undo/Redo, Reset to Safe Default. The editor's tools must not activate while the user drags the layout.
- [ ] **M5-04 · TODO** Save **named workspace profiles**, optionally per physical resolution/GUI scale; switch/import/export/reset layouts without corrupting a project's authored artwork.
- [ ] **M5-05 · TODO** Responsive safety rules: no panels off-screen after switching GUI scale, no clipped Save/Equip or inaccessible critical actions, recovery/reset shortcut even from a broken saved layout, local-only preferences by default.

**Exit:** a user can make the Layers panel wider, set an exact dimension, arrange the workspace and save it, without ever making the editor unusable on a different screen.

### Mission 6 — Export, home polish and ambitious future work

- [ ] **M6-01 · TODO** **Animated export** (APNG/GIF/sprite sheets), real playback preview and clear alpha/palette/frame-budget warnings, while keeping existing .loom and static PNG exports intact.
- [ ] **M6-02 · TODO** Improve Home/Library with high-quality thumbnails, realistic template previews, reliable search/history and honest offline sharing labels. No fake cloud public/friends links.
- [ ] **M6-03 · TODO / gated feasibility spike** Try direct painting on 3D Cape/Elytra mesh with exact hit→UV mapping. Only expand if it is correct, responsive and compatible.
- [ ] **M6-04 · TODO** Better searchable command palette, contextual hints, accessible keyboard/focus/narration, shortcuts, contrast and comprehensive tooltip reviews.
- [ ] **M6-05 · TODO / optional** Assess opt-in server wardrobe/community sharing only with a separate privacy, permissions and hosting design.

### Mission 7 — Windows, performance and release acceptance (runs alongside every mission)

- [ ] **M7-01 · TODO** Test actual Windows client on target hardware, especially 8× projects, many editable layers, GIFs, large references, strokes and timeline scrubbing; record frame-time and memory budgets.
- [ ] **M7-02 · TODO** Check Sodium/Sodium Extra/Iris/shaders/3D Skin Layers, resource reloads, UI resizing, previews, dedicated server multiplayer/protocol3, and lossless project open/save/migrations.
- [ ] **M7-03 · TODO** Keep complete CI capture assertions and focused artifact screenshots. Verify actual screenshots with human visual review; compare pixel accuracy, 2D canvas and **worn Cape/Elytra** on same project.
- [ ] **M7-04 · TODO** Final pass for visual consistency, copy/keyboard/narration, modpack interoperability, crash recovery and docs/architecture release notes.

**Exit:** user-tested, crash-resistant, responsive and visually approved on ordinary Windows hardware, not just headless Mesa.

## The immediate next five checkboxes

1. **M0-05 completed.** Maintain the accepted Asset Library code/screenshot baseline. The cyan fixture issue was corrected using a clean 4× project and full-atlas background before the accepted screenshot captures.
2. **M0-04 completed.** Begin M1 core editor UX with the prepared contextual Browse/Edit Asset Inspector, cosmetic-focused neutral preview and compact-label fixes; verify 330 capture gates.
3. **M1-02 / M1-03** Solve obstructive tooltips, clipping and preview-camera hierarchy; add neutral background and proper GUI3 view.
4. **M1-04 / M2-01** Improve the contextual asset inspector and make Simple Animation clearly navigable.
5. **M3-02** Review actual art in 2D/3D, reject weak pieces, and only then expand toward M3-03.

**Checkoff protocol:** after each merged, screenshot-reviewed slice, tick only its verified subitems here, add merge SHA and workflow evidence in PASS_LOG/CURRENT_STATE, then update the "immediate next five" list. This board itself does not authorize automatically merging or publishing unfinished code.
