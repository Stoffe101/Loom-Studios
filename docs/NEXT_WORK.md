# Loom Studios — Next Work

## Active: SPIKE-05 — multiplayer synchronization

Prove that Loom Studios cosmetics can be synchronized between two clients through a dedicated/integrated server without streaming texture frames.

Initial goals:
- define/register versioned Fabric payloads;
- advertise Loom Studios capability on join;
- synchronize each player's equipped Loom cosmetic identity/state;
- transfer project/compiled proof data only on cache miss;
- render another player's Loom cape/Elytra;
- preserve vanilla behavior for players without Loom Studios;
- reject malformed/oversized payloads cleanly;
- document exact server/client state ownership.

The first network proof can use the current technical-spike cosmetic rather than the full future .loom project format.

## Deferred preview refinement bundle

Do not spend a dedicated pass on these yet. Fold them into the next appropriate UI/editor refinement pass:

- stop the head from automatically following model rotation;
- add a **Facing** control for body/player orientation;
- add a separate **3D Orbit / Pivot** control for full 360-degree inspection;
- keep mouse drag as a fast direct-manipulation option;
- later expose front/back/left/right snap buttons;
- evaluate whether head orientation should be locked by default with an optional head-facing control.

The final editor should distinguish:

```
Player Facing     [ horizontal control / angle ]
3D Orbit/Pivot    [ free 360° inspection control ]
Zoom              [ slider + wheel ]
```

## After SPIKE-05

SPIKE-06 — animation/emissive compatibility proof.

## Documentation

Record exact CI SHA, multi-client topology, packet behavior, validation results, and runtime screenshots/results.
