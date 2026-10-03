# Workshop UI verification — 2026-10-02

These 42 PNGs are actual Minecraft screenshots from source `58491bc8855f854f2f1902e32f1720cb3e5e3274`, tested merge `3de85d41980e1d28d78e429749147031a41766d1`, [Actions #182](https://github.com/Stoffe101/Loom-Studios/actions/runs/37073091623). Both clean build/tests and real capture jobs passed. PNGs were losslessly optimized without changing pixels; all decode completely.

Environment: Minecraft 1.21.11, Loader 0.18.4, API 0.141.1+1.21.11, Temurin Java 21.0.12+1, Loom 1.17.21, Gradle 9.6.1, clean Ubuntu/Mesa llvmpipe/Xvfb. No optional mods/shaders. Captures use an isolated flat development world and real deterministic Moon artwork/projects. They are not generated UI mockups or production template additions.

| Profile | Home | Cape | Elytra | Share | Smart Import | Inspection |
| --- | --- | --- | --- | --- | --- | --- |
| 1920x1080 / GUI 2 | [Home](home-1920x1080-gui2.png) | [Cape](cape-1920x1080-gui2.png) | [Elytra](elytra-1920x1080-gui2.png) | [Export](share-export-1920x1080-gui2.png) / [Import](share-import-1920x1080-gui2.png) | [Placement](smart-import-placement-1920x1080-gui2.png) / [Processing](smart-import-processing-1920x1080-gui2.png) | PASS |
| 1920x1080 / GUI 3 | [Home](home-1920x1080-gui3.png) | [Cape](cape-1920x1080-gui3.png) | [Elytra](elytra-1920x1080-gui3.png) | [Export](share-export-1920x1080-gui3.png) / [Import](share-import-1920x1080-gui3.png) | [Placement](smart-import-placement-1920x1080-gui3.png) / [Processing](smart-import-processing-1920x1080-gui3.png) | PASS |
| 3440x1440 / GUI 2 | [Home](home-3440x1440-gui2.png) | [Cape](cape-3440x1440-gui2.png) | [Elytra](elytra-3440x1440-gui2.png) | [Export](share-export-3440x1440-gui2.png) / [Import](share-import-3440x1440-gui2.png) | [Placement](smart-import-placement-3440x1440-gui2.png) / [Processing](smart-import-processing-3440x1440-gui2.png) | PASS |
| 3440x1440 / GUI 3 | [Home](home-3440x1440-gui3.png) | [Cape](cape-3440x1440-gui3.png) | [Elytra](elytra-3440x1440-gui3.png) | [Export](share-export-3440x1440-gui3.png) / [Import](share-import-3440x1440-gui3.png) | [Placement](smart-import-placement-3440x1440-gui3.png) / [Processing](smart-import-processing-3440x1440-gui3.png) | PASS |

All five approved references under `../../references/ui/` were inspected and compared with every profile. Accepted: timber/steel/lantern/cloth/wordmark framing, navy panel hierarchy, inset cyan/violet emphasis, scenic live preview, parchment footer, source/result/export previews and fixed actions. Native fonts, pixel artwork and paged compact inspectors intentionally adapt the reference style to Minecraft.

Every visible widget passes window/footer bounds and pairwise nonoverlap. Compact Cape Color/Properties/Gradient/Transform/Stops and Elytra Color/Properties/Animation/Playback pages pass; long layer/track collections retain bounded scrollbars. Some editor images show expected keyboard-focus tooltips; they are transient overlays, not displaced controls. The shorter Home gesture hint fits inside the narrow preview.

The production-widget one-pixel selection remains `(5,8)` through release at GUI 3 / 200% zoom. The canvas-body crop `(114,330)–(1284,910)` is pixel-identical in the [live](cape-single-pixel-live-200percent.png) and [committed](cape-single-pixel-committed-200percent.png) captures. [Below-minimum guidance](cape-small-window-guidance.png) is readable.

Runtime checks also pass: editable/portable export hash round-trips, Cape/Elytra PNG dimensions, and both Smart Import pages apply an Image layer with pixels equal to the compiled candidate preview. Workflow/completion markers each occur once. Optional-mod/shader/multiplayer, OS picker/clipboard and exhaustive manual feature interactions were not exercised.

Regenerate with `./gradlew runClient -PuiCapture`; the opt-in development runner owns ignored `run-ui-capture/` test data. Packaged clients and ordinary development launches never run automation.
