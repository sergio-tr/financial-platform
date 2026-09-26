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

SecretVault boundary (R43):
- Envelope encryption AES-256-GCM. KEK remains outside PostgreSQL/Git; DEK per secret version.
- `GATEWAY` may accept `SECRET_BEARING_COMMAND` but never obtains reusable decrypt/lease capability.
- Store/replace via local protected IPC to SecretVault on the WORKER side.
- Connectors receive only short-lived, scoped leases. Secret and backup keys live in separate domains.

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

## Database security (R41)
- Data classes: GLOBAL_REFERENCE, WORKSPACE_SENSITIVE, USER_PRIVATE, SYSTEM_SECURITY, SYSTEM_INTERNAL.
- Workspace-sensitive transactions set per-transaction variables: `app.actor_type`, `app.actor_id`, `app.authenticated_user_id`, `app.workspace_id`, `app.security_revision`, `app.trace_id`; never session-global/pool-wide.
- WORKSPACE_SENSITIVE physical tables: `workspace_id` NOT NULL + ENABLE/FORCE RLS with `USING`/`WITH CHECK` policies.
- SYSTEM_SECURITY uses a dedicated policy (not a generic tenant read).
- Runtime roles (e.g., `gafi_gateway`, `gafi_worker`) do not hold superuser/BYPASSRLS/TRUNCATE/schema CREATE/privileged SET ROLE.
- RLS is defense-in-depth after app authorization, not a claim against arbitrary SQL compromise. Zero unclassified physical objects.

## Data classification and sinks (R46)
- Taxonomy: PUBLIC, INTERNAL, PERSONAL, FINANCIAL_SENSITIVE, SECURITY_SENSITIVE, SECRET. Trust/provenance is tracked separately.
- Sink policy is deny-by-default. SECRET never flows through Rabbit/outbox/replay/Temporal/AI/RAG/telemetry — only dedicated secret-bearing transport + SecretVault/protected IPC/scoped lease.
- Real PERSONAL/FINANCIAL/SECURITY/SECRET data never enters Git/GitHub PR/issues, cloud CI/agents, Slack/Linear planning, or Figma.

## Data lifecycle, retention and deletion (R47)
- Provider disconnect revokes credentials and stops sync but preserves imported history unless a separate delete is requested.
- Privacy purge (deletion) is distinct from financial correction or soft delete.
- Workspace deletion requires recent authentication, fences writes/sync/workflows, and uses resumable per-context receipts.
- PITR/backups are not rewritten. RecoveryControlLedger tombstones prevent resurrection until natural expiry.
- Raw provider payload persistence is OFF by default.
- Deleting private sources purges/invalidates RAG chunks/embeddings/caches/derived artifacts.
- SECRET is never exported; SECURITY_SENSITIVE is excluded by default.
- Do not claim deletion of copies already published or downloaded outside the platform.

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
