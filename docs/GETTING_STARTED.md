# Loom Studios — Getting Started

**Status:** Initial implementation startup plan  
**Updated:** 2026-10-01

## Goal

Begin implementation without committing prematurely to fragile rendering hooks. The first phase proves the toolchain, player cosmetic rendering path, dynamic textures, Elytra behavior, editor preview, networking, and shader compatibility before the full editor is built.

## Local prerequisites

- Git
- Java 21
- an IDE such as IntelliJ IDEA
- enough disk space for Gradle/Minecraft dependencies

Gradle itself should not need a machine-wide installation. The repository will use the Gradle Wrapper.

## SPIKE-00 — Project bootstrap

Create a clean Fabric project targeting:

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 compatibility baseline
- compatible 1.21.11 Fabric API
- Mojang official mappings
- compatible Fabric Loom generation

Prove:

- ./gradlew build
- dev client launch
- dedicated server launch
- no accidental client-only class references on the server

## SPIKE-01 — Static cape

Render one Loom Studios cape on the local player while retaining vanilla cape movement and visibility behavior.

Validate in:

- Fabric only
- Sodium
- Sodium + Iris with shaders off
- Sodium + Iris with shaders on

## SPIKE-02 — Dynamic texture

Generate/update the cape texture at runtime without restarting the client. Prove safe replacement, caching, dirty-state handling, and cleanup.

## SPIKE-03 — Elytra

Override/use a custom Elytra texture while preserving vanilla flight behavior and precedence rules.

## SPIKE-04 — Editor preview

Render the player's skin plus Loom cape/Elytra in a GUI preview that can rotate, zoom, and switch preview modes without changing the real equipped cosmetic until committed.

## SPIKE-05 — Multiplayer

Two clients plus dedicated server. Equip a Loom design on Player A and verify Player B receives, caches, and renders it. Joining/rejoining and cache-miss behavior must be tested.

## SPIKE-06 — Emissive/animated compatibility

Add one minimal animated layer and one emissive/additive overlay and test the complete Fabric/Sodium/Iris matrix.

## Gate before full UI work

Do not begin the full reference-image editor implementation until SPIKE-00 through SPIKE-06 are green or a documented replacement architecture has been accepted.

## Documentation rule

Every spike updates canonical documentation before it is DONE:

- current state
- implementation decision
- files/classes changed
- tests run and results
- Sodium/Iris matrix result when rendering-related
- known issues
- next work
- exact commit SHA
