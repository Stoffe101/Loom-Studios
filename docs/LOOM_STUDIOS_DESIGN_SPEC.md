# Loom Studios — Design Specification

**Status:** Canonical product/design specification  
**Target:** Minecraft Java Edition 1.21.11  
**Loader:** Fabric Loader 0.18.4 compatibility baseline  
**Project:** Loom Studios  
**Primary workstation block:** Cape Loom

## 1. Vision

Loom Studios is an in-game creative studio for designing, editing, animating, importing, previewing, equipping, exporting, and sharing custom capes and Elytra designs.

The experience should feel like a premium Minecraft feature rather than a desktop editor awkwardly pasted into the game.

The central interaction is the **Cape Loom** block. Right-clicking it opens the Loom Studios interface.

Players with Loom Studios installed should automatically see one another's equipped Loom cosmetics on compatible multiplayer servers.

Loom Studios does not replace Mojang's official cape entitlement system. When no Loom cape is equipped, official/vanilla behavior remains available according to final precedence settings.

## 2. Product principles

1. Create inside Minecraft.
2. Live feedback while editing.
3. Non-destructive project editing.
4. Multiplayer-first architecture.
5. Efficient rendering and caching.
6. Safe server validation.
7. Portable designs and sharing.
8. Graceful fallback when optional effects or mods are unavailable.
9. Versioned/migratable project data.
10. Strong Minecraft-friendly visual identity.
11. Sodium, Sodium Extra, Iris/shaders, and 3D Skin Layers are optional compatibility targets; none are required to use Loom Studios.
12. Documentation is part of implementation and is a hard completion gate.

## 3. Visual identity

The approved Loom Studios references establish:

- dark slate/charcoal working panels;
- warm wood, cloth, metal-bracket, and lantern framing;
- cyan and violet accent lighting;
- pixel-friendly typography;
- Minecraft-inspired icons;
- premium but readable control density;
- live 3D player previews;
- restrained parchment/loom decorative elements;
- modern application hierarchy without losing the Minecraft atmosphere.

The implementation should get recognizably close to the reference images while remaining usable at multiple GUI scales and resolutions.

Heavy decorative wood should frame the experience but not consume excessive editor workspace.

## 3.1 Interaction density and progressive disclosure

The approved references are not permission to expose every feature at once.

Loom Studios must follow these interaction rules:

- persistent high-frequency tools use icon-led compact controls;
- the canvas/task remains visually dominant;
- only one inspector context is shown at a time;
- contextual controls appear when the selected tool/layer/task needs them;
- primary editor operation should not depend on scrolling a giant settings column;
- collections may scroll, but the whole editor chrome should not;
- compact mode at approximately 640x360 must be designed intentionally;
- future/unimplemented destinations should not occupy primary navigation;
- Export must be discoverable directly from Cape/Elytra editing.

The reference hierarchy is more important than literal screenshot replication:
- Home/Sharing may carry more workshop decoration;
- Cape/Elytra/Smart Import prioritize clean work surfaces;
- decorative frames must never steal space required for editing.
## 4. Home / start screen

The Cape Loom opens a Loom Studios home screen with:

- Create New Cape
- Edit Elytra
- Load Design
- Import Image
- Loom Codes
- Settings
- Recent Projects
- Templates
- 3D preview of selected/recent design

Recent project cards should display thumbnails and names.

Template categories can include:
- Blank
- Gradients
- Nature
- Space
- Fantasy
- Emblems

## 5. Cape editor

The main cape editor contains:

### Canvas

- pixel grid
- zoom
- pan
- symmetry guide
- visible cape UV/canvas boundaries
- optional checker transparency background

### Core tools

- Pencil
- Eraser
- Fill
- Eyedropper
- Line
- Rectangle
- Gradient
- Move
- Crop
- Flip Horizontal
- Flip Vertical
- Undo
- Redo

Future-friendly tools may include ellipse/selection/text if appropriate, but they are not required for the first editor milestone.

### Brush controls

- size
- opacity
- symmetry off/vertical/both as applicable
- selected color
- palette

### Color system

- RGB
- HSV/HSL-style picker as practical
- hex entry
- saved palette
- recent colors
- eyedropper

