# Loom Studios — Test Matrix

## Toolchain

SPIKE-00 must record exact versions for:
- Minecraft
- Java
- Fabric Loader
- Fabric API
- Fabric Loom
- mappings
- Gradle

Required:
- clean build
- dev client start
- dedicated server start
- CI build on exact commit SHA

## Rendering compatibility matrix

Plain Fabric is the required runtime. The other configurations are optional compatibility targets we actively support and should smoke-test for relevant rendering changes.

| Configuration | Runtime requirement | Compatibility target |
| --- | --- | --- |
| Fabric only | Yes | Primary |
| Fabric + Sodium | No | Supported |
| Fabric + Sodium + Sodium Extra | No | Supported |
| Fabric + Sodium + Iris, shaders OFF | No | Supported |
| Fabric + Sodium + Iris, shaders ON | No | Supported |
| Fabric + 3D Skin Layers | No | Smoke test when player feature rendering changes |

Shader-sensitive effects should be checked with at least one representative shader pack before release.

## Cape tests

- standing
- walking/running
- crouching
- jumping/falling
- armor equipped
- official cape fallback when Loom cape disabled
- Elytra equipped
- remote player rendering
- texture swap while visible

## Elytra tests

- equipped/unequipped
- standing with Elytra
- gliding
- rockets/flight transitions where applicable
- remote players
- texture swap
- cape precedence

## Dynamic texture tests

- repeated updates
- no restart/reconnect required
- dirty-only upload
- old texture disposal
- cache hit/miss behavior
- disconnect/world change cleanup

## Editor preview tests

- rotate
- zoom
- cape view
- Elytra open/closed view
- armor toggle
- temporary unsaved state isolation
- GUI scale variants
- resize/windowed/fullscreen

## Networking tests

- two clients + dedicated server
- first join cache miss
- second join cache hit
- reconnect
- invalid hash
- unsupported schema
- oversized blob
- malformed compressed data
- spam/rate-limit behavior
- player leaving during transfer

## Import tests

- PNG with alpha
- PNG without alpha
- large source image
- odd aspect ratio
- very small image
- fit/stretch/crop/center
- mirror/rotate
- dithering
- color reduction
- brightness/contrast/saturation
- background/transparency handling
- palette-limited/posterize/monochrome/outline/pixel-art modes

## Project format tests

- save/load round-trip
- deterministic output where required
- migration from older schema fixtures
- missing/corrupt asset
- unknown optional field
- unsupported future major version
- portable code round-trip
- invalid portable code rejection

## Animation/effect tests

- loop
- speed
- pause/editor scrub
- moving gradient
- hue shift
- pulse
- sparkle
- emissive/additive layer
- large number of remote animated players
- shader compatibility

## Documentation gate

A test pass is not complete until its result is recorded in PASS_LOG.md and relevant current-state/test documentation.


## SPIKE-02 verified runtime checks

- live cape pixel changes without restart: PASS
- stable vanilla cape motion during updates: PASS
- full optional dev stack initial runtime: PASS
- Elytra fallback to cape texture observed: PASS / expected vanilla behavior

## SPIKE-03 verification additions

- dedicated Elytra texture takes precedence over cape fallback
- cape animation does not alter dedicated Elytra
- gliding pose remains vanilla
- edge transparency renders correctly
- inspect visual thickness from side/top/rear angles


## Elytra thickness proof checks

- 100% visually matches normal vanilla wing volume
- 75/50/25% progressively reduce depth
- 150% increases depth
- wing length/width remain unchanged
- gliding rotations remain vanilla
- returning to 100% fully restores vanilla depth
- non-Loom Elytras remain 100%
- no stale zScale leaks between rendered players


## SPIKE-04 preview checks

- P opens preview without pausing the integrated world
- real player skin/model renders
- cape preview uses current Loom cape texture
- Elytra preview uses current Loom Elytra texture
- C changes preview mode without mutating actual chest equipment
- drag rotates
- wheel zooms
- R resets camera
- dynamic cape updates are visible in GUI preview
- Elytra thickness customization is reflected in preview
- closing preview leaves gameplay state unchanged
