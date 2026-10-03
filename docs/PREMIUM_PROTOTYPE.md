# Premium UI renderer and developer comparison

## Premium studio presentation — final tooltip correction IN PROGRESS (2026-10-03)

Runtime source: `dad8c7fa790c72705b82ace63fffe85ee3f7bc49`. [Build #226](https://github.com/Stoffe101/Loom-Studios/actions/runs/37141274120) passes both jobs: 125 tests, zero failures/errors/skips, and 190 actual Minecraft captures. [Comparison #5](https://github.com/Stoffe101/Loom-Studios/actions/runs/37141274080) passes all eight jobs: 90 decoded screenshots. Tested with Minecraft 1.21.11, Loader 0.18.4, Fabric API 0.141.1, Temurin Java 21, Linux/Xvfb/software Mesa; no Sodium/Iris/shaders.

Implemented: smooth Inter studio labels; 48 SVG icons (45 Lucide and three Loom-specific); rounded button/card hover, focus and selection; resource-backed timber/steel/lantern frame; portrait and wide courtyard scenes; six responsive Home actions and Help; readable compact navigation and layer percentages; native/vector occlusion; smooth timeline, project-menu and visibility/lock controls. Real pixel artwork, player/cape/elytra, native text fields, tooltips, input and narration retain Minecraft ownership. Other scripts retain native font fallback.

Verified profiles: 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3. Visual review inspected Home, Cape/Elytra, Smart Import, sharing, dense layers/groups, context menus, library organization/bulk/version screens and cool/cute templates across all four profiles. GUI 3 dense-layer assertions require four complete rows. Nine full-run idle cache assertions pass while submitted frames advance and paint/upload counts remain unchanged. Screen workflows pass: canvas/preview/expanded middle pan, palette close, alpha isolation, hash/skin reuse, import handles, project/portable/PNG import/export, animation drag/undo, library right-click/delete/Trash/double-click/recovery, unsaved safety, favorite ordering, bulk hide/undo and Equip.

Performance changes: one interaction-invalidated transparent NanoVG PIP surface, globally unique revisions, SVG/font resources loaded once, and a bounded 32-entry cache for template and color-picker textures. Continuous entity rendering remains separate. These submission/cache assertions establish reuse, not hardware FPS.

Reference targets: all five approved Home, Cape, Elytra, Smart Import and Sharing images. Intentional adaptations: existing project workflows remain available; compact profiles use abbreviated copy or icon-only controls with tooltips; template/user artwork stays crisp pixel art; the workshop scene is a generated resource texture rather than a world renderer. Provenance and licenses are in PREMIUM_ASSETS.md and packaged third-party notices.

Remaining manual verification: target hardware frame times and live editing responsiveness; Windows/macOS native loading; Sodium/Iris/shader combinations; resource reload and repeated screen/open-close memory use. Native input fields/tooltips and some ancillary task-page composition can receive a later visual polish pass. F9 is a labeled developer comparison with illustrative sample artwork, not an editor replacement.

## Renderer ownership

Fabric ScreenEvents beforeRender/afterRender collect actual widgets and labels into an immutable paint list. Geometry, copied pose transforms, content and interaction state determine revision changes. PremiumGuiRenderer draws through Fabric's registered special-element picture-in-picture path and reuses the surface at an unchanged revision. PremiumPaint owns NanoVG, Inter and rasterized SVG resources and frees them at client shutdown. Opaque native fills clip preceding commands through GuiGraphicsOcclusionMixin; later native panels/popups do not expose stale labels.

The adapter derives from the Unlicensed NVGRenderer commit f4e8272a83964a3760e6d8a739c70e8274cc14b7. Only LWJGL NanoVG 3.3.3 and platform natives are added; Minecraft owns LWJGL core. No Kotlin, webview, remote rendering API or additional UI framework is needed. Resource-backed frame/scene images are ordinary Minecraft textures.

F9 compares smooth/native controls at identical bounds. Its sample canvas/layers are illustrative; Open editor and layer manager route to the real workflows. CPU submission p50/p95 excludes GPU and entity/frame cost. The Live paint toggle is diagnostic, not the production policy.

## Sources and licenses

- Fabric 1.21.11 GUI rendering: https://docs.fabricmc.net/1.21.11/develop/rendering/gui-graphics
- ScreenEvents source shipped by Fabric API 0.141.1: https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-screen-api-v1/3.1.7+4ebb5c083e/fabric-screen-api-v1-3.1.7+4ebb5c083e-sources.jar
- LWJGL NanoVG API: https://javadoc.lwjgl.org/org/lwjgl/nanovg/package-summary.html
- Java PIP adapter upstream: https://github.com/Noamm9/NVGRenderer
- Inter, OFL 1.1: https://rsms.me/inter/
- Lucide, ISC/Feather MIT: https://lucide.dev/license

The first 12 controls and most later Lucide assets came from upstream main; flip, trash, visibility-off and open-lock additions use 0.468.0. The bundled files/notices are the durable source. owo-ui remains an unadopted layout option; official ModernUI 1.21.11 compatibility was not established. See PREMIUM_UI_AUDIT.md for the earlier research.

2026-10-03 final compact visual review: all six task screenshots were inspected from Comparison #6/run37141948813 at source75f7f59b (workflow-preview-only change; runtime equals dad8c7fa). Review found native tooltips partly occluded by the late vector overlay. The overlay now flushes at GuiGraphics.renderDeferredElements HEAD, before deferred tooltip submission into a higher stratum; afterRender remains a fallback and cannot submit twice. This small final runtime change requires fresh build/capture acceptance. API research: NeoForge migration primer https://docs.neoforged.net/primer/docs/1.21.9/ documents the renderDeferredElements rename; deferred tooltip strata: https://docs.neoforged.net/docs/1.21.8/gui/screens/. Exact 1.21.11 descriptor confirmed through mappings and CI validation is required.
