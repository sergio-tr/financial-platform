# Architecture Manifest

Status: CANONICAL — Phase 0 sync cut P0-R26..P0-R47 (SER-70)  
Scope: Documentation/governance synchronization only; product implementation remains blocked.

## Authorities
- Implementation authority (current): Git `main` (may lag planning until this PR merges)
- Planning: Linear (Phase-0 packs P0-R26..P0-R47); statuses and owners
- Visual: Figma nodes by stable `DES-*` ids (composition/presentation authority)
- Coordination/Review: Slack (this thread)
- Execution/Review agents: Cursor (rules under `.cursor/rules/**`)

## Canonical decisions (Phase-0 packs)
Locked packs included in this sync cut:
- R26 Cross-system protocol ownership/versioning/compatibility
- R35 Public API registry
- R36 Review adjudication
- R37 Traceability graph (IDs and relationships)
- R38 Typed configuration and externalization
- R39 Traceability review adjudication
- R40 Requirements catalog structure (functional, NFR)
- R41 Database security (RLS classes/policies)
- R42 Realtime transport/destinations/limits/replay
- R43 SecretVault boundary and envelope crypto
- R44 Verification evidence catalog
- R45 Design-to-code manifest and tokens authority
- R46 Data classification and sink allowlists (deny-by-default)
- R47 Lifecycle/retention/unlink/deletion/export/backups

## Key documents added/updated in this PR
- Governance:
  - docs/governance/protocol-ownership.md
  - docs/governance/review-adjudication.md
  - docs/governance/typed-configuration.md
  - docs/governance/api-registry.md (PENDING entries where not yet owned)
  - docs/governance/traceability/traceability.schema.json
  - docs/governance/traceability/traceability.yaml
  - docs/governance/traceability/review-adjudication.md
- Requirements:
  - docs/requirements/functional/README.md
  - docs/requirements/functional/TEMPLATE.md
  - docs/requirements/nfr/README.md
  - docs/requirements/nfr/TEMPLATE.md
- Verification (R44):
  - docs/verification/README.md
  - docs/verification/test-catalog.schema.json
  - docs/verification/test-catalog.yaml (PENDING owner SER-107)
- Design (R45):
  - docs/design/README.md
  - docs/design/design-manifest.schema.json
  - docs/design/design-manifest.yaml (PENDING owners SER-108/SER-51)
  - design/tokens/ (Git tokens are authoritative; Figma mirrors)
- Security (R41/R43/R46/R47):
  - docs/security/security-architecture.md (updated references/policies)
  - docs/security/README-data-classification.md
  - docs/security/data-classification.schema.json
  - docs/security/data-classification.yaml (PENDING inventory SER-109)
  - docs/security/data-lifecycle.md
  - docs/security/retention-policies.schema.json
  - docs/security/retention-policies.yaml (PENDING catalog SER-110)
  - docs/runbooks/workspace-deletion.md
  - docs/runbooks/provider-disconnect.md
  - docs/runbooks/data-export.md
- Realtime/UX/Structure:
  - docs/architecture/websocket-architecture.md (R42 additions)
  - docs/ux/dashboard-system.md (R45 authority notes)
  - docs/architecture/repository-structure.md (new paths)
  - README.md (top-level artifacts)
  - docs/planning/phase-0-acceptance-checklist.md (new checks)

## PENDING material (owned by later MWUs)
- Public API records per context and exact contracts: SER-92..SER-95
- Secret/key lifecycle: SER-106; secret submission/leases: SER-56
- Verification catalog population: SER-107
- Design manifest/tokens population: SER-108/SER-51
- Data-classification concrete inventory: SER-109
- Retention registry population: SER-110
- Physical DB catalog and RLS mappings: SER-103

This manifest declares locations and locked semantics; it does not invent missing architecture.

