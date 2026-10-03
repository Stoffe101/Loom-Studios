# Premium UI and performance audit — 2026-10-03

## Additional dependency/API research — 2026-10-03

NanoVG/LWJGL is a stronger rendering-spike candidate than the first audit captured: it provides antialiased vector graphics. Noamm9/NVGRenderer explicitly targets Fabric1.21.11/Loader0.18.4/Java21, with rounded rectangles, gradients, shadows, text, SVG images and GuiGraphics picture-in-picture integration. Its README states Unlicense; build uses Mojang mappings, Kotlin2.3.10/FabricLanguageKotlin and lwjgl-nanovg3.3.3 with Windows/Linux/macOS/macOS-arm64 natives. This is repository-declared support, not tested compatibility in Loom.

Revised next work: compare a small NanoVG rendering prototype against the asset-backed Fabric route using identical buttons/icons/text at all four profiles, then select the renderer using measured frame times and visual quality. Check native/LWJGL alignment, framebuffer/scissor/alpha state, 3D preview interleaving, input transforms, resource disposal and optional shaders. Keep assets local/preloaded; do not load network images in rendering. Evaluate owo-ui layout separately; do not assume the two integrate automatically. Neither dependency is adopted yet.

YACL is useful for settings controls/serialization, but its documented vanilla-oriented configuration design does not deliver the bespoke editor appearance. Exact1.21.11 artifact compatibility was not verified in this follow-up. Dear ImGui is useful for developer inspection/profiling; its authors explicitly prioritize developer tools and note accessibility/internationalization gaps, so it is not the preferred player-facing overhaul. PolyUI compatibility was not established; no recommendation to adopt it.

Sources accessed2026-10-03:
- https://javadoc.lwjgl.org/org/lwjgl/nanovg/package-summary.html
- https://github.com/Noamm9/NVGRenderer
- https://github.com/Noamm9/NVGRenderer/blob/master/build.gradle.kts
- https://github.com/isXander/YetAnotherConfigLib
- https://github.com/ocornut/imgui

Verification: official README/build source inspection only; no installation, runtime benchmark or source change. Research complete; rendering spike TODO. The original cached-texture route remains the fallback. APIs provide rendering/layout capability; authored materials, hierarchy, responsive design and caching still require Loom implementation.

Status: research and visual inspection complete; implementation TODO. This pass changes documentation only. Current appearance is not accepted as premium or reference-equivalent.

## Evidence inspected

