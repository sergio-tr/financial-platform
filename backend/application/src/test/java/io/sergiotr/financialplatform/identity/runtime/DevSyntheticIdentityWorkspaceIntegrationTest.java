package io.sergiotr.financialplatform.identity.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.WorkspaceAuthorizationService;
import io.sergiotr.financialplatform.identity.application.WorkspaceMembershipLifecycleService;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev-synthetic")
class DevSyntheticIdentityWorkspaceIntegrationTest {

    @Autowired
    private WorkspaceAuthorizationService authorizationService;

    @Autowired
    private WorkspaceMembershipLifecycleService membershipLifecycleService;

    @Test
    void springWiresAuthorizationWithFixtureBaseline() {
        var decision = authorizationService.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                2L);
        assertThat(decision.permitted()).isTrue();
    }

    @Test
    void deniesStaleAndFutureAuthRevisionInSpringContext() {
        var stale = authorizationService.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                1L);
        assertThat(stale.denialReason()).isEqualTo(AuthorizationDenialReason.STALE_AUTH_CONTEXT);

        var future = authorizationService.authorize(
                SyntheticWorkspaceFixtures.ALICE,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                99L);
        assertThat(future.denialReason()).isEqualTo(AuthorizationDenialReason.FUTURE_AUTH_CONTEXT);
    }

    @Test
    void deniesForeignWorkspaceViaSpringWiring() {
        var decision = authorizationService.authorize(
                SyntheticWorkspaceFixtures.BOB,
                SyntheticWorkspaceFixtures.BETA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                1L);
        assertThat(decision.denialReason()).isEqualTo(AuthorizationDenialReason.FOREIGN_WORKSPACE);
    }

    @Test
    void membershipLifecycleRunsThroughSpringBeans() {
        var newcomer = UserId.parse("10000000-0000-4000-8000-000000000099");
        var added = membershipLifecycleService.addMember(
                SyntheticWorkspaceFixtures.ALICE,
                2L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                newcomer,
                WorkspaceRole.VIEWER);
        assertThat(added.applied()).isTrue();
        long newcomerRevision = added.membership().orElseThrow().authRevision();

        var allowed = authorizationService.authorize(
                newcomer,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                newcomerRevision);
        assertThat(allowed.permitted()).isTrue();
    }

    @Test
    void revokedMembershipDeniedAfterLifecycleRevoke() {
        var revoked = membershipLifecycleService.revokeMember(
                SyntheticWorkspaceFixtures.ALICE,
                2L,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                SyntheticWorkspaceFixtures.BOB,
                1L);
        assertThat(revoked.applied()).isTrue();
        long bobRevision = revoked.membership().orElseThrow().authRevision();

        var decision = authorizationService.authorize(
                SyntheticWorkspaceFixtures.BOB,
                SyntheticWorkspaceFixtures.ALPHA_WORKSPACE,
                WorkspaceOperation.VIEW_WORKSPACE,
                bobRevision);
        assertThat(decision.denialReason()).isEqualTo(AuthorizationDenialReason.MEMBERSHIP_REVOKED);
    }
}