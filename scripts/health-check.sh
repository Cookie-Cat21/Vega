#!/usr/bin/env bash
set -euo pipefail

KAFKA_HOST="${KAFKA_HOST:-localhost}"
KAFKA_PORT="${KAFKA_PORT:-9092}"
FLINK_REST="${FLINK_REST:-http://localhost:8081}"
SCHEMA_REGISTRY="${SCHEMA_REGISTRY:-http://localhost:8082}"

failures=0

check_tcp() {
  local name="$1"
  local host="$2"
  local port="$3"

  if timeout 3 bash -c "echo >/dev/tcp/${host}/${port}" 2>/dev/null; then
    echo "OK  ${name} (${host}:${port})"
  else
    echo "FAIL ${name} (${host}:${port})"
    failures=$((failures + 1))
  fi
}

check_http() {
  local name="$1"
  local url="$2"

  if curl -sf --max-time 5 "${url}" >/dev/null; then
    echo "OK  ${name} (${url})"
  else
    echo "FAIL ${name} (${url})"
    failures=$((failures + 1))
  fi
}

check_tcp "Kafka" "${KAFKA_HOST}" "${KAFKA_PORT}"
check_http "Flink" "${FLINK_REST}/overview"
check_http "Schema Registry" "${SCHEMA_REGISTRY}/subjects"

if [[ "${failures}" -gt 0 ]]; then
  echo ""
  echo "${failures} health check(s) failed."
  exit 1
fi

echo ""
echo "All Vega stack endpoints are healthy."
