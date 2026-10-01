# Loom Studios — Pass Log

## 2026-10-01 — SPIKE-02 dynamic cape implementation

**Status: IMPLEMENTED / CI + LOCAL RUNTIME VERIFICATION PENDING**

Implemented:
- runtime-created 64x32 NativeImage;
- registered DynamicTexture under a stable Loom Studios Identifier;
- cape render-state now points at the dynamic texture;
- in-place redraw + `DynamicTexture.upload()` every 40 client ticks;
- no texture re-registration per update;
- shutdown cleanup via texture-manager release;
- obvious four-phase palette/highlight animation for visual proof.

The full editor will later replace this procedural generator with project/layer compositing.

---

## 2026-10-01 — SPIKE-01 local runtime verification

**Result: PASS**

The local Windows IntelliJ/Gradle development client was launched successfully with the optional test stack present.

Verified in-world:
- LS test cape appears on the local player;
- vanilla cape movement/geometry works correctly;
- no obvious clipping or rendering failure;
- Sodium, Sodium Extra, Iris, and 3D Skin Layers coexist in the test client;
- screenshot evidence captured by the user.

SPIKE-01 static cape rendering is complete.

Next: SPIKE-02 dynamic runtime texture updates.

---

## 2026-10-01 — 3D Skin Layers dev-runtime crash diagnosis

**Result: FIX IMPLEMENTED / LOCAL RETEST REQUIRED**

The first full optional-mod `runClient` reached Fabric Loader and loaded Loom Studios, Sodium, Sodium Extra, Iris, and 3D Skin Layers.

Crash cause:
- 3D Skin Layers failed during its client entrypoint;
- missing class: `dev.tr7zw.transition.loader.ModLoaderUtil`;
- the supplied 3D Skin Layers JAR contains TRansition and TRender under `META-INF/jars/`;
- Loom's remapped local-runtime copy did not expose those embedded libraries as separate runtime mods in the development namespace.

This is a **development harness issue**, not evidence that Loom Studios' cape mixin conflicts with 3D Skin Layers.

Fix:
- inspect optional dev-mod JARs during Gradle configuration;
- extract embedded `META-INF/jars/*.jar` libraries into `build/dev-mods-nested/`;
- add both top-level and extracted JARs to `modLocalRuntime` so Loom remaps every library into the named dev namespace;
- keep all generated/external JARs out of the shipped Loom Studios artifact.

Sodium Extra also reported that Reese's Sodium Options is recommended but missing. That warning is non-fatal and unrelated to this crash.

Next: local `runClient` retest with the same four optional compatibility mods.

---

## 2026-10-01 — Local IntelliJ test feedback

**Status: FIX APPLIED / CLIENT CRASH UNDER INVESTIGATION**

User local results:
- `runClient` reached the Java client process but exited with Windows status `0xFFFFFFFF`;
- Gradle's outer error does not identify the Minecraft/Fabric cause, so `run/logs/latest.log` / crash report is required;
- `listDevMods` failed solely because the helper task was incompatible with Gradle configuration cache.

Correction:
- disable Gradle configuration cache for this project;
- this matches the development-first IntelliJ/Loom workflow and removes needless friction from helper/run tasks;
- add exact log/crash-report troubleshooting commands to GETTING_STARTED.md.

The client crash itself is not yet attributed to Loom Studios or an optional compatibility mod.

---

## 2026-10-01 — Official Gradle launcher scripts

**Result: CI PASS**

Exact SHA: `c321be1f4d0713d29826a4fd773b614c3b26a226`

GitHub Actions run #7:
- wrapper info: PASS
- full build: PASS
- artifact upload: PASS

Replaced the temporary minimal wrapper launcher scripts with the full official `gradlew` and `gradlew.bat` scripts from Fabric's 1.21.11 example project. The committed wrapper JAR remains the official Gradle wrapper binary.

Reason: Windows/IntelliJ is the primary local development workflow, so wrapper launching should handle quoting, JAVA_HOME, and platform edge cases exactly as the standard Fabric project does.

---

## 2026-10-01 — SPIKE-01 compile checkpoint

**Result: CI PASS / RUNTIME VISUAL TEST PENDING**

Exact SHA: `c7386f0ed3a46bfb51c7ae8614162deb03ee4fd4`

