package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.MembershipStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.domain.WorkspaceStatus;

/** Stable synthetic UUIDs shared with {@code test/fixtures/synthetic/identity}. */
public final class SyntheticWorkspaceFixtures {

    public static final WorkspaceId ALPHA_WORKSPACE = WorkspaceId.parse("00000000-0000-4000-8000-0000000000a1");
    public static final WorkspaceId BETA_WORKSPACE = WorkspaceId.parse("00000000-0000-4000-8000-0000000000b2");
    public static final UserId ALICE = UserId.parse("10000000-0000-4000-8000-000000000001");
    public static final UserId BOB = UserId.parse("10000000-0000-4000-8000-000000000002");
    public static final UserId CAROL = UserId.parse("10000000-0000-4000-8000-000000000003");

    private SyntheticWorkspaceFixtures() {}

    public static InMemorySyntheticWorkspaceStore alphaTeamBaseline() {
        var store = new InMemorySyntheticWorkspaceStore();
        store.putWorkspace(ALPHA_WORKSPACE, WorkspaceStatus.ACTIVE);
        store.putWorkspace(BETA_WORKSPACE, WorkspaceStatus.ACTIVE);
        store.putMembership(ALICE, ALPHA_WORKSPACE, WorkspaceRole.OWNER, MembershipStatus.ACTIVE, 2L);
        store.putMembership(BOB, ALPHA_WORKSPACE, WorkspaceRole.VIEWER, MembershipStatus.ACTIVE, 1L);
        store.putMembership(CAROL, BETA_WORKSPACE, WorkspaceRole.ADMIN, MembershipStatus.ACTIVE, 1L);
        return store;
    }
}