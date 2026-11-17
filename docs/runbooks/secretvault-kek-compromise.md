# Runbook: SecretVault KEK compromise

Owner: **SER-106** / **P0-R43**. Treat as security incident; rewrap alone is insufficient.

## Immediate actions

1. Mark active KEK version **COMPROMISED** in key inventory; halt new lease grants.
2. Generate replacement KEK version; install on WORKER per host policy (`0700`/`0600`, read-only mount).
3. Rewrap all active DEKs to continuity-encrypt ciphertext under new KEK (same as normal rotation).
4. **Mandatory upstream rotation:** revoke and re-issue every provider/integration credential whose ciphertext was protected by the compromised KEK window. Track completion per `SecretRef` / workspace.
5. After upstream rotation completes, revoke superseded secret versions where connector supports validation; audit metadata only.

## Host loss overlap

If compromise coincides with missing KEK file, default policy **`REAUTH_ON_KEK_LOSS`** applies: secrets enter `KEY_UNAVAILABLE_REAUTH_REQUIRED` until operators complete reauth and upstream rotation.

## Prohibited

- Publishing KEK material to Slack, Linear, Git, CI logs, or LLM context.
- Bundling plaintext KEK into PostgreSQL backup dumps or repository encryption keys (separate purpose per R43-09).

## Verification

Execute planned checks listed in `docs/security/secretvault-normative-tests.yaml` when implementation unblocks (TEST-SEC-VAULT-KEK-COMPROMISE-EXTERNAL-ROTATION).

## Related

- Normal rotation: `secretvault-kek-rotation.md`
- Contract: `docs/security/secretvault-crypto-contract.md`