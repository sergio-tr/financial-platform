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

No production data. Not a normative contract artifact.