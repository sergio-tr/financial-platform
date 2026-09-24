# Threat Model Baseline

## Protected assets

- provider credentials/session tokens;
- portfolio positions, balances and transaction history;
- financial reports and AI conversations containing financial context;
- workspace identity/authorization state;
- local encryption master key;
- audit history;
- backup archives.

## Threat actors / failure sources

- unauthenticated device on the LAN;
- compromised browser/device on the LAN;
- malicious or compromised upstream connector/provider response;
- accidental developer/agent data leakage;
- cloud service/external integration exfiltration;
- software dependency vulnerability;
- local malware/user-account compromise;
- operator error causing destructive migration/backup failure.

## Primary mitigations

| Threat | Mitigation baseline |
|---|---|
| WAN exposure | no forwarding/DMZ; firewall LAN allow-list; reverse proxy only |
| LAN snooping | HTTPS/WSS local trusted CA |
| session theft | Secure/HttpOnly/SameSite cookie, server-side session, TLS |
| cross-workspace access | workspace authorization on every command/query/tool |
| secret leakage | SecretVaultPort, redaction, protected paths, no LLM secret context |
| replay/duplicate command | command idempotency keys and connector dedupe |
| malformed provider data | typed mapping/validation and connector warnings/errors |
| data exfiltration to SaaS | explicit LOCAL_ONLY/SUMMARY_EXTERNAL/FULL_EXTERNAL policy |
| coding-agent leakage | synthetic fixtures; protected paths; no production exports in repo |
| lost events | transactional outbox + lossless semantics for critical events |
| backup false confidence | scheduled restore verification |

## Residual risk

A local-only topology does not protect against compromise of the host itself. Host OS patching, disk encryption, local account security and device security remain operational prerequisites outside application code.
