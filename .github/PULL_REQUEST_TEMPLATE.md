## Linear

Link to the Linear issue and status.

## Requirements

- Catalog entries: `docs/requirements/functional/**`, `docs/requirements/nfr/**`
- State remains Phase 0 (LOCKED/PLANNED/PENDING as applicable)

## Architecture

List impacted packs/ADRs (e.g., R26/R41/R42/R43/R46/R47; ADR-0003/0004/0005/0008/0018/0019/0023/0024).

## Use cases

Structure only; do not materialize `<context>.api` methods in this PR.

## Contracts

Registry at `docs/governance/api-registry.md` (PENDING rows until SER-92..95). No concrete `contracts/**` added in this PR.

## Design

`docs/design/**` manifest and `design/tokens/**` (R45). Tokens are Git-authoritative; Figma `DES-*` are presentation authority.

## Verification

`docs/verification/test-catalog.{schema.json,yaml}` (R44). Catalog entries may be PENDING (SER-107).

## Tests

No product tests in this PR (documentation/governance only).

## Docs / Runbooks

Add/update manifests, traceability, security, lifecycle and runbooks as applicable.

## Security / Data impact

R41/R43/R46/R47 policies documented. No real data/secrets; no SECRET in exports; deny-by-default sinks documented.

## Migrations

N/A — documentation/governance only.

---

Squash body must include:
```
Refs: SER-70
Trace: P0-R26, P0-R35, P0-R36, P0-R37, P0-R38, P0-R39, P0-R40, P0-R41, P0-R42, P0-R43, P0-R44, P0-R45, P0-R46, P0-R47; ADR-0003, ADR-0004, ADR-0005, ADR-0008, ADR-0018, ADR-0019, ADR-0023, ADR-0024
```
