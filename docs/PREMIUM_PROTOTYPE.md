# Smooth renderer prototype

IN PROGRESS, 2026-10-03. F9 opens an isolated interactive NanoVG comparison in-world. This is a renderer/layout spike, not a completed Home/Cape overhaul. Authoring and saved artwork remain in the existing editor.

First source71d3d02e115ee6a7a7dc568fb66e10f983d6cd56, Actions#220/run37135206577: Java21 build and125 tests pass, zero failures/errors/skips. Actual-client captures are still running. Subsequent icon-state and production-cache changes need a new exact-source run.

Production performance follow-up: LoomActionCard template artwork and LoomColorPickerWidget SV/hue gradients now use a shared32-entry render-thread texture cache. Keys include template kind or hue bits and dimensions; hue/size changes invalidate naturally and evictions/client shutdown release GPU resources. Template alpha-guide pixels are retained; color gradients are rendered at full source resolution. This avoids up to2,560 fill submissions per4× template face per frame and about1,536 SV-cell submissions for a96×64 picker. These are code-level submission reductions, not measured FPS gains. Runtime regression verification is pending.

Implementation: LWJGL NanoVG3.3.3 module/natives; Java PIP adapter derived from Unlicensed NVGRenderer f4e8272; locally bundled Inter and twelve Lucide SVG controls with notices packaged in third-party/. No Kotlin or additional UI framework is needed for this adapter. Font and icon resources are owned by the NanoVG context and freed at client shutdown. SVGs rasterize once to96px textures, reused thereafter. Smooth and native controls share bounds. Normal Minecraft keyboard/narration widgets own input; paint is separate. The actual entity preview is interleaved with the vector canvas.

Targets:1920×1080 GUI2/GUI3 and3440×1440 GUI2/GUI3. Compact tools use labeled tooltips, preview can collapse, and authoring remains reachable. Sample canvas/layer artwork in this spike is illustrative, not editable. Layer manager/Open editor lead to existing implementations.

Instrumentation reports rolling CPU submission p50/p95 and path count. It excludes total GPU/frame/3D-preview cost and is not an FPS qualification. First-frame asset loading must be separated from warm measurements. Static preview caching and production resource-reload hooks are still TODO.

Testing pending: Java21 compile/unit regressions; eight extra actual-client captures compare smooth/native controls at the four mandatory profiles, with widget bounds/nonoverlap and existing182 captures/regressions retained. Local compile attempt could not resolve existing Fabric Loom1.17.21 through the execution network; use GitHub Actions rather than changing the project's established toolchain.

Reference targets: semantic Home/Cape screens. This prototype tests typography/icon/surface capability; authored timber/steel/lantern/preview scenery and migration of all screens are TODO. Existing renderer remains the fallback until image review and measurement support adoption.
