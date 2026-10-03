# Loom Studios — Animation

**Status:** Schema-v3 model/runtime/timeline MVP implemented; local visual-runtime verification pending.

## Purpose

Animation is editable project intent, not a stream of rendered frames. The authored timeline belongs in the `.loom` project, is synchronized as part of the normal content-addressed project blob, and is evaluated locally by each client.

## Schema v3

Schema v3 extends the schema-v2 typed-layer project with `LoomAnimation`.

Project animation:
- duration: 20..7200 ticks;
- timeline loop;
- playback speed: 0.25x..4.0x;
- up to 64 tracks.

Track:
- stable UUID;
- target layer UUID;
- channel: Cape or Elytra;
- effect;
- enabled;
- speed: 0.1x..8.0x;
- loop;
- 1..128 ordered keyframes.

Keyframe:
- timeline tick;
- scalar value in the bounded animation value range.

Older projects migrate explicitly: v1 -> v2 -> v3 and v2 -> v3. Migrated projects receive an 80-tick looping 1.0x empty timeline.

## Reference integrity

Tracks reference real layer UUIDs. `LoomProject` rejects a track whose target does not exist in its declared Cape/Elytra canvas. When a layer is deleted, `ProjectEdits` prunes its animation tracks before constructing the replacement canvas.

## Current effects

- Pulse: evaluated scalar multiplies layer alpha.
- Scroll: moves the layer raster horizontally with wrapping.
- Hue Shift: rotates hue continuously by the evaluated scalar value.
- Moving Gradient: moves the layer raster vertically with wrapping in the current deterministic MVP.
- Sparkle: deterministic pixel/tick hash with evaluated scalar density.
- Emissive Glow: allows the track to participate in the emissive cape pass and scales emissive alpha.

The old runtime hue-cycle flag remains supported only for legacy compatibility.

## Elytra timeline authoring

Reference target: `docs/references/ui/Loom_Studios_04_Elytra_Animation_Editor.webp`.

The current work-mode hierarchy keeps the unfolded Elytra canvas dominant, docks a compact timeline underneath, and keeps 3D preview/layer authoring on the right.

Current timeline controls:
- Play / Pause;
- scrub by clicking the timeline;
- Loop / Once;
- duration +/-;
- project playback speed +/-;
- Add Track;
- select/enable/delete track;
- cycle effect;
- Add/Remove Keyframe;
- scalar Value +/- at the scrubbed tick;
- cycle track speed.

The timeline renders one row per Elytra animation track, target layer + effect labels, keyframe markers, current-time cursor and scrolling for larger track counts.

## Fixed-tick preview

Timeline scrubbing uses a scoped preview-only runtime bundle. Dirty project state and fixed timeline ticks are allowed, but equipped/network state is never replaced or published. The preview hash remains stable while the scrub position changes, so texture contents update in place instead of creating a new texture identity for every frame.

## Next animation work

- Hardware frame-time measurements while scrubbing and playing dense designs.
- Effect-specific controls for gradient direction, shimmer density and intensity.
- Optional onion-skin/reference frames and a visual preset picker.
- Cross-platform and Sodium/Iris/shader acceptance with real equipped designs.

## Workspace ownership

Elytra uses a compact adaptive timeline dock. Empty timelines show the next action and reserve only 66 logical pixels. Track collections scroll; effect and keyframe authoring lives on Animation -> Keys, while track loop/speed and project playback speed live on Animation -> Playback. Layer opacity/blend/lock/thickness live on Props. Selecting a track opens Animation. Violet accents identify animation without competing with the cyan canvas selection.

## Guided preset workflow

Open the animation studio, choose the layer, choose a preset and rate, then click **Apply & play**. **Save** keeps the changes. Each application is one undoable edit and replaces the selected track; use the timeline's Add Track to compose another effect. Rates are cycles per second at normal global playback speed. Long timelines may require a slower rate to remain within the existing track limits.

Draw stars/highlights on their own layer, keeping the base design solid. Blinking stars fades that detail layer on/off; Shimmer twinkles its existing pixels; Glow creates an emissive highlight track. Color cycle, horizontal scroll and vertical wave work on the selected layer. **Advanced** exposes effect, key value, add/remove key and track speed. Drag timeline keys to change their timing. Presets preserve the existing project format.

Elytra Glow follow-up: a separate cached emissive wing mask and fullbright ElytraModel pass now mirror cape highlights. Masks contain authored pixels only (no alpha-guide checkerboard); update at changed timeline ticks and release with their runtime bundle. Verification includes a wing-only glow capture plus mask presence/change assertions.
