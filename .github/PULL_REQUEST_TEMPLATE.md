## Linear

Link to the Linear issue and status.

## Requirements

- Catalog entries: `docs/requirements/functional/**`, `docs/requirements/nfr/**`
- State remains Phase 0 (LOCKED/PLANNED/PENDING as applicable)

## Architecture

List impacted packs/ADRs (e.g., R26/R41/R42/R43/R46/R47; ADR-0003/0004/0005/0008/0018/0019/0023/0024).

## Use cases

Summarize user flows and structure; note any `<context>.api` additions or changes.

## Contracts

Update `docs/governance/api-registry.md` and list any `contracts/**` additions or changes.

## Design

`docs/design/**` manifest and `design/tokens/**` (R45). Tokens are Git-authoritative; Figma `DES-*` are presentation authority.

## Verification

`docs/verification/test-catalog.{schema.json,yaml}` (R44). Catalog entries should be updated accordingly.

## Tests

List unit/integration/e2e tests added or updated; include product tests when applicable.

## Docs / Runbooks

Add/update manifests, traceability, security, lifecycle and runbooks as applicable.

## Security / Data impact

R41/R43/R46/R47 policies documented. No real data/secrets; no SECRET in exports; deny-by-default sinks documented.

## Migrations

Describe database/data migrations if any, otherwise N/A.

---

Squash body must include:
```
Refs: <ISSUE or ticket ID>
Trace: <relevant requirements (e.g., P0-Rxx) and ADRs (e.g., ADR-00xx)>
```
