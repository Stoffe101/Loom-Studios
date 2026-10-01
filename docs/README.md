# Loom Studios Documentation

This directory is the canonical documentation home for Loom Studios.

## Hard rule

A development or research pass is not considered complete until the relevant documentation is updated.

Every meaningful pass must record:

- what changed
- why it changed
- architecture or design decisions
- tests performed and their results
- compatibility impact
- known issues or risks
- current status
- next work

## Canonical documents

- `LOOM_STUDIOS_DESIGN_SPEC.md` — product vision, features, UX, and visual direction
- `CURRENT_STATE.md` — current implementation state and latest verified checkpoint
- `PASS_LOG.md` — chronological development/research log
- `NEXT_WORK.md` — immediate next tasks and priorities
- `DOCUMENTATION_RULES.md` — mandatory documentation policy
- `GETTING_STARTED.md` — development bootstrap and first-spike flow
- `COMPATIBILITY.md` — Sodium, Iris, shader, and mod compatibility requirements
- `UI_COMPATIBILITY.md` — mandatory display-resolution / GUI-scale layout contract
- `ARCHITECTURE.md` — implementation architecture
- `TECHNICAL_RESEARCH.md` — researched APIs, hooks, constraints, and findings
- `SPIKE_PLAN.md` — ordered technical validation spikes
- `NETWORK_PROTOCOL.md` — multiplayer synchronization design
- `PROJECT_FORMAT.md` — Loom project/code serialization
- `IMPLEMENTATION_ROADMAP.md` — staged build roadmap
- `REFERENCE_FIDELITY_ROADMAP.md` — per-reference-screen implementation/status map
- `TEST_MATRIX.md` — required automated/manual validation
- `DECISIONS.md` — architecture decision record
- `references/ui/` — visual reference mockups

## Source of truth

When documentation conflicts, the newest verified entry in `CURRENT_STATE.md`, `PASS_LOG.md`, and `DECISIONS.md` takes precedence over older planning text.

The design specification remains authoritative for product intent unless a newer decision record explicitly supersedes it.
