# Loom Studios — Test Matrix

## Toolchain

SPIKE-00 must record exact versions for:
- Minecraft
- Java
- Fabric Loader
- Fabric API
- Fabric Loom
- mappings
- Gradle

Required:
- clean build
- dev client start
- dedicated server start
- CI build on exact commit SHA

## Rendering compatibility matrix

Plain Fabric is the required runtime. The other configurations are optional compatibility targets we actively support and should smoke-test for relevant rendering changes.

| Configuration | Runtime requirement | Compatibility target |
| --- | --- | --- |
| Fabric only | Yes | Primary |
| Fabric + Sodium | No | Supported |
| Fabric + Sodium + Sodium Extra | No | Supported |
| Fabric + Sodium + Iris, shaders OFF | No | Supported |
| Fabric + Sodium + Iris, shaders ON | No | Supported |
| Fabric + 3D Skin Layers | No | Smoke test when player feature rendering changes |

Shader-sensitive effects should be checked with at least one representative shader pack before release.

## Cape tests

- standing
- walking/running
- crouching
- jumping/falling
- armor equipped
- official cape fallback when Loom cape disabled
- Elytra equipped
- remote player rendering
- texture swap while visible

## Elytra tests

Automated semantic authoring:
- linked Left-wing pixel mirrors into the opposite Right wing
- Separate Wings edit does not touch the opposite wing
- linked mirroring uses the scaled semantic width at 4x resolution
- locked Elytra Paint layer rejects semantic wing painting
- linked brush edits both wings

Local/runtime:
- semantic wing orientation matches the rendered left/right Elytra
- linked mirror visual orientation is correct
- separate-wing mode is independent
- 1x / 2x / 4x unfolded wing layout remains aligned
- Add / Copy / Delete layer controls
- per-row visibility / lock controls
- Undo / Redo
- project-authored thickness from 25% through 200%
- Save + Equip preserves authored thickness
- integrated Elytra preview rotation / zoom

Existing rendering:
- equipped/unequipped
- standing with Elytra
- gliding
- rockets/flight transitions where applicable
- remote players
- texture swap
- cape precedence

## Dynamic texture tests

- repeated updates
- no restart/reconnect required
- dirty-only upload
- old texture disposal
- cache hit/miss behavior
- disconnect/world change cleanup

## Reference UI architecture regression checks

The 2026-10-02 pre-refactor build failed this gate even though feature behavior broadly worked.

### Critical compact profile
Treat approximately 640x360 effective GUI space (1920x1080 / GUI scale 3) as release-critical.

Verify:
- no text crosses panel boundaries;
- no primary action is outside the viewport;
- no primary editor requires scrolling the entire control surface;
- icon rails remain clickable;
- active tool/tab state is obvious;
- canvas/preview retains more visual importance than settings chrome.

### Home
- five working primary actions fit without scrolling;
- Share / Export wording is explicit;
- no disabled Settings placeholder;
- compact action cards hide subtitles before clipping;
- compact recent projects/templates intentionally show fewer cards.

### Cape
- tool rail fits vertically;
- face/resolution/zoom toolbar does not collide;
- contextual brush controls appear only for brush/shape tools;
- selection movement controls appear only for Select;
- Layers / Color / Properties never render simultaneously;
- Gradient controls only appear for Gradient Properties;
- inspector content fits the compact panel without a full-screen scroll;
- Save / Equip / Share-Export remain reachable.

### Elytra / Animation
- canvas, timeline and 3D preview are simultaneously understandable;
- Layers / Color / Animation are mutually exclusive inspectors;
- + Track targets selected layer;
- timeline click/scrub is not obscured by inspector controls;
- compact timeline hides secondary footer before track rows overlap;
- effect/keyframe editing is in the Animation inspector, not duplicated across the timeline;
- workflow hint remains readable.

### Smart Import
- Placement and Processing are mutually exclusive;
- transform controls fit without vertical scroll;
- processing controls fit without vertical scroll;
- 3D Preview / Apply / Cancel stay visible on both tabs;
- preview surfaces remain usable.

