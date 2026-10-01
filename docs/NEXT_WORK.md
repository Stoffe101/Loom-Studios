# Loom Studios — Next Work

## Verify SPIKE-05 and SPIKE-06

After the exact implementation commit is CI-green, perform one combined runtime pass.

### Single-client emissive check

- run normal Loom Studios - Client;
- enter a world;
- verify the cape still animates;
- press G and confirm the glow/emissive details visibly toggle;
- open P preview and confirm the same base cosmetic still renders.

### Two-client multiplayer check

Use the generated IntelliJ profiles:
- Loom Studios - Client A
- Loom Studios - Client B

Recommended easy topology:
1. Client A creates/opens a world.
2. Open the world to LAN.
3. Note the LAN port.
4. Client B joins `localhost:<port>`.
5. Put both players in third person / observe each other.

Expected:
- A sees B's Loom cape;
- B sees A's Loom cape;
- the two deterministic UUIDs produce different project accent colors;
- remote animation runs without streamed frames;
- emissive pass appears on remote players;
- disconnecting one player removes their equipped Loom state cleanly.

### Optional rendering compatibility

With the current optional dev stack:
- Sodium/Sodium Extra remain stable;
- Iris shaders OFF;
- Iris shaders ON with one installed shader pack;
- G toggle should never make the base cape disappear;
- 3D Skin Layers should remain unaffected.

## If green

Mark SPIKE-05 and SPIKE-06 DONE. The foundation-spike gate is then complete and full editor/core implementation can begin.

## Known deferred preview refinements

Keep head lock, Facing, Orbit/Pivot, snap angles, and final camera controls bundled into later UI refinement rather than opening a dedicated foundation pass.
