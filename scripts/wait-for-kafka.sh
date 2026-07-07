#!/usr/bin/env bash
set -euo pipefail

HOST="${KAFKA_HOST:-localhost}"
PORT="${KAFKA_PORT:-9092}"
TIMEOUT="${TIMEOUT:-120}"

echo "Waiting for Kafka at ${HOST}:${PORT} (timeout ${TIMEOUT}s)..."

for ((i = 1; i <= TIMEOUT; i++)); do
    if (echo >"/dev/tcp/${HOST}/${PORT}") >/dev/null 2>&1; then
        echo "Kafka is ready on ${HOST}:${PORT}"
        exit 0
    fi
    sleep 1
done

echo "Timed out waiting for Kafka at ${HOST}:${PORT}" >&2
exit 1
