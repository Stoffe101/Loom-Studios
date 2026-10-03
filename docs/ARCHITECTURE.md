# Loom Studios — Architecture

2026-10-03 production presentation migration: Fabric ScreenEvents beforeRender/afterRender (verified from fabric-screen-api-v1 sources3.1.7+4ebb5c083e shipped with API0.141.1) batches actual button/action-card paint into one transparent PIP overlay. Existing widget input/narration is retained; immutable state comparisons invalidate only on interaction/geometry/text changes. Popups remove covered earlier commands. NanoVG/Inter/Lucide foundation source82ad78a passes125 tests/eight fast captures; new production migration verification is pending. Forty-three Lucide assets plus three Loom-specific SVGs are bundled with license notices; flip-horizontal/flip-vertical/trash-2 come from Lucide0.468.0, other assets from upstream main. Source: https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-screen-api-v1/3.1.7+4ebb5c083e/fabric-screen-api-v1-3.1.7+4ebb5c083e-sources.jar. Full workshop skin/remaining typography and hardware/shader acceptance remain unfinished.

2026-10-03 implementation spike: Java/LWJGL NanoVG PIP integration derived from NVGRenderer f4e8272, bundled Inter/Lucide notices, isolated F9 comparison, conventional widget input and CPU submission instrumentation. Template/picker follow-up uses a32-entry UI texture cache with dimension/hue/template keys and resource disposal. Initial source71d3d02 Java21/125 tests pass; new follow-up and runtime capture acceptance remain IN PROGRESS. See PREMIUM_PROTOTYPE.md. No Kotlin, owo-ui or production-screen renderer migration adopted yet.

Planned presentation layer: resource-backed theme/components; revision-owned bounded caches for static UI and thumbnails; dynamic canvas/input remain separate. Profile entity preview and 2D submission costs independently. This is a proposal, not an implemented rendering change. See PREMIUM_UI_AUDIT.md.

