package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import java.util.Optional;

/**
 * Server-bound opaque pre-MFA challenge for the current operation. Callers must never supply
 * {@link io.sergiotr.financialplatform.identity.domain.UserId} or stage flags here.
 */
public interface TrustedMfaChallengeContextPort {

    Optional<TrustedPreMfaChallengeId> currentServerChallengeId();
}