### Share / Export
- Export is the default workspace;
- .loom, portable code, Cape PNG and Elytra PNG are visible without switching contexts;
- Import-only actions are hidden while Export is selected;
- Import shows paste/file/preview/library actions without Export clutter;
- preview mode controls remain usable.
## Home dashboard local checks

- responsive three-column shell at all mandatory GUI profiles
- action-card icon/text alignment
- Create New Cape
- Load Design
- Import Image -> Smart Import
- Edit Elytra
- real thumbnail cards
- selected-card state
- selected project drives integrated 3D preview
- preview drag / wheel zoom / reset
- Blank template
- Gradient template
- empty project library
- unreadable project warning
- Share / Export is active and discoverable; Settings is absent until implemented; future template packs remain visibly secondary
- texture resources release when leaving/rebuilding Home

## Editor preview tests

- rotate
- zoom
- cape view
- Elytra open/closed view
- armor toggle
- temporary unsaved state isolation
- GUI scale variants
- resize/windowed/fullscreen

## Networking tests

- two clients + dedicated server
- first join cache miss
- second join cache hit
- reconnect
- invalid hash
- unsupported schema
- oversized blob
- malformed compressed data
- spam/rate-limit behavior
- player leaving during transfer

## Import tests

### Automated transform/processing foundation

- PixelImage rejects invalid dimensions and pixel counts
- PixelImage owns defensive ARGB copies
- mirror horizontal
- mirror vertical
- rotate 90 degrees clockwise
- rotate 90 degrees counter-clockwise
- crop rejects out-of-bounds rectangles
- nearest-neighbor resize is deterministic
- Fit preserves aspect and transparent letterbox
- Stretch fills target
- Crop uses centered source crop matching target aspect
- Center preserves source scale and clips oversized artwork
- custom placement rejects source windows outside the image
- crop/source bounds cannot wrap through integer overflow
- extreme off-screen destination placement clips safely
- zero brightness/contrast/saturation is pixel-stable
- Brightness preserves alpha
- Saturation -1 produces luminance grayscale
- adjustment ranges reject values outside -1..1 / non-finite values

- Reduce Colors returns already-bounded artwork unchanged
- Reduce Colors output uses no more than requested visible RGB colors
- Reduce Colors preserves source alpha
- generated palette and reduced output are deterministic
- Palette Limited maps to nearest supplied RGB
- Palette Limited preserves source alpha
- Posterize honors per-channel level count
- Monochrome uses luminance and preserves alpha
- invalid reduction/palette/posterize bounds are rejected
- Floyd-Steinberg output is deterministic
- dithered output remains limited to supplied/generated palette
- Floyd-Steinberg preserves source alpha
- fully transparent pixels remain untouched
- error diffusion does not cross fully transparent pixels
- dithered Reduce Colors leaves already-bounded art unchanged
- dithered Reduce Colors honors requested maximum color count

### Smart Import automated + local checks

Automated/current CI:
- cape Outside -> Elytra conversion aspect-fits into the semantic wing target
- cape -> Elytra conversion mirrors the right wing deterministically
- cape -> Elytra conversion preserves alpha and works against 4x target canvases
- invalid cape conversion source dimensions are rejected
- Elytra semantic normalized rects remain stable across backing resolutions
- Elytra Image layers can be added and updated non-destructively
- Elytra layer rename/opacity/blend round-trip through the project codec
- Gradient translation uses layer-relative 5% movement steps
- Gradient scale preserves aspect and reports clip-relative scale percent
- Gradient H/V mirror toggles remain independent
- Gradient reset restores semantic clip placement and clears rotation/mirrors
- invalid Gradient scale factors are rejected
- Outline Only clears non-edge interior pixels
- Direct mode with zero adjustments is pixel-stable
- Pixel Art honors configured color bounds
- Palette Limited preserves source alpha
- processing mode stable IDs round-trip
- schema-v1 project bytes migrate explicitly to schema v2
- schema-v2 round-trip preserves Image/Gradient payloads and lock state
- typed Image layers rasterize deterministically
- Gradient layers rasterize deterministically
- typed-layer normalized placement survives 1x -> 4x canvas resizing
- locked Paint layers reject pixel mutation
- Image/Gradient layers reject Paint-tool pixel mutation

