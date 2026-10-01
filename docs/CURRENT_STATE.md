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

## Verified in CI

Exact SHA `a620de1a16334657b7e33f1606c800a673580214` produced the first fully green bootstrap build and uploaded the development JAR artifact.

Exact SHA `c7386f0ed3a46bfb51c7ae8614162deb03ee4fd4` built successfully with the SPIKE-01 static cape mixin, test texture, and client resource configuration.

Exact SHA `c321be1f4d0713d29826a4fd773b614c3b26a226` is the latest fully green implementation checkpoint. GitHub Actions run #7 passed the wrapper check, full build, and artifact upload using the official Fabric Gradle launcher scripts.

## Local verification still required

- IntelliJ/Gradle `runClient`
- `runServer`
- Loader 0.18.4 runtime startup
- visual LS test-cape behavior
- optional-mod detection and compatibility smoke tests

## SPIKE-01 implementation staged

A static development cape has now been implemented in source:

- local-player-only render-state patch;
- vanilla CapeLayer retained;
- body/Elytra/model type untouched;
- cached patched PlayerSkin;
- obvious 64x32 cyan/magenta LS development texture;
- client-only mixin configuration.

Compile/CI and local visual verification are still required before SPIKE-01 can be marked DONE.

## Next

Verify corrected CI and then launch the IntelliJ development client/server locally. In third person, the local player should show the LS test cape. After that, validate optional Sodium/Iris/3D Skin Layers compatibility.
