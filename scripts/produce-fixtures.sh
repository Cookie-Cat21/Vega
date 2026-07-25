#!/usr/bin/env bash
# Produce a bounded set of Avro fixture events for local demo (no live APIs).
set -euo pipefail

TOPIC="${1:-raw-wiki-events}"
COUNT="${2:-5}"
CONTAINER="${SCHEMA_REGISTRY_CONTAINER:-vega-schema-registry}"
BOOTSTRAP="${KAFKA_INTERNAL_BOOTSTRAP:-kafka:29092}"
SCHEMA_URL="${SCHEMA_REGISTRY_INTERNAL_URL:-http://localhost:8081}"
NOW_MS="$(date +%s000)"

if ! docker ps --format '{{.Names}}' | grep -qx "${CONTAINER}"; then
  echo "Schema Registry container '${CONTAINER}' is not running. Start the stack with 'make up'." >&2
  exit 1
fi

WIKI_SCHEMA='{"type":"record","name":"WikiEvent","namespace":"io.vega.connector.wikimedia","fields":[{"name":"id","type":["null","long"],"default":null},{"name":"title","type":"string"},{"name":"user","type":"string"},{"name":"bot","type":"boolean"},{"name":"wiki","type":"string"},{"name":"server_url","type":"string"},{"name":"timestamp","type":"long"},{"name":"type","type":"string"},{"name":"namespace","type":"int"},{"name":"comment","type":["null","string"],"default":null},{"name":"length_old","type":["null","int"],"default":null},{"name":"length_new","type":["null","int"],"default":null},{"name":"revision_old","type":["null","long"],"default":null},{"name":"revision_new","type":["null","long"],"default":null}]}'

EONET_SCHEMA='{"type":"record","name":"NaturalEvent","namespace":"io.vega.connector.eonet","fields":[{"name":"event_id","type":"string"},{"name":"title","type":"string"},{"name":"description","type":["null","string"],"default":null},{"name":"category","type":"string"},{"name":"source_url","type":["null","string"],"default":null},{"name":"latitude","type":"double"},{"name":"longitude","type":"double"},{"name":"event_date","type":"long"},{"name":"magnitude_value","type":["null","double"],"default":null},{"name":"magnitude_unit","type":["null","string"],"default":null},{"name":"is_closed","type":"boolean"},{"name":"ingested_at","type":"long"}]}'

SLNEWS_SCHEMA='{"type":"record","name":"SLNewsArticle","namespace":"io.vega.connector.slnews","fields":[{"name":"article_id","type":"string"},{"name":"title","type":"string"},{"name":"description","type":["null","string"],"default":null},{"name":"link","type":"string"},{"name":"source_feed","type":"string"},{"name":"source_name","type":"string"},{"name":"published_at","type":"long"},{"name":"ingested_at","type":"long"},{"name":"language","type":"string"}]}'

case "${TOPIC}" in
  raw-wiki-events)
    SCHEMA="${WIKI_SCHEMA}"
    ;;
  raw-natural-events)
    SCHEMA="${EONET_SCHEMA}"
    ;;
  raw-sl-news)
    SCHEMA="${SLNEWS_SCHEMA}"
    ;;
  *)
    echo "Unsupported topic: ${TOPIC}" >&2
    echo "Use: raw-wiki-events | raw-natural-events | raw-sl-news" >&2
    exit 1
    ;;
esac

payload_for() {
  local i="$1"
  case "${TOPIC}" in
    raw-wiki-events)
      cat <<EOF
{"id":{"long":${i}},"title":"Vega Demo Wildfire ${i}","user":"vega-fixture","bot":false,"wiki":"enwiki","server_url":"https://en.wikipedia.org","timestamp":${NOW_MS},"type":"edit","namespace":0,"comment":{"string":"fixture edit ${i}"},"length_old":{"int":100},"length_new":{"int":150},"revision_old":{"long":${i}},"revision_new":{"long":$((i + 1000))}}
EOF
      ;;
    raw-natural-events)
      cat <<EOF
{"event_id":"EONET-FIXTURE-${i}","title":"Vega Demo Fire ${i}","description":{"string":"bounded fixture event"},"category":"wildfires","source_url":{"string":"https://example.com/eonet/${i}"},"latitude":6.9,"longitude":79.8,"event_date":${NOW_MS},"magnitude_value":{"double":1.0},"magnitude_unit":{"string":"acres"},"is_closed":false,"ingested_at":${NOW_MS}}
EOF
      ;;
    raw-sl-news)
      cat <<EOF
{"article_id":"sl-fixture-${i}","title":"Vega Demo News ${i}","description":{"string":"bounded fixture article"},"link":"https://example.com/news/${i}","source_feed":"fixture","source_name":"Vega Fixture","published_at":${NOW_MS},"ingested_at":${NOW_MS},"language":"en"}
EOF
      ;;
  esac
}

echo "Producing ${COUNT} fixture record(s) to ${TOPIC}..."

{
  for i in $(seq 1 "${COUNT}"); do
    payload_for "${i}"
  done
} | docker exec -i "${CONTAINER}" kafka-avro-console-producer \
  --bootstrap-server "${BOOTSTRAP}" \
  --topic "${TOPIC}" \
  --property "schema.registry.url=${SCHEMA_URL}" \
  --property "value.schema=${SCHEMA}"

echo "Produced ${COUNT} record(s) to ${TOPIC}."
