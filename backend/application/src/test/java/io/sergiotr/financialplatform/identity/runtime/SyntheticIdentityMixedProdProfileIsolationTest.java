package io.sergiotr.financialplatform.identity.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticWorkspaceStore;
import io.sergiotr.financialplatform.identity.application.TrustedWorkspaceActorBoundary;
import io.sergiotr.financialplatform.identity.application.WorkspaceAuthorizationService;
import io.sergiotr.financialplatform.identity.application.WorkspaceMembershipLifecycleService;
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"prod", "dev-synthetic"})
class SyntheticIdentityMixedProdProfileIsolationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void prodPlusDevSyntheticDoesNotExposeSyntheticBeans() {
        assertThat(context.getBeansOfType(InMemorySyntheticWorkspaceStore.class)).isEmpty();
        assertThat(context.getBeansOfType(WorkspaceAuthorizationService.class)).isEmpty();
        assertThat(context.getBeansOfType(WorkspaceMembershipLifecycleService.class)).isEmpty();
        assertThat(context.getBeansOfType(TrustedWorkspaceActorBoundary.class)).isEmpty();
        assertThat(context.getBeansOfType(SyntheticIdentityWorkspaceConfiguration.class)).isEmpty();
        assertThat(context.getBeansOfType(TrustedPrimaryAuthEvidencePort.class)).isEmpty();
    }
}