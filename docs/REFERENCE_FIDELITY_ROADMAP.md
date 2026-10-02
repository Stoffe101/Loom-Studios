# Loom Studios — Reference Fidelity Roadmap

**Status:** Canonical visual/product tracking document

**Approved visual baseline:** 2026-10-02 refreshed five-screen reference set, committed under `docs/references/ui/`.

The approved Loom Studios reference pack contains five target screens:

1. `Loom_Studios_01_Home_Screen.webp`
2. `Loom_Studios_02_Cape_Editor.webp`
3. `Loom_Studios_03_Smart_Import.webp`
4. `Loom_Studios_04_Elytra_Animation_Editor.webp`
5. `Loom_Studios_05_Loom_Codes_and_Sharing.webp`

The implementation is not expected to reproduce each image pixel-for-pixel. It **is** expected to preserve their hierarchy, interaction model, visual identity and major feature placement while deliberately reducing unnecessary visual density.

## Global visual contract

Across all five screens, preserve:

- dark slate/charcoal editor surfaces;
- cyan and violet accent lighting;
- warm wood / loom / cloth / metal framing where decorative space allows;
- compact Minecraft-readable controls rather than oversized desktop widgets;
- pixel-friendly typography;
- clear icon-led tool grouping;
- strong panel hierarchy;
- restrained decorative framing around a practical working area;
- live 3D player/cosmetic preview where the reference shows it;
- responsive layouts that still work at 1920x1080 GUI scale 3;
- icon-led tools/buttons whose glyph communicates their purpose;
- roughly 70% clean modern editor / 30% magical Minecraft workshop;
- showcase mode for Home/Sharing and calmer work mode for Cape/Smart Import/Elytra;
- cyan/violet glow reserved mainly for active, selected or primary controls.

Reference-image fidelity must never reintroduce text overlap, clipped controls, unusable fixed-height panels, or giant floating windows.

## Reference 01 — Home / Start Screen

### Target experience

The home screen is the project hub rather than a temporary launcher.

Reference-oriented structure:

- Loom Studios title / branded frame;
- primary creation/navigation controls;
- Create New Cape;
- Edit Elytra;
- Load Design;
- Import Image;
- Loom Codes;
- Settings;
- Recent Projects with visual thumbnails;
- Templates;
- selected-project or player 3D preview.

### Current status

Implemented in the reference-shell pass:
- responsive three-column project hub;
- icon-led Create / Elytra / Load / Import / Loom Codes / Settings hierarchy;
- Create New Cape;
- Edit Elytra;
- Load Design;
- Import Image -> Smart Import;
- real Recent Project thumbnail cards from the cached PNG thumbnails;
- selected-project 3D preview with drag/zoom/reset;
- functional Blank and Gradient template cards;
- Nature / Space / Fantasy / Emblems placeholder cards;
- project count/status footer;
- unreadable-project warning state.

Remaining/reference gap:
- project Loom Codes action remains inactive until sharing exists;
- Settings action remains inactive until settings product work exists;
- additional template packs are placeholders;
- final wood/metal/cloth decorative treatment;
- local visual verification at all mandatory GUI profiles.

## Reference 02 — Cape Editor

### Target experience

This is the primary graphics-editor surface.

Reference-oriented structure:

- dominant central canvas;
- compact icon/tool controls;
- professional color controls;
- Swatches dock;
- Layers stack;
- live preview access;
- undo/redo/save/equip;
- readable status and project identity;
- responsive compact mode.

### Current status

Implemented:
- semantic cape-face editing;
- 1x / 2x / 4x project resolution;
- zoom and pan;
- Pencil / Eraser;
- Fill / Eyedropper;
- Line;
- Rectangle outline/filled;
- temporary brush-radius preview;
- symmetry Off / Horizontal / Vertical / Both;
- HSV/SV + hue picker;
- editable Hex / R / G / B / A;
- alpha-aware painting;
- custom grouped Swatches;
- multiple named palettes + sharing;
- layer create/duplicate/delete/reorder;
- layer visibility;
- layer opacity;
- layer rename;
- Normal / Add-Glow / Screen / Multiply / Overlay blend modes;
- emissive-layer toggle;
- undo/redo;
- save/equip;
- unsaved 3D preview;
- scrollable compact right tool rail.

Implemented in the current Gradient/typed-layer UX pass:
- typed Paint / Image / Gradient row icons;
- selected-row accent;
- direct visibility affordance;
- direct lock affordance;
- persistent lock remains synchronized with ProjectSession;
- Gradient create/edit controls;
- Linear / Radial;
- editable stops and stop positions;
- angle;
- repeat / dither;
- move / scale;
- Mirror H / Mirror V;
- reset-to-clip transform;
- pure tested common-core Gradient transform authoring.

