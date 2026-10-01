# Loom Studios — Pass Log

## 2026-10-01 — Cape face-first/static-default green build

**Result: CI PASS / LOCAL UX TEST REQUIRED**

Exact SHA: `475355119f491a631de8a1b5311e5c0e9f1bbdb4`  
GitHub Actions run #39: **SUCCESS**

Green scope:
- editor-created projects are static by default;
- semantic cape-face UV model;
- Outside/Back 10x16 primary canvas;
- face switching;
- pixel hover highlight;
- local + atlas coordinate readout;
- region-aware immutable edits;
- automated mapping/default tests.

---

## 2026-10-01 — Cape paint clarity + static default correction

**Runtime feedback:** functional paint/save/render path works, but raw-atlas editing was confusing and blank-project colors animated unexpectedly.

Implemented:
- editor-created projects default to no hue animation and no emissive effect;
- conventional 64x32 cape UV regions modeled explicitly;
- focused semantic face editor replaces raw full-atlas canvas in CapeEditorScreen;
- default view is 10x16 Outside / Back;
- face cycling for inside/edges/top/bottom;
- large grid, hover highlight and coordinate/UV readout;
- region-local edits map safely into the underlying atlas;
- automated tests cover static editor defaults and Outside-face UV mapping.

CI + second local UX check required.

---

## 2026-10-01 — Phase-2 first interactive editor green build

**Result: CI PASS / LOCAL RUNTIME TEST REQUIRED**

Exact SHA: `c94e5024d5adc7b4bcfa7da77d653986c1cd5a33`  
GitHub Actions run #37: **SUCCESS**

Green scope:
- Loom Studios home screen;
- Recent Projects/open flow;
- blank project creation;
- LoomButton + LoomCanvasWidget;
- Pencil/Eraser;
- starter palette;
- Undo/Redo;
- Save + Save/Equip;
- unsaved ProjectSession 3D preview;
- editor/world equipped-state separation;
- ProjectEdits common model;
- automated project-core tests.

The editor is now ready for first local hands-on testing.

---

## 2026-10-01 — Phase-2 LoomButton narration compile correction

Initial Phase-2 editor SHA `b002d1fdea0e00bdac8bfce8c341ee7e9e906598` reached client compilation and failed because the custom LoomButton inherited AbstractButton but had not implemented the required narration callback.

Correction:
- implement `updateWidgetNarration`;
- reuse Minecraft's default button narration text.

This also keeps the reusable control on the right accessibility path instead of bypassing narration.

---

## 2026-10-01 — Phase-2 first interactive Cape Editor slice

**Status: IMPLEMENTED / CI + LOCAL TEST PENDING**

Implemented:
- Loom Studios home screen;
- Recent Projects selection/open;
- blank cape creation;
- reusable LoomButton;
- reusable 64x32 LoomCanvasWidget;
- pencil/eraser;
- starter palette;
- undo/redo;
- save;
- save+equip;
- unsaved live 3D preview;
- immutable ProjectEdits pixel mutation helper;
- L dev key opens the studio.

This is the first pass where a user can actually paint a Loom project in-game.

---

## 2026-10-01 — Unsaved preview + production preview/library cleanup

**Status: IMPLEMENTED / CI PENDING**

Implemented:
- LoomPlayerPreviewScreen production naming;
- scoped preview-project override during local player render-state extraction;
- dirty editor project can render in preview while world/multiplayer retain equipped project;
- preview GPU bundle eviction on replacement/close;
- preview Elytra thickness comes from preview project;
- Recent Projects selection state;
- stale content-hash thumbnail pruning.

This is the final ownership bridge required before the real Phase-2 editor canvas can mutate ProjectSession live.

---

## 2026-10-01 — Editor/equipped state separation

**Status: IMPLEMENTED / CI PENDING**

