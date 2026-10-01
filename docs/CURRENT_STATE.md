# Loom Studios — Current State

**Checkpoint:** 2026-10-01 implementation bootstrap

## Overall

**Status: SPIKE-00 IN PROGRESS**

The real Fabric project skeleton is present and CI verification is active.

## Baseline

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 development baseline
- Fabric API 0.141.1+1.21.11 initial pin
- Fabric Loom 1.17.21
- Gradle 9.6.1 distribution through the wrapper
- Mojang mappings
- split common/client source sets
- IntelliJ/Loom client and server runs
- optional local dev mods through dev-mods/
- proprietary license
- documentation hard gate

## Optional compatibility targets

Not dependencies:

- Sodium
- Sodium Extra
- Iris
- shader packs
- 3D Skin Layers

## Implemented

- common Fabric entrypoint
- client Fabric entrypoint
- optional-mod detection/logging
- Gradle/IntelliJ run setup
- CI build workflow

## CI history

Bootstrap SHA `400d82c798db6a62a750ba2236481b22897c612e` reached Gradle successfully but failed during project configuration because Loom 1.18.2 now requires a Java 25 Gradle runtime.

Decision: keep Loom Studios development on Java 21 and pin Loom to the latest 1.17 line (`1.17.21`) with Gradle 9.6.1. Loom 1.17 provides the property-based run configuration API and `preferGradleTask` without forcing Java 25.

## Verified in CI

Exact SHA `a620de1a16334657b7e33f1606c800a673580214` produced the first fully green bootstrap build and uploaded the development JAR artifact.

Exact SHA `c7386f0ed3a46bfb51c7ae8614162deb03ee4fd4` built successfully with the SPIKE-01 static cape mixin, test texture, and client resource configuration.

Exact SHA `c321be1f4d0713d29826a4fd773b614c3b26a226` is the latest fully green implementation checkpoint. GitHub Actions run #7 passed the wrapper check, full build, and artifact upload using the official Fabric Gradle launcher scripts.

## Local verification still required

- IntelliJ/Gradle `runClient`
- `runServer`
- Loader 0.18.4 runtime startup
- visual LS test-cape behavior
- optional-mod detection and compatibility smoke tests

## SPIKE-01 implementation staged

A static development cape has now been implemented in source:

- local-player-only render-state patch;
- vanilla CapeLayer retained;
- body/Elytra/model type untouched;
- cached patched PlayerSkin;
- obvious 64x32 cyan/magenta LS development texture;
- client-only mixin configuration.

Compile/CI and local visual verification are still required before SPIKE-01 can be marked DONE.

## Next

Verify corrected CI and then launch the IntelliJ development client/server locally. In third person, the local player should show the LS test cape. After that, validate optional Sodium/Iris/3D Skin Layers compatibility.


## Latest local test feedback

The first Windows `runClient` attempt started the Java client process but exited with status `0xFFFFFFFF`. This is a runtime/client failure rather than a compile failure. The actual cause must be read from `run/logs/latest.log` or the newest crash report before changing rendering code.

The `listDevMods` helper exposed a separate Gradle configuration-cache incompatibility. Configuration cache is now intentionally disabled for the IntelliJ/Loom development workflow.


## Optional-stack crash diagnosis

The first local full-stack runtime did not fail in Loom Studios rendering code. 3D Skin Layers crashed during its client entrypoint because its embedded TRansition library was not present as a remapped top-level development mod.

The supplied SkinLayers3D JAR includes both TRansition and TRender in `META-INF/jars/`. The Gradle development harness now flattens embedded dev libraries into `build/dev-mods-nested/` and feeds them through `modLocalRuntime` so Loom can remap them for the named development namespace.

Local retest is required before marking the optional stack compatible.


## SPIKE-01 runtime verification

**Result: PASS**

Local Windows IntelliJ/Gradle test confirmed:

- Minecraft 1.21.11 development client launches successfully;
- Loom Studios static test cape renders on the local player;
- vanilla cape geometry and movement are preserved;
- the cape is visibly correct in third person;
- Sodium, Sodium Extra, Iris, and 3D Skin Layers were present in the development runtime;
- no visible conflict was observed during the initial test.

