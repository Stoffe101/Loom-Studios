# Loom Studios — Creative Asset Library, Stamps, Scatter and Theme Recipes

**Status: PROPOSED (planning only).** Built-in six-pattern starter packs, custom stamps/favorites and current painting are implemented; the extensive illustrated library and new placement tools below are **not**.
**Prepared:** 2026-10-08. **Parent:** [Master Roadmap](FUTURE_IMPROVEMENTS_ROADMAP.md), IDs AS-01 through AS-04 and PA-01/02.

## 1. Creative promise

A player should be able to create a coherent **starry, foggy night cape** without hand-drawing each moon, star, tree or cloud, yet still have control over color, scale, opacity, spacing, lighting, layers and animation.

The library should contain **many distinct usable designs**, not 400 copies of one star. Treat visual presets as composable building blocks, not stickers trapped in one flattened texture. Support both Cape and Elytra, every editable resolution and both linked/separate-wing workflows.

## 2. Clear distinction: what we have and what we want

**Current (verified v5):**
- Fixed stamp shapes plus six built-in patterns: Star, Heart, Rune, Flame, Scale and Cloud.
- Create Stamp from exact selection; saved custom stamps, favorites, mirroring, rotation, resizing, recoloring and drag painting in the focused Assets screen.
- Layer opacity, per-color alpha, masks, blend modes and animation effects already exist.
- Some artistic templates and preset animations already exist.

**Proposed:**
- A visible **Asset Library** button in **both main editors**, opening a thumbnail browser. This is an asset-object placement tool, **not primarily a stamp brush**.
- Extensive original icon/artwork catalogue with scale, density and variant diversity.
- Two first-class placement paths: **drag an item from the library onto the cape/Elytra**, or **click to select then click/drag to position the live ghost on the canvas**.
- **Every single placed decorative asset creates its own editable layer by default** (or becomes a separately selectable editable object inside a purpose-built collection layer). Explicit Stamp/Paint mode may rasterize into the current Paint layer, but is **not** the default.
- Cohesive theme recipes combining assets, gradients, mist, palette, compositing and optional animation.
- More helpful alpha painting/soft atmospheric brushes and procedural environment effects.
- Built-ins stored as separate read-only assets so they do not exhaust the 64-entry **user-created** stamp library.

These are extensions of existing features, not a reason to replace their validated persistence.

## 2A. Core interaction clarification: place editable assets, not permanent paint dabs

**User intent, clarified 2026-10-08:** The library opens with a button in the Cape or Elytra editor. The player can (A) **drag a chosen asset directly from the library onto the design and release to place it**, or (B) **click the asset in the library, move to the canvas, then click or click-and-drag to place/position it**. Placement displays a translucent exact-pixel ghost and snapping guides. After placement the player should be able to select the asset on the canvas or in Layers and modify it **at any time**.

**Default semantics (required):** One distinct asset object, independently editable. For low counts, it should appear immediately as a named **editable layer** such as `Crescent Moon`, `Pine Tree`, or `Star 01`. Do **not** secretly stamp it permanently into the currently selected Paint layer. Apply as a single undoable operation, select the new item, leave the Asset Library accessible, and show its contextual controls.

**What "fully editable" must mean:**
- Position precisely by dragging; scale with handles (including non-uniform where safe), rotate, flip, align, snap, duplicate, copy/paste and change layer order.
- Set opacity, tint/colors (multicolor retained vs recolor as explicit choices), blend mode, clipping/mask and supported emissive/glow effects.
- Animate the asset/layer with existing effect tracks, preserving exact target IDs after reordering/duplication.
- Edit the asset's actual constituent pixels using familiar Pencil/Eraser/Fill/Wand tools. `Edit Pixels` must be a **real editable mode** backed by saved artwork, or an explicit reversible conversion/copy to Paint data, not a misleading button that only changes opacity.
- Rename, lock, hide, delete, undo/redo; preserve all edits after save/reopen/portable-code import and network equip. Missing local library assets must not break previously placed objects.

