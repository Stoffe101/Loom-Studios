# Loom Studios — Next Work

## Immediate test: first real Cape Editor

After CI is green:
1. git pull;
2. runClient;
3. enter a world;
4. press **L** to open Loom Studios;
5. choose **Create New Cape**;
6. paint on the 64x32 canvas;
7. switch Pencil/Eraser;
8. test Undo/Redo;
9. open 3D Preview and confirm unsaved paint appears there;
10. close preview and return to editor;
11. Save;
12. Save + Equip;
13. close to world and confirm the painted cape is now worn.

Important expected behavior:
- painting should update the editor preview but **not** the world cape until Save + Equip;
- saved project should appear under Recent Projects the next time the home screen opens.

## Next Phase-2 editor work

After this slice is locally green:
- fill tool;
- eyedropper;
- line and rectangle;
- proper color picker + hex/RGB controls;
- canvas zoom/pan;
- symmetry;
- layer selection panel;
- keyboard shortcuts (Ctrl+Z / Ctrl+Y / Ctrl+S);
- better stroke grouping so a drag is one undo action;
- home screen thumbnail rendering;
- start Cape Loom block registration/interactions.

## Deferred polish

Player head-lock/Facing/Orbit controls remain bundled for a later preview UI refinement pass.

Reference-image fidelity remains Phase 8; current screens are functional architecture/UI MVPs.