SPIKE-01 is now considered DONE for the initial runtime proof.

Next active milestone: **SPIKE-02 — dynamic runtime cape texture**.


## SPIKE-02 implementation

**Status: COMPILE VERIFICATION PENDING**

The static resource cape has been replaced in source by an in-memory dynamic cape proof:

- one 64x32 NativeImage;
- one registered DynamicTexture;
- stable texture Identifier;
- vanilla cape renderer still owns geometry/motion;
- procedural LS/checker design changes every 40 client ticks;
- GPU upload occurs only when the image changes;
- resource release is registered for client shutdown;
- no network traffic is involved.

Expected local behavior after pulling: the cape visibly changes palette/highlight roughly every two seconds without reconnecting or restarting.


## SPIKE-02 runtime verification

**Result: PASS**

User local runtime evidence confirms:
- dynamic cape colors change while the game remains running;
- no reconnect/resource reload is required;
- vanilla cape movement remains intact;
- the full optional development stack remains stable.

An Elytra was also equipped during the test. Minecraft rendered the animated cape texture on the Elytra, revealing the vanilla fallback rule: when the player's dedicated Elytra texture is absent, WingsLayer uses the player's cape texture if the cape is visible.

This discovery directly validates the need for separate cape/Elytra channels in Loom Studios.

## SPIKE-03 implementation

**Status: IMPLEMENTED / CI + LOCAL RUNTIME VERIFICATION PENDING**

The render-state patch now supplies a distinct Elytra texture in addition to the dynamic cape texture.

The SPIKE-03 test Elytra:
- uses its own runtime DynamicTexture;
- does not inherit cape animation;
- uses the vanilla Elytra model/animation;
- makes the edge-face UV strips transparent while leaving the major front/back wing faces opaque, as a low-risk experiment to reduce the boxy appearance without replacing vanilla geometry.


## Elytra thickness direction

The transparent edge-face experiment made the Elytra visually paper-thin. That confirms texture alpha is the wrong control for user-adjustable thickness.

Product direction:
- default Elytra geometry/appearance uses vanilla thickness;
- texture controls artwork only;
- an optional Loom Studios geometry setting controls visual wing thickness independently;
- the setting is cosmetic/render-only and does not affect hitboxes or gameplay.

Implementation target:
- 100% = vanilla Elytra thickness;
- thinner/thicker values scale the Elytra model on its local depth axis;
- preserve vanilla animation/pose logic;
- keep the feature optional and fall back to vanilla geometry if compatibility requires it.


## Elytra geometry-thickness proof

**Status: IMPLEMENTED / VERIFICATION PENDING**

Default dedicated Elytra UVs are opaque again so 100% thickness retains vanilla volume.

A client-only ElytraModel hook now applies Loom-specific Z-depth scaling while retaining vanilla wing width, height, pivot, rotation, and gliding animation.

Temporary development key:
- V cycles 100%, 75%, 50%, 25%, 150% thickness.

Non-Loom Elytras are reset to 100% every setup call to avoid state leaking through reused model instances.


## SPIKE-03 runtime verification

**Result: PASS**

Local runtime verification confirmed:
- dedicated Loom Elytra texture renders correctly;
- cape and Elytra textures are independent;
- vanilla Elytra animation/gliding remains intact;
- geometry-depth scaling works at the debug presets;
- returning to 100% restores vanilla thickness;
- the optional development stack remains stable.

The Elytra Thickness feature is now approved for the future Elytra editor. The temporary V-key preset cycler remains developer-only scaffolding until the editor control replaces it.

Next active milestone: **SPIKE-04 — live GUI player preview**.


## SPIKE-04 implementation

**Status: IMPLEMENTED / CI + LOCAL RUNTIME VERIFICATION PENDING**

A first functional Loom Studios player-preview screen now exists.

Temporary development controls:
- P opens the preview screen;
- left-drag rotates the player;
- mouse wheel zooms;
- C toggles Cape / Elytra preview;
- R resets camera;
- Esc closes.

