#!/usr/bin/env bash
# Loop gate (every ~100 commits)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

scripts/million/validate-batch.sh

echo "==> make targets (if present)"
if [[ -f Makefile ]]; then
  grep -q 'million-progress' Makefile && make million-progress || scripts/million/status.sh
fi

echo "==> module tests when Java trees dirty (informational)"
echo "Run mvn test in changed modules before closing the loop."

echo "validate-loop: PASS (batch gate + status)"
