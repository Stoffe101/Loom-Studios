# Loom Studios — Compatibility

**Status:** Canonical compatibility policy  
**Target game:** Minecraft Java Edition 1.21.11  
**Baseline loader:** Fabric Loader 0.18.4

## Core rule

Sodium, Sodium Extra, Iris, shader packs, and 3D Skin Layers are **optional compatibility targets**, not requirements or dependencies of Loom Studios.

Loom Studios must launch and provide its core functionality on normal Fabric without any of them.

## Optional supported integrations

We want Loom Studios to coexist cleanly with:

- Sodium
- Sodium Extra
- Iris
- common Iris shader packs
- 3D Skin Layers

The development runtime loads JARs placed in `dev-mods/` for `runClient` through Loom's `modLocalRuntime`. They are not bundled.

## Suggested rendering smoke-test matrix

1. Fabric only
2. Fabric + Sodium
3. Fabric + Sodium + Sodium Extra
4. Fabric + Sodium + Iris, shaders OFF
5. Fabric + Sodium + Iris, shaders ON
6. Fabric + 3D Skin Layers when player-model feature rendering changed

Compatibility is a supported bonus, not an installation requirement.

## 3D Skin Layers

3D Skin Layers primarily changes the player's outer skin-layer rendering. Loom Studios does not currently depend on its API.

Because both mods touch player rendering, keep it as a smoke-test target for cape/Elytra offsets, visibility, preview rendering, and armor/outer-layer interaction. If early spikes show no meaningful interaction, no special integration is needed.

## Performance / failure rules

- no per-frame image decoding or project deserialization;
- no animation-frame network streaming;
- dirty-only texture uploads;
- cache compiled output;
- shader-sensitive optional effects fail gracefully;
- preserve the base cape/Elytra if an optional effect must be disabled.


## Embedded-library handling in the dev runtime

Some third-party Fabric mods bundle libraries under `META-INF/jars/`. Normal distribution launchers/Fabric installations can load these as nested JARs, but Loom's remapped local development path may not expose them correctly after remapping a raw local JAR.

Loom Studios' Gradle setup therefore:
- scans only local `dev-mods/*.jar` files;
- extracts embedded `META-INF/jars/*.jar` entries to `build/dev-mods-nested/`;
- adds the extracted libraries separately to `modLocalRuntime`;
- never publishes or commits those third-party binaries.

This is currently required for 3D Skin Layers 1.11.3's embedded TRansition/TRender libraries.
