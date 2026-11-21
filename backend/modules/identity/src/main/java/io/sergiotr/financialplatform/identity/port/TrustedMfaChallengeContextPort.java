package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Optional;

/**
 * Server-owned pre-MFA challenge identity. Callers must never supply {@link UserId} for TOTP replay binding.
 */
public interface TrustedMfaChallengeContextPort {

    Optional<UserId> trustedPreMfaUserId();
}