**Architecture constraint and staged recommendation:**
- **First implementation without forcing an immediate schema update:** one placed asset = a project-owned Image layer (or Paint layer only when the player explicitly chooses `Rasterize/Stamp Into Current Layer`), using existing independent source/transform/opacity/mask capabilities. Inspect/extend actual pixel-edit support rather than assuming all Image sources are paintable. Embed/copy source pixels into the `.loom` project; the preinstalled catalog is only an authoring convenience.
- The current project limit is **64 layers per channel**. A night sky with 80 individual stars cannot consume 80 layers. Do not silently exceed budgets or flatten independent objects on reaching the limit.
- **Scaled solution (future typed model):** an `Asset Collection` / `Asset Composition Layer` with **individually addressable child instances**. One `Starfield` collection layer might contain dozens of stars, each individually selectable/movable/recolored/rotated/edited without spamming the visible layer list. Collections show expandable object rows in an inspector. A distinct `Moon` or `Tree Line` may stay as its own layer. An object is not the same as a layer: they can be independent while sharing the parent collection for composition.
- Adding persistent instance objects likely requires a new bounded schema/protocol with migration, stable instance IDs, exact embedded source pixels, deterministic rasterization, cache invalidation, grouping masks/effects, and tests. **This is PROPOSED, not something schema5 currently supports.** Do not implement it implicitly or promise hundreds of independent objects before architecture validation.
- Stamping into an existing Paint layer remains an **explicit secondary quick-paint option** for experienced users. Scatter may place a whole starfield as an editable collection or, in an intentionally selected raster mode, as a Paint layer.

**UI language:** Label the entry **Assets** or **Asset Library**, not just `Stamps`, to convey that the library includes stars, tree silhouettes, clouds, decorations, emblems and other reusable artwork. A separate `Stamp Brush` submode preserves repeated-dab painting.

### Exact behavioral examples

1. Click **Assets** → **Stars** → drag an 8-point star thumbnail onto the cape → release → `Eight-Point Star` appears as the selected independent layer with transform handles; change color/opacity and move it later.
2. Click **Assets** → **Nature** → `Pine Tree` → click on Elytra outside-left face then drag to size → release → tree appears without altering the right wing or underlying sky layer.
3. Click **Assets** → **Celestial** → `Crescent Moon` → place → choose **Edit Pixels** → modify the moon's edge pixels → switch back to transform/animate; reopening preserves the changes.
4. Want 50 stars? Select **Starfield Collection** or **Scatter into Editable Collection** → place many variants → select an individual star inside the collection and recolor or reposition it. Do not create 50 flat layers unless the player explicitly requests independent layers and budgets allow.
5. Press Escape while placing or dragging to cancel with **zero change**. An asset dropped outside canvas never creates a ghost layer or silently paints stray pixels.

## 3. Catalogue strategy and size

**Proposed staged targets, not existing counts:**
- **Starter release (M2):** 120–150 polished, individually meaningful built-in stamps in a first group of packs.
- **Expanded library:** 300–400 original stamps over time, organized in cohesive sets.
- **Long-term:** additional specialized art packs only when original assets, tests and usability justify them. Count is not a quality goal.

Every stamp receives ID, human label, category, keywords/synonyms, tags, creator/provenance, native dimensions, intended resolution, tintability, default anchor, alpha bounds and optional variant family. Use names/tags independent of visual art. Favorites and recently used are dynamic collections, not duplicated assets.

### Proposed catalogue taxonomy

The entries below are **asset brief examples**, not assets already drawn or promised as a fixed per-category count.

