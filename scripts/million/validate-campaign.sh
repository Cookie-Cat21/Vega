#!/usr/bin/env bash
# Campaign gate (every ~1000 commits)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

scripts/million/validate-loop.sh
scripts/million/sync-progress.sh

echo "==> campaign checklist"
[[ -f CHANGELOG.md ]] || { echo "missing CHANGELOG.md" >&2; exit 1; }
[[ -f progress/HANDOFF.md ]] || { echo "missing HANDOFF.md" >&2; exit 1; }

echo "validate-campaign: PASS — remember to append CHANGELOG + epoch notes"