## 6. Layers

Projects use non-destructive layers.

Layer operations:
- add
- delete
- duplicate
- rename
- reorder
- show/hide
- lock
- opacity
- blend mode

Initial useful blend modes:
- Normal
- Add / Glow
- Screen
- Multiply
- Overlay if implementation is clean and performant

Layer types:
- paint/pixel layer
- gradient layer
- imported image layer
- effect layer
- optional reference layer

Example project stack:
- Border
- Glow
- Highlights
- Moon Logo
- Base Gradient

## 7. Gradients

Gradient editing is a first-class feature.

Support:
- linear
- radial
- multiple color stops
- movable stops
- angle/direction
- opacity
- repeat where useful
- optional dithering

Gradients remain editable rather than being immediately flattened.

## 8. Smart Image Import

Image import is designed to make real photos, logos, and artwork survive low-resolution cape/Elytra output.

Supported source format at minimum:
- PNG

Additional common formats may be added later if they can be handled safely and consistently.

### Placement / transform

- Fit
- Stretch
- Crop
- Center
- Keep Aspect Ratio
- Mirror Horizontally
- Mirror Vertically
- Rotate
- move
- scale
- layer opacity
- tint

### Image processing

- Reduce Colors
- Dither
- Brightness
- Contrast
- Saturation
- transparency handling
- background removal/transparent-background workflow where technically reasonable

### Import modes

- Direct
- Pixel-art
- Outline Only
- Monochrome
- Palette Limited
- Posterize

Useful later additions:
- edge detect
- dithered pixel-art
- reference-only import

### Import preview

Show:
- original
- processed result
- resulting cape/Elytra texture
- live 3D player preview

Import actions:
- Apply Import
- Import as New Layer
- Use as Reference Layer
- Cancel

Imported image layers remain editable after import.

## 9. Elytra editor

Cape and Elytra are separate but related editing targets.

The Elytra editor provides:
- unfolded wing canvas
- linked/mirrored wing editing
- separate wing editing
- shared color/tool system
- imported image support
- layers
- gradients
- animation
- live 3D preview

Preview states:
- standing
- wings visible
- open/gliding
- rotate/zoom

A cape design may optionally be used as a starting point for an automatically generated matching Elytra design.

## 10. Live 3D preview

The preview is a core feature, not a final polish item.

Capabilities:
- rotate
- zoom
- front/back/side viewing
- cape mode
- Elytra mode
- Elytra open/closed/gliding mode
- optional armor toggle
- player skin
- animation playback
- screenshot/reference framing as practical

Edits should update the preview in real time.

Preview state is temporary and must not change the actual equipped multiplayer cosmetic until the user chooses to save/equip/commit.

## 11. Animation system

Animated layers are mandatory.

Initial animation types:
- Pulse
- Scroll
- Hue Shift
- Moving Gradient
- Sparkle
- Emissive Glow

Animation UI:
- timeline
- play/pause
- loop
- speed
- duration
- track rows
- keyframes where appropriate
- procedural effect parameters where keyframes would be unnecessary

Animations should be deterministic and evaluated locally.

Do not network rendered frames.

## 12. Emissive and glow effects

Selected layers may be flagged for emissive/additive rendering.

The base cape/Elytra remains independent from optional glow passes.

If a shader pack or rendering configuration cannot support a particular optional effect cleanly:
- keep the base cosmetic visible;
- disable only the incompatible optional effect;
- avoid crashes;
- provide concise diagnostics.

## 13. Import/export

### Editable project

The primary editable file is a versioned **.loom** project.

It preserves:
- layers
- gradients
- imported assets
- transforms
- blend modes
- animation
- effect metadata
- project metadata

### Flattened export

Allow final texture export such as:
- cape PNG
- Elytra PNG

Flattened export is not the editable source of truth.

## 14. Loom Codes

Loom Codes provide easy sharing.

### Short code

Human-friendly example:

LS-7F4A-K92Q-XP31

A short code resolves stored project/share data on the applicable server/service.

### Portable code

Versioned self-contained representation such as an LSP1-prefixed payload.

