# Usability / animation feedback audit

State: IN PROGRESS. Base main `895e8f32d49e68b69dbc7bf30d9091355896809c`.

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

Evidence pending: CI build/tests; full 218 captures; fast suites including 28 new feedback-specific captures; body/cape retention visual review; scrolled palette clipping; animation preset undo and control bounds. Environment: MC1.21.11, Loader0.18.4, API0.141.1, Java21, Linux/Mesa/Xvfb. Optional mods and target hardware are separate manual acceptance.
