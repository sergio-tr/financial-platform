package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Objects;

/**
 * Credentials bundle for challenge issuance after server-verified primary auth. Callers must not
 * forge evidenceRef; lifecycle re-verifies against {@link io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort}.
 */
public record VerifiedPrimaryForMfaIssuance(
        UserId subjectUserId, long authRevision, AuthenticationStage targetMfaStage, String primaryEvidenceRef) {

    public VerifiedPrimaryForMfaIssuance {
        Objects.requireNonNull(subjectUserId, "subjectUserId");
        Objects.requireNonNull(targetMfaStage, "targetMfaStage");
        if (primaryEvidenceRef == null || primaryEvidenceRef.isBlank()) {
            throw new IllegalArgumentException("primaryEvidenceRef required");
        }
        if (targetMfaStage != AuthenticationStage.MFA_REQUIRED
                && targetMfaStage != AuthenticationStage.MFA_ENROLLMENT_REQUIRED) {
            throw new IllegalArgumentException("targetMfaStage must be MFA_REQUIRED or MFA_ENROLLMENT_REQUIRED");
        }
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
    }
}