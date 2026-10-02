# Loom Studios — Reference Fidelity Roadmap

**Requested five-screen style/alignment pass: DONE, 2026-10-02.**

Approved baseline: the five refreshed references under `docs/references/ui/`. Each was visually inspected individually before the implementation and compared with actual Minecraft captures after it. The contract preserves their hierarchy, interaction model and visual identity while reducing density for compact Minecraft GUI layouts; it does not require pixel-for-pixel reproduction.

## Accepted shared visual contract

Dark navy/slate panels, cyan selected controls, violet animation accents, timber planks, steel brackets/rivets, lanterns, stitched cloth pennants, a cyan-to-violet pixel wordmark, and a parchment status plaque establish the workshop style. Decoration stays inside reserved header/footer/edge bounds. Moonlit forest scenery frames the real interactive player preview. Inset bevels avoid extending control geometry. Compact controls/pages preserve the canvas and task surfaces.

## Screen acceptance

| Reference | Implemented hierarchy and style | Acceptance |
| --- | --- | --- |
| 01 Home | Five real action cards, actual recent cape-face thumbnails, vertical Blank/Gradient templates, selected-project live preview, branded frame | PASS at all four profiles |
| 02 Cape | Dominant canvas, labeled normal/icon compact tool rail, contextual controls, Layers/Color/Properties pages, live preview, Save/Save + Equip/Export | PASS at all four profiles |
| 03 Smart Import | Source image, Placement/Processing settings, processed/atlas results, inline live candidate, fixed Apply/Cancel | Both pages PASS at all four profiles; applied pixels equal candidate |
| 04 Elytra/Animation | Unfolded semantic wings, Linked/Separate editing, live preview, layer/color/property/animation pages, adaptive timeline and key/playback controls | PASS at all four profiles; compact pages and long tracks PASS |
| 05 Share/Export | Real design fingerprint, Export/Import pages, immediately visible editable/portable/PNG actions, actual Cape/Elytra PNG previews, live preview toggle | Both pages PASS at all four profiles; export round-trips/dimensions PASS |

## Verified evidence

Source `58491bc8855f854f2f1902e32f1720cb3e5e3274`; tested merge `3de85d41980e1d28d78e429749147031a41766d1`. [Actions #182](https://github.com/Stoffe101/Loom-Studios/actions/runs/37073091623) passed clean build/tests and 42 fresh actual Minecraft captures on 2026-10-02 at 22:36 UTC. All PNGs decode and were visually inspected. Each visible widget passes window/footer bounds and pairwise nonoverlap. [Screenshot index](verification/editor-workspace/README.md) contains the complete set.

Required profiles: 1920×1080 GUI 2/3 and 3440×1440 GUI 2/3. Additional evidence covers compact Color/Properties/Gradient/Transform/Stops/Animation/Playback, long layer/track collections, live/released one-pixel selection at 200% and below-minimum guidance. The full inspected canvas-body crop is identical before/after release. Some editor captures retain expected keyboard-focus tooltips; these are transient overlays rather than layout controls.

## Intentional adaptations

- Native Minecraft fonts and code-native pixel artwork/icons preserve legibility and avoid external asset dependencies.
- Compact inspectors use bounded pages; only genuine collections scroll.
- Elytra editing uses semantic rectangular wing faces corresponding to the real vanilla texture atlas.
- Sharing is local/offline: `LS-...` is a fingerprint and `LSP1:` contains the project. Hosted visibility/resolver/gallery/Favorite/chat-card controls are not represented as working services.
- Home offers real actions; Settings is absent. Blank/Gradient templates work; additional category packs remain unavailable.

## Separate future product/release work

This completed styling pass does not add recent-color history, import tint/background-removal/direct handles, a Settings product, additional template packs, a hosted sharing service or standing/open/gliding preview presets. These are separate scope decisions, not blockers for the requested workshop-style/profile fix. Optional-mod/shader/multiplayer compatibility, OS picker/clipboard and exhaustive manual feature checks remain release work.

PR #14 remains unmerged. Next concrete step: review the verified implementation/evidence; merge only when authorized. The prior failed/partial checkpoints and their fixes remain documented in PASS_LOG.md.
