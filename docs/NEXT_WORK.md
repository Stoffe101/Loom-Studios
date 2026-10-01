# Loom Studios — Next Work

## Immediate priority

### SPIKE-00 — Toolchain bootstrap

Bootstrap the real Fabric project with:

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 baseline
- compatible Fabric API 1.21.11 version
- compatible Fabric Loom version
- Mojang mappings
- split client/common environment source sets
- Gradle wrapper
- minimal GitHub Actions build

Prove:

- ./gradlew build
- dev client launch
- dedicated server launch
- no accidental client-only class references during dedicated server startup

Record exact successful versions, commands, commit SHA, and CI result.

### SPIKE-01 — Static vanilla-path cape

Implement one obvious hard-coded test cape and substitute only the player cape texture while letting vanilla own geometry and motion.

Test:

- Fabric only
- Fabric + Sodium
- Fabric + Sodium + Iris, shaders OFF
- Fabric + Sodium + Iris, shaders ON

### SPIKE-02 — Dynamic runtime texture

Generate and update a cape texture in memory without restarting the game. Prove safe texture replacement, caching, dirty-state updates, and cleanup.

### SPIKE-03 — Elytra

Prove a custom Elytra texture with a real equipped Elytra and vanilla flight behavior.

### SPIKE-04 — Editor preview

Create a minimal GUI player preview with isolated temporary cape/Elytra state.

### SPIKE-05 — Multiplayer

Two clients plus dedicated server. Equip on Player A and verify Player B receives/caches/renders the design.

### SPIKE-06 — Animation/emissive compatibility

Add one minimal animated effect and one emissive/additive overlay and validate the full Sodium/Iris matrix.

## Gate

Do **not** invest heavily in recreating the full reference UI until SPIKE-00 through SPIKE-06 are green or a replacement architecture is explicitly documented and accepted.

## Documentation updates required after every spike

- CURRENT_STATE.md
- PASS_LOG.md
- NEXT_WORK.md
- relevant deep technical document
- DECISIONS.md when architecture changes
- TEST_MATRIX.md when test requirements/results change
