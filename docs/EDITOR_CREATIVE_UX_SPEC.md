# Loom Studios — Editor UX, Atmospheric Painting, Compositing and Export

**Status: PROPOSED (planning only).** Existing features and release tests are recorded in [CURRENT_STATE.md](CURRENT_STATE.md); this document proposes improvements, not implementation.
**Prepared:** 2026-10-08. **Master backlog:** [FUTURE_IMPROVEMENTS_ROADMAP.md](FUTURE_IMPROVEMENTS_ROADMAP.md).
**Companion specs:** [Animation redesign](ANIMATION_UX_REDESIGN_SPEC.md) and [large creative asset library](CREATIVE_ASSET_LIBRARY_SPEC.md).

## 1. Design north star

A player should recognize a real creative editing studio immediately. Preserve the established dark navy/slate panels, cyan/violet active states, original iconography and warm workshop frame, but do **not** spend half the workspace on chrome and tiny previews.

Visual principles inherited from the five [approved references](references/ui/README.md):
- **Canvas/art and player result are primary; decoration is a supporting frame.**
- One active tool and one contextual inspector at a time.
- Give every item an obvious reason to be on the screen. Avoid large walls of identical 22-pixel buttons.
- Keep important actions (Undo/Redo, Save, Save + Equip, Back, export) discoverable without navigating several pages.
- On compact GUI3, intentional focus modes are better than shrinking canvas, timeline and 3D preview into equally tiny boxes.
- High-information ultrawide layouts should expose **more useful artwork**, not taller blank panels.
- Maintain the reference design's 70% clean editor / 30% Minecraft workshop balance; glow only selected/active/focused/primary actions.

### Screens to guide implementation

- Home: [Reference 01](references/ui/Loom_Studios_01_Home_Screen.webp): project hub, thumbnail quality, selected design.
- Cape: [Reference 02](references/ui/Loom_Studios_02_Cape_Editor.webp): large 2D canvas, nearby tools, coherent Layers/Colors/Preview.
- Smart Import: [Reference 03](references/ui/Loom_Studios_03_Smart_Import.webp): visible source → processing → result flow.
- Elytra + Animation: [Reference 04](references/ui/Loom_Studios_04_Elytra_Animation_Editor.webp): wings, semantic faces, preview + animation.
- Sharing: [Reference 05](references/ui/Loom_Studios_05_Loom_Codes_and_Sharing.webp): clarity and confidence, adapted to **real offline** features. The mockup's hosted visibility/chat service is not currently real.

## 2. Workspace and preview redesign (UX-01/02)

### Dockable/focus-first composition

Proposed presets: **Painting**, **Animation**, **Preview**, **Import/Compare**. In comfortable viewports offer resize splitters and saved proportions; at compact sizes offer a few deliberate pane modes. Suggested conceptual regions:
- Tool rail at left with active tool marked; secondary tools behind one **More**/subtool disclosure.
- Main 2D canvas given the largest practical region.
- Side panel with layers or selected tool properties, not two simultaneous tall inspectors.
- 3D player/cosmetic preview always findable; smart collapse or expand where necessary.
- Optional bottom timeline only when actively animating, with drag-to-resize divider.
- Contextual small action bar close to work; menus stay inside window and above the current render layer.

Keep workspace preferences local (mode, split sizes, active tool, zoom/pan, preview pose). Do not let screen rebuilds destroy selected layer/selection, pending stroke, open Inspector context, palette pin or active animation lane without an explicit state reason.

### Preview improvements

- Default camera auto-frames the **cape or wings**, not the entire scenic environment. Preview recenters after target/preset change only when the user has not manually overridden camera.
- Display player+cape, cape-only, Elytra-only, open/gliding poses and relevant inside/edge faces clearly; preserve current character-visible toggle.
- Neutral dark/checkerboard background for pixel inspection and scenic background for showcase; optional lighting strength only if actually supported by the render path.
- Clear controls: rotate drag, middle-pan, wheel-zoom, double-click expand; Reset View should be visible in expanded mode.
- Optional split Before/After for imported/adjusted graphics; actual rendered texture pixels must match candidate.
- Allow preview to scale on ultrawide rather than hide in a fixed narrow strip.
- Do not mutate live inventory, equipped state, skin or neighboring clients when changing preview.

