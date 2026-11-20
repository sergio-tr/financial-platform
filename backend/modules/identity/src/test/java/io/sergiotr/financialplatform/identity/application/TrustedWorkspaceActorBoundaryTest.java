package io.sergiotr.financialplatform.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.support.FixedWorkspaceActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrustedWorkspaceActorBoundaryTest {

    private InMemorySyntheticWorkspaceStore store;
    private FixedWorkspaceActorContext actorContext;
    private TrustedWorkspaceActorBoundary boundary;

    @BeforeEach
    void setUp() {
        store = SyntheticWorkspaceFixtures.alphaTeamBaseline();
        var auth = new WorkspaceAuthorizationService(store);
        var lifecycle = new WorkspaceMembershipLifecycleService(store, store, auth);
        actorContext = new FixedWorkspaceActorContext();
        boundary = new TrustedWorkspaceActorBoundary(actorContext, auth, lifecycle);
    }

    @Test
    void authorizesFromTrustedContextNotCallerParameters() {
        actorContext.setActor(
                new TrustedWorkspaceActor(SyntheticWorkspaceFixtures.ALICE, 2L, SecurityAssuranceStatus.AUTHENTICATED_FULL));
        var decision =
                boundary.authorize(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, WorkspaceOperation.VIEW_WORKSPACE);
        assertTrue(decision.permitted());
    }

    @Test
    void deniesAnonymousContext() {
        actorContext.clear();
        var decision =
                boundary.authorize(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, WorkspaceOperation.VIEW_WORKSPACE);
        assertFalse(decision.permitted());
        assertEquals(AuthorizationDenialReason.UNAUTHENTICATED, decision.denialReason());
    }

    @Test
    void deniesPreMfaAssurance() {
        actorContext.setActor(
                new TrustedWorkspaceActor(SyntheticWorkspaceFixtures.ALICE, 2L, SecurityAssuranceStatus.PRE_MFA));
        var decision =
                boundary.authorize(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, WorkspaceOperation.VIEW_WORKSPACE);
        assertEquals(AuthorizationDenialReason.INSUFFICIENT_ASSURANCE, decision.denialReason());
    }

    @Test
    void membershipMutationUsesTrustedActorOnly() {
        actorContext.setActor(
                new TrustedWorkspaceActor(SyntheticWorkspaceFixtures.ALICE, 2L, SecurityAssuranceStatus.AUTHENTICATED_FULL));
        var newcomer = UserId.parse("10000000-0000-4000-8000-000000000099");
        var added = boundary.addMember(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, newcomer, WorkspaceRole.VIEWER);
        assertTrue(added.applied());
    }
}