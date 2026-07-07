#!/usr/bin/env bash
set -euo pipefail

CONNECT_REST="${CONNECT_REST:-http://localhost:8083}"

register_connector() {
  local name="$1"
  local payload="$2"

  echo "Registering connector: ${name}"
  curl -sf -X POST "${CONNECT_REST}/connectors" \
    -H "Content-Type: application/json" \
    -d "${payload}"
  echo ""
}

register_connector "vega-wikimedia-source" '{
  "name": "vega-wikimedia-source",
  "config": {
    "connector.class": "io.vega.connector.wikimedia.WikimediaSourceConnector",
    "tasks.max": "1",
    "topic": "raw-wiki-events"
  }
}'

register_connector "vega-eonet-source" '{
  "name": "vega-eonet-source",
  "config": {
    "connector.class": "io.vega.connector.eonet.EONETSourceConnector",
    "tasks.max": "1",
    "topic": "raw-natural-events"
  }
}'

register_connector "vega-slnews-source" '{
  "name": "vega-slnews-source",
  "config": {
    "connector.class": "io.vega.connector.slnews.SLNewsSourceConnector",
    "tasks.max": "1",
    "topic": "raw-sl-news"
  }
}'

echo "All Vega Kafka Connect sources registered."
