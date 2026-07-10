#!/usr/bin/env bash
# Mark current batch in_progress in PROGRESS.json
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

python3 - <<'PY'
import json
from pathlib import Path
path = Path("progress/PROGRESS.json")
data = json.loads(path.read_text())
data["status"] = "in_progress"
path.write_text(json.dumps(data, indent=2) + "\n")
print(f"Claimed batch {data.get('batch')} (status=in_progress)")
PY
