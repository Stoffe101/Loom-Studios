# Loom Studios — Next Work

## Active: SPIKE-04 — live GUI player preview

Build the first Loom Studios GUI rendering proof.

Goals:
- open a custom development screen from a temporary keybind;
- render the actual local player's skin/model inside the screen;
- show Loom cape/Elytra cosmetics on the preview;
- rotate the preview with mouse drag;
- provide basic zoom;
- switch Cape / Elytra preview modes;
- keep preview state isolated from the actual equipped/world state where possible;
- reuse the same cosmetic texture/render path rather than inventing a fake preview renderer.

The SPIKE-04 UI is intentionally functional rather than pretty. It exists to prove the preview architecture before the reference-image editor is built.

## Approved future Elytra editor control

- Thickness slider
- 100% default = vanilla
- thinner/thicker cosmetic values
- Reset to Vanilla
- no gameplay/hitbox changes

## After SPIKE-04

SPIKE-05 — multiplayer synchronization.

## Documentation

Record exact CI SHA, local runtime result, preview behavior, and any render-state isolation limitations.
