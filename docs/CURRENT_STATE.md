# Loom Studios — Current State

**Checkpoint:** 2026-10-01 repository/documentation bootstrap

## Overall

**Status: REPOSITORY CREATED / TECHNICAL FOUNDATION DONE / MOD IMPLEMENTATION NOT STARTED**

Repository: Stoffe101/Loom-Studios

The product/design specification, visual references, compatibility requirements, and technical feasibility research exist. The GitHub repository now exists and the canonical docs/ structure has been established.

## Confirmed direction

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 exact compatibility baseline
- compatible Fabric API 1.21.11 build to be pinned by SPIKE-00
- compatible Fabric Loom version to be pinned by SPIKE-00
- Mojang mappings
- split common/client source sets
- vanilla-first cape texture override
- vanilla-first Elytra texture override to be proven
- dynamic runtime textures through NativeImage / NativeImageBackedTexture
- custom feature layer for emissive overlays
- local editing with server-authoritative saved/equipped project synchronization
- hash-addressed project cache
- editable .loom project format
- short and portable Loom Codes
- Sodium compatibility mandatory
- Iris + shaders compatibility mandatory
- proprietary / All Rights Reserved licensing
- documentation is a mandatory completion gate

## Proven by research, not yet by Loom Studios code

- Minecraft 1.21.11 custom cape render-state override is viable.
- dynamic textures are available through vanilla client APIs.
- Fabric supports custom payload networking and large-payload splitting.
- Fabric custom screens/widgets are sufficient for the planned editor.
- player skin data provides separate cape and Elytra texture slots.
- additional living-entity feature rendering is available for optional passes.

## Must be proven by Loom Studios spikes

- exact Gradle/Loom/Fabric API tuple with Loader 0.18.4
- exact cape hook/mixin target using Mojang mappings
- Elytra texture precedence with equipped Elytra
- preview override scoping
- emissive geometry alignment
- Sodium compatibility
- Iris compatibility with shaders off/on
- real multi-client project cache/synchronization
- image import/file-dialog portability
- animation performance budget

## Immediate next action

Execute **SPIKE-00 — Toolchain bootstrap**.

Once build, dev client, and dedicated server are green, immediately begin **SPIKE-01 — Static custom cape through the vanilla renderer**.

## Documentation checkpoint

The repository now uses docs/ as the canonical documentation home. All meaningful future passes must update documentation before being called complete.
