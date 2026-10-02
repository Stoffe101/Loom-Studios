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

### Future PNG/UI import checks

- PNG with alpha
- PNG without alpha
- large source image
- odd aspect ratio
- very small image
- fit/stretch/crop/center
- mirror/rotate
- dithering
- color reduction
- brightness/contrast/saturation
- background/transparency handling
- palette-limited/posterize/monochrome/outline/pixel-art modes

## Project format tests

- save/load round-trip
- deterministic output where required
- migration from older schema fixtures
- missing/corrupt asset
- unknown optional field
- unsupported future major version
- portable code round-trip
- invalid portable code rejection

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

## SPIKE-03 verification additions

- dedicated Elytra texture takes precedence over cape fallback
- cape animation does not alter dedicated Elytra
- gliding pose remains vanilla
- edge transparency renders correctly
- inspect visual thickness from side/top/rear angles


## Elytra thickness proof checks

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

- schema-v1 project encode/decode round trip
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
- schema-v1 BlendMode ordinal mapping is pinned by automated test
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
