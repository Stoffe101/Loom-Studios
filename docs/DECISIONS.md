# Loom Studios — Architectural Decisions

## ADR-001 — Minecraft 1.21.11 / Loader 0.18.4 baseline
**Status:** Accepted

Develop for Minecraft 1.21.11 and actively verify Fabric Loader 0.18.4.

## ADR-002 — Java 21
**Status:** Accepted

Use Java 21.

## ADR-003 — Mojang mappings
**Status:** Accepted

Use official Mojang mappings.

## ADR-004 — Split common/client source sets
**Status:** Accepted

Keep client rendering/UI code out of dedicated-server execution paths.

## ADR-005 — Vanilla-first cape rendering
**Status:** Accepted pending SPIKE-01

Substitute Loom cape texture/state while Minecraft retains geometry and movement.

## ADR-006 — Vanilla-first Elytra path
**Status:** Proposed pending SPIKE-03

Use the dedicated Elytra texture path first; add a narrow hook only if required.

## ADR-007 — Separate optional emissive pass
**Status:** Proposed pending SPIKE-06

Base cosmetics must not depend on shader-sensitive glow rendering.

## ADR-008 — No live edit-stream networking
**Status:** Accepted

Synchronize saved/equipped projects, not brush strokes or rendered animation frames.

## ADR-009 — Content-addressed project distribution
**Status:** Accepted

Use SHA-256 project identity/cache-miss transfer.

## ADR-010 — .loom is editable source of truth
**Status:** Accepted

Flattened textures are compiled/export products.

## ADR-011 — Reusable Loom UI kit
**Status:** Accepted

Build reusable UI controls, not screenshot-backed screens.

## ADR-012 — Documentation hard gate
**Status:** Mandatory

No meaningful pass is complete without documentation updates.

## ADR-013 — Optional compatibility mods
**Status:** Accepted

Sodium, Sodium Extra, Iris, shader packs, and 3D Skin Layers are not dependencies. Plain Fabric is the core runtime. Optional compatibility is actively supported and smoke-tested.

## ADR-014 — Proprietary licensing
**Status:** Accepted

Loom Studios remains proprietary / All Rights Reserved. Third-party test JARs are not bundled or committed.

## ADR-015 — dev-mods/ + modLocalRuntime
**Status:** Accepted

Optional compatibility JARs live in gitignored `dev-mods/` and are loaded only by the Loom development runtime.


## ADR-016 — Patch PlayerSkin in AvatarRenderer render-state extraction
**Status:** Implemented for SPIKE-01, runtime verification pending

For Minecraft 1.21.11, inject at the tail of `AvatarRenderer.extractRenderState(Avatar, AvatarRenderState, float)`.

For the static cape spike:
- affect only the local player;
- replace only the cape field using `PlayerSkin.Patch`;
- preserve body, Elytra, and model-type fields;
- force `showCape` only for the development spike;
- cache the patched skin to avoid allocating a new record every rendered frame.

This deliberately keeps vanilla `CapeLayer` responsible for geometry and movement.


## ADR-017 — Disable Gradle configuration cache for development
**Status:** Accepted

Loom Studios prioritizes a predictable IntelliJ + Fabric Loom development workflow over configuration-cache reuse.

Gradle configuration cache is disabled because Loom/custom development tasks and generated run configurations still have rough edges around cache-safe script closures. This avoids repeated false failures in helper tasks such as `listDevMods` and reduces friction during rapid client testing.


## ADR-018 — One registered DynamicTexture per active compiled cape
**Status:** Implemented for SPIKE-02, runtime verification pending

The dynamic-cape proof uses one long-lived `NativeImage` and one registered `DynamicTexture`.

Changes mutate the backing image and call `upload()` only when output is dirty. The texture identifier remains stable, so the player render-state patch does not churn resources or allocate a new skin record every update.

Texture resources are released during client shutdown.

This is the baseline ownership model for the future editor/compiler cache.


## ADR-019 — Cape and Elytra are independent texture channels
**Status:** Accepted

Minecraft 1.21.11's WingsLayer selects:
1. the player's dedicated Elytra texture when present;
2. otherwise the visible cape texture;
3. otherwise the equipment/default texture.

Loom Studios therefore supplies independent cape and Elytra textures by default. A future editor option may explicitly link or derive Elytra artwork from the cape, but automatic vanilla fallback must not silently substitute the cape when a Loom project intends separate designs.

## ADR-020 — Preserve vanilla Elytra geometry by default
**Status:** Accepted for core path

The default Loom Studios Elytra renderer keeps vanilla WingsLayer/ElytraModel geometry and animation for compatibility.

The vanilla model is intentionally volumetric (10x20x2 plus CubeDeformation 1.0 per wing), which can look thick. The first low-risk visual experiment hides edge-face UV strips through transparency.

A future optional "Slim Elytra" visual mode may use custom geometry if desired, but it must be opt-in and separately compatibility-tested.


## ADR-021 — Elytra thickness is a geometry setting, not a texture trick
**Status:** Accepted

Default Loom Studios Elytra rendering must preserve vanilla wing thickness.

User customization may expose an Elytra thickness control. The preferred implementation is local model-depth scaling while keeping vanilla WingsLayer animation and equipment behavior.

Proposed semantics:
- 100% = exact vanilla thickness;
- values below 100% = slimmer wings;
- values above 100% = chunkier/stylized wings;
- setting is purely visual and never changes collision/hitboxes.

Do not implement "thin Elytra" by making UV edge faces transparent. That changes visual surface coverage rather than actual geometry and produced an undesirable paper-thin result.


