# Loom Studios — Project Format

## Goals

The .loom format must preserve editable intent rather than only the flattened final texture.

It must support:
- cape and Elytra canvases
- layers
- gradients
- imported images
- transforms
- blend modes
- opacity
- animation tracks/keyframes
- emissive/effect metadata
- thumbnails/metadata
- future migrations

## Container

Working design:
- versioned Loom project container
- structured manifest
- embedded or content-addressed binary assets
- deterministic serialization where practical
- optional compression
- integrity hash

Exact container encoding is finalized after implementation spikes.

## Conceptual manifest

Project:
- schemaVersion
- projectId
- name
- author metadata where appropriate
- created/modified timestamps
- canvas definitions
- layers
- assets
- animations
- export/runtime settings

Layer:
- stable ID
- name
- type
- visible
- opacity
- blend mode
- transform
- effect/emissive flags
- source parameters

Image layer:
- asset reference
- crop
- position
- scale
- rotation
- flip X/Y
- tint
- import-processing metadata where preservation is useful

Gradient layer:
- gradient type
- stops
- coordinates/angle
- repeat mode
- dithering
- opacity/blend

Animation:
- target layer/property
- track type
- keyframes or procedural parameters
- duration
- loop mode
- speed/phase

## Runtime output

Runtime textures are compiled/cache products. They are not the editable source of truth.

## Loom Codes

### Short code

Human-friendly server/service-backed identifier such as:

LS-7F4A-K92Q-XP31

It resolves to stored project/share data and is convenient for chat.

### Portable code

Self-contained compressed project representation with a version prefix such as LSP1.

Portable codes require strict maximum sizes and decoding limits.

## Migration

Every saved project declares a schema version. Loading an old project runs explicit migrations rather than silently interpreting old fields as new semantics.
