# Local demo — prove data moved

This walkthrough shows Kafka → Flink → local file sink without Azure secrets and without relying on live Wikimedia forever.

## Prerequisites

- Java 21, Maven 3.9+, Docker Compose v2
- ~4 GB RAM free for Compose
- First `make up` / `make demo` builds `vega-flink:1.20-java21` (Flink 1.20 has no official Java 21 Hub tag)

## One-command path

```bash
cp .env.example .env
make demo
```

`make demo` will:

1. Build connector + Flink JARs
2. Start Compose (`make up`)
3. Wait for Kafka, Connect, Flink
4. Submit `WikiEnrichmentJob`
5. Produce 5 bounded Avro wiki fixtures
6. Assert non-empty files under `./data/vega-output/wiki_events_enriched/`

## Manual path

```bash
cp .env.example .env
make build
mkdir -p data/vega-output data/iceberg
make up
./scripts/wait-for-kafka.sh
./scripts/wait-for-connect.sh
./scripts/health-check.sh
./scripts/submit-jobs.sh --wiki-only
# wait until WikiEnrichmentJob is RUNNING in Flink UI
./scripts/produce-fixtures.sh raw-wiki-events 5
# wait ~15–30s for a checkpoint, then:
find data/vega-output/wiki_events_enriched -type f -size +0c
```

Full stack (live connectors + all six jobs):

```bash
make bootstrap
```

Live connectors need egress to Wikimedia / NASA / RSS feeds. Prefer fixtures when offline.

## Verification checklist

| Check | How |
|---|---|
| Kafka has messages | Kafka UI → topic `raw-wiki-events` |
| Flink job running | Flink UI → `WikiEnrichmentJob` = RUNNING |
| Sink has rows | `find data/vega-output -type f -size +0c` |
| Connect healthy | `curl -s http://localhost:8083/` |
| Optional dbt compile | `cd dbt && DATABRICKS_HOST=https://x DATABRICKS_HTTP_PATH=/sql/1.0/warehouses/x DATABRICKS_TOKEN=x dbt compile` |

## What this does / does not prove

**Proves:** Avro fixtures on Kafka, Flink job execution, checkpointed file sink output on disk.

**Does not prove:** Iceberg on ADLS, Databricks marts, AKS deploy, live connector durability, DLQ wiring.

Iceberg on object storage needs `VEGA_ICEBERG_ENABLED=true` and either a local warehouse path that Flink can write, or Azure OAuth + `abfs://` warehouse. There is no MinIO service in Compose today.

## Tear down

```bash
make teardown
# or wipe volumes + local sink data:
make clean
```
