# Loom Studios — Architectural Decisions

## ADR-001 — Minecraft 1.21.11 / Loader 0.18.4 baseline
**Status:** Accepted

Develop for Minecraft 1.21.11 and actively verify Fabric Loader 0.18.4.

## ADR-002 — Java 21
**Status:** Accepted

Use Java 21.

## ADR-003 — Mojang mappings
**Status:** Accepted

Use official Mojang mappings.

## ADR-004 — Split common/client source sets
**Status:** Accepted

Keep client rendering/UI code out of dedicated-server execution paths.

## ADR-005 — Vanilla-first cape rendering
**Status:** Accepted pending SPIKE-01

Substitute Loom cape texture/state while Minecraft retains geometry and movement.

## ADR-006 — Vanilla-first Elytra path
**Status:** Proposed pending SPIKE-03

Use the dedicated Elytra texture path first; add a narrow hook only if required.

## ADR-007 — Separate optional emissive pass
**Status:** Proposed pending SPIKE-06

Base cosmetics must not depend on shader-sensitive glow rendering.

## ADR-008 — No live edit-stream networking
**Status:** Accepted

Synchronize saved/equipped projects, not brush strokes or rendered animation frames.

## ADR-009 — Content-addressed project distribution
**Status:** Accepted

Use SHA-256 project identity/cache-miss transfer.

## ADR-010 — .loom is editable source of truth
**Status:** Accepted

Flattened textures are compiled/export products.

## ADR-011 — Reusable Loom UI kit
**Status:** Accepted

Build reusable UI controls, not screenshot-backed screens.

## ADR-012 — Documentation hard gate
**Status:** Mandatory

No meaningful pass is complete without documentation updates.

## ADR-013 — Optional compatibility mods
**Status:** Accepted

Sodium, Sodium Extra, Iris, shader packs, and 3D Skin Layers are not dependencies. Plain Fabric is the core runtime. Optional compatibility is actively supported and smoke-tested.

## ADR-014 — Proprietary licensing
**Status:** Accepted

Loom Studios remains proprietary / All Rights Reserved. Third-party test JARs are not bundled or committed.

## ADR-015 — dev-mods/ + modLocalRuntime
**Status:** Accepted

Optional compatibility JARs live in gitignored `dev-mods/` and are loaded only by the Loom development runtime.


## ADR-016 — Patch PlayerSkin in AvatarRenderer render-state extraction
**Status:** Implemented for SPIKE-01, runtime verification pending

For Minecraft 1.21.11, inject at the tail of `AvatarRenderer.extractRenderState(Avatar, AvatarRenderState, float)`.

For the static cape spike:
- affect only the local player;
- replace only the cape field using `PlayerSkin.Patch`;
- preserve body, Elytra, and model-type fields;
- force `showCape` only for the development spike;
- cache the patched skin to avoid allocating a new record every rendered frame.

This deliberately keeps vanilla `CapeLayer` responsible for geometry and movement.


## ADR-017 — Disable Gradle configuration cache for development
**Status:** Accepted

Loom Studios prioritizes a predictable IntelliJ + Fabric Loom development workflow over configuration-cache reuse.

Gradle configuration cache is disabled because Loom/custom development tasks and generated run configurations still have rough edges around cache-safe script closures. This avoids repeated false failures in helper tasks such as `listDevMods` and reduces friction during rapid client testing.


## ADR-018 — One registered DynamicTexture per active compiled cape
**Status:** Implemented for SPIKE-02, runtime verification pending

The dynamic-cape proof uses one long-lived `NativeImage` and one registered `DynamicTexture`.

Changes mutate the backing image and call `upload()` only when output is dirty. The texture identifier remains stable, so the player render-state patch does not churn resources or allocate a new skin record every update.

Texture resources are released during client shutdown.

This is the baseline ownership model for the future editor/compiler cache.


## ADR-019 — Cape and Elytra are independent texture channels
**Status:** Accepted

Minecraft 1.21.11's WingsLayer selects:
1. the player's dedicated Elytra texture when present;
2. otherwise the visible cape texture;
3. otherwise the equipment/default texture.

Loom Studios therefore supplies independent cape and Elytra textures by default. A future editor option may explicitly link or derive Elytra artwork from the cape, but automatic vanilla fallback must not silently substitute the cape when a Loom project intends separate designs.

## ADR-020 — Preserve vanilla Elytra geometry by default
**Status:** Accepted for core path

