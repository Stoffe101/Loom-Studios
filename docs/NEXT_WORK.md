# Loom Studios — Next Work

## Active: SPIKE-03 local verification

After CI is green, pull and launch runClient.

Test with an Elytra equipped:

- cape keeps changing palette every ~2 seconds;
- Elytra no longer changes with the cape;
- Elytra uses the dedicated cyan/violet/pink test design;
- gliding and normal vanilla wing animation still work;
- inspect whether transparent edge-face UVs make the wings feel visually less chunky;
- Sodium/Sodium Extra/Iris/3D Skin Layers remain stable.

If the wings still feel too thick, document that as a vanilla-geometry limitation rather than treating it as a texture bug. Then decide later whether an optional Slim Elytra renderer belongs in the product.

## After SPIKE-03

SPIKE-04 — live GUI player preview using temporary cosmetic state.

## Documentation

Record exact CI SHA and local runtime results.