2026-10-03 library/layer polish (implemented; automated checks pass at source `01d5303`, Actions #217; final visual acceptance PARTIAL): LibraryOrganization writes folder/tag/group keys through atomic EditorPreferences.setAll, bounded to 1 MiB. It remains separate from immutable project artwork. ProjectFileStore backs up a changed existing save through ProjectVersions before replacement; WorkspaceRecovery opts out of automatic history. Snapshots live in projects/history/projectUUID, retain 20 unique content hashes, validate identity and filename checksum before restore, and preserve the current save if validation/backup fails. LayerBatch preflights canvas limits and applies selected operations as one workspace history entry. Group names filter/display local layer membership without changing compositing or network state. ProjectLibraryIndex caches descriptors by file time/size and bounds full artwork reuse to eight projects; thumbnail compilation is reused for unchanged files. New task screens retain the workshop chrome and compact profile ownership; 125 tests and 182 actual-client captures pass; final visual review was blocked by executor HTTP 503.

2026-10-03 safety pass (DONE, source `5c6b68d`, Actions #204; PR #17 merged): WorkspaceNavigation owns explicit Screen-level unsaved choices; ProjectSession.discardChanges restores a saved/initial immutable baseline without writing. Explicit keepDraft propagates failures; draft discard precedes history reset. Library confirmation wraps reversible store moves with one immediate undo target. Pure RecentColors/DesignColors feed cached virtual palette groups; explicit saving writes existing-format palettes. Background preferences and diagnostics stay local, outside artwork/network state.

## Principles

- keep common/server code independent from client rendering classes
- use vanilla rendering paths whenever possible
- treat editable projects and compiled runtime textures as separate layers
- keep editing local and synchronize saved/equipped state
- content-address remote projects by hash
- design for Sodium/Iris compatibility from the first render spike
- keep UI components reusable

## High-level modules

### Common

Responsibilities:
- project model
- validation rules
- serialization schema
- hashes/identifiers
- networking payload definitions
- server-side equipped state
- project metadata
- permissions and limits

### Client

Responsibilities:
- editor screens/widgets
- cape/Elytra rendering integration
- dynamic texture compilation/cache
- animation evaluation
- image import processing
- player preview
- client project library
- file import/export
- Loom Code clipboard/UI

### Server

Responsibilities:
- validate saved/equipped project references
- distribute project blobs on cache miss
- persist equipped state
- enforce project/asset/animation size limits
- protect against malformed or abusive transfers

## Project pipeline

Editable project:
layers + gradients + imported assets + transforms + animations

→ validate

→ compile/composite

→ runtime cape/Elytra textures + optional effect masks

→ cache by project/content hash

→ render through vanilla-first cape/Elytra paths

## Rendering strategy

### Base cape

Patch/substitute the cape texture data used by the player's render state and allow Minecraft to render normal cape geometry and motion.

### Base Elytra

First attempt the dedicated Elytra texture slot exposed by player skin data. If this is insufficient, use the narrowest possible equipment/feature hook.

### Emissive/additive effects

Use a separate Loom Studios feature pass aligned with the corresponding cape/Elytra geometry. If shader compatibility fails, the optional effect may be disabled while preserving the base cosmetic.

## Editor preview

The preview renders a synthetic/isolated player state inside the GUI and points it at temporary project output. Unsaved preview state must not alter real equipped multiplayer state.

For timeline authoring, the preview may request a fixed schema-v3 timeline tick. That fixed-tick bundle is isolated under a preview-only hash, updates cached textures in place while scrubbing, and is excluded from normal world-time animation ticking.

## Multiplayer flow

1. player saves/equips project locally
2. client computes project hash
3. client sends validated equip/save request
4. server stores authoritative equipped hash/metadata
5. remote client learns equipped hash
6. remote client checks local cache
7. cache miss requests project blob
8. blob is validated and cached
9. remote client compiles/renders locally

Animation frames are never streamed continuously.

Schema-v3 animation definitions travel inside the normal content-addressed project blob. Each client evaluates the timeline locally.

## Persistence

- client library stores editable local projects
- server stores equipped references and server-shared project blobs/metadata as required
- caches use content hashes
- schema is versioned and migratable

## UI architecture

Build reusable Loom widgets:
- LoomPanel
- LoomButton
- LoomTab
- LoomSlider
- LoomDropdown
- LoomTooltip
- LoomIconButton
- LoomScrollPanel
- LoomLayerRow
- LoomTimeline
- LoomColorPicker
- LoomCanvas
- LoomPlayerPreview

Reference images define visual targets, not screenshot backgrounds.

## Compatibility philosophy

Loom Studios should integrate with Minecraft/Fabric rendering rather than replace large rendering systems. Sodium and Iris must work without being dependencies.


## Implemented multiplayer proof

The SPIKE-05 implementation now proves the intended separation:

```
editable/project bytes
        |
        v
     SHA-256
        |
 Client HELLO(hash)
        |
        v
 Server project cache
        |
        +---- cache miss ---> request blob once
        |
        v
 equipped UUID -> hash
        |
        v
 remote client
        |
        +---- local cache miss ---> request blob once
        |
        v
 local texture compiler / animator
```

This keeps rendered frames off the network and makes the future .loom project store/cache architecture concrete.


## Phase-1 live client ownership

The local editing/runtime path now has a single source of truth:

```
ClientProjectWorkspace
        |
        v
   ProjectSession
   /     |      \
history dirty   LoomProject
                 |
        +--------+--------+
        |                 |
        v                 v
ClientCosmeticSync   RuntimeCosmeticCache
(multiplayer hash)  (compiled GPU textures)
        |                 |
        +--------+--------+
                 |
                 v
       PlayerCosmeticRenderer
```

Networking no longer owns a second local project copy.

Saved project discovery is separate:
`ProjectLibraryIndex -> ProjectDescriptor -> cached cape thumbnail`.

The Home screen now consumes that index through lazily loaded `LoomProjectCard` thumbnails and an isolated selected-project `LoomPlayerPreviewWidget`.

Cape and Elytra editors share the same canvas-backed typed `LoomLayerListWidget`; target-specific editing semantics remain separate.

Elytra editing maps the two semantic 10x20 front-face wings through `ElytraWing`, while runtime output remains the standard full Elytra canvas.

This ownership model is the baseline for editor widgets.


## Editing versus equipped state

Phase 1 now enforces a second ownership boundary:

```
ProjectSession (dirty allowed)
        |
        +---- editor preview / widgets
        |
       save
        |
        v
saved .loom
        |
      equip
        v
Equipped LoomProject snapshot
        |
        +---- world renderer
        +---- multiplayer sync
```

Unsaved edits never automatically cross the equip/network boundary.

Current enforcement:
- editor widgets and scoped 3D preview read `ProjectSession`;
- world rendering reads the equipped snapshot;
- `ClientCosmeticSync` announces/uploads the equipped hash/project only;
- Save updates persistence only;
- Save + Equip advances the world/network snapshot.

## Schema-v3 animation pipeline

```
LoomAnimation
  duration / loop / playback speed
          |
          v
  AnimationTrack[]
 layer UUID + channel + effect
 enabled + speed + loop
          |
          v
 AnimationKeyframe[]
      tick + value
          |
          v
   AnimationEvaluator
          |
          v
 LoomTextureCompiler.compileAnimated
          |
 RuntimeCosmeticCache
  Cape / Elytra / emissive textures
```

Current effects are evaluated deterministically and locally. The legacy runtime hue-cycle flag remains compatibility-only behavior; new authoring uses schema-v3 tracks.

## Editor workspace geometry

`CanvasViewportTransform` is common-core integer geometry shared by rendering and input. Pixel selections are inclusive in project space and exclusive on their screen right/bottom boundaries. Texture, grid, cursor, live drag and committed outline derive from those boundaries. `LoomWorkspaceLayout` assigns bounded regions for both editors, including compact and adaptive timeline composition. `LoomInspectorLayout` allocates visible property rows inside the assigned inspector.

Cape and both semantic Elytra wings use revision-cached textures; layer rows cache real raster thumbnails. Canvas zoom/pan/grid and preview rotation/zoom persist across screen rebuilds. Tool controls belong to the tool context; layer/gradient fields belong to inspector pages; animation fields belong to Keys/Playback. Floating Swatches remains an explicit overlay.

`LoomUiCapture` is an opt-in development-only test harness (`-PuiCapture`). It creates an isolated test world, renders the real widgets, asserts bounds, captures screenshots and exits. Reflection is confined to this harness to exercise private editor contexts without exposing production APIs.


### Workshop shell (2026-10-03)

`LoomWorkshopArt` draws deterministic code-native pixel artwork inside shared chrome and preview bounds. `LoomScreenChrome` owns the reserved brand header and bounded status footer. `LoomWorkspaceLayout` reserves 36/56 header units and expands tool labels only in spacious profiles. Smart Import owns source, settings, processed/atlas and candidate-player regions separately. `LoomCaptureFixtures` is called only by the opt-in development capture runner; it exercises real projects, thumbnails, conversion and image processing without adding production templates.

## Feedback preview/input ownership (2026-10-03, verified)

LoomPreviewState owns a fresh Mojang-mapped renderer snapshot; widget and expanded screen share orientation/extraction. PreviewOverride scopes the local cosmetic supplier, uses a distinct content-hash namespace and restores nested overrides. PlayerCosmeticRenderer caches codec/hash work by immutable project identity; RuntimeCosmeticCache applies checkerboard only to preview bundle base images, preserving exported/equipped alpha and emissive masks. Fixed-tick playback recompiles only channels with enabled tracks. Small inline previews open the full player preview through their header expand affordance or double click, preserving the timeline supplier.

Screen captures middle-button drag/release on both canvases and gives floating palettes pointer priority. Palette close/Escape updates the parent visibility flag. WorkspaceLayout reserves the shell boundary and adaptive inspector/timeline budgets down to 600×320; PixelShapes shares ellipse pixels across live feedback and ProjectEdits commit/history. No schema or wire-protocol change.

LoomPointerScreen now captures the middle-button pointer for marked canvas/3D widgets on all five workspace screens. This also enables the user’s middle-pan expectation in the 3D viewport, with scissored screen-space offsets and view-state preservation. Expanded preview owns the same pan gesture directly and uses workshop chrome/scenery. Capture targets include five inline preview pans, expanded input and both filled circle gesture phases (68 total).

Preview skin patches use a separate UUID cache from equipped/world skin patches, preventing both render passes from invalidating each other every frame. The capture harness alternates world/preview extraction twenty times and asserts identical preview skin object plus zero additional codec hashes. Alpha-guide isolation is verified for both Cape and Elytra images.

Feedback architecture verified at `b7f7212388858109b96f3e8fc8a7175bc3ef8c45` in Actions #191; fresh snapshots, explicit GUI rear-view quaternion, separate preview namespaces/cache, alpha isolation and routed input passed. No schema/network change.

## Library and authoring milestone (verified)

EditorPreferences is a local atomic properties store; favorites/preferences do not change project schema or portable hashes. ProjectFileStore gains UUID-preserving rename and reversible Trash moves with non-overwriting restore. WorkspaceRecovery writes dirty snapshots to a separate draft store, never equipping/publishing them. Library collection pagination keeps controls bounded. Shared PixelPatch/PixelDrawing/SurfaceEdits operate on semantic faces and preserve untouched UV pixels and layer eligibility. Shared animation workspace uses existing schema-v3 tracks with compound keyframe drags. Preview poses affect only owned render snapshots.

TemplateCatalog supplies original immutable layered pixel designs; selecting a template forks a new project ID. Smart Import handles bind to the existing normalized LayerTransform and candidate/apply pipeline. The shared animation screen retains schema-v3 tracks/effects and compound gesture history. Local settings remain outside portable artwork and multiplayer state. Final evidence: Actions #200 at 9b22438db8c8aea42a10ee5800000c56415ac4e7.
