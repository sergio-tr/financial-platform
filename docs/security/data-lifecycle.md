# Data Lifecycle and Retention (R47)

Status: CANONICAL (policy/runbooks).

Key semantics:
- Provider disconnect revokes credentials and stops sync but preserves imported history unless separately deleted.
- Privacy purge is distinct from financial correction or soft delete.
- Workspace deletion requires recent authentication and fences writes/sync/workflows with resumable receipts.
- PITR/backups are immutable. RecoveryControlLedger tombstones block resurrection until expiry.
- Encrypted backup **recovery-point retention** is registered in `docs/security/retention-policies.yaml` (P0-R30 R30-03 baseline; ratified D1). Operational cadence details remain in Linear P0-R30 until full P0-G materialization.
- Raw provider payload persistence is OFF by default.
- Deleting private sources purges/invalidates RAG chunks/embeddings/caches/derived artifacts.
- SECRET is never exported; SECURITY_SENSITIVE is excluded by default.
- No claim to delete copies already published or downloaded outside the platform.

