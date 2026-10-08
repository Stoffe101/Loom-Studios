# Loom Studios — Future Improvements Master Roadmap

**Status:** PROPOSED / planning only. No feature in this document is claimed implemented unless explicitly labeled CURRENT.
**Prepared:** 2026-10-08
**Baseline:** main merge `7c98ac0f8e6261a1abc888e9bf789af4bfae491f`; accepted Animation 2.1 runtime `7288c5877fcc5b708ae1ef945096e8a119323b49`.
**Related:** [Animation UX spec](ANIMATION_UX_REDESIGN_SPEC.md), [Asset library spec](CREATIVE_ASSET_LIBRARY_SPEC.md), [Editor/painting/compositing spec](EDITOR_CREATIVE_UX_SPEC.md).
**Intent:** A living product backlog and design contract, **not** a replacement for [CURRENT_STATE.md](CURRENT_STATE.md), [NEXT_WORK.md](NEXT_WORK.md), or verified implementation records.

## 1. Product goal

Make Loom Studios welcoming to someone who wants an attractive cape in five minutes, while retaining precision for an artist building detailed animated designs. Prioritize a **visual, canvas-first, discoverable** experience over adding yet more equally visible controls.

The approved references are [Home](references/ui/Loom_Studios_01_Home_Screen.webp), [Cape](references/ui/Loom_Studios_02_Cape_Editor.webp), [Smart Import](references/ui/Loom_Studios_03_Smart_Import.webp), [Elytra/Animation](references/ui/Loom_Studios_04_Elytra_Animation_Editor.webp), and [Sharing](references/ui/Loom_Studios_05_Loom_Codes_and_Sharing.webp). Their intended balance is roughly 70% clean modern editor / 30% magical Minecraft workshop. The main canvas/preview must outrank the framing.

### Current foundation (do not rebuild)

- v5 schema/protocol3, old-project migrations, bounded 1×/2×/4×/6×/8× artwork.
- Paint, Image, Gradient layers; masks, Alpha Lock, clipping, blend/opacity, seam and separate Elytra faces.
- Non-destructive Smart Import, PNG/JPEG/GIF/BMP/TIFF/WBMP, GIF playback, image processing.
- Animation 2.1 **already** includes independent per-parameter lanes, multi-key editing, editable GIF frames, onion skin, custom curves, timeline zoom, and collapsible layer tracks.
- **Already** supports selection-created custom stamps, favorites, transform/color options, and six starter pattern packs (Star, Heart, Rune, Flame, Scale, Cloud).
- Save/drafts/version history/library, offline portable codes, static PNG export, 3D previews, and server-synchronized equipped cosmetics.
- Organizational layer groups already exist, but they do **not** provide nested compositing/effects.
- Reference images/custom stamps are currently local editor-side assets, absent from exported/equipped project payloads.

Proposals below extend these systems. Keep current users' projects and workflows available. Do not treat historical TODOs in older documents as current gaps.

## 2. Observations from current workflow captures