Local/runtime verification:
- typed layer icons distinguish Paint / Image / Gradient
- direct visibility control changes only visibility
- direct lock control changes only lock state
- locked typed layer authoring controls disable correctly
- Gradient Linear / Radial
- Gradient stop add/remove/color/position
- Gradient angle / move / scale / Mirror H / Mirror V / reset
- Gradient repeat / dither
- PNG with alpha
- PNG without alpha
- large source image
- odd aspect ratio
- very small image
- file-picker cancel path
- invalid/non-PNG content rejection
- source >256px persistence downscale
- import that would exceed 1 MiB project size is rejected
- Fit / Stretch / Crop / Center
- Keep Aspect
- move / free scale / arbitrary rotate
- Mirror H / Mirror V
- Dither
- Reduce Colors
- Brightness / Contrast / Saturation
- Direct / Pixel Art / Outline / Monochrome / Palette Limited / Posterize
- selected Loom Swatches palette
- Original / Processed / Cape Texture preview
- isolated 3D candidate preview
- Apply as new Image layer
- reopen/edit Image layer
- Undo/Redo around Apply
- candidate preview does not change equipped/network state
- Elytra linked import produces left/right semantic Image layers
- opposite imported wing is horizontally mirrored
- editing an Elytra Image layer reopens Smart Import
- Cape -> Wings creates an editable Paint layer
- Elytra layer reorder / opacity / rename / blend
- Elytra Swatches select/update the active color

## Animation automated + local checks

Automated/current CI:
- schema v1 migrates through v2 into v3;
- schema v2 migrates into v3 with default empty timeline;
- schema v3 animation track/keyframe round-trip;
- keyframe interpolation;
- timeline duration clamping;
- malformed keyframe ordering rejection;
- missing target-layer references rejected;
- deleting a target layer prunes its animation tracks;
- authored Cape/Elytra runtime compiler builds in CI;
- fixed-tick preview cache path builds in CI.

Local/runtime verification:
- add/select/enable/delete Elytra animation tracks;
- scrub/play/pause;
- loop/once;
- duration and playback speed;
- per-track speed;
- effect cycling;
- add/remove keyframes;
- keyframe value editing;
- keyframe markers align to timeline ticks;
- Pulse / Scroll / Hue Shift / Moving Gradient / Sparkle / Emissive Glow;
- timeline Undo/Redo;
- scrubbed 3D preview matches exact authored tick;
- preview remains isolated from equipped/network state;
- timeline stays usable at all mandatory GUI profiles.
## Loom Codes / sharing tests

Automated/current CI:
- schema-v3 project portable-code round trip;
- portable code preserves animation/layers/runtime;
- stable short design fingerprint;
- imported project receives a fresh project UUID;
- fork import preserves Cape/Elytra/runtime/animation data;
- wrong prefix rejected;
- malformed Base64 rejected;
- whitespace-tolerant portable-code detection;
- sharing/export/import client sources compile against Minecraft 1.21.11 mappings.

Local/runtime:
- Copy Design ID clipboard;
- Copy Portable Code clipboard;
- Paste Portable Code;
- `.loom` native file picker import;
- imported-project Cape/Elytra 3D preview;
- Import to Library;
- Import + Open;
- `.loom` export;
- portable `.txt` export;
- Cape PNG export;
- Elytra PNG export;
- unique repeated export filenames;
- all exports remain within Loom Studios exports folder;
- Reference 05 responsive layout at all mandatory GUI profiles.
## Project format tests

