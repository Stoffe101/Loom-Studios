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


## Latest local test feedback

The first Windows `runClient` attempt started the Java client process but exited with status `0xFFFFFFFF`. This is a runtime/client failure rather than a compile failure. The actual cause must be read from `run/logs/latest.log` or the newest crash report before changing rendering code.

The `listDevMods` helper exposed a separate Gradle configuration-cache incompatibility. Configuration cache is now intentionally disabled for the IntelliJ/Loom development workflow.


## Optional-stack crash diagnosis

The first local full-stack runtime did not fail in Loom Studios rendering code. 3D Skin Layers crashed during its client entrypoint because its embedded TRansition library was not present as a remapped top-level development mod.

The supplied SkinLayers3D JAR includes both TRansition and TRender in `META-INF/jars/`. The Gradle development harness now flattens embedded dev libraries into `build/dev-mods-nested/` and feeds them through `modLocalRuntime` so Loom can remap them for the named development namespace.

Local retest is required before marking the optional stack compatible.


## SPIKE-01 runtime verification

**Result: PASS**

Local Windows IntelliJ/Gradle test confirmed:

- Minecraft 1.21.11 development client launches successfully;
- Loom Studios static test cape renders on the local player;
- vanilla cape geometry and movement are preserved;
- the cape is visibly correct in third person;
- Sodium, Sodium Extra, Iris, and 3D Skin Layers were present in the development runtime;
- no visible conflict was observed during the initial test.

SPIKE-01 is now considered DONE for the initial runtime proof.

Next active milestone: **SPIKE-02 — dynamic runtime cape texture**.


## SPIKE-02 implementation

**Status: COMPILE VERIFICATION PENDING**

The static resource cape has been replaced in source by an in-memory dynamic cape proof:

- one 64x32 NativeImage;
- one registered DynamicTexture;
- stable texture Identifier;
- vanilla cape renderer still owns geometry/motion;
- procedural LS/checker design changes every 40 client ticks;
- GPU upload occurs only when the image changes;
- resource release is registered for client shutdown;
- no network traffic is involved.

Expected local behavior after pulling: the cape visibly changes palette/highlight roughly every two seconds without reconnecting or restarting.


## SPIKE-02 runtime verification

**Result: PASS**

User local runtime evidence confirms:
- dynamic cape colors change while the game remains running;
- no reconnect/resource reload is required;
- vanilla cape movement remains intact;
- the full optional development stack remains stable.

An Elytra was also equipped during the test. Minecraft rendered the animated cape texture on the Elytra, revealing the vanilla fallback rule: when the player's dedicated Elytra texture is absent, WingsLayer uses the player's cape texture if the cape is visible.

This discovery directly validates the need for separate cape/Elytra channels in Loom Studios.

## SPIKE-03 implementation

**Status: IMPLEMENTED / CI + LOCAL RUNTIME VERIFICATION PENDING**

The render-state patch now supplies a distinct Elytra texture in addition to the dynamic cape texture.

The SPIKE-03 test Elytra:
- uses its own runtime DynamicTexture;
- does not inherit cape animation;
- uses the vanilla Elytra model/animation;
- makes the edge-face UV strips transparent while leaving the major front/back wing faces opaque, as a low-risk experiment to reduce the boxy appearance without replacing vanilla geometry.


## Elytra thickness direction

The transparent edge-face experiment made the Elytra visually paper-thin. That confirms texture alpha is the wrong control for user-adjustable thickness.

Product direction:
- default Elytra geometry/appearance uses vanilla thickness;
- texture controls artwork only;
- an optional Loom Studios geometry setting controls visual wing thickness independently;
- the setting is cosmetic/render-only and does not affect hitboxes or gameplay.

Implementation target:
- 100% = vanilla Elytra thickness;
- thinner/thicker values scale the Elytra model on its local depth axis;
- preserve vanilla animation/pose logic;
- keep the feature optional and fall back to vanilla geometry if compatibility requires it.


## Elytra geometry-thickness proof

**Status: IMPLEMENTED / VERIFICATION PENDING**

Default dedicated Elytra UVs are opaque again so 100% thickness retains vanilla volume.

A client-only ElytraModel hook now applies Loom-specific Z-depth scaling while retaining vanilla wing width, height, pivot, rotation, and gliding animation.

Temporary development key:
- V cycles 100%, 75%, 50%, 25%, 150% thickness.

Non-Loom Elytras are reset to 100% every setup call to avoid state leaking through reused model instances.
