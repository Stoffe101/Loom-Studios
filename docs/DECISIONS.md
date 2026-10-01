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