### Layer row density

At a comfortable size: type icon, small thumbnail, concise layer name, visibility/lock, selectable opacity/blend, and meaningful badges for Animation, GIF/Frames, Mask, Clip, Alpha Lock and Emissive. On compact GUI3 collapse low-priority fields into a tooltip/contextual inspector without clipping text. Keep layer hover/selection/drag states visually different. Existing organization groups remain recognizable while true render groups are planned separately.

## 2A. Asset Library entry, object selection and properties

The [Creative Asset Library spec](CREATIVE_ASSET_LIBRARY_SPEC.md) is an **object-first editor workflow**, not simply a paint brush. In both Cape and Elytra editors, users click **Assets**, then either (A) drag an asset thumbnail directly onto the canvas and release, or (B) click an item to attach a translucent preview to the cursor and click/drag on the canvas to place it. **Default result: a newly selected independent editable asset layer**, with direct transform handles and properties. The asset can be renamed, moved, scaled, rotated, recolored, opacity-adjusted, masked, animated, and pixel-edited later.

The library drawer and layer selection must work together:
- Do not close or completely replace the main editor when opening Assets; preserve canvas, preview and existing unsaved edits.
- Hovering the canvas shows source-size-correct ghost and valid wing/face targeting. Drop outside cancels. After drop, the new asset remains selected for editing.
- Selected asset inspector shows simple high-frequency controls and `Edit Pixels` entry. `Edit Pixels` must genuinely allow source artwork modification, possibly by editing/copying/rasterizing into a Paint layer while retaining an undoable path, not pretend a static image has native painting when unsupported.
- **Stamp Into Current Paint Layer** is an opt-in alternate painting mode, never silent default.
- One asset per ordinary layer works for a moon or tree, but a 50-star sky should become a bounded **Asset Collection** with independently selectable object instances. This is **future typed architecture**, not existing schema5 functionality, and requires versioning/migration, deterministic network export and layer-cap safety.
- Clear separation between selecting/editing the placed object versus choosing an asset to place. On-canvas dragging of an already placed item moves it instead of repeatedly stamping copies.
- Inspect resize/swap/duplicate/rename/delete/undo/redo with various zoom levels, after Save and with separate Elytra wings.

Acceptance: without knowing Loom internals, a user drags a star from the library onto the cape, recolors and resizes it, reopens later and still moves that star independently. A second user clicks a pine-tree asset and drags to place it on one Elytra wing. Both outcomes must look correct in 2D and 3D, and neither requires a custom texture pack on another multiplayer client.

## 3. Selection, masks and guided editing (UX-04/06/07)

**Current:** Wand, exact selection, mask paint/reveal/hide/invert, alpha lock, clipping and color/gradient controls are present.

**Proposed:**
- Direct manipulation for Reference Images: drag/resize/rotate handles, Fit/Fill/Reset, modifier snapping, keep-aspect, position/size readout, correct lock semantics. Reuse Smart Import transform expectations.
- Selection presentation: configurable outline/highlight opacity, precise one-pixel marquee, keyboard modifier hints, edge snapping and obvious selection-clear state.
- Mask preview modes: Composite / Mask Only / Mask on Artwork / Original-vs-Masked; show brush cursor/strength on mask, invert/clear with safe Undo.
- When editing inside a Cape/Elytra face, display face/wing identity at a glance. A lock/Alpha Lock/mask prohibition must have a clear disabled reason.
- Make Layer Properties contextual and group relevant settings. Do not make a new dedicated modal for each trivial toggle.
- Brush controls and palette placement may be compact but must remain reachable without giant modal slabs.
- Persist pins/zoom/pan/selected color across appropriate context switches; authored state and preview state must remain distinct.
- Bring Reference direct manipulation to equivalent quality on both Cape and separate Elytra wing faces; do not accidentally paint another wing.

