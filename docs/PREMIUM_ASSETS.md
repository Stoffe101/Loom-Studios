# Premium workshop assets

Created2026-10-03 using the built-in image-generation tool. Both outputs were inspected and committed as production texture assets. The semantic Home reference03-003AB50C-8B39-4E46-A73B-ED32184E08A8.jpeg was supplied as a style reference only.

- assets/loom-studios/textures/ui/workshop-frame.png —1672×941 RGBA, Git blob bb68d2bd2b0343f2cd4c7d8d1be0a1562b65d84e. Transparent center verified. Slices reserve the existing header, seven-pixel side rim and bottom beam; no embedded controls/text. New visual acceptance is pending.
- assets/loom-studios/textures/ui/preview-courtyard.png —1024×1536 opaque, Git blob2d6ec71e8465d9fa4a726468347e7ef2b41146d8. Static scenic texture; the real interactive Minecraft player and cape/elytra remain separate. Aspect-preserving crop is bottom-aligned to retain the dais.

Frame prompt: Production game UI decorative frame, transparent interior, warm dark oak beams, beveled hammered steel brackets/copper bolts, two amber lanterns, recessed blank navy sign and cyan/purple pennants. Orthographic landscape frame with top/header, bottom beam and narrow side uprights. No text, controls, icons or characters; suitable for responsive nine-slice scaling.

Preview prompt: Production tall2:3 moonlit Minecraft fantasy castle courtyard, clear cobblestone standing area in lower quarter, distant pine trees/turrets, amber windows and lower-left lantern, pale moon upper-right, quiet center for the actual player silhouette. Block-built architecture with smooth lighting and restrained depth of field. No characters/equipment, text, UI or borders.

Asset resource identifiers are loaded by Minecraft's texture manager and used through ordinary GUI blits. No runtime image API or external service is called. Scenery replaces procedural fill submissions; UI shapes/text/icons use the separate cached NanoVG overlay. Generated art is decorative and carries no authoring state.

Wide preview follow-up: assets/loom-studios/textures/ui/preview-courtyard-wide.png,2172×724, Git blobaba67f1d37bed3dd49c9aa7953df0e04ea70b12f. Built-in image-generation tool, inspected before use. Prompt: a very wide3:1 premium moonlit Minecraft fantasy castle courtyard with flat cobblestone bottom quarter, distant pine trees/castle towers at both sides, amber windows and far-left hanging lantern, midnight sky/pale moon far right, quiet center for a separately rendered actual player. Smooth lighting/atmospheric depth. No character, equipment, text, UI or borders. Viewports wider than1.35 use this composition; portrait views retain the original. New source needs visual acceptance.
