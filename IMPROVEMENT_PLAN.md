# Vega Improvement Plan

**Current:** 203 commits  
**Previous target:** 200 commits — **COMPLETE**  
**Next target:** **1,000,000 commits** — see [`MILLION_COMMIT_PLAN.md`](./MILLION_COMMIT_PLAN.md)

Live cursor: [`progress/PROGRESS.json`](./progress/PROGRESS.json)  
Handoff: [`progress/HANDOFF.md`](./progress/HANDOFF.md)

## Legacy — 200 Commit Roadmap (COMPLETE)

Each loop followed MASTER_PROMPT.md: analyze → prioritize → implement → validate → commit → repeat.

| Loop | Commits | Focus | Status |
|------|---------|-------|--------|
| 1 | 13–50 (38) | Core pipeline: Iceberg, metrics, DLQ, Flink config, unit tests | Complete |
| 2 | 51–90 (40) | K8s hardening, Kafka Connect, Terraform, CI/CD | Complete |
| 3 | 91–130 (40) | dbt tests, Grafana, Prometheus, scripts, DX | Complete |
| 4 | 131–165 (35) | Integration tests, resilience, security, docs | Complete |
| 5 | 166–200 (35) | Polish, tuning, runbooks, final validation | Complete |

## Million-commit hierarchy (active)

| Unit | Size | Role |
|------|------|------|
| Batch | 25 | Agent session work unit |
| Loop | 100 | Full analyze→implement→validate cycle |
| Campaign | 1,000 | Themed push + gate |
| Epoch | 10,000 | Retrospective bookmark |
| Era | 100,000 | Product expansion phase |

**Era 0 (commits → 1,000):** bootstrap factory, tooling, first campaigns.  
**Eras 1–10:** pipeline depth → multi-source → lakehouse → platform → observability → security → performance → DX → product surfaces → hardening.

## Agent entrypoint

```bash
scripts/million/status.sh
# then follow progress/HANDOFF.md
```

Paste the handoff prompt from `MILLION_COMMIT_PLAN.md` §11 into the next agent session.