Architecture:
- uses Minecraft's render-state entity submission path;
- extracts a player render state without changing the real entity;
- modifies only the extracted preview state to simulate no chest item or an Elytra;
- therefore Cape/Elytra mode switching does not equip/unequip anything in the actual world;
- the normal Loom Studios cape/Elytra render-state mixin still supplies the same runtime cosmetic textures used in-world.

The screen is intentionally plain and is not a visual reference implementation yet.


## SPIKE-04 runtime verification

**Result: PASS**

Exact implementation SHA: `faca3953ef07c9a2755bd0619963a59712cae548`  
GitHub Actions run #17: **SUCCESS**

Local runtime screenshots confirm:
- preview screen opens correctly;
- real player skin/model renders;
- Cape mode shows the Loom cape;
- Elytra mode shows the dedicated Loom Elytra;
- preview-only Cape/Elytra switching works;
- rotation and zoom controls work;
- actual world equipment is not changed.

Observed UX refinement:
- the player head follows the preview rotation more than desired;
- this is not a blocker for the render-state architecture and is deferred to the next bundled preview/UI refinement pass.

SPIKE-04 is DONE.

Next active milestone: **SPIKE-05 — multiplayer synchronization**.


## SPIKE-05 implementation

**Status: IMPLEMENTED / CI + TWO-CLIENT RUNTIME VERIFICATION PENDING**

The multiplayer proof now implements:
- versioned C2S/S2C Fabric payloads;
- graceful `canSend` detection for servers without Loom Studios;
- per-client proof project with SHA-256 content identity;
- server cache-miss request before upload;
- strict project byte/hash/schema validation;
- server-side project cache;
- authoritative equipped-project hash per player;
- equipped-state broadcast;
- client cache-miss project request;
- remote project cache and remote player rendering;
- project data contains parameters, not rendered animation frames;
- disconnect unequip broadcast;
- server-stop cache cleanup.

Two additional Loom run profiles exist:
- Loom Studios - Client A
- Loom Studios - Client B

They use separate run directories and deterministic development identities.

## SPIKE-06 implementation

**Status: IMPLEMENTED / CI + VISUAL/SHADER VERIFICATION PENDING**

The animation/emissive proof now uses:
- deterministic animation phase from world game time + project animation period;
- no frame networking;
- generated emissive mask per compiled project;
- Fabric player feature-render registration;
- second PlayerCapeModel submission using entityTranslucentEmissive;
- base cape remains on vanilla CapeLayer;
- G debug key toggles the emissive pass for A/B comparison;
- remote projects use the same local animation/effect evaluation path.


## SPIKE-05 compile correction

Initial combined implementation SHA `9312856fc1449895b3b973371a1a0b566ed85a2f` failed common compilation only because `ServerPlayer#getServer()` is not exposed in the Minecraft 1.21.11 Mojang-mapped API.

The implementation now obtains the server through `ServerPlayer.level().getServer()`. Networking architecture is unchanged.


## SPIKE-05/06 CI checkpoint

Exact SHA `f33c8adf3baa7cfdd7f6a4d1e7fd28e15c0a357c` passed GitHub Actions run #20, including full Java 21/Minecraft 1.21.11 build and artifact upload.

A small follow-up hardens the client handshake by deferring HELLO until player/level state exists if Fabric's JOIN callback arrives before the local entity is ready.

Manual verification remains the completion gate.


## SPIKE-06 alignment hardening

The emissive cape layer now mirrors vanilla CapeLayer's chest-equipment WINGS suppression and HUMANOID armor translation. This keeps the optional glow mask aligned with the base cape instead of drifting when armor is worn.


## Latest combined SPIKE-05/06 implementation checkpoint

Exact SHA `20451c816ecf9f8f806968c2663f462d161e659c` is the latest fully green implementation checkpoint.

GitHub Actions run #23:
- wrapper verification: PASS
- full Java 21 / Minecraft 1.21.11 build: PASS
- mod artifact upload: PASS

