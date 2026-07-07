#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== Building Vega artifacts ==="
make -C "${ROOT}" build

echo "=== Starting local stack ==="
make -C "${ROOT}" up

echo "=== Registering Kafka Connect sources ==="
"${ROOT}/scripts/register-connectors.sh"

echo "=== Submitting Flink jobs ==="
"${ROOT}/scripts/submit-jobs.sh"

echo "=== Vega local bootstrap complete ==="
