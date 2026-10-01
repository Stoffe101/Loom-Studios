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


## Implemented schema v1 foundation

The first real project model is now implemented.

Current binary v1 contents:
- magic header `LOOM`;
- schema version;
- project UUID;
- project name;
- runtime settings proven by the spikes;
- cape canvas;
- Elytra canvas;
- ordered paint layers.

Current paint-layer fields:
- stable UUID;
- name;
- visible;
- opacity;
- blend mode;
- emissive flag;
- ARGB pixel data.

Current limits are deliberately bounded:
- 256 KiB serialized project;
- 256 x 256 maximum canvas dimension;
- 64 layers per canvas;
- bounded project/layer names.

Only `NORMAL` blend is implemented in the compiler today. The enum is already serialized so later blend modes can be added through explicit schema/version handling.

The local library uses `<gameDir>/loom-studios/projects/<project UUID>.loom`.

The development cosmetics used by the runtime/network proof are now generated as real schema-v1 projects, encoded through this codec, SHA-256 addressed, transferred, decoded and compiled locally.


## Project metadata and load/migration policy

Schema v1 now stores:
- created timestamp (epoch milliseconds);
- modified timestamp (epoch milliseconds).

The editor session updates the modified timestamp when a real project edit is committed.

All project loading goes through `LoomProjectMigrations`.

Current migration table:
- schema 1 -> decode directly as current schema;
- every other schema -> reject explicitly.

There are no legacy public schemas yet. When schema 2 is introduced, schema-1 migration must be implemented here before old projects are considered supported.

## Persistence/session lifecycle

`ProjectSession` owns:
- current immutable project;
- undo/redo history;
- monotonically increasing in-memory revision;
- persisted source path;
- persisted content hash;
- dirty-state calculation.

A session starts dirty when created from an unsaved project.

Saving:
1. encodes the current project;
2. writes to a temporary file;
3. atomically replaces the target where the filesystem supports it;
4. records the saved content hash;
5. becomes clean.

Loading a saved .loom file creates a clean session.