Portable codes can move projects between worlds/servers/devices without relying on the original server, subject to strict size limits.

### Private chat UX

Generating a code can create a message visible only to the player.

Desired interaction:
- left-click copies the code;
- buttons/actions can include Copy, Import, Favorite, Preview;
- code is never broadcast automatically to public server chat.

## 15. Multiplayer

Players with Loom Studios installed should see other Loom Studios cosmetics automatically.

High-level flow:
- player equips saved project;
- server validates the request;
- equipped state references a content hash;
- remote clients learn the hash;
- clients with the project cached render immediately;
- cache misses request the project;
- project is validated and cached;
- texture is compiled locally.

Players without the mod continue normal gameplay and simply do not see Loom Studios custom cosmetics.

## 16. Permissions and visibility

Possible design visibility modes:
- Private
- Friends or allow-list style sharing where implemented
- Server-visible
- Public Template if a future gallery/service exists

Server owners need configuration for:
- whether custom capes are allowed;
- whether custom Elytra is allowed;
- project size limits;
- layer limits;
- animation limits;
- imported asset limits;
- sharing permissions;
- expensive effect controls.

## 17. Rendering direction

Base cape and Elytra rendering should use vanilla/Fabric-compatible paths as much as possible.

Goals:
- retain normal cape physics/movement;
- preserve armor interaction;
- preserve Elytra precedence;
- minimize conflicts with Sodium and Iris;
- avoid replacing broad renderer systems.

Dynamic texture updates should occur only when compiled output changes.

## 18. Sodium / Iris / shaders / 3D Skin Layers

These are optional supported integrations, not Loom Studios dependencies. Compatibility should be tested in:

1. Fabric only
2. Fabric + Sodium
3. Fabric + Sodium + Iris, shaders OFF
4. Fabric + Sodium + Iris, shaders ON

Loom Studios must work on plain Fabric without Sodium, Sodium Extra, Iris, shader packs, or 3D Skin Layers.

Shader packs must not be able to turn a failed optional glow pass into a missing base cape or hard crash. 3D Skin Layers should be smoke-tested around player feature rendering but needs no dedicated integration unless a real conflict is found.

See COMPATIBILITY.md and TEST_MATRIX.md.

## 19. Performance

Rules:
- no per-frame image decoding;
- no per-frame project parsing;
- no animation-frame network streaming;
- cache compiled static textures;
- update dynamic textures only when dirty;
- cache remote projects/textures by hash;
- provide quality/disable options for expensive effects;
- limit abusive project complexity server-side.

## 20. Safety and validation

Untrusted project/network/import data must be bounded.

Validate:
- serialized byte size
- decompressed size
- image dimensions
- layer count
- keyframe count
- animation count
- asset count
- enum/type identifiers
- schema version
- content hash
- compression behavior

Malformed projects must fail cleanly.

## 21. Custom UI library

Build reusable controls rather than hand-coding every screen independently.

Planned component family:
- LoomPanel
- LoomButton
- LoomTab
- LoomSlider
- LoomDropdown
- LoomTooltip
- LoomIconButton
- LoomScrollPanel
- LoomLayerRow
- LoomTimeline
- LoomColorPicker
- LoomCanvas
- LoomPlayerPreview

This is essential for keeping the visual language consistent and making later polish affordable.

## 22. Approved reference screens

The visual reference set includes:

1. Home / Start Screen
2. Cape Editor
3. Smart Import
4. Elytra + Animation Editor
5. Loom Codes / Share / Import / Export

Binary references belong under docs/references/ui/.

## 23. Initial development gate

Before the full UI is built, complete the technical spike phase:

- SPIKE-00 Toolchain
- SPIKE-01 Static Cape
- SPIKE-02 Dynamic Texture
- SPIKE-03 Elytra
- SPIKE-04 Editor Preview
- SPIKE-05 Multiplayer
- SPIKE-06 Animation/Emissive Compatibility

See SPIKE_PLAN.md.

## 24. Licensing

Loom Studios is proprietary / All Rights Reserved.

The repository LICENSE governs official use.

Do not add MIT, Apache, GPL, or another open-source license without an explicit project decision replacing the current proprietary direction.

