# VEGA — Master Execution Prompt
## For Agentic AI (Cursor Agent / Claude Code / Codex)

---

> **READ THIS ENTIRE DOCUMENT BEFORE WRITING A SINGLE LINE OF CODE.**
> This is a self-contained orchestration plan. Execute it top-to-bottom, phase-by-phase. Do NOT skip ahead. Do NOT start Phase N+1 until Phase N is validated. Commit after every phase.

---

## PROJECT CONTEXT

**Vega** is a production-grade, end-to-end real-time data engineering pipeline. It ingests live event streams, processes them with Apache Flink, stores results in an Apache Iceberg lakehouse on Azure, and exposes data via Databricks/dbt. Infrastructure runs on Kubernetes (AKS), provisioned with Terraform, deployed via GitHub Actions CI/CD, monitored with Prometheus + Grafana.

**Repository:** https://github.com/Cookie-Cat21/Vega
**Working Directory:** `C:\Users\Ovindu\Documents\PET PROJECTS\Vega`
**Core Language:** Java 21 for all connectors and Flink jobs. Everything else uses the appropriate tooling for that layer.

### What Already Exists (Phase 1 — DONE)
- `docker-compose.yml` — Kafka KRaft (Confluent 7.6.0), Schema Registry, Kafka Connect, Kafka UI, Flink JobManager + TaskManager
- `docker-compose.monitoring.yml` — Prometheus + Grafana
- `Makefile` — `make up`, `make down`, `make logs`, `make monitoring`
- `prometheus/prometheus.yml`, `grafana/provisioning/datasources/prometheus.yml`
- `.gitignore`, `.env.example`, `README.md`

### Port Map (already configured)
| Service | Host Port |
|---|---|
| Kafka UI | 8080 |
| Flink UI | 8081 |
| Schema Registry | 8082 |
| Kafka Connect REST | 8083 |
| Kafka Bootstrap | 9092 |
| Prometheus | 9090 |
| Grafana | 3000 |

---

## END GOAL

Vega tracks global natural events and real-time human reactions to them.
- A wildfire starts in Australia (NASA EONET) → Wikipedia edits about that wildfire spike within minutes → Vega shows this correlation live.
- The result is a live Grafana dashboard showing how the world reacts to natural events in real-time, backed by a queryable Iceberg lakehouse for historical analysis.

---

## DATA SOURCES

### Source 1 — Wikimedia EventStreams (ACTIVE)
- Free, public, real-time SSE stream of every Wikipedia edit globally. No API key.
- ~50–100 events/second continuously.
- SSE endpoint: `https://stream.wikimedia.org/v2/stream/recentchange`
- Each event fields: `title`, `user`, `bot`, `wiki`, `timestamp`, `length.new`, `length.old`, `comment`, `type`, `namespace`, `server_url`
- **Connector type:** SSE (Server-Sent Events) — persistent streaming connection, never polled.

### Source 2 — NASA EONET (ACTIVE)
- NASA's Earth Observatory Natural Event Tracker. Free REST API. No API key.
- Tracks active natural events: wildfires, hurricanes, floods, volcanic eruptions, earthquakes.
- API endpoint: `https://eonet.gsfc.nasa.gov/api/v3/events`
- Each event fields: `id`, `title`, `category` (array), `geometry` (coordinates + date), `sources`, `closed` (null if ongoing)
- **Connector type:** REST polling — hit API every 60 seconds, diff against last state, produce only new/updated events.

### Source 3 — Lanka Lens / Sri Lanka RSS Feeds (ACTIVE — Phase 11 DONE)
> Phase 11 complete. `connectors/slnews/` and `SLNewsEnrichmentJob` are implemented and active.

---

## TECH STACK — EXACT VERSIONS

| Layer | Technology | Version | Notes |
|---|---|---|---|
| Language | Java | 21 (LTS) | All connectors + Flink jobs |
| Build | Maven | 3.9.x | Multi-module where sensible |
| Broker | Apache Kafka | KRaft mode | Confluent 7.6.0 images |
| Schema | Confluent Schema Registry | 7.6.0 | Avro schema enforcement |
| Connectors | Kafka Connect API | bundled | Custom Java source connectors |
| Serialization | Apache Avro | 1.11.3 | Schema-first |
| Processing | Apache Flink | 1.20 (upgrade path to 2.x) | Java DataStream API ONLY |
| Table Format | Apache Iceberg | 1.6.x | iceberg-flink-runtime |
| Object Storage | Azure Data Lake Storage Gen2 | — | Iceberg table store |
| Checkpoints | Azure Blob Storage | — | Flink checkpoints/savepoints |
| Analytics | Databricks | Latest | Queries Iceberg via Unity Catalog |
| Transform | dbt | 1.8.x | SQL models on Databricks |
| Container | Docker | Latest | Multi-stage builds |
| Orchestration | Docker Compose v2 | — | Local dev |
| Prod Orchestration | Kubernetes 1.30.x | AKS | Production |
| K8s Kafka | Strimzi Operator | 0.41.x | Helm install |
| K8s Flink | Flink K8s Operator | 1.9.x | Helm install |
| IaC | Terraform | 1.8.x | All Azure resources |
| CI/CD | GitHub Actions | — | Build → test → deploy |
| Metrics | Prometheus | 2.x | Scrape Flink + Kafka |
| Dashboards | Grafana | 10.x | Live pipeline dashboards |