## 4. Painting engine: opacity, flow, soft brush and mist (PA-01)

### What currently exists

Layer opacity and selected color alpha already work, as do masks, blending, gradients, stamps and image-layer color adjustments. These are **not** the same as a carefully specified soft painting engine.

### Desired paint controls

- **Opacity:** caps the overall coverage/alpha of the *whole continuous stroke*. Repeated overlapping dabs in one stroke should not become unexpectedly darker when Opacity=25%, unless chosen Flow semantics say otherwise.
- **Flow:** per-dab incremental color deposition; holding a still airbrush or repainting can accumulate gradually and reproducibly.
- **Hardness/Feather:** pixel-radius falloff from solid center to softened edge. At 1× this naturally appears pixelated; do not hide it behind an inaccurate smoothed preview.
- **Brush Shape:** Square/Circle/soft Airbrush and currently selected custom stamp.
- **Spacing and Smoothing:** density of dabs along path and optional path interpolation; bound computational cost.
- **Blend and Alpha:** standard source-over alpha, clear erase semantics, no unexpected desaturation or black halos.
- **Stylus pressure:** optional future input with mouse/keyboard fallback, not assumed to exist.
- **Symmetry:** current mirror rules must still work with opacity/flow and semantic face clipping.

Brush stroke preview and committed output must match at 1×/2×/4×/6×/8×. Undo is one compound edit per continuous stroke. Alpha Lock and editable layer masks have priority over proposed brush effects. Define mathematical compositing semantics and automated tests **before** integrating UI.

### Misty Night example

1. Deep indigo-to-violet gradient background.
2. Add celestial stamps: crescent moon and a varied scattered star field.
3. New "Low Mist" layer, with soft brush/gradient mask at low opacity over the bottom third.
4. A second cloud/mist layer behind stars, optionally pale cyan, softly blended.
5. Optional distant pine silhouettes and tiny fireflies.
6. Optional Twinkle (stars) and Drift (mist), using existing animation engine.
7. Preview on the actual cape/Elytra, adjust alpha and animation, Save/Equip.

**No shader required.** Optional emissive is an enhancement, never a dependency. Real low-resolution pixel behavior should be explained honestly.

### Procedural atmospheric patterns (PA-02)

Potential non-destructive effect/asset types: seeded noise haze, translucent cloud bands, snowfall, leaf scatter, glitter/starfield, frost edges, light rays, soft vignette, abstract textile patterns. Prefer a deterministic cached evaluator and explicit seed. A separate persistent procedural layer type would require a documented schema/protocol migration; initially pre-render as editable Paint layers where viable.

## 5. Real compositing groups and adjustments (CP-01/02)

### Current gap

There are organizational named groups/batch operations and normal Paint/Image/Gradient layer types. These are not nested render groups or generic adjustment layers.

### Compositing groups

A future "Group" should own an ordered child subtree, distinct from folder/tag organization. Plan explicitly:
- Visible/hidden, lock, group opacity, blend mode and group mask with clear nesting.
- Optional animation of a whole group's opacity or supported parameters.
- Deterministic compositing/isolated blend behavior; define group vs child blend evaluation before coding.
- Drag layers in/out, move/duplicate group, ungroup with no pixel loss, multi-select actions and sane Undo.
- Preserve stable layer/track IDs and reject cyclic/invalid membership.
- Cap nesting and flattening cost with bounded caches and clearly explained limits.
- Migration path from existing flat layers and organizational groups. Never silently change old artwork appearance.

### Adjustment layers

Potential operations (ordered, bounded and reversible): Brightness/Contrast, Hue/Saturation, Tint, Gradient Map, Levels/Curves later. Layer applies to content beneath, optionally clipped to current group/selection/mask. Effects must not permanently mutate source pixels. Compatible with animated evaluation as appropriate and safe at 8×.

