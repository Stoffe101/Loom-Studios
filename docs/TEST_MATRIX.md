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

Every rendering milestone must be tested in:

| Configuration | Required |
| --- | --- |
| Fabric only | Yes |
| Fabric + Sodium | Yes |
| Fabric + Sodium + Iris, shaders OFF | Yes |
| Fabric + Sodium + Iris, shaders ON | Yes |

Shader-sensitive effects should also be checked with a second materially different shader pack before release.

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