This checkpoint includes:
- multiplayer project/hash synchronization proof;
- remote-player runtime cosmetic rendering;
- deterministic local animation;
- emissive cape feature pass;
- vanilla armor/wing alignment rules for the emissive layer;
- Client A / Client B run profiles;
- Windows-safe concurrent optional-dev-mod handling.

SPIKE-05 and SPIKE-06 remain **runtime verification pending**, not DONE.


## 2026-10-01 multiplayer runtime feedback

**SPIKE-05 primary path: PASS**

Two-client LAN runtime evidence confirms:
- distinct Client A / Client B Loom projects render simultaneously;
- remote project transfer/cache succeeds;
- both projects animate/color-cycle correctly;
- no visible multiplayer failure was observed in the main path.

Negative-path/disconnect checks remain useful release-hardening coverage but no longer block the project-core transition.

## Elytra visual-thickness correction

Runtime feedback showed that the raw vanilla Elytra model depth looks noticeably too chunky when Loom Studios paints all edge UV faces opaquely.

Correction implemented:
- non-Loom Elytras stay at Minecraft raw zScale 1.0;
- Loom user-facing 100% thickness now maps to a calibrated 0.5 raw Z-depth scale;
- user-facing presets remain relative (100/75/50/25/150%);
- Elytra edge UVs are darkened instead of neon-bright, reducing the exaggerated boxed edge;
- texture and geometry controls remain independent.

This is a visual-baseline correction, not a gameplay/hitbox change.

## Phase 1 — Project Core bootstrap

The disposable tiny ProofProject is no longer the active multiplayer/runtime model.

Implemented first real project-core slice:
- versioned LoomProject schema v1;
- separate cape and Elytra canvases;
- immutable paint layers;
- visibility, opacity, emissive flag and blend-mode field;
- deterministic bounded binary .loom codec;
- SHA-256 content identity;
- server/client validation uses the real project codec;
- runtime texture compiler flattens project layers;
- multiplayer cache transfers real LoomProject bytes;
- local .loom project library filesystem foundation;
- undo/redo ProjectHistory foundation;
- development project factory now produces real schema-v1 projects.

The old ProofProject source remains temporarily in-tree only as dead spike history and can be removed after the new runtime path is locally reverified.


## Latest Phase-1 green checkpoint

Exact SHA `66695ffc66458c675654746c473809e9a13f1488` passed GitHub Actions run #27.

Verified in the normal `build` lifecycle:
- common/client compilation;
- remapped production and sources JARs;
- schema-v1 project-core JUnit tests;
- deterministic encode/decode/hash;
- deep immutable layer equality;
- project-history behavior;
- artifact upload.

The obsolete ProofProject class is now removed. Development multiplayer/runtime data uses LoomProject end-to-end.

Local verification still required for the newly calibrated Elytra 100% visual baseline.


## Elytra calibrated baseline runtime verification

**Result: PASS**

Local visual feedback confirms the calibrated Loom Elytra default now looks good. The user-facing 100% baseline (raw model Z scale 0.5 for Loom-owned wings) is accepted as the default.

## Phase 1 — project session/persistence slice

Implemented:
- project timestamps via LoomProjectMetadata;
- explicit schema-loader/migration dispatch through LoomProjectMigrations;
- pure ProjectFileStore with bounded safe-root loading and atomic-save fallback;
- ProjectSession owning ProjectHistory, revision, dirty state and source path;
- edits automatically update modified timestamp;
- undo/redo participate in dirty-state tracking;
- save establishes the persisted hash baseline;
- load starts clean;
- LocalProjectLibrary now binds the pure file store to Minecraft's game directory.

Schema v1 remains pre-release. There are no historical migrations yet, but future versions must be routed explicitly rather than silently reinterpreted.


## Phase-1 session/persistence green checkpoint

Exact SHA `3de9c581c212ac390f699841dc844c6478d3927e` passed GitHub Actions run #29.

Green scope:
- project metadata timestamps;
- explicit schema migration/load gate;
- ProjectFileStore safety and atomic-save fallback;
- ProjectSession dirty/save/load lifecycle;
- revision and undo/redo ownership;
- expanded automated project-core tests;
- production/common/client build and artifact upload.


## Phase 1 — live workspace/runtime-cache/library slice

