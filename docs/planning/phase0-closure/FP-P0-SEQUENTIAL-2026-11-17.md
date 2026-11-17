# FP-P0-SEQUENTIAL-2026-11-17

Status: ACTIVE queue plan (coordination only). `IMPLEMENTATION_LOCK=ON`.

Parent: SER-20. Repo: `sergio-tr/financial-platform`.

## Merge anchor

- PR #18 merged → `b53d3ee4f839545e64b3ba4d6e1f7c14ca154187` (D1 alignment + pointers; **not** repeated by later MWUs).
- PR #19 merged → `6e19595d24ff452c4093622d1511faa94f6df04a` (Q1 SER-110 retention registry).

## Steps

| Step | Ticket | State | Notes |
|------|--------|-------|-------|
| Q1 | SER-110 | `MERGE_CONFIRMED` | `retention-policies.yaml` P0-R30 R30-03 |
| Q2 | SER-51 | `RUNNING_ONE_MWU` | `design/tokens/**` DTCG foundation v1 |
| Q3 | SER-106 | `QUEUED` | Full P0-R43 materialization |
| Q4 | SER-112 | `QUEUED` | ADR-0030 enmienda propuesta |
| Q5 | SER-19 | `QUEUED` | Composiciones non-normative |

## Resume

After each PR merge: re-fetch `origin/main`, confirm `merge_commit_sha`, then next MWU in this thread. GitHub PR subscription on the open MWU PR (`cursor-subscriptions`).