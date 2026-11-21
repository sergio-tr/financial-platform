package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;

public interface TrustedPreMfaChallengeConsumePort {

    enum ConsumeOutcome {
        CONSUMED,
        NOT_FOUND,
        NOT_ACTIVE,
        STALE_REVISION
    }

    ConsumeOutcome consumeIfActive(TrustedPreMfaChallengeId challengeId, long expectedAuthRevision);
}