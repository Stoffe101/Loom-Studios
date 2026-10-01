# Loom Studios — Current State

**Checkpoint:** 2026-10-01 implementation bootstrap

## Overall

**Status: SPIKE-00 IN PROGRESS**

The real Fabric project skeleton is now present.

## Baseline

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 development baseline
- Fabric API 0.141.1+1.21.11 initial pin
- Fabric Loom 1.18-SNAPSHOT
- Gradle 9.7.1 wrapper
- Mojang mappings
- split common/client source sets
- generated IntelliJ/Loom client and server runs
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

## SPIKE-00 still needs verification

- exact bootstrap commit GitHub Actions result
- local runClient
- local runServer
- Loader 0.18.4 runtime
- optional-mod runtime detection

## Next

Finish SPIKE-00 verification, then implement SPIKE-01 static cape rendering.
