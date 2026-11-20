package io.sergiotr.financialplatform.identity.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.application.WorkspaceAuthorizationService;
import io.sergiotr.financialplatform.identity.application.WorkspaceMembershipLifecycleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class SyntheticIdentityDefaultProfileIsolationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void defaultContextHasNoSyntheticIdentityBeans() {
        assertThat(context.getBeansOfType(InMemorySyntheticWorkspaceStore.class)).isEmpty();
        assertThat(context.getBeansOfType(WorkspaceAuthorizationService.class)).isEmpty();
        assertThat(context.getBeansOfType(WorkspaceMembershipLifecycleService.class)).isEmpty();
        assertThat(context.getBeansOfType(SyntheticIdentityWorkspaceConfiguration.class)).isEmpty();
    }
}