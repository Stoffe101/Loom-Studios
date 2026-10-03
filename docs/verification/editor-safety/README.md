# Editor safety and usability verification

Implementation source: `5c6b68d82e1b878e48872a3ddb881e26056232ae`, [Actions #204](https://github.com/Stoffe101/Loom-Studios/actions/runs/37097790670), [PR #17](https://github.com/Stoffe101/Loom-Studios/pull/17).

State: DONE for this implementation pass. Java 21 build and 117 tests pass, with zero failures/errors/skips. All 128 runtime captures decode and were visually reviewed; all workflow assertions pass. Completed 2026-10-03 04:56 UTC. Merged at `c8a6b31d0f513a1ca04d47c16614ae77d8b21136` (04:59 UTC); merge tree equals the exact tested source. Subsequent documentation/evidence-only main push results are not claimed passed here.

Evidence: 28 new original `safety-*.png` captures plus five `review-*.png` contact sheets; `manifest.json` identifies all 128 exact-run screenshots by dimensions and SHA-256. All originals and full logs are also in the Actions #204 `loom-editor-screenshots` artifact. The previous 100 regression captures were regenerated and reviewed as well. Transient native keyboard-focus tooltips are overlays, not displaced controls. The existing 854×480/GUI2 too-small-window fixture deliberately tests the below-minimum fallback; required 1920×1080/GUI3 and 635×320 logical layouts are functional.

Packaged mod: `loom-studios-dev` artifact from the same Actions run; `loom-studios-0.0.1-dev.jar` SHA-256 `2b48032b4713d1e35c04a7137b183a9dfccc0e929591d646e9c64bf6d9bb25bd`. New production classes and Fabric dependencies verified; no JUnit classes packaged.

Environment: Minecraft 1.21.11, Fabric Loader 0.18.4, Fabric API 0.141.1+1.21.11, Temurin Java 21, Loom 1.17.21, Gradle 9.6.1, Ubuntu/Xvfb/software Mesa. Commands: `./gradlew --no-daemon build` and `xvfb-run … ./gradlew --no-daemon -Dorg.gradle.jvmargs=-Xmx1G --max-workers=2 runClient -PuiCapture`.

The harness retains all 100 previous regression captures and adds 28: unsaved choices, named delete confirmation, recent/design colors, and light/dark/checker backgrounds at 1920×1080 GUI2/GUI3 and 3440×1440 GUI2/GUI3; compact 635×320 logical Settings, unsaved dialog, immediate Undo and expanded palette management. Visible widgets are checked for bounds and nonoverlap, excluding the intentional floating palette.

Runtime assertions exercise the actual Screen choices for unsaved Cancel, Keep draft without overwriting saved artwork, Discard to the saved baseline, and Save and continue; delete Cancel, recoverable deletion and immediate Undo; persisted design palettes, background/artwork hash isolation and privacy-filtered diagnostics. Existing canvas/3D input, preview isolation, animation/import, library/draft and export round-trip assertions remain enabled.

All five original approved reference images were inspected again: Home, Cape, Elytra, Smart Import and Sharing. Dialogs and new controls reuse the timber/steel/navy/cyan-violet workshop shell. Intentional adaptations remain native Minecraft fonts, code-native icons and compact controls; neutral preview backgrounds are user-selectable inspection surfaces rather than a replacement style.

Manual acceptance still required: ordinary client/OS clipboard, hardware performance, optional Sodium/Sodium Extra/Iris/3D Skin Layers, shaders off/on and live two-client multiplayer. Mesa capture tests do not establish hardware FPS or modpack compatibility. Failed filesystem/clipboard actions should also be exercised on the target OS; source guards preserve navigation on failed decisions and report clipboard unavailability.

Suggested test order: edit a saved design and try all four leaving choices; switch designs/templates while dirty; delete from Designs and Drafts, Cancel once, then Undo and Trash restore; paint with several colors, reopen Swatches and save the Design colors group; switch backgrounds in Settings/expanded preview and export transparent PNGs; copy diagnostics and inspect before sharing.
