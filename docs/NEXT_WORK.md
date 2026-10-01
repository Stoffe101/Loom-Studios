# Loom Studios — Next Work

## Immediate verification: clearer cape painting

After CI is green:
1. git pull;
2. runClient;
3. press L -> Create New Cape;
4. confirm the canvas now says **Outside / Back • 10x16** and is much larger/clearer;
5. hover pixels and confirm the cyan selection outline + Pixel/UV coordinate readout;
6. paint several colors and confirm they stay **static**;
7. open 3D Preview and confirm the Outside/Back design appears where expected;
8. use **Face: ...** to cycle to Inside, edges, Top and Bottom;
9. Save + Equip and check the world cape.

Expected:
- no automatic hue cycling on newly created projects;
- the default canvas corresponds directly to the main cape face seen from behind;
- raw 64x32 atlas knowledge is not required to paint a basic cape.

## Next Phase-2 work after verification

- stroke grouping so one drag = one undo operation;
- Fill;
- Eyedropper;
- Line/Rectangle;
- proper color picker + hex/RGB;
- canvas zoom/pan where useful;
- symmetry;
- layer selection panel;
- keyboard shortcuts;
- Recent Project thumbnail rendering in the home screen;
- Cape Loom block registration/interaction.

The raw UV-atlas view can return later as an Advanced/UV mode with labeled region outlines rather than being the default editor.
