# Premium UI acceptance and test handoff

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

## Next manual test

Use the normal studio screens, not the F9 renderer sample. At each required profile, edit a multi-layer cape and elytra, open the palette, zoom and middle-drag both canvas and expanded preview, hover compact tool icons, save and equip, and reopen the design. Check the library menu, Trash restore and version restore with disposable designs. Compare idle and active-edit frame times with the previous build on the same hardware/world/settings.

Then verify Windows/macOS native loading, resource reload, repeated screen changes/resizing and optional Sodium/Iris/shaders. Report screen, resolution/GUI scale, mod list and frame-time evidence for any regression. More work should follow observed issues: first responsiveness/compatibility, then smoother native text-field styling and richer ancillary task-page composition.