Implemented:
- dirty ProjectSession is now distinct from saved/equipped world state;
- multiplayer publishes only the equipped project hash;
- in-world renderer consumes only the equipped project;
- unsaved edits can remain local to the editor;
- equip requires the current session to be saved/clean;
- save-and-equip convenience path added;
- WorkspaceState + listener registration added for future widgets/status bars.

This aligns the implementation with the original architecture rule that unsaved preview edits must not silently mutate multiplayer equipped state.

---

## 2026-10-01 — Workspace refactor compile correction

Initial workspace-refactor SHA `b62392d0e80acdc2afdc4a326c2320d290b10752` failed client compilation because the lifecycle callback still invoked `PlayerCosmeticRenderer.close()` with no argument after the renderer close method was changed to accept the Minecraft client.

Correction:
- register `PlayerCosmeticRenderer::close` directly with `CLIENT_STOPPING`.

No runtime architecture change.

---

## 2026-10-01 — Workspace retention + editor-created project foundation

**Status: IMPLEMENTED / CI PENDING**

Follow-up hardening before Phase 2:
- opening a saved project now survives the next player-bind tick instead of being replaced by the development factory project;
- blank editable projects can now be created with real metadata and transparent cape/Elytra base layers;
- local runtime GPU bundles are explicitly released when the live project content hash changes;
- local patched-skin cache is invalidated at the same time;
- automated test covers blank-project metadata/base-layer construction.

This prevents a future paint stroke from leaking one GPU texture bundle per project revision.

---

## 2026-10-01 — Phase-1 live workspace/runtime cache/library implementation

**Status: IMPLEMENTED / CI PENDING**

Changes:
- one ClientProjectWorkspace owns the local ProjectSession;
- ClientCosmeticSync no longer owns a duplicate local LoomProject;
- changed workspace hashes automatically trigger a new HELLO/cache-miss publication;
- RuntimeCosmeticCache owns GPU textures and animation uploads;
- PlayerCosmeticRenderer owns player skin patching/debug render controls;
- DynamicCosmeticSpike removed from the active architecture;
- Recent Projects descriptor/index added;
- compiled cape thumbnails cached as PNG files;
- library index skips broken files instead of poisoning the whole library.

Next: exact-SHA CI and then one local runtime regression.

---

## 2026-10-01 — Phase-1 session/persistence green checkpoint

**Result: PASS**

Exact SHA: `3de9c581c212ac390f699841dc844c6478d3927e`  
GitHub Actions run #29: **SUCCESS**

Verified:
- common/client compilation;
- schema-v1 metadata serialization;
- explicit migration gate;
- ProjectFileStore save/load/path containment tests;
- ProjectSession dirty/save/load/undo/redo tests;
- remapped artifact build/upload.

Phase 1 can now move to the client workspace/runtime-cache ownership slice.

---

## 2026-10-01 — Elytra visual calibration accepted + Phase-1 session slice

**Elytra result: PASS**

The calibrated 100% Loom Elytra thickness was locally verified and accepted as the default.

**Project-core implementation staged:**
- created/modified project metadata;
- explicit schema migration dispatch;
- root-bounded reusable ProjectFileStore;
- ProjectSession owns undo/redo, revision, dirty state, save/load state;
- save uses temporary file + atomic replace where supported;
- LocalProjectLibrary delegates to the tested pure file-store core;
- new tests cover migration rejection, save/load, dirty transitions and path containment.

CI verification required before this Phase-1 slice is marked green.

---

## 2026-10-01 — Phase-1 project core green checkpoint

**Result: PASS**

Exact SHA: `66695ffc66458c675654746c473809e9a13f1488`  
GitHub Actions run #27: **SUCCESS**

Passed:
- Java 21 / Minecraft 1.21.11 build;
- common and client compilation;
- JUnit project-core test suite;
- remap JAR/sources;
- artifact upload.

The old ProofProject spike type is removed after this green replacement. LoomProject is now the real network/runtime project object.

