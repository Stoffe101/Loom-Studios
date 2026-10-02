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


## Implemented schema v2

Schema v2 is now the current project format.

Current binary contents:
- magic header `LOOM`;
- schema version;
- project UUID;
- project name;
- created/modified timestamps;
- runtime settings;
- cape canvas;
- Elytra canvas;
- ordered typed layers.

### Common layer fields

All schema-v2 layers store:
- stable UUID;
- name;
- visible;
- opacity;
- stable blend-mode string id;
- emissive flag;
- persistent lock flag;
- stable layer-kind string id.

Current layer kinds:
- `paint`;
- `image`;
- `gradient`.

### Paint layer

Stores:
- exact ARGB pixel payload matching the current canvas dimensions.

### Image layer

Stores:
- bounded embedded ARGB source image;
- normalized source crop;
- normalized destination transform;
- normalized semantic clip;
- processing mode id;
- Brightness;
- Contrast;
- Saturation;
- optional color limit;
- Dither flag;
- Posterize levels;
- optional palette.

Embedded source constraints:
- maximum 256 px per dimension in the persisted project;
- the temporary Smart Import processing pipeline may accept larger images under its separate safety limits;
- import downscales the embedded source with nearest-neighbor sampling when required;
- the final encoded project must still fit the 1 MiB project limit.

### Gradient layer

Stores:
- stable Linear/Radial type id;
- 2..16 ordered color stops;
- normalized transform;
- normalized semantic clip;
- repeat flag;
- dither flag.

### Blend compatibility

Schema v1 stored blend enum ordinals.

Those ordinals remain frozen compatibility data for v1 decoding:
- 0 Normal;
- 1 Add / Glow;
- 2 Screen;
- 3 Multiply;
- 4 Overlay.

Schema v2 stores stable blend string ids and is therefore no longer dependent on enum ordering.

### Current bounded limits

- 1 MiB serialized project;
- 256 x 256 maximum Loom canvas dimension;
- 64 layers per canvas;
- 256 px maximum embedded Image-layer source dimension;
- up to 256 Image-layer palette colors;
- up to 16 Gradient stops;
- bounded project/layer/string fields.

### Migration table

All loading still passes through `LoomProjectMigrations`.

Current migration table:
- schema 1 -> decode with the original paint-only layout, then explicitly migrate to schema 2;
- schema 2 -> decode directly;
- every other schema -> reject explicitly.

A schema-v1 migration preserves:
- project identity/name;
- timestamps;
- runtime settings;
- cape/Elytra dimensions;
- ordered Paint layers;
- visibility/opacity/blend/emissive;
- ARGB pixels.

Migrated v1 Paint layers begin unlocked because lock metadata did not exist in v1.

### Emissive compatibility

Per-layer emissive flags remain the render-time authority.

The runtime project-level emissive flag is retained as a compatibility mirror while the existing runtime model uses it. Editor operations synchronize that mirror with whether any cape layer is emissive.

### Local persistence

The local library remains:

`<gameDir>/loom-studios/projects/<project UUID>.loom`

Projects are still deterministically encoded and SHA-256 addressed for runtime/network cache identity.


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