### Key Maven Dependencies
```xml
<java.version>21</java.version>
<kafka.version>3.7.0</kafka.version>
<confluent.version>7.6.0</confluent.version>
<flink.version>1.20.0</flink.version>
<iceberg.version>1.6.0</iceberg.version>
<avro.version>1.11.3</avro.version>
<azure.storage.version>12.25.0</azure.storage.version>
<junit.version>5.10.2</junit.version>
<mockito.version>5.11.0</mockito.version>
```

---

## CODING STANDARDS — ENFORCE EVERYWHERE

1. **Java 21 style:** Records for all data models. Virtual threads for HTTP clients (SSE + REST polling). Pattern matching with `switch` expressions. Sealed interfaces where type hierarchies are closed.
2. **Flink:** DataStream API ONLY. Never Table API. Use `IcebergSink` from `iceberg-flink-runtime` with exactly-once semantics.
3. **No hardcoded credentials.** Environment variables everywhere. `.env.example` for all required vars.
4. **Multi-stage Dockerfiles:** Build stage `maven:3.9-eclipse-temurin-21`, runtime stage `eclipse-temurin:21-jre`.
5. **JUnit 5 + Mockito** for every Java class. Test file naming: `*Test.java`. Integration tests: `*IT.java`.
6. **Avro schemas** define the contract. Generate Java classes from `.avsc` files via `avro-maven-plugin`.
7. **No comments unless the WHY is non-obvious.** No Javadoc boilerplate. Well-named identifiers are the documentation.
8. **Package structure:** `io.vega.connector.<source>` for connectors, `io.vega.flink.jobs`, `io.vega.flink.operators`, `io.vega.flink.models`, `io.vega.flink.sinks` for Flink.
9. **Error handling:** Retry with exponential backoff for network calls. Dead-letter topic for unparseable events. Never swallow exceptions silently.
10. **Logging:** SLF4J with Logback. Structured logging where possible. Log levels: ERROR for failures, WARN for recoverable issues, INFO for lifecycle events, DEBUG for event-level detail.

---

## REPOSITORY STRUCTURE — TARGET STATE

```
vega/
├── MASTER_PROMPT.md                     # This file
├── README.md
├── Makefile
├── docker-compose.yml
├── docker-compose.monitoring.yml
├── .gitignore
├── .env.example
│
├── connectors/
│   ├── wikimedia/                       # Phase 2
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/java/io/vega/connector/wikimedia/
│   │       │   ├── WikimediaSourceConnector.java
│   │       │   ├── WikimediaSourceTask.java
│   │       │   ├── WikimediaSourceConfig.java
│   │       │   └── WikimediaSSEClient.java
│   │       ├── main/resources/
│   │       │   └── avro/WikiEvent.avsc
│   │       └── test/java/io/vega/connector/wikimedia/
│   │           ├── WikimediaSourceConnectorTest.java
│   │           ├── WikimediaSourceTaskTest.java
│   │           └── WikimediaSourceConfigTest.java
│   │
│   └── eonet/                           # Phase 3
│       ├── pom.xml
│       ├── Dockerfile
│       └── src/
│           ├── main/java/io/vega/connector/eonet/
│           │   ├── EONETSourceConnector.java
│           │   ├── EONETSourceTask.java
│           │   ├── EONETSourceConfig.java
│           │   └── EONETRestClient.java
│           ├── main/resources/
│           │   └── avro/NaturalEvent.avsc
│           └── test/java/io/vega/connector/eonet/
│               ├── EONETSourceConnectorTest.java
│               ├── EONETSourceTaskTest.java
│               └── EONETSourceConfigTest.java
│
├── flink-jobs/                          # Phase 4
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/io/vega/flink/
│       │   ├── jobs/
│       │   │   ├── WikiEnrichmentJob.java
│       │   │   ├── EONETEnrichmentJob.java
│       │   │   ├── AnomalyDetectionJob.java
│       │   │   ├── AggregationJob.java
│       │   │   └── CorrelationJob.java
│       │   ├── operators/
│       │   │   ├── EditEnricher.java
│       │   │   ├── GeoEnricher.java
│       │   │   └── AnomalyDetector.java
│       │   ├── models/
│       │   │   ├── RawWikiEvent.java
│       │   │   ├── EnrichedWikiEvent.java
│       │   │   ├── NaturalEvent.java
│       │   │   ├── EditAnomaly.java
│       │   │   ├── EditAggregate.java
│       │   │   └── EventCorrelation.java
│       │   └── sinks/
│       │       └── IcebergSinkFactory.java
│       └── test/java/io/vega/flink/
│           ├── jobs/
│           ├── operators/
│           └── models/
│
├── iceberg/                             # Phase 5
│   └── schemas/
│       ├── wiki_events_enriched.sql
│       ├── natural_events.sql
│       ├── edit_anomalies.sql
│       ├── edit_aggregates.sql
│       └── event_correlations.sql
│
├── terraform/                           # Phase 8
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   ├── backend.tf
│   ├── aks.tf
│   ├── adls.tf
│   ├── blob.tf
│   ├── acr.tf
│   └── networking.tf
│
├── k8s/                                 # Phase 7
│   ├── namespace.yaml
│   ├── kafka/
│   │   ├── kafka-cluster.yaml
│   │   └── schema-registry.yaml
│   ├── flink/
│   │   ├── wiki-enrichment-job.yaml
│   │   ├── eonet-enrichment-job.yaml
│   │   ├── anomaly-job.yaml
│   │   ├── aggregation-job.yaml
│   │   └── correlation-job.yaml
│   └── monitoring/
│       ├── prometheus-config.yaml
│       └── grafana-datasource.yaml
│
├── helm/                                # Phase 7
│   ├── strimzi-values.yaml
│   └── flink-operator-values.yaml
│
├── dbt/                                 # Phase 6
│   ├── dbt_project.yml
│   ├── profiles.yml
│   └── models/
│       ├── staging/
│       │   ├── stg_wiki_events.sql
│       │   └── stg_natural_events.sql
│       └── marts/
│           ├── edit_velocity_by_wiki.sql
│           ├── top_edited_articles.sql
│           ├── bot_vs_human_ratio.sql
│           ├── natural_events_by_type.sql
│           └── event_reaction_time.sql
│
├── dashboards/                          # Phase 10
│   └── grafana/
│       └── vega-overview.json
│
├── scripts/
│   └── submit-jobs.sh
│
├── prometheus/
│   └── prometheus.yml
│
├── grafana/
│   └── provisioning/
│       └── datasources/
│           └── prometheus.yml
│
└── .github/                             # Phase 9
    └── workflows/
        ├── build-wikimedia-connector.yml
        ├── build-eonet-connector.yml
        ├── build-flink-jobs.yml
        └── deploy-to-aks.yml
```

