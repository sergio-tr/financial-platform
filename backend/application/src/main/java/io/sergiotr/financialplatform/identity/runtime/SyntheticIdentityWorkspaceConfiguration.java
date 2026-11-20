package io.sergiotr.financialplatform.identity.runtime;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.WorkspaceAuthorizationService;
import io.sergiotr.financialplatform.identity.application.WorkspaceMembershipLifecycleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring wiring for in-memory synthetic Identity fixtures. Active only with explicit {@code dev-synthetic} profile
 * and {@code financial-platform.identity.synthetic.enabled=true}; never registered for default or production-like runs.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev-synthetic")
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
}