Elytra visual calibration still needs local eyes-on verification because CI cannot judge appearance.

---

## 2026-10-01 — Gradle 9 JUnit launcher correction

**Initial test-wiring SHA:** `58f25c610a8f27b002def89cfac0dfc51ae8d9cc`  
**GitHub Actions run #26:** FAILED in `:test`

Production/common/client compilation all passed. The failure was test-runner wiring only: Gradle 9 requires the JUnit Platform launcher to be present explicitly on the test runtime classpath.

Correction:
- JUnit BOM 5.10.2;
- JUnit Jupiter test implementation;
- explicit `junit-platform-launcher` test runtime.

No Loom runtime/project behavior changed in this correction.

---

## 2026-10-01 — Phase-1 project-core CI + invariant hardening

Exact SHA `fe734caa608bf1e3524222dd467f606d8a5dd6cc` passed GitHub Actions run #25.

Follow-up correctness hardening:
- LoomLayer now has deep pixel-array equality/hash semantics;
- schema-v1 runtime cape/Elytra canvases are explicitly constrained to 64x32;
- JUnit 5 project-core tests added to the normal Gradle `build` lifecycle;
- tests cover deterministic encode/decode/hash, defensive pixel ownership, trailing-byte rejection, runtime canvas dimensions, and undo/redo branching.

---

## 2026-10-01 — SPIKE-05 runtime PASS + Phase-1 project-core bootstrap

**Runtime result from two-client test: PASS for the primary synchronization path**

Observed:
- two different development players/projects visible together;
- per-player project colors differ;
- both remote/local cosmetics continue cycling color;
- no main-path synchronization issue reported.

**Implementation now staged:**
- Elytra 100% visual baseline calibrated to 0.5 raw model Z scale for Loom cosmetics;
- darker edge UV treatment to reduce the boxy/fat appearance;
- real schema-v1 LoomProject model;
- cape/Elytra canvases and immutable paint layers;
- bounded deterministic .loom binary codec;
- SHA-256 hashing/validation on real project bytes;
- multiplayer proof migrated from ProofProject to LoomProject;
- layer-to-texture compiler;
- local project library file plumbing;
- undo/redo project history foundation.

CI and local visual/runtime verification are required before this pass is marked fully green.

---

## 2026-10-01 — SPIKE-05/06 latest green implementation checkpoint

**Result: CI PASS / MANUAL RUNTIME PASS REQUIRED**

Exact SHA: `20451c816ecf9f8f806968c2663f462d161e659c`  
GitHub Actions run #23: **SUCCESS**

Green scope:
- content-addressed multiplayer proof protocol;
- server-side project validation/cache/equipped state;
- client-side cache-miss retrieval and remote compilation;
- remote-player cape/Elytra render-state application;
- deterministic local animation;
- generated emissive cape mask;
- Fabric-registered emissive player feature layer;
- vanilla chest-armor offset and wing-suppression parity;
- Client A and Client B dev profiles;
- Windows-safe nested optional-mod extraction for concurrent clients.

Completion gate remaining:
- actual two-client LAN synchronization;
- visible remote cosmetics on both sides;
- emissive ON/OFF visual proof;
- Iris/shader smoke test.

---

## 2026-10-01 — Multi-client Windows dev-runtime hardening

**Status: IMPLEMENTED / CI RETEST REQUIRED**

Before the SPIKE-05 two-client test, the optional nested-library extraction was changed to be concurrency-friendly.

Previously Gradle deleted and recreated `build/dev-mods-nested/` during every configuration. On Windows, launching Client B while Client A still had an extracted TRansition/TRender JAR open could cause a file-lock failure.

Now:
- current embedded-library outputs are tracked explicitly;
- existing matching-size files are reused;
- the directory is not deleted during normal configuration;
- only outputs belonging to currently installed dev mods are added to `modLocalRuntime`;
- stale files are therefore ignored even if they remain under build/ until the next clean.