Remaining/reference gap:
- broader icon toolbar treatment outside Layers;
- Select/Move/Flip local visual verification;
- recent colors;
- richer tooltips/shortcut help;
- integrated preview panel rather than separate utilitarian preview screen;
- final decorative/reference styling and compact-mode polish.

## Reference 03 — Smart Import

### Target experience

A guided image-processing workspace, not a raw PNG paste dialog.

Required reference controls:

Placement/transform:
- Fit;
- Stretch;
- Crop;
- Center;
- Keep Aspect Ratio;
- Mirror Horizontal/Vertical;
- Rotate;
- move / scale;
- opacity;
- tint.

Processing:
- Reduce Colors;
- Dither;
- Brightness;
- Contrast;
- Saturation;
- transparency/background handling.

Modes:
- Direct;
- Pixel-art;
- Outline Only;
- Monochrome;
- Palette Limited;
- Posterize.

Preview:
- original;
- processed result;
- resulting texture;
- live 3D player preview.

### Current status

Functional Smart Import is implemented and CI green.

Implemented:
- Home and Cape Editor import entry points;
- PNG picker/decoder adapter;
- Original preview;
- Processed preview;
- Cape Texture preview;
- isolated 3D candidate preview;
- Fit / Stretch / Crop / Center;
- Keep Aspect;
- move / scale / arbitrary rotation;
- Mirror H/V;
- Brightness / Contrast / Saturation;
- Reduce Colors;
- Floyd-Steinberg Dither;
- Direct;
- Pixel Art;
- Outline Only;
- Monochrome;
- Palette Limited;
- Posterize;
- selected Loom Swatches palette integration;
- Apply as editable schema-v2 Image layer;
- reopen/edit existing Image layers;
- non-destructive source/transform/processing persistence.

Still missing for final reference fidelity:
- tint;
- background-removal workflow;
- direct transform handles;
- final decorative treatment;
- local visual verification at all required GUI profiles.

Imported artwork remains a real editable Image layer rather than a destructive paste.

## Reference 04 — Elytra + Animation Editor

### Target experience

A dedicated Elytra workspace sharing Loom Studios' editor language while adding wing-specific and timeline controls.

Required:
- unfolded wings;
- linked/mirrored wing editing;
- independent wing editing;
- layers;
- shared color/Swatches/tools;
- live preview;
- standing/open/gliding states;
- Elytra thickness;
- animation timeline/tracks;
- keyframes/procedural effects;
- play/pause/loop/speed.

### Current status

Implemented foundation:
- independent Loom Elytra texture;
- vanilla gliding/wing animation preserved;
- high-resolution Elytra canvas support;
- deterministic animation/emissive rendering proof;
- reusable Elytra 3D preview mode.

Implemented in the semantic editor pass:
- unfolded Left / Right semantic wing canvas;
- vanilla-atlas 10x20 front-face mapping;
- linked mirrored painting;
- independent Separate Wings mode;
- Pencil / Eraser;
- brush sizing;
- 1x / 2x / 4x;
- reusable layer stack;
- Add / Copy / Delete Paint layers;
- direct visibility / persistent lock row controls;
- Undo / Redo;
- Save / Save + Equip;
- integrated 3D Elytra preview;
- project-authored thickness 25%–200%.

Remaining/reference gap:
- cape-to-Elytra conversion;
- Elytra-target Image/Smart Import path;
- richer layer properties/reorder;
- shared Swatches parity;
- standing/open/gliding preview-state controls;
- animation timeline;
- animated layer/effect model;
- final reference layout and local visual verification.

## Reference 05 — Loom Codes / Sharing

### Target experience

A deliberate private share/import/export surface.

Required:
- generate Loom Code;
- private player-only result;
- left-click/copy interaction;
- Import;
- Preview;
- Favorite;
- project export/import;
- portable code path;
- clear distinction between editable project and flattened PNG export.

### Current status

Implemented related infrastructure:
- project hashing;
- multiplayer project cache/sync;
- palette-specific `LOOMPAL1:` clipboard codes;
- local project serialization.

Not yet implemented:
- project-level Loom Code;
- short code resolver/storage;
- portable project code;
- private clickable chat UX;
- project favorite/preview/import flow.

Palette share codes are **not** a substitute for project Loom Codes.

## Reference-driven build order

The practical order is:

1. locally verify Cape + Smart Import + Gradient + Home + Elytra;
2. finish remaining Elytra workflow/parity;
3. animation/timeline;
4. Loom Codes/sharing;
5. final visual-fidelity pass across all five references.

This ordering preserves the approved references while avoiding disposable UI built before the underlying feature model exists.

## Asset note

The approved optimized WebP references are committed under `docs/references/ui/` and are the canonical repository-side visual targets.

Full-resolution PNG masters are also kept in the ChatGPT Project Library under `/Loom Studios/UI References/`.

These refreshed references supersede the earlier rough visual concept set while preserving the same product identity.
