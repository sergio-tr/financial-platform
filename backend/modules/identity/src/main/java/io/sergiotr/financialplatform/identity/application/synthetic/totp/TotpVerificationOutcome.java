package io.sergiotr.financialplatform.identity.application.synthetic.totp;

/**
 * MFA verification outcome. {@link #CRYPTO_ACCEPTED_REPLAY_NOT_PROVEN} is test-only labeling when a fake CAS accepts;
 * not production anti-replay proof.
 */
public enum TotpVerificationOutcome {
    ACCEPTED,
    CRYPTO_MISMATCH,
    AUTHENTICATOR_INACTIVE,
    SECRET_UNAVAILABLE,
    REPLAY_STEP_ALREADY_ACCEPTED,
    REPLAY_PERSISTENCE_NOT_CONFIGURED,
    INVALID_INPUT
}