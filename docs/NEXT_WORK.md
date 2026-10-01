# Loom Studios — Next Work

## Active: SPIKE-04 runtime verification

After CI is green:

1. git pull
2. runClient
3. enter a world
4. press P to open the Loom preview spike

Verify:
- actual player skin/model appears;
- cape mode displays the dynamic Loom cape;
- C switches to Elytra without changing real equipped chest item;
- C switches back to cape;
- left-drag rotates smoothly;
- mouse wheel zooms;
- R resets the camera;
- closing the screen returns to the unchanged world/equipment state;
- dynamic cape continues updating inside the preview;
- Elytra thickness setting is reflected in preview rendering;
- optional mod stack remains stable.

## After SPIKE-04

SPIKE-05 — multiplayer synchronization.

## UI reminder

Do not judge this spike against the approved Loom Studios reference images. It exists only to validate the reusable preview/render-state architecture.