Implemented:
- ClientProjectWorkspace now owns the live local ProjectSession;
- local rendering and networking read the same session/project/hash;
- multiplayer automatically republishes when the workspace project hash changes;
- GPU DynamicTexture ownership moved to RuntimeCosmeticCache;
- PlayerCosmeticRenderer replaces the old DynamicCosmeticSpike facade;
- project library now has ProjectDescriptor entries sorted by modified time;
- saved projects receive cached compiled-cape PNG thumbnails;
- unreadable/corrupt .loom files are isolated and skipped during library indexing;
- project save refreshes the Recent Projects index.

This removes the largest remaining duplicate local-project state before the editor starts mutating projects.


## Workspace/edit-runtime hardening

Before Phase 2, two editor-critical ownership issues were addressed:

- a loaded project remains the active workspace when a player identity is bound;
- changing the live project hash releases the previous local GPU runtime bundle and invalidates the local patched skin.

LoomProjectFactory can now create true blank editable projects with transparent cape/Elytra base layers and real timestamps.


## Phase 1 — edit session vs equipped-state separation

The live workspace now distinguishes:
- **editing project**: mutable through ProjectSession/history and allowed to be dirty;
- **equipped project**: saved immutable project snapshot used by world rendering and multiplayer.

Consequences:
- unsaved editor changes no longer automatically alter the player's visible/networked cape;
- `equipCurrent()` requires a clean/saved session;
- `saveAndEquip()` is the explicit convenience path;
- ClientCosmeticSync publishes the equipped hash, not the dirty editing hash;
- PlayerCosmeticRenderer renders the equipped project in-world;
- WorkspaceState/listeners provide a live change-notification surface for Phase-2 widgets.

The existing development project is still auto-equipped at first bootstrap so current runtime tests remain visible.


## Phase 1 — unsaved preview + library-state refinement

Implemented:
- reusable LoomPlayerPreviewScreen replaces the SPIKE-04 class name;
- preview render-state extraction can temporarily override the local player's cosmetic with the dirty ProjectSession project;
- world/multiplayer rendering remains on the saved/equipped snapshot;
- preview runtime bundles are released when replaced/closed;
- Elytra preview thickness resolves from the preview project's runtime settings instead of the equipped debug preset;
- project library index now owns stable selection state for the future Recent Projects UI;
- stale thumbnail files for older hashes of the same project are pruned when a new thumbnail is generated.

This completes the core editing/equipped/preview separation needed by the Phase-2 canvas.


## Phase 2 — Cape Editor MVP first interactive slice

Implemented:
- L development key opens the first Loom Studios home screen;
- Create New Cape creates a real blank ProjectSession;
- Recent Projects are sourced from ProjectLibraryIndex;
- selected saved project can be opened into the editor;
- first reusable LoomButton and LoomCanvasWidget controls;
- real 64x32 cape canvas renders the compiled ProjectSession;
- Pencil and Eraser edit the first non-emissive cape layer;
- four starter palette colors;
- undo/redo;
- Save;
- Save + Equip;
- 3D Preview returns to the editor when closed;
- editor title/status reflects dirty/equipped state;
- ProjectEdits provides immutable common-layer pixel editing.

The visual shell is intentionally only an MVP. Reference-image fidelity remains a later dedicated phase, but the editor is now operating on the real project/session/render/network architecture.


## Phase-2 first editor CI checkpoint

Exact SHA `c94e5024d5adc7b4bcfa7da77d653986c1cd5a33` passed GitHub Actions run #37.

Verified by CI:
- common/client compilation;
- Phase-1 project tests;
- Phase-2 editor/widget compilation;
- narration-capable LoomButton;
- remapped mod artifact;
- artifact upload.

The first interactive Cape Editor now requires local runtime/UX verification.


## Phase-2 paint-clarity correction from first runtime screenshots

First local editor test passed functionally but exposed two UX/behavior issues:

1. blank projects inherited the development spike hue-cycle setting, so user-painted colors animated unexpectedly;
2. presenting the entire 64x32 texture atlas as the main canvas made it unclear which pixels represented the visible cape face.

