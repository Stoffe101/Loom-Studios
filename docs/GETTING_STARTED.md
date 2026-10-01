# Loom Studios — Getting Started

## Development baseline

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4
- Fabric API 0.141.1+1.21.11 initial pin
- Mojang mappings
- Fabric Loom 1.18-SNAPSHOT
- Gradle 9.7.1 wrapper

## IntelliJ IDEA

1. Clone `Stoffe101/Loom-Studios`.
2. Open the repository folder in IntelliJ IDEA.
3. Let IntelliJ import/sync Gradle.
4. Run **Loom Studios - Client** from the generated run profiles, or run the Gradle task `runClient`.
5. The development instance uses `run/`.

The run profile prefers the Gradle task so command-line and IntelliJ testing use the same setup.

## Optional compatibility mods

Copy optional test JARs into `dev-mods/`:

- sodium-fabric-0.8.7+mc1.21.11.jar
- sodium-extra-fabric-0.8.3+mc1.21.11.jar
- iris-fabric-1.10.7+mc1.21.11.jar
- skinlayers3d-fabric-1.11.3-mc1.21.11.jar

Then run **Loom Studios - Client** / `runClient`. All JARs in `dev-mods/` are added only to the local development runtime.

Use `gradlew listDevMods` to see what Gradle finds.

## Commands

Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat listDevMods
.\gradlew.bat runClient
```

Linux/macOS:

```bash
./gradlew build
./gradlew listDevMods
./gradlew runClient
```

## Spike order

SPIKE-00 toolchain → SPIKE-01 static cape → SPIKE-02 dynamic texture → SPIKE-03 Elytra → SPIKE-04 preview → SPIKE-05 multiplayer → SPIKE-06 animation/emissive.


## Troubleshooting runClient

If Gradle ends with only a generic Windows exit such as:

```
Process ... finished with non-zero exit value -1
```

the useful Minecraft/Fabric error is normally in:

- `run/logs/latest.log`
- the newest `run/crash-reports/crash-*.txt`, if a crash report was generated

PowerShell helpers:

```powershell
Get-Content .\run\logs\latest.log -Tail 250
```

and, when a crash report exists:

```powershell
$crash = Get-ChildItem .\run\crash-reports\*.txt |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
Get-Content $crash.FullName -Tail 300
```

For isolation, temporarily moving JARs out of `dev-mods/` gives a plain-Fabric client without changing the project dependencies.


## 3D Skin Layers in the development client

3D Skin Layers 1.11.3 bundles TRansition and TRender inside its JAR. Loom Studios automatically flattens those embedded libraries into `build/dev-mods-nested/` for the development runtime so Fabric Loom can remap them correctly.

After changing optional JARs in `dev-mods/`, run:

```powershell
.\gradlew.bat listDevMods
```

The output should show the four top-level test mods plus extracted TRansition/TRender library JARs when 3D Skin Layers is present.