---

## EXECUTION PLAN — PHASES 2 THROUGH 10

### IMPORTANT EXECUTION RULES
1. **Execute phases in strict order: 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10.**
2. **After each phase, validate it works, then create a git commit with a descriptive message.**
3. **Commit message format:** `Phase N: <short description>` — e.g., `Phase 2: Wikimedia SSE Kafka source connector`
4. **If a phase has independent sub-tasks, use subagents/parallel execution for speed.** If not, execute sequentially.
5. **Every Java class gets a corresponding test class.** No exceptions. Run `mvn test` and fix all failures before committing.
6. **Phase 11 (Lanka Lens / SL RSS) is DONE.** SL News connector and enrichment job are implemented.
7. **After all phases are complete, run the improvement loops** (see POST-PHASE IMPROVEMENT LOOPS below). Repeat up to 5 times to harden the codebase.
8. **After committing, push to origin/main.**

---

### PHASE 2 — Wikimedia Kafka Source Connector (Java)

**Goal:** Wikipedia edit events flowing from the SSE stream into Kafka topic `raw-wiki-events`.

**Subagent breakdown — spawn 3 subagents in parallel:**

**Subagent 2A — Maven Project + Avro Schema:**
1. Create `connectors/wikimedia/pom.xml` with dependencies: `kafka-connect-api`, `avro`, `confluent-avro-serializer`, `slf4j`, `logback`, `junit-jupiter`, `mockito-core`.
2. Create Avro schema `src/main/resources/avro/WikiEvent.avsc`:
   ```json
   {
     "type": "record",
     "name": "WikiEvent",
     "namespace": "io.vega.connector.wikimedia",
     "fields": [
       {"name": "id", "type": ["null", "long"], "default": null},
       {"name": "title", "type": "string"},
       {"name": "user", "type": "string"},
       {"name": "bot", "type": "boolean"},
       {"name": "wiki", "type": "string"},
       {"name": "server_url", "type": "string"},
       {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
       {"name": "type", "type": "string"},
       {"name": "namespace", "type": "int"},
       {"name": "comment", "type": ["null", "string"], "default": null},
       {"name": "length_old", "type": ["null", "int"], "default": null},
       {"name": "length_new", "type": ["null", "int"], "default": null},
       {"name": "revision_old", "type": ["null", "long"], "default": null},
       {"name": "revision_new", "type": ["null", "long"], "default": null}
     ]
   }
   ```
3. Configure `avro-maven-plugin` to generate Java classes from the schema.
4. Run `mvn compile` to verify schema generation works.