One-layer Image processing remains available; introducing generic adjustments should **not** remove or reinterpret those saved settings. Visually distinguish Adjustment from Paint/Image/Gradient and include one-click bypass comparison.

## 6. Text, emblems and procedural artwork (CP-03)

**Proposed, not current typed layer kinds.**

- Text layer: crisp pixel-font selection, alignment, horizontal/vertical layout, size/spacing, outline/stroke, shadow, tint, rotate and clip. Show exact raster result at source resolution.
- Emblem/badge builder: shields, crests, simple frames, geometric composition using library stamps.
- Pattern layer: repeating checkers/stripes/scales/sparkles/ornamental borders with scale, tiling, seed, direction and palette, independent of hand painting.
- Text and patterns can optionally be animated using existing track architecture, but do not invent partially supported runtime controls.
- Decide whether source stays typed/editable or is converted to a normal Paint layer. Persisting new typed layers is a separate migration and compatibility decision.

## 7. Smart Import polish and format research (IM-01/02)

The current app already imports PNG/JPEG/GIF/BMP/TIFF/WBMP and has non-destructive image layers, transforms, background removal, tint, brightness/contrast/saturation, reduction/palette and image-to-swatches.

**Proposed experience:**
- Guided processing starter cards: Direct, Clean Logo, Pixel Art, Photo, Remove Background, with explicit "Advanced Processing" controls.
- Large, clear Before/After split/slider and 3D candidate preview, correct alpha checker, output atlas/face scope.
- Show chosen resolution and explain the difference between 8× editable output and a lower-detail input image. Avoid implying invented resolution.
- Current Image layer retains edit entry and precise source processing; don't flatten unless explicitly chosen.
- Keep semantic Elytra face/linked wings visible; no overlapping/twin-wing duplication.
- Research optional WebP decoder separately (test licensing, bounds, security and Windows/macOS). AVIF/HEIC can remain unsupported without a deliberate decoder design.

## 8. Animated export and sharing (EX-01/SH-01)

### Export

**Current:** Save portable/editable .loom, offline codes, static Cape/Elytra PNG render and imports.

**Proposed:**
- Export animation as **APNG**, **GIF**, or sprite-sheet plus JSON/timing manifest, with format limitations explained.
- Choose current Cape/Elytra channel, time range, frames/second vs exact tick sampling, transparency, repeat mode and intended output scale.
- GIF transparency and palette color reduction may visibly change alpha/color compared with 32-bit PNG/APNG; show a real candidate preview and warnings.
- Hard caps on frame count, dimensions and total decoded/encoded bytes, with cancellation and progress without freezing the Minecraft main thread.
- Output must match evaluated animation at exported timestamps. Support both procedural effects and editable GIF frames.
- Filenames never silently overwrite; no remote upload required.

### Sharing

Current "LS-..." design ID is a local fingerprint, **not** a hosted lookup code; "LSP1:" portable project code carries actual content. Sharing UI must never pretend that fictitious public/friends/private permissions or clickable chat cards are working. Present offline/export flows clearly and use actual names. Future server-local wardrobe/showcase is optional, requires opt-in publishing, server-side permission/rate-limit storage plan and a security/privacy review before coding. A public gallery/cloud service is a separate project.

## 9. Home / Library / showcase refinement (LB-01)

- Better art-framed recent cards and distinct rich template thumbnails; use actual project pixels not fake preview advertising.
- Selected project gets a useful, reliably framed live 3D preview and clearly labeled Edit/Equip/Export actions.
- Existing favorite-first sorting, filters, folders/tags, Trash and version history remain intact.
- Surface template/theme recipe packs with illustrated cards rather than add a seventh permanent rail of tool buttons.
- Showcase presentation may have more timber/lantern decoration; authoring layout must be quieter.
- Empty/loading/failed-library states should explain next action, not resemble missing UI.
- Keep thumbnail caches bounded; scrolling should not compile/reupload each design continuously.

