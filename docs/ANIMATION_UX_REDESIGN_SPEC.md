# Loom Studios — Animation UI/UX Redesign Specification

**Status: PROPOSED (planning only).** Animation 2.1's functionality is already implemented. This proposal redesigns its **presentation, discoverability and interactions**, without deleting or replacing its data model.
**Prepared:** 2026-10-08. **Master backlog:** [FUTURE_IMPROVEMENTS_ROADMAP.md](FUTURE_IMPROVEMENTS_ROADMAP.md), UX-03/05/08 and animation portions of AS-04, EX-01.
**Approved visual benchmark:** [Elytra + Animation reference](references/ui/Loom_Studios_04_Elytra_Animation_Editor.webp), [Cape Editor](references/ui/Loom_Studios_02_Cape_Editor.webp).

## 1. Why change the experience?

The current engine can animate separate parameter properties, use tracks/keyframes, preview timed effects, edit Bézier curves, zoom/scrub/copy/paste and edit GIF frames with onion skin. Real screenshots show that a new user still encounters technical screens with dense rows, many equal-weight text buttons, weak context about which object is animated, and switching between a large curve surface and the cosmetic preview.

**Design intent:** users should understand _what is animated, where, why, and how to change it_ without memorizing the schema. The animation editor should look like a creative timeline, not a diagnostics utility.

No new effect or keyframe model is required merely to achieve this. The primary effort is clear navigation, information hierarchy and input design.

## 2. Two modes, one shared animation document

### Simple Animation (default for first-time users)

A preset-first, visual, no-timeline-required experience:
1. Select the current Cape/Elytra or a layer.
2. Browse visually animated preset thumbnails.
3. Choose a preset; preview it **before** Apply.
4. Adjust only key controls in plain language: Speed, Strength, Direction, Color, Loop, maybe Density.
5. Press Play, Save or switch to Advanced.

Suggested first card families (extend current actual effects/presets, not new runtime technology): Twinkling Stars, Gentle Glow, Moon Pulse, Floating Mist, Scrolling Pattern, Moving Gradient, Magic Sparkles, Falling Snow, Ember Drift, Color Cycle.

Each card must explain whether it animates layer position, hue, opacity, emissive/glow or a generated effect. Presets must never silently insert extra layers/tracks that the user cannot find. Applying a preset is one Undo step.

### High-priority entry point: **Animate Asset** from the Inspector (M1-04A / M2-00)

A player should **not** need to manually navigate the Animation workspace and rediscover the layer they just placed. The selected asset's Edit Inspector exposes **Animate Asset**, passing its exact saved `layerId`, current channel (`CAPE` or `ELYTRA`), wing/face context for preview, and a return target. This route comes **before** optional new effect families and advanced graph polish.

- First-time animation: open Simple mode with **visual preset cards**, contextual sliders for Speed/Strength and supported options, **Try / Apply / Cancel**, and a real worn-cosmetic preview.
- Existing animation: label action **Edit Animation** and focus that asset's existing track(s); identify advanced/customized lanes instead of replacing them. Switching Simple ↔ Advanced never applies a recipe or writes project data.
- **Try must be pure**: isolated evaluated preview, no `.loom` edit, history entry, equip, remote sharing or multiplayer effect. **Apply** creates one Undo step; **Cancel/Back** returns to the selected asset Inspector with unchanged artwork and selection.
- A renamed/reordered layer still resolves by persistent UUID, not by name or layer index. Left/right Elytra target and face previews must not silently switch.
- Do not add a parallel asset-specific animation data model or an extra permanent panel. Reuse Animation 2.1 engine, existing animations and Loom's premium UI components.
- On constrained GUI3 use one compact contextual panel and preview. On ultrawide use the available width. Place large edit/preview actions ahead of verbose explanations and button rows.

**Acceptance:** from a freshly placed star, complete **Animate Asset → Twinkle → Try → Apply → Back to Asset** and later reopen **Edit Animation**; from a cloud on a chosen Elytra wing, create Drift without changing the opposite wing. Verify screenshot layouts, UUID/track identity, source pixels, Save/Undo/Redo and preview/equip parity. Until this is implemented and tested it remains **TODO**, even though the underlying Animation Studio already exists.

