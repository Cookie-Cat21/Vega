# Vega detox audit (2026-07-25)

## Theater still in tree (pre-detox)

| Path | Role |
|---|---|
| `MILLION_COMMIT_PLAN.md` | 1M-commit factory plan |
| `IMPROVEMENT_PLAN.md` | loop-oriented improvement plan |
| `MASTER_PROMPT.md` | commit-farming orchestration prompt |
| `docs/adr/0001-million-commit-loop.md` | ADR for the loop |
| `scripts/million/` | status/sync/commit-one/validate helpers |
| `progress/` | PROGRESS.json, HANDOFF, NEXT_BATCH, ledger (~1k files) |
| Makefile `million-*` targets | operator surface for the loop |
| README / CONTRIBUTING / CHANGELOG | public advertising of the loop |
| `.env.example` `VEGA_MILLION_LOOP_ENABLED` | docs-only flag |

Git history retains old commits; no history rewrite.

## Real engineering assets (keep)

- `connectors/{wikimedia,eonet,slnews}` — Kafka Connect sources + unit tests
- `flink-jobs/` — six DataStream jobs, Iceberg/file sinks, unit tests
- `iceberg/schemas/`, `dbt/`, `docker-compose.yml`, `terraform/`, `k8s/`, `helm/`
- CI: `.github/workflows/test-all.yml` (+ per-module builds, compose/terraform validate, dbt-compile, deploy-to-aks)

## Local path vs cloud

| Prove locally today | Needs cloud secrets / Azure |
|---|---|
| Compose: Kafka KRaft, Schema Registry, Connect, Flink UI, Kafka UI | ADLS Gen2 Iceberg warehouse (`abfs://` + OAuth) |
| Maven unit tests for connectors + jobs | AKS deploy (`deploy-to-aks.yml`, Terraform apply) |
| Connector JAR mount + Connect REST registration | Databricks SQL warehouse for dbt runs |
| File sink under `/tmp/vega-output` when Iceberg disabled | End-to-end Iceberg on object storage |

## Broken / incomplete for “data moved” demo

1. Flink Compose services omit `KAFKA_BOOTSTRAP_SERVERS` / `SCHEMA_REGISTRY_URL` (defaults are host `localhost`, wrong inside containers).
2. `scripts/submit-jobs.sh` uploads `/opt/flink/usrlib/...` from the **host** (path missing); errors swallowed.
3. File/Iceberg output dirs are not volume-mounted → output dies with the container.
4. No bounded fixture producer — only live Wikimedia / EONET / RSS.
5. `DeadLetterPublisher` exists but is unused by source tasks (docs overclaim DLQ).
6. dbt CI sets `DBT_DATABRICKS_*`; `profiles.yml` expects `DATABRICKS_*`.
7. `test-all.yml` runs on `push` to `main` only (not PRs).
8. Iceberg on MinIO is absent; local default is file sink (`VEGA_ICEBERG_ENABLED=false`).

## Detox plan

1. Delete theater paths; strip Makefile/README/docs references.
2. Rewrite README as honest portfolio sandbox.
3. Fix Compose Flink env + job submit + mounted output; add `make demo` with bounded Avro fixtures.
4. Fix dbt CI env names; keep unit tests green; document cloud gaps.
