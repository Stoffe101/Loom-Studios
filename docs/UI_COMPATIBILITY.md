# Loom Studios — UI Compatibility Contract

> **Current-resolution note (2026-10-08):** The runtime supports editable **1×/2×/4×/6×/8×** projects, subject to bounded storage, cache and editor limits. This contract applies to all five scales; some older checkpoint examples below mention only 4×, and those are historical. Required GUI viewport checks remain the four profiles listed below; new M1 work must additionally validate actual Image/Asset Library placement and Animation lanes on populated designs, not only empty screens.


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

## Verified workshop chrome budget

Actions #182 verifies all five screens at the four required profiles. Shared brand headers reserve 36 logical pixels in compact mode and 56 normally; decorative timber/steel/cloth/lantern artwork stays inside those bounds. Normal editor tool rails reserve 106 pixels for labels; compact rails use 30-pixel icons. The 20-pixel footer owns the centered parchment plaque and independently clipped left/right status. Preview gesture hints shorten below 190 pixels and clip to their assigned width. These budgets are included in canvas, preview and inspector layout calculations.

## Editor requirements

At every required profile:

- the main editing canvas must remain usable;
- no required action may be clipped beyond the screen;
- inspector controls must fit their assigned bounds through contextual tabs/pages;
- only genuine collections (layers, swatches, tracks) scroll;
- the scrollbar must remain mouse-operable;
- tool rows must not overlap;
- disabled labels/buttons must remain legible;
- floating windows must be clamped so their title bar remains reachable;
- movable windows may overlap the canvas by design, but must never become permanently stranded off-screen;
- pinned windows must preserve their position while editing;
- screen rebuilds/resolution changes must not silently reset editor state;
- 3D Preview, Save, Save + Equip, Undo/Redo, and Back must remain reachable.

## High-resolution canvas performance

1×, 2×, 4×, 6× and 8× project resolution must not change the editor layout contract.

The canvas renderer uses a revision-cached DynamicTexture so project resolution should primarily affect edit/upload cost rather than per-frame GUI draw-call count.

A continuous brush stroke is one compound history entry.

Performance regressions should be tested especially at:

- 1920×1080 / GUI 3, because it is the tightest required logical viewport;
- 8× / 512×256 project resolution, especially with imported multi-frame GIFs and numerous layers; 6× is also required.

## Bounded inspector

`LoomWorkspaceLayout` assigns non-overlapping canvas, tool context, timeline, preview, tabs and inspector rectangles above the footer. `LoomInspectorLayout` allocates rows and fails fast on overflow. Extend controls with a named page or a compact row, never with an unbounded stack. Properties and Gradient controls use pages; Animation separates Keys and Playback. Layer/swatch/track collections retain their own scrolling viewport.

## Floating editor windows

The Palettes window is the first floating tool window and establishes the expected behavior for future Layers, Animation, Import, Effects, and similar windows:

- movable by title bar;
- pinnable;
- clamped to screen bounds;
- independent from collection scrollbars;
- retains state while the current editor screen remains alive;
- may be hidden without losing palette/library data.

Future floating windows should reuse the same interaction model unless a newer ADR supersedes it.

## Manual compatibility pass

For each required profile:

1. open Loom Studios;
2. create/open a project;
3. verify canvas dimensions and header;
4. visit each inspector tab/page and scroll long collections;
5. open the Palettes window;
6. move it to each side/corner;
7. pin/unpin it;
8. paint at 1×, 2×, 4×, 6× and 8× (including bounded heavy multi-layer/GIF cases);
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
- zoom must not force the inspector off-screen;
- floating palette overlay must continue receiving topmost interaction even when it overlaps the zoomed canvas.


## Compact floating-tool rule

At logical widths/heights equivalent to 1920x1080 GUI scale 3, floating tools must not open as near-full-screen modal slabs.

Swatches specifically:
- uses a reduced compact width/height;
- defaults palette-management controls to collapsed;
- preserves a large scrolling swatch viewport;
- must not overlap its own labels/buttons.

Future floating tools should use the same principle: the frequently-used content stays visible, while infrequent management controls collapse behind an explicit Edit/Manage affordance.


## Reference UI architecture rules

The 2026-10-02 local test invalidated the previous "show most controls in a scrollable column" approach.

The editor shell now follows these rules:

1. **Progressive disclosure over button walls.**
   - persistent tools use compact icon rails;
   - layer/color/animation/gradient controls live in contextual inspector tabs;
   - controls irrelevant to the current task stay hidden.

2. **Primary editing should not require a giant vertical settings scroll.**
   - normal Cape authoring must fit tool rail + canvas + one inspector context;
   - Elytra keeps canvas + timeline + preview visible together;
   - Smart Import separates Placement and Processing rather than stacking both.

3. **640x360 effective GUI size is a first-class compact layout.**
   This approximates 1920x1080 at GUI scale 3 and is release-critical.

4. **Export is a first-class destination.**
   Cape/Elytra editors expose Share / Export directly.
   The sharing screen separates Export and Import into explicit workspaces.

5. **Disabled future features should not occupy primary navigation.**
   Settings was removed from Home until it is a real destination.

6. **Reference fidelity is structural before decorative.**
   Match hierarchy, grouping, density, icon usage, preview placement and canvas dominance before adding heavier workshop ornament.

7. **Scroll only the content that genuinely scales.**
   Layer lists, grouped swatches and long track collections may scroll.
   The whole primary editor control surface should not.

### Compact-mode pass criteria

At <=700x420 effective GUI size:
- icon rails replace verbose tool buttons;
- action-card subtitles may collapse;
- only essential project cards/templates are shown;
- timeline secondary controls collapse before track rows overlap;
- inspector controls use compact rows/tabs;
- text must not render across neighboring panels;
- all primary actions remain reachable without scrolling the full screen.
