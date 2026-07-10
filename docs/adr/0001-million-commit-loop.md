# ADR 0001 — Million-commit agentic loop

**Status:** Accepted  
**Date:** 2026-07-10

## Context

Vega completed its 200-commit phased build and improvement loops. Continuing ad-hoc agent sessions without a hierarchy caused planning docs to go stale and made handoffs brittle. The project owner wants a durable agentic factory that can run until the repository reaches 1,000,000 commits.

## Decision

Adopt the hierarchy and protocol in `MILLION_COMMIT_PLAN.md`:

- Atomic purposeful commits only (no empty commits)
- Batch (25) → Loop (100) → Campaign (1k) → Epoch (10k) → Era (100k)
- Live state in `progress/PROGRESS.json` with session handoff files
- Era-gated work factories so the loop expands product surface instead of grinding noise

## Consequences

- Agents have a clear session contract and default stop point (one loop)
- Git history and CI cost will grow substantially; path filters and blobless clones become important
- Meaningful work supply depends on unlocking new connectors/jobs/marts each era
