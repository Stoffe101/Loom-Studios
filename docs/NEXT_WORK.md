# Loom Studios — Next Work

## Immediate local verification

### Compact Swatches regression

At **1920x1080 GUI scale 3**:
1. open Swatches;
2. confirm the window is substantially smaller than the previous screenshot;
3. confirm Saved palettes no longer overlaps Import/Export;
4. confirm compact mode opens with management controls collapsed;
5. click Edit -> controls appear;
6. click Done -> controls collapse and the swatch area expands;
7. verify several named palettes remain directly clickable;
8. test move + Pin.

Also recheck:
- 1920x1080 GUI x2;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.

### Numeric color / alpha

- type a Hex color and verify picker/sliders update;
- type R/G/B values and verify Hex/picker update;
- change A from 255 to a lower value;
- paint semi-transparent pixels;
- add a transparent/semi-transparent color to a custom palette;
- export/import it and verify alpha survives.

### Symmetry

Test Pencil, Eraser, Fill, Line and Rectangle with:
- Off;
- Horizontal;
- Vertical;
- Both.

Confirm symmetry always stays inside the active semantic face.

### Layers

- New layer;
- select rows;
- paint different artwork on each layer;
- toggle visibility with the left visibility square;
- Duplicate;
- Layer Up / Layer Down;
- change opacity;
- Delete;
- Undo / Redo each operation;
- verify the last remaining layer cannot be deleted;
- verify 3D Preview reflects the composed layer stack.

## Next Phase-2 work

After this verification:
1. layer rename;
2. layer lock;
3. emissive-layer/effect controls;
4. blend-mode UI once more blend implementations exist;
5. transform/selection tools;
6. shortcut/tooltips help surface;
7. Recent Project thumbnail cards;
8. Cape Loom block interaction;
9. Elytra Editor;
10. smart PNG/image import after layer/transform foundations are stable.

## Required UI profiles

Every editor layout change continues to target:
- 1920x1080 GUI x2;
- 1920x1080 GUI x3;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.
