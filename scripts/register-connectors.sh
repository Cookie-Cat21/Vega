#!/usr/bin/env bash
set -euo pipefail

CONNECT_REST="${CONNECT_REST:-http://localhost:8083}"

upsert_connector() {
  local name="$1"
  local config_json="$2"

  if curl -sf "${CONNECT_REST}/connectors/${name}" >/dev/null; then
    echo "Updating connector: ${name}"
    curl -sf -X PUT "${CONNECT_REST}/connectors/${name}/config" \
      -H "Content-Type: application/json" \
      -d "${config_json}"
  else
    echo "Registering connector: ${name}"
    curl -sf -X POST "${CONNECT_REST}/connectors" \
      -H "Content-Type: application/json" \
      -d "{\"name\": \"${name}\", \"config\": ${config_json}}"
  fi
  echo ""
}

upsert_connector "vega-wikimedia-source" '{
  "connector.class": "io.vega.connector.wikimedia.WikimediaSourceConnector",
  "tasks.max": "1",
  "topic": "raw-wiki-events"
}'

upsert_connector "vega-eonet-source" '{
  "connector.class": "io.vega.connector.eonet.EONETSourceConnector",
  "tasks.max": "1",
  "topic": "raw-natural-events"
}'

upsert_connector "vega-slnews-source" '{
  "connector.class": "io.vega.connector.slnews.SLNewsSourceConnector",
  "tasks.max": "1",
  "topic": "raw-sl-news"
}'

echo "All Vega Kafka Connect sources registered."
