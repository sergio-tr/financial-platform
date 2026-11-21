# Synthetic identity/workspace fixtures (SER-144)

Stable UUIDs for DEV/TEST only. Values mirror `SyntheticWorkspaceFixtures` in `backend/modules/identity`.

| Key | UUID |
|-----|------|
| ALPHA workspace | `00000000-0000-4000-8000-0000000000a1` |
| BETA workspace | `00000000-0000-4000-8000-0000000000b2` |
| Alice (OWNER on ALPHA) | `10000000-0000-4000-8000-000000000001` |
| Bob (VIEWER on ALPHA) | `10000000-0000-4000-8000-000000000002` |
| Carol (ADMIN on BETA) | `10000000-0000-4000-8000-000000000003` |

SER-145 adds synthetic membership lifecycle tests (compare-and-set on `authRevision`); see `WorkspaceMembershipLifecycleService`.

SER-150 (V1-IDENTITY-PASSWORD-ARGON2ID) adds Argon2id hashing and new-password policy in `backend/modules/identity` (synthetic DEV/TEST only). Password verify does not grant `AUTHENTICATED_FULL` and is not wired to HTTP login.

**Review note:** PR #49 (SER-149) merged without human Approval Agent sign-off (`No apruebo`); treat production login enablement as blocked until an independent auth code review completes.

No production data. Not a normative contract artifact.