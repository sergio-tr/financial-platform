# SER-159 — Identity/Workspace physical catalog gaps (non-normative)

Parent: **SER-103** slice **SER-159**. Status: **PARTIAL** — session tables only are `NORMATIVE_PLANNED`.

## Materialized in Git (this MWU)

| Object | Authority | Notes |
| --- | --- | --- |
| `platform_session.spring_session` | P0-R21 R21-02, SER-40 | Spring Session JDBC; `SYSTEM_INTERNAL` |
| `platform_session.spring_session_attributes` | P0-R21 R21-02, SER-40 | JSONB allowlist attributes |

Validator: `scripts/governance/validate-ser-159-physical-catalog.py`  
Catalog: `docs/security/postgres-physical-catalog/identity-workspace-session.v1.yaml`

## Blocked gaps (human ratification required)

| Gap ID | Conceptual entity | Why blocked |
| --- | --- | --- |
| G-IDW-001 | User password verifier | No frozen table/columns for Argon2id storage in accepted Git packs |
| G-IDW-002 | User security revision | No physical authRevision registry name |
| G-IDW-003 | MFA authenticator + TOTP seed ref | Persistence + CAS (SER-24 I20) not table-named |
| G-IDW-004 | MFA challenge / pending enrollment | Synthetic Java only on main |
| G-IDW-005 | Recovery code verifier | Deferred in SER-24 I16 |
| G-IDW-006 | Workspace aggregate | Schema `workspace` locked (O-02) but table name absent |
| G-IDW-007 | Workspace membership | RLS policy text + table name not frozen |
| G-IDW-008 | Authentication audit store | Audit API without PostgreSQL object names |

## Explicit non-goals

- Does **not** mark SER-103 or SER-109 Done.
- Does **not** override `docs/security/data-classification.yaml` (`owner: PENDING (SER-109)`).
- No Liquibase, no live RLS install, no product Identity persistence code.

## Next recommended MWUs

1. Ratify identity `USER_PRIVATE` / `SYSTEM_SECURITY` table names (Linear/Git ADR) then extend catalog.
2. SER-109 field-level inventory without contradicting this slice.
3. Remaining SER-103 bounded contexts (connectivity, portfolio, …) as separate slices.