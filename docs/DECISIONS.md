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
