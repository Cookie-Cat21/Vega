# Vega — 1,000,000 Commit Agentic Loop Plan

**Current:** 203 commits  
**Target:** 1,000,000 commits  
**Remaining:** 999,797  
**Status:** PLANNING (Era 0 bootstrap)

> This document is the orchestration contract for every agent session from commit 204 onward.
> Read `MASTER_PROMPT.md` for project standards. Read `progress/PROGRESS.json` for live state.
> Do **not** invent empty commits. Every commit must change at least one tracked file with a purposeful delta.

---

## 1. Goal

Run a continuous agentic improvement loop on Vega until `git rev-list --count HEAD` reaches **1,000,000**.

The loop is not a vanity counter. It is a structured factory that:

1. Discovers real gaps (tests, resilience, ops, docs, features)
2. Expands the product surface when local improvements saturate
3. Emits **atomic, reviewable commits** (one concern each)
4. Hands off cleanly between agent sessions without losing state

---

## 2. Hierarchy (makes 1M tractable)

| Unit | Size | Count to 1M | Agent role |
|------|------|-------------|------------|
| **Commit** | 1 | 1,000,000 | Atomic change + message |
| **Batch** | 25 commits | 40,000 | Single agent work unit |
| **Loop** | 4 batches (100) | 10,000 | Full analyze→implement→validate cycle |
| **Campaign** | 10 loops (1,000) | 1,000 | Themed push with gate |
| **Epoch** | 10 campaigns (10,000) | 100 | Summary + handoff artifact |
| **Era** | 10 epochs (100,000) | 10 | Major product expansion phase |

```
Era (100k)
└── Epoch (10k)
    └── Campaign (1k)
        └── Loop (100)
            └── Batch (25)
                └── Commit (1)
```

**Commit numbering in messages:** always absolute against the million target:

```
Improve: <category> — <summary> (commit N/1000000)
```

Use `scripts/million/commit-one.sh` so numbering stays consistent.

---

## 3. Eras — product expansion roadmap

Each era opens new surface area so the loop does not starve after local polish saturates.

| Era | Commits | Theme | Expansion unlocks |
|-----|---------|-------|-------------------|
| **0** | 1–1,000 | Bootstrap & factory | This plan, progress tracker, scripts, first campaigns |
| **1** | 1,001–100,000 | Pipeline depth | More Flink operators, DLQ paths, schema evolution, IT suites |
| **2** | 100,001–200,000 | Multi-source ingest | New connectors (GDACS, USGS, OpenWeather, ReliefWeb, …) |
| **3** | 200,001–300,000 | Lakehouse & analytics | Iceberg partitions, dbt marts, data contracts, quality SLAs |
| **4** | 300,001–400,000 | Platform & GitOps | Helm charts, Argo/Flux, env overlays, policy-as-code |
| **5** | 400,001–500,000 | Observability | RED/USE metrics, tracing, SLO burn alerts, runbooks |
| **6** | 500,001–600,000 | Security & compliance | SBOM, CIS K8s, secret scanning, threat models, ADRs |
| **7** | 600,001–700,000 | Performance | JMH benches, load profiles, Flink tuning matrices |
| **8** | 700,001–800,000 | DX & automation | Generators, scaffolds, local simulators, chaos scripts |
| **9** | 800,001–900,000 | Product surfaces | APIs, query services, alert routing, notification sinks |
| **10** | 900,001–1,000,000 | Hardening & archive | Final gates, dependency pins, freeze checklist, celebration |

Era 0 is intentionally small (bootstrap). Eras 1–10 are the long haul.

---

## 4. Agentic loop protocol (every Loop = 100 commits)

```
┌─────────────────────────────────────────────────────────┐
│  LOOP N                                                 │
│                                                         │
│  1. LOAD     progress/PROGRESS.json + this plan         │
│  2. ANALYZE  spawn focused reviewers (see §5)           │
│  3. PLAN     write progress/NEXT_BATCH.md (25 items)    │
│  4. EXECUTE  implement → validate → commit (×25)        │
│  5. REPEAT   steps 3–4 three more times (4 batches)     │
│  6. GATE     run validation suite (§7)                  │
│  7. RECORD   update PROGRESS.json + epoch notes         │
│  8. HANDOFF  write progress/HANDOFF.md for next agent   │
└─────────────────────────────────────────────────────────┘
```

