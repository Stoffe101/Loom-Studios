# Dropdown / cosmetic preview feedback audit

State: implementation complete; exact-source runtime and visual acceptance pending.
Base main: `2b2e5f1182396a8eb3f0f50ecd3dfc03db88611a`.

| Feedback | Diagnosis | Implemented change |
|---|---|---|
| 214803 animation effect | Repeated cycling hides available effects | Bounded modal dropdown with labels and effect descriptions; studio recipes and rate also use direct choices |
| 214844 Home actions | Cards and Browse/Help touch the left-panel frame | Six-pixel inset; available card height excludes panel padding; footer actions reserve bottom gutter |
| Character-hidden preview | Item feature layers still render after body suppression | Clear both hand render states/stacks in the isolated snapshot; hidden character also clears head/armor/shoulder/projectile attachments |

Both feedback crops and all five approved references were visually inspected. Workshop framing, scenic previews, smooth controls, cyan/violet hierarchy and compact editing space remain the design target.

Additional usability: effect-specific key-value labels and explanatory tooltips; dropdown arrows/Home/End/Enter, wheel scrolling, narration, Escape/outside cancellation and resize dismissal. Selection closes before editing/rebuild; underlying mouse/keyboard interaction is blocked while open.

Preview changes mutate only a freshly extracted GUI render state. Real inventory is untouched. Chest Elytra and authored cosmetic textures remain available for the relevant preview mode. Held objects are omitted with the character visible too.

Documentation audit: historical drawing/library/grouping/version/animation/pose/keyframe/import-handle TODOs are already delivered and must not be reimplemented. Effect labels/help are now delivered. Richer effect parameter schemas, onion skin/reference layers, image tint/background removal and hosted gallery/resolver are optional future scope with separate data/service design. Hardware/OS/shader/multiplayer release acceptance cannot be replaced by adding features.

Failures corrected: constructor-time Fabric screen callback registration (`69d509d`) failed before Home initialization; register via AFTER_INIT before the premium flush. The next attempt (`d2376f4`) exposed a capture-only reflection helper unable to find inherited choicePopup; it now walks superclasses. Builds passed but those attempts are not accepted runtime evidence. The next functional run d3140b5 passed128 tests and16 comparison jobs; full-size review exposed native pixel text in the separate popup hook. Render popup inside PremiumControls before finish instead.

Verification target: runtime source `d3140b56a0e1cc94bc9aa172d68a00404b16e0fb`; Build246/run37157595670 (238 actual client captures) and Comparison23/run37157595700 (16 jobs, 138 views including20 feedback-specific views), MC1.21.11/Loader0.18.4/API0.141.1/Temurin21/Linux/Mesa/Xvfb. Four profiles:1920×1080 and3440×1440, GUI2/3. Runtime assertions cover popup bounds/cancel/keyboard choice, clean hand/armor states, unchanged real inventory, and existing workflow regressions. Cache checks are not hardware FPS measurements.
