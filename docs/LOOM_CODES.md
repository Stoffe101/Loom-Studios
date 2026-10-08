# Loom Studios — Loom Codes / Sharing

> **Current-status note (2026-10-08):** Portable-code and .loom project exports use the current schema-v5 model and migrate old supported formats on import. Existing `LS-...` strings are fingerprints, **not** online lookup codes. Current static PNG export evaluates the project at tick 0; animated GIF/APNG/sprite-sheet export is future work. This is separate from sharing editable project data via `LSP1:`.


**Status:** Local/offline sharing is implemented in the accepted schema-v5 runtime; Windows/target-platform acceptance and future hosted-service design remain open.

## Product rule

Sharing UI must distinguish real local functionality from future hosted functionality.

Loom Studios currently supports:
- offline portable project codes;
- clipboard transfer;
- external `.loom` files;
- local library import;
- editable-project export;
- Cape/Elytra PNG export.

Loom Studios does not currently provide a hosted short-code resolver, public gallery, friends/server/public permission backend, remote Favorite collection, or clickable chat-card service.

## Design fingerprint

Format: `LS-XXXX-XXXX-XXXX`.

The value is deterministic from the project's SHA-256 content identity and is a display fingerprint only until a real resolver service exists.

## Portable project code

Format: `LSP1:<URL-safe Base64>`.

Payload pipeline:
1. current `.loom` project encoding;
2. DEFLATE compression;
3. URL-safe Base64 without padding.

Decode is bounded by explicit prefix, maximum code length, maximum compressed bytes, bounded inflation, maximum normal project serialized size, and final `LoomProjectCodec` validation/migration.

Portable codes include the complete editable project: Cape layers, Elytra layers, runtime settings and schema-v5 animation tracks/keyframes and supported effect/parameter data.

## Import identity

Imported projects are forked with a new project UUID, fresh timestamps and an ` (Imported)` name suffix.

Layer UUIDs are preserved so animation tracks continue targeting the correct layers.

## Sharing screen

Reference: `docs/references/ui/Loom_Studios_05_Loom_Codes_and_Sharing.webp`.

Implemented actions:
- Copy Design ID;
- Copy Portable Code;
- Save Portable Code;
- Export Project (.loom);
- Export Cape PNG;
- Export Elytra PNG;
- Paste Portable Code from clipboard;
- Import .loom Project File;
- Preview Mine;
- Preview Import;
- Cape/Elytra preview mode;
- Import to Library;
- Import + Open.

## Export directory

Local exports use `.minecraft/loom-studios/exports/`.

Exports never silently overwrite an earlier export. Repeated filenames gain a numeric suffix.

PNG export compiles the authored project at timeline tick 0.

## Future hosted sharing

A future backend could add resolver-backed short codes, gallery listing, friends/server/public visibility, clickable chat cards and Favorites/collections.

That work should reuse validated content-addressed project blobs and must receive explicit protocol/security design before implementation.

Until then, the `LS-...` value remains a fingerprint and `LSP1:` remains the actual portable transfer format.

## Local verification queue

- clipboard copy/paste;
- very small and large portable projects;
- malformed portable code rejection;
- native `.loom` picker;
- import preview;
- Import to Library;
- Import + Open;
- animated imported project;
- Cape/Elytra preview toggle;
- `.loom` export;
- portable `.txt` export;
- Cape/Elytra PNG export;
- repeated export filename suffixing;
- all four mandatory GUI profiles;
- compare hierarchy/density against Reference 05.
