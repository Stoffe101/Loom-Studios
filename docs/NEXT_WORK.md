# Loom Studios — Next Work

## Phase 1 — active verification

Current implementation now has:
- one live ClientProjectWorkspace / ProjectSession;
- automatic project-hash republishing to multiplayer;
- production RuntimeCosmeticCache;
- PlayerCosmeticRenderer facade;
- Recent Projects descriptor/index;
- cached project thumbnails.

After CI is green, do one quick local regression:
- runClient;
- confirm cape still renders/animates;
- P preview still works;
- Elytra still looks correct;
- optional G/V debug controls still work.

No dedicated two-client test is required unless the single-client regression or CI exposes a networking issue.

## Next Phase-1 work

1. add a production client workspace controller API for create/open/save/save-as/equip;
2. add project-library selection/recent-project state;
3. expose live-session change notifications for editor widgets;
4. finish removing SPIKE naming from preview/effect scaffolding where appropriate;
5. make thumbnail/render-cache invalidation explicit on edits.

Then begin **Phase 2 Cape Editor MVP**:
- Cape Loom block interaction;
- home/start screen using the Recent Projects index;
- cape canvas;
- pencil/eraser/fill/eyedropper;
- color controls;
- live 3D preview bound to the ProjectSession;
- save/equip.

## Remaining compatibility gate

SPIKE-06 still needs final Iris shader smoke testing before release hardening.