### Advanced Animation (visible but not forced)

Full editor for power users:
- Layer stack with collapsible subtracks/parameter lanes and clear badges for animated values.
- Timeline ruler and scrub/playback controls.
- Selection/move/copy/paste and multiple-key operations with predictable rules.
- Curve/easing editor associated with a selected key/segment.
- Contextual inspector for current effect or selected parameter.
- Persistent cosmetic preview or easily expanded mode.
- Zoom and pan without deleting the playhead.
- Search/focus track if many layer/effect groups exist.

**Invariant:** switching Simple ↔ Advanced never flattens, simplifies, overwrites, resamples or discards keyframes. If a complex Advanced project cannot be represented with the limited Simple controls, show **"Customized animation — adjust in Advanced"**, with safe read-only playback and a jump to the relevant lane. Never claim it is still a named stock preset when user-edited.

## 3. Proposed workspace hierarchy

### Full/comfortable viewport

- Top: concise breadcrumb **Cape / Animation** or **Elytra / Animation**, project title, Save status, Back.
- Main region: generous rotating 3D preview / optional canvas, with active overlay legend.
- Bottom: horizontally generous dockable/resizable timeline with ticks, color-coded keys and grouped tracks.
- Side inspector: selected layer, effect card, selected parameter and quick actions; not a second wall of permanent controls.
- Bottom transport: Go to start, Previous key, Play/Pause, Next key, Loop, current/total time, optional playback rate.
- Toggle: Simple / Advanced, and a small one-click "Focus Timeline" or "Focus Preview" action.

A timeline should be tall enough to show meaningful rows but should **not** permanently steal 60% of the canvas. On ultrawide, use available width for labels/keys/preview instead of adding enormous empty gutters.

### Compact 1920×1080, GUI3 (~640×360 logical)

This is a different composition, not the full-screen layout compressed:
- 36-ish logical-pixel brand/header as established by current contract, compact title/breadcrumb.
- Persistent small-but-legible live preview or toggleable Canvas/Preview pane, with clear active target.
- Timeline in a bounded region where a real key/segment is still selectable.
- One contextual properties page at a time. Hidden advanced properties remain reachable by direct "Parameters" action.
- No full-window modal that traps Save/Back/Undo; menus are bounded with usable outside-click/Escape.
- Scroll **track lists only**, not the entire app shell; layer name area stays stable while keys scroll.
- If timeline and preview cannot coexist at usable sizes, use explicit focus layouts with state retention, not two tiny unusable copies.

### Ultrawide 3440×1440 GUI2 (~1720×720 logical)

Use the available workspace for a larger preview and full timeline, optionally simultaneous curve pane. No additional pointless navigation rail; increase information density **where it helps**, not by exposing all settings at once.

### Personalization

Remember workspace mode, timeline zoom, last active lane, collapsed group state, preview camera/pose and dock proportions in **local editor preferences**, not project/equipped cosmetic content. Save meaningful project state separately. Avoid accidentally persisting transient modal or dragging state.

## 4. Track list, labels and keyframe language

- Rename top-level player UI text **"Animation 2.1 · Parameter lanes"** to **"Advanced Animation"**, with "Parameters & Curves" as the technical subpage.
- Differentiate layers, effects, parameters and individual keys by indentation, icon, row shading and color, rather than solely a purple rectangle.
- Group lanes visibly beneath their owning layer/effect; only show active parameters until expanded.
- Clearly label the animated property: **Star Density**, **Glow Intensity**, **Scroll X**, **Opacity**, **Hue Shift**, **Gradient Angle**, etc. Avoid anonymous Value 0/1.
- Use meaningful physical/visual units in inspector (%, degrees, pixels, color swatches) even if stored values are normalized 0–1.
- Label timing in seconds with optional ticks, recognizing Minecraft's 20 ticks/second logic where applicable.
- Selected keys get high-contrast fill/outline; hovered/unselected/disabled keys have distinguishable states.
- Purple bars (track duration), diamonds (keys) and cyan playhead need a visible legend/tooltip or first-use explanation.
- Expose current playhead time and the actual active parameter value prominently; no unexplained spare neon stripe.
- Show explicit locked/muted/hidden states where applicable, and disabled controls must say why.
- New track empty state: **1. Select layer → 2. Choose effect → 3. Set duration → 4. Add key or preset → 5. Play**.
- Help is brief and contextual; do not permanently consume three rows repeating instructions once the user has learned the workflow.

