# Vega — Real-Time Streaming Lakehouse Pipeline

Vega tracks global natural events and real-time human reactions to them. A wildfire starts (NASA EONET) → Wikipedia edits spike → Vega captures the correlation live.

## Tech Stack

Java 21 · Kafka 4.x (KRaft) · Flink 2.x · Apache Iceberg · Azure ADLS Gen2 · Databricks · dbt · Terraform · Prometheus · Grafana

## Data Sources

| Source | Type | Status |
|---|---|---|
| Wikimedia EventStreams | SSE (real-time) | Active |
| NASA EONET | REST polling (60s) | Active |
| Sri Lanka RSS Feeds | RSS polling (5m) | Future |

## Quick Start

```bash
# Start the core stack (Kafka, Flink, Schema Registry, Kafka UI)
make up

# Start monitoring (Prometheus, Grafana)
make monitoring

# View logs
make logs

# Tear down
make down
```

### Endpoints

| Service | URL |
|---|---|
| Kafka UI | http://localhost:8080 |
| Flink UI | http://localhost:8081 |
| Schema Registry | http://localhost:8082 |
| Kafka Connect | http://localhost:8083 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

## License

MIT