Baseline main: 2d6a23c320f1209982eae253ea92b3d59379f54d. Actual captures: source 01d530366a0f25ff89265eef27ec286c0d545d2f, Actions run 37125589956 (#217), artifact 11275741226. Downloaded archive SHA-256 e222756d7f2799c7e09e556755b4659b8b9371db23a2a647a237dfedd11872b8. All 182 PNGs decoded; all eleven contact sheets inspected, with full-size Home, dense Cape layers and Smart Import at 1920×1080 GUI3. Latest feedback image 20261003-150225 and all five original JPEG references inspected individually after the archive.

The previous HTTP503 download/inspection blocker is resolved. This closes archive access, not aesthetic acceptance. Prior 125 passing tests and bounds assertions establish functional coverage, not smooth rendering or premium presentation. No new game run, FPS measurement or dependency integration test was performed in this documentation pass.

Environment baseline: Minecraft1.21.11, Loader0.18.4, FabricAPI0.141.1+1.21.11, Java21; prior captures use software Mesa. Hardware, optional mods/shaders and multiplayer acceptance remain outstanding.

## What the images show

| Area | Current implementation | Required direction |
| --- | --- | --- |
| Shell | Repeated procedural timber stripes and flat lanterns; oversized logo; thin parchment footer | Authored dimensional timber/steel/lantern assets, restrained lighting, compact editor header; preserve richer Home branding |
| Icons | Tiny sixteen-cell handmade silhouettes, inconsistent weight; many unexplained compact symbols | One coherent SVG-derived set, antialiased atlas, consistent optical size; custom recognizable cape/wing silhouettes |
| Text | Minecraft pixel font dominates labels and long menus | Smooth readable UI family, measured hierarchy and baseline alignment; pixel lettering reserved for brand |
| Panels | Uniform navy blocks and repeated hard outlines | Subtle surface depth, controlled corners, one border owner, meaningful spacing and clear selected/hover/focus states |
| Home/library | Flat cape textures stretched over repeated scenery; sparse huge list screens | Balanced card grid, cached model thumbnails where viable, stronger card/title hierarchy, contextual dialogs and empty states |
| Preview | Small floating subject or letterbox with crude forest | Framed subject-centered studio, clear cape/wing modes and camera controls, static background art; neutral inspection mode retained |
| Compact editor | GUI3 fits but icons replace tool names, preview competes with layers, inspector remains cramped | Task-focused compact layout with collapsible/tabbed preview, expandable layers, contextual properties and accessible tooltips/shortcuts |
| Import/animation | Rows of equal-looking buttons and excessive empty space | Grouped task sections, visible processing choices, real sliders, integrated timeline and contextual inspector |

Reference filename order differs from semantic order: original 01 = Elytra/animation, 02 = Sharing, 03 = Home, 04 = Smart Import, 05 = Cape. Use visual screen identity when matching assets. Preserve the references' crafted workshop character, cyan selection and restrained violet accents. Do not imply sparkles or compositing effects shown in concept art are already implemented. Authored project pixels remain crisp; smooth UI assets must use their own filtering.

## Code audit: performance candidates, not measured root causes

- LoomActionCard compiles template pixels once but renderWidget submits one fill per face cell. At the current 4× canvas, a 40×64 thumbnail can submit 2,560 fills; six visible thumbnails can submit 15,360. These are API submissions, not proven individual GPU draw calls.
- LoomColorPickerWidget rebuilds SV colors through nested two-pixel loops and hue rows every render. Cache SV by hue and size; hue ramp is static.
- LoomWorkshopArt and preview/checker scenery use repeated fills every frame. Bake static decorations and reusable checker patterns into textures.
- LoomImagePreviewWidget draws transform outlines with pixel-step fills. Use bounded line geometry or a suitable small pipeline instead.
- LoomPlayerPreviewWidget extracts entity state and constructs camera state every frame. Profile separately from 2D UI; cache/throttle unchanged static preview only if its measured cost warrants it. Dragging, animation and changes must remain responsive.
- LoomCapeFaceWidget already has revision/region/size texture caching, and LoomImagePreviewWidget already uploads source textures on changes. Preserve these mechanisms; do not incorrectly replace them on the assumption that all artwork recompiles every frame.
- Inspect layer thumbnails, project suppliers, hashing and save/recovery work with counters before attributing stalls to them. Prior immutable-preview cache tests do not measure total frame time.

## Researched rendering choices

1. Recommended first path: existing Minecraft/Fabric GUI plus an asset-backed component/theme layer. Fabric's version-specific 1.21.11 docs show GUI texture blits, pipeline selection and scissor clipping. This is sufficient for atlas icons, textured panels and cached artwork without introducing a browser or another runtime.
2. Lucide SVG sources provide coherent product controls. Export selected icons offline at several physical resolutions, pack a texture atlas, retain ISC and Feather-derived MIT notices, and verify strokes at GUI2/GUI3. Do not render SVG paths or simulate antialiasing with thousands of fills per frame. Cape/elytra pictograms need matching bespoke assets.
3. Inter is a screen UI font distributed under SIL OFL1.1. Prototype a bundled custom Minecraft font and measure glyph metrics, fallback, tooltip/narration behavior and GUI2/GUI3 sharpness. This pass has not verified the exact 1.21.11 font-provider integration or its quality.
4. owo-ui offers component trees, dynamic layout, scroll containers and animations. Official repository has release 0.13.0+1.21.11 (2026-01-31); braid-ui properties target1.21.11 and FabricAPI0.141.2. Candidate for a focused layout spike, not an adopted dependency: Loom currently uses0.141.1, so mapping/API/input/preview interoperability must be built and tested first.
5. ModernUI offers antialiased scalable text and a richer framework. Official current compatibility matrix lists26.1–26.1.2 and1.21.6–1.21.8, but not1.21.11. Do not make it a required dependency based on an unofficial fork or an untested port.
6. Extra optimization mods may be tested later; they cannot substitute for fixing Loom's own per-frame work.

Primary sources (accessed2026-10-03):
- https://docs.fabricmc.net/1.21.11/develop/rendering/gui-graphics
- https://lucide.dev/license
- https://github.com/lucide-icons/lucide
- https://rsms.me/inter/
- https://docs.wispforest.io/owo/ui/
- https://github.com/wisp-forest/owo-lib/releases/tag/0.13.0%2B1.21.11
- https://github.com/wisp-forest/owo-lib/blob/braid-ui/gradle.properties
- https://github.com/BloCamLimb/ModernUI-MC#compatibility-matrix

## Implementation sequence

1. Add reproducible per-screen frame-time capture and compile/hash/upload/submission counters. Measure warm idle, painting, color changes, library scrolling, rotation/pan, animation and import drag. Separate cold load from steady-state p50/p95/p99 and allocations/GC.
2. Remove confirmed repeated 2D work: template texture cache, picker ramps, baked scenery/checkers and icon atlas. Define explicit revision/hue/size/resource-reload invalidation, bounded ownership and texture disposal on screen closure/reload.
3. Build one complete Home and Cape vertical slice: real assets, smooth font, common buttons/cards/menus/dialogs, coherent spacing/borders/states, subject-centered preview. Compare actual client screenshots against semantic Home/Cape references before migrating everything.
4. Implement responsive variants using actual logical viewport. 1920×1080 GUI3 is640×360; 1920GUI2 is960×540; ultrawide logical dimensions must be read from Window rather than assumed integer divisions. Keep drawing and input transforms aligned, reduce decorative height, expose preview/layers as panes or tabs, and retain core drawing/save/equip actions. Never force global GUI scale or show a resize warning for mandatory profiles.
5. Migrate library/templates/import/sharing/timeline/layers/settings to the same components. Use integrated group disclosure and selection, contextual properties, consistent notices and confirmation dialogs. Avoid more feature scope until this experience is polished.
6. Run existing regression tests and actual-client captures at1920×1080 and3440×1440 GUI2/GUI3; verify mouse/keyboard/narration/focus, all state variants, layer density, no overlaps or duplicate borders, and unchanged pixel export/alpha.
7. Measure ordinary-client performance on the user's hardware, then optional Sodium/SodiumExtra/Iris/3DSkinLayers and shaders separately. Proposed target: stable60FPS (16.7ms total frame budget) in normal editor use; compare editor overhead to baseline and report frame-time distribution. This is a target, not an achieved result or guaranteed hardware outcome.

Next executable task: instrument the baseline and replace template/picker/scenery redraws with cached textures while establishing the icon/font/asset vertical slice. The overhaul is not implemented or merged by this research pass.