## 5. Direct timeline interaction

- Single click: select key; Ctrl-click toggles membership; Shift-click selects range in a lane, with documented cross-lane behavior.
- Mouse drag moves selected keys as a single validated edit. Show ghost positions and tentative tick readouts while dragging.
- Snap options: Off, whole tick, small time step, other keys/playhead; modifier temporarily bypasses snap.
- Keyboard: Space Play/Pause when focus is not editing text; arrow keys nudge key/selection; Delete remove; Ctrl+C/V clipboard; Ctrl+Z/Y undo/redo; Home/End seek. Narration and menu-focus interactions take precedence.
- Right click: relevant context menu (Add key here / Copy / Paste / Duplicate / Delete / Easing); never hide essential actions exclusively in right-click menus.
- Multi-key paste previews collisions and out-of-duration operations before committing; respect current atomic rejection rule.
- Keep current key selection and zoom after Undo or switching panes when safe; do not silently jump to start.
- Preserve exact layer/track IDs and no accidental key deletion when collapsing/expanding groups.
- Seeking/scrubbing only changes isolated preview evaluation, not equipped state or dirty project unless an authoring edit occurs.
- Add keyboard/focus-visible mouse alternatives to handle drag thresholds at tiny GUI scales.

## 6. Curves and easing

The v5 Custom Bézier backend is already present. Improve explanation and feedback:
- Curve panel identifies **"Selected key → next key"** explicitly.
- Easing gallery cards: Linear, Ease In, Ease Out, Smooth, Step and Custom; each shows a small curve drawing with movement example on hover or focus.
- Graph axes communicate **time** and **effect value**, with tick/value readouts for selected handles.
- On hover/drag, show the outgoing handle/time and curve shape, not only a pair of unlabeled dots.
- Reset Custom to last preset, copy/paste easing, optional apply easing to multiple selected segments (with Undo).
- A mini curve glyph can appear on track or inspector; advanced graph should not occupy half of compact workspace without a direct reason.
- Keep Bézier monotonic time bounds and no invalid/overshooting curves, preserving current defensive validation.
- Curve UI must always respect all scissor regions; Animation 2.1 previously encountered nested scissor leak and blank preview bugs, so add explicit screen regression assertions here.

## 7. A preview that explains motion

- The live preview should be **cosmetic-centered** rather than scenic-background-centered; offer character visible/hidden and neutral/scenic backgrounds.
- Retain useful standing/open/gliding modes, camera pan/orbit/zoom and the verified isolated preview semantics.
- Switching a selected effect/preset should make a small **preview candidate** without altering the equipped project.
- Optional A/B compare: Current animation vs Candidate preset, with clear Apply/Cancel.
- For animated GIF/image frames, show frame index/time and pink previous/cyan next ghosts only when onion skin is enabled.
- Optional motion paths or per-parameter value readouts can appear over preview for advanced use, but must be collapsible and absent from export.
- A preview should not disappear when switching curve tabs, selecting keys, maximizing the timeline or clicking beneath a modal.
- Retain smart caching: idle previews must not recompute textures when the authored state and tick are unchanged.

## 8. The visual effect preset gallery

Every card must use real examples produced by Loom's current rendering/evaluation, not decorative promises:
- Thumbnail of actual effect on sample artwork.
- Short verb: **Twinkle**, **Pulse**, **Glow**, **Drift**, **Color Shift**, **Scroll**.
- One-sentence description in plain language, e.g. "Gently fades the selected stars in and out."
- Scope: Selected Layer / Both Wings / Whole Artwork, constrained to supported targets.
- "Apply" previews and creates concrete editable tracks, not hidden one-off runtime magic.
- Hover preview may play a short local loop; cap simultaneous animated thumbnails and memory to avoid heavy menu rendering.
- Colors/strength/speed controls use direct meaningful ranges; a separate Advanced panel opens the full typed parameters.
- Defaults should look attractive with the shipped theme recipes (Misty Night twinkle, Emberfall glow, Snowfall drift).
- Preset changes should be reversible with one Undo and not duplicate old tracks without notice.

