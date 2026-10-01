# Loom Studios — UI Compatibility Contract

This document defines the minimum desktop GUI profiles Loom Studios must support cleanly.

It is a hard UI compatibility gate. New editor panels, floating windows, tool rows, preview controls, and future Elytra/animation interfaces must be checked against every profile below.

## Required profiles

| Physical resolution | Minecraft GUI scale | Approximate logical GUI area | Required |
| --- | ---: | ---: | --- |
| 1920×1080 | 2 | 960×540 | YES |
| 1920×1080 | 3 | 640×360 | YES |
| 3440×1440 | 2 | 1720×720 | YES |
| 3440×1440 | 3 | ~1147×480 | YES |

Minecraft may round the logical width/height by a pixel depending on platform/window state. Layout code must not depend on an exact quotient.

## Editor requirements

At every required profile:

- the main editing canvas must remain usable;
- no required action may be clipped beyond the screen;
- the right tool rail must scroll when its content is taller than its viewport;
- the scrollbar must remain mouse-operable;
- tool rows must not overlap;
- disabled labels/buttons must remain legible;
- floating windows must be clamped so their title bar remains reachable;
- movable windows may overlap the canvas by design, but must never become permanently stranded off-screen;
- pinned windows must preserve their position while editing;
- screen rebuilds/resolution changes must not silently reset editor state;
- 3D Preview, Save, Save + Equip, Undo/Redo, and Back must remain reachable.

## High-resolution canvas performance

1x, 2x, and 4x project resolution must not change the editor layout contract.

The canvas renderer uses a revision-cached DynamicTexture so project resolution should primarily affect edit/upload cost rather than per-frame GUI draw-call count.

A continuous brush stroke is one compound history entry.

Performance regressions should be tested especially at:

- 1920×1080 / GUI 3, because it is the tightest required logical viewport;
- 4x / 256×128 project resolution, because it is the heaviest currently supported editable texture.

## Right tool rail

The right-side editor controls live inside Minecraft's ScrollableLayout.

This is intentional. Do not return to a fixed absolute vertical stack.

Future controls may extend the rail without forcing the canvas smaller or allowing controls to fall below the screen.

## Floating editor windows

The Palettes window is the first floating tool window and establishes the expected behavior for future Layers, Animation, Import, Effects, and similar windows:

- movable by title bar;
- pinnable;
- clamped to screen bounds;
- independent from the tool-rail scrollbar;
- retains state while the current editor screen remains alive;
- may be hidden without losing palette/library data.

Future floating windows should reuse the same interaction model unless a newer ADR supersedes it.

## Manual compatibility pass

For each required profile:

1. open Loom Studios;
2. create/open a project;
3. verify canvas dimensions and header;
4. scroll the right rail from first to last control;
5. open the Palettes window;
6. move it to each side/corner;
7. pin/unpin it;
8. paint at 1x, 2x, and 4x;
9. open/close 3D Preview;
10. confirm Save / Save + Equip remain reachable;
11. resize/re-enter the screen and verify no window becomes inaccessible.

Record results in `TEST_MATRIX.md` / `PASS_LOG.md`.


## Zoomed canvas behavior

Zoom is explicitly supported at every required display/GUI-scale profile.

- 100% remains fit-to-view;
- up to 800% zoom is allowed;
- canvas content is clipped to its viewport;
- middle-mouse panning must remain reachable;
- zoom must not force the right tool rail off-screen;
- floating palette overlay must continue receiving topmost interaction even when it overlaps the zoomed canvas.
