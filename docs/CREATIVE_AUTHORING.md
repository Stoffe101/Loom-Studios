# Creative authoring / Animation2.1

Status: IN PROGRESS. Official first checkpoint build passes164 tests; compact Screen assertions pass. Final visual acceptance and exact-source final build are pending.

## Usage

- Cape/Elytra → Animate → Advanced → Lanes & curve editor. Choose a parameter then Add lane. Multiple independent properties animate together. Click diamonds; Ctrl/Shift-click toggles selection. Select lane chooses all its keys. Ctrl+C/V or Copy keys/Paste preserve relative spacing from the playhead. Paste rejects beyond the duration. Drag selected keys together; collisions replace destinations as one Undo. Delete keys removes lanes when empty, while the primary track retains one key.
- Zoom changes the visible time span. Wheel scrolls lanes; Ctrl/Shift-wheel pans time. Layer rows collapse child tracks. Curve controls the outgoing transition following selected keys. Choose Custom and drag either handle; Linear/Ease In/Ease Out/Smooth/Step remain available. Curve handles stay bounded and ordered; no overshoot. 3D preview toggles the graph area for live cosmetic inspection. Save from the parent studio.
- Both editors → Assets → References. + Image imports a guide on the active face. Reference chooser, opacity, visibility, above/below, lock, movement and scale controls are local to the project. Guides start locked. Unlock before transform/delete. Reference sidecars remain on this computer and deliberately do not travel in exported .loom/Loom Code or equipped designs.
- Assets → Frames. Select an animated Image layer first; Convert GIF to Editable Animation bakes image processing to the active semantic face, undoable in one step. Pick a frame from the list, change duration1–1200 ticks, duplicate/delete/reorder, paint/erase directly, or Preview loop. Each painting drag is one Undo. Existing mask/clipping flags still affect equipped artwork; Alpha Lock preserves frame transparency.
- Onion skin: Off/Previous/Next/Both and opacity. Pink previous/cyan next; ghosts appear beneath current artwork only in the editor. GIF frame neighbors wrap around the loop. The standalone Onion tab can scrub effect time; it uses adjacent ticks for procedural tracks.
- Assets → Stamps. Select artwork with Select or Wand first. Create Stamp from Selection captures exact selected pixels; transparent/unselected areas stay empty. Name/save immediately, favorite, rotate90°, mirror, resize up to128px on the longest side, choose original/selected colors and paint. Favorites are sorted first. Six built-in patterns: Star, Heart, Rune, Flame, Scale, Cloud. Custom stamps persist across projects on this computer.

## Limits / compatibility

Project schema5 / protocol3; existing1–4 migrate. Older releases cannot read newly saved projects. Up to16 unique typed lanes per effect track,128 keys per lane,16,384 parameter keys total. Storage remains8 MiB serialized/64 MiB expanded/60 MiB artwork. Frames remain64 /4M combined embedded pixels; conversion/duplication can reject when that budget is exceeded. Rejection leaves project/history intact.

Local guides:8 per project,512px image dimensions,8 MiB sidecar. Stamp library:64 entries,128×128 source,8 MiB sidecar. Sidecars use atomic replacement, validate dimensions/counts/IDs before use and cannot alter project hashes. Selected-frame rendering and uploaded previews use unchanged-frame caching; reference work stays out of runtime cosmetic compilation.

## Verification

Core164 tests pass at384 MiB: lane evaluation/save/code, cubic inversion/preservation, clipboard bounds, duration/effect compatibility, frame editing/timing/face isolation, ghost source immutability, references/hash exclusion, stamp data/transforms/favorites and malformed metadata/assets. Official Build259 passes;32 new four-profile captures and real Screen assertions are running alongside the310 full suite.

Reference targets01/04/05: cyan/purple selection, inset panels, clear grouped controls. Focused tabs intentionally preserve the compact editing area instead of adding another long rail. Actual hardware FPS, optional mods, Windows/macOS and dedicated-server concurrency still require manual tests.
