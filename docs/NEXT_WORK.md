# Loom Studios — Next Work

## Immediate state

Elytra visual baseline: **VERIFIED / ACCEPTED**.

Phase 1 Project Core is active.

## Current Phase-1 slice

**GREEN at exact SHA `3de9c581c212ac390f699841dc844c6478d3927e`, GitHub Actions #29.**

Completed:
- project created/modified metadata;
- explicit schema-loader/migration dispatch;
- ProjectSession owning undo/redo;
- revision tracking;
- hash-based dirty state;
- save/load lifecycle;
- reusable root-bounded ProjectFileStore;
- Minecraft LocalProjectLibrary adapter;
- automated persistence/session tests.

## Next Phase-1 slices

1. client workspace/session manager that makes ProjectSession the live local editing source;
2. runtime cache ownership separated from DynamicCosmeticSpike;
3. project-library index/descriptor model for Recent Projects;
4. thumbnail generation/cache;
5. decompose remaining SPIKE-named runtime code into production services.

After those are stable, begin Phase 2 Cape Editor MVP:
- Cape Loom block interaction;
- home/start screen;
- real cape canvas;
- pencil/eraser/fill/eyedropper;
- color controls;
- live 3D preview wired to ProjectSession;
- save/equip.

## Remaining foundation compatibility gate

SPIKE-06 still needs a final shader smoke test:
- G off/on;
- Iris shaders OFF;
- Iris shaders ON with one shader pack.

This no longer blocks Phase-1 implementation, but remains required before release-hardening.