## ADR-022 — Preview uses isolated extracted render state
**Status:** Implemented for SPIKE-04, runtime verification pending

The Loom Studios GUI preview renders the real local player entity through Minecraft's normal entity renderer, but modifies only the extracted RenderState used for that GUI submission.

This allows preview-only changes such as:
- cape versus Elytra mode;
- preview equipment;
- later preview animation/pose options;

without equipping items or mutating the actual world/player entity.

The preview should reuse the same Loom cosmetic texture/render integrations as gameplay rather than maintaining a separate fake model pipeline.


## ADR-023 — Server-authoritative equipped hash with client-side compiled cache
**Status:** Implemented for SPIKE-05, runtime verification pending

The server owns the authoritative UUID -> equipped project hash mapping. Project blobs are content-addressed by SHA-256 and transferred only on cache miss.

Clients verify received blobs before caching/compiling them.

Players connected to a server without Loom Studios keep local-only Loom rendering because client sends are guarded by `ClientPlayNetworking.canSend`.

## ADR-024 — Animation time is evaluated locally
**Status:** Implemented for SPIKE-06, runtime verification pending

Animation definitions/parameters belong to project data. Clients derive animation phase locally from synchronized game time. Rendered texture frames are never continuously transmitted.

## ADR-025 — Emissive cape is an additive feature layer
**Status:** Implemented for SPIKE-06, runtime verification pending

The base cape remains Minecraft's normal CapeLayer. Loom Studios registers an additional player feature layer that submits a second PlayerCapeModel using the project's generated emissive mask and Minecraft's translucent-emissive render type.

If this optional layer has a compatibility problem, disabling it must leave the base cape intact.


## ADR-026 — User-facing Elytra 100% is a calibrated visual baseline
**Status:** Accepted from runtime feedback

Minecraft's raw 1.21.11 Elytra model uses a visibly volumetric wing box. With Loom Studios' fully opaque custom edge UVs, raw model `zScale=1.0` appears substantially fatter than the normal vanilla Elytra appearance.

For Loom-owned Elytra rendering:
- user-facing 100% thickness maps to raw model Z scale 0.5;
- thinner/thicker controls are relative to that baseline;
- non-Loom Elytras remain raw vanilla Z scale 1.0;
- custom edge-face artwork remains opaque but uses a recessed/darker treatment by default;
- this affects rendering only.

This supersedes the earlier assumption that raw model Z scale 1.0 should necessarily be the user-visible 100% baseline.

## ADR-027 — Schema-v1 project core is deterministic bounded binary data
**Status:** Accepted / implemented

The first real .loom project representation uses a deterministic bounded binary codec rather than Java object serialization.

Reasons:
- stable SHA-256 content identity;
- strict size/count validation before allocation;
- explicit schema versioning;
- straightforward server/client parity;
- no arbitrary-class deserialization surface.

Schema v1 currently contains project identity/name, runtime settings, cape/Elytra canvases and ordered paint layers.

Future structural changes require explicit schema migration.

## ADR-028 — Phase-1 undo/redo starts with immutable project snapshots
**Status:** Accepted / implemented foundation

Editor changes are expressed as project transformations through ProjectHistory.

The first implementation stores bounded immutable project snapshots because it keeps correctness simple while tools are still being designed.

High-volume paint operations may later store specialized deltas internally without changing the editor/session-facing undo/redo contract.


## ADR-029 — ProjectSession owns editor history and dirty state
**Status:** Accepted / implemented

The future editor must not independently track "changed" booleans alongside project history.

One ProjectSession owns:
- ProjectHistory;
- current project revision;
- persisted source path;
- last saved content hash;
- dirty-state calculation.

Dirty state is content-based: current project hash differs from the last saved hash, or the project has never been saved.

This naturally allows undoing back to the exact saved project to become clean again.

## ADR-030 — File storage core is independent from Fabric game-directory lookup
**Status:** Accepted / implemented

ProjectFileStore receives an explicit root Path and contains all bounded load/save/list behavior.

LocalProjectLibrary is only the Minecraft/Fabric adapter that points the store at the game's Loom Studios project folder.

This keeps file safety and save/load behavior unit-testable without starting Minecraft.

## ADR-031 — Every schema load passes an explicit migration gate
**Status:** Accepted / implemented

LoomProjectCodec does not silently treat unknown schema versions as current data.

Loading first inspects the schema version and dispatches through LoomProjectMigrations.

Schema v1 currently decodes directly because there is no released legacy schema. Future schema versions must add explicit migration logic.


## ADR-032 — ClientProjectWorkspace is the sole local editable project owner
**Status:** Accepted / implemented

Networking, rendering and the future editor must not each retain their own mutable local project copy.

ClientProjectWorkspace owns exactly one local ProjectSession. Consumers query the workspace project/hash/revision.

When the content hash changes, ClientCosmeticSync republishes the new hash through the existing cache-miss protocol.

## ADR-033 — GPU texture lifetime belongs to RuntimeCosmeticCache
**Status:** Accepted / implemented

Project/session state contains editable data only.

RuntimeCosmeticCache owns:
- NativeImage buffers;
- DynamicTexture objects;
- registered texture identifiers;
- animation-phase uploads;
- hash-keyed compiled runtime bundles.

PlayerCosmeticRenderer owns only render-state patching and small render-facing lookup/debug behavior.

## ADR-034 — Recent Projects index tolerates individual corrupt files
**Status:** Accepted / implemented

A single malformed .loom file must not prevent the library/home screen from opening.

