package io.sergiotr.financialplatform.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.MembershipStatus;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.domain.WorkspaceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkspaceAuthorizationServiceTest {

    private InMemorySyntheticWorkspaceStore store;
    private WorkspaceAuthorizationService service;

    @BeforeEach
    void setUp() {
        store = SyntheticWorkspaceFixtures.alphaTeamBaseline();
        service = new WorkspaceAuthorizationService(store);
    }

    @Test
    void permitsOwnerForDeletionRequest() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.REQUEST_WORKSPACE_DELETION,
                2L);
        assertTrue(decision.permitted());
    }

    @Test
    void deniesViewerHorizontalEscalationToAdminOperation() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.BOB,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.MANAGE_MEMBERS,
                1L);
        assertFalse(decision.permitted());
        assertEquals(AuthorizationDenialReason.INSUFFICIENT_ROLE, decision.denialReason());
    }

    @Test
    void deniesForeignWorkspaceWhenSubjectHasNoMembership() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.BOB,
                SyntheticWorkspaceFixtures.BETA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                1L);
        assertFalse(decision.permitted());
        assertEquals(AuthorizationDenialReason.FOREIGN_WORKSPACE, decision.denialReason());
    }

    @Test
    void deniesUnknownWorkspace() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                WorkspaceId.parse("99999999-9999-4999-8999-999999999999"),
                WorkspaceOperation.VIEW_WORKSPACE,
                0L);
        assertEquals(AuthorizationDenialReason.UNKNOWN_WORKSPACE, decision.denialReason());
    }

    @Test
    void deniesRevokedMembership() {
        store.putMembership(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceRole.OWNER,
                MembershipStatus.REMOVED,
                3L);
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                3L);
        assertEquals(AuthorizationDenialReason.MEMBERSHIP_REVOKED, decision.denialReason());
    }

    @Test
    void deniesStaleClientAuthRevision() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                1L);
        assertEquals(AuthorizationDenialReason.STALE_AUTH_CONTEXT, decision.denialReason());
    }

    @Test
    void deniesFutureForgedClientAuthRevision() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                99L);
        assertEquals(AuthorizationDenialReason.FUTURE_AUTH_CONTEXT, decision.denialReason());
    }

    @Test
    void permitsExactClientAuthRevision() {
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                2L);
        assertTrue(decision.permitted());
    }

    @Test
    void deniesWhenWorkspaceNotAccessible() {
        store.putWorkspace(SyntheticWorkspaceFixtures.BETA_WORKSPACE, WorkspaceStatus.DELETED);
        var decision = service.authorize(
                SyntheticWorkspaceFixtures.CAROL,
                SyntheticWorkspaceFixtures.BETA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                1L);
        assertEquals(AuthorizationDenialReason.WORKSPACE_NOT_ACCESSIBLE, decision.denialReason());
    }
}