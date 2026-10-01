# Loom Studios — Next Work

## Runtime verification queue

These features are implemented/CI-green but still need local in-game verification when available:

### Compact Swatches
- 1920x1080 GUI 3 smaller footprint;
- no Saved palettes overlap;
- Edit / Done management collapse;
- multiple palettes;
- move + Pin.

### Color / alpha
- editable Hex/R/G/B/A;
- alpha slider;
- semi-transparent paint;
- alpha-aware custom palette export/import.

### Symmetry
- Off;
- Horizontal;
- Vertical;
- Both;
- Pencil/Eraser/Fill/Line/Rectangle.

### Layers
- New / Duplicate / Delete;
- selection;
- visibility;
- reorder;
- opacity;
- rename;
- Emissive;
- blend modes:
  - Normal
  - Add / Glow
  - Screen
  - Multiply
  - Overlay
- Undo/Redo;
- 3D Preview compositing.

## CI-safe work that can continue without local runtime

Priority order:

1. expose the implemented PixelSelection model in the Cape Editor;
2. drag-to-select overlay;
3. Move/nudge selected pixels;
4. Flip Horizontal / Vertical controls;
5. crop/selection bounds architecture;
6. Recent Project thumbnail-card component;
7. reusable tooltip/icon-button primitives;
8. Smart Import processing core tests;
9. Elytra semantic UV/editor model;
10. animation authoring schema research/design.

## Schema-expansion gate

Do **not** bolt persistent layer lock or new layer kinds directly onto schema v1.

The next schema version should group:
- persistent layer lock;
- layer kind/type;
- non-destructive image-layer data;
- gradient-layer data;
- effect/animation metadata as appropriate;
- migration fixtures from schema v1.

## Reference-image priority

Every new UI feature must be checked against the five approved reference screens.

See `REFERENCE_FIDELITY_ROADMAP.md`.

The near-term goal is not final pixel-perfect styling. It is to build the correct screen hierarchy and reusable controls so the final fidelity pass does not require another UI rewrite.

## Required UI profiles

- 1920x1080 GUI x2;
- 1920x1080 GUI x3;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.