ProjectLibraryIndex loads each project independently, logs/rejects broken entries, and exposes the count of rejected files.

Thumbnails are derived caches and are keyed by project UUID + content-hash prefix.


## ADR-035 — Editing state and equipped state are separate
**Status:** Accepted / implemented

ClientProjectWorkspace owns both concepts but they have different lifecycles.

Editing state:
- lives in ProjectSession;
- may be dirty;
- feeds future editor/canvas/preview widgets;
- supports undo/redo before saving.

Equipped state:
- is a saved immutable LoomProject snapshot;
- is what PlayerCosmeticRenderer shows in the world;
- is what ClientCosmeticSync publishes to the server;
- changes only through an explicit equip action.

`equipCurrent()` requires the session to be clean. `saveAndEquip()` performs the common combined action.

This prevents a paint stroke or undo operation from unexpectedly changing other players' view before the user saves/equips.

## ADR-036 — Workspace widgets observe immutable WorkspaceState snapshots
**Status:** Accepted / implemented foundation

ClientProjectWorkspace exposes listener registration using immutable WorkspaceState values containing:
- current project;
- revision;
- dirty state;
- source path;
- equipped project/hash.

Phase-2 UI widgets should react to these snapshots rather than polling unrelated global fields.


## ADR-037 — Editor preview uses a scoped render-state project override
**Status:** Accepted / implemented

The in-GUI player preview must show unsaved ProjectSession output without mutating the player's world/equipped state.

PlayerCosmeticRenderer therefore supports a scoped preview override only while the preview extracts the local player's render state.

Outside that scope:
- world renderer uses the equipped project;
- multiplayer publishes the equipped project;
- no global player entity/equipment mutation occurs.

Preview runtime texture bundles are derived caches and are evicted when the preview hash changes or the screen closes.

## ADR-038 — Project-library selection belongs to the library model
**Status:** Accepted / implemented foundation

ProjectLibraryIndex owns the currently selected project UUID and preserves it across refreshes when the project still exists.

If selection disappears, the newest remaining project becomes the default selection.

This gives the Phase-2 home/Recent Projects screen stable selection behavior independent of widget instances.


## ADR-039 — Phase-2 editor writes immutable project edits through the workspace
**Status:** Accepted / implemented

Editor widgets never mutate NativeImage/runtime textures directly.

Pixel tools produce a new LoomProject through ProjectEdits, then submit that project through ClientProjectWorkspace/ProjectSession.

Consequences:
- undo/redo remains authoritative;
- dirty state remains correct;
- preview compilation follows the same project data;
- save/equip/network behavior remains downstream of one model.

## ADR-040 — Functional editor UI precedes reference-fidelity polish
**Status:** Accepted

Phase 2 builds reusable controls and complete editing behavior first.

The approved reference images remain the visual target, but exact decorative framing/layout fidelity is deferred to the dedicated reference-fidelity phase so core editor behavior is not entangled with temporary screen scaffolding.


## ADR-041 — Cape editor is face-first, atlas-backed
**Status:** Accepted from first runtime editor feedback

The editable source remains a standard 64x32 cape atlas, but the primary user experience must not require understanding raw UV layout.

Default Cape Editor view:
- semantic Outside / Back face;
- local 10x16 coordinates;
- large pixel cells;
- explicit face name and size;
- hover/UV diagnostics.

Users may cycle to Inside, left/right edges, top and bottom.

A raw-atlas mode may exist later as an advanced view with UV guides.

## ADR-042 — New editor projects are static unless animation is explicitly enabled
**Status:** Accepted from first runtime editor feedback

Development proof projects may continue to hue-cycle for diagnostics.

Normal projects created through the editor start with:
- hue cycle OFF;
- emissive effect OFF;
- Elytra thickness 100%.

Animation is an explicit creative feature, never an accidental default.


## ADR-043 — Higher-detail cosmetics use uniform atlas resolution scales
**Status:** Accepted / implemented

Cape/Elytra artwork may use 1x, 2x, or 4x texture atlases:
- 64x32;
- 128x64;
- 256x128.

UV semantics remain based on the canonical 64x32 layout and scale uniformly. This lets Minecraft's normalized model UVs sample extra texel detail without changing cape/Elytra geometry.

Resolution changes resample editable layers and remain normal ProjectSession edits, so they participate in undo/redo and dirty state.

The same resolution system is shared by Cape and Elytra canvases even though the current UI exposes it only in Cape Editor.

## ADR-044 — High-resolution project blobs use Fabric packet splitting
**Status:** Accepted / implemented

A 4x editable project can exceed vanilla custom-payload packet limits.

Loom Studios registers project blob payloads through Fabric 1.21.11 `registerLarge`, while keeping strict application-level size/hash/schema validation.

Project serialized data remains bounded to 1 MiB in this phase.

## ADR-045 — Color selection is one synchronized RGB/HSV value
**Status:** Accepted / implemented foundation

The editor color control exposes multiple views of one selected ARGB color:
- saturation/value square;
- hue strip;
- RGB sliders + numeric values;
- hex display;
- palette swatches.

Changing any control updates the same selected color used by paint tools.


## ADR-046 — User palettes are first-class local assets
**Status:** Accepted / implemented

Custom color palettes are persisted independently from cape/Elytra projects.

A palette contains:
- UUID;
- user-visible name;
- ordered list of up to 32 opaque RGB colors.

Palette files use the shareable `.loompalette` JSON format under the Loom Studios game folder.

## ADR-047 — Palette sharing supports files and clipboard codes
**Status:** Accepted / implemented