**Subagent 2B — Connector Implementation:**
1. `WikimediaSourceConfig.java` — extends `AbstractConfig`. Config properties: `topic` (default: `raw-wiki-events`), `sse.url` (default: Wikimedia SSE endpoint), `batch.size` (default: 100), `poll.timeout.ms` (default: 1000).
2. `WikimediaSourceConnector.java` — extends `SourceConnector`. Returns single task. Validates config. Returns `WikimediaSourceTask.class` from `taskClass()`.
3. `WikimediaSSEClient.java` — Uses Java 21 `HttpClient` with virtual threads to consume the SSE stream. Parses `event: message` + `data: {json}` format. Puts parsed events into a `LinkedBlockingQueue<WikiEvent>`. Handles reconnection with exponential backoff (1s, 2s, 4s, max 30s). Filters for `type: "edit"` events only (ignore log, categorize, etc.).
4. `WikimediaSourceTask.java` — extends `SourceTask`. In `start()`, creates `WikimediaSSEClient` and starts consuming on a virtual thread. In `poll()`, drains up to `batch.size` events from the queue, converts each to a `SourceRecord` with Avro schema and value. Uses `Collections.singletonMap("stream", "wikimedia")` as source partition, `Collections.singletonMap("position", lastTimestamp)` as source offset.
5. Register the connector class in `META-INF/services/org.apache.kafka.connect.source.SourceConnector`.

**Subagent 2C — Dockerfile + Tests:**
1. `Dockerfile` — Multi-stage: `maven:3.9-eclipse-temurin-21` build stage runs `mvn clean package -DskipTests`. Runtime stage `confluentinc/cp-kafka-connect:7.6.0` copies the JAR to `/usr/share/java/vega-wikimedia-connector/`.
2. Write unit tests for every class:
   - `WikimediaSourceConfigTest.java` — validates defaults, validates required fields, tests invalid config rejection.
   - `WikimediaSourceConnectorTest.java` — tests `taskConfigs()` returns correct config, `taskClass()` returns correct class, `version()` returns non-null.
   - `WikimediaSourceTaskTest.java` — tests `poll()` returns records when queue has events, returns empty list on empty queue, tests record schema matches Avro schema, tests offset tracking.
3. Run `mvn test` — all tests must pass.

**After all 3 subagents complete:**
1. Update `docker-compose.yml` — add the Wikimedia connector container or mount the JAR into the existing `kafka-connect` service.
2. Start the stack: `make up`
3. Deploy the connector via Kafka Connect REST API:
   ```bash
   curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '{
     "name": "wikimedia-source",
     "config": {
       "connector.class": "io.vega.connector.wikimedia.WikimediaSourceConnector",
       "tasks.max": "1",
       "topic": "raw-wiki-events"
     }
   }'
   ```
4. Validate: open Kafka UI at `localhost:8080`, confirm `raw-wiki-events` topic is receiving messages at ~50-100/sec.
5. **Commit:** `Phase 2: Wikimedia SSE Kafka source connector`
6. **Push to origin/main.**

---

### PHASE 3 — NASA EONET Kafka Source Connector (Java)

**Goal:** Natural disaster events flowing from NASA EONET REST API into Kafka topic `raw-natural-events`.

**Subagent breakdown — spawn 3 subagents in parallel:**

**Subagent 3A — Maven Project + Avro Schema:**
1. Create `connectors/eonet/pom.xml` — same dependency pattern as wikimedia connector, plus `jackson-databind` for JSON parsing.
2. Create Avro schema `src/main/resources/avro/NaturalEvent.avsc`:
   ```json
   {
     "type": "record",
     "name": "NaturalEvent",
     "namespace": "io.vega.connector.eonet",
     "fields": [
       {"name": "event_id", "type": "string"},
       {"name": "title", "type": "string"},
       {"name": "description", "type": ["null", "string"], "default": null},
       {"name": "category", "type": "string"},
       {"name": "source_url", "type": ["null", "string"], "default": null},
       {"name": "latitude", "type": "double"},
       {"name": "longitude", "type": "double"},
       {"name": "event_date", "type": "long", "logicalType": "timestamp-millis"},
       {"name": "magnitude_value", "type": ["null", "double"], "default": null},
       {"name": "magnitude_unit", "type": ["null", "string"], "default": null},
       {"name": "is_closed", "type": "boolean"},
       {"name": "ingested_at", "type": "long", "logicalType": "timestamp-millis"}
     ]
   }
   ```
3. Configure `avro-maven-plugin`. Run `mvn compile`.

**Subagent 3B — Connector Implementation:**
1. `EONETSourceConfig.java` — Config: `topic` (default: `raw-natural-events`), `api.url` (default: `https://eonet.gsfc.nasa.gov/api/v3/events`), `poll.interval.ms` (default: 60000), `days.lookback` (default: 30), `status` (default: `open`).
2. `EONETRestClient.java` — Uses Java 21 `HttpClient` with virtual threads. Hits the EONET API with query params `?status=open&days=<lookback>`. Parses JSON response into list of `NaturalEvent`. Handles rate limiting (EONET is generous but respect it). Retries with backoff on 5xx.
3. `EONETSourceConnector.java` — Standard connector boilerplate. Single task.
4. `EONETSourceTask.java` — In `poll()`: if `poll.interval.ms` has elapsed since last poll, call `EONETRestClient`, compare returned event IDs against stored offsets (last seen IDs stored in Kafka Connect offset storage as `Map<String, String>` with partition `{"source": "eonet"}` and offset `{"last_event_ids": "id1,id2,..."}"`). Produce only new/updated events. EONET events can have multiple geometry entries — produce one record per geometry point (an event with 10 fire locations = 10 Kafka records, same event_id, different lat/lon/date).
5. Register in `META-INF/services`.

