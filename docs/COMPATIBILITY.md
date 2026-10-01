# Loom Studios — Compatibility Requirements

**Status:** Canonical compatibility requirements  
**Updated:** 2026-10-01  
**Target game:** Minecraft Java Edition 1.21.11  
**Target loader:** Fabric Loader 0.18.4

## Compatibility is a release requirement

Loom Studios must be designed so cape, Elytra, editor-preview, animation, and emissive rendering coexist with common Fabric rendering mods rather than assuming a pure-vanilla renderer.

A rendering feature is not considered DONE until its applicable compatibility checks pass.

## Mandatory targets

### Sodium

Sodium on Minecraft 1.21.11 is a first-class supported configuration.

Rules:

- prefer vanilla/Fabric-supported rendering paths;
- avoid undocumented raw OpenGL state;
- do not require Sodium APIs for core functionality;
- isolate any optional Sodium-specific integration;
- test cape, Elytra, preview, dynamic texture updates, transparency, and emissive effects with Sodium installed.

### Iris

Iris on Minecraft 1.21.11 is a first-class supported configuration.

Rules:

- core Loom Studios visuals must work with shaders disabled and enabled;
- base cape/Elytra surfaces should remain on Minecraft-compatible entity rendering paths wherever practical;
- custom emissive/additive passes must be tested under Iris;
- no critical feature may depend on one specific shader pack;
- incompatible optional effects must degrade gracefully rather than crash or hide the base cosmetic.

## Mandatory render matrix

Every rendering milestone must be checked in at least:

1. Fabric only
2. Fabric + Sodium
3. Fabric + Sodium + Iris, shaders OFF
4. Fabric + Sodium + Iris, shaders ON with a conservative/common shader pack

Shader-sensitive effects should also be tested with at least one materially different shader pack before being called fully compatible.

## Features requiring matrix validation

- static cape
- animated cape
- static Elytra
- animated Elytra
- live editor 3D preview
- dynamic texture updates
- transparency
- additive/emissive passes
- armor interaction
- cape/Elytra precedence
- Elytra flight pose/movement
- remote-player multiplayer rendering

## Performance requirements

- no per-frame image decoding;
- no per-frame project deserialization;
- no animation frame streaming over the network;
- update dynamic textures only when compiled output is dirty;
- cache static compiled textures;
- evaluate synchronized animation definitions locally;
- cache remote cosmetics by content hash;
- expensive optional effects need quality/disable controls.

## Failure behavior

If an optional visual pass is incompatible with a shader setup:

1. preserve the base cape/Elytra where possible;
2. disable only the incompatible pass;
3. avoid hard crashes;
4. log a concise diagnostic;
5. show a user-facing warning only when actionable.

Sodium/Iris regressions are real regressions and must be investigated during the same implementation phase.
