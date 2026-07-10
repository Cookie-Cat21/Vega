#!/usr/bin/env bash
# Show million-commit loop progress
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

COUNT="$(git rev-list --count HEAD 2>/dev/null || echo 0)"
TARGET=1000000
REMAIN=$((TARGET - COUNT))
PCT="$(awk -v c="$COUNT" -v t="$TARGET" 'BEGIN { printf "%.4f", (c/t)*100 }')"

echo "Vega million-commit status"
echo "  commits:   ${COUNT} / ${TARGET}"
echo "  remaining: ${REMAIN}"
echo "  progress:  ${PCT}%"

if [[ -f progress/PROGRESS.json ]]; then
  echo "  tracker:   progress/PROGRESS.json"
  if command -v python3 >/dev/null 2>&1; then
    python3 - <<'PY'
import json
from pathlib import Path
p = json.loads(Path("progress/PROGRESS.json").read_text())
print(f"  era/epoch/campaign/loop/batch: {p.get('era')}/{p.get('epoch')}/{p.get('campaign')}/{p.get('loop')}/{p.get('batch')}")
print(f"  status:    {p.get('status')}")
print(f"  next #:    {p.get('next_commit_number')}")
PY
  fi
fi
