# Loom Studios — Getting Started

## Library and layer tools

Favorites appear first in Designs; search matches design names, local folder names and tags. Right-click a recent Home project or library card for Edit, Rename, Favorite, Duplicate, Delete, Equip, Folder / tags and Backup / versions. Double-click opens a design for editing. Home immediate Favorite/Duplicate/Equip actions return to Home; other workflows use the shared library/task screens.

Ctrl-click library cards to add/remove selections; Shift-click selects a range. Bulk actions can assign folder/tags, favorite/unfavorite, create backups and move to recoverable Trash. Bulk restore refuses to overwrite an existing saved file. Counts report completed and failed items. Empty folder/tag fields clear existing organization, including when applying to several designs.

Each changed explicit save keeps the previous artwork in local history, up to 20 unique snapshots per saved design. Backup / versions can create a manual snapshot or restore a previous one after confirmation; restore validates identity/checksum and backs up the current save first. Unsaved workspace changes use the usual Save/Keep draft/Discard/Cancel guard. Recovery autosave remains separate and does not automatically create saved-design versions.

Click Manage in the Cape/Elytra layer panel for the full layer manager. Ctrl/Shift selects several layers; Select all shown applies to the current group filter. Assign/clear group changes local organization. Group filtering, hide/show, lock/unlock, duplicate, move up/down and confirmed delete operate on the selection. Canvas batch operations are one Ctrl+Z undo step; group labels are local preferences and are not canvas history. At least one layer must remain, and oversized duplication leaves the document unchanged. The compact inspector uses smaller rows; full managers show the stack without competing with the 3D panel.

Folder/tag/group metadata is installation-local: it does not travel in exported artwork or version snapshots. Groups do not create nested rendering/group effects. The template collection now has fourteen designs, including Aurora, Dragon, Phoenix, Crystal, Cat, Fox, Frog and Heart. Previous/Next switches pages and keeps preview selection on the visible page.

## Development baseline

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4
- Fabric API 0.141.1+1.21.11 initial pin
- Mojang mappings
- Fabric Loom 1.17.21
- Gradle 9.6.1 wrapper

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


## Elytra thickness

Open Elytra -> Props -> Thickness. The authored 25%–200% value is saved with the project and used by Save + Equip. The historical V-key override was removed; 100% is the calibrated vanilla baseline.

## Editor screenshot verification

`./gradlew runClient -PuiCapture` explicitly enables a development-only screenshot flow that creates a flat test world in the isolated `run-ui-capture` directory. It records 42 actual Minecraft captures across all five screens at the four required display/GUI profiles, checks visible widget bounds and pairwise nonoverlap, and verifies project/portable/PNG exports plus candidate/apply pixel equivalence on both import pages. Normal launches and packaged clients never invoke it.

## Running two development clients on Windows

The optional embedded-library extractor is safe to reuse while another dev client is running. This matters for the SPIKE-05 Client A / Client B test because Windows may lock loaded JARs.

Generated profiles:
- `Loom Studios - Client A`
- `Loom Studios - Client B`

Equivalent Gradle tasks:

```powershell
.\gradlew.bat runClientA
.\gradlew.bat runClientB
```

Run them in separate IntelliJ/terminal sessions. Each uses its own Minecraft run directory.
