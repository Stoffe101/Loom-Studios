# Library and authoring verification

Active milestone: 2026-10-03. Evidence is collected from the real Minecraft client, not UI mockups. Source checkpoint and final results will be recorded after successful exact-commit checks.

## Scope

- Home: Browse / View All; preview one design; double-click selects and opens its editor.
- Right click: Edit, Rename, Favorite, Duplicate and Delete to Trash. Trash can restore saved designs and discarded drafts without overwriting an existing UUID.
- Designs and Drafts are separate. Thirty-second dirty snapshots and navigation/exit checkpoints never silently equip or overwrite the explicitly saved project. Recovery returns a dirty editing session; an explicit save clears the draft.
- Search, latest/name sort, favorites and bounded card pagination. Mouse-wheel paging affects the card collection only.
- Six independent layered templates: Blank, Gradient, Nature, Space, Fantasy and Emblem.
- Cape/Elytra semantic-face clipboard, flip, rotate, selection movement, line, rectangle and circle tools. Paint lock and linked-wing mirroring remain enforced.
- Shared animation studio: layer/effect selection, opacity/phase/strength controls, duration/playback, presets, keyframe add/remove/drag and single-step gesture undo.
- Preview: Standing, Open Wings and Gliding, camera presets, facing, zoom, orbit and middle-button pan. Pose/facing survive editor rebuilds.
- Smart Import: artwork drag to move, corner handles to scale, violet top handle to rotate; existing numeric controls remain available.
- Editor settings and keyboard help are local preferences and do not change portable designs, schema-v3 or the network protocol.

## Required profiles

| Physical display | GUI scale | New capture views |
|---|---|---|
| 1920×1080 | 2 | Library, templates, settings, both animation studios, open/gliding previews |
| 1920×1080 | 3 | Same seven views |
| 3440×1440 | 2 | Same seven views |
| 3440×1440 | 3 | Same seven views |
| 1904×960 windowed (635×320 logical) | 3 | Context menu, Drafts, Trash, Rename |

The 68 earlier editor/import/share/layout/preview captures are also regenerated. The harness checks all visible native widget bounds and pairwise nonoverlap before every screenshot. A floating context menu intentionally overlays the selected card; it is clamped inside the usable screen.

## Interactive test handoff

1. Open Browse, single-click and double-click different designs. Right-click to rename, duplicate, favorite and delete; restore from Trash. Search and sort a collection larger than one page.
2. Change a saved design, wait for autosave or leave the editor; reopen Drafts and recover it. Confirm the original saved design stays unchanged until Save.
3. Create each template and edit its layers. Copy/paste/rotate selections across both editors; check linked wings, locked layers and undo/redo.
4. Add effects on both channels, drag keyframes, change values and play the preview. Undo a drag once. Adjust an imported image using all three handle types and compare the candidate to the applied result.
5. Repeat at the four display/GUI profiles. Test hardware FPS and optional Sodium/Iris/Sodium Extra/3D Skin Layers, shaders off/on, and a second multiplayer client. These external scenarios are not claimed by software-Mesa CI.