Evidence: accepted runtime [Build #263](https://github.com/Stoffe101/Loom-Studios/actions/runs/37201892306) (164 core tests, 310 actual-client captures), [Comparison #38](https://github.com/Stoffe101/Loom-Studios/actions/runs/37201892320) (24 suites/210 captures), and successful [main Build #265](https://github.com/Stoffe101/Loom-Studios/actions/runs/37203088720). These are Linux/Mesa automated runs, not Windows/Iris performance evidence.

- The premium timber frame, icon set, typography and layer list have improved. A wholesale visual restart is neither needed nor wanted.
- At 1920×1080 GUI3, the true canvas, player and cape can be small relative to navigation/decorative chrome and equally weighted controls.
- New advanced editing screens expose many text buttons and technical labels; a user cannot infer the correct sequence of actions by appearance alone.
- Reference image placement uses repetitive direction/size buttons despite an existing transform architecture.
- GIF frames rely on a frame selector/Previous/Next rather than an immediately understandable filmstrip.
- Animation curves, track keys and live cosmetic preview can feel like disconnected work areas.
- Current stamp capability is stronger than its discoverability: there is no large, illustrated, searchable, theme-organized asset catalog.
- Dense/ultrawide screenshots demonstrate the need for distinct responsive compositions, not simply bigger panels or more scrolling.

The accepted CI checked layout bounds and exercised interactions; that does **not** certify aesthetic parity, Windows FPS, every mod combination, or that every future workflow is understandable.

## 3. Priority policy

- **P0:** critical release validation, first-run clarity, fundamental workspace usability, prevent lost work.
- **P1:** high-impact creative workflow and accessibility, including practical asset discovery.
- **P2:** major reusable creative systems after UX stabilizes.
- **P3:** exploratory architecture with dedicated proof-of-concept and budget.

Do not make a new schema/protocol revision just to move buttons. Do not trade away compatibility, safety, project reversibility, or responsive performance for a screenshot.

## 4. Complete backlog and implementation status

| ID | Scope | Priority | Current vs proposed | Primary owner/spec |
| --- | --- | --- | --- | --- |
| UX-01 | Resizable, adaptive editor workspaces; painting/animation/showcase layout presets | P0 | PROPOSED; existing layout is bounded but largely fixed | Editor UX |
| UX-02 | Larger auto-framed cosmetic previews, neutral inspection view, persistent camera presets | P0 | PARTIALLY CURRENT; enhanced framing PROPOSED | Editor UX |
| UX-03 | Animation Simple/Advanced usability overhaul with readable timeline and visual effects | P0 | Powerful current engine; interaction redesign PROPOSED | Animation UX |
| UX-04 | Direct drag/resize/rotate for reference images | P1 | References CURRENT; direct manipulation PROPOSED | Editor UX |
| UX-05 | Real frame-filmstrip thumbnails, drag reorder, blank frame animation | P1 | GIF conversion/editing CURRENT; improved entry/navigation PROPOSED | Animation UX |
| UX-06 | Mask/selection visualization modes and clearer in-canvas feedback | P1 | Masks/Wand CURRENT; visualization overhaul PROPOSED | Editor UX |
| UX-07 | Status-rich layer rows, contextual inspector and uncluttered navigation | P1 | Layer rows/tabs CURRENT; discoverability enhancements PROPOSED | Editor UX |
| UX-08 | Searchable actions, inline help, onboarding, shortcut/focus/narration audit | P1 | Help/tooltips CURRENT; systematic discoverability PROPOSED | Editor UX |
| AS-01 | Large curated categorized stamp/asset library, variants and illustrated browser | P0/P1 | Six pattern packs/custom stamps CURRENT; collection/browser PROPOSED | Asset library |
| AS-02 | Stamp hover ghost, direct on-canvas placement, smart transforms, target-new-layer | P1 | Basic stamping CURRENT; visual placement workflow PROPOSED | Asset library |
| AS-03 | Deterministic scatter brush for stars/leaves/snow/embers | P1 | Simple stamps CURRENT; scatter PROPOSED | Asset library |
| AS-04 | Editable theme recipes: misty night, aurora, enchanted forest, emberfall, etc. | P1 | Existing static templates/presets CURRENT; layered design recipes PROPOSED | Asset library |
| PA-01 | Brush opacity vs flow, hardness/airbrush, organic fog/mist, fade masks | P1 | Layer/color alpha CURRENT; dedicated paint semantics PROPOSED | Editor UX |
| PA-02 | Atmospheric procedural effects and texture layers | P2 | Gradient/effects CURRENT; fog/noise/pattern system PROPOSED | Editor UX |
| CP-01 | Real compositing layer groups (not just organizational groups) | P2 | Organization CURRENT; nested group rendering PROPOSED | Editor UX |
| CP-02 | Non-destructive adjustment layers incl. hue/contrast/tint/gradient map | P2 | Per-image processing CURRENT; generic adjustment layers PROPOSED | Editor UX |
| CP-03 | Editable text/emblem layers, pixel fonts, outline and optional animation | P2 | No dedicated text layer CURRENT | Editor UX |
| EX-01 | Animated GIF/APNG and sprite-sheet export with transparent preview | P2 | Static PNG and editable .loom export CURRENT; animation export PROPOSED | Editor UX |
| IM-01 | Smart Import guided modes, live before/after and explicit scale-quality messaging | P1 | Processing CURRENT; workflow polish PROPOSED | Editor UX |
| IM-02 | Safe WebP decoder investigation (other codecs later) | P2 | Not advertised/supported; decoder decision PROPOSED | Editor UX |
| LB-01 | Home and Library thumbnails, diversity, hover actions, visual polish | P2 | Usable CURRENT; showcase refinement PROPOSED | Editor UX |
| SH-01 | Sharing clarity, accurate permissions terminology, future opt-in server wardrobe | P2/P3 | Local/offline CURRENT; service/server collection not implemented | Editor UX |
| 3D-01 | Direct 3D painting by raycast-to-UV, starting pencil/erase/eyedropper | P3 | Rotatable 3D preview CURRENT; 3D painting PROPOSED | Editor UX |
| ENG-01 | Windows/RTX performance, optional mods/shaders, resource reload, dedicated-server soak | P0 | Automated Linux checks CURRENT; environment acceptance OPEN | Verification below |
| ENG-02 | Artifact review, layout/visual regression, profiling, memory and Undo budgets | P0 | Existing capture/CI CURRENT; enhanced coverage PROPOSED | Verification below |
| DOC-01 | Remove misleading historical-only statements from canonical live docs | P1 | Existing docs contain older superseded sections | Documentation |
| OPS-01 | Workflow/toolchain maintenance (including deprecated Actions versions when present) | P2 | Verify and update per current Actions logs | Engineering |

All IDs above belong to the same plan. A later implementation pass must reference its IDs in PRs and PASS_LOG, and mark actual implemented state in CURRENT_STATE. Completing a high-level area does not automatically mark its subfeatures done.

## 5. Recommended milestones and dependencies

### M0 — Release safety and baseline measurements (P0)

Scope: ENG-01, ENG-02, visual baseline, maintain documentation. Confirm actual Windows launch and responsive editing, 8× GIF/multi-layer workflows, Windows file picker/clipboard, Sodium/Sodium Extra/Iris/shader combinations, 3D Skin Layers, refresh/reload/open-close, multiplayer and large-project protocol3. Capture p50/p95/p99 frame times and peak memory where possible.

**Exit gate:** a reproducible baseline with results, known risks, and no unexplained crashing or data loss. Passing CI is necessary but insufficient.

### M1 — Studio UX 3.0: make the existing system enjoyable (P0/P1)

Scope: UX-01/02/03/04/05/06/07/08 and IM-01. Redesign the animation workspace and canvas/preview density first; provide a frame filmstrip and simple-to-advanced progressive disclosure; preserve existing capabilities. The animation and editor specs define interactions.

**Exit gate:** five-minute novice task, advanced key editing and dense-layer tests can be completed without losing preview/canvas visibility or navigating a giant settings column. No regression in existing .loom, masks, animation or equipped state.

### M2 — Large asset library and guided creativity (P0/P1)

Scope: AS-01/02/03/04 and first PA-01 painting controls. Deliver **read-only built-in asset packs** independent from the existing custom-stamp cap; thumbnail search, favorites, recently used; drag/drop/stamp preview; night/sky/nature packs; scatter; editable theme examples.

**Exit gate:** a player can make a cohesive **Misty Night** design using a gradient, clouds/fog, several star shapes and optional animation without manually drawing those shapes. The result survives save/reload and looks identical equipped and transferred.

### M3 — Atmosphere and non-destructive compositing (P1/P2)

Scope: full PA-01/02 plus CP-01/02/03. First define mathematical alpha/flow semantics and compositing-group order. Separate stable paint engine work from schema migration. Make each new layer/effect editable and bounded.

**Exit gate:** low-opacity atmospheric designs remain predictable under Undo, masks, scaling, 3D preview, export and network transfer, with old designs unchanged.

### M4 — Export, import and sharing refinement (P2)

Scope: EX-01, IM-02, LB-01, SH-01 UX. Add bounded animated exports and stronger Home/Library presentation. Hosted/cloud or server-local sharing requires separate opt-in privacy/security design, explicit permission semantics and a service plan.

**Exit gate:** animation export playback parity, real content preview, correct transparency and no misleading claim that the present local code/fingerprint is a hosted short link.

### M5 — 3D authoring research (P3)

Scope: 3D-01 as a gated prototype. Raycast, normal, surface identity and UV mapping must be deterministic, with full wing/face and seam tests. Only after this proof should the full tool family follow.

**Exit gate:** no incorrectly targeted pixels, no unrelated geometry/skin mutations, parity with the 2D editor, acceptable FPS and unchanged vanilla fallback.

## 6. Global UI acceptance contract

Mandatory physical-resolution / Minecraft GUI-scale pairs (from [UI_COMPATIBILITY.md](UI_COMPATIBILITY.md)):

| Profile | Logical viewport | Expected behavior |
| --- | --- | --- |
| 1920×1080 / GUI2 | approximately 960×540 | Full work layout, readable panels, preview |
| 1920×1080 / GUI3 | approximately 640×360 | Purpose-designed compact composition: task remains visible |
| 3440×1440 / GUI2 | approximately 1720×720 | Use space for larger canvas + useful preview, not dead borders |
| 3440×1440 / GUI3 | approximately 1147×480 | More breathing room; readable contextual dock |

Every milestone involving UI must verify:
- Art/task first. Main canvas, active artwork and preview should not become miniature beside chrome.
- No clipped widgets/text, broken hit targets, orphan menus, double borders, uncontrolled scrolling or trapped floating windows.
- One inspector context at a time; frequent actions reachable without nested modal detours.
- Pinned palette state, zoom/pan, chosen layer/key and unsaved edits survive screen resize/workspace switches when semantics allow.
- Keyboard/focus/narration/tooltip support and visible selected/disabled states; selected glow is semantic, not everywhere.
- Natural interaction parity for Cape and Elytra, including separate wing, inside/sides, alpha/clipping and masks.
- Automated four-profile screenshots **and human inspection of original screenshots**, including deliberately dense fixtures, not only empty/default canvases.
- Real input tests: drag, release, Undo/Redo, cancel, select, delete, open menus, outside click, tooltips and hover occlusion.

## 7. Data, performance, safety and compatibility guardrails

- Existing v5 project, validated legacy migrations, bounded storage, history and protocol3 are the baseline; migration plans are required before new persistent compositing/adjustment layer types.
- Built-in stamp assets are shipped **read-only** resources. A placed stamp becomes normal authored pixels/elements in the editable project. Its appearance must **not** depend on a locally installed stamp pack on another player's machine.
- References are editor-only local sidecars; if ever made portable, privacy/export choices must be explicit. Never silently include private source/reference images in a shared cape.
- Save does not automatically equip. Painting a mask or a custom stamp does not publish a dirty project to the server.
- All procedural/random placements use a saved bounded seed so preview, Undo/Redo, export and peer renders are deterministic.
- Keep compiled texture caches bounded; no per-frame source decoding or network streaming of rendered frames; reject overbudget edits before committing.
- Any optional glow/shader conflict must preserve the base cosmetic. Optional mods remain optional.
- Built-in assets must have clear ownership/provenance and must not copy third-party logos, proprietary characters or unlicensed pack art.
- Native players without the mod are unaffected; a user should not have to install asset-packs on a dedicated server just to see baked cosmetics.

## 8. Research / design decisions still to make

These are **not** architecture decisions yet:

1. Docking model: fixed layout presets first or full resizable splitters with saved workspace state?
2. Brush opacity semantics: whole-stroke opacity vs per-dab flow, alpha compositing and gradient masks.
3. Stamp internal format: small immutable per-resolution pixel assets with optional tint channels or bounded parametric shapes.
4. Template/recipe serialization: apply into existing Paint/Image/Gradient layers first; consider new procedural layer types only with an explicit migration plan.
5. Compositing hierarchy: group masks and clipping across nested groups, layer IDs and animation target integrity.
6. Animated exports: codec/dependencies, frame budget, disposal/alpha behavior, GIF indexed palette loss and per-tick frame scheduling.
7. Hosted/service sharing: privacy, moderation, permission model and server ownership. Never fake this UI.
8. 3D painting: picking algorithm and how non-vanilla/mixin geometry maps to semantic UVs.

Record accepted decisions in DECISIONS.md **when they are made**, not speculatively in this proposal.

## 9. Planning-pass verification and handoff

- **Work changed:** planning documentation only. No Java code, textures, runtime schemas, services or enabled features changed.
- **Basis:** canonical October 2026 feature/status docs, all five approved repo reference images, reviewed workflow screenshot suites and live source inspection.
- **Automated tests in this documentation pass:** none run; previous accepted CI evidence belongs to the SHAs above, not this documentation branch.
- **Risks:** prioritization is a proposal; visual polish acceptance needs user testing; a huge content set must be original, catalogued and performant; planned compositing may require schema/protocol migration.
- **Next:** review the roadmap, approve M1/M2 design goals, select the initial stamp pack and make screenshot-grounded interaction mockups before implementation. Do not begin unrelated feature work until the high-priority interaction design is clear.