- current schema-v3 save/load round-trip (preserving v2 typed payloads)
- deterministic output where required
- schema-v1 -> schema-v2 -> schema-v3 migration
- stable schema-v2 blend IDs
- stable layer-kind IDs
- Paint payload round-trip
- Image payload round-trip
- Gradient payload round-trip
- persistent lock round-trip
- malformed typed payload rejection
- oversized embedded Image source rejection
- oversized project rejection
- unsupported future major version
- implemented portable code round-trip
- implemented invalid portable code rejection

## Animation/effect tests

- loop
- speed
- pause/editor scrub
- moving gradient
- hue shift
- pulse
- sparkle
- emissive/additive layer
- large number of remote animated players
- shader compatibility

## Documentation gate

A test pass is not complete until its result is recorded in PASS_LOG.md and relevant current-state/test documentation.


## SPIKE-02 verified runtime checks

- live cape pixel changes without restart: PASS
- stable vanilla cape motion during updates: PASS
- full optional dev stack initial runtime: PASS
- Elytra fallback to cape texture observed: PASS / expected vanilla behavior

## SPIKE-03 verification additions (historical)

- dedicated Elytra texture takes precedence over cape fallback
- cape animation does not alter dedicated Elytra
- gliding pose remains vanilla
- edge transparency renders correctly
- inspect visual thickness from side/top/rear angles


## Elytra thickness proof checks (historical V-key flow; current Props control)

- 100% visually matches normal vanilla wing volume
- 75/50/25% progressively reduce depth
- 150% increases depth
- wing length/width remain unchanged
- gliding rotations remain vanilla
- returning to 100% fully restores vanilla depth
- non-Loom Elytras remain 100%
- no stale zScale leaks between rendered players


## SPIKE-04 preview checks

- P opens preview without pausing the integrated world
- real player skin/model renders
- cape preview uses current Loom cape texture
- Elytra preview uses current Loom Elytra texture
- C changes preview mode without mutating actual chest equipment
- drag rotates
- wheel zooms
- R resets camera
- dynamic cape updates are visible in GUI preview
- Elytra thickness customization is reflected in preview
- closing preview leaves gameplay state unchanged


## SPIKE-04 verified runtime checks

- preview screen opens: PASS
- local player model/skin visible: PASS
- Cape mode: PASS
- Elytra mode: PASS
- preview-only mode switching: PASS
- drag rotation: PASS
- zoom: PASS
- real equipment remains unchanged: PASS
- optional stack remains operational: PASS

Deferred UX check:
- neutral/locked head orientation during orbit
- independent Player Facing and 3D Orbit/Pivot controls


## SPIKE-05 multiplayer runtime checks

- server without Loom Studios: client remains local-only and does not error
- first project on server: cache miss -> one upload -> equip broadcast
- second client: equipped hash -> cache miss -> one project download
- remote player cape renders
- distinct Client A / Client B proof colors visible
- malformed hash rejected
- oversized proof blob rejected by codec/validation
- mismatched SHA-256 rejected
- disconnect removes equipped state
- reconnect reuses architecture cleanly
- no animation-frame packets

## SPIKE-06 animation/emissive checks

- base cape animation remains deterministic
- G toggles only the emissive pass
- base cape remains visible when emissive is OFF
- emissive geometry follows cape movement
- remote player emissive layer renders
- preview does not crash with feature layer registered
- Sodium compatibility smoke test
- Iris shaders OFF smoke test
- Iris shaders ON smoke test
- 3D Skin Layers coexistence smoke test


## Phase-1 project-core checks

- schema-v2 project encode/decode round trip
- deterministic encoding produces stable SHA-256
- invalid magic rejected
- unsupported schema rejected
- oversized project rejected
- oversized canvas/layer counts rejected
- pixel count mismatch rejected
- unknown blend-mode value rejected
- trailing bytes rejected
- server re-hashes project blob before cache/equip
- client re-hashes downloaded blob before cache
- layer compiler respects visibility
- layer compiler respects opacity
- emissive-only compilation excludes non-emissive layers
- local library rejects paths outside its project root
- project save uses temporary file + replace/atomic move where available
- undo clears redo after a new edit
- history limit is bounded

## Elytra calibrated-baseline checks

