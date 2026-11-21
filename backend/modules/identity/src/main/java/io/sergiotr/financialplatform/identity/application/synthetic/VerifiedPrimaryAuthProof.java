package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Server-origin proof that primary authentication succeeded for a subject at a revision. Minted only
 * inside this package after trusted Argon2id verification; never holds client-supplied secrets.
 */
public final class VerifiedPrimaryAuthProof {

    private final UUID proofId;
    private final UserId subjectUserId;
    private final long authRevision;
    private final Instant issuedAt;
    private final Instant expiresAt;

    private VerifiedPrimaryAuthProof(
            UUID proofId, UserId subjectUserId, long authRevision, Instant issuedAt, Instant expiresAt) {
        this.proofId = Objects.requireNonNull(proofId, "proofId");
        this.subjectUserId = Objects.requireNonNull(subjectUserId, "subjectUserId");
        this.authRevision = authRevision;
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
    }

    static VerifiedPrimaryAuthProof mint(UserId subjectUserId, long authRevision, Instant issuedAt, Instant expiresAt) {
        return new VerifiedPrimaryAuthProof(UUID.randomUUID(), subjectUserId, authRevision, issuedAt, expiresAt);
    }

    UUID proofId() {
        return proofId;
    }

    public UserId subjectUserId() {
        return subjectUserId;
    }

    public long authRevision() {
        return authRevision;
    }

    Instant issuedAt() {
        return issuedAt;
    }

    Instant expiresAt() {
        return expiresAt;
    }

    boolean isActiveAt(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return !instant.isBefore(issuedAt) && instant.isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return "VerifiedPrimaryAuthProof{proofId="
                + proofId
                + ", subjectUserId="
                + subjectUserId
                + ", authRevision="
                + authRevision
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerifiedPrimaryAuthProof that)) {
            return false;
        }
        return proofId.equals(that.proofId);
    }

    @Override
    public int hashCode() {
        return proofId.hashCode();
    }
}