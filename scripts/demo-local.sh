#!/usr/bin/env bash
# Bounded local demo: build → up → fixtures → WikiEnrichmentJob → assert file sink.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

FIXTURE_COUNT="${VEGA_DEMO_FIXTURE_COUNT:-5}"
OUTPUT_DIR="${ROOT}/data/vega-output/wiki_events_enriched"
FLINK_REST="${FLINK_REST:-http://localhost:8081}"
WAIT_SEC="${VEGA_DEMO_WAIT_SEC:-120}"
DEMO_SUFFIX="${VEGA_CONSUMER_GROUP_SUFFIX:-demo-$(date +%s)}"

mkdir -p data/vega-output data/iceberg data/flink-checkpoints data/flink-savepoints
chmod -R a+rwX data 2>/dev/null || sudo chmod -R a+rwX data 2>/dev/null || true

echo "=== 1/6 Build artifacts ==="
make build

echo "=== 2/6 Start Compose stack ==="
# Unique consumer group so each demo run re-reads fixture offsets with earliest.
export VEGA_CONSUMER_GROUP_SUFFIX="${DEMO_SUFFIX}"
# Compose does not pass host env into Flink by default — recreate with override.
cat > docker-compose.demo.override.yml <<EOF
services:
  flink-jobmanager:
    environment:
      VEGA_CONSUMER_GROUP_SUFFIX: ${DEMO_SUFFIX}
      VEGA_KAFKA_STARTING_OFFSETS: earliest
  flink-taskmanager:
    environment:
      VEGA_CONSUMER_GROUP_SUFFIX: ${DEMO_SUFFIX}
      VEGA_KAFKA_STARTING_OFFSETS: earliest
EOF

docker compose -f docker-compose.yml -f docker-compose.demo.override.yml up -d --build
./scripts/wait-for-kafka.sh
./scripts/wait-for-connect.sh

echo "Waiting for Flink JobManager..."
elapsed=0
until curl -sf --max-time 3 "${FLINK_REST}/overview" >/dev/null; do
  if [[ "${elapsed}" -ge 120 ]]; then
    echo "Timed out waiting for Flink" >&2
    exit 1
  fi
  sleep 3
  elapsed=$((elapsed + 3))
done

echo "=== 3/6 Health check ==="
./scripts/health-check.sh

echo "=== 4/6 Produce ${FIXTURE_COUNT} wiki fixture events (before job submit) ==="
rm -rf "${OUTPUT_DIR}" 2>/dev/null || sudo rm -rf "${OUTPUT_DIR}" 2>/dev/null || true
mkdir -p "${OUTPUT_DIR}"
chmod -R a+rwX data 2>/dev/null || sudo chmod -R a+rwX data 2>/dev/null || true
./scripts/produce-fixtures.sh raw-wiki-events "${FIXTURE_COUNT}"

echo "=== 5/6 Submit WikiEnrichmentJob (earliest + group suffix ${DEMO_SUFFIX}) ==="
JOB_IDS="$(curl -sf "${FLINK_REST}/jobs" | grep -oE '"id":"[a-f0-9]+"' | cut -d'"' -f4 || true)"
for jid in ${JOB_IDS}; do
  detail="$(curl -sf "${FLINK_REST}/jobs/${jid}" || true)"
  if echo "${detail}" | grep -q '"name":"WikiEnrichmentJob"'; then
    state="$(echo "${detail}" | grep -oE '"state":"[A-Z]+"' | head -1 | cut -d'"' -f4 || true)"
    if [[ "${state}" == "RUNNING" || "${state}" == "RESTARTING" || "${state}" == "CREATED" ]]; then
      echo "Cancelling existing job ${jid}"
      curl -sf -X PATCH "${FLINK_REST}/jobs/${jid}?mode=cancel" >/dev/null || true
      sleep 2
    fi
  fi
done

./scripts/submit-jobs.sh --wiki-only

echo "Waiting for WikiEnrichmentJob to reach RUNNING..."
elapsed=0
while true; do
  running="$(curl -sf "${FLINK_REST}/jobs" || true)"
  for jid in $(echo "${running}" | grep -oE '"id":"[a-f0-9]+"' | cut -d'"' -f4); do
    detail="$(curl -sf "${FLINK_REST}/jobs/${jid}" || true)"
    if echo "${detail}" | grep -q '"name":"WikiEnrichmentJob"' && echo "${detail}" | grep -q '"state":"RUNNING"'; then
      break 2
    fi
  done
  if [[ "${elapsed}" -ge 60 ]]; then
    echo "WikiEnrichmentJob did not reach RUNNING in time" >&2
    curl -sf "${FLINK_REST}/jobs" || true
    exit 1
  fi
  sleep 2
  elapsed=$((elapsed + 2))
done

echo "=== 6/6 Wait for file sink output (up to ${WAIT_SEC}s) ==="
elapsed=0
while true; do
  hits="$(find "${OUTPUT_DIR}" -type f ! -name '.*' -size +0c 2>/dev/null | wc -l | tr -d ' ')"
  inprogress="$(find "${OUTPUT_DIR}" -type f -name '*.inprogress*' -size +0c 2>/dev/null | wc -l | tr -d ' ')"
  total_bytes="$(find "${OUTPUT_DIR}" -type f -size +0c -printf '%s\n' 2>/dev/null | awk '{s+=$1} END {print s+0}')"
  if [[ "${hits}" -gt 0 || "${inprogress}" -gt 0 ]] && [[ "${total_bytes}" -gt 0 ]]; then
    echo ""
    echo "SUCCESS: file sink has data under ${OUTPUT_DIR}"
    echo "  files with bytes: ${hits} (in-progress: ${inprogress})"
    echo "  total bytes:      ${total_bytes}"
    echo ""
    echo "Sample output:"
    find "${OUTPUT_DIR}" -type f -size +0c | head -3 | while read -r f; do
      echo "--- ${f} ---"
      head -c 400 "${f}" || true
      echo ""
    done
    echo ""
    echo "Also verify:"
    echo "  Kafka UI:  http://localhost:8080  (topic raw-wiki-events)"
    echo "  Flink UI:  http://localhost:8081  (WikiEnrichmentJob RUNNING)"
    rm -f docker-compose.demo.override.yml
    exit 0
  fi
  if [[ "${elapsed}" -ge "${WAIT_SEC}" ]]; then
    echo "Timed out waiting for Flink file sink output in ${OUTPUT_DIR}" >&2
    echo "Flink jobs:" >&2
    curl -sf "${FLINK_REST}/jobs" >&2 || true
    echo "" >&2
    echo "TaskManager logs (tail):" >&2
    docker logs --tail 120 vega-flink-taskmanager >&2 || true
    rm -f docker-compose.demo.override.yml
    exit 1
  fi
  sleep 3
  elapsed=$((elapsed + 3))
done
