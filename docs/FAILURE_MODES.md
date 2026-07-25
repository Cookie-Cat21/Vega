# Failure modes

Honest notes on how Vega behaves when things go wrong. Local Compose defaults assumed unless noted.

## Duplicate events

| Source | Behavior |
|---|---|
| Wikimedia SSE | Stream may replay after reconnect; connector offsets are Connect-managed, not exactly-once end-to-end into Iceberg/file sink. |
| EONET REST | Polling can re-see open events; connector dedupes by event id where implemented. |
| Fixture demo | Re-running `produce-fixtures.sh` appends more records — sinks are append-only. |

Downstream Flink jobs do not globally dedupe. Correlation and aggregates can double-count if the same raw event is re-ingested.

## Late events

- Jobs use event-time timestamps from payloads where assigned (e.g. wiki `timestamp`).
- Watermark strategy is mostly monotonous / simple — late arrivals after the watermark can be dropped from windowed jobs (`AggregationJob`, `CorrelationJob`).
- Enrichment jobs are mostly map/filter and are more tolerant of disorder.

## Connector DLQ (`DeadLetterPublisher`)

- Each connector module includes `io.vega.connector.common.DeadLetterPublisher`.
- It publishes loosely formatted JSON to `VEGA_DLQ_TOPIC` (default `vega-dead-letter`).
- **Source tasks do not call it yet.** Parse failures today are more likely to error the task than land in a DLQ.
- Treat DLQ as available plumbing, not an operational guarantee.

## Restart / checkpoints

- Flink jobs enable checkpointing (`VEGA_CHECKPOINT_INTERVAL_MS`, default 60s in code; Compose sets 10s).
- Checkpoint mode: exactly-once. Local dirs: `/tmp/flink-checkpoints` (Compose volume).
- File sink commits on checkpoint — if you kill TaskManagers before a checkpoint, recent records may not appear under `./data/vega-output`.
- Restart strategy: fixed-delay, 3 attempts, 10s delay (`FlinkEnvFactory`).
- AKS path intends Azure Blob checkpoint storage; that requires cloud secrets and is not exercised by `make demo`.

## Schema / serde failures

- Flink deserializers expect Confluent Avro on the value bytes + Schema Registry.
- Producing plain JSON to `raw-*` topics will not be consumed successfully by the jobs.
- Use `scripts/produce-fixtures.sh` or the Connect Avro converter path.

## Connect / plugin load failures

- Compose bind-mounts shaded connector JARs from `connectors/*/target/`. Missing JARs (`make build` not run) → Connect starts but connector creation fails.
- Re-register with `make register-connectors` (idempotent upsert).

## Cloud-only failure modes

- ADLS Iceberg (`abfs://`) without `AZURE_CLIENT_ID` / `AZURE_CLIENT_SECRET` / `AZURE_TENANT_ID` fails at catalog init.
- `deploy-to-aks.yml` / Terraform apply fail without Azure credentials — expected in a portfolio clone.
- dbt run (not just compile) needs a real Databricks SQL warehouse and `DATABRICKS_*` env vars.
