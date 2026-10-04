# Schema-v4 authoring

Status: DONE — Linux runtime, core and four-profile visual acceptance. See CURRENT_STATE.md for exact-source evidence and remaining platform/performance checks.

## Requested scope

| Request | Implementation | Verification |
| --- | --- | --- |
| More formats | Header-first PNG/JPEG/JPG/GIF/BMP/TIFF/WBMP content decoding | PNG/JPEG/BMP/GIF/TIFF/WBMP tests pass |
| GIF animation | Bounded composed frames, offsets/disposal, per-frame delays, runtime frame scheduling | Core timing/disposal/serialization pass; real Cape/Elytra GIF texture changes pass |
| 1/2/4/6/8× Cape and Elytra | 512×256 maximum atlas; resolution shown in both editors | Core resize/mask tests pass; four-profile captures/bounds pass |
| Layer crash and storage | Per-layer compressed v4; exact expansion/serialized/artwork budgets; rejected edits keep history | 48 layers at8× round-trip, entropy rollback and forged expansion tests pass |
| Save/load/Loom Code/network | v1–v3 migrations; v4 portable/file; protocol2 fragmentation/cache/upload bounds | File/code pass; 3.15 MiB live integrated C2S/S2C pass |
| Animate entry/return | Bottom actions open shared studio; Back returns; idle wing dock removed | real Screen return and profile captures pass |
| Wing faces and image overlap | Independent boxes, outside/inside/edges/top/bottom; linked mirror stays on selected face | Distinct-face/isolation/migration tests pass; actual outside artwork reviewed; geometry/isolation pass |
| Typed effects and easing | Pulse range, Scroll XY/distance, Gradient angle/width/colors/offset, Sparkle density/seed/size/brightness, Glow intensity/falloff; five outgoing easing curves | Serialization/key-move tests pass; parameter captures and live preview pass |
| Alpha Lock, clipping, masks | Persisted flags/masks; mask paint/reveal/hide/invert/remove; resizes masks | Core compositing/immutability tests pass; real controls and profile captures pass |
| Local background removal | Pick original pixel, tolerance, connected/global, before/after checker transparency | Core connectivity/tolerance pass; real controls and profile captures pass |
| Tint, brightness/contrast/saturation | Non-destructive, reopen Image layer via Smart Import | Core settings/source preservation pass; real controls and profile captures pass |
| Image-to-swatches | Extract16 colors and select saved palette | Palette tests existing; real action saves/selects palette |
| Wand/Replace Color | Exact BitSet selection and connected/global tolerance; recolor/delete/clear/invert | Disconnected selection/recolor tests pass; real controls and profile captures pass |
| Brush/stamp presets | Square/Circle/Cross/Diamond/Star, selected-pixel constraint | Stamp tests pass; real drag/single undo pass |
| Seam editing | Unfolded cape outside plus four neighboring edges; strokes cross edges, corners excluded | Edge stroke/inside isolation test passes; four-profile unfolded workspace reviewed |
| Timeline clarity | Reserved ruler band; remove unnamed decorative purple top border; legend/tooltips explain actual purple spans/diamond keys/cyan playhead | four-profile timeline review passes |
| Four display profiles | 1920×1080 and3440×1440 GUI2/3; focused tools preserve compact editor | 278 full +178 comparison screenshots pass |

## How to use

- Increase resolution from either editor’s resolution controls. More source detail needs a sufficiently detailed source image; 8× does not invent detail.
- Import chooses a supported image by actual content. GIF playback is automatic on an Image layer. Select an Image layer and reopen Smart Import to adjust it later.
- Smart Import → Processing → Adjust / Background / Swatches (compact) or Background, tint & swatches opens local before/after adjustment previews. Click the original to sample the background; Remove BG offers tolerance and connected/global removal. Adjust offers signed brightness/contrast/saturation, tint and Create Swatches from Image. Apply keeps the original source.
- Elytra’s bottom surface dropdown selects Outside/Inside/Left edge/Right edge/Top/Bottom. Linked mirrors that selected face; Separate edits a wing independently. Import uses the selected surface.
- Animate opens the shared studio. Apply a preset, or choose Advanced → Parameters & easing. Easing edits the outgoing segment of the key nearest the current cursor. Purple bars are track spans, diamonds are keys, cyan is the playhead.
- Surface tools → Wand / color selects connected or global matching pixels with tolerance. Replace preserves selected alpha; Delete clears the selected pixels. Brush stamps respect that exact selection. Cape seam mode unfolds neighbors beside the outside face.
- Surface tools → Layer masks exposes Alpha Lock and Clip below. Edit mask: white reveals, black hides; use Reveal/Hide brush, invert or remove mask. Masks can affect Paint/Image/Gradient layers. Pixel tools require an unlocked Paint layer.

## Limits and compatibility

A project allows up to64 layers per channel, subject to the combined60 MiB artwork/64 MiB expansion and8 MiB serialized budgets. Thus not every maximum can be used simultaneously. High-entropy GIFs/images can reach the serialized limit sooner than sparse paint. A rejected edit leaves artwork and undo history intact and reports a budget message. Portable codes can become long; use a `.loom` file for large transfers.

Schema4 is not readable by older releases. Existing1–3 projects migrate on load and retain source payloads, layer IDs and animation tracks. Saving an upgrade preserves original file bytes in version history; old history files are verified by their original byte checksum. Both client and server require protocol2 for larger multiplayer projects. Existing Fabric fragmentation is retained; caches are byte/entry bounded and idle bundles stop animation work. A32 MiB immutable layer-block cache avoids recompressing unchanged artwork, including retained source data in its bounds. Single bounded workers validate uploads and decode downloads off game threads; evicted equipped artwork is requested again. Actual integrated upload/download verification passes; dedicated-server soak remains manual. Local preview remains available without a compatible server.

No format decoder service, cloud image processing or new runtime image dependency is required. Formats beyond Java’s installed ImageIO readers (such as WebP/HEIC/AVIF) are explicitly unsupported rather than advertised without a decoder.
