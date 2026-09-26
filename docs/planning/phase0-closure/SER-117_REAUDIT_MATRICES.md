# SER-35 / SER-117 re-audit — dry-run matrices — 2026-09-26

Mode: planning DRY_RUN only. IMPLEMENTATION_LOCK=ON. No product mutation. No fabricated Git paths as IMPLEMENTED.

## Authority uniqueness (planning-side)
| Check | Result |
|-------|--------|
| Prior F-117-01 P0-R27 duplicate | FIXED per SER-117 report (not re-broken in this run) |
| Prior F-117-02 P0-R28 duplicate | FIXED per SER-117 report |
| Active first-party namespace uniqueness `sergiotr` | **FAIL** — ACTIVE_INVALID `gafi`/`Gafi` in I19 + SER-108 evidence docs |
| Duplicate stable contract pack authorities SER-92..95 | single document each observed; amendments appended to same docs (OK) |

## Dependency cycle
Sequence under test: planning freeze → SER-114 → SER-70 → materialization → SER-98 → SER-20
Result: **NO_CYCLE** in current SER-117 sequencing (F-117-03 FIXED). Note: premature SER-70 PR #2 attachment exists but SER-70 issue is Backlog and sequencing forbids execution before freeze+SER-114 — treated as non-authoritative premature attempt, not a planning cycle.

## Representative TaskPacket semantic matrix
| Slice | Semantic self-sufficiency for DRY_RUN | Result |
|-------|----------------------------------------|--------|
| identity | SER-92 candidate + gaps SER92-IR-* | **FAIL_CLOSED** (missing registry/fields) |
| realtime | R42 + SER-104/105; envelope schemas incomplete; SER-58 blocked | **FAIL_CLOSED** |
| manual import | SER-94 facts incomplete (ellipsis) | **FAIL_CLOSED** |
| financial calculation | FIN_DECIMAL_V1 present; policy/golden registries missing | **FAIL_CLOSED** |
| X-Ray | typed outputs improved; golden/policy versions missing | **FAIL_CLOSED** |
| connector | SER-93 fact/config ellipsis | **FAIL_CLOSED** |
| Temporal/workflow | SER-95 input schemas undefined | **FAIL_CLOSED** |
| AI | tool/result schema registries missing; no-trading rule present | **FAIL_CLOSED** |
| frontend | SER-36 spike pending evidence; DES MEDIUM/COMPACT blocked; state arch not adjudicated | **FAIL_CLOSED** |
| lifecycle | deletion preview/confirm records incomplete | **FAIL_CLOSED** |
| migration | no concrete migration MWU packet | **FAIL_CLOSED** |

Rule applied: missing structural/security/finance/contract/design fact ⇒ BLOCKED_DECISION / FAIL_CLOSED, no invention.

## Adversarial dry-runs (SER-117)
| Case | Expected | Observed |
|------|----------|----------|
| duplicate authority | fail closed | **FAIL_CLOSED** — would reject second active P0-R* with same ID (policy validated against F-117-01 fix pattern) |
| superseded authority | fail closed | **FAIL_CLOSED** — superseded docs cannot resolve as active |
| missing finance semantics | fail closed | **FAIL_CLOSED** — SER94-IR gaps trigger BLOCKED_DECISION |
| missing RLS/security | fail closed | **FAIL_CLOSED** — R41 exists at architecture level but physical table catalog SER-103 PENDING; packet without RLS binding rejected |
| missing contract | fail closed | **FAIL_CLOSED** — SER-92..95 CHANGES_REQUIRED / incomplete facts |
| widened allowedPaths | fail closed | **FAIL_CLOSED** — DRY_RUN packets cannot widen beyond declared ownerContext/proposedPaths; no product write performed |
| weaken-test request | fail closed | **FAIL_CLOSED** — normative TEST cannot be weakened by prompt; catalog remains PLANNED |
| missing DES | fail closed | **FAIL_CLOSED** — user-facing frontend packet without LOCKED DES/MEDIUM+COMPACT rejected |
| AI/provider trading or data-egress bypass | fail closed | **FAIL_CLOSED** — FORBIDDEN execution tools / deny-by-default egress; no mutation |

All invalid cases fail closed **without product/repository mutation**.

## Checkpoint
P0_PLANNING_FROZEN=NO
