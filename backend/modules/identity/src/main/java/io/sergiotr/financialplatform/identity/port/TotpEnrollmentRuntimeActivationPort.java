package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.UserId;

/**
 * Marks a confirmed enrollment as runtime-active for login (SER-24 Iteration 20 durable store).
 * Default synthetic adapter is fail-closed until SER-103/109 clearance.
 */
public interface TotpEnrollmentRuntimeActivationPort {

    enum ActivationOutcome {
        ACTIVATED,
        NOT_CONFIGURED,
        ALREADY_ACTIVE,
        SUBJECT_MISMATCH,
        NOT_CONFIRMED
    }

    ActivationOutcome activateForRuntimeLogin(UserId owner, AuthenticatorId authenticatorId);
}