# SER-160 — Identity field privacy catalog gaps (non-normative)

Parent: **SER-109** slice **SER-160**. Status: **PARTIAL** — Identity/Workspace/Session/MFA field families only.

## Materialized in Git (this MWU)

| Artifact | Purpose |
| --- | --- |
| `docs/security/field-privacy-catalog/identity-workspace-session-mfa.v1.yaml` | Field-level R46 class, sink allow/deny, retention cross-refs |
| `docs/security/field-privacy-catalog.schema.json` | JSON Schema |
| `scripts/governance/validate-ser-160-field-privacy-catalog.py` | Executable validator |
| `test/fixtures/governance/ser-160/negative/*` | Adversarial negative fixtures |

Cross-refs: `docs/security/postgres-physical-catalog/identity-workspace-session.v1.yaml` (SER-159), `docs/security/data-classification.yaml` (class skeleton).

## Blocked / open matrix

| Gap ID | Scope | Status |
| --- | --- | --- |
| G-IDW-001..008 | Physical PostgreSQL columns for identity/workspace/MFA/audit | OPEN (SER-159) |
| G-COV-FIELD-001 | Finance, connectors, portfolio, AI context field inventories | OPEN (remainder SER-109) |
| G-COV-FIELD-002 | Global class owners in `data-classification.yaml` | OPEN (`PENDING (SER-109)`) |
| G-COV-FIELD-003 | Runtime egress enforcement | OUT OF SCOPE (static CI validation only) |

## Explicit non-goals

- Does **not** mark SER-109 or SER-103 Done.
- Does **not** change class-level `owner: PENDING (SER-109)` in `data-classification.yaml`.
- No Liquibase, no runtime sink enforcement, no product code.

## Next recommended MWUs

1. SER-109 remainder contexts (finance, connectors) field catalogs with same validator pattern.
2. Ratify G-IDW physical names (SER-103) then align field `postgresql_planned_column` surfaces.
3. Human security review closure for auth PRs #49/#50/#52 before production login claims.