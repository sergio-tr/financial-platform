# Traceability Review Adjudication (R39 / SER-97 Stage A)

Status: CANONICAL

Purpose: govern adjudication of the R37/SER-96 trace graph and record forward-only migrations.

Process:
1. Identify candidate edges and nodes to add/change.
2. Validate authority (requirements, ADRs, planning packs, manifests).
3. Check for duplicates, superseded IDs, and dangling references.
4. Decide: ACCEPTED / REVISE / REJECT with rationale.
5. Update `traceability.yaml` and re-run validation.

This process does not invent missing requirements or contracts; it records relationships and locations only.

## SER-97 Stage A — forward-only graph migration

Authority: SER-96 Contract Pack (`d3d6bb51-3c7b-470d-bede-2b7eb3c91ac9`), P0-R37, P0-R39, human adjudication 2026-10-30.

### Applied canonical mappings

| Legacy | Canonical | Notes |
|---|---|---|
| NodeType `SER` | `LINEAR_ISSUE` | statuses remapped to PLANNED/ACTIVE/DONE/CANCELED |
| NodeType `P0-R` | `DECISION_PACK` | |
| NodeType `REQ` | `REQUIREMENT` | |
| NodeType `DES` | `DESIGN` | `PENDING` → `DRAFT` |
| NodeType `DOC` | `DOCUMENTATION` | status `LOCKED`/`PLANNED` → `ACTIVE`; ids keep `DOC:<path>` |
| relation `tests` | `VERIFIES` | direction unchanged |
| relation `documents` (owner → DOC) | `DOCUMENTS` (DOCUMENTATION → owner) | direction reversed per SER-96 |
| relation `implements` (SER → P0-R) | `TRACKED_BY` (DECISION_PACK → LINEAR_ISSUE) | direction reversed |
| relation `references` (DES → REQ/NFR) | `DESIGNS` | |
| `DOC:LEGACY-PR-001` | `LEGACY_EXCEPTION` `LEGACY-PR-001` `ACCEPTED_EXCEPTION` | sole LEGACY_EXCEPTION |
| `SER-111` | `LINEAR_ISSUE` status `CANCELED` | title retains `CANCELED_SUPERSEDED by P0-R59` |

### Adjudication A — DROP_NON_CANONICAL_GRAPH_EDGE (cross-type SUPERSEDES)

Removed because SER-96 allows `SUPERSEDES` only for same `NodeType`:

| from | to | legacy relation |
|---|---|---|
| REQ-REALTIME-002 | NFR-REL-002 | supersedes |
| REQ-REALTIME-001 | NFR-REL-004 | supersedes |
| P0-R59 | SER-111 | supersedes |

`NFR-REL-002` and `NFR-REL-004` remain SUPERSEDED alias nodes from SER-101. Alias mapping stays in SER-101 requirement artifacts; absence of a cross-type graph edge does not rewrite that documentary history.

SER-111 cancellation authority remains in P0-R59, `docs/governance/execution-baseline.md`, and `docs/planning/phase0-closure/AUTHORITY_SUPERSESSION_2026-10-30.md`. No `SUPERSEDES` edge encodes that causal link.

### Adjudication B — DROP_NON_CANONICAL (`references`)

Removed because SER-96 defines no authorized from/to pair for these legacy `references` edges. No replacement relation was applied (not CONSTRAINS, DERIVES_FROM, DOCUMENTS, TRACKED_BY, or any approximation). Source and target nodes remain.

| from | to |
|---|---|
| P0-R26 | ADR-0003 |
| P0-R26 | ADR-0004 |
| P0-R26 | ADR-0005 |
| P0-R41 | ADR-0008 |
| P0-R41 | ADR-0018 |
| P0-R43 | ADR-0019 |
| P0-R59 | ADR-0023 |
| P0-R59 | P0-R57 |
| P0-R59 | P0-R52 |
| P0-R59 | P0-R48 |
| P0-R59 | P0-R54 |

Count: 7 P0-R→ADR + 4 P0-R→P0-R = 11.

Historical knowledge is not rewritten; only non-canonical graph connectivity was dropped.

### Additional DROP (LEGACY_EXCEPTION target)

| from | to | reason |
|---|---|---|
| P0-R37 | DOC:LEGACY-PR-001 (legacy id) | `DOCUMENTS` cannot target `LEGACY_EXCEPTION` per SER-96 matrix |

### Adjudication C — OPEN metadata families

NodeTypes `SCHEMA`, `MESSAGE`, `ROUTE`, `DOCUMENTATION`, `RELEASE_CONTROL`, `LEGACY_EXCEPTION`, `MIGRATION`, `WORKFLOW_DEFINITION`, `SOURCE_OWNER` exist in the taxonomy where SER-96 defines them. Stage A does not invent family-specific required metadata schemas, placeholder instances, owners, or paths beyond what already existed as `DOC:<path>` documentation ids / the single authorized LEGACY-PR-001 exception.

### Validator profiles (implementation detail — not graph statuses)

- `foundation` (Stage A CI default): structural SER-96 grammar, relation matrix, duplicate/orphan/superseded checks, SER-101 invariants.
- `full` (Stage B after SER-102): coverage cardinalities that need UseCase/SOURCE_OWNER mappings.

`DEFINED_BUT_NOT_ACTIVATED` is not a Node status. `PENDING_AUTHORITY` is not a graph status.
