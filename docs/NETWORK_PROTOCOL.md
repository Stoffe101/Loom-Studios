# Loom Studios — Network Protocol

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
