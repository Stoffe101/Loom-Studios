# Loom Studios

Loom Studios is a Minecraft Java Edition Fabric mod for creating, editing, animating, importing, previewing, equipping, and sharing custom capes and Elytra designs directly inside Minecraft.

## Target

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.18.4**
- Java **21**
- Sodium compatibility: **optional supported integration**
- Iris + shader compatibility: **optional supported integration**

## Current status

**Editor productization + animation authoring are active.**

The current development build includes schema-v3 editable projects with explicit v1 -> v2 -> v3 migration, Paint/Image/Gradient layers, persistent layer locking, save/load + undo/redo, equipped-state multiplayer synchronization, cape/Elytra runtime rendering, 1x/2x/4x editable texture resolutions, isolated 3D preview, the Cape Editor, grouped Swatches, selection Move/Flip tooling, non-destructive Smart Import for Cape and Elytra, the reference-oriented Home dashboard, semantic Elytra editing with linked/separate wings, cape-to-Elytra conversion, project-authored thickness, a real animation timeline with layer-targeted tracks/keyframes plus Pulse/Scroll/Hue Shift/Moving Gradient/Sparkle/Emissive Glow, and offline-first Loom Codes sharing with portable project codes, import preview, `.loom` exchange and Cape/Elytra PNG export.

The current branch also contains a major responsive reference-UI shell refactor: icon-led tool rails, contextual inspectors, compact 640x360 behavior, tabbed Smart Import, simplified animation authoring, and explicit Share / Export navigation.

Local re-verification of that new shell, preview-state polish, Cape Loom block flow, and final Iris/shader hardening remain in progress.

## Documentation

The canonical project documentation lives in the **docs/** directory.

Documentation is a **hard completion gate**. A meaningful research, implementation, bug-fix, rendering, networking, UI, compatibility, or testing pass is not complete until the relevant documentation is updated.

Start with:

- docs/CURRENT_STATE.md
- docs/NEXT_WORK.md
- docs/PASS_LOG.md
- docs/DOCUMENTATION_RULES.md
- docs/GETTING_STARTED.md
- docs/COMPATIBILITY.md

## License

Loom Studios is proprietary software. See LICENSE.

Copyright © 2026 Christoffer Lööf. All Rights Reserved.
