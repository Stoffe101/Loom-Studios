# Loom Studios — Next Work

## Finish SPIKE-00

1. Verify GitHub Actions on the bootstrap commit.
2. Clone/open the project in IntelliJ.
3. Put optional JARs in `dev-mods/`.
4. Run **Loom Studios - Client** / `runClient`.
5. Confirm optional-mod detection in logs.
6. Run `runServer` and verify client-only classes do not leak to the server.
7. Record exact results and SHA.

## SPIKE-01

After SPIKE-00 is green:

- add an obvious static test cape;
- override the cape texture through the narrowest vanilla-compatible player render path;
- preserve vanilla geometry/movement;
- test plain Fabric first;
- then smoke-test Sodium/Iris/3D Skin Layers.

Update canonical docs with every result.
