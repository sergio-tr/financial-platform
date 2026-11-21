package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.port.AuthenticatorStepCompareAndSetPort;

/** Default runtime: no durable CAS adapter (SER-103/109); TOTP MFA fails closed. */
public final class FailClosedAuthenticatorStepCompareAndSetPort implements AuthenticatorStepCompareAndSetPort {

    @Override
    public CasAttemptResult compareAndSetAcceptedStep(
            AuthenticatorId authenticatorId, long matchedStep, long observedLastAcceptedStep) {
        return new CasAttemptResult(CasOutcome.NOT_CONFIGURED, observedLastAcceptedStep);
    }
}