- Loom 100% visually resembles normal vanilla Elytra thickness
- V 75/50/25% become progressively thinner
- V 150% is visibly thicker/stylized
- non-Loom Elytra remains raw vanilla model depth
- no gameplay/hitbox/flight behavior changes


### Automated Phase-1 checks now in CI

`LoomProjectCodecTest` covers:
- schema-v1 encode/decode equality;
- byte-for-byte deterministic re-encoding;
- stable SHA-256 after round trip;
- layer pixel defensive copies;
- trailing-byte rejection;
- 64x32 runtime canvas invariant;
- undo/redo and redo invalidation after branching edits.


## Project session/persistence automated checks

- created/modified metadata round-trip
- unsupported schema rejected through migration gate
- unsaved session starts dirty
- save establishes clean persisted hash
- real edit becomes dirty
- edit updates modified timestamp
- undo back to saved content becomes clean
- redo becomes dirty again
- reload starts clean
- saved project round-trips exactly
- file-store rejects paths outside configured library root
- library listing ignores non-.loom files


## Workspace/runtime/library checks

- one workspace project is used for local render + network hash
- workspace project hash change triggers multiplayer republish
- runtime textures are cached by project content hash
- non-Loom texture lookups do not acquire Loom thickness/effects
- cache shutdown releases registered textures
- Recent Projects sort newest modified first
- corrupt .loom file is skipped without aborting entire scan
- thumbnail filename changes when project content hash changes
- save refreshes project-library index


## Phase-2 first editor runtime checks

- L opens Loom Studios home screen
- Create New Cape creates dirty blank project
- canvas shows 64x32 checker transparency
- Pencil changes requested pixel
- Eraser restores transparency
- drag painting works
- Undo/Redo operate on edits
- 3D Preview shows unsaved session output
- world cape remains equipped old project before Save + Equip
- Save creates .loom file and thumbnail
- saved project appears in Recent Projects
- Save + Equip changes world cape
- multiplayer sync republishes newly equipped saved hash


## Cape face-first editor checks

- blank editor project hue cycle is false
- blank editor project emissive is false
- Outside local (0,0) maps to atlas (1,1)
- Outside local (9,15) maps inside the expected 10x16 face
- face editor defaults to Outside / Back
- painted Outside pixels appear on the player's rear-facing cape surface
- Inside edits do not overwrite Outside
- edge/top/bottom local coordinate bounds are enforced
- hover highlight matches edited pixel
- pixel/UV coordinate readout matches CapeUvRegion mapping


## High-resolution canvas automated/runtime checks

- 64x32 project remains valid
- 128x64 project remains valid
- 256x128 project remains valid
- unsupported dimensions rejected
- 1x -> 4x preserves existing painted region via nearest-neighbor scale
- Outside region dimensions scale 10x16 -> 20x32 -> 40x64
- brush size >1 edits multiple region pixels
- RuntimeCosmeticCache image sizes follow project canvas sizes
- 4x project encode remains within configured 1 MiB bound for normal low-layer projects
- C2S/S2C project blobs use large payload registration
- high-resolution cape renders in preview/world
- color picker HSV, RGB, hex and palette remain synchronized


## Palette checks

- palette share-code round trip
- palette JSON validates format/version/name/color count
- new palette persists under game folder
- palette import receives new UUID
- exported file is readable after restart
- palette swatch applies selected editor color
- right-click color removes it unless it is the final remaining color
- palette window can move while unpinned
- pinned palette window cannot move
- tool-rail scrollbar and floating palette window coexist at 1920x1080 GUI scale 3


## Mandatory desktop UI profile matrix

Every meaningful editor-layout change must be visually checked at:

| Physical | GUI scale | Status for current slice |
| --- | ---: | --- |
| 1920×1080 | 2 | LOCAL TEST REQUIRED |
| 1920×1080 | 3 | LOCAL TEST REQUIRED |
| 3440×1440 | 2 | LOCAL TEST REQUIRED |
| 3440×1440 | 3 | LOCAL TEST REQUIRED |

