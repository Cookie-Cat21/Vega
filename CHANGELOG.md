# Changelog

## [Unreleased] — Road to 1,000,000

### Added
- `MILLION_COMMIT_PLAN.md` — agentic loop orchestration toward 1M commits
- `progress/` — live cursor (`PROGRESS.json`), handoff, next-batch checklist
- `scripts/million/` — status, sync, commit-one, validate, claim/complete helpers
- Makefile targets: `million-progress`, `million-sync`, `million-validate-batch`, `million-validate-loop`

## [1.0.0] - 2026-07-07

### Added
- Phases 1–11: full streaming lakehouse pipeline
- Improvement loops 1–5: 200-commit production hardening
- Kafka Connect sources: Wikimedia, EONET, SL News
- Six Flink jobs with metrics and Iceberg sinks
- K8s manifests, Terraform, CI/CD, Grafana, dbt models
