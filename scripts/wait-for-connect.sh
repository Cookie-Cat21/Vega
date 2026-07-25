#!/usr/bin/env bash
set -euo pipefail

CONNECT_REST="${CONNECT_REST:-http://localhost:8083}"
TIMEOUT_SEC="${CONNECT_TIMEOUT_SEC:-180}"
elapsed=0

echo "Waiting for Kafka Connect at ${CONNECT_REST}..."
until curl -sf --max-time 3 "${CONNECT_REST}/" >/dev/null; do
  if [[ "${elapsed}" -ge "${TIMEOUT_SEC}" ]]; then
    echo "Timed out waiting for Kafka Connect after ${TIMEOUT_SEC}s" >&2
    exit 1
  fi
  sleep 3
  elapsed=$((elapsed + 3))
done

echo "Kafka Connect is ready."