Pass criteria are defined in `UI_COMPATIBILITY.md`.


## Zoom / pan / brush cursor

- 100% is fit-to-view
- Zoom +/- reaches configured limits only
- mouse wheel over canvas changes zoom
- mouse wheel over right tool rail still scrolls the rail
- middle drag pans only the zoomed canvas
- panning clamps so the canvas cannot be lost
- zoomed canvas is scissored to its editor viewport
- painted pixel mapping remains correct after zoom/pan
- brush radius cursor scales with Brush size
- radius cursor remains correctly centered at 1x/2x/4x project resolution
- 4x at high zoom remains interactive

## Palette overlay interaction regression

- palette overlay receives click before underlying canvas/tool widgets
- palette name field accepts keyboard input
- New / Save Name / Add Current Color buttons operate
- palette list row selection operates
- palette swatch left-click applies color
- palette swatch right-click removes color
- title-bar drag operates only while unpinned
- Pin prevents moving
- Import/Export buttons operate while overlay overlaps canvas

## Fill / Eyedropper

- Fill affects only four-connected matching pixels
- Fill cannot cross semantic face bounds
- Fill is one undo entry
- Eyedropper reads composited visible color
- transparent Eyedropper target leaves selected paint color unchanged


## Swatches-style palette dock

- multiple named palette groups visible simultaneously
- each group shows its own ordered swatch grid
- group-header click selects palette for management actions
- swatch click selects both palette and active editor color
- up to 64 swatches persist/round-trip
- multiple groups scroll without overlapping window controls
- Delete affects only selected palette
- import/export/share-code remains palette-specific
- moving/pinning still works after grouped redesign

## Temporary brush-size preview

- normal Pencil/Eraser hover shows single-pixel target only
- Brush +/- triggers temporary radius circle
- radius circle disappears automatically
- changing size from tool rail still shows preview at canvas center
- preview does not alter project data

## Line / Rectangle

- line preview follows drag endpoint
- line commit paints both endpoints
- line commit is one undo entry
- rectangle preview follows drag bounds
- outline rectangle leaves interior untouched
- filled rectangle colors interior
- outline rectangle respects Brush size
- shape tools stay inside active semantic face


## Compact Swatches screenshot regression

- 1920x1080 GUI 3 Swatches window no longer dominates the editor viewport
- compact Swatches defaults to management collapsed
- Edit/Done toggles management controls
- Saved palettes label never overlaps management buttons
- swatch viewport grows when management collapses
- title, Edit and Pin remain usable at compact width

## Alpha / numeric color

- #RRGGBB field updates selected RGB while preserving alpha
- R/G/B numeric fields accept 0..255
- A numeric field accepts 0..255
- alpha slider updates the same selected ARGB color
- semi-transparent paint survives save/load
- custom palette export/import preserves alpha
- old #RRGGBB palette files still decode as alpha 255

## Symmetry

- Horizontal mirrors x within active semantic face
- Vertical mirrors y within active semantic face
- Both produces all four mirrored transforms
- center-axis duplicate transforms remain harmless/no-op
- Fill symmetry cannot cross semantic face bounds
- Line/Rectangle mirror both drag endpoints
- Eyedropper ignores symmetry

## Layers foundation

- add layer creates transparent top layer
- duplicate copies pixels/properties with a fresh UUID
- delete cannot remove final layer
- selected layer receives paint edits
- visibility immediately affects compiled editor/preview output
- move up/down changes compositing order
- opacity changes compiled result
- layer actions participate in undo/redo
- Layers list displays top-most first


## Layer effects / blend modes

- layer rename persists through project encode/decode
- emissive flag persists through project encode/decode
- blend mode persists through project encode/decode
- schema-v1 BlendMode ordinal mapping remains pinned for migration compatibility; schema v2 uses stable blend IDs
- Normal behaves as standard source-over
- Add / Glow does not hide source over transparent destination
- Screen does not hide source over transparent destination
- Multiply does not hide source over transparent destination
- Overlay does not hide source over transparent destination
- opacity remains applied before blend calculation
- semantic face preview and full runtime compiler use the same blend mode
- emissive-only compilation still excludes non-emissive layers
- shader-on/off runtime smoke test is required before calling emissive UI DONE


