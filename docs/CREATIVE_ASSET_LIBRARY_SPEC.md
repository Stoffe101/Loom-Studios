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
- Large **dedicated Stamp Tool** accessible in both main editors with searchable thumbnail browser.
- Extensive original icon/artwork catalogue with scale, density and variant diversity.
- On-canvas hover ghost and drag placement, stamp-on-new-layer, quick actions, scatter/streak modes.
- Cohesive theme recipes combining assets, gradients, mist, palette, compositing and optional animation.
- More helpful alpha painting/soft atmospheric brushes and procedural environment effects.
- Built-ins stored as separate read-only assets so they do not exhaust the 64-entry **user-created** stamp library.

These are extensions of existing features, not a reason to replace their validated persistence.

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

## 4. Stamp browser: player interaction

### Entry and layout

A visible **Stamp** tool appears in Cape/Elytra tool rail. Selecting it opens a **contextual Stamp Library drawer**; it should not permanently add 100 controls to the editor. On narrow GUI3, the catalog becomes a compact single-column/grid sheet or bounded popup and can be dismissed to preserve the canvas.

Suggested rough structure (interaction concept, not pixel-perfect layout):

- Header: search field (e.g., "crescent", "pine", "star"), Favorites and Recent.
- Pack pills/tabs: All, Celestial, Sky, Nature, Fantasy, Shapes, More.
- Thumbnail grid with meaningful artwork, name on hover/focus, optional palette marker.
- Selected stamp preview, small controls for Size, Rotation, Mirror, Opacity and Color.
- Actions: **Stamp**, **Scatter**, **Place on New Layer**, **Manage My Stamps**.

No unbounded whole-screen scrolling. Grid is the scrolling collection; the canvas, Save and Undo remain visible/reachable.

### Search and discovery

- Search on label, aliases, tags, category and family (e.g. "fog"/"mist"/"haze"; "fir"/"pine"/"spruce").
- Favorites, Recents, pack filtering, optional "monochrome" and "seam/border" filters.
- Provide thumbnail variations and size previews without recalculating full raster data on every mouse movement.
- Sort options: Recommended, Name, Recently Used; keyboard arrow/Enter accessibility, tooltips and narrator descriptions.
- Empty state suggests broader terms; do not display a blank catalog with no explanation.
- Packs can be toggled/filtered, but shipped read-only packs should not be overwritten by custom stamp edits.

### Placement and edit semantics

1. Hover the canvas to see a **translucent, cursor-anchored stamp preview** at actual texture resolution.
2. Click once to place; drag for repeated stamping (respect paint tool gesture semantics).
3. Mouse wheel with modifier or a compact control adjusts scale; rotation/mirror/flip and snapping are accessible without reopening screens.
4. **Place on New Layer** creates a named Paint layer and applies the placement in a single Undo step, without publishing/equipping.
5. Stamps respect exact selection, layer lock, current wing/face, masks and Alpha Lock.
6. If a stamp reaches a semantic face edge, offer explicit Clip/Wrap-across-seam behavior where supported; **never silently paint the other Elytra wing**.
7. Use current authored color or retained palette colors as a deliberate choice; never unintentionally overwrite multicolor source art.
8. After placing, user can undo the gesture immediately, resize/move the new layer or choose another stamp.
9. On high-DPI/GUI scale, the preview is aligned with the exact clicked UV/pixel. No one-pixel drift after pan/zoom.

Existing custom stamps remain valid; the new library discovers them under My Stamps alongside built-ins.

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
