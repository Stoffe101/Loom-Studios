# Usability / animation feedback audit

State: DONE for implementation, Linux runtime and visual acceptance; target-hardware/optional-mod acceptance remains manual. Base main `895e8f32d49e68b69dbc7bf30d9091355896809c`.

| Feedback crop | Diagnosis | Change / verification target |
|---|---|---|
| 195255 header | Whole decorative band stretched horizontally; title/subtitle hit edges | Preserve cap/lantern/banner proportions; repeat timber; reserve aligned 48/72px header |
| 195334 preview | Body obstructs cape inspection | Skin-face toggle; suppress body render type only on flagged preview snapshots; retain cape/Elytra layers |
| 195525 Home actions | Actions flush to outer border | Six-pixel gutter; bounded equal-width actions; extra footer height |
| 195639 View All | Button bottom touches header divider | Reduce to 13px with three-pixel top/bottom inset |
| 195726 export code | Text starts on cyan border | Twelve-pixel panel inset plus eight-pixel text gutter |
| 195817 swatches | Deferred smooth labels paint after native scissors popped | Capture transformed/intersected nested scissors with every deferred command |
| 200127 menu | Rows flush to popup border | Shared 174px menu, 6px gutter and 24px rows; same geometry for paint/input/clamping |

Additional artifact finding: animation properties can overflow footer at GUI3. Redesigned with presets first, optional advanced controls, and bounded preview height. Recipes: Glow, Pulse, Shimmer, Blinking stars, Color cycle, Horizontal scroll, Vertical wave. Apply replaces selected track or creates one; rate 0.25–2 cycles/second; existing keyframes remain editable. Stars operate on a user-drawn separate detail layer. Shimmer uses existing per-pixel sparkle, not a new particle system.

References inspected: Home, Cape, Elytra, Smart Import, Sharing. Preserve workshop timber, cyan/violet hierarchy, smooth controls and scenic preview. Compact adaptation reserves readable editing space rather than copying reference proportions literally.

Additional scissor correction: native opaque fills intersect the active clip before occluding earlier vector commands, so scrolled group headers cannot erase pinned controls. Clipped commands retain their actual label/control bounds to avoid fragmentation from unrelated pixel fills.

Exact runtime source `b447e7add4cf12ded65d477ad4f5c01bfc95c6fe`. Build #241/run37153029252 Java job passes: 128 tests, zero failures/errors/skips. Comparison #19/run37153029222 passes all twelve jobs and decodes 118 images, including 28 new usability views. Final-source contact sheets reviewed for all four usability profiles, production GUI3 at both resolutions, and compact dense layers/context menus. Body-hidden cape/wing retention, scrolled palette management clipping, animation preset undo/control bounds and animated Elytra Glow masks pass. Full Build240 capture found an out-of-bounds Copy diagnostics button on compact Settings. Adaptive 24px compact rows and smaller top inset correct it; Build241 full rerun passes. All 218 originals decode locally; final compact Settings, swatches, unsaved choices and delete-undo captures visually reviewed. Environment: MC1.21.11, Loader0.18.4, API0.141.1, Java21, Linux/Mesa/Xvfb. Optional mods and target hardware are separate manual acceptance.

Elytra Glow follow-up: a separate cached emissive wing mask and fullbright ElytraModel pass now mirror cape highlights. Masks contain authored pixels only (no alpha-guide checkerboard); update at changed timeline ticks and release with their runtime bundle. Verification includes a wing-only glow capture plus mask presence/change assertions.

Failures corrected before acceptance: final renderWidget override replaced with renderContents (Build232); compact inspector reservation fixed by reducing preview minimum to48 (Build233); idle-cache probe restricted to static renderer stages rather than playing animation fixtures (Comparison14); ElytraModel made non-generic for exact MC1.21.11 (Build238). Local Fabric Loom dependency resolution remained blocked; authoritative checks run in CI. No known remaining failing automated checks at the runtime SHA above.

Additional full-run finding: fixed Settings offsets assumed the old header height. Use uniform adaptive row spacing so all nine controls fit above the compact footer.

Packaged release JAR inspected: presets, character control, visibility mixins and wing emissive layer present; five nested dependencies and client mixin descriptor present. Build241 artifact loom-studios-dev; JAR SHA256 `12074823b5674e0ec48da93c6c3e2f067bd5363b61dec1fbfbd504253e1e7709`.

Final acceptance 2026-10-03 21:08 UTC: Build241 full client completes all218 captures, all workflow/input/animation/library/safety/polish/usability assertions and nine idle-cache probes. Final runtime source is b447e7add4cf12ded65d477ad4f5c01bfc95c6fe. Screenshot artifact11285112342; build artifact11284493004. Acceptance commit is documentation only; subsequent main checks are not claimed passed here.
