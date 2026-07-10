#!/usr/bin/env bash
# Atomic commit helper for the million-commit loop
# Usage: scripts/million/commit-one.sh <N> <category> <summary> [paths...]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

N="${1:?usage: commit-one.sh N category summary [paths...]}"
CATEGORY="${2:?category required}"
SUMMARY="${3:?summary required}"
shift 3

if [[ $# -gt 0 ]]; then
  git add -- "$@"
else
  git add -u
fi

if git diff --cached --quiet; then
  echo "error: nothing staged; refusing empty commit" >&2
  exit 1
fi

git commit -m "Improve: ${CATEGORY} — ${SUMMARY} (commit ${N}/1000000)"
echo "Created commit ${N}/1000000"