**Subagent 3C — Dockerfile + Tests:**
1. `Dockerfile` — Same pattern as Phase 2.
2. Unit tests for all classes. For `EONETRestClient`, mock the HTTP response with a sample EONET JSON payload (include a realistic multi-event response).
3. Test that deduplication works: feed the same events twice, verify only new ones are produced.
4. Run `mvn test`.

**After all 3 subagents complete:**
1. Update `docker-compose.yml` to mount the EONET connector JAR.
2. Deploy via REST API. Validate: `raw-natural-events` topic receiving events in Kafka UI.
3. **Commit:** `Phase 3: NASA EONET REST Kafka source connector`
4. **Push to origin/main.**

---

### PHASE 4 — Flink Stream Processing Jobs (Java)

**Goal:** Five Flink jobs processing both streams and writing to Iceberg (or local filesystem for initial testing).

**Subagent breakdown — spawn 5 subagents in parallel (one per job), plus 1 for shared code:**

**Subagent 4-SHARED — Maven Project + Shared Code:**
1. Create `flink-jobs/pom.xml` — Dependencies: `flink-streaming-java`, `flink-connector-kafka`, `flink-avro`, `iceberg-flink-runtime-1.20`, `flink-connector-files` (for local FS sink during testing), Kafka client, Avro, JUnit 5, Mockito.
2. Create model records in `io.vega.flink.models`:
   - `RawWikiEvent.java` — Java record matching WikiEvent Avro schema
   - `EnrichedWikiEvent.java` — extends with: `editSizeDelta` (int), `languageGroup` (String), `isNewArticle` (boolean)
   - `NaturalEvent.java` — Java record matching NaturalEvent Avro schema
   - `EditAnomaly.java` — `title`, `user`, `wiki`, `timestamp`, `anomalyType` (enum: LARGE_EDIT, RAPID_EDITS), `editSizeDelta`, `editCount`
   - `EditAggregate.java` — `wiki`, `windowStart`, `windowEnd`, `totalEdits`, `botEdits`, `humanEdits`, `avgEditSize`
   - `EventCorrelation.java` — `naturalEventId`, `naturalEventTitle`, `category`, `wikiArticleTitle`, `editCount`, `firstEditTimestamp`, `reactionTimeSeconds`, `windowStart`, `windowEnd`
3. Create `IcebergSinkFactory.java` in `io.vega.flink.sinks` — factory that builds Iceberg sinks given a table name and schema. For local dev, write to local Hadoop catalog at `/tmp/iceberg/warehouse`. For prod, use ADLS Gen2 (configured via env vars).
4. Create utility: `FlinkEnvFactory.java` — creates `StreamExecutionEnvironment` with checkpointing enabled, reads Kafka bootstrap servers from env.

**Subagent 4A — WikiEnrichmentJob:**
1. Consume from `raw-wiki-events` (Kafka source, Avro deserialization).
2. Map each event through `EditEnricher` operator:
   - `editSizeDelta` = `length_new - length_old` (handle nulls → 0)
   - `languageGroup` = extract from `wiki` field (e.g., `enwiki` → `en`, `dewiki` → `de`)
   - `isNewArticle` = `length_old == null || length_old == 0`
3. Write to Iceberg table `wiki_events_enriched`.
4. Tests: verify enrichment logic with sample events.