| Pack | Proposed examples and distinct variants |
| --- | --- |
| **Stars & Cosmos** | 4-point/5-point/6-point/8-point stars; tiny pixel stars; star clusters; sparkles; sparkle trails; cross stars; comet; meteor; crescent moons (several phases); full/half/new moons; Saturn-like planet; ringed planets; constellations; nebula wisps; galaxies; orbit circles; shooting stars |
| **Clouds, Sky & Atmosphere** | Thin cirrus; puffy cumulus; layered drifting cloudbanks; dark storm clouds; fog banks; ground mist; wisps; curved mist ribbons; small vapor curls; moon halo; horizon haze; light beams; sunrise streaks; sky streaks; smoke puffs; cloud edges |
| **Trees & Forest** | Pine/spruce silhouettes; oak; birch; willow; cherry tree; bare winter tree; twisted fantasy tree; palm; bamboo clump; small sapling; pine branch; falling leaf clusters; roots; stumps; tree line/parallax forest silhouettes; moss; vines |
| **Flowers, Plants & Fungi** | Daisy; rose; sunflower; tulip; lavender; dandelion; sakura blossoms; fern; ivy; grass tufts; cattails; mushrooms (multiple cap shapes); glowing fungi; berries; thorns; succulent; lily pad; lotus; mushroom clusters |
| **Animals & Creatures** | Cat silhouette; fox; wolf; rabbit; owl; raven; dove; moth; butterfly; bee; dragonfly; bat; koi/fish; turtle; deer antlers; bear paw; dragon silhouette; phoenix emblem; small fantasy familiar shapes. Avoid character IP |
| **Weather & Seasons** | Different snowflakes; snow piles; icicles; raindrops; rainfall streaks; lightning bolts; snow dust; sun rays; rainbow bands; storm swirls; wind gusts; autumn leaf; falling leaves; scattered petals; frost edges |
| **Fantasy & Magic** | Rune families; magic circles; sigils; wand spark; spell burst; enchanted crystal; potion vial; magic mist; energy crescent; fiery emblem; spectral flame; floating shard; ornate charm; stars inside circles; alchemy signs; portal arcs |
| **Borders & Geometry** | Dotted and dashed lines; diamonds; chevrons; triangles; zigzags; corners; scallops; hearts; spirals; circles; badges; ornamental trims; medieval textile borders; pixel lace; knots; repeating tile strips; symmetric cape hems |
| **Emblems & Heraldry** | Shields; crest outlines; crowns; wings; laurels; crossed tools; swords as stylized artwork; compass; mountain badge; anchor; pennant; abstract guild insignia; banners; geometric heraldic dividers |
| **Fire, Water & Elements** | Flame tongues; embers; sparks; ash; smoke; waves; splash; whirlpool; water droplets; bubbles; ice shards; crystals; rock chips; sand swirls; electric arcs; wind curls; glow rings |
| **Technology, Sci-Fi & Abstract** | Circuit traces; pixel heartbeats; gears; cog arcs; hexagons; neon grids; HUD corner marks; binary bits; glitch shards; retro icons; small satellites; holographic strips; energy bands; chromatic pixels |
| **Objects, Symbols & Whimsy** | Bows; ribbon ends; lanterns; keys; books; feathers; tiny charms; bells; cupcakes; cookies; presents; badges; music notes; coffee cup; paw prints; footprints; compass rose; snow globe |
| **Texture & Fabric Details** | Stitch patterns; seams; patches; leather rivets; chain links; worn edges; speckles; marble veins; cracked stone lines; fabric weaves; paint splatter; scattered confetti; decorative noise clusters |
| **Animation / Particle Kits** | Twinkle variants; paired sparkle frames; falling particle clusters; drifting leaves; glowing motes; swirling fragments; firefly shapes; rain/snow sprites; smoke wisps; dust motes, each designed to compose with effects rather than necessarily contain embedded animation |

**Variations that add genuine value:** solid/silhouette/outline, simple/detailed, left/right/symmetric, small/medium/large, clean/rough, flat/gradient-ready, tintable vs palette-preserving, day/night/seasonal visual families.

### Suggested first-release curation

Prioritize categories users can combine immediately:
1. 25–30 celestial pieces including 10+ distinguishable star/sparkle types;
2. 20–25 clouds/mist/weather elements with deliberately different silhouettes;
3. 20–25 tree/foliage silhouettes and little forest edges;
4. 20–25 fantasy/flora symbols, hearts and decorative shapes;
5. 15–25 borders/ornaments and particle accents.

Targets may be adjusted after reviewing actual asset quality and density. Do **not** fill quotas with trivial rotations that a rotation control already provides.

### Style and quality

- Crisp Minecraft-compatible pixel art at intended base dimensions; deterministic nearest-neighbor scaling when pixel-perfect mode is active.
- Include some richer 4×/8× source variants that justify high-resolution authoring; do not merely upscale low-resolution icons and advertise extra detail.
- Transparent backgrounds, no unintentional white/black halos, correct alpha edging when composited over black, checkerboard and busy photographs.
- Color-independent/tintable stamps should preserve alpha and shading hierarchy under palette mapping.
- Directional pieces should use semantic anchors: center for stars/hearts, trunk bottom for trees, ridge/baseline for borders, floor/horizon for fog/cloud banks.
- Standardized preview framing lets users understand actual ink coverage, not just empty asset margins.

## 4. Asset Library browser: player interaction

### Entry and layout

A conspicuous **Assets** button appears in both Cape and Elytra editors. Clicking opens a **contextual Asset Library drawer**, not a permanent tool wall or a separate full-screen editor. The canvas remains visible as a live drop target. At narrow GUI3, use a compact bounded overlay/drawer and collapse it automatically or temporarily after the player picks an asset, allowing the player to place it.