Export performs both:
- write a human-readable `.loompalette` file to the exports folder;
- copy a compact versioned `LOOMPAL1:` share code to the clipboard.

Import first accepts a share code from the clipboard. If none is present, it scans the imports folder.

Imported palettes receive a new UUID so importing another person's palette cannot overwrite an existing local palette with the same source identity.

## ADR-048 — Palettes window is movable, pinnable editor chrome
**Status:** Accepted / implemented foundation

The Palettes window is an editor overlay rather than part of the vertical tool rail.

It may be moved by its title bar. Pin locks its position. This keeps palette switching reachable on compact GUI layouts without permanently consuming the canvas/tool-rail width.


## ADR-049 — Zoom changes the semantic canvas viewport, never project resolution
**Status:** Accepted / implemented

Canvas resolution (1x/2x/4x) and editor zoom are separate concepts.

- project resolution changes stored texture detail;
- zoom only changes how large the current semantic face is displayed;
- zoom never resamples project pixels;
- 100% means fit-to-editor viewport;
- zoom can increase to 800%;
- middle-mouse pan is available when the zoomed face exceeds the viewport.

This avoids conflating "more detail" with "bigger on screen."

## ADR-050 — Brush radius is visible before painting
**Status:** Accepted / implemented

Pencil and Eraser show a circular hover outline representing their current brush radius.

The radius preview is editor-only and does not alter project data.

Fill/Eyedropper retain a single-pixel hover target instead.

## ADR-051 — Floating editor overlays receive topmost input routing
**Status:** Accepted / implemented

Minecraft screen child iteration can allow an earlier underlying widget to receive input even when a later floating overlay is drawn visually above it.

CapeEditorScreen therefore routes mouse/focus/scroll events to a visible Palettes overlay first when the pointer is within that window.

This pattern should be reused by future floating Layers, Animation, Import, and Effects windows.

## ADR-052 — Flood Fill operates on the active layer inside one semantic face
**Status:** Accepted / implemented

Fill compares pixels on the active editable layer and performs four-connected flood fill.

It never crosses from the selected semantic cape face into another UV face.

Eyedropper differs intentionally: it samples the final composited visible face color so the selected color matches what the user sees.


## ADR-053 — Custom palettes use a grouped Swatches-dock model
**Status:** Accepted from runtime UX feedback / implemented

The palette UI should resemble professional graphics-editor swatch panels rather than a single selected-record form.

One floating Swatches window shows multiple named palettes at once.

Each palette:
- has a visible name/header;
- owns an ordered swatch grid;
- may contain up to 64 colors;
- can be selected for rename, Add Current, export, or delete;
- can be imported/exported independently.

Clicking any swatch immediately selects both the source palette and that paint color.

This keeps rapid color switching frictionless when editing.

## ADR-054 — Brush radius preview is transient
**Status:** Accepted from runtime UX feedback / implemented

The large circular Pencil/Eraser footprint indicator is a size-change aid, not a permanent cursor.

Normal editing:
- shows the simple target-pixel outline.

After Brush size changes:
- show the radius circle briefly;
- if the pointer is outside the canvas because the user clicked Brush +/- controls, show the radius preview at the center of the active semantic face;
- automatically return to the normal target outline.

## ADR-055 — Line and Rectangle commit once on mouse release
**Status:** Accepted / implemented

Line and Rectangle are drag-defined shape tools.

During drag:
- the canvas draws a cyan preview only;
- project data is not mutated continuously.

On mouse release:
- one immutable project edit is committed;
- the edit participates in normal ProjectSession history as one undo step.

Rectangle supports outline and filled modes. Outline uses the current Brush size.


## ADR-056 — Swatches management collapses on compact editor viewports
**Status:** Accepted from runtime screenshots / implemented

On compact logical viewports, especially 1920x1080 at Minecraft GUI scale 3, the Swatches tool must prioritize color selection over palette administration.

Compact mode:
- uses a smaller floating window;
- starts with management collapsed;
- keeps all named swatch groups visible/scrollable;
- exposes Edit/Done to reveal or hide management controls.

The swatch section position is derived from the current management state. Hard-coded text positions that can overlap the management rows are prohibited.

## ADR-057 — Selected paint color is ARGB, not RGB-only
**Status:** Accepted / implemented

The editor selected color now retains 8-bit alpha.

All color entry surfaces represent one synchronized ARGB value:
- HSV/SV + hue;
- R/G/B sliders;
- A slider;
- editable #RRGGBB field;
- editable R/G/B/A numeric fields;
- custom Swatches.

Opaque legacy palette colors remain valid. Non-opaque palette colors serialize as #AARRGGBB.

## ADR-058 — Symmetry is evaluated within the active semantic face
**Status:** Accepted / implemented

Symmetry never mirrors across unrelated cape UV faces.

Available modes:
- Off;
- Horizontal;
- Vertical;
- Both.

Pencil, Eraser, Fill, Line and Rectangle apply mirrored edits inside the current face dimensions at the active 1x/2x/4x resolution.

## ADR-059 — Layer stack order is source-order bottom to top
**Status:** Accepted / implemented UI foundation

LoomCanvas layer order remains render order:
- index 0 = bottom;
- last index = top.

The Layers UI displays this in graphics-editor convention with the top-most layer first.

Layer create/duplicate/delete/reorder/visibility/opacity are ProjectSession edits and therefore participate in undo/redo and dirty-state tracking.


## ADR-060 — Blend-mode enum ordinals are schema-v1 compatibility data
**Status:** Accepted / implemented

Schema v1 stores `BlendMode.ordinal()`.

