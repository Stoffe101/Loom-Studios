# Loom Studios — UI Reference Images

**Approved reference set:** 2026-10-02

These five images are the canonical visual targets for Loom Studios. They replace the earlier rough concept set as the active design reference.

Repository copies are optimized WebP assets so the Git repository stays reasonably small. The full-resolution PNG masters are also stored in the ChatGPT Project Library under:

`/Loom Studios/UI References/`

## Canonical repository assets

1. [Home / Start Screen](./Loom_Studios_01_Home_Screen.webp)
2. [Cape Editor](./Loom_Studios_02_Cape_Editor.webp)
3. [Smart Import](./Loom_Studios_03_Smart_Import.webp)
4. [Elytra + Animation Editor](./Loom_Studios_04_Elytra_Animation_Editor.webp)
5. [Loom Codes / Sharing](./Loom_Studios_05_Loom_Codes_and_Sharing.webp)

These are **design references, not pixel-perfect implementation screenshots**. Loom Studios should preserve their hierarchy, interaction intent and product identity while adapting layouts to Minecraft GUI constraints and real implementation needs.

## Approved visual direction

The target balance is approximately **70% clean modern creative editor / 30% magical Minecraft workshop**.

Keep:
- dark slate/charcoal work surfaces;
- warm wood, cloth, metal and lantern framing where decorative space permits;
- cyan + violet identity accents;
- pixel-readable typography;
- strong iconography for tools, buttons, layer types and actions;
- project thumbnails;
- clear canvas hierarchy;
- live 3D preview;
- timeline/keyframes where animation is authored;
- deliberate sharing/import/export surfaces.

Restrain:
- glow on inactive controls;
- nested borders around every sub-panel;
- repeated decorative banners/slogans;
- oversized fixed chrome;
- multiple equally-dominant focal regions.

Glow is semantic: it should primarily communicate **selected, active, focused or primary action**.

## Showcase mode versus work mode

### Showcase-oriented screens
Home and Loom Codes / Sharing may carry more of the workshop personality because they are discovery/presentation surfaces.

### Work-oriented screens
Cape Editor, Smart Import and Elytra + Animation should become calmer once the user starts creating:
- thinner shell/chrome;
- larger working region;
- quieter inactive controls;
- icon-led tool groups;
- one clearly dominant task area;
- collapsible/compact secondary panels where needed.

The canvas/result being edited should visually outrank the surrounding decoration.

## Screen intent

### 01 Home
Project hub with creation/navigation, recent-project thumbnails, templates and selected-project/player preview.

### 02 Cape Editor
Canvas-first editing workspace with icon tools, Colors/Swatches, typed Layers and live 3D preview.

### 03 Smart Import
Focused source → processing → result workflow with transform controls, processing modes, texture preview and candidate 3D preview.

### 04 Elytra + Animation
Unfolded-wing editor with linked/separate wings, typed layers, live gliding preview and a readable animation timeline.

### 05 Loom Codes / Sharing
Simple share-code-first hub with import/export options, permissions and design previews.

## Responsive rule

The reference look must adapt rather than collapse at smaller logical GUI sizes.

Required profiles:
- 1920x1080 GUI x2;
- 1920x1080 GUI x3;
- 3440x1440 GUI x2;
- 3440x1440 GUI x3.

Reference fidelity must never reintroduce clipping, text overlap, giant modal/floating panels or controls that become unusable at GUI scale 3.

See:
- `../../LOOM_STUDIOS_DESIGN_SPEC.md`
- `../../REFERENCE_FIDELITY_ROADMAP.md`
- `../../UI_COMPATIBILITY.md`
