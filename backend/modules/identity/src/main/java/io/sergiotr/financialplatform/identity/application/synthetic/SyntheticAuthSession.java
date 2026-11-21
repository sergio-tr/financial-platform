package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** In-memory synthetic session; construct only from this package via the state machine. */
record SyntheticAuthSession(
        UserId userId,
        AuthenticationStage stage,
        long authRevision,
        Instant stageExpiresAt,
        boolean disabled,
        AuthSessionTrustMark trustMark,
        Optional<TrustedPreMfaChallengeId> preMfaChallengeId,
        Optional<String> verifiedPrimaryEvidenceRef) {

    SyntheticAuthSession {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(stageExpiresAt, "stageExpiresAt");
        Objects.requireNonNull(trustMark, "trustMark");
        Objects.requireNonNull(preMfaChallengeId, "preMfaChallengeId");
        Objects.requireNonNull(verifiedPrimaryEvidenceRef, "verifiedPrimaryEvidenceRef");
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
    }

    TrustedWorkspaceActor toTrustedActor() {
        if (stage != AuthenticationStage.AUTHENTICATED || !AuthSessionTrustMark.isAuthenticated(trustMark)) {
            throw new IllegalStateException("session not authenticated through verified MFA path");
        }
        return new TrustedWorkspaceActor(userId, authRevision, SecurityAssuranceStatus.AUTHENTICATED_FULL);
    }
}