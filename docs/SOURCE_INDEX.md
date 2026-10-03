# Loom Studios — Source Index

## Premium studio presentation — DONE: UI/layout and Linux client verification (2026-10-03)

Runtime source: `778c2eea7f50ae46240a46fe506be7ceb8f2e238`. [Build #229](https://github.com/Stoffe101/Loom-Studios/actions/runs/37142515469) passes both jobs: 125 tests, zero failures/errors/skips, and 190 actual Minecraft captures. [Comparison #8](https://github.com/Stoffe101/Loom-Studios/actions/runs/37142515428) passes all eight jobs: 90 decoded screenshots. Tested with Minecraft 1.21.11, Loader 0.18.4, Fabric API 0.141.1, Temurin Java 21, Linux/Xvfb/software Mesa; no Sodium/Iris/shaders.

Implemented: smooth Inter studio labels; 48 SVG icons (45 Lucide and three Loom-specific); rounded button/card hover, focus and selection; resource-backed timber/steel/lantern frame; portrait and wide courtyard scenes; six responsive Home actions and Help; readable compact navigation and layer percentages; native/vector occlusion; smooth timeline, project-menu and visibility/lock controls. Real pixel artwork, player/cape/elytra, native text fields, tooltips, input and narration retain Minecraft ownership. Other scripts retain native font fallback.

Verified profiles: 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3. Visual review inspected Home, Cape/Elytra, Smart Import, sharing, dense layers/groups, context menus, library organization/bulk/version screens and cool/cute templates across all four profiles. GUI 3 dense-layer assertions require four complete rows. Nine full-run idle cache assertions pass while submitted frames advance and paint/upload counts remain unchanged. Screen workflows pass: canvas/preview/expanded middle pan, palette close, alpha isolation, hash/skin reuse, import handles, project/portable/PNG import/export, animation drag/undo, library right-click/delete/Trash/double-click/recovery, unsaved safety, favorite ordering, bulk hide/undo and Equip.

Performance changes: one interaction-invalidated transparent NanoVG PIP surface, globally unique revisions, SVG/font resources loaded once, and a bounded 32-entry cache for template and color-picker textures. Continuous entity rendering remains separate. These submission/cache assertions establish reuse, not hardware FPS.

Reference targets: all five approved Home, Cape, Elytra, Smart Import and Sharing images. Intentional adaptations: existing project workflows remain available; compact profiles use abbreviated copy or icon-only controls with tooltips; template/user artwork stays crisp pixel art; the workshop scene is a generated resource texture rather than a world renderer. Provenance and licenses are in PREMIUM_ASSETS.md and packaged third-party notices.

Remaining manual verification: target hardware frame times and live editing responsiveness; Windows/macOS native loading; Sodium/Iris/shader combinations; resource reload and repeated screen/open-close memory use. Native input fields/tooltips and some ancillary task-page composition can receive a later visual polish pass. F9 is a labeled developer comparison with illustrative sample artwork, not an editor replacement.

Final corrections: GuiGraphicsOcclusionMixin flushes the studio surface at renderDeferredElements HEAD before native tooltips enter their higher stratum. afterRender remains a guarded fallback. Non-overlapping opaque canvas pixels return before allocating/copying the vector command list. The compact tooltip captures were inspected, alongside final-source Home/import/share/editor contact sheets at all four profiles.

Verification evidence is attached to the exact runtime source above. The subsequent acceptance commit changes documentation only. Build artifacts are available as loom-studios-dev; screenshot artifacts are loom-editor-screenshots and loom-premium-* on the linked runs. CI emits JPEG contact sheets for review and retains original PNGs as artifacts.

Primary tooltip/render-order research: https://docs.neoforged.net/primer/docs/1.21.9/ (renderDeferredElements rename), https://docs.neoforged.net/docs/1.21.8/gui/screens/ (deferred tooltip strata), and exact Minecraft 1.21.11 method descriptors checked before compilation. Linux/X11 missing narrator/cursor-shape and offline Realms messages are capture-environment limitations; rendering/input assertions still pass. They do not establish optional-mod or cross-platform acceptance.

This file records external technical sources consulted for Loom Studios and the license/usage implications where relevant.

## Fabric documentation

### Fabric custom screens/widgets

Purpose:
- custom editor screens
- reusable interactive widgets
- GUI rendering/input/narration

Source:
- Fabric documentation: custom screens/widgets

Use:
- API/architecture reference

### Fabric networking

Purpose:
- custom payloads
- client/server protocol
- project synchronization

Source:
- Fabric documentation: networking

Use:
- API/architecture reference

### Fabric rendering concepts

Purpose:
- understand 1.21.x rendering-state direction
- avoid outdated raw-render assumptions

Source:
- Fabric documentation: rendering/basic concepts

Use:
- API/architecture reference

### Fabric automated testing

Purpose:
- GameTest/client tests
- CI strategy

Source:
- Fabric documentation: automatic testing

Use:
- test architecture reference

## Fabric Maven / API docs

Purpose:
- verify Loader/Fabric API/Loom artifacts
- inspect mapped Minecraft/Fabric APIs

Use:
- version/API reference

## Minecraft mappings

Purpose:
- inspect player rendering, skin texture, Elytra, and dynamic texture APIs

Preferred development direction:
- official Mojang mappings

Yarn may be consulted during research only when it provides easier public symbol browsing. Final source code should follow the mapping choice pinned by SPIKE-00.

## Sodium

Purpose:
- mandatory compatibility target

License:
- verify current upstream license before copying any code; Loom Studios should not require Sodium code reuse for its core implementation.

Use:
- runtime compatibility testing and public API/reference only if needed.

## Iris

Purpose:
- mandatory shader compatibility target

License:
- verify current upstream license before copying any implementation code.

Use:
- runtime compatibility testing and public API/reference only if needed.

## Existing cape/cosmetic mods

Purpose:
- observe modern Minecraft 1.21.x integration patterns and compatibility issues.

Rule:
- record the repository/license before copying or adapting implementation code;
- do not copy code whose license conflicts with Loom Studios proprietary licensing;
- behavioral observations and independent reimplementation are acceptable.

## Research update rule

When a new external source materially affects architecture:
1. add it here;
2. record what question it answered;
3. note license constraints;
4. update DECISIONS.md if the result changes architecture.


## Custom Capes by builtdoor1

Repository:
- `builtdoor1/Custom-Capes`

License:
- CC0 1.0 Universal

Question answered:
- confirmed a clean Minecraft 1.21.11 Mojang-mapped approach for applying a cape without replacing vanilla cape geometry;
- inject after `AvatarRenderer.extractRenderState`;
- patch only the cape slot on `PlayerSkin`;
- let vanilla `CapeLayer` retain movement, armor offsets, and Elytra suppression.

Loom Studios implementation:
- uses the same public 1.21.11 rendering seam;
- keeps its own names, project architecture, state model, and future dynamic-texture system;
- the consulted source is CC0, but the source is still recorded here for traceability.


## Fabric 1.21.11 networking documentation

Repository/source:
- `FabricMC/fabric-docs`
- 1.21.11 networking guide and reference examples

Questions answered:
- `CustomPacketPayload` record structure;
- `PayloadTypeRegistry.playC2S()/playS2C()`;
- `ClientPlayNetworking` and `ServerPlayNetworking` receiver/send shape;
- server-side validation expectations;
- tracking/networking architecture.

Use:
- API and architecture reference only.

## Minecraft 1.21.11 mapped/decompiled API inspection

Source inspected:
- `rrrRex1024/minecraft-1-21-11-source`

Classes inspected during the foundation spikes:
- `AvatarRenderer`
- `CapeLayer`
- `WingsLayer`
- `ElytraModel`
- `PlayerCapeModel`
- `InventoryScreen`
- `RenderTypes`
- player/entity render-state classes

Questions answered:
- cape/Elytra fallback behavior;
- vanilla cape armor/wing suppression offsets;
- Elytra model dimensions and thickness;
- GUI entity render-state submission;
- emissive render type availability.

Use:
- version-specific API behavior inspection and independent Loom Studios implementation.
- No Minecraft assets are copied into the Loom Studios source.

## Animation / schema v3

Common model:
- `src/main/java/dev/loomstudios/project/LoomAnimation.java`
- `src/main/java/dev/loomstudios/project/AnimationTrack.java`
- `src/main/java/dev/loomstudios/project/AnimationKeyframe.java`
- `src/main/java/dev/loomstudios/project/AnimationChannel.java`
- `src/main/java/dev/loomstudios/project/AnimationEffectType.java`
- `src/main/java/dev/loomstudios/project/AnimationAuthoring.java`
- `src/main/java/dev/loomstudios/project/AnimationEvaluator.java`

Client authoring/runtime:
- `src/client/java/dev/loomstudios/client/ui/LoomAnimationTimelineWidget.java`
- `src/client/java/dev/loomstudios/client/screen/ElytraEditorScreen.java`
- `src/client/java/dev/loomstudios/client/render/LoomTextureCompiler.java`
- `src/client/java/dev/loomstudios/client/render/RuntimeCosmeticCache.java`
- `src/client/java/dev/loomstudios/client/render/PlayerCosmeticRenderer.java`
- `src/client/java/dev/loomstudios/client/ui/LoomPlayerPreviewWidget.java`

Persistence:
- `src/main/java/dev/loomstudios/project/LoomProject.java`
- `src/main/java/dev/loomstudios/project/LoomProjectCodec.java`
- `src/main/java/dev/loomstudios/project/LoomProjectMigrations.java`

## Loom Codes / local sharing

Common project-transfer format:
- `src/main/java/dev/loomstudios/project/LoomProjectCode.java`

Client sharing/export:
- `src/client/java/dev/loomstudios/client/screen/LoomCodesScreen.java`
- `src/client/java/dev/loomstudios/client/sharing/LoomShareExportAdapter.java`

Related local project library:
- `src/client/java/dev/loomstudios/client/project/LocalProjectLibrary.java`
- `src/client/java/dev/loomstudios/client/project/ProjectLibraryIndex.java`
- `src/main/java/dev/loomstudios/project/ProjectFileStore.java`

Native project-file picker reuses LWJGL TinyFileDialogs, the same native dialog dependency already used by Smart Import.

### 2026-10-03 preview snapshot research

- https://docs.fabricmc.net/develop/rendering/basic-concepts — official extraction/submission rendering explanation; used to evaluate GUI snapshot ownership.
- https://maven.fabricmc.net/docs/yarn-1.21.11+build.1/net/minecraft/client/render/entity/EntityRenderer.html — official Fabric API mappings for fresh createRenderState/updateRenderState methods. Implementation uses the project’s Mojang-mapped createRenderState()/extractRenderState().
- https://github.com/neoforged/NeoForge/issues/2500 — upstream report of GUI state reuse in 1.21.6+ (not proof of identical 1.21.11 behavior); fresh snapshot avoids depending on that reuse contract. Runtime validation remains required.

### Preview pose state research (2026-10-03)

Fabric Yarn 1.21.11 API documents BipedEntityRenderState isGliding/leftWingPitch/Roll/Yaw and PlayerEntityRenderState glidingTicks. Mojang-mapped source uses Humanoid/Avatar equivalents; exact names and rendering behavior must pass compilation and captures before acceptance. Only isolated preview snapshots receive authored pose values; world state remains untouched.
- https://maven.fabricmc.net/docs/yarn-1.21.11%2Bbuild.6/net/minecraft/client/render/entity/state/BipedEntityRenderState.html
- https://maven.fabricmc.net/docs/yarn-1.21.11%2Bbuild.6/net/minecraft/client/render/entity/state/PlayerEntityRenderState.html

## 2026-10-03 — Follow-up API checks

Version-specific questions: suppress preview body without hiding cosmetic layers; match the Minecraft 1.21.11 wing-model API. Checked against the exact Mojang-mapped distribution compiled/run by repository CI: LivingEntityRenderer.getRenderType takes LivingEntityRenderState plus three booleans; ElytraModel is non-generic and accepts a baked ELYTRA ModelPart. Initial incorrect generic usage failed compile and was corrected. Acceptance requires actual client rendering, not signature assumptions. Capture assertions check unflagged world snapshots and animated Elytra emissive-mask selection. Runtime/reference source: the repository's Fabric Loom build uses official Minecraft 1.21.11 mappings; no third-party renderer dependency added.

### 2026-10-03 held-item preview API check

Exact Mojang-mapped MC1.21.11 client jar from repository Fabric Loom cache inspected: ArmedEntityRenderState exposes left/rightHandItemState and left/rightHandItemStack; ItemStackRenderState has clear/isEmpty; HumanoidRenderState exposes head/chest/legs/feet equipment; AvatarRenderState exposes heldOnHead, shoulder parrots, arrows/stingers. Clearing extracted snapshot fields avoids ItemInHand/armor/head feature leaks without touching player inventory. ScreenEvents.afterRender uses the established Fabric screen event API already used by PremiumControls. CI compilation/runtime remains required evidence.

- Fabric ScreenEvents lifecycle: https://maven.fabricmc.net/docs/fabric-api-0.136.0%2B1.21.8/net/fabricmc/fabric/api/client/screen/v1/ScreenEvents.html — screen callbacks register during initialization; implementation follows the existing AFTER_INIT pattern in PremiumControls. Initial constructor registration failed at runtime and was corrected.
