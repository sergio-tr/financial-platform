package io.sergiotr.financialplatform.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.MembershipMutationDenialReason;
import io.sergiotr.financialplatform.identity.domain.MembershipStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkspaceMembershipLifecycleServiceTest {

    private InMemorySyntheticWorkspaceStore store;
    private WorkspaceMembershipLifecycleService lifecycle;

    @BeforeEach
    void setUp() {
        store = SyntheticWorkspaceFixtures.alphaTeamBaseline();
        var auth = new WorkspaceAuthorizationService(store);
        lifecycle = new WorkspaceMembershipLifecycleService(store, store, auth);
    }

    @Test
    void ownerAddsMemberWithFreshAuthRevision() {
        var dave = UserId.parse("10000000-0000-4000-8000-000000000004");
        var result = lifecycle.addMember(
                SyntheticWorkspaceFixtures.ALICE, 2L, SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, dave, WorkspaceRole.MEMBER);
        assertTrue(result.applied());
        assertEquals(1L, result.membership().orElseThrow().authRevision());
    }

    @Test
    void deniesAddWhenActorAuthRevisionStale() {
        var dave = UserId.parse("10000000-0000-4000-8000-000000000004");
        var result = lifecycle.addMember(
                SyntheticWorkspaceFixtures.ALICE, 1L, SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, dave, WorkspaceRole.MEMBER);
        assertFalse(result.applied());
        assertEquals(AuthorizationDenialReason.STALE_AUTH_CONTEXT, result.authorizationDenial().orElseThrow());
    }

    @Test
    void deniesAddWhenActorAuthRevisionFuture() {
        var dave = UserId.parse("10000000-0000-4000-8000-000000000004");
        var result = lifecycle.addMember(
                SyntheticWorkspaceFixtures.ALICE, 50L, SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, dave, WorkspaceRole.MEMBER);
        assertFalse(result.applied());
        assertEquals(AuthorizationDenialReason.FUTURE_AUTH_CONTEXT, result.authorizationDenial().orElseThrow());
    }

    @Test
    void adminCannotGrantOwnerRole() {
        store.putMembership(
                SyntheticWorkspaceFixtures.CAROL,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.ADMIN,
                MembershipStatus.ACTIVE,
                1L);
        var eve = UserId.parse("10000000-0000-4000-8000-000000000005");
        var result = lifecycle.addMember(
                SyntheticWorkspaceFixtures.CAROL,
                1L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                eve,
                WorkspaceRole.OWNER);
        assertEquals(MembershipMutationDenialReason.PRIVILEGE_ESCALATION, result.mutationDenial().orElseThrow());
    }

    @Test
    void changeRoleIncrementsAuthRevisionOnSuccess() {
        var result = lifecycle.changeRole(
                SyntheticWorkspaceFixtures.ALICE,
                2L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                SyntheticWorkspaceFixtures.BOB,
                WorkspaceRole.MEMBER,
                1L);
        assertTrue(result.applied());
        assertEquals(2L, result.membership().orElseThrow().authRevision());
    }

    @Test
    void deniesRoleChangeOnStaleExpectedRevision() {
        var result = lifecycle.changeRole(
                SyntheticWorkspaceFixtures.ALICE,
                2L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                SyntheticWorkspaceFixtures.BOB,
                WorkspaceRole.MEMBER,
                0L);
        assertEquals(MembershipMutationDenialReason.STALE_WRITE, result.mutationDenial().orElseThrow());
    }

    @Test
    void deniesRevokingLastOwner() {
        var result = lifecycle.revokeMember(
                SyntheticWorkspaceFixtures.ALICE, 2L, SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, SyntheticWorkspaceFixtures.ALICE, 2L);
        assertEquals(MembershipMutationDenialReason.LAST_OWNER_PROTECTED, result.mutationDenial().orElseThrow());
    }

    @Test
    void revokeIncrementsAuthRevision() {
        store.putMembership(
                SyntheticWorkspaceFixtures.CAROL,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.OWNER,
                MembershipStatus.ACTIVE,
                1L);
        var result = lifecycle.revokeMember(
                SyntheticWorkspaceFixtures.ALICE, 2L, SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, SyntheticWorkspaceFixtures.BOB, 1L);
        assertTrue(result.applied());
        assertEquals(MembershipStatus.REMOVED, result.membership().orElseThrow().status());
        assertEquals(2L, result.membership().orElseThrow().authRevision());
    }

    @Test
    void adminCannotRevokeOwnerWhenTwoOwnersExist() {
        store.putMembership(
                SyntheticWorkspaceFixtures.CAROL,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.OWNER,
                MembershipStatus.ACTIVE,
                1L);
        store.putMembership(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.ADMIN,
                MembershipStatus.ACTIVE,
                3L);
        var result = lifecycle.revokeMember(
                SyntheticWorkspaceFixtures.ALICE,
                3L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                SyntheticWorkspaceFixtures.CAROL,
                1L);
        assertEquals(MembershipMutationDenialReason.PRIVILEGE_ESCALATION, result.mutationDenial().orElseThrow());
    }

    @Test
    void adminCannotDemoteOwnerWhenTwoOwnersExist() {
        store.putMembership(
                SyntheticWorkspaceFixtures.CAROL,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.OWNER,
                MembershipStatus.ACTIVE,
                1L);
        store.putMembership(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.ADMIN,
                MembershipStatus.ACTIVE,
                3L);
        var result = lifecycle.changeRole(
                SyntheticWorkspaceFixtures.ALICE,
                3L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                SyntheticWorkspaceFixtures.CAROL,
                WorkspaceRole.MEMBER,
                1L);
        assertEquals(MembershipMutationDenialReason.PRIVILEGE_ESCALATION, result.mutationDenial().orElseThrow());
    }

    @Test
    void authorizationAfterRoleChangeDeniesStaleClientRevision() {
        lifecycle.changeRole(
                SyntheticWorkspaceFixtures.ALICE,
                2L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                SyntheticWorkspaceFixtures.BOB,
                WorkspaceRole.MEMBER,
                1L);
        var auth = new WorkspaceAuthorizationService(store);
        var decision = auth.authorize(
                SyntheticWorkspaceFixtures.BOB,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                io.sergiotr.financialplatform.identity.domain.WorkspaceOperation.VIEW_WORKSPACE,
                1L);
        assertEquals(AuthorizationDenialReason.STALE_AUTH_CONTEXT, decision.denialReason());
    }
}