Therefore:
- existing blend modes must never be reordered;
- new schema-v1-compatible modes may only be appended;
- automated tests pin the current ordinal mapping.

Current order:
1. Normal
2. Add / Glow
3. Screen
4. Multiply
5. Overlay

A future schema should encode stable identifiers rather than relying on enum ordinals.

## ADR-061 — Persistent layer lock waits for coordinated schema expansion
**Status:** Accepted

Layer lock must persist with the project.

It will not be implemented as temporary editor-only state and will not be jammed into schema v1 without a migration plan.

The next project-schema expansion should coordinate:
- lock state;
- layer kind/type;
- image/gradient layer payloads;
- future effect/animation metadata where appropriate.

## ADR-062 — Reference fidelity is continuous, not Phase-8-only
**Status:** Accepted

The five approved visual references remain active constraints while functionality is built.

Phase 8 remains the dedicated final polish/unification pass, but earlier phases should already preserve:
- screen hierarchy;
- panel roles;
- control density;
- major interactions;
- dark/cyan/violet Loom identity;
- live-preview placement intent;
- responsive behavior.

This reduces the chance of a final full UI rewrite.


## ADR-063 — Pixel selection is editor state, transforms are project edits
**Status:** Accepted / implemented foundation

The rectangular selection itself is not serialized into `.loom`.

It represents temporary editor state.

Operations performed on that selection are immutable project edits and therefore:
- update dirty state;
- participate in Undo/Redo;
- operate on the selected layer;
- stay inside the currently active semantic cape face.

This separation keeps project files free of accidental UI-session state while still making selection transforms deterministic and undoable.

## ADR-064 — Multiplayer publication reads equipped state, never dirty editor state
**Status:** Accepted / implemented

The local editable `ProjectSession` may be dirty and is allowed to feed the scoped editor preview.

World rendering and multiplayer publication must instead use:
- `ClientProjectWorkspace.equippedProject()`;
- `ClientProjectWorkspace.equippedProjectHash()`.

`ClientCosmeticSync` must not announce or upload dirty editor content.

This restores the boundary established by ADR-035 and makes Save versus Save + Equip meaningful.

## ADR-065 — Schema-v1 emissive master follows emissive cape layers
**Status:** Accepted / implemented

Schema v1 already stores both:
- per-layer `emissive`;
- project runtime `emissiveEnabled`.

Until schema v2 can simplify effect metadata:
- per-layer emissive flags are the render-time authority;
- editor operations keep the schema-v1 master synchronized as a compatibility mirror;
- at least one emissive cape layer -> runtime emissive master ON;
- no emissive cape layers -> runtime emissive master OFF.

Treating layers as render authority also protects older pre-fix projects whose stored master flag may disagree with their layer flags.

Blank editor projects still begin with no emissive layers and therefore no glow.

The synthetic SPIKE-06 shimmer stripe is not valid authored project output and has been removed.

## ADR-066 — Selection UI is temporary editor state and nudge is non-destructive to face bounds
**Status:** Accepted / implemented

`PixelSelection` remains non-serialized editor state.

The Cape Editor may:
- drag-select;
- show a persistent outline;
- move/nudge the selected pixels;
- flip horizontally/vertically;
- clear the selection.

Common transform primitives retain clipping behavior for reusable low-level operations.

The user-facing nudge controls clamp movement so the full selection remains inside the active semantic cape face. This avoids accidental pixel loss from a one-pixel nudge at an edge.

Changing semantic face or project resolution clears the temporary selection because its coordinates no longer describe the same editing surface.

Undo/Redo also clear the temporary selection. Project history does not serialize editor selection coordinates, so clearing avoids leaving a moved selection box pointing at stale post-transform coordinates after the project snapshot jumps backward or forward.

## ADR-067 — Smart Import processing core is schema-independent and bounded
**Status:** Accepted / implemented foundation

Smart Import transform/processing math lives in pure common code before schema-v2 Image layers or UI are introduced.

`PixelImage` is an immutable ARGB processing value with:
- maximum dimension 4096;
- maximum 16,777,216 pixels;
- exact pixel-count validation;
- defensive array ownership.

It is a temporary processing/preview/compiler representation, not the persistent Image-layer design.

Placement semantics are pinned:
- Fit = contain + preserve aspect + center + transparent letterbox;
- Stretch = fill target without preserving aspect;
- Crop = centered source crop preserving target aspect, then fill target;
- Center = no scale, centered with target clipping.

Crop never changes fixed Minecraft cape/Elytra UV surface dimensions.

The first raster transform set is mirror H/V, quarter-turn rotation, crop, nearest-neighbor resize and placement rendering.

Brightness/Contrast/Saturation use normalized -1..1 controls and preserve alpha.

Schema v2 should store editable source/transform/processing intent and may reuse this core to compile previews/output.

## ADR-068 — Smart Import Reduce Colors uses bounded deterministic histogram quantization
**Status:** Accepted / implemented foundation

Reduce Colors must remain deterministic and bounded even for the maximum accepted source raster.

The common-core quantizer therefore:
- uses a fixed 5-bit-per-channel RGB histogram with at most 32,768 occupied bins;
- ignores fully transparent pixels when deriving the palette;
- weights histogram bins by source pixel frequency;
- repeatedly splits the highest-impact color box along its widest RGB channel;
- produces at most 256 colors;
- maps visible source RGB to the nearest generated palette color;
- preserves original source alpha;
- returns already-within-limit artwork unchanged.

This avoids an unbounded map containing every distinct 24-bit source color while still adapting the palette to image content.

