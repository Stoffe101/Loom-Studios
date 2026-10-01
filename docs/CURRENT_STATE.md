# Loom Studios — Current State

**Checkpoint:** 2026-10-01 implementation bootstrap

## Overall

**Status: SPIKE-00 IN PROGRESS**

The real Fabric project skeleton is present and CI verification is active.

## Baseline

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 development baseline
- Fabric API 0.141.1+1.21.11 initial pin
- Fabric Loom 1.17.21
- Gradle 9.6.1 distribution through the wrapper
- Mojang mappings
- split common/client source sets
- IntelliJ/Loom client and server runs
- optional local dev mods through dev-mods/
- proprietary license
- documentation hard gate

## Optional compatibility targets

Not dependencies:

- Sodium
- Sodium Extra
- Iris
- shader packs
- 3D Skin Layers

## Implemented

- common Fabric entrypoint
- client Fabric entrypoint
- optional-mod detection/logging
- Gradle/IntelliJ run setup
- CI build workflow

## CI history

Bootstrap SHA `400d82c798db6a62a750ba2236481b22897c612e` reached Gradle successfully but failed during project configuration because Loom 1.18.2 now requires a Java 25 Gradle runtime.

Decision: keep Loom Studios development on Java 21 and pin Loom to the latest 1.17 line (`1.17.21`) with Gradle 9.6.1. Loom 1.17 provides the property-based run configuration API and `preferGradleTask` without forcing Java 25.

## SPIKE-00 still needs verification

- green CI on the corrected toolchain
- local runClient
- local runServer
- Loader 0.18.4 runtime
- optional-mod runtime detection

## Next

Verify corrected CI, then local IntelliJ client/server launch. After SPIKE-00 is green, begin static cape rendering.
