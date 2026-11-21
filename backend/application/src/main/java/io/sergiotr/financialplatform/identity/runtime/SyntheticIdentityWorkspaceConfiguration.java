package io.sergiotr.financialplatform.identity.runtime;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.TrustedWorkspaceActorBoundary;
import io.sergiotr.financialplatform.identity.application.WorkspaceAuthorizationService;
import io.sergiotr.financialplatform.identity.application.WorkspaceMembershipLifecycleService;
import io.sergiotr.financialplatform.identity.port.WorkspaceActorContextPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring for in-memory synthetic Identity fixtures. Active only with exclusive {@code dev-synthetic} profile
 * and {@code financial-platform.identity.synthetic.enabled=true}; never registered for default or production-like runs.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(DevSyntheticExclusiveProfileCondition.class)
@ConditionalOnProperty(
        name = "financial-platform.identity.synthetic.enabled",
        havingValue = "true",
        matchIfMissing = false)
class SyntheticIdentityWorkspaceConfiguration {

    @Bean
    InMemorySyntheticWorkspaceStore inMemorySyntheticWorkspaceStore() {
        return SyntheticWorkspaceFixtures.alphaTeamBaseline();
    }

    @Bean
    WorkspaceAuthorizationService workspaceAuthorizationService(InMemorySyntheticWorkspaceStore store) {
        return new WorkspaceAuthorizationService(store);
    }

    @Bean
    WorkspaceMembershipLifecycleService workspaceMembershipLifecycleService(
            InMemorySyntheticWorkspaceStore store, WorkspaceAuthorizationService workspaceAuthorizationService) {
        return new WorkspaceMembershipLifecycleService(store, store, workspaceAuthorizationService);
    }

    @Bean
    WorkspaceActorContextPort workspaceActorContextPort() {
        return new SecurityContextWorkspaceActorAdapter();
    }

    @Bean
    TrustedWorkspaceActorBoundary trustedWorkspaceActorBoundary(
            WorkspaceActorContextPort workspaceActorContextPort,
            WorkspaceAuthorizationService workspaceAuthorizationService,
            WorkspaceMembershipLifecycleService workspaceMembershipLifecycleService) {
        return new TrustedWorkspaceActorBoundary(
                workspaceActorContextPort, workspaceAuthorizationService, workspaceMembershipLifecycleService);
    }
}