## ADR-069 — Palette Limited and dithering preserve source transparency
**Status:** Accepted / implemented foundation

Palette Limited processing treats the selected palette as an RGB constraint. Source alpha remains authoritative.

Floyd-Steinberg dithering:
- diffuses RGB error only;
- preserves source alpha;
- leaves fully transparent pixels byte-for-byte unchanged;
- does not diffuse error into or through fully transparent pixels;
- remains deterministic for the same source and ordered palette.

This prevents invisible/transparent areas from becoming color bridges during import processing.

Posterize and Monochrome follow the same alpha-preservation rule.

## ADR-070 — Schema v2 introduces typed layers with explicit v1 migration
**Status:** Accepted / implemented

Schema v2 coordinates the previously deferred structural changes instead of patching schema v1 piecemeal.

Schema-v2 layers have:
- stable layer-kind string IDs;
- stable blend-mode string IDs;
- persistent lock state;
- type-specific payloads.

Current kinds:
- Paint;
- Image;
- Gradient.

Schema-v1 bytes retain their original decoder and blend ordinals. Loading a v1 project explicitly migrates its Paint layers into an equivalent v2 project with lock disabled.

Unknown schema versions remain rejected.

## ADR-071 — Image layers preserve editable source, transform and processing intent
**Status:** Accepted / implemented

Smart Import never silently flattens imported artwork into Paint pixels.

A schema-v2 Image layer owns:
- bounded embedded source pixels;
- normalized source crop;
- normalized destination transform;
- semantic clip;
- processing mode/settings;
- optional palette.

Runtime/editor rasters are compiled products only.

This keeps Fit/Crop/position/scale/rotation/mirror/color-processing choices editable after save/reopen.

## ADR-072 — Typed-layer transforms are normalized to the Loom canvas
**Status:** Accepted / implemented

Image and Gradient layer placement is stored in normalized canvas coordinates.

Consequences:
- 1x / 2x / 4x project resolution changes preserve authored placement;
- typed layers do not need destructive pixel resampling when only the Loom backing resolution changes;
- arbitrary destination rotation is a persistent transform property;
- semantic target clipping remains independent of the source asset resolution.

Paint layers continue to resize their backing pixels when the project resolution changes.

## ADR-073 — Smart Import candidate preview is isolated from equipped/network state
**Status:** Accepted / implemented

The Smart Import workspace may build and preview a temporary candidate project before Apply.

That candidate is allowed to feed:
- Original/Processed/Texture previews;
- the scoped 3D preview.

It must not:
- replace the editable workspace project before Apply;
- equip the project;
- publish a multiplayer hash.

Applying commits the Image-layer edit into normal ProjectSession history. Save and Save + Equip keep their existing separate semantics.

## ADR-074 — Persisted Image sources are bounded independently from processing sources
**Status:** Accepted / implemented

The processing pipeline may temporarily accept images up to its common-core safety limits.

Persisted Image-layer source data is stricter:
- maximum 256 pixels per dimension;
- nearest-neighbor downscale before persistence when required;
- final project encode must still fit the 1 MiB `.loom` limit.

This prevents a single imported image from bypassing project/network size bounds while preserving a larger temporary source for the guided import workflow.



## ADR-075 — Refreshed five-screen UI reference set defines the visual contract
**Status:** Accepted

The 2026-10-02 refreshed five-screen reference pack is the canonical Loom Studios visual target.

Repository assets:
- `docs/references/ui/Loom_Studios_01_Home_Screen.webp`;
- `docs/references/ui/Loom_Studios_02_Cape_Editor.webp`;
- `docs/references/ui/Loom_Studios_03_Smart_Import.webp`;
- `docs/references/ui/Loom_Studios_04_Elytra_Animation_Editor.webp`;
- `docs/references/ui/Loom_Studios_05_Loom_Codes_and_Sharing.webp`.

Full-resolution PNG masters are also stored in the ChatGPT Project Library under `/Loom Studios/UI References/`.

These images are references, not screenshot specifications. The implementation must preserve hierarchy and identity while improving usability.

Visual rules:
- target roughly 70% clean modern creative editor / 30% magical Minecraft workshop;
- Home and Sharing are showcase-oriented and may carry more decoration;
- Cape Editor, Smart Import and Elytra/Animation are work-oriented and should reduce chrome around the task;
- the main canvas/result/preview should be the dominant focal area;
- cyan/violet glow primarily indicates selected, active, focused or primary states;
- inactive controls stay quieter;
- icons should communicate the purpose of tools/buttons/layer types wherever practical;
- use spacing/surface contrast instead of nesting borders around every region;
- responsive usability outranks decorative fidelity at constrained GUI scales.

The refreshed set supersedes the earlier rough concept references.


## ADR-076 — Gradient authoring uses bounded common-core transform helpers
**Status:** Accepted / implemented

Gradient transform controls must not embed one-off geometry math inside the Minecraft screen.

The common-core `GradientAuthoring` helper owns the deterministic authoring operations used by the Cape Editor:
- translation uses 5% of the current Gradient width/height per UI step;
- uniform scale preserves aspect and remains inside `LayerTransform` bounds;
- horizontal and vertical mirrors toggle independently;
- reset restores the Gradient transform to its semantic clip, zero rotation and no mirrors;
- scale display is relative to the semantic clip.

These operations update only existing schema-v2 `GradientLayerData` / `LayerTransform` state and therefore require no project-format migration.

The Layers list also exposes visibility, typed identity and persistent lock as direct row affordances. Lock remains normal project state and participates in ProjectSession history.


