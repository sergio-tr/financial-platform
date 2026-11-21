package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.UserId;

/**
 * Durable per-{@link AuthenticatorId} last-accepted TOTP step (SER-24 Iteration 20).
 * PostgreSQL row lock / CAS only; in-memory implementations do not constitute security proof.
 */
public interface AuthenticatorStepCompareAndSetPort {

    enum CasOutcome {
        ACCEPTED,
        STALE_RACE,
        NOT_CONFIGURED,
        OWNER_MISMATCH
    }

    record CasAttemptResult(CasOutcome outcome, long lastAcceptedStep) {}

    /**
     * @param matchedStep cryptographically matched time step (strictly &gt; prior accepted)
     * @param observedLastAcceptedStep server-read checkpoint; must match durable row for success
     * @param observedReplayStateVersion server-read row version for optimistic concurrency
     */
    CasAttemptResult compareAndSetAcceptedStep(
            AuthenticatorId authenticatorId,
            UserId expectedOwnerUserId,
            long matchedStep,
            long observedLastAcceptedStep,
            long observedReplayStateVersion);
}