The default Loom Studios Elytra renderer keeps vanilla WingsLayer/ElytraModel geometry and animation for compatibility.

The vanilla model is intentionally volumetric (10x20x2 plus CubeDeformation 1.0 per wing), which can look thick. The first low-risk visual experiment hides edge-face UV strips through transparency.

A future optional "Slim Elytra" visual mode may use custom geometry if desired, but it must be opt-in and separately compatibility-tested.


## ADR-021 — Elytra thickness is a geometry setting, not a texture trick
**Status:** Accepted

Default Loom Studios Elytra rendering must preserve vanilla wing thickness.

User customization may expose an Elytra thickness control. The preferred implementation is local model-depth scaling while keeping vanilla WingsLayer animation and equipment behavior.

Proposed semantics:
- 100% = exact vanilla thickness;
- values below 100% = slimmer wings;
- values above 100% = chunkier/stylized wings;
- setting is purely visual and never changes collision/hitboxes.

Do not implement "thin Elytra" by making UV edge faces transparent. That changes visual surface coverage rather than actual geometry and produced an undesirable paper-thin result.


## ADR-022 — Preview uses isolated extracted render state
**Status:** Implemented for SPIKE-04, runtime verification pending

The Loom Studios GUI preview renders the real local player entity through Minecraft's normal entity renderer, but modifies only the extracted RenderState used for that GUI submission.

This allows preview-only changes such as:
- cape versus Elytra mode;
- preview equipment;
- later preview animation/pose options;

without equipping items or mutating the actual world/player entity.

The preview should reuse the same Loom cosmetic texture/render integrations as gameplay rather than maintaining a separate fake model pipeline.


## ADR-023 — Server-authoritative equipped hash with client-side compiled cache
**Status:** Implemented for SPIKE-05, runtime verification pending

The server owns the authoritative UUID -> equipped project hash mapping. Project blobs are content-addressed by SHA-256 and transferred only on cache miss.

Clients verify received blobs before caching/compiling them.

Players connected to a server without Loom Studios keep local-only Loom rendering because client sends are guarded by `ClientPlayNetworking.canSend`.

## ADR-024 — Animation time is evaluated locally
**Status:** Implemented for SPIKE-06, runtime verification pending

Animation definitions/parameters belong to project data. Clients derive animation phase locally from synchronized game time. Rendered texture frames are never continuously transmitted.

## ADR-025 — Emissive cape is an additive feature layer
**Status:** Implemented for SPIKE-06, runtime verification pending

The base cape remains Minecraft's normal CapeLayer. Loom Studios registers an additional player feature layer that submits a second PlayerCapeModel using the project's generated emissive mask and Minecraft's translucent-emissive render type.

If this optional layer has a compatibility problem, disabling it must leave the base cape intact.


## ADR-026 — User-facing Elytra 100% is a calibrated visual baseline
**Status:** Accepted from runtime feedback

Minecraft's raw 1.21.11 Elytra model uses a visibly volumetric wing box. With Loom Studios' fully opaque custom edge UVs, raw model `zScale=1.0` appears substantially fatter than the normal vanilla Elytra appearance.

For Loom-owned Elytra rendering:
- user-facing 100% thickness maps to raw model Z scale 0.5;
- thinner/thicker controls are relative to that baseline;
- non-Loom Elytras remain raw vanilla Z scale 1.0;
- custom edge-face artwork remains opaque but uses a recessed/darker treatment by default;
- this affects rendering only.

This supersedes the earlier assumption that raw model Z scale 1.0 should necessarily be the user-visible 100% baseline.

## ADR-027 — Schema-v1 project core is deterministic bounded binary data
**Status:** Accepted / implemented

The first real .loom project representation uses a deterministic bounded binary codec rather than Java object serialization.

Reasons:
- stable SHA-256 content identity;
- strict size/count validation before allocation;
- explicit schema versioning;
- straightforward server/client parity;
- no arbitrary-class deserialization surface.

Schema v1 currently contains project identity/name, runtime settings, cape/Elytra canvases and ordered paint layers.

Future structural changes require explicit schema migration.

## ADR-028 — Phase-1 undo/redo starts with immutable project snapshots
**Status:** Accepted / implemented foundation

Editor changes are expressed as project transformations through ProjectHistory.

The first implementation stores bounded immutable project snapshots because it keeps correctness simple while tools are still being designed.

