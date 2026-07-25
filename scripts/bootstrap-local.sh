#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

mkdir -p "${ROOT}/data/vega-output" "${ROOT}/data/iceberg"

echo "=== Building Vega artifacts ==="
make -C "${ROOT}" build

echo "=== Starting local stack ==="
make -C "${ROOT}" up

echo "=== Waiting for Kafka ==="
"${ROOT}/scripts/wait-for-kafka.sh"

echo "=== Waiting for Kafka Connect ==="
"${ROOT}/scripts/wait-for-connect.sh"

echo "=== Waiting for Flink ==="
elapsed=0
until curl -sf --max-time 3 "${FLINK_REST:-http://localhost:8081}/overview" >/dev/null; do
  if [[ "${elapsed}" -ge 120 ]]; then
    echo "Timed out waiting for Flink" >&2
    exit 1
  fi
  sleep 3
  elapsed=$((elapsed + 3))
done

echo "=== Registering Kafka Connect sources ==="
"${ROOT}/scripts/register-connectors.sh"

echo "=== Submitting Flink jobs ==="
"${ROOT}/scripts/submit-jobs.sh"

echo "=== Vega local bootstrap complete ==="
echo "File sink output: ${ROOT}/data/vega-output"
echo "Bounded proof without live APIs: make demo"