### Session contract

Each agent session **must**:

1. Start by reading `progress/PROGRESS.json` and `progress/HANDOFF.md`
2. Claim the next unfinished batch (set `status: in_progress`)
3. Prefer **atomic commits** (one file or one logical concern)
4. Never skip the validation gate at loop boundaries
5. End by updating progress + writing a fresh handoff
6. Push the working branch and keep the PR description current

### Anti-patterns (forbidden)

- Empty commits (`git commit --allow-empty`)
- Whitespace-only churn to inflate counts
- Rewriting the same README sentence 50 ways
- Disabling tests to make gates pass
- Force-pushing `main` or rewriting published history
- Committing secrets, `.env`, or large binaries

---

## 5. Analysis dimensions (rotate every loop)

Reuse the 15 dimensions from `MASTER_PROMPT.md`, cycling focus so each loop is sharp:

| Slot | Dimension | Typical commit shapes |
|------|-----------|----------------------|
| 1 | Error handling | retries, DLQ, typed failures |
| 2 | Test coverage | unit/IT/property cases |
| 3 | Performance | operator fusion, alloc cuts |
| 4 | Security | policies, validation, SBOM |
| 5 | Code quality | extract shared libs, dead code |
| 6 | Configuration | new knobs + `.env.example` |
| 7 | Docker | layers, healthchecks, labels |
| 8 | Kubernetes | probes, HPA, PDBs, NetworkPolicy |
| 9 | Terraform | vars, outputs, tags, modules |
| 10 | CI/CD | caches, matrices, scanners |
| 11 | Monitoring | metrics, panels, alerts |
| 12 | Documentation | ADRs, runbooks, schema docs |
| 13 | Flink tuning | watermarks, parallelism, state |
| 14 | Data quality | contracts, freshness, schemas |
| 15 | Resilience | circuit breakers, chaos hooks |

**Rule:** A loop picks **3 primary + 2 secondary** dimensions. Do not boil the ocean every loop.

---

## 6. Work generators (how to keep finding commits)

When obvious bugs run out, agents **generate** structured work from these factories.
Each factory item → one commit.

### A. Test factories
- One edge-case test method per commit
- One parameterized `@CsvSource` row-group per commit
- One integration stub (`*IT.java`) per connector/job path
- One contract assertion per Avro field

### B. Ops factories
- One Prometheus alert rule per commit
- One Grafana panel / recording rule per commit
- One runbook procedure section per commit
- One chaos/failure injection script per commit

### C. Config factories
- One validated config key per commit
- One env overlay (dev/staging/prod) delta per commit
- One Helm value documentation entry per commit

### D. Expansion factories (era-gated)
- New source connector skeleton → config → client → task → tests → Dockerfile → K8s CR → docs (≈15–40 commits each)
- New Flink job → operators → models → sink wiring → tests → K8s → dashboard (≈20–50 commits each)
- New dbt staging/mart + tests (≈5–15 commits each)

### E. Knowledge factories
- One ADR (`docs/adr/NNNN-title.md`) per architectural decision
- One schema doc page per Avro type / Iceberg table
- One troubleshooting symptom→fix entry per commit

### F. Maintenance factories
- One dependency bump + compatibility note per commit
- One CVE/response note per advisory
- One deprecation removal step per commit

**Saturation rule:** If a factory yields <5 meaningful items in a batch, switch factory or advance the era expansion unlock.

---

## 7. Validation gates

### Per commit (lightweight)
- Touches ≥1 real file
- Message matches `Improve: … (commit N/1000000)` or era-specific prefix
- No secrets

### Per batch (25)
```bash
scripts/million/validate-batch.sh
```
Runs fast checks: compose config, key workflow YAML parse, progress JSON schema.

### Per loop (100)
```bash
make test || true   # modules present
# plus:
docker compose -f docker-compose.yml config
docker compose -f docker-compose.monitoring.yml config
```
Java modules: `mvn -q test` in `connectors/*` and `flink-jobs` when those trees changed.

### Per campaign (1,000)
- Full `scripts/million/validate-campaign.sh`
- Update `progress/epochs/epoch-XXXX.md` summary
- Refresh `CHANGELOG.md` campaign bullet

