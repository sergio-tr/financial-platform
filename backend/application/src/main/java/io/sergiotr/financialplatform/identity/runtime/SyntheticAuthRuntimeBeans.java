package io.sergiotr.financialplatform.identity.runtime;

import io.sergiotr.financialplatform.identity.adapter.crypto.Argon2idSyntheticPasswordHasher;
import io.sergiotr.financialplatform.identity.adapter.crypto.Argon2idTrustedPrimaryAuthEvidence;
import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticPasswordCredentialStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticCommonPasswordBlocklist;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordPolicy;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordService;
import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordCredential;
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * DEV/TEST synthetic primary-password verification beans. Hashes are minted at startup from non-committed probe
 * passwords; not exposed through HTTP login.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(DevSyntheticExclusiveProfileCondition.class)
@ConditionalOnProperty(
        name = "financial-platform.identity.synthetic.enabled",
        havingValue = "true",
        matchIfMissing = false)
class SyntheticAuthRuntimeBeans {

    static final String DEV_PRIMARY_PROBE_ALICE = "dev-synthetic-primary-probe-alice";
    static final String DEV_PRIMARY_PROBE_BOB = "dev-synthetic-primary-probe-bob";

    @Bean
    InMemorySyntheticPasswordCredentialStore syntheticPasswordCredentialStore() {
        var hasher = new Argon2idSyntheticPasswordHasher();
        var store = new InMemorySyntheticPasswordCredentialStore();
        store.put(new SyntheticPasswordCredential(
                SyntheticWorkspaceFixtures.ALICE, hasher.encode(DEV_PRIMARY_PROBE_ALICE), false));
        store.put(new SyntheticPasswordCredential(
                SyntheticWorkspaceFixtures.BOB, hasher.encode(DEV_PRIMARY_PROBE_BOB), false));
        return store;
    }

    @Bean
    SyntheticPasswordService syntheticPasswordService() {
        var hasher = new Argon2idSyntheticPasswordHasher();
        return new SyntheticPasswordService(
                new SyntheticPasswordPolicy(new SyntheticCommonPasswordBlocklist().loadFromClasspath()), hasher);
    }

    @Bean
    TrustedPrimaryAuthEvidencePort trustedPrimaryAuthEvidencePort(
            InMemorySyntheticPasswordCredentialStore store, SyntheticPasswordService passwordService) {
        return new Argon2idTrustedPrimaryAuthEvidence(store, passwordService);
    }
}