## Selection / transform core

Automated/common-core:
- PixelSelection normalizes drag endpoints through `between(...)`
- invalid negative/inverted direct selections are rejected
- selection outside active semantic face is rejected
- horizontal flip reverses selected x order only
- vertical flip reverses selected y order only
- combined flip applies both axes
- move captures source before clearing
- overlapping moves are deterministic
- move clears original selected pixels
- destination pixels remain inside semantic face
- pixels moved outside semantic face are clipped rather than touching other UV faces
- transform affects selected layer only
- transform participates in normal ProjectSession history once exposed through editor UI

Editor/runtime checks:
- Select drag creates the expected inclusive region
- persistent outline matches selected pixels at 1x / 2x / 4x
- Move Left / Right / Up / Down moves selected-layer pixels only
- arrow-key nudge matches button nudge
- user-facing nudge clamps the whole selection at semantic-face edges
- Flip H / Flip V update one undoable project edit
- changing face clears the temporary selection
- changing project resolution clears the temporary selection
- Undo/Redo clear temporary selection state after project history jumps
- selection remains aligned after zoom and pan
- compact tool rail remains usable after selection controls are added


## Equipped/editor state regression checks

- dirty editor paint does not change the local world cosmetic before Save + Equip
- dirty editor paint does not announce a new multiplayer hash
- Save without Equip keeps the previous equipped world/network snapshot
- Save + Equip changes the equipped world/network snapshot
- scoped 3D Preview still renders dirty editor state

## Emissive authoring regression checks

Automated:
- enabling the first emissive cape layer enables schema-v1 runtime emissive master
- disabling the final emissive cape layer disables the runtime emissive master

Runtime:
- blank editor project has no glow
- toggling one layer Emissive On produces only that authored layer in the glow mask
- toggling the final emissive layer Off removes glow
- no synthetic SPIKE shimmer stripe appears
- shader OFF base cape remains visible
- shader ON base cape remains visible even if optional glow behavior differs


## Schema-v2 typed layer checks

Automated:
- v1 Paint layers migrate to v2 as unlocked Paint layers
- Image layer source/crop/transform/processing survives encode/decode
- Gradient type/stops/transform/repeat/dither survives encode/decode
- persistent layer lock survives encode/decode
- duplicate typed layer receives a fresh UUID while preserving payload
- typed-layer resize keeps normalized authoring data unchanged
- shared compiler applies visibility / opacity / blend / emissive to typed layers

Local/runtime:
- Paint / Image / Gradient rows are distinguishable
- Lock prevents accidental Paint-tool edits
- Image and Gradient layers composite correctly in editor preview
- Image and Gradient layers composite correctly in 3D preview
- Image and Gradient layers composite correctly when equipped
- remote client receives/compiles schema-v2 project through normal hash/blob sync

## Smart Import mandatory UI profile matrix

Verify the Smart Import screen at:
- 1920x1080 GUI scale 2
- 1920x1080 GUI scale 3
- 3440x1440 GUI scale 2
- 3440x1440 GUI scale 3

Pass criteria:
- Original / Processed / Cape Texture previews remain readable
- 3D candidate preview remains reachable
- transform/processing controls do not overlap
- file/apply/cancel controls remain reachable
- labels do not clip
- Placement / Processing tabs fit without primary vertical scrolling

## 2026-10-02 workspace repair verification

Implementation base: `f49ddb2e9727e16d3e6438772534f278d0520a38`; verified source `a2a9628afced367d4a178f944c5cce239d110fbf`, tested merge `ed4b04f900dc200eb43fa6074fecd5d6a832e084`, Actions #178.

