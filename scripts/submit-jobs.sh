#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FLINK_REST="${FLINK_REST:-http://localhost:8081}"
CONTAINER="${FLINK_JOBMANAGER_CONTAINER:-vega-flink-jobmanager}"
JAR_IN_CONTAINER="${FLINK_JAR_IN_CONTAINER:-/opt/flink/usrlib/vega-flink-jobs.jar}"
PARALLELISM="${VEGA_FLINK_PARALLELISM:-1}"

JOBS=(
  "io.vega.flink.jobs.WikiEnrichmentJob"
  "io.vega.flink.jobs.EONETEnrichmentJob"
  "io.vega.flink.jobs.AnomalyDetectionJob"
  "io.vega.flink.jobs.AggregationJob"
  "io.vega.flink.jobs.CorrelationJob"
  "io.vega.flink.jobs.SLNewsEnrichmentJob"
)

if [[ "${1:-}" == "--wiki-only" ]]; then
  JOBS=("io.vega.flink.jobs.WikiEnrichmentJob")
fi

if ! curl -sf --max-time 5 "${FLINK_REST}/overview" >/dev/null; then
  echo "Flink JobManager not reachable at ${FLINK_REST}" >&2
  exit 1
fi

if ! docker exec "${CONTAINER}" test -f "${JAR_IN_CONTAINER}"; then
  echo "Flink JAR missing in container at ${JAR_IN_CONTAINER}." >&2
  echo "Run 'make build' first so Compose can mount flink-jobs/target/*.jar." >&2
  exit 1
fi

for job in "${JOBS[@]}"; do
  echo "Submitting ${job}..."
  docker exec "${CONTAINER}" flink run -d \
    -p "${PARALLELISM}" \
    -c "${job}" \
    "${JAR_IN_CONTAINER}"
done

echo "Submitted ${#JOBS[@]} Flink job(s)."
