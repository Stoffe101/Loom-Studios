# Loom Studios — Source Index

This file records external technical sources consulted for Loom Studios and the license/usage implications where relevant.

## Fabric documentation

### Fabric custom screens/widgets

Purpose:
- custom editor screens
- reusable interactive widgets
- GUI rendering/input/narration

Source:
- Fabric documentation: custom screens/widgets

Use:
- API/architecture reference

### Fabric networking

Purpose:
- custom payloads
- client/server protocol
- project synchronization

Source:
- Fabric documentation: networking

Use:
- API/architecture reference

### Fabric rendering concepts

Purpose:
- understand 1.21.x rendering-state direction
- avoid outdated raw-render assumptions

Source:
- Fabric documentation: rendering/basic concepts

Use:
- API/architecture reference

### Fabric automated testing

Purpose:
- GameTest/client tests
- CI strategy

Source:
- Fabric documentation: automatic testing

Use:
- test architecture reference

## Fabric Maven / API docs

Purpose:
- verify Loader/Fabric API/Loom artifacts
- inspect mapped Minecraft/Fabric APIs

Use:
- version/API reference

## Minecraft mappings

Purpose:
- inspect player rendering, skin texture, Elytra, and dynamic texture APIs

Preferred development direction:
- official Mojang mappings

Yarn may be consulted during research only when it provides easier public symbol browsing. Final source code should follow the mapping choice pinned by SPIKE-00.

## Sodium

Purpose:
- mandatory compatibility target

License:
- verify current upstream license before copying any code; Loom Studios should not require Sodium code reuse for its core implementation.

Use:
- runtime compatibility testing and public API/reference only if needed.

## Iris

Purpose:
- mandatory shader compatibility target

License:
- verify current upstream license before copying any implementation code.

Use:
- runtime compatibility testing and public API/reference only if needed.

## Existing cape/cosmetic mods

Purpose:
- observe modern Minecraft 1.21.x integration patterns and compatibility issues.

Rule:
- record the repository/license before copying or adapting implementation code;
- do not copy code whose license conflicts with Loom Studios proprietary licensing;
- behavioral observations and independent reimplementation are acceptable.

## Research update rule

When a new external source materially affects architecture:
1. add it here;
2. record what question it answered;
3. note license constraints;
4. update DECISIONS.md if the result changes architecture.
