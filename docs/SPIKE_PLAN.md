# Loom Studios — Technical Spike Plan

The spike phase exists to remove Minecraft-specific uncertainty before the full editor is built.

## SPIKE-00 — Toolchain bootstrap

**Goal:** prove the exact build stack.

Required:
- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.4 compatibility
- Fabric API pin
- Fabric Loom pin
- Mojang mappings
- split client/common sources
- Gradle wrapper
- GitHub Actions build

Pass criteria:
- build succeeds
- dev client launches
- dedicated server launches
- no client class leakage on server
- exact versions documented

## SPIKE-01 — Static cape via vanilla path

**Goal:** substitute one hard-coded Loom cape while keeping vanilla cape physics and visibility behavior.

Pass criteria:
- local player shows test cape
- normal cape motion preserved
- crouch/run/armor behavior sane
- Elytra precedence sane
- Fabric/Sodium/Iris matrix recorded

## SPIKE-02 — Dynamic texture

**Goal:** update a cape generated in memory without restart/reconnect.

Pass criteria:
- NativeImage-backed texture changes live
- dirty-only updates
- replaced textures disposed safely
- repeated updates do not leak resources

## SPIKE-03 — Elytra

**Goal:** custom Elytra texture while preserving vanilla equipment and flight animation.

Pass criteria:
- custom texture visible when Elytra equipped
- flight pose correct
- cape/Elytra precedence documented
- compatibility matrix green or deviations documented

## SPIKE-04 — GUI player preview

**Goal:** isolated live preview inside a Screen.

Pass criteria:
- rotate and zoom
- cape and Elytra preview modes
- temporary editor state does not change actual equipped design
- preview shares rendering code with gameplay where practical

## SPIKE-05 — Multiplayer synchronization

**Goal:** two clients and a dedicated server exchange an equipped design.

Pass criteria:
- Player A equips design
- server validates state
- Player B receives hash/project on cache miss
- reconnect/join behavior works
- invalid/oversized payload handling proven

## SPIKE-06 — Animated/emissive compatibility

**Goal:** prove one animated layer and one emissive/additive pass.

Pass criteria:
- deterministic local animation
- no per-frame network streaming
- base cosmetic survives optional-effect failure
- Fabric/Sodium/Iris shader matrix recorded
- acceptable performance budget

## Foundation gate

The full reference-image editor begins only after these spikes are green or an explicitly documented architecture replacement supersedes a failed spike.
