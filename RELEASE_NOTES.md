# Vega v1.0.0 Release Notes

**Release date:** July 2026

Vega v1.0.0 delivers a production-grade, end-to-end real-time streaming lakehouse that correlates global natural disasters with Wikipedia edit activity.

## Highlights

- Three live data sources ingesting into Kafka with Avro schema enforcement
- Six Flink stream processing jobs with exactly-once semantics
- Iceberg lakehouse tables on Azure ADLS Gen2
- dbt analytics marts on Databricks
- Full AKS deployment with Terraform provisioning
- Prometheus + Grafana observability with alerting

## Phase Summary

### Phase 2 — Wikimedia SSE Connector

Kafka Connect source connector consuming Wikimedia EventStreams SSE. Produces ~50–100 edit events/second to `raw-wiki-events` with Avro serialization and dead-letter handling.

### Phase 3 — NASA EONET Connector

REST polling connector hitting the EONET API every 60 seconds. Deduplicates events and produces geometry-expanded records to `raw-natural-events`.

### Phase 4 — Flink Stream Processing

Six DataStream jobs:

| Job | Input | Output |
|---|---|---|
| WikiEnrichmentJob | `raw-wiki-events` | `wiki_events_enriched` |
| EONETEnrichmentJob | `raw-natural-events` | `natural_events` |
| AnomalyDetectionJob | `raw-wiki-events` | `edit_anomalies` |
| AggregationJob | `raw-wiki-events` | `edit_aggregates` |
| CorrelationJob | dual-stream | `event_correlations` |
| SLNewsEnrichmentJob | `raw-sl-news` | `sl_news_articles` |

### Phase 5 — Iceberg Table Schemas

SQL DDL for all lakehouse tables with partitioning strategies and column documentation.

### Phase 6 — dbt Analytics

Staging models and five mart models including `event_reaction_time` — the key metric answering how fast the world reacts to disasters on Wikipedia.

### Phase 7 — Kubernetes Manifests

Production-ready AKS manifests for Strimzi Kafka, Schema Registry, Flink Operator deployments, and monitoring ConfigMaps.

### Phase 8 — Terraform Infrastructure

Azure provisioning for AKS, ADLS Gen2, Blob storage (checkpoints), ACR, and networking with remote state backend.

### Phase 9 — CI/CD

GitHub Actions workflows for per-module builds, full test matrix, AKS deployment, Terraform validation, dbt compile, and Docker Compose validation.

### Phase 10 — Observability

Grafana dashboard (`vega-overview.json`) with pipeline health, Wikipedia insights, natural events, correlation panels, and JVM metrics. Prometheus alert rules for consumer lag, checkpoint failures, ingest stalls, and service health.

### Phase 11 — Lanka Lens / Sri Lanka RSS (SL News)

SL News Kafka Connect source connector polling Sri Lanka RSS feeds. `SLNewsEnrichmentJob` processes articles into the lakehouse. Completes the third data source for regional news correlation.

## Post-Release Improvements

Five improvement loops hardened the codebase across:

- Error handling with retries, circuit breakers, and dead-letter queues
- Expanded test coverage including integration test stubs and metrics operator tests
- Security hardening (K8s network policies, secret management)
- Performance tuning (Flink operator chaining, checkpoint intervals)
- CI/CD enhancements (Dependabot, validation workflows, PR template)
- Documentation (`docs/ARCHITECTURE.md`, `docs/CONTRIBUTING.md`)
- Monitoring additions (per-source ingest stall alerts, custom Flink metrics)

## Upgrade Notes

- Requires Java 21, Kafka 3.7 (KRaft), Flink 1.20, Iceberg 1.6
- Copy `.env.example` to `.env` and configure Azure credentials for production
- Run `make bootstrap` for local development or deploy via GitHub Actions to AKS

## Known Limitations

- Integration tests (`*IT.java`) require a running local stack and are disabled in CI by default
- Reverse geocoding in EONETEnrichmentJob uses a static lookup table, not a live API
- dbt compile in CI uses placeholder Databricks credentials

## Improvement Loops (Commits 13–200)

Five post-phase improvement loops delivered:

- **Loop 1:** Unit test coverage, Flink metrics, DLQ publishers, Docker/Makefile DX
- **Loop 2:** K8s Kafka Connect, NetworkPolicy, Terraform Key Vault + Databricks
- **Loop 3:** dbt tests, Grafana panels, scripts, runbook, CI workflows
- **Loop 4:** Integration test stubs, logback configs, HPA, probes
- **Loop 5:** Documentation, OCI labels, parallelism tuning, final validation

## Contributors

Vega — Cookie-Cat21
