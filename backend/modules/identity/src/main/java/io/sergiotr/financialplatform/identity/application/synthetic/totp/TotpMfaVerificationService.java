package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.port.AuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.port.TotpAuthenticatorStatusReadPort;
import io.sergiotr.financialplatform.identity.port.TotpSecretReadPort;
import java.time.Clock;
import java.util.Objects;

/**
 * Verifies TOTP with fail-closed durable replay boundary. Does not mint AUTHENTICATED_FULL or wire HTTP login.
 */
public final class TotpMfaVerificationService {

    private final Clock clock;
    private final TotpSecretReadPort secretRead;
    private final TotpAuthenticatorStatusReadPort statusRead;
    private final AuthenticatorStepCompareAndSetPort stepCas;
    private final Rfc6238TotpCryptographicVerifier crypto;

    public TotpMfaVerificationService(
            Clock clock,
            TotpSecretReadPort secretRead,
            TotpAuthenticatorStatusReadPort statusRead,
            AuthenticatorStepCompareAndSetPort stepCas) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.secretRead = Objects.requireNonNull(secretRead, "secretRead");
        this.statusRead = Objects.requireNonNull(statusRead, "statusRead");
        this.stepCas = Objects.requireNonNull(stepCas, "stepCas");
        this.crypto = new Rfc6238TotpCryptographicVerifier(clock);
    }

    public TotpVerificationOutcome verify(AuthenticatorId authenticatorId, String totpCode, long lastAcceptedStep) {
        Objects.requireNonNull(authenticatorId, "authenticatorId");
        if (!Rfc6238TotpCryptographicVerifier.isValidTotpCode(totpCode)) {
            return TotpVerificationOutcome.INVALID_INPUT;
        }
        if (statusRead.status(authenticatorId) != TotpAuthenticatorStatusReadPort.Status.ACTIVE) {
            return TotpVerificationOutcome.AUTHENTICATOR_INACTIVE;
        }
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
        var cas = stepCas.compareAndSetAcceptedStep(authenticatorId, matchedStep.getAsLong(), lastAcceptedStep);
        return switch (cas.outcome()) {
            case NOT_CONFIGURED -> TotpVerificationOutcome.REPLAY_PERSISTENCE_NOT_CONFIGURED;
            case STALE_RACE -> TotpVerificationOutcome.REPLAY_STEP_ALREADY_ACCEPTED;
            case ACCEPTED -> TotpVerificationOutcome.ACCEPTED;
        };
    }
}