Third-party components retain their own licenses.

## 25. Documentation governance

Documentation is a hard rule.

Every meaningful pass updates:
- CURRENT_STATE.md
- PASS_LOG.md
- NEXT_WORK.md
- relevant technical document
- DECISIONS.md when architecture changes
- TEST_MATRIX.md when verification changes

An implementation is not DONE only because it compiles.

## 26. Definition of the desired experience

A player should be able to:

1. craft/place a Cape Loom;
2. right-click it;
3. open a polished Loom Studios home screen;
4. start a cape or Elytra project;
5. paint pixels or use gradients/layers;
6. import an image and process it intelligently;
7. see every meaningful edit on a live player preview;
8. animate selected layers;
9. save and equip the design;
10. walk/fly around with normal Minecraft movement;
11. have other compatible players automatically see it;
12. export the project or texture;
13. generate a private clickable Loom Code and share it intentionally.

That is the core Loom Studios promise.


## 27. Elytra geometry customization

Loom Studios should preserve vanilla Elytra geometry by default while allowing optional cosmetic geometry customization.

### Thickness

The editor may expose an **Elytra Thickness** control.

Requirements:
- default is 100%, matching vanilla Minecraft;
- thinner and thicker values are render-only;
- no gameplay, collision, physics, durability, or hitbox changes;
- vanilla wing pose/gliding animation remains authoritative;
- texture artwork is independent from thickness;
- reset-to-vanilla is always available.

A model-depth scaling approach is preferred over hiding texture edge faces.

Potential future advanced geometry controls, only if stable:
- wing width
- wing length
- back offset
- resting spread/angle

These advanced options are not required for the first editor release and must remain compatible with vanilla gliding animation and optional rendering mods.


## 28. Elytra editor — geometry controls

The Elytra editor must include a geometry section separate from artwork/layers.

### Required v1 control

**Thickness**
- default: 100% vanilla
- thinner values reduce visible wing depth
- thicker values increase visible wing depth
- render-only cosmetic setting
- no hitbox, collision, flight, durability, or gameplay effects
- **Reset to Vanilla** action

The final slider range should be chosen from runtime usability testing rather than copied blindly from the temporary debug presets.

### Development note

The temporary V-key thickness cycler is only a technical-spike tool and must not be part of the finished user experience.


## 29. Player preview camera and facing controls

The final live 3D preview should separate **character orientation** from **viewer/orbit orientation**.

### Player Facing

Provide a simple facing/body-orientation control:
- horizontal angle control or slider;
- 0–360 degree range;
- snap/reset to back-facing default;
- optional front/back/left/right quick presets.

This changes how the character body is facing inside the preview.

### 3D Orbit / Pivot

Provide a separate free inspection control:
- full 360-degree horizontal orbit;
- limited or comfortable vertical orbit/pitch;
- mouse drag remains supported;
- visual trackball/orbit-style control is preferred over forcing users to type angles.

This changes the viewer's inspection angle rather than the character's intended facing.

### Head behavior

The preview should not make the head conspicuously chase the orbit camera by default.

Preferred behavior:
- head stays naturally aligned with the body during ordinary design inspection;
- an optional head-facing control may be added later if useful;
- a reset action restores neutral body/head orientation.

### Zoom

Keep mouse-wheel zoom and later add a visible zoom control in the final editor.

These controls belong to the final preview/editor UI and should be implemented as part of a bundled preview polish pass rather than treated as separate foundation spikes.


## Swatches dock interaction reference

The custom palette surface should follow the interaction model of professional graphics-editor Swatches panels.

Visual/UX direction:
- one compact floating dock titled Swatches;
- multiple saved palette groups shown together;
- each group has a name/header followed immediately by its swatch grid;
- dense square color chips rather than large full-width palette cards;
- scrolling applies to the grouped swatch area;
- palette-management controls remain compact and secondary to the swatches themselves;
- Pin/move behavior remains available for editor workflow customization.

The goal is fast cross-palette color picking: a user should be able to see Pastels, Nether, and Cyberpunk simultaneously and click any color directly.

Brush-radius visualization is intentionally temporary. The canvas should remain visually clean during ordinary drawing.
