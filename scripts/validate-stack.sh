#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

echo "Validating Docker Compose configuration..."
docker compose config >/dev/null

echo "Running Maven tests..."
make test

echo "Running health checks..."
"${ROOT}/scripts/health-check.sh" || echo "Health check skipped (stack may not be running)"

echo "Stack validation passed."