## 9. Editable frame animation as a visual filmstrip

**Current:** imported animated GIF Image layer can be converted to editable frames, with frame dropdown, Previous/Next, duration, duplicate/delete/reorder/draw/loop. Onion skin already exists.

**Proposed entry:** **Create Frame Animation** on a selected image/paint-related target without requiring an imported GIF first. Define a valid, bounded source-frame representation and migration plan before writing new serialization code.

**Proposed filmstrip:**
- Horizontal thumbnails or vertical strip depending on available width.
- Current frame has cyan outline, selected range highlighted, ghost indicators pink/cyan.
- Click/drag to change frame, reorder; buttons Add / Duplicate / Delete / Duration / Preview Loop.
- Show duration in milliseconds/seconds first, with ticks in advanced mode.
- Multiple frame selection for batch duration/reorder only after storage semantics are verified.
- Onion skin toggle and opacity one click away; never baked into saved/equipped artwork.
- New blank frame choices: Duplicate Current / Transparent / Previous Composite (as explicitly labeled).
- Select exact wing/face and maintain current safe isolation from the opposite wing.
- Single gesture = one Undo where appropriate. Frame conversion remains reversible without losing the original GIF unexpectedly.
- Avoid scrollbars nested inside the filmstrip when a row of actionable thumbnails would fit.

## 10. Editing safeguards and unambiguous terms

- **Apply** means change editable project; **Save** writes project; **Equip** changes player cosmetic. These must remain distinct.
- A preset can be tried without Save/Equip; Cancel restores candidate exactly.
- "Animation speed" should not be confused with Minecraft tick rate; give intuitive units and preserve deterministic time evaluation.
- Disabling a track should be clearly different from deleting it.
- Avoid claiming effects are shader-compatible when optional emissive rendering is unverified; show fallback warnings only when relevant.
- Complex custom curves remain intact when using Simple mode; do not downgrade them.
- Reference images/onion ghosts/local stamp tools are editor-only unless a future approved design explicitly changes that privacy boundary.

## 11. Screenshot and usability acceptance

All must pass the four required profiles (1920×1080 GUI2/GUI3, 3440×1440 GUI2/GUI3), plus realistic long/dense cases:

1. First use: novice animates a star layer via a visual Twinkle preset in under five minutes with no unexplained menu.
2. Expert use: add two independent parameter lanes, select/drag/copy/paste multiple keys, custom curve, reverse by Undo, save/reopen; same resulting animation values.
3. Switching Simple/Advanced repeatedly retains every key, lane, curve, duration, selected layer and target.
4. Long project (many layers/tracks) retains readable row labels and usable scrolling; no text overlap, dead control, clipped bottom transport or duplicate panel boundaries.
5. The playhead, duration, key/value labels and selected effect are understandable in a single captured frame.
6. Curve and preview are both reachable in compact mode; large wide mode uses actual width, and all scissor/popovers are rendered above correct layers.
7. Character/Elytra mode changes do not affect real player inventory, world rendering or equipped cosmetic until explicit action.
8. Frame filmstrip preserves different durations, selected frame, painting/Undo, linked/separate wing isolation and bounded storage.
9. Basic keyboard navigation, focus indicators, narrator descriptions, tooltips and disabled reasons.
10. Static frame comparisons plus real Screen input gestures and human image review against Reference 04. Include actual project art and empty-state screenshots.

## 12. Implementation order

1. Wireframe + four-profile interaction proof; validate compact/ultrawide canvas/timeline/preview allocations.
2. Reorganize existing animation UI into Simple/Advanced without any schema change; preserve all v5 features.
3. Visual effect gallery and explanatory value labels, using existing presets/engine.
4. Timeline affordances, selection feedback, transport, curve mini-previews and state persistence.
5. Filmstrip/new frame-entry architecture (model change only if essential, with explicit compatibility review).
6. Screenshot/UI gesture tests, GPU/runtime performance, usability pass and docs/acceptance.

**No feature is DONE by this specification.** Record source SHA, Minecraft/Fabric versions, test results, UI captures, remaining issues, decisions and next work in canonical docs before implementation is accepted.