This is development-harness-only and does not affect shipped Loom Studios artifacts.

---

## 2026-10-01 — SPIKE-06 vanilla cape alignment parity

**Status: IMPLEMENTED / CI RETEST REQUIRED**

The emissive feature pass now mirrors vanilla CapeLayer's chest-equipment behavior:
- suppress glow when the equipped chest asset provides a WINGS layer;
- apply the same armor offset when the chest asset provides a HUMANOID layer.

Reason: the second glow model must stay pixel-aligned with the vanilla base cape under armor and non-vanilla wing equipment, not merely when the chest slot is empty.

---

## 2026-10-01 — SPIKE-05/06 first green compile checkpoint

**Result: CI PASS / RUNTIME VERIFICATION PENDING**

Exact SHA `f33c8adf3baa7cfdd7f6a4d1e7fd28e15c0a357c`  
GitHub Actions run #20: **SUCCESS**

Verified by CI:
- common networking payloads compile;
- server cache/validation code compiles;
- client synchronization/cache code compiles;
- remote-capable dynamic cosmetic renderer compiles;
- Fabric player emissive feature layer compiles;
- Client A / Client B run configurations configure successfully;
- packaged development artifact uploads successfully.

Follow-up robustness improvement:
- client hello is now allowed to wait until the local player/level are actually available after JOIN instead of silently giving up if JOIN fires slightly early.

Manual two-client and shader/emissive verification is still required before SPIKE-05/06 are marked DONE.

---

## 2026-10-01 — SPIKE-05 Mojang-mapping server accessor correction

**Status: FIX APPLIED / CI RETEST REQUIRED**

Exact implementation SHA `9312856fc1449895b3b973371a1a0b566ed85a2f` reached `compileJava` and failed on four calls to `ServerPlayer#getServer()`.

Minecraft 1.21.11 Mojang mappings keep the ServerPlayer server field private and expose the server through the player's ServerLevel instead.

Correction:
- `context.player().getServer()` -> `context.player().level().getServer()`
- `player.getServer()` -> `player.level().getServer()`

No protocol or architecture change was required.

---

## 2026-10-01 — SPIKE-05 + SPIKE-06 implementation

**Status: IMPLEMENTED / CI + RUNTIME VERIFICATION PENDING**

SPIKE-05:
- SHA-256 content-addressed proof project;
- server cache-miss upload;
- project validation;
- equipped-state broadcast;
- remote cache-miss download;
- remote player cosmetic rendering;
- graceful no-server-mod behavior via `ClientPlayNetworking.canSend`;
- separate Client A / Client B IntelliJ run profiles.

SPIKE-06:
- deterministic client-side animation using synchronized world time;
- no animation-frame packets;
- generated per-project emissive mask;
- additional Fabric-registered player render layer;
- vanilla base CapeLayer retained;
- fullbright/translucent-emissive second cape pass;
- G toggles effect locally for visual comparison.

Runtime verification remains required before either spike is DONE.

---

## 2026-10-01 — SPIKE-04 local runtime verification

**Result: PASS**

Exact implementation SHA: `faca3953ef07c9a2755bd0619963a59712cae548`  
GitHub Actions run #17: **SUCCESS**

User screenshots verified:
- local player renders correctly inside the custom GUI;
- Cape mode works;
- Elytra mode works;
- preview-only switching does not require changing actual world equipment;
- drag rotation works;
- zoom works;
- Loom cape/Elytra cosmetics use the same gameplay render path.

Deferred minor issue:
- head orientation follows the preview rotation and feels too "look-at-camera" for a design inspection tool.

Planned bundled refinement:
- separate body-facing control from camera/orbit control;
- optionally lock head orientation or expose an explicit head/facing control;
- preserve full 360-degree model inspection.

This is polish, not an architectural failure. SPIKE-04 is DONE.

Next: SPIKE-05 multiplayer synchronization.

