#!/usr/bin/env bash
# Bump batch/loop/campaign/epoch/era counters after a successful batch
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

scripts/million/sync-progress.sh

python3 - <<'PY'
import json
from pathlib import Path
path = Path("progress/PROGRESS.json")
data = json.loads(path.read_text())

batch = int(data.get("batch", 0)) + 1
loop = int(data.get("loop", 0))
campaign = int(data.get("campaign", 0))
epoch = int(data.get("epoch", 0))
era = int(data.get("era", 0))

# 4 batches per loop, 10 loops per campaign, 10 campaigns per epoch, 10 epochs per era
if batch >= 4:
    batch = 0
    loop += 1
if loop >= 10:
    loop = 0
    campaign += 1
if campaign >= 10:
    campaign = 0
    epoch += 1
if epoch >= 10 and era >= 1:
    # eras 1–10 are 100k; era 0 is special-cased by humans at commit 1000
    epoch = 0
    era += 1
    unlocked = set(data.get("unlocked_eras") or [])
    unlocked.add(era)
    data["unlocked_eras"] = sorted(unlocked)

data["batch"] = batch
data["loop"] = loop
data["campaign"] = campaign
data["epoch"] = epoch
data["era"] = era
data["status"] = "planned"
path.write_text(json.dumps(data, indent=2) + "\n")
print(f"Advanced to era={era} epoch={epoch} campaign={campaign} loop={loop} batch={batch}")
PY
