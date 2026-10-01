# Loom Studios — Next Work

## Immediate verification

Pull the latest implementation after CI is green.

### Elytra visual baseline

- equip the Loom Elytra;
- verify default 100% now looks close to a normal vanilla Elytra instead of thick/boxy;
- press V through 75/50/25/150%;
- confirm 100% is the sensible reset/default;
- confirm non-Loom Elytras are unaffected.

### Project-core runtime regression

The visible development cape should look essentially the same, but it is now compiled from a real schema-v1 LoomProject instead of ProofProject.

Verify:
- single-player cape still renders and cycles;
- two-client LAN projects still differ and sync;
- Elytra still uses its independent project canvas;
- G emissive toggle still works;
- P preview still works.

## Phase 1 — Project Core active focus

Implemented foundation:
- versioned project model;
- cape/Elytra canvases;
- immutable paint layers;
- schema-v1 deterministic serialization;
- content hashing;
- texture compiler;
- local .loom library filesystem plumbing;
- undo/redo history foundation.

Next Phase-1 slices:
1. project metadata and explicit migrations;
2. editor/session state that owns ProjectHistory;
3. dirty-state/save/load lifecycle;
4. runtime cache ownership separated from the old spike class;
5. project-library index + thumbnails;
6. remove/decompose remaining SPIKE-named runtime scaffolding.

## Foundation-spike cleanup still pending

SPIKE-06 emissive/shader compatibility should still get a final visual smoke pass:
- G off/on;
- Iris shaders OFF;
- Iris shaders ON with one shader pack.

This does not block project-core work, but it remains a release gate before the foundation phase is declared entirely closed.

## Known deferred preview refinements

Keep head lock, Facing, Orbit/Pivot, snap angles, and final camera controls bundled into the later UI refinement pass.