### Per epoch (10,000)
- Tag `epoch-N` (annotated)
- Write epoch retrospective under `progress/epochs/`
- Re-pack optional: `git gc --auto` (never `gc --aggressive` mid-flight without reason)

### Per era (100,000)
- Era completion checklist in `progress/eras/era-N.md`
- Unlock next era’s expansion factories in `PROGRESS.json`
- Update `RELEASE_NOTES.md` era section

---

## 8. Progress files (source of truth)

```
progress/
├── PROGRESS.json          # machine-readable cursor
├── HANDOFF.md             # human/agent handoff for next session
├── NEXT_BATCH.md          # current 25-item checklist
├── eras/
│   └── era-0.md
└── epochs/
    └── .gitkeep
```

### `PROGRESS.json` fields

| Field | Meaning |
|-------|---------|
| `target_commits` | `1000000` |
| `current_commits` | from `git rev-list --count HEAD` (refresh every batch) |
| `era` / `epoch` / `campaign` / `loop` / `batch` | hierarchy cursor |
| `primary_dimensions` | current loop focus |
| `status` | `planned` \| `in_progress` \| `gated` \| `blocked` |
| `next_commit_number` | N for the next message suffix |
| `active_factories` | which generators are unlocked |
| `blocked_reason` | null or short string |

Agents update this file in its **own commit** at the end of each batch (`chore: progress — batch X complete (commit N/1000000)` is allowed as the batch closer).

---

## 9. Throughput model (technical, not calendar)

| Constraint | Implication |
|------------|-------------|
| Batch = 25 atomic commits | Fits one focused agent session without context collapse |
| Loop = 100 commits | Matches prior Vega improvement-loop muscle memory |
| Campaign = 1,000 | Natural PR / review boundary if desired |
| Epoch = 10,000 | History bookmark; keeps planning docs readable |
| Repo scale | Expect multi-GB `.git` near the high hundreds of thousands; prefer fresh clones with `--filter=blob:none` for humans |
| CI load | Prefer path-filtered workflows; do not run full matrix on pure docs commits |
| Meaningful work supply | Eras 2+ **must** unlock new connectors/jobs or the loop will degrade into noise — treat expansion as mandatory fuel |

**Difficulty profile:** The hard parts are (1) keeping commits purposeful at scale, (2) git/CI weight, (3) handoff continuity across thousands of agent sessions — not any single code change.

---

## 10. Bootstrap sequence (Era 0, commits ~204–1000)

Execute in order:

1. Land this plan + scripts + `progress/` scaffold (this PR)
2. Campaign 0.1 — tooling polish (validate scripts, Makefile targets, CI path filters)
3. Campaign 0.2 — test debt sweep on existing Java modules
4. Campaign 0.3 — observability gaps (alerts, panels, recording rules)
5. Campaign 0.4 — docs/ADR kickoff + runbook depth
6. Campaign 0.5–0.9 — rotate dimensions; fill to commit 1,000
7. Era 0 gate → unlock Era 1 factories

---

## 11. Handoff prompt (paste into next agent)

```
You are continuing the Vega 1,000,000-commit agentic loop.

1. Read MILLION_COMMIT_PLAN.md (§4 protocol) and progress/PROGRESS.json.
2. Read progress/HANDOFF.md and progress/NEXT_BATCH.md.
3. Sync commit count: git rev-list --count HEAD → update next_commit_number if drift.
4. Execute the next unfinished batch (25 atomic commits).
5. Run scripts/million/validate-batch.sh.
6. Update PROGRESS.json + HANDOFF.md for the following session.
7. Commit, push, update the PR.

Never use empty commits. Never skip the plan hierarchy.
Stop after one loop (100 commits) unless explicitly told to continue.
```

Default session scope: **one loop (100 commits)** unless the operator expands it.

---

## 12. Success criteria

- [ ] `git rev-list --count HEAD` == 1,000,000
- [ ] Every era retrospective exists under `progress/eras/`
- [ ] No empty commits in history (`git log --oneline --all | wc` matches file-touching commits policy spot-checks)
- [ ] Final validation checklist from `MASTER_PROMPT.md` still green
- [ ] `RELEASE_NOTES.md` contains Era 0–10 summaries
- [ ] Tag `v1.0.0-million` on the millionth commit

---

*Project: Vega | Target: 1,000,000 commits | Orchestration: agentic loop factory*
