# AGENTS.md

## Cursor Cloud specific instructions

Vega is an infrastructure-only ("Phase 1") repo: the whole product is a Docker Compose
streaming stack (Kafka KRaft, Schema Registry, Kafka Connect, Kafka UI, Flink
JobManager/TaskManager) plus an optional monitoring stack (Prometheus, Grafana).
There is no application source, build system, lint, or automated tests yet
(see `MASTER_PROMPT.md` for the planned roadmap — treat it as future work, not
existing functionality). Standard commands live in the `Makefile` and `README.md`.

### Running the stack
- Docker has no systemd here. The Docker daemon must be started manually, e.g.
  `sudo dockerd` in a background/tmux session, before any `make`/`docker` command.
  The daemon uses the `fuse-overlayfs` storage driver with the containerd snapshotter
  disabled (`/etc/docker/daemon.json`).
- Core stack: `make up`. Monitoring: `make monitoring`. `make up` MUST run before
  `make monitoring` — the monitoring compose file attaches to the external
  `vega-network` created by the core stack. Other targets: `make logs`, `make status`,
  `make down`, `make clean` (clean also removes volumes).
- Endpoints: Kafka UI :8080 · Flink UI :8081 · Schema Registry :8082 · Kafka Connect
  :8083 · Kafka bootstrap :9092 · Prometheus :9090 · Grafana :3000 (admin / vega).

### Non-obvious gotchas
- Kafka volume permissions: the `kafka-data` named volume is mounted at
  `/tmp/kraft-combined-logs`, but the Confluent image runs as uid 1000. A freshly
  created (root-owned) volume makes Kafka exit with `meta.properties ... Permission
  denied`. If Kafka is unhealthy after a fresh `make up` (or after `make clean`), fix
  the volume ownership and restart it:
  `sudo chown -R 1000:1000 "$(docker volume inspect workspace_kafka-data -f '{{.Mountpoint}}')" && docker start vega-kafka`
  (the compose project name is `workspace`, from the repo directory name).
- Prometheus `kafka` / `flink-*` scrape targets show `down` by design: this phase has
  not wired up a Kafka JMX-to-Prometheus exporter or the Flink Prometheus reporter
  (`:9101` is raw JMX, `:9249` is unconfigured). Prometheus and Grafana themselves run
  fine. Don't treat those down targets as a regression.
- The Kafka `CLUSTER_ID` must be a base64-encoded 16-byte UUID (22 chars). An invalid
  value makes Kafka exit immediately during format with "not equal to the expected 16
  bytes".

### Smoke test (core functionality = Kafka messaging)
```bash
docker exec vega-kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vega.events --partitions 3 --replication-factor 1
echo '{"event_id":"eonet-1","type":"wildfire"}' | docker exec -i vega-kafka kafka-console-producer --bootstrap-server kafka:29092 --topic vega.events
docker exec vega-kafka kafka-console-consumer --bootstrap-server kafka:29092 --topic vega.events --from-beginning --max-messages 1 --timeout-ms 10000
```
