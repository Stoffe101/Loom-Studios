# Loom Studios — Next Work

## Immediate local verification

Use the current head after pulling.

### Palette interaction regression

1. open Palettes;
2. click the name field and type;
3. New must create a palette;
4. Add Current Color must update it;
5. saved palette rows must be clickable;
6. palette swatches must switch the active editor color;
7. right-click a saved swatch must remove it;
8. Pin/unpin + window dragging must work;
9. Export must copy a `LOOMPAL1:` share code;
10. Import must accept that clipboard code.

This pass specifically fixes the overlay input-routing bug from the previous local test.

### Canvas zoom / pan / cursor

1. hover Pencil/Eraser at brush size 1 and several larger sizes;
2. confirm the cyan circle reflects brush radius;
3. use Zoom + / Zoom -;
4. click the percentage to reset to 100%;
5. mouse-wheel over the canvas to zoom;
6. at >100%, middle-drag to pan;
7. verify drawing coordinates remain correct after panning/zooming;
8. verify 4x remains smooth at high zoom.

### New paint tools

- Fill: connected-color flood fill must stay inside the active semantic cape face;
- Eyedropper: clicking a visible pixel must update the HSV/RGB picker and active paint color.

Keyboard:
- B Pencil
- E Eraser
- G Fill
- I Eyedropper
- [ / ] brush size
- 0 reset zoom
- Ctrl+Z / Ctrl+Y
- Ctrl+S
- Ctrl+Shift+S

## Next Phase-2 milestone

After this local verification:
1. Line tool with live drag preview;
2. Rectangle tool with outline/filled mode;
3. editable hex and RGB numeric text fields;
4. alpha/opacity control;
5. symmetry;
6. layer panel;
7. keyboard-shortcut help/tooltip surface;
8. Recent Project thumbnail rendering;
9. Cape Loom block interaction;
10. Elytra Editor.

## Required UI profiles

Continue checking:
- 1920x1080 GUI x2;
- 1920x1080 GUI x3;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.
