# Loom Studios — Technical Research Summary

**Checkpoint:** 2026-10-01  
**Target:** Minecraft Java 1.21.11 / Fabric Loader 0.18.4

This document records the technical conclusions that justify beginning implementation. Source links and license notes belong in SOURCE_INDEX.md.

## Build/toolchain

- Java 21 is the target runtime/toolchain.
- Fabric Loader 0.18.4 exists and is the required compatibility baseline.
- Fabric API and Fabric Loom exact pins must be proven in SPIKE-00 rather than guessed.
- official Mojang mappings are the preferred 1.21.11 mapping direction.
- common/client code should be split from the start.

## Cape rendering

Preferred approach:
- substitute the cape texture/state used by the player renderer;
- keep vanilla cape geometry and motion;
- avoid replacing the full player renderer.

Reason:
- smallest compatibility surface;
- preserves crouch/run/armor/Elytra behavior;
- better odds with Sodium/Iris.

The exact Mojang-mapped hook is a SPIKE-01 deliverable.

## Elytra

Player skin/render data has a distinct Elytra texture concept. First attempt a texture override through the normal player/Elytra path.

Unknown to prove:
- exact precedence when real Elytra equipment is present;
- whether a narrow additional hook is required;
- remote-player behavior.

## Dynamic textures

Minecraft client APIs support images backed by runtime textures. Loom Studios should:
- composite editable project output into NativeImage-like data;
- upload only when output becomes dirty;
- cache compiled output;
- dispose superseded GPU resources.

No per-frame image decoding.

## Custom GUI/editor

Fabric/Minecraft custom screens are sufficient for a fully custom editor.

Use reusable Loom components instead of giant pre-rendered screen textures.

Required custom components include:
- canvas
- color picker
- layer list
- timeline
- player preview
- sliders/dropdowns/tooltips
- scroll panels
- icon buttons

## Player preview

Use the same underlying render concepts as gameplay where practical, but isolate preview cosmetic state from the actual equipped multiplayer state.

## Networking

Editing stays local.

Network only:
- saved project metadata/blobs
- equip/unequip state
- cache misses
- share-code operations

Projects are addressed by SHA-256 content hash.

Large project transfers should use Fabric-supported payload facilities and strict server-side size/rate validation.

## Project storage

Editable project is the source of truth.

Runtime/export PNGs are compiled products.

The project format must be versioned and migratable.

## Smart image import

Java-side image processing is feasible for:
- resize
- crop
- rotation
- mirroring
- brightness/contrast/saturation
- quantization
- dithering
- alpha handling
- posterization
- monochrome
- outline/edge processing
- pixel-art conversion

Imported images remain transformable layers rather than being destructively baked immediately.

## Animation

Prefer parameter/keyframe animation definitions evaluated locally.

Do not synchronize rendered frames over the network.

Animated texture output should update only when visible output changes and within an explicit performance budget.

## Emissive/additive rendering

Use a separate optional feature pass rather than forcing true emissive semantics into the ordinary base texture.

Base cape/Elytra must survive if an optional shader-sensitive pass is disabled.

## Sodium / Iris / Sodium Extra / 3D Skin Layers

These are optional compatibility targets, not dependencies. Plain Fabric remains the required core runtime.

Design rule:
- use Minecraft/Fabric-supported rendering paths;
- avoid broad renderer replacement;
- avoid hidden OpenGL state assumptions;
- validate every rendering milestone under the canonical four-way matrix.

## Major risks requiring early spikes

1. exact 1.21.11 mapped hook locations;
2. Elytra override precedence;
3. emissive geometry alignment;
4. shader-pack behavior;
5. GUI preview state isolation;
6. large multiplayer project transfer limits;
7. animation performance with multiple remote players.

These risks are intentionally front-loaded into SPIKE-00 through SPIKE-06.


## DynamicTexture 1.21.11 implementation note

For Mojang-mapped Minecraft 1.21.11 the validated API shape used by SPIKE-02 is:

- `com.mojang.blaze3d.platform.NativeImage`
- `net.minecraft.client.renderer.texture.DynamicTexture`
- constructor with a debug-name supplier plus NativeImage
- `TextureManager.register(Identifier, texture)`
- mutate NativeImage pixels
- `DynamicTexture.upload()` to send changed pixels to the GPU
- `TextureManager.release(Identifier)` for cleanup

The runtime design keeps a stable registered texture and uploads only dirty output rather than registering a new texture every frame.


## Elytra fallback and geometry findings (1.21.11)

Source inspection of vanilla 1.21.11 establishes:

`WingsLayer.getPlayerElytraTexture`:
1. returns the player's dedicated Elytra texture when present;
2. otherwise returns the visible cape texture;
3. otherwise returns null so equipment/default rendering applies.

This explains why the SPIKE-02 animated cape appeared on the Elytra automatically.

Vanilla `ElytraModel.createLayer` builds each wing from a box using:
- UV origin: (22, 0)
- width: 10
- height: 20
- depth: 2
- CubeDeformation: 1.0
- texture dimensions: 64x32

The resulting geometry is deliberately thick. Texture work can reduce the visual prominence of the edge faces, but truly thinner geometry would require a custom model/render path and should remain optional for compatibility.
