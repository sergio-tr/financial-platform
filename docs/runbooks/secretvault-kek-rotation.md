# Runbook: SecretVault KEK rotation

Owner: **SER-106** / **P0-R43**. Operational steps only; no product autoexecution.

## Preconditions

- Human operator with LOCAL privileged access on WORKER host.
- Maintenance window recorded in audit (metadata only).
- `IMPLEMENTATION_LOCK=ON` — this runbook documents procedure; runtime automation ships post-unlock.

## Procedure (normal rotation)

1. Generate new **256-bit** SecretVault KEK version; write to new host file (`0600`) under `0700` directory; never commit to Git or copy into DB backup artifacts.
2. Mark new KEK version **ACTIVE** in key inventory; prior version **RETIRING**.
3. For each `ACTIVE` secret version row in `SYSTEM_SECURITY`, rewrap DEK with new KEK (new wrap nonce; wrap AAD domain unchanged).
4. Verify decrypt sample: at least one secret per workspace class plus any `ROTATING` versions.
5. Mark old KEK **RETIRED** after all DEKs rewrapped and verified.
6. Emit audit events: `kek_rotation_started`, `kek_rotation_completed` (no key material).

## Failure handling

- If rewrap fails mid-flight, leave SecretVault **LOCKED** for new STORE/LEASE until inventory consistent; do not delete old KEK until verification passes.
- Rollback is **restore previous KEK file from secure offline break-glass** only if new KEK never served traffic; otherwise complete forward rewrap.

## Related

- Compromise path: `secretvault-kek-compromise.md`
- Contract: `docs/security/secretvault-crypto-contract.md`