- Java 21.0.9, Minecraft 1.21.11, Fabric Loader 0.18.4, Fabric API 0.141.1+1.21.11, Loom 1.17.21, Gradle 9.6.1.
- Full `build`: PASS in clean Actions #178 after final navigation/alpha/capture polish, Java 21.0.12+1, unmodified Loom.
- `CanvasViewportTransformTest`: PASS for all semantic cape faces, 1x/2x/4x, fit through 800% zoom, positive/negative pan, pixel centers and exclusive selection edges.
- `LoomWorkspaceLayoutTest`: PASS for all four mandatory profiles, ±1 logical rounding, Cape and Elytra with 0/1/5/16 tracks; positive bounded regions and canvas minimums.
- Actual Minecraft editor screenshots: PASS for the four required profiles and compact inspector/collection scenarios. All 22 PNGs regenerated in clean Actions #178; live/released single-pixel outline region is identical at GUI 3 / 200% and the below-minimum guidance is readable.
- Container-only Loom Unix socket probe workaround: cached plugin platform probe returns false because this container forbids Unix sockets. No project source or dependency version is changed by this workaround. Clean Actions #178 independently passed without this workaround.
- Optional mods, shaders, multiplayer and full interactive workflow: NOT RUN for this repair.

The opt-in development capture checks every visible widget against the window/footer bounds and records Cape/Elytra at 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3. Inspect selection outline, context row, property pages, cached wing composition, preview and timeline; geometry tests alone do not establish reference fidelity.


## 2026-10-02 workshop styling gate — PASS

Verified source `58491bc8855f854f2f1902e32f1720cb3e5e3274`, tested merge `3de85d41980e1d28d78e429749147031a41766d1`: [Actions #182](https://github.com/Stoffe101/Loom-Studios/actions/runs/37073091623) passed build/tests and all 42 fresh actual Minecraft captures on 2026-10-02 at 22:36 UTC.

| Physical display | GUI scale | Logical area | Home / Cape / Elytra / Share Export+Import / Smart Placement+Processing |
| --- | --- | --- | --- |
| 1920×1080 | 2 | 960×540 | PASS |
| 1920×1080 | 3 | 640×360 | PASS |
| 3440×1440 | 2 | 1720×720 | PASS |
| 3440×1440 | 3 | 1147×480 | PASS |

All 42 fresh PNGs decode fully and were inspected against the five approved references. Every visible widget is bounded above the footer and pairwise nonoverlapping. Compact property/color/animation pages and long layer/track collections pass. The live/released one-pixel canvas-body crop `(114,330)–(1284,910)` is identical at GUI 3 / 200% zoom; below-minimum guidance is readable. Source labels, candidate hints, brand artwork, preview scenery, export previews and fixed actions fit their assigned panels.

Runtime workflow checks PASS: editable/portable export hash round-trips, Cape/Elytra PNG dimensions, both Smart Import pages add an Image layer whose compiled pixels equal the candidate. Capture completion and workflow markers occur once. Focus tooltips are transient expected overlays in some editor captures. Optional mods, shader packs, multiplayer, OS picker/clipboard and exhaustive manual feature interactions were not exercised here.

Environment: MC 1.21.11 / Loader 0.18.4 / API 0.141.1+1.21.11 / Temurin 21.0.12+1 / Loom 1.17.21 / Gradle 9.6.1 / clean Ubuntu-Mesa llvmpipe-Xvfb. Build/tests PASS; capture job PASS. Retained evidence: [index](verification/editor-workspace/README.md).

## 2026-10-03 main integration and user test handoff

PR #14 merge: `1e2bc6d2e800e4217d9243e21e61f00a44966a2a`, verified PR head `356d063028eb7eef392fff3d1d8e7adc9d4d4c83`. Both Actions #183 jobs passed at that head, with 42 actual screenshots and export/import workflow markers. This handoff changes documentation only; the previously accepted production source and screenshot pixels are unchanged. Main push CI uses the same Java 21 build and isolated Mesa capture jobs; inspect the exact main commit's checks in GitHub Actions. The full interactive release queue in NEXT_WORK remains manual, including OS picker/clipboard, optional mods/shaders and multiplayer.
