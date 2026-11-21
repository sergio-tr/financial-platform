package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionDenialReason;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionResult;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedMfaEvidencePort;
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class SyntheticAuthenticationStateMachine {

    private static final Duration STAGE_TTL = Duration.ofMinutes(15);

    private final Clock clock;
    private final TrustedPrimaryAuthEvidencePort primaryAuthEvidence;
    private final TrustedMfaEvidencePort mfaEvidence;

    public SyntheticAuthenticationStateMachine(
            Clock clock, TrustedPrimaryAuthEvidencePort primaryAuthEvidence, TrustedMfaEvidencePort mfaEvidence) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.primaryAuthEvidence = Objects.requireNonNull(primaryAuthEvidence, "primaryAuthEvidence");
        this.mfaEvidence = Objects.requireNonNull(mfaEvidence, "mfaEvidence");
    }

    public SyntheticAuthSession begin(UserId userId, long authRevision) {
        Objects.requireNonNull(userId, "userId");
        return mint(userId, AuthenticationStage.ANONYMOUS, authRevision, false, AuthSessionTrustMark.bootstrap());
    }

  /** Verified transition only; returns authoritative next session when applied (no blind stage elevation). */
    public SyntheticAuthTransition transition(
            SyntheticAuthSession session, AuthenticationTransitionEvent event, String evidenceRef) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(event, "event");
        if (session.disabled()) {
            return SyntheticAuthTransition.denied(AuthenticationTransitionDenialReason.DISABLED);
        }
        if (isExpired(session)) {
            return SyntheticAuthTransition.denied(AuthenticationTransitionDenialReason.EXPIRED);
        }
        AuthenticationTransitionResult outcome =
                switch (session.stage()) {
                    case ANONYMOUS -> fromAnonymous(session, event, evidenceRef);
                    case PRIMARY_AUTH_VERIFIED -> fromPrimaryVerified(session, event, evidenceRef);
                    case MFA_REQUIRED -> fromMfaRequired(session, event, evidenceRef);
                    case MFA_ENROLLMENT_REQUIRED -> fromMfaEnrollment(session, event, evidenceRef);
                    case AUTHENTICATED, RECOVERY_REQUIRED ->
                            AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
                };
        if (!outcome.applied()) {
            return SyntheticAuthTransition.denied(outcome.denialReason());
        }
        var nextStage = outcome.stage().orElseThrow();
        AuthSessionTrustMark mark =
                nextStage == AuthenticationStage.AUTHENTICATED
                        ? AuthSessionTrustMark.authenticated()
                        : AuthSessionTrustMark.intermediate();
        var next = mint(session.userId(), nextStage, session.authRevision(), session.disabled(), mark);
        return SyntheticAuthTransition.applied(outcome, next);
    }

    private AuthenticationTransitionResult fromAnonymous(
            SyntheticAuthSession session, AuthenticationTransitionEvent event, String evidenceRef) {
        if (event != AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH) {
            return AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
        }
        if (!primaryAuthEvidence.acceptsPrimaryAuth(session.userId(), evidenceRef)) {
            return AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INSUFFICIENT_EVIDENCE);
        }
        return AuthenticationTransitionResult.applied(AuthenticationStage.PRIMARY_AUTH_VERIFIED);
    }

    private AuthenticationTransitionResult fromPrimaryVerified(
            SyntheticAuthSession session, AuthenticationTransitionEvent event, String evidenceRef) {
        return switch (event) {
            case DIRECT_MFA_CHALLENGE -> AuthenticationTransitionResult.applied(AuthenticationStage.MFA_REQUIRED);
            case DIRECT_MFA_ENROLLMENT -> AuthenticationTransitionResult.applied(AuthenticationStage.MFA_ENROLLMENT_REQUIRED);
            case VERIFY_MFA -> {
                if (!mfaEvidence.acceptsMfa(session.userId(), evidenceRef)) {
                    yield AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INSUFFICIENT_EVIDENCE);
                }
                yield AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
            }
            default -> AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
        };
    }

    private AuthenticationTransitionResult fromMfaRequired(
            SyntheticAuthSession session, AuthenticationTransitionEvent event, String evidenceRef) {
        if (event != AuthenticationTransitionEvent.VERIFY_MFA) {
            return AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
        }
        if (!mfaEvidence.acceptsMfa(session.userId(), evidenceRef)) {
            return AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INSUFFICIENT_EVIDENCE);
        }
        return AuthenticationTransitionResult.applied(AuthenticationStage.AUTHENTICATED);
    }

    private AuthenticationTransitionResult fromMfaEnrollment(
            SyntheticAuthSession session, AuthenticationTransitionEvent event, String evidenceRef) {
        if (event != AuthenticationTransitionEvent.COMPLETE_MFA_ENROLLMENT) {
            return AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
        }
        if (!mfaEvidence.acceptsMfa(session.userId(), evidenceRef)) {
            return AuthenticationTransitionResult.denied(AuthenticationTransitionDenialReason.INSUFFICIENT_EVIDENCE);
        }
        return AuthenticationTransitionResult.applied(AuthenticationStage.AUTHENTICATED);
    }

    private SyntheticAuthSession mint(
            UserId userId,
            AuthenticationStage stage,
            long authRevision,
            boolean disabled,
            AuthSessionTrustMark trustMark) {
        return new SyntheticAuthSession(userId, stage, authRevision, expiresAt(), disabled, trustMark);
    }

    private boolean isExpired(SyntheticAuthSession session) {
        return clock.instant().isAfter(session.stageExpiresAt());
    }

    private Instant expiresAt() {
        return clock.instant().plus(STAGE_TTL);
    }
}