## ADR-077 — Elytra authoring uses semantic wing regions
**Status:** Accepted / implemented

The Elytra editor works in semantic unfolded wing space rather than exposing the raw 64x32 atlas.

Current authoring regions:
- Left Wing: atlas origin 24,2, logical size 10x20;
- Right Wing: atlas origin 36,2, logical size 10x20.

The Right wing's vanilla UV orientation is mirrored relative to the Left. Linked Mirror authoring therefore copies a local edit into the opposite wing using `mirroredX = semanticWidth - 1 - localX`.

The mapping scales with the project's 1x/2x/4x backing resolution.

Linked Mirror versus Separate Wings is transient editor mode, not serialized project meaning. Actual authored pixels remain ordinary Elytra layer data.

## ADR-078 — Elytra thickness is project-authored runtime state
**Status:** Accepted / implemented

`LoomRuntimeSettings.elytraThickness` is the single runtime authority for Loom Elytra depth.

The semantic Elytra Editor exposes 25%–200% in 25-point steps, matching the schema's existing 0.25–2.0 bound.

The old development V-key thickness preset override is retired because it could override the saved/equipped project value for the local player. Preview, equipped local rendering and remote rendering now read the same project-authored value.

No schema migration is required because the thickness field already existed in schema v1/v2 runtime settings.

## ADR-079 — Layer list widgets are canvas-backed, not Cape-specific
**Status:** Accepted / implemented

The typed layer-list UI consumes a `LoomCanvas` supplier rather than hard-coding `project.cape()`.

This allows Cape and Elytra editors to share:
- Paint/Image/Gradient type iconography;
- selected-row treatment;
- visibility affordance;
- persistent lock affordance;
- opacity display;
- scrolling behavior.

Target-specific edits remain in the owning editor/ProjectEdits path.


## ADR-080 — Elytra Smart Import uses semantic twin Image layers
**Status:** Accepted / implemented

The two Elytra wing-front UV regions are disjoint. Smart Import must not pretend they form one rectangular editable target.

A linked Elytra import therefore creates:
- one Image layer clipped to the semantic left wing;
- one Image layer clipped to the semantic right wing;
- the right-wing transform mirrors horizontal placement relative to the left.

The source remains embedded/editable through normal schema-v2 Image-layer data. Runtime compilation still flows through the shared typed-layer rasterizer.

Existing Elytra Image layers can be reopened as a single target in Smart Import.

## ADR-081 — Cape-to-Elytra conversion is an editable Paint starting layer
**Status:** Accepted / implemented

Cape-to-Elytra conversion compiles the current cape Outside face, aspect-fits it into the semantic Elytra wing face, mirrors it into the opposite wing and adds the result as a new Paint layer.

The operation does not:
- overwrite the existing Elytra stack;
- resize the fixed Minecraft UV layout;
- introduce a new schema field.

This makes conversion a reversible/undoable authoring action and preserves the original cape project.

## ADR-082 — Schema v3 owns authored animation
**Status:** Accepted / implemented

Animation authoring is first-class project data rather than additional meaning hidden inside legacy runtime flags.

Schema v3 appends project duration/loop/playback speed plus bounded layer-targeted tracks and ordered keyframes. Migration is explicit: v1 -> v2 -> v3, v2 -> v3, and v3 direct. Migrated v1/v2 projects receive an empty default timeline.

The existing runtime hue-cycle flag remains compatibility behavior only. New authoring should use schema-v3 tracks.

## ADR-083 — Animation tracks must reference live layer IDs
**Status:** Accepted / implemented

A track's layer UUID must exist in the track's declared Cape/Elytra channel. Project validation rejects orphan references.

Layer deletion removes targeting tracks before replacing the canvas, keeping immutable project validation atomic and preventing dangling animation data.

## ADR-084 — Timeline preview uses a fixed-tick preview cache
**Status:** Accepted / implemented

Timeline scrubbing must not mutate the real player, equip the dirty project, publish network state, or allocate a fresh texture identifier for every scrubbed tick.

The scoped player preview therefore uses a stable preview-only hash and a runtime bundle with an optional fixed timeline tick. Scrubbing updates that bundle's texture contents in place. Fixed-tick preview bundles are excluded from normal game-time animation updates.

## ADR-085 — Reference 04 uses a compact canvas-above / timeline-below work layout
**Status:** Accepted / implemented

The approved Elytra + Animation reference remains the composition target, but work-mode usability outranks screenshot reproduction.

The current Elytra editor keeps the unfolded wing canvas dominant, docks the compact timeline beneath it, keeps the 3D preview/layer actions on the right, and uses restrained cyan/violet emphasis for selected/active timeline state.

## ADR-086 — LSP1 is the offline portable project code
**Status:** Accepted / implemented

Loom Studios needs project sharing that works before any hosted service exists.

`LSP1:` is a self-contained versioned portable code containing a DEFLATE-compressed bounded `.loom` project encoded as URL-safe Base64.

Decode rules:
- explicit `LSP1:` prefix;
- encoded-length bound;
- compressed-byte bound;
- bounded decompression;
- final normal Loom project codec validation/migration.

This makes offline copy/paste sharing real without inventing a server dependency.

## ADR-087 — LS short IDs are fingerprints until a resolver exists
**Status:** Accepted / implemented

`LS-XXXX-XXXX-XXXX` is currently a human-readable deterministic fingerprint derived from the project's content hash.

It is **not** represented as a globally resolvable cloud share code.

A future hosted service may register this fingerprint or issue another identifier, but product copy must not imply online resolution before a real backend exists.

