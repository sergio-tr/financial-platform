# Security Architecture

## Trust boundary
The application is LAN-only but still handles high-sensitivity financial information. LAN access is not considered trusted merely because it is local.

## Network

- No router port forwarding to the platform.
- No DMZ/UPnP exposure.
- Host firewall permits HTTPS/WSS only from local network ranges.
- Only reverse proxy listens on the LAN interface.
- Backend, PostgreSQL, Temporal and Ollama stay host/internal-network only.
- Use HTTPS/WSS with a local trusted CA.

## Authentication/session

Initial design:
- username/email + Argon2id password hash;
- server-side sessions;
- Secure, HttpOnly, SameSite=Strict cookies;
- WebSocket inherits authenticated Principal at handshake;
- authorization is rechecked per message/destination/workspace, not only once at connect.

No JWT is stored in browser localStorage/sessionStorage.

## Workspace authorization

Every command/query that references workspace state verifies membership and required role. Roles begin with OWNER, ADMIN, MEMBER, VIEWER. Cross-workspace access must fail closed.

## Secrets

Application code accesses credentials through `SecretVaultPort` and opaque `SecretRef`. Secrets are encrypted at rest using authenticated encryption; master key material remains outside DB, Git, Cursor and LLM context. Logs and exceptions must never print resolved secrets.

## Data exfiltration policy

Workspace integration policy:
- `LOCAL_ONLY` (default)
- `SUMMARY_EXTERNAL`
- `FULL_EXTERNAL`

Slack/Notion/email publishing cannot receive portfolio-level data unless workspace policy explicitly permits it. The default is no external financial-data export.

## AI data boundary

Local Ollama may receive authorized workspace financial context. Cloud coding agents must not receive production credentials, bank statements, account exports or real portfolio snapshots.

## Audit

Append-only audit events cover authentication, authorization failures, provider configuration, secret rotation, syncs, workflow launches/cancellations, AI tool calls, report exports and settings changes.

## Backups

Backups are encrypted. Retention baseline: 7 daily, 4 weekly, 3 monthly. Restore is periodically tested; a successful dump without a restore test is not considered a proven backup.

## Threats explicitly addressed

- accidental WAN exposure;
- credential leakage into code/logs/LLM context;
- workspace isolation failure;
- WebSocket destination over-permission;
- connector compromise or malformed upstream data;
- replay/duplicate command execution;
- cloud-agent exposure of production data;
- external integration exfiltration;
- unverified backup recovery.
