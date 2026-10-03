# Library and layer polish — automated verification passed; visual review PARTIAL

## Library / layer polish — implementation verified; visual acceptance PARTIAL (2026-10-03)

Merged through [PR #18](https://github.com/Stoffe101/Loom-Studios/pull/18) at `7f9800e399e487ae59e5ba0f3b2c24fec6e697fb`, 2026-10-03 13:36 UTC. Merge tree equals the exact tested source tree `01d530366a0f25ff89265eef27ec286c0d545d2f`. Implemented: hover menus with action icons and Equip, favorite-first Designs, Home thumbnails matching catalog artwork/creation, eight new cool/cute motifs (14 total), scenic recent cards with status badges, atomic local folders/tags with search/filter, Ctrl/Shift selection and bulk actions, twenty checksum-verified saved-design snapshots with pre-restore backups, accurate transient save/equip feedback, bounded library artwork reuse, compact eighteen-pixel layer rows, and Cape/Elytra managers with named groups and batch selection/visibility/locking/duplication/reordering/deletion. Oversized batches fail before editing document/history. No portable project or network schema change.

[Actions #217](https://github.com/Stoffe101/Loom-Studios/actions/runs/37125589956), exact source above: both checks pass, Java21/Minecraft1.21.11 build, **125 tests, zero failures/errors/skips**, and **182 real Minecraft/Mesa captures** with visible-widget bounds/nonoverlap at 1920×1080 and 3440×1440 GUI2/GUI3 plus compact task screens. GUI3 dense-layer captures assert at least four complete rows. Actual Screen inline menu/Escape, Home/catalog artwork equivalence, favorite ordering, Ctrl bulk selection, batch hide/undo and Equip pass with prior preview/input/import/animation/library/safety regressions.

All five feedback screenshots (121206, 121353, 121438, 121637, 122234) and five approved references were viewed. Earlier actual Home/template/library and compact dense-layer screenshots were inspected. **The final screenshot archive could not be downloaded/inspected locally because executor/file tools failed with HTTP 503 (environment_status_unavailable). Final image-by-image visual acceptance remains PARTIAL.** The tested revision was merged under the existing authorization for in-game testing; this documentation-only follow-up does not claim subsequent main checks passed. [Evidence, environment and acceptance checklist](verification/library-polish/README.md).

Next: finish final archive inspection once file access recovers, then ordinary-client usability and hardware frame-time testing. Folder/tag/group metadata is local and excluded from exports/backups; groups organize membership without compositing or animation changes. Recovery autosave opts out of automatic version history. Optional Sodium/Sodium Extra/Iris/3D Skin Layers, shaders and live multiplayer remain unverified. No known failing checks on the merged source.


## Environment and evidence

Minecraft 1.21.11; Java 21; Fabric Loader 0.18.4; Fabric API 0.141.1+1.21.11; Fabric Loom 1.17.21. GitHub Ubuntu runners, Xvfb/Mesa software rendering. Commands: `./gradlew --no-daemon build` and the isolated `runClient -PuiCapture` workflow. No optional renderer mods or shaders were tested.

[Run #217 / screenshot and mod artifacts](https://github.com/Stoffe101/Loom-Studios/actions/runs/37125589956#artifacts). Screenshot artifact ID 11275741226, archive SHA-256 `e222756d7f2799c7e09e556755b4659b8b9371db23a2a647a237dfedd11872b8`. Mod artifact ID 11275580758, archive SHA-256 `82f516e61f4ce35260f8bfdbbcddda41cbedccd2808f24228dd45edbea6686bb`. These are archive digests, not individual PNG/JAR digests. GitHub artifact expiry: 2027-01-01. [Capture inventory](manifest.json) preserves names/logical areas and interaction-check results without claiming final PNG visual inspection.

The runner generated 182 images: existing regression profiles plus all four mandatory profiles for hover menus, both new template pages, organized library, bulk actions, folder/tags, versions, dense Cape/Elytra layers, both grouped managers and group naming; six compact 635×320 task views supplement them. Visible controls pass bounds/nonoverlap; mandatory 1920×1080 GUI3 dense lists assert four complete rows.

## Corrections found during implementation

The first checkpoint assigned Screen.minecraft despite its final declaration; removed before successful builds. Initial fixed inspector reservation overflowed while changing GUI scales; reservation now follows actual padding/control strides. The first stress fixture exceeded the existing 1 MiB project budget; it now uses six Ultra cape layers, matching the feedback scenario. Batch encoded-size validation and a regression test protect unchanged project/dirty/undo state. The oversized-batch test originally expected a new unsaved session to be clean; corrected to preserve its actual baseline. Visual review also found template pagination leaving an off-page preview selected; preview selection now follows the visible page. Home Gradient creation now uses the same catalog artwork as its thumbnail. Save feedback expires and disappears after another edit.

## Remaining acceptance

Final archive decode/image-by-image review is pending due to executor/file service HTTP 503. Earlier actual Home/templates/library/compact-layer images were viewed against all supplied references; native Minecraft fonts/code-native motifs and scenic static recent thumbnails are deliberate adaptations. Static cards avoid running a 3D renderer for each project. Software captures verify render/input/cache contracts but do not establish hardware FPS.

In an ordinary client, test GUI2/GUI3 browsing, Ctrl/Shift selection, tags/folder filters, bulk Trash/restore, snapshot restore, group filtering/actions and save/equip feedback; inspect final screenshots and confirm compact readability. Then measure actual hardware responsiveness and optional Sodium/Sodium Extra/Iris/3D Skin Layers, shaders off/on, OS clipboard/filesystem failures and live two-client multiplayer.