## 10. Direct 3D painting: separately gated prototype (3D-01)

This is high risk but potentially a signature tool:
- Raycast player/cape/Elytra geometry to semantic surface, wing side, UV and texture pixel.
- Pencil, Erase, Eyedropper proof-of-concept on the 3D preview; identical authored pixels to 2D editing.
- Deal correctly with rotations, pose, gliding model, layer locks, symmetry, zoom/pan, seam edges and backfaces.
- Never paint skin, inventory, real-world player model state or the opposite wing inadvertently.
- Support hover hit marker/pixel grid and explicit "Paint on 3D" toggle to prevent confusion with normal rotate-drag gestures.
- No dependency on a particular shader or rendering optimization mod. Evaluate performance and compatibility before broadening tool set.
- If a correct UV mapping cannot be guaranteed, disable the tool with explanation rather than guessing pixels.

## 11. Discoverability, help, accessibility and input (UX-08)

- Optional first-use guide for four paths: Draw, Stamp, Animate, Import.
- Searchable command/action palette (Canvas, Layers, Save, Animate, Stamp, Reference, etc.) with keyboard-accessible alternatives.
- Contextual help with clear one-line descriptions and action-specific shortcuts; do not show long instructional paragraphs on every editor frame.
- Consistent iconography/states, selected/hover/disabled/focus contrast, tooltip and narrator strings.
- Check tiny hit targets, scroll capture, keyboard focus, tab order, drag thresholds, Escape/Cancel behavior, color dependence and alternative to Ctrl/Shift-only actions.
- First-time flows should avoid technical schemas/ticks/parameter value jargon. Expert controls remain accessible rather than removed.

## 12. Engineering and QA acceptance

Every meaningful implementation pass must update canonical docs per [DOCUMENTATION_RULES.md](DOCUMENTATION_RULES.md). No feature is DONE until verified. Test matrix:

- All four required 1920×1080 and 3440×1440 GUI2/GUI3 profiles, plus windowed/compact cases.
- Screenshot comparison using **real populated canvases**, many layers/animation tracks, masking, palette popups, stamp drawer and import before/after.
- Inspect original captures and record approved-reference adaptations; bounds tests alone don't certify readability.
- Click/drag/release/scroll/wheel/keyboard/focus/Undo/Redo tests; no overlap, invisible scissor, unclickable action or trapped dialog.
- Cape/Elytra both channels, independent wing inside/outside/top/edges, transparent/opaque preview, alpha/masks, 1× through 8×.
- Large-project budget rejection before committing unsavable state, history/version recovery and format migration.
- Saved/equipped/project/export/hash invariants, no accidental publication of dirty project or private local reference assets.
- Windows real hardware (ordinary game client, including 8× GIF and large layers), resource reload/leak tests, Sodium/Sodium Extra/Iris/shaders/3D Skin Layers, dedicated-server concurrent multiplayer/protocol3.
- Frame-time p50/p95/p99, memory allocations and GPU uploads under idle/edit/playback; previous caching tests are not FPS evidence.
- Provenance/attribution and original visual quality of any shipped artwork/font/icon/dependency.

## 13. Implementation acceptance order

1. UX-01/02/07: real responsive layout, usable cosmetic preview, contextual inspectors, accessible action surfaces.
2. UX-04/06/08: direct reference transforms, mask feedback and discovery polish.
3. PA-01: deliberate opacity/flow/soft brush semantics; then theme/scatter integration with the [Asset Library spec](CREATIVE_ASSET_LIBRARY_SPEC.md).
4. CP-01/02/03: groups, adjustment and text/pattern architecture, each gated by versioning design.
5. IM-01/EX-01/LB-01/SH-01: targeted import/export/library/share quality, real animated outputs.
6. 3D-01: proof-of-concept when earlier editor workflows are stable.

**Current status:** this is a design/research deliverable only. Do not imply shader validation, code implementation, asset creation or user acceptance from its existence.