Corrections implemented:
- normal editor-created projects now use static runtime defaults (hue cycle OFF, emissive OFF);
- development bootstrap projects retain their animated proof behavior;
- introduced canonical CapeUvRegion mapping;
- editor now defaults to the real 10x16 Outside / Back face;
- face editor scales that region to fill the workspace;
- strong per-pixel grid;
- hover outline;
- local pixel + atlas UV coordinate readout;
- Face button cycles Outside, Inside, Left Edge, Right Edge, Top and Bottom;
- ProjectEdits can edit local face coordinates without exposing UV math to the UI.

The 64x32 atlas remains the underlying source of truth; the editor now presents semantic cape faces instead of raw atlas space by default.


## Cape face-first editor green checkpoint

Exact SHA `475355119f491a631de8a1b5311e5c0e9f1bbdb4` passed GitHub Actions run #39.

Verified:
- static editor runtime defaults;
- CapeUvRegion model;
- face-first editor widget;
- project edit mapping tests;
- client compilation;
- remapped artifact;
- artifact upload.

Second local UX verification is now the gate.


## Phase-2 high-resolution canvas + color/brush controls

Implemented:
- cape canvas supports 1x (64x32), 2x (128x64), and 4x (256x128);
- semantic face dimensions scale with the atlas (Outside becomes 10x16, 20x32, 40x64);
- resolution changes resample every cape layer non-destructively through ProjectSession/undo;
- common ProjectResizer supports Elytra canvases too, ready for the Elytra editor;
- RuntimeCosmeticCache now creates NativeImages at each project's actual cape/Elytra dimensions, so high-resolution projects reach the live 3D/world renderer at native detail;
- project blob transport uses Fabric 1.21.11 large-payload registration for high-resolution project bytes;
- project serialized bound raised to 1 MiB;
- brush size control added and brush edits affect a circular multi-pixel area;
- reusable LoomColorPickerWidget added with saturation/value square, hue strip, live swatch, hex readout, RGB channel sliders/values, and palette swatches.

Supported high-resolution sizes are intentionally bounded to 4x for the first editor release to keep project/network/memory costs predictable.


## High-resolution/color-control green checkpoint

Exact SHA `f8b477568628a45120c5a31dcc54c80633effe06` passed GitHub Actions run #42.

Green scope:
- 1x/2x/4x cape canvas resolutions;
- dynamic-size runtime NativeImages for cape and Elytra;
- Fabric large-payload project transport;
- scaled semantic cape-face UV editing;
- brush sizing and circular multi-pixel brush;
- HSV-style color picker;
- RGB sliders with numeric values;
- hex display;
- palette swatches;
- project-core automated tests and artifact upload.

Local visual verification is now the gate.


## High-resolution performance + GUI-scale layout correction

After local 2x/4x testing, the editor hot path was reworked:
- semantic face preview is GPU-texture cached per ProjectSession revision;
- active-face compilation avoids compiling the unused atlas area;
- high-res canvas rendering no longer emits one checker/color/grid rectangle per texel every frame;
- drag strokes are compound history edits;
- dirty/equipped UI checks avoid repeated full-project hashing.

The right-side tool rail is now a ScrollableLayout. This specifically addresses 1920x1080 at GUI scale 3 while preserving the already-good 3440x1440 scale 2/3 layouts.


## Phase-2 custom palette library/window

Implemented:
- persistent user-created ColorPalette model;
- named palettes with up to 32 colors;
- local palette library under `loom-studios/palettes/`;
- movable floating Palettes window;
- Pin state locks window position;
- palette selection list;
- click palette color to make it the active paint color;
- right-click palette color to remove it;
- Add Current Color;
- create/rename/save palette;
- export to `.loompalette` file;
- export also copies a compact `LOOMPAL1:` share code;
- Import accepts a share code from clipboard;
- Import fallback scans `loom-studios/palettes/imports/`;
- palette export files land in `loom-studios/palettes/exports/`;
- Palettes button includes a four-swatch palette icon and sits inside the scrollable tool rail.

Palette changes synchronize back into the main HSV/RGB color picker.
