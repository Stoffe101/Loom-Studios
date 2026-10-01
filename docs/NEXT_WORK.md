# Loom Studios — Next Work

## Phase 1 — finalization

Current implementation now covers:
- versioned project model/codec/migrations;
- session/undo/redo/dirty/save/load;
- edit state separate from equipped state;
- multiplayer uses equipped content hash;
- runtime GPU cache;
- unsaved project preview override;
- Recent Projects index/selection;
- cached thumbnails with stale-cache pruning;
- production player-preview foundation.

After exact-SHA CI and one runtime regression, Phase 1 is functionally ready for Phase 2.

## Immediate local regression

- runClient;
- world cape still renders/animates;
- P opens **Loom Studios - Player Preview**;
- preview Cape/Elytra toggle still works;
- V/G dev controls still work;
- Elytra baseline remains correct.

No UI for editing exists yet, so the dirty-preview override will receive its first interactive exercise in Phase 2.

## Next: Phase 2 Cape Editor MVP

Start with:
1. editor/home screen shell using ProjectLibraryIndex;
2. create blank project + open recent project;
3. reusable Loom widgets;
4. 64x32 cape pixel canvas bound to ClientProjectWorkspace;
5. pencil + eraser;
6. undo/redo buttons/shortcuts;
7. live player preview showing unsaved session state;
8. save and save+equip.

Then add:
- fill;
- eyedropper;
- line/rectangle;
- color picker/palette;
- symmetry;
- move/crop/flip.

## Compatibility gate

Final Iris shader emissive smoke test remains a release-hardening requirement.
