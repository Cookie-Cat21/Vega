# Changelog

## [Unreleased]

### Removed
- Million-commit / agentic factory theater (`MILLION_COMMIT_PLAN.md`, `MASTER_PROMPT.md`, `IMPROVEMENT_PLAN.md`, `scripts/million/`, `progress/`, related Makefile targets and docs)

### Changed
- README rewritten as an honest portfolio / learning streaming lakehouse
- Local Compose Flink services wired to in-network Kafka and Schema Registry
- Job submit + bounded `make demo` path for proving data movement without live APIs

## [1.0.0] - 2026-07-07

### Added
- Kafka Connect sources: Wikimedia, EONET, SL News
- Six Flink jobs with metrics and Iceberg / file sinks
- Iceberg table DDL, dbt models, Compose stack, monitoring
- K8s / Terraform / CI scaffolding for Azure (requires secrets to run)
