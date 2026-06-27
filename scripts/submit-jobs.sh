#!/usr/bin/env bash
set -euo pipefail

FLINK_REST="${FLINK_REST:-http://localhost:8081}"
JAR="${FLINK_JAR:-/opt/flink/usrlib/vega-flink-jobs.jar}"

JOBS=(
  "io.vega.flink.jobs.WikiEnrichmentJob"
  "io.vega.flink.jobs.EONETEnrichmentJob"
  "io.vega.flink.jobs.AnomalyDetectionJob"
  "io.vega.flink.jobs.AggregationJob"
  "io.vega.flink.jobs.CorrelationJob"
  "io.vega.flink.jobs.SLNewsEnrichmentJob"
)

for job in "${JOBS[@]}"; do
  echo "Submitting ${job}..."
  curl -s -X POST "${FLINK_REST}/jars/upload" -F "jarfile=@${JAR}" > /dev/null 2>&1 || true
  JAR_ID=$(curl -s "${FLINK_REST}/jars" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
  curl -s -X POST "${FLINK_REST}/jars/${JAR_ID}/run" \
    -H "Content-Type: application/json" \
    -d "{\"entryClass\": \"${job}\", \"parallelism\": 1}"
  echo ""
done

echo "All Vega Flink jobs submitted."
