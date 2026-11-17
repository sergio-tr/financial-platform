# SecretVault crypto contract (P0-R43 / SER-106)

Status: **FULLY MATERIALIZED** in Git from Linear pack **P0-R43** (`LOCKED SECURITY BASELINE EXTENSION`, 2026-09-25).  
Owner: **SER-106**. Refines **ADR-0019** / **D-019**.  
Namespace on disk paths: **`sergiotr`** (historic `gafi` names in the source pack are superseded in Git by SER-70 routing).

`IMPLEMENTATION_LOCK=ON`. Runtime code MUST NOT ship until implementation unlock; this document is build authority.

## Canonical registry files

| File | Role |
|------|------|
| `secretvault-crypto-format.yaml` | Allowlisted `cryptoFormatVersion`, algorithms, sizes, host/lease policy |
| `secretvault-crypto-record.schema.json` | `SYSTEM_SECURITY` ciphertext row shape |
| `secretvault-normative-tests.yaml` | R43-30 `TEST-SEC-VAULT-*` identities (PLANNED; outside SER-107 catalog of 18) |

## Threat boundary (R43-01)

SecretVault protects provider/integration credentials from Git, logs, telemetry, AI context, cross-workspace retrieval, LAN plaintext-read APIs, and over-broad connector credentials. It does not claim safety against a fully compromised WORKER/SecretVault process during legitimate decryption.

## Process roles (R43-02–R43-03)

- **GATEWAY** (`sergiotr_gateway`): may submit `SECRET_BEARING_COMMAND` STORE requests over protected IPC; never holds KEK; never receives decrypt/lease capability or plaintext responses.
- **WORKER** (`sergiotr_worker`): hosts SecretVault service, KEK mount read, DEK unwrap, scoped LEASE grant, connector handoff.
- **Migrator/backup** roles: may read ciphertext columns for backup/restore; never receive KEK or plaintext secrets.

## Identity and envelope (R43-04–R43-07)

- **`SecretRef`**: opaque stable identifier; never embeds provider plaintext.
- **`SecretVersionId`**: immutable per ciphertext generation; rotation creates a new version row.
- **`SecretKeyVersion` / `kekVersionId`**: identifies which SecretVault KEK wrapped the DEK.
- **Status**: `ACTIVE | ROTATING | REVOKED | KEY_UNAVAILABLE_REAUTH_REQUIRED`.
- **Envelope**: **AES-256-GCM** secret payload; fresh **256-bit DEK** per version; **96-bit** nonce; **128-bit** tag; DEK wrapped by active **256-bit SecretVault KEK** with separate nonce and AAD domain (`dekWrap` in format registry).
- **AAD (secret payload)** binds, in stable order: `cryptoFormatVersion`, `secretRef`, `secretVersionId`, `workspaceId`, `ownerBinding`, `secretSlotId` (see `aadBindingFields` in format YAML). Serialization is deterministic UTF-8 field encoding defined at implementation time; documentation MUST NOT invent ad hoc per-connector variants.
- **CSPRNG** for DEKs, nonces, lease tokens; business IDs are forbidden as key material.

## Key separation (R43-08–R43-10)

Independent purposes: SecretVault **KEK**, fingerprint HMAC, backup-repository key, TLS/CA keys, session/MFA material, RecoveryControlLedger signing key. No purpose reuse.

**KEK storage:** random material on host filesystem **outside Git/DB**, directory `0700`, file `0600`, read-only mount to SecretVault/WORKER only (example path `/run/secrets/sergiotr_secretvault_kek`). Forbidden: environment variables, CLI args, container image layers, Git.

**GATEWAY** holds no KEK and no plaintext-read capability. Secret-bearing commands use protected local STORE IPC; no plaintext on RabbitMQ, outbox, replay, Temporal, or logs.

## IPC and leases (R43-11–R43-19)

- **STORE** capability: submit credential bytes once; idempotent on idempotency key; no plaintext return channel to GATEWAY.
- **LEASE** capability: WORKER-only; `CredentialLeaseRequest` MUST match `workspaceId`, `secretSlotId`, `ownerBinding`, and requested connector capability scope.
- Grant validity **≤ 60s** (`leasePolicy.maxGrantSeconds`); execution-bound; single use where connector requires; **no cross-workspace** lease.
- Lease tokens are CSPRNG; invalid/expired lease fails closed.

## Rotation and compromise (R43-20–R43-22)

- **Secret version rotation:** store new version, validate with provider when supported, switch `ACTIVE`, revoke prior, audit safe metadata only.
- **KEK rotation:** new KEK version, rewrap all active DEKs, verify decrypt, retire old KEK — see `docs/runbooks/secretvault-kek-rotation.md`.
- **KEK compromise:** mark `COMPROMISED`, install new KEK, rewrap for continuity, **mandatory upstream credential rotation** — see `docs/runbooks/secretvault-kek-compromise.md`. Rewrap alone is insufficient.

## Startup, host loss, backup (R43-23–R43-26)

- Missing/unreadable KEK ⇒ `SecretVault = LOCKED`; no auto-generated replacement over existing ciphertext.
- Default host-loss mode: **`REAUTH_ON_KEK_LOSS`**; DB backups do not bundle plaintext KEK.
- Backup repository encryption uses a **separate** key from SecretVault KEK.
- After restore: key inventory check; missing KEK ⇒ `KEY_UNAVAILABLE_REAUTH_REQUIRED`; RecoveryControlLedger tombstones reapplied before workspace readiness.

## DB record and versioning (R43-14, R43-29)

`SYSTEM_SECURITY` stores ciphertext envelopes only — schema: `secretvault-crypto-record.schema.json`. No plaintext columns.

- Allowlisted `cryptoFormatVersion` values live in `secretvault-crypto-format.yaml` (currently `sergiotr-secret-envelope-v1`).
- Decrypt MUST allowlist formats; unknown ⇒ `SECRET_CRYPTO_FORMAT_UNSUPPORTED`.
- Tamper ⇒ `SECRET_TAG_MISMATCH` or `SECRET_AAD_MISMATCH` without leaking offsets.

## Mandatory tests (R43-30)

Normative identities: `docs/security/secretvault-normative-tests.yaml` (`TEST-SEC-VAULT-*`, all **PLANNED**). Covers tamper, wrong KEK, format mismatch, GATEWAY KEK isolation, sink scan, STORE idempotency, lease bounds, KEK rotation, compromise external rotation, missing KEK startup, restore without KEK, browser persistence.

## Traceability (R43-31)

Maps to REQ-CON-002, NFR-SEC-003, NFR-SEC-005, NFR-PRIV-001, NFR-REC-003; implementation evidence via P0-R37 when catalogs merge.