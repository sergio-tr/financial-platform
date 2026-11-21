package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import java.util.Optional;

public interface TrustedPreMfaChallengeLookupPort {

    Optional<TrustedPreMfaChallengeSnapshot> find(TrustedPreMfaChallengeId challengeId);
}