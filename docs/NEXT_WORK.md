# Loom Studios — Next Work

## Immediate verification: high-resolution canvas + color controls

After CI is green:
1. git pull and runClient;
2. L -> Create New Cape;
3. use Res + to switch 64x32 -> 128x64 -> 256x128;
4. confirm Outside/Back changes 10x16 -> 20x32 -> 40x64;
5. paint fine details at 4x;
6. open 3D Preview and confirm the extra detail is visible rather than being collapsed to 1x;
7. Save + Equip and inspect the cape in-world;
8. test Brush +/- at several sizes;
9. drag in the color square and hue strip;
10. drag R/G/B sliders and confirm numeric/hex values and paint color update;
11. click palette swatches.

Expected:
- higher resolution produces genuinely finer texel detail in live preview/world rendering;
- old 1x projects still load unchanged;
- changing resolution is undoable;
- RGB/HSV controls stay synchronized.

## Next Phase-2 work

- group a drag stroke into one undo action;
- Fill;
- Eyedropper;
- Line/Rectangle;
- editable hex/RGB numeric fields;
- alpha/opacity control;
- symmetry;
- layer panel;
- keyboard shortcuts;
- Recent Project thumbnail rendering;
- Cape Loom block interaction;
- begin Elytra Editor using the same resolution/color/brush foundations.

## Resolution policy

Initial supported atlas sizes:
- 64x32 (1x)
- 128x64 (2x)
- 256x128 (4x)

The architecture is scale-aware. Larger sizes can be considered later after performance/network testing.
