# Typed Configuration and Externalization (R38)

Status: CANONICAL

## Model
- Definitions use `SettingDefinition<T>` with explicit type.
- Scopes: INSTANCE_STARTUP, INSTANCE_RUNTIME, WORKSPACE, USER.
- Precedence is permitted only when defined by the `SettingDefinition`.
- Revisions are immutable and carry CAS semantics and audit metadata.
- Secrets are never stored as settings; use `SecretRef` and SecretVault.

## Change flow
1. Authorization → validation
2. Persist immutable revision → audit/outbox publication
3. Handlers/WSS react to changes without polling

## Policies
- Unknown core keys fail fast.
- Financial algorithm parameters are versioned artifacts, not mutable settings.
- Feature flags cannot weaken auth/MFA/RLS/audit/encryption/no-trading/stale indicators.
- Config export/import excludes secrets and supports dry-run/migration.

References: ADR-0019, ADR-0021, ADR-0008, R41, R43.

