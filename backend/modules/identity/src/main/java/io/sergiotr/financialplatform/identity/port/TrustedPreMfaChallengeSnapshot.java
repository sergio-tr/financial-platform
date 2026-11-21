package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.time.Instant;
import java.util.Objects;

/** Server-owned active challenge view for TOTP verification; callers cannot mint this record. */
public record TrustedPreMfaChallengeSnapshot(
        TrustedPreMfaChallengeId challengeId,
        UserId subjectUserId,
        long boundAuthRevision,
        AuthenticationStage boundStage,
        Instant issuedAt,
        Instant expiresAt,
        LifecycleStatus status) {

    public enum LifecycleStatus {
        ACTIVE,
        CONSUMED,
        REVOKED
    }

    public TrustedPreMfaChallengeSnapshot {
        Objects.requireNonNull(challengeId, "challengeId");
        Objects.requireNonNull(subjectUserId, "subjectUserId");
        Objects.requireNonNull(boundStage, "boundStage");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(status, "status");
        if (boundAuthRevision < 0) {
            throw new IllegalArgumentException("boundAuthRevision");
        }
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
    }

    @Override
    public String toString() {
        return "TrustedPreMfaChallengeSnapshot[opaque]";
    }
}