#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

echo "Validating Docker Compose configuration..."
docker compose config >/dev/null

echo "Running Maven tests..."
make test

echo "Stack validation passed."
