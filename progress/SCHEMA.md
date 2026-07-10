# progress/PROGRESS.json schema

Machine-readable cursor for the million-commit loop.

| Field | Type | Description |
|-------|------|-------------|
| `schema_version` | number | Schema revision (currently `1`) |
| `project` | string | Project id (`vega`) |
| `target_commits` | number | Always `1000000` |
| `current_commits` | number | Synced from `git rev-list --count HEAD` |
| `next_commit_number` | number | Next `N` in commit messages |
| `remaining_commits` | number | `target - current` |
| `era` | number | 0–10 |
| `epoch` | number | 0–9 within era |
| `campaign` | number | 0–9 within epoch |
| `loop` | number | 0–9 within campaign |
| `batch` | number | 0–3 within loop |
| `status` | string | `planned` \| `in_progress` \| `gated` \| `blocked` |
| `blocked_reason` | string\|null | Why blocked, if any |
| `primary_dimensions` | string[] | Current loop focus (3) |
| `secondary_dimensions` | string[] | Supporting focus (2) |
| `active_factories` | string[] | Unlocked work generators |
| `unlocked_eras` | number[] | Eras available to execute |
| `notes` | string | Freeform operator notes |

Refresh counters with `scripts/million/sync-progress.sh`.
