#!/usr/bin/env bash
# Sync progress/PROGRESS.json counters from git history
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

COUNT="$(git rev-list --count HEAD)"
TARGET=1000000
REMAIN=$((TARGET - COUNT))
NEXT=$((COUNT + 1))

python3 - <<PY
import json
from pathlib import Path
path = Path("progress/PROGRESS.json")
data = json.loads(path.read_text())
data["current_commits"] = ${COUNT}
data["remaining_commits"] = ${REMAIN}
data["next_commit_number"] = ${NEXT}
data["target_commits"] = ${TARGET}
path.write_text(json.dumps(data, indent=2) + "\n")
print(f"Synced: current={${COUNT}} next={${NEXT}} remaining={${REMAIN}}")
PY