## ADR-088 — Imported shared projects fork identity
**Status:** Accepted / implemented

Importing another person's portable code or external `.loom` project creates a new local project UUID and appends an Imported suffix to the local name.

Cape, Elytra, runtime and animation authoring data remain intact.

This prevents a received design from overwriting a local library entry that shares the sender's original project UUID.

## ADR-089 — Sharing exports never silently overwrite prior exports
**Status:** Accepted / implemented

Loom Codes local exports live under `.minecraft/loom-studios/exports`.

Editable project, portable-code text and PNG exports allocate unique numbered filenames if a prior file already exists.

The sharing UI exposes local/private behavior explicitly. Hosted visibility/permission controls remain unimplemented rather than cosmetic placeholders that claim a service exists.

## ADR-090 — Progressive disclosure replaces persistent button walls
**Status:** Accepted / implemented in reference UI refactor

The pre-refactor editor exposed most authoring controls simultaneously, often inside long vertical scrolling columns.

Local testing showed that this made the product difficult to understand even when the underlying features worked.

The primary editor architecture is now:
- compact icon tool rail for persistent tools;
- contextual canvas controls;
- one inspector context at a time;
- tabs for Layers / Color / task-specific Properties or Animation;
- hidden irrelevant controls rather than disabled clutter.

Scrolling remains appropriate for genuinely variable collections such as large layer, swatch, or track lists, but not for the entire primary editor control surface.

## ADR-091 — 640x360 effective GUI space is a first-class compact breakpoint
**Status:** Accepted / implemented

1920x1080 at Minecraft GUI scale 3 was the clearest failure profile in the local test.

Loom Studios therefore treats <=700x420 effective GUI size as a first-class compact layout rather than a desktop layout that happens to be scaled down.

Compact behavior includes:
- icon-only primary tool/navigation buttons;
- reduced project/template counts;
- collapsed secondary text;
- smaller inspector rows;
- timeline secondary-control collapse;
- preserved canvas/preview priority.

The critical mental test size is approximately 640x360.

## ADR-092 — Export must be directly discoverable from authoring screens
**Status:** Accepted / implemented

Export functionality previously existed in Loom Codes but was not discoverable during normal Cape/Elytra authoring.

Cape and Elytra now expose **Share / Export** directly in persistent top navigation.

The sharing screen is split into Export and Import. Export immediately surfaces editable .loom, portable code, Cape PNG and Elytra PNG.

## ADR-093 — Nonfunctional destinations do not occupy primary Home navigation
**Status:** Accepted / implemented

A disabled Settings card contributed visual noise without providing value.

Settings is removed from the Home primary action stack until there is a real preferences product surface.

## 2026-10-02 — Shared canvas boundaries and bounded workspace

**Accepted.** The failed screenshots showed live selection using pixel centers while committed selection used boundaries, and controls escaping their inspector ownership. Use one integer transform for texture/input/grid/shape/selection, with exclusive screen right/bottom edges. Do not draw a brush hover cell over a selection gesture as a false handle.

Use one shared bounded layout for Cape/Elytra. Ordinary controls fit contextual pages; only layers/swatches/tracks scroll. This supersedes earlier ADRs prescribing a whole right-side ScrollableLayout. Compact 640×360 is a first-class composition. The timeline yields space when empty; layer properties and animation Playback have explicit owners. Keep workshop decoration slim on work screens and stronger on showcase screens.

Add an opt-in development capture runner to obtain actual Minecraft evidence. Builds and pure layout tests are necessary but cannot establish visual acceptance. Production runs do not execute capture automation.


## 2026-10-03 — Shared workshop artwork and reserved chrome

Use code-native pixel artwork for the timber shell, steel fasteners, lanterns, pennants, brand wordmark and scenic preview. It scales with Minecraft GUI coordinates and needs no stretched screenshot backdrop. The 36/56-unit header is part of the layout contract; inset highlights never enlarge control bounds. Smart Import uses source/settings/result/live-preview ownership with fixed bottom actions. All five screens must pass the four physical-resolution/GUI-scale profiles, including visible-control bounds and nonoverlap, before visual acceptance.

## 2026-10-03 — preview and windowed input/layout ownership

A GUI preview owns a fresh entity snapshot and sets a coherent rear three-quarter body/head angle; the live player is never rotated/equipped for the preview. Immutable project identity caches the expensive codec hash and separate preview namespace; exported/equipped textures keep authored alpha. Preview-only checkerboard makes blank/transparent garment geometry judgeable and is labeled Alpha guide. Render-state reuse was investigated using Fabric renderer API and NeoForge issue #2500 (older Minecraft versions); it remains a candidate until current-version captures establish the visible outcome.

Screen owns middle-button capture until release because container drag routing is left-button oriented. Circle live feedback and commit share PixelShapes’ inclusive pixel-centre ellipse; existing editable-layer/brush/lock/symmetry/history contracts apply. At 600×320 logical pixels controls page/scroll in bounded regions, rows compress to 19 pixels, timeline height reserves a 90-pixel canvas, outer margin is eight and bottom content ends 28 pixels above the window edge. UI selection uses one outline. Floating palette is an intentional overlay with explicit close/Escape and pointer priority. Native pixel icons and compact paged inspectors remain reference adaptations.

Feedback architecture verified at `b7f7212388858109b96f3e8fc8a7175bc3ef8c45` in Actions #191; fresh snapshots, explicit GUI rear-view quaternion, separate preview namespaces/cache, alpha isolation and routed input passed. No schema/network change.
