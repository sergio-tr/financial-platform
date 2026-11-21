# PostgreSQL physical security catalog (SER-103)

Versioned, machine-readable **planned** physical objects: security class, ownership columns, RLS intent, and runtime DB role privileges (P0-R41).

| File | Role |
| --- | --- |
| `identity-workspace-session.v1.yaml` | SER-159 first slice (identity / workspace / sessions) |
| `../postgres-physical-catalog.schema.json` | JSON Schema for catalog documents |

CI: `scripts/governance/validate-ser-159-physical-catalog.py` and `run-ser-159-catalog-fixtures.py`.

Phase 0 materialization only — not deployed DDL.