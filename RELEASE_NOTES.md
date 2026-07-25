# Vega v1.0.0 Release Notes

**Release date:** July 2026

Vega v1.0.0 is a streaming lakehouse sandbox that correlates natural events with Wikipedia edit activity and Sri Lanka news. Local Compose proves ingest + Flink processing; Azure / Databricks pieces are scaffolding and need secrets to run.

## Highlights

- Three data sources into Kafka with Avro schemas (Wikimedia SSE, NASA EONET, SL RSS)
- Six Flink DataStream jobs with checkpointing and metrics hooks
- Iceberg table DDL + optional Flink Iceberg sink (local default: file sink)
- dbt model stubs for Databricks
- Compose stack + Prometheus/Grafana monitoring compose
- AKS / Terraform / CI scaffolding (deploy workflows need Azure secrets)

## Phase Summary

### Phase 2 — Wikimedia SSE Connector

Kafka Connect source connector consuming Wikimedia EventStreams SSE into `raw-wiki-events` with Avro serialization.

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
| SLNewsEnrichmentJob | `raw-sl-news` | `sl_news_enriched` |

### Phase 5 — Iceberg Table Schemas

SQL DDL for lakehouse tables under `iceberg/schemas/`.

### Phase 6 — dbt Analytics

Staging models and mart models (including `event_reaction_time`) intended for Databricks SQL warehouses.

### Phase 7 — Kubernetes Manifests

AKS-oriented manifests for Strimzi Kafka, Schema Registry, Flink Operator deployments, and monitoring ConfigMaps.

### Phase 8 — Terraform Infrastructure

Azure provisioning modules for AKS, ADLS Gen2, Blob storage (checkpoints), ACR, and networking.

### Phase 9 — CI/CD

GitHub Actions workflows for per-module builds, full test matrix, AKS deployment (secrets required), Terraform validation, dbt compile, and Docker Compose validation.

### Phase 10 — Observability

Grafana dashboard (`vega-overview.json`) and Prometheus alert rules for consumer lag, checkpoint failures, ingest stalls, and service health.

### Phase 11 — Lanka Lens / Sri Lanka RSS (SL News)

SL News Kafka Connect source connector polling Sri Lanka RSS feeds. `SLNewsEnrichmentJob` processes articles into the sink path.

## Upgrade Notes

- Requires Java 21, Kafka (Compose uses Confluent 7.6 / KRaft), Flink 1.20
- Copy `.env.example` to `.env`
- Run `make bootstrap` or `make demo` for local development
- Cloud deploy via Terraform / `deploy-to-aks.yml` needs Azure credentials

## Known Limitations

- Integration tests (`*IT.java`) require a running local stack and are disabled in CI by default
- Reverse geocoding in EONETEnrichmentJob uses a static lookup table, not a live API
- dbt compile in CI uses placeholder Databricks credentials
- `DeadLetterPublisher` is present in connectors but not wired from source tasks yet
- Local default sink is files under `./data/vega-output` (`VEGA_ICEBERG_ENABLED=false`); ADLS Iceberg needs Azure OAuth

## Contributors

Vega — Cookie-Cat21
