package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordCredential;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Optional;

/** Read-only access to server-owned Argon2id hashes for synthetic primary-factor checks. */
public interface SyntheticPasswordCredentialReadPort {

    Optional<SyntheticPasswordCredential> findByUserId(UserId userId);
}