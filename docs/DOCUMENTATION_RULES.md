# Loom Studios — Documentation Rules

**Status:** Mandatory project rule

Documentation is part of the implementation. It is not optional cleanup performed after coding.

## Hard rule

Every meaningful research, implementation, bug-fix, refactor, protocol, rendering, UI, persistence, compatibility, or test pass must update the canonical documentation before the pass is considered complete.

## Required updates per pass

At minimum record:

1. **What changed** — files, subsystems, or features.
2. **Why** — requirement, bug, research result, or design decision.
3. **Implementation state** — DONE / PARTIAL / BLOCKED / TODO.
4. **Architecture impact** — interfaces, data flow, assumptions, dependencies.
5. **Testing performed** — commands, scenarios, exact environment, result.
6. **Known issues/risks** — failures must not be hidden behind optimistic wording.
7. **Next work** — next concrete task or tasks.
8. **Decision changes** — update DECISIONS.md when architecture changes.

## Fast handoff trio

These files must always agree:

- CURRENT_STATE.md
- PASS_LOG.md
- NEXT_WORK.md

## Status language

- DONE — implemented and verified.
- PARTIAL — some scope works; missing scope is listed.
- IN PROGRESS — active and not yet passable.
- BLOCKED — cannot proceed; reason documented.
- TODO — not started.

A feature is never DONE merely because it compiles.

## Test evidence

For meaningful checkpoints record:

- date/time
- exact commit SHA
- Minecraft version
- Fabric Loader version
- Fabric API version
- Java version
- relevant optional mods such as Sodium/Iris/shader pack
- command or scenario
- result
- notes, logs, or screenshot references

CI status belongs to the exact commit SHA that ran.

## Research discipline

When implementation reveals a new version-specific uncertainty:

1. document the question;
2. research only that point;
3. update SOURCE_INDEX.md;
4. record the resulting decision;
5. continue implementation.

## Reference-image discipline

UI work must record which approved reference screen it targets and any intentional deviations.

## Session completion checklist

A meaningful pass is not complete until:

- relevant tests have run;
- documentation is updated;
- CURRENT_STATE.md is truthful;
- PASS_LOG.md contains the pass;
- NEXT_WORK.md states the next concrete step;
- architecture changes are reflected in DECISIONS.md;
- unfinished work is explicitly described.
