## Linear

Primary: SER-NNN

## Requirements

Catalog index: `docs/requirements/functional/README.md` and `docs/requirements/nfr/README.md`.
Phase 0 identifiers stay LOCKED definitions. Do not mark them IMPLEMENTED or VERIFIED in this phase.

## Architecture

List impacted packs. The current governance cut is P0-R26 through P0-R57, including the namespace amendments to P0-R40, P0-R41 and P0-R42, plus SER-101, SER-107 and SER-108 when those registries change.

## Use cases

Name canonical use-case identifiers, or `N/A — DOC_GOVERNANCE_ONLY` for a documentation synchronization.

## Contracts

Registry pointer: `docs/governance/api-registry.md`. Concrete `contracts/**` bodies stay pending until the owning contract materialization. Do not add placeholder schemas.

## Design

`docs/design/design-manifest.yaml` records semantic DES identities. DTCG token values stay owned by SER-51. Do not invent frame identifiers.

## Tests

Normative identities live in `docs/verification/test-catalog.yaml` and remain PLANNED until a source and a passing run exist.

## Docs / Runbooks

List governance, security and runbook paths changed by the pull request.

## Security / Data impact

State the classification, retention, secret-boundary and realtime impact even when the change is documentation only. Real personal, financial, security and secret data does not enter Git, pull requests, cloud CI, Slack, Linear or Figma.

## Migrations

N/A — documentation/governance only, unless this pull request contains a migration.

---

Squash body must include:

```
Refs: SER-70
Trace: P0-R26, P0-R35, P0-R36, P0-R37, P0-R38, P0-R39, P0-R40, P0-R41, P0-R42, P0-R43, P0-R44, P0-R45, P0-R46, P0-R47, P0-R48, P0-R49, P0-R50, P0-R51, P0-R52, P0-R53, P0-R54, P0-R55, P0-R56, P0-R57, SER-101, SER-107, SER-108
```

GitHub PR #1 remains the legacy traceability exception and is not rewritten.
