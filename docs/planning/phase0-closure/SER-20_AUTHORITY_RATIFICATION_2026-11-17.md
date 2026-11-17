# SER-20 — authority ratification pointer (2026-11-17)

Status: CANONICAL coordination record for decisions registered in SER-20.  
Authority: Sergio Truebano (2026-11-17), superseding ambiguous agent inference from SER-20-D1-D4 recovery.

`IMPLEMENTATION_LOCK=ON`. This note authorizes Git documentation alignment only, not product implementation.

## Ratified decisions

| ID | Decision | Git action |
|----|----------|------------|
| D1 | Backup retention baseline follows **P0-R30 R30-03**: `7 daily`, `4 weekly`, **`6 monthly`** recovery points | Align `docs/security/security-architecture.md` |
| D2 | SecretVault crypto semantics materialize faithfully from **P0-R43** / **SER-106** | Add `docs/security/secretvault-crypto-contract.md`; reconcile summary text in `security-architecture.md` |
| D3 | Investigate **ADR-0030** vs accepted STOMP broker relay (**SER-38**, REQ-REALTIME-002); no assumption of exception | See `SER-20_ADR-0030_STOMP_BROKER_SCOPE.md` |
| D4 | DTCG implementation authority path: **`design/tokens/**`** in Git (not `frontend/design-system/tokens/**`) | Manifest/README already route here; Linear SER-51 issue text is non-authoritative for Git path until reconciled |
| D5 | MEDIUM/COMPACT responsive compositions remain **non-normative** proposals until Figma evidence | `SER-19_RESPONSIVE_COMPOSITION_PROPOSALS.md` (no manifest node IDs) |

## Evidence base

Git `main` @ materialization merge base: `a4e8d72f707c2ffc805ab35e0589ac6f939c54a8`.