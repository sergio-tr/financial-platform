package io.sergiotr.financialplatform.identity.application.synthetic.enrollment;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.PendingTotpEnrollmentId;
import java.util.Optional;

public record TotpEnrollmentOutcome(
        Result result,
        Optional<PendingTotpEnrollmentId> pendingId,
        Optional<AuthenticatorId> pendingAuthenticatorId) {

    public enum Result {
        PENDING_STARTED,
        CONFIRMED_SYNTHETIC,
        CANCELLED,
        DENIED_MISSING_CHALLENGE,
        DENIED_NOT_FOUND,
        DENIED_WRONG_STAGE,
        DENIED_EXPIRED,
        DENIED_STALE_AUTH_REVISION,
        DENIED_DISABLED,
        DENIED_LOCKED,
        DENIED_REVOKED,
        DENIED_SUBJECT_UNKNOWN,
        DENIED_DUPLICATE_PENDING,
        DENIED_INVALID_TOTP,
        DENIED_WRONG_OWNER,
        DENIED_NOT_PENDING,
        DENIED_CHALLENGE_INACTIVE
    }

    public static TotpEnrollmentOutcome pendingStarted(PendingTotpEnrollmentId id, AuthenticatorId authenticatorId) {
        return new TotpEnrollmentOutcome(
                Result.PENDING_STARTED, Optional.of(id), Optional.of(authenticatorId));
    }

    public static TotpEnrollmentOutcome confirmedSynthetic(PendingTotpEnrollmentId id, AuthenticatorId authenticatorId) {
        return new TotpEnrollmentOutcome(
                Result.CONFIRMED_SYNTHETIC, Optional.of(id), Optional.of(authenticatorId));
    }

    public static TotpEnrollmentOutcome cancelled(PendingTotpEnrollmentId id) {
        return new TotpEnrollmentOutcome(Result.CANCELLED, Optional.of(id), Optional.empty());
    }

    public static TotpEnrollmentOutcome denied(Result reason) {
        return new TotpEnrollmentOutcome(reason, Optional.empty(), Optional.empty());
    }
}