# SER-161 — Finance field privacy catalog gaps (non-normative)

Parent: **SER-109** slice **SER-161**. Status: **PARTIAL** — manual import, portfolio, snapshot, and X-Ray derived field families only.

## Materialized in Git (this MWU)

| Artifact | Purpose |
| --- | --- |
| `docs/security/field-privacy-catalog/manual-import-portfolio-snapshot.v1.yaml` | 22 field families (import, provider, account/portfolio, transactions, lots, snapshots, performance/X-Ray, public instrument refs) |
| `scripts/governance/field_privacy_catalog_lib.py` | Shared validator semantics (SER-160 + SER-161) |
| `scripts/governance/validate-field-privacy-catalogs-regression.py` | Regression over Identity + Finance catalogs |
| `scripts/governance/validate-ser-161-field-privacy-catalog.py` | Slice validator + negative harness entrypoint |
| `test/fixtures/governance/ser-161/negative/*` | Eight adversarial fixtures |

Cross-refs: SER-160 identity catalog (sink vocabulary), `data-classification.yaml` (class skeleton unchanged), P0-R24/R25, Linear SER-94 pack authority (records not duplicated in Git).

## Blocked / open matrix

| Gap ID | Scope | Status |
| --- | --- | --- |
| G-FIN-001 | Physical PostgreSQL objects for portfolio/account/transaction/snapshot | OPEN (SER-103; no invented table names) |
| G-FIN-002 | Importer quarantine and normalized row retention duration | OPEN (SER-110 + human ratification) |
| G-COV-FIELD-001 | Finance slice in Git; connectors/AI remainder | **PARTIAL** (this MWU) |
| G-COV-FIELD-002 | Global class owners in `data-classification.yaml` | OPEN (`PENDING (SER-109)`) |
| G-COV-FIELD-003 | Runtime egress enforcement | OUT OF SCOPE (static CI only) |

## Explicit non-goals

- Does **not** mark SER-109, SER-103, or SER-110 Done.
- Does **not** modify product backend/frontend or import runtime.
- No bank integrations, no real financial data, no Liquibase.
- Human auth security review gap (PR #49/#50/#52) unchanged.

## Next recommended MWUs

1. SER-103 ratify G-FIN-001 physical names then align field `postgresql_planned_column` surfaces.
2. SER-109 connectors/AI field inventory (remainder G-COV-FIELD-001).
3. SER-110 importer/quarantine retention policies for G-FIN-002.