**Subagent 4B — EONETEnrichmentJob:**
1. Consume from `raw-natural-events`.
2. Map through `GeoEnricher` operator:
   - `regionName` = reverse geocode from lat/lon. Use a static lookup table of major world regions (don't call external API). Map lat/lon ranges to region names (e.g., -35 to -10 lat, 110 to 155 lon → "Australia", etc.). Cover major regions, default to "Unknown".
   - `severityLabel` = derive from category + magnitude. E.g., "Wildfires" with no magnitude → "Active", "Severe Storms" → map magnitude to "Minor"/"Moderate"/"Severe".
3. Write to Iceberg table `natural_events`.
4. Tests: verify geo lookup and severity mapping.

**Subagent 4C — AnomalyDetectionJob:**
1. Consume from `raw-wiki-events`.
2. Detect anomalies via `AnomalyDetector` operator:
   - **Large edit:** `editSizeDelta > 10000` characters.
   - **Rapid edits:** Same user makes > 5 edits within 60 seconds (use Flink keyed process function with state, keyed by `user`).
3. Write anomalies to Iceberg table `edit_anomalies`.
4. Tests: verify both anomaly types trigger correctly.

**Subagent 4D — AggregationJob:**
1. Consume enriched wiki stream (or raw stream with inline enrichment).
2. Key by `wiki` field.
3. 1-minute tumbling window.
4. Aggregate: `totalEdits`, `botEdits`, `humanEdits`, `avgEditSize`.
5. Write to Iceberg table `edit_aggregates`.
6. Tests: verify window aggregation with sample events.

**Subagent 4E — CorrelationJob (THE FLAGSHIP):**
1. Dual-stream consume: `raw-natural-events` + `raw-wiki-events`.
2. Strategy:
   - For each natural event, extract keywords from the title (e.g., "Wildfire in California" → ["wildfire", "california"]).
   - Filter wiki edits whose `title` contains any of those keywords (case-insensitive).
   - Use a 30-minute event-time window.
   - Use Flink's `CoProcessFunction` or interval join to correlate.
   - For each correlation: count matching wiki edits, find the first edit timestamp, compute `reactionTimeSeconds = firstEditTimestamp - naturalEvent.eventDate`.
3. Write to Iceberg table `event_correlations`.
4. Tests: verify correlation with synthetic event pairs.

**After all subagents complete:**
1. Run `mvn test` in `flink-jobs/`. All tests pass.
2. Create `Dockerfile` for flink-jobs.
3. Create `scripts/submit-jobs.sh` that submits all 5 jobs to the local Flink cluster via REST API.
4. Update `docker-compose.yml` to mount the Flink job JARs.
5. **Commit:** `Phase 4: Flink stream processing jobs — 5 jobs`
6. **Push to origin/main.**

---

### PHASE 5 — Iceberg Table Schemas

**Goal:** Define all Iceberg table schemas as SQL DDL for documentation and Databricks registration.

**Execute sequentially (small phase):**
1. Create `iceberg/schemas/wiki_events_enriched.sql` — `CREATE TABLE` with all enriched fields, partitioned by `date(timestamp)`.
2. Create `iceberg/schemas/natural_events.sql` — partitioned by `category`.
3. Create `iceberg/schemas/edit_anomalies.sql` — partitioned by `date(timestamp)`.
4. Create `iceberg/schemas/edit_aggregates.sql` — partitioned by `date(window_start)`.
5. Create `iceberg/schemas/event_correlations.sql` — partitioned by `date(window_start)`.
6. Each DDL should include comments on columns and table-level properties.
7. **Commit:** `Phase 5: Iceberg table schemas`
8. **Push to origin/main.**

---

### PHASE 6 — dbt Models

**Goal:** dbt project with staging and mart models targeting Databricks.

**Subagent breakdown — spawn 2 subagents:**

**Subagent 6A — Project Setup + Staging:**
1. Create `dbt/dbt_project.yml` — project name `vega`, profile `vega`.
2. Create `dbt/profiles.yml` — Databricks target using env vars for host, token, warehouse.
3. Create staging models (1:1 with Iceberg tables, light cleaning):
   - `stg_wiki_events.sql` — select from `wiki_events_enriched`, cast types, rename for consistency.
   - `stg_natural_events.sql` — same pattern.
4. Create `dbt/models/staging/schema.yml` with column-level tests (not_null, unique where applicable).

**Subagent 6B — Mart Models:**
1. `edit_velocity_by_wiki.sql` — edits per minute per wiki, rolling 1-hour average.
2. `top_edited_articles.sql` — top 100 most-edited articles in last 24 hours.
3. `bot_vs_human_ratio.sql` — bot edit percentage per wiki per hour.
4. `natural_events_by_type.sql` — active events grouped by category, with counts and latest coordinates.
5. `event_reaction_time.sql` — THE KEY MART: for each natural event, avg/min/max reaction time (seconds until first Wikipedia edit), number of correlated edits, top edited articles. This is the query that answers "how fast does the world react to disasters?"
6. Create `dbt/models/marts/schema.yml` with tests.

**After subagents complete:**
1. Verify `dbt compile` succeeds (may need stub data or skip if no Databricks connection).
2. **Commit:** `Phase 6: dbt staging and mart models`
3. **Push to origin/main.**

---

### PHASE 7 — Kubernetes Manifests

**Goal:** Production-ready K8s manifests for AKS deployment.

**Subagent breakdown — spawn 3 subagents:**

**Subagent 7A — Namespace + Kafka on K8s (Strimzi):**
1. `k8s/namespace.yaml` — namespace `vega`.
2. `helm/strimzi-values.yaml` — Strimzi operator Helm values.
3. `k8s/kafka/kafka-cluster.yaml` — Strimzi `KafkaNodePool` + `Kafka` CR. 3 brokers, KRaft mode, topic auto-creation disabled, retention 7 days, replication factor 3.
4. `k8s/kafka/schema-registry.yaml` — Deployment + Service for Schema Registry pointing to Strimzi Kafka bootstrap.

**Subagent 7B — Flink on K8s:**
1. `helm/flink-operator-values.yaml` — Flink K8s Operator Helm values.
2. `k8s/flink/wiki-enrichment-job.yaml` — `FlinkDeployment` CR. Job mode, parallelism 2, image from ACR.
3. `k8s/flink/eonet-enrichment-job.yaml` — same pattern.
4. `k8s/flink/anomaly-job.yaml`
5. `k8s/flink/aggregation-job.yaml`
6. `k8s/flink/correlation-job.yaml`
7. All jobs: checkpointing to Azure Blob, savepoint on upgrade, resource limits set.

**Subagent 7C — Monitoring on K8s:**
1. `k8s/monitoring/prometheus-config.yaml` — ConfigMap with scrape configs for Strimzi Kafka + Flink pods (use K8s service discovery).
2. `k8s/monitoring/grafana-datasource.yaml` — ConfigMap for Grafana provisioning.

**After subagents complete:**
1. Review manifests for consistency (image names, namespace references, service names).
2. **Commit:** `Phase 7: Kubernetes manifests — Strimzi, Flink Operator, monitoring`
3. **Push to origin/main.**

---

### PHASE 8 — Terraform (Infrastructure as Code)

**Goal:** All Azure infrastructure provisioned via Terraform.

**Execute sequentially (IaC requires careful ordering):**
1. `terraform/main.tf` — provider config (azurerm), resource group.
2. `terraform/variables.tf` — all input variables with descriptions and defaults where safe.
3. `terraform/backend.tf` — Azure Blob backend for remote state.
4. `terraform/networking.tf` — VNet, subnets for AKS.
5. `terraform/aks.tf` — AKS cluster with system + user node pools, RBAC enabled, Azure CNI.
6. `terraform/adls.tf` — ADLS Gen2 storage account + filesystem for Iceberg tables.
7. `terraform/blob.tf` — Blob storage account for Flink checkpoints + Terraform state.
8. `terraform/acr.tf` — Azure Container Registry for Docker images.
9. `terraform/outputs.tf` — AKS cluster name, ACR login server, ADLS endpoint, resource group.
10. Add `terraform/.terraform/` and `*.tfstate*` to `.gitignore` (already done).
11. Verify: `terraform init` and `terraform validate` pass.
12. **Commit:** `Phase 8: Terraform — AKS, ADLS Gen2, ACR, Blob, networking`
13. **Push to origin/main.**

---

### PHASE 9 — CI/CD (GitHub Actions)

**Goal:** Push to main = automatic build, test, and deploy.

**Subagent breakdown — spawn 4 subagents:**

**Subagent 9A — `build-wikimedia-connector.yml`:**
- Trigger: push to `main` with changes in `connectors/wikimedia/**`.
- Steps: checkout, setup Java 21, `mvn test`, `mvn package`, Docker build, push to ACR (credentials from GitHub secrets).

**Subagent 9B — `build-eonet-connector.yml`:**
- Same pattern for `connectors/eonet/**`.

**Subagent 9C — `build-flink-jobs.yml`:**
- Trigger: push to `main` with changes in `flink-jobs/**`.
- Steps: checkout, setup Java 21, `mvn test`, `mvn package`, Docker build, push to ACR.

**Subagent 9D — `deploy-to-aks.yml`:**
- Trigger: workflow_dispatch + runs after any build workflow completes.
- Steps: checkout, Azure login (service principal from secrets), get AKS credentials, Flink savepoint (if job already running), `kubectl apply` all K8s manifests, verify pods healthy.

**After subagents complete:**
1. Verify YAML syntax of all workflow files.
2. **Commit:** `Phase 9: GitHub Actions CI/CD workflows`
3. **Push to origin/main.**

---

### PHASE 10 — Grafana Dashboard + Observability

**Goal:** Live pipeline dashboard and alerting.

**Execute sequentially:**
1. Create `dashboards/grafana/vega-overview.json` — Grafana dashboard JSON with panels:
   - **Row 1:** Pipeline health — events/sec (wiki), events/sec (EONET), Kafka consumer lag, Flink checkpoint duration.
   - **Row 2:** Wikipedia insights — edits per wiki (time series), bot vs human ratio (pie chart), top edited articles (table).
   - **Row 3:** Natural events — active events by category (bar chart), events on world map (if Grafana worldmap plugin available, otherwise table with lat/lon).
   - **Row 4:** Correlations — reaction time histogram, recent correlations table (natural event → wiki article → reaction time), correlation rate over time.
   - **Row 5:** JVM — heap usage, GC pauses, thread count (per Flink TaskManager).
2. Update `grafana/provisioning/` to auto-load the dashboard.
3. Update `prometheus/prometheus.yml` with alert rules:
   - Consumer lag > 10,000 → warning
   - Checkpoint failure → critical
   - No events received in 5 minutes → critical
   - JVM heap > 90% → warning
4. Create `prometheus/alert-rules.yml` with these rules.
5. Update `docker-compose.monitoring.yml` to mount alert rules.
6. **Commit:** `Phase 10: Grafana dashboard + Prometheus alert rules`
7. **Push to origin/main.**

---

## POST-PHASE IMPROVEMENT LOOPS

> After ALL phases (2–11) are complete and committed, execute the following improvement cycle.
> **Note:** Improvement loops are an ongoing quality practice — not a one-time step. Each iteration spawns analysis subagents, prioritizes findings, implements fixes, validates, and commits per category. By loop 5 the codebase should be production-hardened.

### IMPROVEMENT LOOP — REPEAT 5 TIMES

For each iteration of the loop:

**Step 1 — Spawn 15 subagents for analysis (in parallel).**

Each subagent focuses on one specific improvement dimension. They ONLY analyze and report findings — they do NOT make changes yet.

| # | Subagent Focus | What to look for |
|---|---|---|
| 1 | **Error Handling** | Missing try/catch, unhandled edge cases, silent exception swallowing, missing retries on network calls |
| 2 | **Test Coverage** | Missing test cases, untested edge cases, missing integration tests, assertions that are too weak |
| 3 | **Performance** | Inefficient serialization, unnecessary object creation in hot paths, suboptimal Flink operator chains, missing operator chaining hints |
| 4 | **Security** | Hardcoded values that should be secrets, missing input validation, SQL injection in dbt, insecure defaults in K8s manifests (runAsRoot, no resource limits, no network policies) |
| 5 | **Code Quality** | Code duplication across connectors, shared utilities that should be extracted, inconsistent naming, dead code |
| 6 | **Configuration** | Missing config options, hardcoded magic numbers, missing validation in `*Config.java` classes, missing `.env.example` entries |
| 7 | **Docker** | Image size optimization, unnecessary layers, missing health checks, missing `.dockerignore`, build cache optimization |
| 8 | **Kubernetes** | Missing resource requests/limits, missing pod disruption budgets, missing horizontal pod autoscalers, missing liveness/readiness probes, missing network policies |
| 9 | **Terraform** | Missing outputs, missing variable validation, missing lifecycle rules, missing tags, state locking, missing data sources for existing resources |
| 10 | **CI/CD** | Missing caching (Maven, Docker layers), missing matrix builds, missing status badges, missing artifact retention, missing dependency scanning |
| 11 | **Monitoring** | Missing metrics, missing dashboard panels, incomplete alert rules, missing runbooks, missing log aggregation config |
| 12 | **Documentation** | Missing README sections, missing architecture diagrams (as Mermaid in README), missing API documentation for connectors, missing runbook for operations |
| 13 | **Flink Tuning** | Checkpoint interval optimization, state backend configuration, watermark strategy, window trigger optimization, operator parallelism tuning |
| 14 | **Data Quality** | Missing data validation in Flink operators, missing dead-letter queue handling, missing schema evolution handling, missing data freshness monitoring |
| 15 | **Resilience** | Missing circuit breakers, missing backpressure handling, missing graceful shutdown, missing savepoint-on-cancel, missing retry policies in K8s |

**Step 2 — Collect all findings into a prioritized improvement plan.**

Rank findings by:
1. **Critical** — Security vulnerabilities, data loss risks, crashes. Fix immediately.
2. **High** — Performance bottlenecks, missing error handling, missing tests for critical paths.
3. **Medium** — Code quality, configuration, documentation.
4. **Low** — Nice-to-haves, minor optimizations.

**Step 3 — Execute improvements.**

Spawn subagents to implement the improvements in parallel where independent. Group related changes together.

**Step 4 — Validate.**

Run all tests (`mvn test` in each module). Verify Docker builds. Verify `terraform validate`. Verify `dbt compile`. Fix any regressions.

**Step 5 — Commit.**

Create ONE commit per improvement category (not one giant commit). Example:
- `Improve: error handling — add retries, circuit breakers, dead-letter queues`
- `Improve: test coverage — add edge case tests for all operators`
- `Improve: security — add K8s network policies, remove hardcoded defaults`
- `Improve: performance — optimize Flink operator chaining, reduce serialization`

Push all commits to origin/main.

**Step 6 — Repeat from Step 1.**

Each subsequent loop iteration should find fewer issues. By loop 5, the codebase should be production-hardened.

---

## FINAL VALIDATION CHECKLIST

After all improvement loops are complete, verify:

- [ ] `mvn test` passes in `connectors/wikimedia/`, `connectors/eonet/`, `flink-jobs/`
- [ ] All Dockerfiles build successfully
- [ ] `docker compose config` validates both compose files
- [ ] `terraform validate` passes
- [ ] `dbt compile` succeeds
- [ ] All GitHub Actions workflow YAML is valid
- [ ] No hardcoded credentials anywhere (grep for `password`, `secret`, `token`, `key` — all should be env vars or K8s secrets)
- [ ] Every Java class has a corresponding test file
- [ ] README.md is up to date with architecture diagram, quick start, and all endpoints
- [ ] `.env.example` lists every required environment variable
- [ ] All commits are pushed to origin/main

---

## POST-IMPROVEMENT STATUS (200 COMMITS — COMPLETE)

All five improvement loops and the final validation checklist have been executed.
See `IMPROVEMENT_PLAN.md`, `RELEASE_NOTES.md`, and `CHANGELOG.md` for details.

---

## THINGS TO NEVER DO

1. **Never use Flink Table API** — DataStream API only.
2. **Never hardcode credentials** — always environment variables.
3. **Never skip tests** — every class gets tested.
4. **Never use ZooKeeper** — Kafka runs in KRaft mode.
5. **Never commit `.env` files** — only `.env.example`.
6. **Never use `latest` tags in production K8s manifests** — pin specific versions.
7. **Never skip Flink checkpointing** — exactly-once semantics always.
8. **Never write Javadoc boilerplate** — only comment non-obvious WHY.
9. **Never put secrets in Terraform state** — use Azure Key Vault references.

---

*Project: Vega | Owner: Cookie-Cat21 | Stack: Java 21, Kafka KRaft, Flink, Iceberg, Azure AKS, ADLS Gen2, Databricks, dbt, Terraform, Prometheus, Grafana*
