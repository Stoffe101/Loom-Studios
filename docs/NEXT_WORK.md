# Loom Studios — Next Work

## 1. Complete CI verification

- confirm the Gradle bootstrap is green on the newest exact SHA;
- if the SPIKE-01 mixin fails compilation, fix only the mapped 1.21.11 seam and document it.

## 2. Local IntelliJ verification

Pull/clone the repository, place optional compatibility JARs in `dev-mods/`, sync Gradle, then launch **Loom Studios - Client**.

Verify:

- Minecraft 1.21.11 launches;
- Loom Studios logs both common/client initialization;
- optional mods present in `dev-mods/` are detected;
- F5/third-person shows the cyan/magenta LS test cape on the local player;
- the cape moves with vanilla cape physics.

Then launch **Loom Studios - Server** / `runServer` and verify clean dedicated-server startup.

## 3. SPIKE-01 compatibility smoke tests

After plain Fabric works:

- Sodium
- Sodium + Sodium Extra
- Sodium + Iris, shaders off
- Sodium + Iris, shaders on
- 3D Skin Layers

These are optional compatibility targets, not required dependencies.

## 4. Then SPIKE-02

Replace the static resource cape with a runtime-generated dynamic texture and prove live texture changes without restart/reconnect.

Update documentation with every result.
