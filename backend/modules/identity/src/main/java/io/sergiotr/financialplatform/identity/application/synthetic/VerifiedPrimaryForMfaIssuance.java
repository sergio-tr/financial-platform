package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Objects;

/** Challenge issuance input bound to a server-minted primary proof (no credential material). */
public record VerifiedPrimaryForMfaIssuance(
        UserId subjectUserId,
        long authRevision,
        AuthenticationStage targetMfaStage,
        VerifiedPrimaryAuthProof primaryProof) {

    public VerifiedPrimaryForMfaIssuance {
        Objects.requireNonNull(subjectUserId, "subjectUserId");
        Objects.requireNonNull(targetMfaStage, "targetMfaStage");
        Objects.requireNonNull(primaryProof, "primaryProof");
        if (targetMfaStage != AuthenticationStage.MFA_REQUIRED
                && targetMfaStage != AuthenticationStage.MFA_ENROLLMENT_REQUIRED) {
            throw new IllegalArgumentException("targetMfaStage must be MFA_REQUIRED or MFA_ENROLLMENT_REQUIRED");
        }
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
        if (!subjectUserId.equals(primaryProof.subjectUserId())) {
            throw new IllegalArgumentException("primaryProof subject mismatch");
        }
        if (primaryProof.authRevision() != authRevision) {
            throw new IllegalArgumentException("primaryProof authRevision mismatch");
        }
    }
}