---

## 2026-10-01 — SPIKE-04 live preview implementation

**Status: IMPLEMENTED / CI + LOCAL TEST PENDING**

Implemented:
- custom in-world Screen;
- P debug key to open;
- real local-player render-state extraction;
- preview-only chest equipment override;
- Cape/Elytra preview toggle without mutating actual equipment;
- drag rotation;
- wheel zoom;
- reset control;
- same Loom runtime cosmetic textures as gameplay;
- non-pausing in-world behavior.

This screen is architectural scaffolding only. Final Loom Studios UI work will replace its visual treatment after the foundation spikes.

---

## 2026-10-01 — SPIKE-03 local runtime verification

**Result: PASS**

Exact implementation SHA: `5d8fce628228fc570750c60734a7bedf1497701a`  
GitHub Actions run #15: **SUCCESS**

User verified in-world:
- dedicated Elytra texture works;
- cape animation remains independent;
- vanilla gliding/wing animation works;
- V-key thickness presets function correctly;
- 100% restores vanilla thickness;
- thinner/thicker render-only geometry values work;
- no observed conflict with the optional development mod stack.

Product decision confirmed:
- Elytra editor will later expose Thickness as a real control;
- 100% is the default;
- Reset to Vanilla is required;
- debug V-key cycling is temporary development tooling.

SPIKE-03 is DONE.

Next: SPIKE-04 live GUI player preview.

---

## 2026-10-01 — SPIKE-03 geometry-thickness proof implementation

**Status: IMPLEMENTED / CI + LOCAL TEST PENDING**

Changes:
- restored opaque Elytra edge/top/bottom UV coverage so default 100% appears volumetric like vanilla;
- added an ElytraModel mixin that changes only wing local Z scale;
- default is exactly 1.0 (100% vanilla thickness);
- temporary V key cycles 100%, 75%, 50%, 25%, 150%;
- non-Loom Elytras are explicitly reset to zScale 1.0 because vanilla model instances are reused;
- thickness is visual only and does not affect hitboxes/gameplay.

Goal: prove that Loom Studios can expose thickness customization without replacing vanilla pose/animation behavior.

---

## 2026-10-01 — Elytra thickness design correction

**Result: DECISION ACCEPTED**

Runtime screenshots showed the transparent edge-face UV experiment made the dedicated Elytra texture look effectively paper-thin.

Decision:
- remove edge-face transparency as the default visual strategy;
- preserve normal vanilla Elytra appearance at the default setting;
- separate artwork from geometry;
- add future user-controlled Elytra thickness as a render-only model setting.

The intended implementation will scale wing depth rather than hiding side-face textures.

---

## 2026-10-01 — SPIKE-02 local runtime verification

**Result: PASS**

User runtime screenshots confirm:
- cape palette/highlight changes live;
- vanilla cape movement continues to work;
- no restart or reconnect is required;
- optional development stack remains stable.

Additional discovery:
- equipping an Elytra caused Minecraft to use the animated cape texture for the wings;
- source inspection confirmed vanilla `WingsLayer.getPlayerElytraTexture` prefers `skin.elytra()`, then falls back to visible `skin.cape()`.

This proves both the dynamic-texture pipeline and the requirement for independent cape/Elytra texture channels.

---

## 2026-10-01 — SPIKE-03 dedicated Elytra implementation

**Status: IMPLEMENTED / VERIFICATION PENDING**

Implemented a separate runtime Elytra texture and supplied it through the Elytra field of `PlayerSkin.Patch`.

The test texture also leaves Elytra edge-face UV strips transparent as an experiment to reduce visible boxiness without changing vanilla geometry.

Source inspection documented that vanilla 1.21.11 Elytra geometry is a 10x20x2 box per wing with CubeDeformation(1.0F), explaining the user's observation that it looks visually thick.

Next: CI + local in-world Elytra verification.

---

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
