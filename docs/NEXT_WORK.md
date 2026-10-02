# Loom Studios — Next Work

## Current gate

The active implementation slice contains:
- equipped-state multiplayer hardening;
- emissive-layer/runtime synchronization;
- removal of the old SPIKE shimmer;
- first Select tool UI;
- drag selection;
- persistent selection outline;
- nudge/move controls;
- Flip H / Flip V;
- expanded selection tests.

Before calling that slice DONE:
1. GitHub Actions must pass on the exact implementation head;
2. the latest selection/layer/emissive UI remains queued for local runtime verification.

## Runtime verification queue

The user is currently away from the development PC, so do not claim visual/runtime completion for these items yet.

### Select / transforms
- drag selection at 1x / 2x / 4x;
- persistent outline alignment;
- Move Left / Right / Up / Down;
- arrow-key nudge;
- Flip H / Flip V;
- correct behavior after zoom/pan;
- selection clears on face/resolution changes;
- tool-rail layout at all required GUI profiles.

### Layers / effects
- New / Duplicate / Delete;
- visibility;
- reorder;
- opacity;
- rename;
- Emissive;
- Normal / Add-Glow / Screen / Multiply / Overlay;
- Undo/Redo;
- 3D Preview compositing;
- emissive visual output with shaders OFF/ON.

### Swatches / color
- compact 1920x1080 GUI 3 footprint;
- no Saved palettes overlap;
- Edit / Done collapse;
- multiple palette groups;
- move + Pin;
- editable Hex/R/G/B/A;
- alpha slider;
- semi-transparent paint;
- alpha-aware palette import/export.

## CI-safe work that can continue without local runtime

Priority order:

1. finish/verify the current selection UI slice in CI;
2. define reusable transform primitives needed by imported image layers:
   - integer/floating position;
   - scale;
   - rotate;
   - mirror;
   - fit/center/aspect rules;
3. build pure image-processing primitives with automated tests:
   - resize;
   - crop;
   - mirror;
   - rotate;
   - brightness;
   - contrast;
   - saturation;
   - color reduction;
   - dithering;
4. design schema v2 and migration fixtures;
5. add first-class Gradient data model/compiler tests;
6. prepare non-destructive Image layer representation;
7. build Smart Import processing core around those primitives;
8. prepare Elytra semantic UV/editor model;
9. design animation authoring schema/timeline data model;
10. add reusable tooltip/icon-button primitives where they do not depend on visual judgment.

## Schema-v2 gate

Do **not** bolt persistent layer lock or new layer kinds directly onto schema v1.

Schema v2 should coordinate:
- stable layer-kind/type identifiers;
- persistent layer lock;
- Paint layer payload;
- Image layer payload;
- Gradient layer payload;
- effect/reference metadata where appropriate;
- transform data;
- future animation targeting;
- migration from schema v1;
- stable blend identifiers instead of ordinal-only encoding.

The schema design should be documented before implementation changes old project bytes.

## Crop clarification

Do not implement a destructive “crop the cape face to a smaller canvas.”

Minecraft cape UV face dimensions are fixed by the texture layout.

For the Cape Editor, selection-based crop behavior should mean a transform/selection workflow, not changing the semantic cape-face dimensions.

For Smart Import, Crop is an image-placement operation inside the fixed cape/Elytra target.

## Reference-image priority

Every new UI feature must continue tracking the five approved reference screens.

Near-term work should preserve the reference hierarchy without spending time on final decorative polish while no local visual verification is available.

See `REFERENCE_FIDELITY_ROADMAP.md`.

## Required UI profiles

- 1920x1080 GUI x2;
- 1920x1080 GUI x3;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.

## Documentation cleanup rule

When a task becomes implemented:
- remove it from active TODO lists;
- move verification-only work into the runtime verification queue;
- keep historical implementation detail in `PASS_LOG.md`;
- keep `CURRENT_STATE.md` focused on what is true now;
- explicitly mark superseded decisions in `DECISIONS.md` rather than leaving contradictory active guidance.
