# Financial Platform — Phase 0 Architecture & Governance Baseline

Status: **Architecture Baseline v1.0**  
Date: **2026-09-23**

This package converts the agreed architecture into repository-ready governance and planning artefacts. It is intentionally implementation-light: Phase 0 exists to make future work deterministic before Angular/Spring feature development begins.

## Authority order

1. Security invariants
2. Accepted ADRs
3. Decision Register
4. AsyncAPI / JSON Schemas
5. Domain and architecture documentation
6. Approved issue specification
7. Approved implementation plan
8. Existing code
9. Agent assumptions

If code conflicts with an accepted ADR, the ADR wins and the inconsistency is architectural debt.

## Main artefacts

- `AGENTS.md` — root agent governance.
- `docs/governance/architecture-manifest.md` — sources of truth and Phase-0 packs.
- `docs/governance/traceability/traceability.{schema.json,yaml}` — R37 graph (locations/relations).
- `docs/governance/review-adjudication.md` and `docs/governance/traceability/review-adjudication.md`.
- `docs/planning/decision-register.md` — canonical list of locked decisions.
- `docs/planning/master-plan.md` — final phased implementation plan.
- `docs/planning/cursor-operating-model.md` — cost-aware Cursor usage policy.
- `docs/architecture/architecture-baseline.md` — system baseline and module boundaries.
- `docs/architecture/websocket-architecture.md` — WSS/STOMP application protocol.
- `docs/connectors/connector-spi.md` — provider connector contracts and capabilities.
- `docs/domain/portfolio-xray.md` — Portfolio X-Ray domain specification.
- `docs/workflows/workflow-architecture.md` — Temporal boundary and workflow semantics.
- `docs/security/security-architecture.md` — LAN-only, secret, auth and data policies (R41/R43/R46/R47 updates).
- `docs/security/README-data-classification.md`, `docs/security/data-classification.*` — R46.
- `docs/security/data-lifecycle.md`, `docs/security/retention-policies.*`, `docs/runbooks/*.md` — R47.
- `docs/ux/dashboard-system.md` — dashboard and responsive UX baseline.
- `docs/verification/**` — R44 verification catalog structure.
- `docs/design/**`, `design/tokens/**` — R45 design manifest and tokens authority.
- `docs/adr/` — accepted Architecture Decision Records.

## Phase 0 rule

No production feature implementation starts until this package has been reviewed, committed and the repository-level Cursor governance has been installed and validated.
