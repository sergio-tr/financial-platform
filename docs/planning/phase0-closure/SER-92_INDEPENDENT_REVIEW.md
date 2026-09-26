# SER-92 Independent READ-ONLY review — 2026-09-26

Reviewer role: independent contract/security review (not pack author).
Input: SER-92 Contract Pack v1 + Adjudication amendment v1.1 (Linear document de3c80be0a59).
Result: **CHANGES_REQUIRED**

## Finding IDs

### SER92-IR-001 — Permission×resource×purpose matrix incomplete
Severity: BLOCKING compile-readiness
The amendment enumerates PermissionCode, ResourceTypeCode, OperationPurposeCode and ResourceAuthorizationRef, and states that “a permission declares its legal resource types/purposes in the registry,” but the pack does not publish the actual AUTH_REGISTRY_V1 mapping rows (which permissions accept which resource types/purposes/minimum assurance). An implementer would invent mappings.
Required: explicit registry table or machine-readable rows for every PermissionCode.

### SER92-IR-002 — Several view/command records still incomplete
Severity: BLOCKING
Still underspecified for compile-ready Java without invention:
- InvitationAdminView fields/nullability
- TotpEnrollmentMaterial.manualSecret / otpauthUri encoding bounds and redaction rules beyond SECRET label
- FieldError shape referenced by PasswordRejected/ValidationRejected
- SettingChange exact allowed SettingValue variants per SettingId (typed list alone insufficient for unknown settingIds)
- AuditExportId lifecycle view fields for getAuditExport beyond status enum prose
- WorkspaceDeletionRequestView fields
These cannot be guessed from surrounding prose.

### SER92-IR-003 — Host-local SecurityAdmin surface transport applicability unresolved
Severity: BLOCKING for R35-42 completeness
`identity.security-admin.host-recovery.begin:v1` is “host-local only, not exposed as normal LAN business WSS route,” but the pack does not give an exact application-port vs transport applicability entry that an agent can bind without inventing the control-surface contract.

### SER92-IR-004 — Idempotency conflict payload shape
Severity: NONBLOCKING residual / clarify
`IdempotencyConflict` appears as bare outcome token in multiple methods without a shared typed record (conflicting key, stored fingerprint class, safe details). Prefer one sealed shared problem/outcome record referenced by all methods.

## Positive observations (not acceptance)
- SensitiveText wrapper, no AUTHENTICATED without MFA, SYSTEM_ADMIN non-financial, one-time-secret non-replay rule, and exact RemoveWorkspaceMember/ResetWorkspacePreference records materially improve v1.
- No Spring/JPA/Temporal/MCP leakage observed in declared API types.

## Verdict
`CHANGES_REQUIRED` — not yet sufficient to write all framework-neutral compile-ready Java contracts without inventing fields/types/registry rows.
