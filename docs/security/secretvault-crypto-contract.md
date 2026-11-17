# SecretVault crypto contract (P0-R43 / SER-106)

Status: **MATERIALIZED** from Linear pack **P0-R43** (`LOCKED SECURITY BASELINE EXTENSION`, 2026-09-25).  
Owner: **SER-106**. Refines **ADR-0019** / **D-019**.  
Namespace on disk paths: **`sergiotr`** (historic `gafi` names in the source pack are superseded in Git by SER-70 routing).

`IMPLEMENTATION_LOCK=ON`. Normative tests remain **PLANNED** in `docs/verification/test-catalog.yaml` until SER-112/SER-106 close.

## Threat boundary (R43-01)

SecretVault protects provider/integration credentials from Git, logs, telemetry, AI context, cross-workspace retrieval, LAN plaintext-read APIs, and over-broad connector credentials. It does not claim safety against a fully compromised WORKER/SecretVault process during legitimate decryption.

## Identity and envelope (R43-04–R43-07)

- `SecretRef`, immutable `SecretVersionId`, `SecretKeyVersion`, status `ACTIVE | ROTATING | REVOKED | KEY_UNAVAILABLE_REAUTH_REQUIRED`.
- **AES-256-GCM** per version; fresh **256-bit DEK**; **96-bit** nonce; **128-bit** tag; DEK wrapped by active **256-bit SecretVault KEK** with separate nonce/AAD domain.
- AAD binds at least: `formatVersion`, `SecretRef`, `SecretVersionId`, `workspaceId`, owner/slot binding, `secretSlotId`.
- CSPRNG for DEKs, nonces, lease tokens; no reuse of business IDs as key material.

## Key separation (R43-08–R43-10)

Independent purposes: SecretVault **KEK**, fingerprint HMAC, backup-repository key, TLS/CA keys, session/MFA material, RecoveryControlLedger signing key.

**KEK storage:** random material on host filesystem **outside Git/DB**, directory `0700`, file `0600`, read-only mount to SecretVault/WORKER only (e.g. `/run/secrets/sergiotr_secretvault_kek`). Not in environment variables, CLI args, or Git.

**GATEWAY** holds no KEK and no plaintext-read capability. Secret-bearing commands use protected local STORE IPC; no plaintext on RabbitMQ, outbox, replay, Temporal, or logs.

## IPC and leases (R43-11–R43-19)

Separate **STORE** (GATEWAY submit, no plaintext return) and **LEASE** (WORKER only) capabilities. Leases require `CredentialLeaseRequest` dimensions matching binding; grant validity **≤ 60s**; execution-bound; no cross-workspace lease.

## Rotation and compromise (R43-20–R43-22)

- Secret version rotation: store new version, validate if supported, switch active, revoke old, audit metadata only.
- **KEK rotation:** new KEK version, rewrap DEKs, verify all active secrets, retire old KEK.
- **KEK compromise:** mark compromised, new KEK, rewrap for continuity, **mandatory upstream credential rotation**; rewrap alone is insufficient.

## Startup, host loss, backup (R43-23–R43-26)

- Missing/unreadable KEK ⇒ `SecretVault = LOCKED`; no auto-generated replacement over existing ciphertext.
- Default host-loss mode: **`REAUTH_ON_KEK_LOSS`**; DB backups do not bundle plaintext KEK.
- Backup repository encryption uses a **separate** key from SecretVault KEK.
- After restore: key inventory check; missing KEK ⇒ `KEY_UNAVAILABLE_REAUTH_REQUIRED`; RecoveryControlLedger tombstones reapplied.

## DB record and versioning (R43-14, R43-29)

`SYSTEM_SECURITY` table stores refs, versions, ciphertext, wrap metadata, KEK version, status, safe audit fields only — no plaintext.

Persist `cryptoFormatVersion`; decrypt allowlist only; unknown format ⇒ `SECRET_CRYPTO_FORMAT_UNSUPPORTED`.

## Mandatory tests (R43-30)

Including: ciphertext/AAD/wrapped-DEK tamper; wrong KEK; GATEWAY cannot read KEK; secrets absent from logs/Rabbit/outbox/replay/Temporal; idempotent STORE; lease bounds; KEK rotation rewrap; compromised KEK external rotation; missing KEK startup; restore without KEK ⇒ reauth; browser persistence scan clean.

## Traceability (R43-31)

Maps to REQ-CON-002, NFR-SEC-003, NFR-SEC-005, NFR-PRIV-001, NFR-REC-003; register TEST-SEC-VAULT-* via P0-R37 when materialized.