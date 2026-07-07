#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== Tearing down Vega local stack ==="
make -C "${ROOT}" down
make -C "${ROOT}" monitoring-down

echo "=== Vega local teardown complete ==="