Conceptual layout:
- Search (e.g., "crescent", "pine", "cloud"), Favorites and Recent.
- Categories as thumbnails/pills: All, Stars, Sky, Trees, Nature, Fantasy, Shapes, More.
- Grid of actual asset thumbnails with short name, variant tags and accessible hover/focus preview.
- Selected asset shows **Drag onto design** / **Click to place** help and a small preview.
- Default mode is **Place Editable Asset**. Advanced choices are **Add to Existing Collection**, **Create Collection**, **Scatter Brush** and **Stamp into Current Paint Layer** (explicitly destructive/rasterized).
- After placement, contextual property controls are near the artwork or in the existing inspector: Position, Scale, Rotation, Flip, Color/Tint, Opacity, Blend, Mask, Glow, Edit Pixels, Animate.
- Save and Undo remain reachable. Collection is the only scrolling area; the rest of the editor's chrome does not scroll.

**Two equally supported gestures:**
1. **Drag from library:** press/drag an item thumbnail, ghost follows cursor across browser boundary into design, release on a valid pixel/face to commit the independent editable item, release elsewhere to cancel.
2. **Click to pick:** click a thumbnail to arm a placement cursor; moving into canvas shows true-scale ghost. Click to place at default size, or press-and-drag to size/position and release to commit. Escape/right-click cancels; the selected item can immediately be repositioned with visible handles.

Provide accessible keyboard placement with Move/Nudge/Confirm/Cancel for players unable to drag. No accidental painting when drag enters/stops over the canvas. A placement does not equip or publish cosmetic content.

### Search and discovery

- Search on label, aliases, tags, category and family (e.g. "fog"/"mist"/"haze"; "fir"/"pine"/"spruce").
- Favorites, Recents, pack filtering, optional "monochrome" and "seam/border" filters.
- Provide thumbnail variations and size previews without recalculating full raster data on every mouse movement.
- Sort options: Recommended, Name, Recently Used; keyboard arrow/Enter accessibility, tooltips and narrator descriptions.
- Empty state suggests broader terms; do not display a blank catalog with no explanation.
- Packs can be toggled/filtered, but shipped read-only packs should not be overwritten by custom stamp edits.

### Placement and edit semantics

1. Every default placement creates an **independently editable layer/object**, visibly selected with canvas transform handles and a clear name. The player can come back hours later and change it.
2. Transparent live ghost accurately matches final authored pixels at 1× through 8×; coordinate mapping remains exact under zoom, pan and GUI scale.
3. Drag from the library **or** click-to-arm followed by click/click-drag on the canvas, with cancel/confirm behavior as specified above. Dragging after an item has been placed should **move that object**, not accidentally repeat-stamp new copies. Repeat-stamping is a separate brush mode.
4. Edit position/size/rotation/mirror/recolor/opacity/blend and supported mask/glow/animation in the existing contextual layer inspector. Provide a real pixel editing action, not only a flat-image transform panel.
5. For sparse compositions, use one normal editable project-owned layer per instance. Multiple independent effects and layer ordering should work just like existing Image/Gradient/Paint content.
6. To avoid exceeding **64 layers/channel**, a future collection model groups many individually selectable child instances; avoid prematurely flattening or rejecting the user's intended starfield without offering an alternate workflow.
7. Keep **Stamp into Current Layer** as an explicit secondary mode for repeated brush dabs and quick pixel painting, with a warning that independently moving the object later will require its own layer or selection.
8. Lock/selection/masks/Alpha Lock and per-face scope must be respected. Never leak painted pixels to the opposite Elytra wing, or silently wrap past a seam.
9. Recolor mono assets from current color; preserve multicolor artwork until the player explicitly requests tint/remap. Never discard custom source pixel edits.
10. A single placement or transform gesture is one Undo step; save/reload/import/export and multiplayer output must not depend on local builtin/custom asset-library availability.

Existing custom stamps remain valid in My Assets/My Stamps, and the current Stamp Brush workflow stays available.

## 5. Scatter Brush (deterministic)

Creative examples: **starfield**, falling petals, embers, fireflies, rain droplets, snowflakes, leaves, magical dust.

