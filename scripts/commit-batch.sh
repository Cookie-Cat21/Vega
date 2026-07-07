#!/usr/bin/env bash
# Helper to commit files one-at-a-time toward 200-commit goal
set -euo pipefail
COMMIT="${1:?usage: commit-batch.sh START_NUM}"
shift
for entry in "$@"; do
  file="${entry%%|*}"
  msg="${entry##*|}"
  if [ -f "$file" ] || [ -d "$file" ]; then
    git add "$file"
    git commit -m "Improve: ${msg} (commit ${COMMIT}/200)"
    COMMIT=$((COMMIT + 1))
  fi
done
echo "Next commit number: ${COMMIT}"
