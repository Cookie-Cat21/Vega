# Scripts

| Script | Purpose |
|--------|---------|
| `bootstrap-local.sh` | Build, start stack, register connectors, submit jobs |
| `register-connectors.sh` | Deploy Kafka Connect sources (idempotent) |
| `submit-jobs.sh` | Submit Flink jobs via JobManager CLI |
| `produce-fixtures.sh` | Bounded Avro fixture events for local demo |
| `demo-local.sh` | End-to-end local proof: fixtures → Flink file sink |
| `health-check.sh` | Verify Kafka / Flink / Schema Registry / Connect |
| `validate-stack.sh` | Compose config + Maven tests + soft health check |
| `wait-for-kafka.sh` | Block until Kafka accepts TCP |
| `wait-for-connect.sh` | Block until Connect REST is ready |
| `teardown-local.sh` | Stop local services |

Makefile wrappers: `make bootstrap`, `make demo`, `make register-connectors`, `make submit-jobs`, `make health-check`.
