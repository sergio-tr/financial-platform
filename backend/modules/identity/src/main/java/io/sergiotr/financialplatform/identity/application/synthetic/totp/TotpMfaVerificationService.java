package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.port.AuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.port.TotpAuthenticatorBindingReadPort;
import io.sergiotr.financialplatform.identity.port.TotpSecretReadPort;
import io.sergiotr.financialplatform.identity.port.TrustedMfaChallengeContextPort;
import java.time.Clock;
import java.util.Objects;

/**
 * Verifies TOTP with fail-closed durable replay boundary. Replay checkpoint and actor identity are
 * server-owned; does not mint AUTHENTICATED_FULL or wire HTTP login.
 */
public final class TotpMfaVerificationService {

    private final Clock clock;
    private final TrustedMfaChallengeContextPort challengeContext;
    private final TotpSecretReadPort secretRead;
    private final TotpAuthenticatorBindingReadPort bindingRead;
    private final AuthenticatorStepCompareAndSetPort stepCas;
    private final Rfc6238TotpCryptographicVerifier crypto;

    public TotpMfaVerificationService(
            Clock clock,
            TrustedMfaChallengeContextPort challengeContext,
            TotpSecretReadPort secretRead,
            TotpAuthenticatorBindingReadPort bindingRead,
            AuthenticatorStepCompareAndSetPort stepCas) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.challengeContext = Objects.requireNonNull(challengeContext, "challengeContext");
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
        var trustedUser = challengeContext.trustedPreMfaUserId();
        if (trustedUser.isEmpty()) {
            return TotpVerificationOutcome.TRUSTED_CHALLENGE_MISSING;
        }
        var binding = bindingRead.findBinding(authenticatorId);
        if (binding.isEmpty()) {
            return TotpVerificationOutcome.AUTHENTICATOR_UNKNOWN;
        }
        var snapshot = binding.get();
        if (!snapshot.ownerUserId().equals(trustedUser.get())) {
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
            case ACCEPTED -> TotpVerificationOutcome.ACCEPTED;
        };
    }
}