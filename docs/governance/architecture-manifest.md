# Architecture manifest

Status: CANONICAL governance routing through P0-R57 (SER-70).
Scope: documentation and governance synchronization only. Product implementation remains blocked.
`IMPLEMENTATION_LOCK=ON`.

The PR #2 and PR #3 Git snapshot is a `STALE_PRE_FREEZE_SNAPSHOT` wherever it differs from the Linear amendments accepted on 2026-10-12. See `docs/planning/phase0-closure/AUTHORITY_SUPERSESSION_2026-10-12.md`. This manifest does not mark any requirement or normative test IMPLEMENTED or VERIFIED.

## Authorities

- Implementation authority after merge: Git `main`. Until this synchronization merges, current Linear amendments override the stale Git snapshot.
- Planning: Linear Phase-0 packs P0-R26 through P0-R57.
- Requirements: SER-101 is the sole locked REQ/NFR registry. Bodies stay with SER-101.
- Verification: SER-107 is the sole locked normative TEST registry. Entries are PLANNED.
- Visual semantics: SER-108. Physical MEDIUM and COMPACT frames remain pending.
- Active first-party namespace: `sergiotr` / `Sergiotr`.
- Coordination: Slack. Execution agents read this manifest and current Git before historical chat.
- Cursor rules: `.cursor/rules/**`.

## Routed decisions

Locked packs already in the previous cut remain routed: P0-R26, P0-R35, P0-R36, P0-R37, P0-R38, P0-R39, P0-R40, P0-R41, P0-R42, P0-R43, P0-R44, P0-R45, P0-R46 and P0-R47.

Additional locked packs routed by this synchronization:

- P0-R48 — Pull-request orchestration, CI-to-local acceptance and human-promotion contract
- P0-R49 — Test layers, execution lanes and required quality-gate contract
- P0-R50 — Local acceptance environment, preview deployment and manual approval contract
- P0-R51 — Local environment segregation and workstation bootstrap contract
- P0-R52 — Agent execution firewall, Task Packet and zero-assumption contract
- P0-R53 — Minimum Work Unit, Definition of Ready/Done and change-risk classification contract
- P0-R54 — Final implementation roadmap, vertical slices and dependency-gate contract
- P0-R55 — Synthetic test-data, golden-fixture and provider-simulator contract
- P0-R56 — Delivery state machine, acceptance invalidation and production-promotion contract
- P0-R57 — Phase-0 planning freeze, zero-gap audit and implementation-unlock contract

P0-R57 keeps `IMPLEMENTATION_LOCK=ON` until its own later unlock gate. This synchronization does not turn that lock off.

## Accepted namespace facts

Database roles, exactly five:

- `sergiotr_gateway`
- `sergiotr_worker`
- `sergiotr_migrator`
- `sergiotr_backup`
- `sergiotr_db_owner`

STOMP user destinations, exactly three:

- `/user/exchange/amq.direct/sergiotr.v1.control`
- `/user/exchange/amq.direct/sergiotr.v1.events`
- `/user/exchange/amq.direct/sergiotr.v1.artifacts`

There is no fourth replies destination. Client acknowledgment is session flow-control. PostgreSQL is the replay authority. The default Origin host is `sergiotr.home.arpa`.

## Legacy traceability exception

GitHub PR #1 predates P0-R37. It is recorded as `DOC:LEGACY-PR-001` and is not rewritten. No later pull request is aligned unless its primary `SER-*` resolves in this traceability graph.

## Key documents

- `docs/governance/protocol-ownership.md`
- `docs/governance/review-adjudication.md`
- `docs/governance/typed-configuration.md`
- `docs/governance/api-registry.md` (PLANNED entries where the owning contract is not materialized)
- `docs/governance/traceability/traceability.schema.json`
- `docs/governance/traceability/traceability.yaml`
- `docs/governance/traceability/review-adjudication.md`
- `docs/requirements/functional/README.md` (identifier index; bodies owned by SER-101)
- `docs/requirements/nfr/README.md` (identifier index; bodies owned by SER-101)
- `docs/verification/README.md`
- `docs/verification/test-catalog.schema.json`
- `docs/verification/test-catalog.yaml` (18 PLANNED tests)
- `docs/design/README.md`
- `docs/design/design-manifest.schema.json`
- `docs/design/design-manifest.yaml` (two semantic records)
- `design/tokens/` (placeholder only; values owned by SER-51)
- `docs/security/security-architecture.md`
- `docs/security/README-data-classification.md`
- `docs/security/data-classification.schema.json`
- `docs/security/data-classification.yaml` (classes only; field inventory owned by SER-109)
- `docs/security/data-lifecycle.md`
- `docs/security/retention-policies.schema.json`
- `docs/security/retention-policies.yaml` (empty registry; values owned by SER-110)
- `docs/runbooks/workspace-deletion.md`
- `docs/runbooks/provider-disconnect.md`
- `docs/runbooks/data-export.md`
- `docs/architecture/websocket-architecture.md`
- `docs/planning/phase0-closure/AUTHORITY_SUPERSESSION_2026-10-12.md`

## Pending material owned after this synchronization

- Requirement bodies: SER-101
- Traceability validator and richer graph links: SER-97 and SER-102
- Public contract bodies: post-SER-70 contract materialization of the accepted SER-92..95 planning packs. No `contracts/**` stub is created here.
- SecretVault crypto format and key lifecycle: SER-106
- Normative test sources, lanes and fixtures: SER-112 and SER-116
- Task Packet firewall: SER-115
- Acceptance runner: SER-111 and SER-113
- DTCG token values: SER-51
- Physical MEDIUM and COMPACT frames and visual approval: SER-108
- Field-level classification inventory: SER-109
- Retention registry values: SER-110
- Physical table catalog: SER-103
- Post-Git traceability proof: SER-98
- Implementation-unlock audit: SER-20

This manifest routes accepted decisions. It does not invent missing architecture.
