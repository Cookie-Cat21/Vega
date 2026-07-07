# Troubleshooting

## No events in Kafka UI

- Check connector status: `curl localhost:8083/connectors?expand=status`
- Re-register: `./scripts/register-connectors.sh`

## Flink job not running

- Open Flink UI at http://localhost:8081
- Re-submit: `./scripts/submit-jobs.sh`

## High consumer lag

- Scale TaskManagers or increase `VEGA_FLINK_PARALLELISM`
