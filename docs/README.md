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

## Live check-off mission board

- [Active Mission Board](ACTIVE_MISSIONS.md) — **the current, actionable check-off list** with priorities, stage exit gates, existing PR status, next five tasks, and a future ElvUI-inspired workspace-layout editor. Tick only verified/accepted tasks and keep CURRENT_STATE / PASS_LOG / NEXT_WORK in sync.

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
- `SMART_IMPORT.md` — Smart Import transform/processing architecture and status
- `CREATIVE_AUTHORING.md` — Animation2.1 lanes/curves, reference guides, editable frames/onion skins and custom stamps
- `AUTHORING_V4.md` — higher-resolution tools, format limits, image processing and usage
- `ANIMATION.md` — schema-v5 animation model, runtime evaluation, timeline authoring and preview isolation
- `LOOM_CODES.md` — local/offline project sharing, portable codes, imports/exports and hosted-service boundary
- `IMPLEMENTATION_ROADMAP.md` — staged build roadmap
- `REFERENCE_FIDELITY_ROADMAP.md` — per-reference-screen implementation/status map
- `TEST_MATRIX.md` — required automated/manual validation
- `DECISIONS.md` — architecture decision record
- `references/ui/` — visual reference mockups

## Proposed future-development specifications (NOT implemented)

A documentation-only creative-workflow planning pass dated **2026-10-08** prepared the following proposals from the five approved references, accepted workflow screenshots and current source:

- [Complete improvements roadmap](FUTURE_IMPROVEMENTS_ROADMAP.md) — baseline versus proposed features, all prioritized gaps, dependencies, milestones and release gates.
- [Large Creative Asset Library](CREATIVE_ASSET_LIBRARY_SPEC.md) — diverse stars, clouds, trees, nature/fantasy stamps, browsing, custom stamps, scatter painting, opacity and editable theme recipes.
- [Animation UI/UX redesign](ANIMATION_UX_REDESIGN_SPEC.md) — Simple/Advanced animation, effect gallery, resizable timeline, key/curve discoverability and visual frame authoring.
- [Editor UX and Creative Tools](EDITOR_CREATIVE_UX_SPEC.md) — adaptive layouts, preview, reference transforms, atmospheric paint, compositing/adjustment layers, import/export, accessibility and 3D proof-of-concept.

**Status rule:** these four documents are a proposed backlog, not verified implementation. [CURRENT_STATE.md](CURRENT_STATE.md) remains authoritative for shipped functionality. Feature acceptance requires new code, test and screenshot evidence and updates to CURRENT_STATE / PASS_LOG / NEXT_WORK.

## Source of truth

When documentation conflicts, the newest verified entry in `CURRENT_STATE.md`, `PASS_LOG.md`, and `DECISIONS.md` takes precedence over older planning text.

The design specification remains authoritative for product intent unless a newer decision record explicitly supersedes it.

- [Animation 2.1 user testing](ANIMATION21_TESTING.md) — installation, new workflows and platform acceptance.
