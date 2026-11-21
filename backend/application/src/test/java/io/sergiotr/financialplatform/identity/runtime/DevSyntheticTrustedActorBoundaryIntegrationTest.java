package io.sergiotr.financialplatform.identity.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.TrustedWorkspaceActorBoundary;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev-synthetic")
class DevSyntheticTrustedActorBoundaryIntegrationTest {

    @Autowired
    private TrustedWorkspaceActorBoundary boundary;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authorizesWhenSecurityContextCarriesFullAssuranceActor() {
        SecurityContextHolder.getContext()
                .setAuthentication(new SyntheticTrustedActorAuthentication(new TrustedWorkspaceActor(
                        SyntheticWorkspaceFixtures.ALICE, 2L, SecurityAssuranceStatus.AUTHENTICATED_FULL)));

        var decision = boundary.authorize(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, WorkspaceOperation.VIEW_WORKSPACE);
        assertThat(decision.permitted()).isTrue();
    }

    @Test
    void deniesWhenSecurityContextMissing() {
        var decision = boundary.authorize(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, WorkspaceOperation.VIEW_WORKSPACE);
        assertThat(decision.denialReason()).isEqualTo(AuthorizationDenialReason.UNAUTHENTICATED);
    }

    @Test
    void deniesPreMfaActorInSecurityContext() {
        SecurityContextHolder.getContext()
                .setAuthentication(new SyntheticTrustedActorAuthentication(new TrustedWorkspaceActor(
                        SyntheticWorkspaceFixtures.ALICE, 2L, SecurityAssuranceStatus.PRE_MFA)));

        var decision = boundary.authorize(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, WorkspaceOperation.VIEW_WORKSPACE);
        assertThat(decision.denialReason()).isEqualTo(AuthorizationDenialReason.INSUFFICIENT_ASSURANCE);
    }

    @Test
    void addMemberUsesTrustedActorNotForgedIdentity() {
        SecurityContextHolder.getContext()
                .setAuthentication(new SyntheticTrustedActorAuthentication(new TrustedWorkspaceActor(
                        SyntheticWorkspaceFixtures.ALICE, 2L, SecurityAssuranceStatus.AUTHENTICATED_FULL)));
        var newcomer = UserId.parse("10000000-0000-4000-8000-000000000088");
        var result = boundary.addMember(SyntheticWorkspaceFixtures.ALPHA_WORKSPACE, newcomer, WorkspaceRole.MEMBER);
        assertThat(result.applied()).isTrue();
    }
}