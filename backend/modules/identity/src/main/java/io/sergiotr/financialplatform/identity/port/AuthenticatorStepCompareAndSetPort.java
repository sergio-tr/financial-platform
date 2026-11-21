package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;

/**
 * Durable per-{@link AuthenticatorId} last-accepted TOTP step (SER-24 Iteration 20).
 * PostgreSQL row lock / CAS only; in-memory implementations do not constitute security proof.
 */
public interface AuthenticatorStepCompareAndSetPort {

    enum CasOutcome {
        ACCEPTED,
        STALE_RACE,
        NOT_CONFIGURED
    }

    record CasAttemptResult(CasOutcome outcome, long lastAcceptedStep) {}

    /**
     * @param matchedStep cryptographically matched time step (strictly &gt; prior accepted)
     * @param observedLastAcceptedStep value read before compare; must match durable row for success
     */
    CasAttemptResult compareAndSetAcceptedStep(
            AuthenticatorId authenticatorId, long matchedStep, long observedLastAcceptedStep);
}