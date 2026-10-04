# Animation 2.1 user testing

## Install

Use Minecraft1.21.11, Java21, Fabric Loader0.18.4 or a compatible newer loader and Fabric API0.141.1+1.21.11. Replace the older Loom Studios JAR in the profile mods folder; do not keep both versions. Back up the profile config/loom-studios folder before testing. New saves use schema5; older Loom versions cannot open them. Multiplayer peers need this release/protocol3.

## Focused test route

1. Open an existing project and save/reopen it; export/redeem a portable Loom Code. Try a large8× project with multiple layers and GIF frames. Oversized operations must show an error without crashing or losing edits.
2. Cape/Elytra → Animate → Advanced → Lanes & curve editor. Add two different parameter lanes, select keys across lanes with Ctrl/Shift, drag them, Undo, copy/paste at another playhead position. Paste beyond duration should reject cleanly. Choose Custom easing, drag both handles, Undo, preview playback, zoom and collapse a layer. Double-click3D preview for a full inspection.
3. Select an imported GIF Image layer → Assets → Frames → Convert GIF to Editable Animation. Draw and erase on separate frames, change ticks, duplicate/reorder/delete, Undo, Preview loop, save/reopen. Test Alpha Lock and locked layers. Repeat on an Elytra inside face and confirm the opposite wing is unchanged.
4. Assets → References → + Image. Change opacity/visibility/above/below, unlock and move/resize. Confirm the guide survives reopening locally but is absent from exported PNG/Loom Code and equipped cosmetics. Guides are intentionally local to this computer.
5. Assets → Onion skin. Compare previous/next/both and opacity, including first/last GIF frames. Ghosts must never appear on the equipped cape/Elytra.
6. Select/Wand some artwork → Assets → Stamps → Create Stamp from Selection. Rename, favorite, rotate, mirror, resize, use original/selected colors, paint and Undo. Reopen Minecraft and confirm the saved stamp/favorite persists. Try Star/Heart/Rune/Flame/Scale/Cloud patterns.
7. Repeat layout checks at1920×1080 and3440×1440, GUI2 andGUI3. Check dropdowns, bottom gutters, layer scrolling, timeline scrolling, curve handles and preview middle-drag pan/zoom.

## Remaining environment checks

Automated verification uses Linux/Java21/Mesa/Xvfb without optional mods. Windows performance, your RTX5070, Sodium/Iris/3D Skin Layers/shaders, resource reloads and concurrent dedicated-server transfers still need real-machine testing. Report the resolution/GUI scale, project resolution, layer/frame counts, modpack and reproduction steps with a screenshot/log for any issue.
