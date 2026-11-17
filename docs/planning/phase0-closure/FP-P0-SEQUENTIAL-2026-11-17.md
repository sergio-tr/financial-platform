# FP-P0-SEQUENTIAL-2026-11-17

Status: ACTIVE queue plan (coordination only). `IMPLEMENTATION_LOCK=ON`.

Parent: SER-20. Repo: `sergio-tr/financial-platform`.

## Merge anchor

- PR #18 merged → `b53d3ee4f839545e64b3ba4d6e1f7c14ca154187` (D1 alignment + pointers; **not** repeated by later MWUs).

## Steps

| Step | Ticket | State | Notes |
|------|--------|-------|-------|
| Q1 | SER-110 | `RUNNING_ONE_MWU` | Backup recovery retention registry (P0-R30 R30-03) only |
| Q2 | SER-51 | `QUEUED` | `design/tokens/**` |
| Q3 | SER-106 | `QUEUED` | Full P0-R43 materialization |
| Q4 | SER-112 | `QUEUED` | ADR-0030 enmienda propuesta |
| Q5 | SER-19 | `QUEUED` | Composiciones non-normative |

## Resume

After each PR merge: re-fetch `origin/main`, confirm `merge_commit_sha`, then next MWU in this thread. GitHub PR subscription on the open MWU PR (`cursor-subscriptions`).