High-volume paint operations may later store specialized deltas internally without changing the editor/session-facing undo/redo contract.


## ADR-029 — ProjectSession owns editor history and dirty state
**Status:** Accepted / implemented

The future editor must not independently track "changed" booleans alongside project history.

One ProjectSession owns:
- ProjectHistory;
- current project revision;
- persisted source path;
- last saved content hash;
- dirty-state calculation.

Dirty state is content-based: current project hash differs from the last saved hash, or the project has never been saved.

This naturally allows undoing back to the exact saved project to become clean again.

## ADR-030 — File storage core is independent from Fabric game-directory lookup
**Status:** Accepted / implemented

ProjectFileStore receives an explicit root Path and contains all bounded load/save/list behavior.

LocalProjectLibrary is only the Minecraft/Fabric adapter that points the store at the game's Loom Studios project folder.

This keeps file safety and save/load behavior unit-testable without starting Minecraft.

## ADR-031 — Every schema load passes an explicit migration gate
**Status:** Accepted / implemented

LoomProjectCodec does not silently treat unknown schema versions as current data.

Loading first inspects the schema version and dispatches through LoomProjectMigrations.

Schema v1 currently decodes directly because there is no released legacy schema. Future schema versions must add explicit migration logic.


## ADR-032 — ClientProjectWorkspace is the sole local editable project owner
**Status:** Accepted / implemented

Networking, rendering and the future editor must not each retain their own mutable local project copy.

ClientProjectWorkspace owns exactly one local ProjectSession. Consumers query the workspace project/hash/revision.

When the content hash changes, ClientCosmeticSync republishes the new hash through the existing cache-miss protocol.

## ADR-033 — GPU texture lifetime belongs to RuntimeCosmeticCache
**Status:** Accepted / implemented

Project/session state contains editable data only.

RuntimeCosmeticCache owns:
- NativeImage buffers;
- DynamicTexture objects;
- registered texture identifiers;
- animation-phase uploads;
- hash-keyed compiled runtime bundles.

PlayerCosmeticRenderer owns only render-state patching and small render-facing lookup/debug behavior.

## ADR-034 — Recent Projects index tolerates individual corrupt files
**Status:** Accepted / implemented

A single malformed .loom file must not prevent the library/home screen from opening.

ProjectLibraryIndex loads each project independently, logs/rejects broken entries, and exposes the count of rejected files.

Thumbnails are derived caches and are keyed by project UUID + content-hash prefix.


## ADR-035 — Editing state and equipped state are separate
**Status:** Accepted / implemented

ClientProjectWorkspace owns both concepts but they have different lifecycles.

Editing state:
- lives in ProjectSession;
- may be dirty;
- feeds future editor/canvas/preview widgets;
- supports undo/redo before saving.

Equipped state:
- is a saved immutable LoomProject snapshot;
- is what PlayerCosmeticRenderer shows in the world;
- is what ClientCosmeticSync publishes to the server;
- changes only through an explicit equip action.

`equipCurrent()` requires the session to be clean. `saveAndEquip()` performs the common combined action.

This prevents a paint stroke or undo operation from unexpectedly changing other players' view before the user saves/equips.

## ADR-036 — Workspace widgets observe immutable WorkspaceState snapshots
**Status:** Accepted / implemented foundation

ClientProjectWorkspace exposes listener registration using immutable WorkspaceState values containing:
- current project;
- revision;
- dirty state;
- source path;
- equipped project/hash.

Phase-2 UI widgets should react to these snapshots rather than polling unrelated global fields.


## ADR-037 — Editor preview uses a scoped render-state project override
**Status:** Accepted / implemented

The in-GUI player preview must show unsaved ProjectSession output without mutating the player's world/equipped state.

PlayerCosmeticRenderer therefore supports a scoped preview override only while the preview extracts the local player's render state.

Outside that scope:
- world renderer uses the equipped project;
- multiplayer publishes the equipped project;
- no global player entity/equipment mutation occurs.

Preview runtime texture bundles are derived caches and are evicted when the preview hash changes or the screen closes.

## ADR-038 — Project-library selection belongs to the library model
**Status:** Accepted / implemented foundation

ProjectLibraryIndex owns the currently selected project UUID and preserves it across refreshes when the project still exists.

If selection disappears, the newest remaining project becomes the default selection.

This gives the Phase-2 home/Recent Projects screen stable selection behavior independent of widget instances.
