# Loom Studios — Implementation Roadmap

## Phase 0 — Foundation spikes

**Status:** primary technical paths proven; final SPIKE-06 emissive/shader smoke test still pending.

SPIKE-00 through SPIKE-06:
- toolchain
- static cape
- dynamic texture
- Elytra
- GUI preview
- multiplayer sync
- animated/emissive compatibility

**Gate:** no full editor build before foundation is green or explicitly redesigned.

## Phase 1 — Project core

**Status:** IN PROGRESS. Schema-v1 model/codec, basic layer compiler, local-library plumbing and undo/redo foundation are implemented and CI-green.

- versioned project model
- canvas model
- layers
- serialization
- local project library
- texture compiler
- runtime cache
- undo/redo command model

## Phase 2 — Cape editor MVP

- Cape Loom block interaction
- home/start screen
- cape canvas
- pencil
- eraser
- fill
- eyedropper
- line/rectangle
- move/crop/flip
- color picker/palette
- symmetry
- live 3D preview
- save/equip

## Phase 3 — Layer system

- add/delete/duplicate/reorder
- visibility/lock
- opacity
- blend modes
- non-destructive base/paint/image/gradient layers
- thumbnails

## Phase 4 — Smart Import

- PNG import
- fit/stretch/crop/center
- aspect lock
- mirror/rotate
- brightness/contrast/saturation
- color reduction
- dithering
- transparency/background handling
- Direct
- Pixel-art
- Outline
- Monochrome
- Palette Limited
- Posterize
- imported image transforms

## Phase 5 — Elytra editor

- separate Elytra canvas
- linked/mirrored wings
- independent wings
- cape-to-Elytra starting conversion
- open/closed/gliding preview

## Phase 6 — Animation/effects

- timeline
- tracks/keyframes
- pulse
- scroll
- hue shift
- moving gradient
- sparkle
- emissive/additive effects
- performance controls

## Phase 7 — Multiplayer library/sharing

- server validation/storage
- content-hash cache
- remote-player sync
- short Loom Codes
- portable codes
- private clickable chat result
- copy/import/preview/favorite flow
- permissions/visibility

## Phase 8 — Reference-image fidelity

Rebuild/polish each approved reference screen:
- home
- cape editor
- Smart Import
- Elytra animation editor
- Loom Codes/share

Validate:
- 1080p
- 1440p
- ultrawide
- multiple GUI scales
- keyboard/mouse navigation
- narration/accessibility where supported

## Phase 9 — Release hardening

- CI matrix
- clean dedicated server
- Sodium/Iris/shader matrix
- project migrations
- malformed input tests
- performance profiling
- packaging
- release documentation
