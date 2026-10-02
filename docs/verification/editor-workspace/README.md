# Editor workspace verification — 2026-10-02

These are actual Minecraft 1.21.11 screenshots, rendered with Fabric Loader 0.18.4 / Fabric API 0.141.1+1.21.11 / Java 21.0.9 and an isolated flat test world. No optional mods or shader packs were installed. They are not image-generation mockups.

| Display profile | Cape | Elytra | Inspection |
| --- | --- | --- | --- |
| 1920×1080 / GUI 2 | [Cape](cape-1920x1080-gui2.png) | [Elytra](elytra-1920x1080-gui2.png) | PASS |
| 1920×1080 / GUI 3 | [Cape](cape-1920x1080-gui3.png) | [Elytra](elytra-1920x1080-gui3.png) | PASS |
| 3440×1440 / GUI 2 | [Cape](cape-3440x1440-gui2.png) | [Elytra](elytra-3440x1440-gui2.png) | PASS |
| 3440×1440 / GUI 3 | [Cape](cape-3440x1440-gui3.png) | [Elytra](elytra-3440x1440-gui3.png) | PASS |

Compact pages inspected: Cape Color, Properties, Gradient, Transform, Stops; Elytra Color, Properties, Animation, Playback. Long layer and track collections remain bounded with visible scrollbars. The initial runtime captures revealed hidden playback/thickness values and incorrect wing thumbnails; both were corrected and recaptured. Early screenshots with the tutorial toast were replaced by clean captures.

A production-widget single-pixel selection was exercised during dragging and after release at 200% zoom. The earlier run measured identical cyan bounds `(758,588)–(846,676)` in both screenshots. One later committed PNG was truncated when the execution environment disconnected; that invalid file was removed. The capture CI job regenerates the pair and the below-minimum window guidance, and the logs must report completion before acceptance.

Approved style targets are the five images under `../../references/ui/`. The repair follows their canvas-first hierarchy, navy surfaces, cyan selection, violet animation accents, timber/steel shell, icon rail and right preview/inspector. Intentional differences: native Minecraft font, simpler pixel icons and paged compact properties. This evidence establishes editor bounds and visual clarity; it does not claim full decorative fidelity, Home/Import/Share acceptance, shader compatibility or complete manual workflow acceptance.

Run `./gradlew runClient -PuiCapture` to regenerate evidence. This explicitly creates test data under ignored `run-ui-capture/`. Packaged clients and ordinary development launches never run capture automation.
