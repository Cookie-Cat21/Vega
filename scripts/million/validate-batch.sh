#!/usr/bin/env bash
# Lightweight validation after a 25-commit batch
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

echo "==> progress JSON"
python3 - <<'PY'
import json, sys
from pathlib import Path
p = Path("progress/PROGRESS.json")
json.loads(p.read_text())
print("PROGRESS.json: ok")
PY

echo "==> required plan files"
for f in MILLION_COMMIT_PLAN.md progress/HANDOFF.md progress/NEXT_BATCH.md; do
  [[ -f "$f" ]] || { echo "missing $f" >&2; exit 1; }
done

echo "==> docker compose config"
if command -v docker >/dev/null 2>&1; then
  docker compose -f docker-compose.yml config >/dev/null
  docker compose -f docker-compose.monitoring.yml config >/dev/null
  echo "compose: ok"
else
  echo "compose: skipped (docker not installed)"
fi

echo "==> no staged secrets heuristic"
if git grep -nE 'BEGIN (RSA |OPENSSH )?PRIVATE KEY' -- ':!.git' >/dev/null 2>&1; then
  echo "error: private key material detected" >&2
  exit 1
fi

echo "validate-batch: PASS"