- Choose one stamp or a set of related stamps; optionally a weight per variant.
- Settings: spacing, size range, rotation jitter, position jitter, density, opacity/flow, random seed, path follow, optional brush pressure substitute, mirrored symmetry.
- Presets: Sparse Stars, Dense Stars, Falling Snow, Fireflies, Floating Embers, Forest Leaves, Light Rain, Sparkle Trail.
- Preview scatter positions live but freeze the random seed during a gesture. Saving/editing/copying produces the same result across runs.
- Never randomize on each render frame. Record actual rasterized artwork/seed according to the selected persistence design.
- One gesture = one history entry; drag Undo restores the exact prior region.
- Keep high-resolution strokes bounded by stamp count, texture area, time and memory; reject or simplify pathological configurations before accepting.
- Scatter should work independently of Animation. Animation may later move/fade a separate particle layer instead of streaming each stamped particle.

## 6. Soft painting and atmosphere integration

**Existing:** layer opacity, selected alpha, masks, blending and gradients. **Proposed:** explicit brush **Opacity** (max stroke coverage) and **Flow** (per dab build-up), optional Hardness/Feathering/Airbrush, and non-destructive fade tools.

- Explain Opacity vs Flow in help and tooltips. For continuous strokes, avoid unexpectedly dark repeated overlapping dabs unless Flow/Build-Up is intentionally active.
- Airbrush accumulates controllably during sustained painting, independent of framerate. A held still mouse should not render infinite dabs.
- Mist brushes use a palette of faint translucent cloud shapes; crisp source pixels remain available at 1×, with noticeably richer gradation at 4×/8×.
- Mask gradients, selection feathering and layer alpha are distinct. Show the composited result rather than displaying misleading soft lighting that cannot actually render on the cape.
- Fog/smoke procedural presets need bounded deterministic noise/seed and cached output; never re-randomize or decode on each preview frame.
- Optional emissive glow uses existing separate glow rendering and degrades gracefully with shader incompatibility.
- Make blending choices understandable with miniature live thumbnails: Normal, Add, Screen, Multiply, Overlay. Keep a clear Reset/None action.

## 7. Theme recipes: instant starting points, not flattened pictures

A **Creative Theme Recipe** is a reproducible set of normal editable layers, settings, swatches, placements and optional animation. Applying one should produce understandable editable content instead of opaque pre-rendered pixels.

### Example: Misty Night

1. Deep indigo to near-black sky Gradient layer.
2. Moon layer from Celestial library, optional halo/glow on a separate layer.
3. Scattered small/medium stars of several shapes and colors, with a named Starfield layer.
4. One or more translucent cloud/fog layers with adjustable density, opacity and color.
5. Optional pine/tree-line silhouette along the lower hem.
6. Optional twinkle animation on Starfield and slowly drifting mist, editable in Simple and Advanced Animation.
7. Palette: midnight blue, violet, cool cyan, moon ivory; all replaceable by the user.

Expected result: a non-artist can choose recipe, reposition moon/trees, paint more fog, modify layer opacity, change the palette and play Twinkle, without drawing stars manually.

### Additional proposed recipes

| Theme | Editable constituents | Optional motion |
| --- | --- | --- |
| **Aurora** | Gradient night sky, broad aurora ribbons, sparse starfield, mountain/pine silhouettes | Gradient movement, subtle glow |
| **Enchanted Forest** | Mossy gradient, fern/vine/tree silhouettes, mushrooms, floating runes/fireflies | Fireflies drift/sparkle |
| **Emberfall** | Warm dark base, flame border, ash/ember scatter, glow mask | Slow ember drift/pulse |
| **Cherry Dusk** | Peach-violet gradient, branch, sakura petals, cloudbank | Petal drift |
| **Winter Whisper** | Frost border, clouds, snowflakes, fir silhouettes | Gentle falling snow |
| **Deep Ocean** | Teal gradient, bubbles, kelp, shells, moonlight shafts | Vertical bubbles |
| **Celestial Heraldry** | Bold crest, geometric stars, ornate moon, textile trim | Subtle sigil pulse |
| **Neon Circuit** | Dark grid, circuits, diagonal bars, neon lines | Scroll and hue shifts |
| **Mushroom Hollow** | Forest backdrop, luminous mushrooms, spores, mist | Glow and drifting spores |
| **Void Galaxy** | Dark starfield, nebula wisps, orbits, crystals | Twinkle and gradient |
| **Golden Sunrise** | Warm sky, cloud layers, sun rays, mountains | Slow light sweep |
| **Stormcaller** | Dark cloudbank, rain/bolt emblems, mist, edge trims | Flicker and drift |

