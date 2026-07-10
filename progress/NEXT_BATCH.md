# Next Batch — Era 0 / Epoch 0 / Campaign 0 / Loop 0 / Batch 0

**Commit range:** 204–228 (25 commits)  
**Dimensions:** documentation, configuration, CI/CD  
**Status:** ready

## Checklist

Each item = one atomic commit via `scripts/million/commit-one.sh`.

1. [ ] Add Makefile targets: `million-progress`, `million-validate-batch`
2. [ ] Document million-loop quickstart in `scripts/million/README.md`
3. [ ] Add `docs/adr/0001-million-commit-loop.md` ADR
4. [ ] Add `docs/adr/README.md` index
5. [ ] Expand `.env.example` with `VEGA_MILLION_LOOP_ENABLED` flag (documentation only)
6. [ ] Add path-filter note to `.github/workflows/test-all.yml` (comment)
7. [ ] Add `progress/eras/era-0.md` bootstrap checklist
8. [ ] Add campaign tracker stub `progress/campaigns/.gitkeep` + README
9. [ ] Wire `scripts/million/status.sh` output example into scripts README
10. [ ] Add `CONTRIBUTING.md` section pointing agents at million plan
11. [ ] Add schema for progress JSON in `progress/SCHEMA.md`
12. [ ] Add `scripts/million/sync-progress.sh` to refresh counts from git
13. [ ] Add loop dimension rotation table excerpt to era-0 notes
14. [ ] Add validate-campaign.sh stub with campaign gate checklist
15. [ ] Add validate-loop.sh stub
16. [ ] Cross-link README.md to MILLION_COMMIT_PLAN.md
17. [ ] Add CHANGELOG.md entry for million-loop planning
18. [ ] Add RELEASE_NOTES.md “Road to 1M” section
19. [ ] Add `.gitattributes` note/comment for generated progress if needed
20. [ ] Add `scripts/million/claim-batch.sh` to mark batch in_progress
21. [ ] Add `scripts/million/complete-batch.sh` to bump counters
22. [ ] Add epoch template `progress/epochs/TEMPLATE.md`
23. [ ] Add factory catalog shortlist to `progress/FACTORIES.md`
24. [ ] Refresh HANDOFF.md for batch 1
25. [ ] Close batch: sync PROGRESS.json (batch 0 → complete, batch 1 ready)

## Validation

```bash
scripts/million/validate-batch.sh
scripts/million/sync-progress.sh
```