GitHub Actions run #5:
- Java 21 setup: PASS
- Gradle wrapper: PASS
- project build: PASS
- SPIKE-01 client mixin compilation: PASS
- resources/mixin JSON/test cape packaging: PASS
- artifact upload: PASS

Implemented:
- local-player-only AvatarRenderer render-state injection;
- cape-only `PlayerSkin.Patch`;
- vanilla `CapeLayer` remains responsible for motion/geometry;
- cyan/magenta 64x32 LS test cape;
- patched-skin cache;
- client-only mixin configuration.

Still required before SPIKE-01 is DONE:
- local runClient visual confirmation;
- normal vanilla cape movement;
- optional Sodium/Iris/Sodium Extra/3D Skin Layers smoke tests.

---

## 2026-10-01 — First green SPIKE-00 CI baseline

**Result: CI PASS**

Exact SHA: `a620de1a16334657b7e33f1606c800a673580214`

GitHub Actions run #4 completed successfully and uploaded the Loom Studios development artifact. This proves the Java 21 + Minecraft 1.21.11 + Loader 0.18.4 dependency baseline + Fabric API 0.141.1 + Loom 1.17.21 + Gradle 9.6.1 build combination.

Local dev-client and dedicated-server launches remain required before SPIKE-00 is fully DONE.

---

## 2026-10-01 — SPIKE-00 JAR configuration-cache correction

**Status: IN PROGRESS**

Exact SHA `680876b000cd6f50c079d6bb6abc9a83b48cc2d4`:

- Gradle wrapper: PASS
- Loom 1.17.21 on Java 21: PASS
- Minecraft/Fabric dependency setup: PASS
- `compileJava`: PASS
- `processResources`: PASS after prior correction
- `jar`: FAIL configuration-cache validation

Cause: the LICENSE rename closure read `project.base.archivesName` at task execution time.

Correction: capture the archive name during configuration and use only that captured value in the JAR task closure.

Next: rerun exact-SHA CI.

---

## 2026-10-01 — SPIKE-00 Gradle configuration-cache correction

**Status: IN PROGRESS**

Corrected SHA `6f442183229d9931457e4df47023751e18a32811`:

- Loom 1.17.21 resolved and ran successfully on Java 21.
- Minecraft/Fabric dependency setup reached compilation.
- `compileJava` completed.
- Build failed in `processResources` because the Groovy closure referenced `project.version` at execution time while Gradle configuration cache is enabled.

Correction:

- capture the mod version during configuration;
- pass the captured value into `processResources`;
- keep configuration cache enabled instead of papering over the issue.

Next: rerun exact-SHA CI.

---

## 2026-10-01 — SPIKE-00 toolchain correction

**Status: IN PROGRESS**

Bootstrap SHA `400d82c798db6a62a750ba2236481b22897c612e`:

- Gradle wrapper itself: PASS
- Java runtime: Java 21 PASS
- Build: FAIL during Loom plugin resolution
- Cause: current Loom 1.18.2 requires Java 25 to run Gradle

Correction:

- pin Fabric Loom to `1.17.21`
- pin Gradle distribution to `9.6.1`
- retain Java 21
- retain Minecraft 1.21.11
- retain Fabric Loader 0.18.4 baseline

Reason: Loom 1.17 already contains the modern property-based run configuration API and `preferGradleTask`, while avoiding Loom 1.18's Java 25 build-runtime requirement.

Next: CI verification on the corrected exact SHA.

---

## 2026-10-01 — SPIKE-00 bootstrap implementation

**Status: IN PROGRESS**

Implemented:

- real Fabric Gradle scaffold
- Minecraft 1.21.11 / Java 21
- Loader 0.18.4 baseline
- Fabric API 0.141.1+1.21.11 initial pin
- Mojang mappings
- split common/client sources
- Gradle wrapper
- IntelliJ/Loom client and server run profiles
- client run prefers Gradle runClient
- local optional compatibility mods through dev-mods/ + modLocalRuntime
- common/client entrypoints
- detection logging for Sodium, Sodium Extra, Iris, and 3D Skin Layers
- GitHub Actions build
- compatibility docs corrected: optional supported integrations, not dependencies

Verification pending:
- corrected CI
- local client/server launches
- optional test stack runtime

Next: finish SPIKE-00, then static cape.

---

## 2026-10-01 — Technical foundation research

**Result: PASS**

No blocker found for the planned core architecture.
