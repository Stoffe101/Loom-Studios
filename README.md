# Loom Studios

Loom Studios is a Minecraft Java Edition Fabric mod for creating, editing, animating, importing, previewing, equipping, and sharing custom capes and Elytra designs directly inside Minecraft.

## Target

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.18.4**
- Java **21**
- Sodium compatibility: **optional supported integration**
- Iris + shader compatibility: **optional supported integration**

## Current status

**Animation 2.1 and creative authoring are ready for user testing.** The final Linux verification passes164 tests and all four resolution/GUI-scale profiles; see [exact-source acceptance](docs/CURRENT_STATE.md) and [new authoring workflows](docs/CREATIVE_AUTHORING.md).

The current development build includes schema-v5 editable projects with v1–v4 migration and bounded compressed layer storage, Paint/Image/Gradient layers, persistent layer locking, save/load + undo/redo, equipped-state multiplayer synchronization, cape/Elytra runtime rendering, 1x/2x/4x/6x/8x editable texture resolutions, isolated 3D preview, the Cape Editor, grouped Swatches, selection Move/Flip tooling, non-destructive PNG/JPEG/GIF/BMP/TIFF/WBMP Smart Import for Cape and Elytra, the reference-oriented Home dashboard, independent Elytra outside/inside/edge editing with linked/separate wings, cape-to-Elytra conversion, project-authored thickness, a real animation timeline with layer-targeted tracks/keyframes plus Pulse/Scroll/Hue Shift/Moving Gradient/Sparkle/Emissive Glow, and offline-first Loom Codes sharing with portable project codes, import preview, `.loom` exchange and Cape/Elytra PNG export.

Authoring also includes typed effect parameters and easing, automatic GIF playback, Alpha Lock, Clip to Layer Below, editable masks, exact-color Wand/Replace tools, five brush stamps, unfolded cape seam editing, and local background removal/tint/brightness/contrast/saturation with image-to-swatches. See [the authoring guide](docs/AUTHORING_V4.md) for usage, limits and compatibility.

Animation 2.1 adds independent typed parameter lanes, multi-key selection/copy/paste, editable easing curves, collapsible tracks and timeline zoom. Both editors also offer editor-only reference guides, onion skins, GIF-to-editable-frame conversion and a saved custom stamp library with favorites, rotation/mirroring and six pattern packs. Reference guides/stamps stay local; project schema5 and multiplayer protocol3 require the new release on both peers.

The studio includes smooth labels/vector icons, a responsive workshop UI, compact layers, guided animation recipes and direct effect/preset/rate dropdowns, cosmetic-only previews, and a design library with favorites, folders/tags, recoverable Trash, bulk actions and backup/version history.

Automated Linux client verification covers 1920×1080 and 3440×1440 at GUI scales2/3. See docs/CURRENT_STATE.md for exact tested commits, screenshots and workflow evidence. Target-hardware performance, Windows/macOS native loading, optional modpacks/shaders and live multiplayer acceptance remain separate release checks.

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
