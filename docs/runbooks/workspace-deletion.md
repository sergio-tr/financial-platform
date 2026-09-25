# Runbook: Workspace Deletion (R47)

Prerequisites:
- Recent authentication of requesting user.

Flow:
1. Fence writes/sync/workflows for the workspace.
2. Issue resumable per-context receipts for deletion steps.
3. Ensure RecoveryControlLedger tombstones to avoid resurrection from backups.
4. Verify purge of RAG/embeddings/caches/derived artifacts tied to the workspace.

Notes: PITR/backups are not rewritten; deletion is forward-only from the platform's control.

