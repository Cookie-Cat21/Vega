# Vega Improvement Plan — 200 Commit Roadmap

**Current:** 12 commits (Phases 1–11)  
**Target:** 200 commits  
**Remaining:** 188 commits across 5 improvement loops

## Loop Structure

Each loop follows MASTER_PROMPT.md: analyze → prioritize → implement → validate → commit → repeat.

| Loop | Commits | Focus |
|------|---------|-------|
| 1 | 13–50 (38) | Core pipeline: Iceberg, metrics, DLQ, Flink config, unit tests |
| 2 | 51–90 (40) | K8s hardening, Kafka Connect, Terraform, CI/CD |
| 3 | 91–130 (40) | dbt tests, Grafana, Prometheus, scripts, DX |
| 4 | 131–165 (35) | Integration tests, resilience, security, docs |
| 5 | 166–200 (35) | Polish, tuning, runbooks, final validation |

## Commit Categories

### Loop 1 — Core Pipeline (commits 13–50)
- [ ] 13: IMPROVEMENT_PLAN.md
- [ ] 14–30: Flink unit tests (17 missing classes)
- [ ] 31: VegaMetrics — custom Prometheus counters
- [ ] 32: Wire metrics into WikiEnrichmentJob
- [ ] 33: Wire metrics into EONETEnrichmentJob
- [ ] 34: Wire metrics into CorrelationJob
- [ ] 35: Wire metrics into AnomalyDetectionJob
- [ ] 36: Wire metrics into AggregationJob
- [ ] 37: Wire metrics into SLNewsEnrichmentJob
- [ ] 38: DeadLetterHandler utility
- [ ] 39: DLQ in WikimediaSourceTask
- [ ] 40: DLQ in EONETSourceTask
- [ ] 41: DLQ in SLNewsSourceTask
- [ ] 42: IcebergSinkFactory — real catalog config
- [ ] 43: Flink Prometheus reporter in docker-compose
- [ ] 44: .dockerignore flink-jobs
- [ ] 45: .dockerignore wikimedia
- [ ] 46: .dockerignore eonet
- [ ] 47: .dockerignore slnews
- [ ] 48: Makefile build/test targets
- [ ] 49: Flink java21 image fix
- [ ] 50: Loop 1 validation commit

### Loop 2 — Infrastructure (commits 51–90)
- [ ] 51–56: K8s KafkaConnect + 3 KafkaConnector CRs
- [ ] 57–60: K8s NetworkPolicy, PDB, probes
- [ ] 61–65: Terraform Key Vault, outputs, validation
- [ ] 66–70: CI unified test workflow, image tag patching
- [ ] 71–75: Helm install in deploy workflow
- [ ] 76–80: Flink deployment hardening (resources, savepoints)
- [ ] 81–85: Prometheus scrape config improvements
- [ ] 86–90: Loop 2 validation

### Loop 3 — Analytics & DX (commits 91–130)
- [ ] 91–100: dbt schema/freshness tests (10 commits)
- [ ] 101–105: Grafana SL News panels + dashboard fixes
- [ ] 106–110: Prometheus alert rules expansion
- [ ] 111–115: Scripts (register-connectors, health-check, validate)
- [ ] 116–120: README, LICENSE, runbook sections
- [ ] 121–125: .env.example completeness
- [ ] 126–130: Loop 3 validation

### Loop 4 — Hardening (commits 131–165)
- [ ] 131–140: Integration test scaffolding (*IT.java)
- [ ] 141–145: Circuit breaker / retry utilities
- [ ] 146–150: Connector shared library extraction
- [ ] 151–155: Schema evolution config
- [ ] 156–160: Logback production configs
- [ ] 161–165: Loop 4 validation

### Loop 5 — Polish (commits 166–200)
- [ ] 166–175: Flink tuning (watermarks, parallelism)
- [ ] 176–180: CorrelationJob SL news integration
- [ ] 181–185: Operations runbook
- [ ] 186–190: MASTER_PROMPT.md update (Phase 11 unblocked)
- [ ] 191–195: Final test coverage push
- [ ] 196–200: Final validation checklist + release notes

## Validation Gates

After each loop:
```bash
make test
make build
docker compose config
terraform validate
dbt compile
```

## Progress Tracker

| Commit # | SHA | Description | Loop |
|----------|-----|-------------|------|
| (updated as commits land) | | | |
