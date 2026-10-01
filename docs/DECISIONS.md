# Loom Studios — Architectural Decisions

This file is append-only in spirit. Decisions can be superseded, but old reasoning should remain visible.

## ADR-001 — Target Minecraft 1.21.11 and Loader 0.18.4 baseline

**Status:** Accepted

Develop for Minecraft 1.21.11. Compatibility testing must include Fabric Loader 0.18.4 exactly.

## ADR-002 — Java 21

**Status:** Accepted

Use Java toolchain 21.

## ADR-003 — Mojang mappings

**Status:** Accepted provisionally until SPIKE-00

Prefer official Mojang mappings for the 1.21.11 project.

## ADR-004 — Split common/client source sets

**Status:** Accepted

Use split source sets so renderer/UI classes never leak into dedicated-server execution paths.

## ADR-005 — Vanilla-first base cape rendering

**Status:** Accepted pending spike validation

Substitute Loom Studios cape texture state while allowing Minecraft to retain cape geometry, motion, crouch behavior, and Elytra visibility logic.

## ADR-006 — Vanilla-first Elytra override

**Status:** Proposed / SPIKE-03 required

First attempt the dedicated player-skin Elytra texture path. Fall back to a narrow equipment/feature hook only if necessary.

## ADR-007 — Separate emissive feature pass

**Status:** Proposed / SPIKE-06 required

Base texture stays on the vanilla renderer. Emissive/additive effects render through a Loom Studios feature pass aligned with vanilla geometry.

## ADR-008 — No live edit-stream networking

**Status:** Accepted

Editing is client-local. Network traffic occurs on save/equip/share rather than for every brush stroke or animation frame.

## ADR-009 — Content-addressed project distribution

**Status:** Accepted

Use SHA-256 project identity and cache-miss transfer for saved/equipped multiplayer designs.

## ADR-010 — Full projects are not player attachments

**Status:** Accepted

Only small state such as equipped project hashes belongs directly on player synchronization state. Large project blobs use dedicated storage/cache transfer.

## ADR-011 — .loom is editable source of truth

**Status:** Accepted

Flattened PNGs are runtime/export products, not the only project representation.

## ADR-012 — Custom reusable Loom UI kit

**Status:** Accepted

Build reusable panels, buttons, tabs, sliders, timelines, color pickers, canvas widgets, and player previews instead of using screenshot backgrounds or vanilla-only buttons.

## ADR-013 — Documentation is a hard gate

**Status:** Accepted / mandatory

No meaningful development or research pass is complete until canonical documentation is updated.

## ADR-014 — Sodium + Iris are first-class compatibility targets

**Status:** Accepted / mandatory

Rendering milestones must pass the Fabric-only, Sodium, Sodium+Iris shaders-off, and Sodium+Iris shaders-on matrix.

## ADR-015 — Proprietary licensing

**Status:** Accepted

Loom Studios source, assets, UI artwork, branding, and original project materials remain proprietary / All Rights Reserved. Official compiled releases may be used under the repository LICENSE. No permissive open-source license such as MIT is granted.

## ADR-016 — Respect third-party licenses

**Status:** Accepted

Record external licenses before reusing implementation code or assets. Researching behavior and compatibility patterns is allowed; copying incompatible licensed code is not.
