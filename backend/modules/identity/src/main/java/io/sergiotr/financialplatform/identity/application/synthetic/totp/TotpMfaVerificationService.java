package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.port.AuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.port.TotpAuthenticatorBindingReadPort;
import io.sergiotr.financialplatform.identity.port.TotpSecretReadPort;
import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import io.sergiotr.financialplatform.identity.port.TrustedMfaChallengeContextPort;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeConsumePort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeSnapshot;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

/**
 * Verifies TOTP with fail-closed durable replay boundary. Replay checkpoint and actor identity are
 * server-owned; does not mint AUTHENTICATED_FULL or wire HTTP login.
 */
public final class TotpMfaVerificationService {

    private final Clock clock;
    private final TrustedMfaChallengeContextPort challengeContext;
    private final TrustedPreMfaChallengeLifecycleService challengeLifecycle;
    private final TotpSecretReadPort secretRead;
    private final TotpAuthenticatorBindingReadPort bindingRead;
    private final AuthenticatorStepCompareAndSetPort stepCas;
    private final Rfc6238TotpCryptographicVerifier crypto;

    public TotpMfaVerificationService(
            Clock clock,
            TrustedMfaChallengeContextPort challengeContext,
            TrustedPreMfaChallengeLifecycleService challengeLifecycle,
            TotpSecretReadPort secretRead,
            TotpAuthenticatorBindingReadPort bindingRead,
            AuthenticatorStepCompareAndSetPort stepCas) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.challengeContext = Objects.requireNonNull(challengeContext, "challengeContext");
        this.challengeLifecycle = Objects.requireNonNull(challengeLifecycle, "challengeLifecycle");
        this.secretRead = Objects.requireNonNull(secretRead, "secretRead");
        this.bindingRead = Objects.requireNonNull(bindingRead, "bindingRead");
        this.stepCas = Objects.requireNonNull(stepCas, "stepCas");
        this.crypto = new Rfc6238TotpCryptographicVerifier(clock);
    }

    public TotpVerificationOutcome verify(AuthenticatorId authenticatorId, String totpCode) {
        Objects.requireNonNull(authenticatorId, "authenticatorId");
        if (!Rfc6238TotpCryptographicVerifier.isValidTotpCode(totpCode)) {
            return TotpVerificationOutcome.INVALID_INPUT;
        }
        var challengeId = challengeContext.currentServerChallengeId();
        var challengeValidation = challengeLifecycle.validateForTotp(challengeId, false);
        if (challengeValidation != TrustedPreMfaChallengeLifecycleService.ValidationOutcome.VALID) {
            return mapChallengeFailure(challengeValidation);
        }
        TrustedPreMfaChallengeSnapshot challenge =
                challengeLifecycle.find(challengeId.get()).orElseThrow();
        UserId trustedUser = challenge.subjectUserId();
        var binding = bindingRead.findBinding(authenticatorId);
        if (binding.isEmpty()) {
            return TotpVerificationOutcome.AUTHENTICATOR_UNKNOWN;
        }
        var snapshot = binding.get();
        if (!snapshot.ownerUserId().equals(trustedUser)) {
            return TotpVerificationOutcome.AUTHENTICATOR_OWNER_MISMATCH;
        }
        if (snapshot.status() != TotpAuthenticatorBindingReadPort.Status.ACTIVE) {
            return TotpVerificationOutcome.AUTHENTICATOR_INACTIVE;
        }
        long lastAcceptedStep = snapshot.lastAcceptedStep();
        var seed = secretRead.findSeed(authenticatorId);
        if (seed.isEmpty()) {
            return TotpVerificationOutcome.SECRET_UNAVAILABLE;
        }
        var matchedStep = crypto.findGreatestMatchingStep(seed.get(), totpCode, lastAcceptedStep);
        if (matchedStep.isEmpty()) {
            if (crypto.matchesOnlyStaleSteps(seed.get(), totpCode, lastAcceptedStep)) {
                return TotpVerificationOutcome.REPLAY_STEP_ALREADY_ACCEPTED;
            }
            return TotpVerificationOutcome.CRYPTO_MISMATCH;
        }
        var cas = stepCas.compareAndSetAcceptedStep(
                authenticatorId,
                snapshot.ownerUserId(),
                matchedStep.getAsLong(),
                lastAcceptedStep,
                snapshot.replayStateVersion());
        return switch (cas.outcome()) {
            case NOT_CONFIGURED -> TotpVerificationOutcome.REPLAY_PERSISTENCE_NOT_CONFIGURED;
            case STALE_RACE, OWNER_MISMATCH -> TotpVerificationOutcome.REPLAY_STEP_ALREADY_ACCEPTED;
            case ACCEPTED -> {
                var consume = challengeLifecycle.consumeIfActive(
                        challengeId.get(), challenge.boundAuthRevision());
                if (consume != TrustedPreMfaChallengeConsumePort.ConsumeOutcome.CONSUMED) {
                    yield TotpVerificationOutcome.TRUSTED_CHALLENGE_NOT_ACTIVE;
                }
                yield TotpVerificationOutcome.ACCEPTED;
            }
        };
    }

    private static TotpVerificationOutcome mapChallengeFailure(
            TrustedPreMfaChallengeLifecycleService.ValidationOutcome outcome) {
        return switch (outcome) {
            case MISSING -> TotpVerificationOutcome.TRUSTED_CHALLENGE_MISSING;
            case NOT_FOUND -> TotpVerificationOutcome.TRUSTED_CHALLENGE_NOT_FOUND;
            case NOT_ACTIVE -> TotpVerificationOutcome.TRUSTED_CHALLENGE_NOT_ACTIVE;
            case EXPIRED -> TotpVerificationOutcome.TRUSTED_CHALLENGE_EXPIRED;
            case WRONG_STAGE -> TotpVerificationOutcome.TRUSTED_CHALLENGE_WRONG_STAGE;
            case STALE_AUTH_REVISION -> TotpVerificationOutcome.TRUSTED_CHALLENGE_STALE_AUTH_REVISION;
            case DISABLED_SUBJECT -> TotpVerificationOutcome.TRUSTED_CHALLENGE_NOT_ACTIVE;
            case VALID -> throw new IllegalStateException("VALID");
        };
    }
}