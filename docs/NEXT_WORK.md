# Loom Studios — Next Work

## Active: SPIKE-02 — Dynamic runtime cape

Replace the static resource cape with an in-memory generated runtime texture.

Goals:

- create a cape image at runtime;
- register it with Minecraft's texture manager;
- patch the local player's cape state to the dynamic texture;
- update visible pixels without restarting/reconnecting;
- avoid recreating/uploading textures unnecessarily;
- safely close/replace GPU-backed texture resources;
- keep vanilla cape motion/geometry unchanged;
- preserve optional-mod compatibility.

Initial proof should make the cape visibly change on a timer or debug trigger so success is unmistakable.

## After SPIKE-02

SPIKE-03 — custom Elytra texture through the narrowest vanilla-compatible path.

## Documentation

Update CURRENT_STATE, PASS_LOG, NEXT_WORK, DECISIONS, TEST_MATRIX, and relevant technical docs with exact results.
