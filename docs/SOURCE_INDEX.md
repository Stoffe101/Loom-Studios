# Loom Studios — Source Index

2026-10-03 visual correction: source3ed5c7807e469491423a9740d87bce962db7f297 passes125 tests/full190 captures and36 fast decoded captures. Visual review found duplicated native/vector preview headings and excessive label clipping (100%, Manage, Moonlit and compact navigation). Corrected by preserving opaque native-fill occlusion, using9-point labels with modest width allowance, and measuring button labels before choosing full text or icon-only. Home now has six responsive action cards, compact descriptive copy and a Help shortcut. Timeline glyph controls use proper SVGs/hover states. A separately composed wide courtyard avoids a stone-only crop in compact previews. These corrections require a new exact-source build/capture review; fast verification now also covers all48 library/layer/menu profile states and six compact task states. Full automated workflows remain unchanged; optional mods/shaders and hardware frame times remain unverified.

2026-10-03 premium migration follow-up: source eb839ce44681d409fa2107c77b1021f2a03c194f passes Java21/125 tests and full190 actual-client captures (Actions223/run37137908432). Fast run37137908200 passes20 production and8 renderer captures; production images decode and were inspected. Smooth actual button/card controls work; no hardware FPS claim. New source adds resource-backed generated workshop frame and courtyard scenery, smooth studio labels,48 SVG icons including visibility/locks, popup occlusion clips, and stricter cache probes requiring real submitted frames while paint/upload counts stay stable. This new source needs its own build/capture acceptance. Pixel artwork, actual 3D models, input/narration, text fields and tooltips retain Minecraft ownership. Other scripts fall back to native font providers; Inter uses a conservative Latin/Greek/Cyrillic/punctuation subset. Runtime reload, optional mods/shaders and hardware frame-time testing remain TODO. The workspace execution service became unresponsive during the pass; subsequent changes are saved directly to the existing GitHub draft branch using reviewed source transformations and will be compiled/captured by CI.

Next: verify this source's125 tests,190 full captures and fast36 production/editor/renderer captures; inspect every required profile and popup/layer layout. Correct artwork stretching or text clipping before production acceptance/merge. Reference targets: all five approved screens, with semantic Home/Cape references guiding frame/materials, typography and preview scene.

2026-10-03 production presentation migration: Fabric ScreenEvents beforeRender/afterRender (verified from fabric-screen-api-v1 sources3.1.7+4ebb5c083e shipped with API0.141.1) batches actual button/action-card paint into one transparent PIP overlay. Existing widget input/narration is retained; immutable state comparisons invalidate only on interaction/geometry/text changes. Popups remove covered earlier commands. NanoVG/Inter/Lucide foundation source82ad78a passes125 tests/eight fast captures; new production migration verification is pending. Forty-three Lucide assets plus three Loom-specific SVGs are bundled with license notices; flip-horizontal/flip-vertical/trash-2 come from Lucide0.468.0, other assets from upstream main. Source: https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-screen-api-v1/3.1.7+4ebb5c083e/fabric-screen-api-v1-3.1.7+4ebb5c083e-sources.jar. Full workshop skin/remaining typography and hardware/shader acceptance remain unfinished.

2026-10-03 implementation spike: Java/LWJGL NanoVG PIP integration derived from NVGRenderer f4e8272, bundled Inter/Lucide notices, isolated F9 comparison, conventional widget input and CPU submission instrumentation. Template/picker follow-up uses a32-entry UI texture cache with dimension/hue/template keys and resource disposal. Initial source71d3d02 Java21/125 tests pass; new follow-up and runtime capture acceptance remain IN PROGRESS. See PREMIUM_PROTOTYPE.md. No Kotlin, owo-ui or production-screen renderer migration adopted yet.

2026-10-03 follow-up research: NanoVG through NVGRenderer explicitly targets Fabric1.21.11 and is now a rendering-spike candidate for smooth shapes/text/SVGs; Kotlin/native packaging and actual Loom compatibility remain untested. Next compare a small NanoVG prototype with cached Fabric assets before adoption; owo-ui layout evaluation is separate. YACL is settings-oriented and ImGui is developer-tool-oriented. No dependencies/code added or runtime tests run. See PREMIUM_UI_AUDIT.md for primary sources and requirements.

2026-10-03 premium UI research: version-specific Fabric GUI docs, Lucide ISC/Feather MIT sources, Inter OFL1.1, official owo-ui docs/release0.13.0+1.21.11 and ModernUI compatibility matrix are recorded with URLs and adoption limits in PREMIUM_UI_AUDIT.md. No third-party assets or dependencies added yet.

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