Recipe controls:
- Before Apply: thumbnail canvas and animated 3D candidate, choose Cape/Elytra, choose colors, intensity, optional animation.
- Applying as **New Project** or **Add as Layers** must be explicit. Replace existing artwork only through confirmation with reversible backup/history.
- Generate layer names by function and retain editing controls. Respect layer, key, project-byte and memory budgets; one action/one Undo.
- For Elytra, choose **Linked wings** or **Separate wings** and obey selected semantic faces. A recipe must never assume Cape UVs map directly onto the entire wing atlas.
- Layering and animation must survive a portable .loom and multiplayer transfer without requiring recipe pack binaries on the destination.
- A recipe should be usable without installing a third-party shader, texture pack or new server-side asset library.

## 8. Asset architecture and ownership

**Proposal, not an adopted ADR:**

- Built-ins: versioned read-only asset resources distributed inside the mod JAR, with a generated manifest/index and cacheable thumbnail atlas.
- Custom stamps: current bounded local library, migration-compatible; keep user originals and favorites. Avoid silently counting built-ins as custom entries.
- Artwork placement: convert built-in stamp pixels or deterministic parameter output into normal project-owned authored content. Shared projects must not reference missing client-only pack IDs.
- Reference images remain private editor-side assets, excluded from export/equip/network as in the current architecture.
- Built-ins may support flat pixels with color/tint mask metadata; optional richer/detailed variants should be separate art, not noisy upscaling.
- Keep asset source/provenance in a repository manifest with creator, generation/edit provenance, license, visual review and intended pack. No unlicensed third-party images, franchise mascots, trademarks or scraped pixel art.
- Deduplicate identical thumbnails/pixel payloads, lazy-load grids, bound caches and release textures on resource reload.
- External community packs are **future optional scope**, and need strict parsing, path safety, sandboxing and provenance rules before import.

## 9. Acceptance criteria and testing

### User journeys

**A. Five-minute Misty Night:** new Cape → Theme or Library → add night sky → choose crescent moon → scatter mixed stars → add cloud/mist → tune opacity → preview/equip. No manual star drawing required; user can edit all parts afterward.

**B. Tree-line Elytra:** select wing and inside/outside face → place different tree silhouettes on each wing or linked mirror → check both 2D and 3D views → Undo/reload/network. Artwork must not bleed to unrelated faces/wings.

**C. Custom stamp continuity:** exact selection → create stamp → name/favorite → restart Minecraft → select from My Stamps → recolor/mirror/place → Undo/Redo.

**D. Animation continuity:** choose twinkle creative preset → switch Simple/Advanced → edit existing keys → return to main editor → save/portable/share. No effect lost or duplicated.

### Automated and visual coverage

- Assert catalog/manifest unique IDs and search aliases, no missing thumbnails, validated bounds and alpha.
- Test at least one sample from every pack, all palette modes, different pivot types, and stamping near atlas edges.
- Test stamp placement under zoom/pan, selection mask, Alpha Lock, layer lock, clipping, undo/redo, high resolution, Elytra independent faces.
- Deterministic scatter with identical seed produces identical project bytes/pixels; unrelated frames don't vary.
- Stamping 1000s of assets should not occur on one frame. Benchmark catalog open/search/scroll, cold/warm thumbnail render and resize.
- Test 1×/2×/4×/6×/8× output; check tiny sprite clarity, high-res source detail and alpha preview.
- Four required GUI profiles plus narrow-window interaction; artwork must stay the main focus and no stamp grid may obscure critical save/navigation actions.
- Compare real screenshots against approved references 02 (Cape), 04 (Elytra/Animation), 01 (Home). Review actual 3D candidate/equipped parity.
- Verify privacy: reference assets do not enter exports; baked stamps do; no external network requirement.
- Human acceptance: new player understands the Stamp tool and completes Misty Night unaided within five minutes.

## 10. Implementation slices

1. **AS-01A:** manifest, original curated starter pack, thumbnail atlas, category/search/favorites/recent; no new project format.
2. **AS-02:** select/hover ghost/direct stamp placement, brush controls, stamp-on-new-layer, parity both editors.
3. **AS-03:** scatter engine, deterministic seeded stroke, presets and UI.
4. **PA-01:** paint opacity/flow, hardness, gradient-mist brushes, mask fade semantics.
5. **AS-04:** theme recipe metadata and candidate preview, first Misty Night and two contrasting themes, saved projects.
6. **Expand:** curate remaining packs, variations, QA and new theme recipes only after visual-quality review.

**Documentation/verification contract:** each slice updates CURRENT_STATE, PASS_LOG, NEXT_WORK, affected architecture/decisions and the test matrix. This specification remains PROPOSED until accepted implementation evidence exists.
