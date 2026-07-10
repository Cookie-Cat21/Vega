# Million-commit loop scripts

Helpers for the Vega agentic loop defined in `/MILLION_COMMIT_PLAN.md`.

| Script | Purpose |
|--------|---------|
| `status.sh` | Print commit count vs 1,000,000 |
| `sync-progress.sh` | Refresh `progress/PROGRESS.json` from git |
| `commit-one.sh` | Atomic commit with `commit N/1000000` message |
| `claim-batch.sh` | Mark batch `in_progress` |
| `complete-batch.sh` | Advance batch/loop/campaign counters |
| `validate-batch.sh` | Fast gate after 25 commits |
| `validate-loop.sh` | Gate after ~100 commits |
| `validate-campaign.sh` | Gate after ~1000 commits |

## Typical batch flow

```bash
scripts/million/sync-progress.sh
scripts/million/claim-batch.sh
# ... implement items from progress/NEXT_BATCH.md ...
scripts/million/commit-one.sh 204 documentation "add ADR index" docs/adr/README.md
scripts/million/validate-batch.sh
scripts/million/complete-batch.sh
```
