# Editor workspace verification — 2026-10-02

These are actual Minecraft 1.21.11 screenshots, rendered with Fabric Loader 0.18.4 / Fabric API 0.141.1+1.21.11 / Java 21.0.12+1 on clean GitHub Actions / Mesa and an isolated flat test world. No optional mods or shader packs were installed. They are not image-generation mockups.

| Display profile | Cape | Elytra | Inspection |
| --- | --- | --- | --- |
| 1920×1080 / GUI 2 | [Cape](cape-1920x1080-gui2.png) | [Elytra](elytra-1920x1080-gui2.png) | PASS |
| 1920×1080 / GUI 3 | [Cape](cape-1920x1080-gui3.png) | [Elytra](elytra-1920x1080-gui3.png) | PASS |
| 3440×1440 / GUI 2 | [Cape](cape-3440x1440-gui2.png) | [Elytra](elytra-3440x1440-gui2.png) | PASS |
| 3440×1440 / GUI 3 | [Cape](cape-3440x1440-gui3.png) | [Elytra](elytra-3440x1440-gui3.png) | PASS |

Compact pages inspected: Cape Color, Properties, Gradient, Transform, Stops; Elytra Color, Properties, Animation, Playback. Long layer and track collections remain bounded with visible scrollbars. The initial runtime captures revealed hidden playback/thickness values and incorrect wing thumbnails; both were corrected and recaptured. Early screenshots with the tutorial toast were replaced by clean captures.

A production-widget single-pixel selection was exercised during dragging and after release at GUI 3 / 200% zoom. The outline region `(690,590)–(780,680)` is pixel-identical in the live and committed captures. The below-minimum window capture shows readable resize/GUI-scale guidance. All 22 PNGs were regenerated and the completion marker was verified in [Actions #178](https://github.com/Stoffe101/Loom-Studios/actions/runs/37068918058), source `a2a9628afced367d4a178f944c5cce239d110fbf`, tested merge `ed4b04f900dc200eb43fa6074fecd5d6a832e084`. Both build and real screenshot jobs passed. These PNGs replace the earlier local set.

Approved style targets are the five images under `../../references/ui/`. The repair follows their canvas-first hierarchy, navy surfaces, cyan selection, violet animation accents, timber/steel shell, icon rail and right preview/inspector. Intentional differences: native Minecraft font, simpler pixel icons and paged compact properties. This evidence establishes editor bounds and visual clarity; it does not claim full decorative fidelity, Home/Import/Share acceptance, shader compatibility or complete manual workflow acceptance.

Run `./gradlew runClient -PuiCapture` to regenerate evidence. This explicitly creates test data under ignored `run-ui-capture/`. Packaged clients and ordinary development launches never run capture automation.
