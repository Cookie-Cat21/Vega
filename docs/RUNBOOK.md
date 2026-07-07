# Vega Operations Runbook

Operational procedures for the Vega real-time streaming lakehouse pipeline.

## Startup

1. **Build artifacts**

   ```bash
   make build
   ```

2. **Start the core stack** (Kafka KRaft, Schema Registry, Kafka Connect, Flink, Kafka UI):

   ```bash
   make up
   ```

3. **Verify services are healthy**:

   ```bash
   make status
   ```

   Confirm Kafka UI (http://localhost:8080), Flink UI (http://localhost:8081), Schema Registry (http://localhost:8082), and Kafka Connect (http://localhost:8083) are reachable.

4. **Deploy Kafka Connect sources** — register wikimedia, eonet, and slnews connectors via the Connect REST API (see `README.md` Quick Start for curl examples).

5. **Submit Flink jobs**:

   ```bash
   ./scripts/submit-jobs.sh
   ```

   Six jobs should appear in the Flink UI: WikiEnrichment, EONETEnrichment, AnomalyDetection, Aggregation, Correlation, and SLNewsEnrichment.

6. **Start monitoring** (optional for local dev):

   ```bash
   make monitoring
   ```

   Prometheus: http://localhost:9090 · Grafana: http://localhost:3000

## Connector Recovery

When a Kafka Connect source stops producing (check Kafka UI topic offsets or `VegaNoEventsReceived` / `VegaSLNewsIngestStalled` alerts):

1. **Check connector status**:

   ```bash
   curl -s http://localhost:8083/connectors/<connector-name>/status | jq .
   ```

   Replace `<connector-name>` with `wikimedia-source`, `eonet-source`, or `slnews-source`.

2. **Inspect connector logs**:

   ```bash
   docker compose logs -f kafka-connect
   ```

3. **Restart a failed connector**:

   ```bash
   curl -X POST http://localhost:8083/connectors/<connector-name>/restart
   ```

4. **Restart the connector task** (if the connector is RUNNING but the task is FAILED):

   ```bash
   curl -X POST http://localhost:8083/connectors/<connector-name>/tasks/0/restart
   ```

5. **Re-deploy the connector** if config is corrupt or the connector is stuck in UNASSIGNED:

   ```bash
   curl -X DELETE http://localhost:8083/connectors/<connector-name>
   # Re-create using the curl commands in README.md
   ```

6. **Verify recovery** — confirm the raw topic offset is advancing in Kafka UI (`raw-wiki-events`, `raw-natural-events`, or `raw-sl-news`).

## Flink Savepoint Restart

Use savepoints for graceful Flink job upgrades or recovery without reprocessing from scratch.

1. **Trigger a savepoint** for a running job (get `jobid` from Flink UI or REST):

   ```bash
   curl -X POST "http://localhost:8081/jobs/<jobid>/savepoints" \
     -H "Content-Type: application/json" \
     -d '{"target-directory": "file:///tmp/flink-savepoints", "cancel-job": false}'
   ```

2. **Cancel the job with savepoint** (for planned restarts):

   ```bash
   curl -X POST "http://localhost:8081/jobs/<jobid>/savepoints" \
     -H "Content-Type: application/json" \
     -d '{"target-directory": "file:///tmp/flink-savepoints", "cancel-job": true}'
   ```

   Note the savepoint path from the response.

3. **Restore from savepoint**:

   ```bash
   curl -X POST "http://localhost:8081/jars/<jar-id>/run" \
     -H "Content-Type: application/json" \
     -d '{
       "entryClass": "io.vega.flink.jobs.WikiEnrichmentJob",
       "parallelism": 2,
       "savepointPath": "file:///tmp/flink-savepoints/savepoint-<id>"
     }'
   ```

4. **Production (AKS)** — Flink Kubernetes Operator jobs use `upgradeMode: savepoint` in manifests under `k8s/flink/`. Savepoints are stored in Azure Blob (`wasbs://`).

5. **Verify** — confirm job is RUNNING in Flink UI and checkpoint/savepoint directories are writable (`flink-savepoints` volume locally).

## Alert Response

| Alert | Severity | Likely Cause | Response |
|---|---|---|---|
| `VegaKafkaConsumerLagHigh` | warning | Slow consumer or downstream backpressure | Check Flink job backpressure in UI; scale task slots or increase parallelism |
| `VegaFlinkCheckpointFailure` | critical | State backend or storage issue | Inspect Flink JobManager logs; verify checkpoint directory is writable |
| `VegaNoEventsReceived` | critical | Connector down or source API unavailable | Follow **Connector Recovery**; check Wikimedia SSE / EONET API status |
| `VegaSLNewsIngestStalled` | critical | SL News RSS connector stalled | Restart `slnews-source`; verify RSS feed URLs are reachable |
| `VegaFlinkJobDown` | critical | Jobs crashed or never submitted | Re-run `./scripts/submit-jobs.sh`; check Flink TaskManager logs |
| `VegaSchemaRegistryDown` | critical | Schema Registry container unhealthy | `docker compose restart schema-registry`; verify Kafka is healthy first |
| `VegaJvmHeapHigh` | warning | Memory pressure on JVM services | Restart affected container; review heap settings |

**General triage steps:**

1. Open Grafana dashboards in `dashboards/grafana/` for pipeline overview.
2. Check Prometheus alerts at http://localhost:9090/alerts.
3. Correlate alert timestamp with `docker compose logs` for the affected service.
4. After remediation, confirm alert resolves and raw-topic ingest rates recover.
