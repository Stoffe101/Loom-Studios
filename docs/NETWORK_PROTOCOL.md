# Loom Studios — Network Protocol

## Implemented protocol2

Schema4 increases the project envelope to8 MiB. Both client and server must run compatible protocol2 builds; ordinary or incompatible servers still allow local cosmetics. Fabric registerLarge performs transport fragmentation with512 bytes reserved above the serialized budget. Every blob is bounded, SHA-256 checked and fully decoded before use. Only compatible clients with an expected current hash may upload. Equipped state remains server authoritative; edits and animation frames stay local until Save + Equip.

Server: one validation worker/four queued uploads; raw LRU cache64 MiB/64 entries; upload1/sec, download4/sec; commits verify exact player connection and expected hash. Client: one decoder worker/two queued downloads, session-epoch commits, decoded LRU128 MiB/64 entries. Visible cache misses retry after5 seconds; an evicted equipped blob is requested again from its connected owner. Queues and histories are bounded, not unlimited by the higher resolution. See AUTHORING_V4.md and CURRENT_STATE.md for exact validation evidence and remaining dedicated-server soak coverage. Historical proposed messages follow.


## Goals

- server-authoritative equipped state
- no brush-stroke or animation-frame streaming
- content-addressed design transfer
- cache-friendly joins
- explicit size/validation limits
- forward-compatible protocol versioning

## Core identifiers

Each serialized saved project receives a SHA-256 content hash.

A player's equipped state references:
- project hash
- project/schema version
- cape enabled state
- Elytra enabled state
- small cosmetic metadata needed for negotiation

Large assets are transferred separately.

## Proposed messages

### Client → Server

- HELLO / capabilities
- SAVE_PROJECT metadata
- PROJECT_BLOB upload when required
- EQUIP_PROJECT
- UNEQUIP
- REQUEST_PROJECT hash
- SHARE_CODE_REQUEST as required by server-backed short codes

### Server → Client

- HELLO_ACK / negotiated capabilities
- EQUIPPED_STATE for players
- PROJECT_NEEDED hash
- PROJECT_BLOB
- PROJECT_REJECTED reason
- SHARE_CODE_CREATED
- CACHE_HINTS / state updates where useful

Names are conceptual until implementation pins Fabric payload classes.

## Local publication boundary

The network layer publishes the explicitly equipped project snapshot, never the dirty editable `ProjectSession`.

Saving without equipping must not change the advertised hash. Save + Equip is the operation that advances local world/multiplayer state.

## Cache-miss flow

1. remote player state announces hash H
2. client checks cache for H
3. missing client requests H
4. server sends validated blob
5. receiving client verifies size, schema, and SHA-256
6. client stores H and compiles runtime texture

## Large payloads

Use Fabric-supported large-payload handling/splitting where appropriate rather than inventing frame-by-frame custom fragmentation unless testing demonstrates a need.

## Limits

Server config must enforce:
- maximum serialized project bytes
- maximum imported asset bytes
- maximum layer count
- maximum animation tracks/keyframes
- maximum image dimensions
- rate limits for repeated upload/equip/share requests

## Animation

Synchronize animation definitions and a stable time basis. Each client evaluates animation locally.

Do not stream generated texture frames.

## Security

Never trust:
- claimed size before decoding
- hashes without recomputing
- layer/asset counts
- image dimensions
- compression ratios
- enum/type identifiers
- project/schema versions

Malformed or unsupported payloads must be rejected cleanly without disconnecting unrelated players where avoidable.


## SPIKE-05 implemented proof protocol

Protocol version: 1

Flow:

1. Client creates a tiny versioned ProofProject and SHA-256 hash.
2. Client sends HELLO(protocol, hash) only if the server advertises the Loom channel.
3. Server sends PROJECT_NEEDED(hash) when the content is not cached.
4. Client uploads PROJECT_BLOB(hash, bytes).
5. Server verifies:
   - maximum bytes;
   - SHA-256;
   - schema version;
   - field ranges.
6. Server stores hash -> bytes and broadcasts EQUIPPED_STATE(player UUID, hash).
7. A remote client receiving an unknown equipped hash sends PROJECT_REQUEST(hash).
8. Server returns PROJECT_BLOB(hash, bytes).
9. Client verifies, caches, compiles local runtime textures, and renders the remote player.

Disconnect broadcasts an empty equipped hash as an unequip signal.

The proof project is deliberately tiny. The final .loom transport will retain this same content-addressed/cache-miss architecture and move oversized project blobs to Fabric's 1.21.11 large-payload registration path when required.

Animation is not transported frame-by-frame. The project carries animation parameters; clients evaluate them locally.

## Schema-v3 animation data

Animation definitions are part of the normal encoded `.loom` project blob and therefore use the existing content-addressed cache/equip protocol.

The network does **not** stream timeline ticks, keyframe interpolation results, or rendered animation frames.

Clients evaluate the saved animation locally. A schema-v3 project received from the server passes through the same bounded codec/migration validation as local files.

## Local Loom Codes versus hosted share codes

Current product sharing is deliberately offline-first.

- `LSP1:` is a self-contained compressed project code. It does not contact the server.
- `LS-XXXX-XXXX-XXXX` is currently a deterministic content fingerprint, not a remotely resolvable token.
- importing an `LSP1:` code passes through the same bounded Loom project codec validation/migration as files/network blobs.

Conceptual SHARE_CODE_REQUEST / SHARE_CODE_CREATED messages remain future-only and must not be treated as implemented protocol until a real resolver service exists.

A future hosted sharing system should store/resolve validated content-addressed project blobs rather than streaming editor state or animation frames.
