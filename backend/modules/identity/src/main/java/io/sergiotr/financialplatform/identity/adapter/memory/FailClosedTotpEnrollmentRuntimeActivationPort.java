package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TotpEnrollmentRuntimeActivationPort;

/** Synthetic default: no durable authenticator store; runtime login activation remains unavailable. */
public final class FailClosedTotpEnrollmentRuntimeActivationPort implements TotpEnrollmentRuntimeActivationPort {

    @Override
    public ActivationOutcome activateForRuntimeLogin(UserId owner, AuthenticatorId authenticatorId) {
        return ActivationOutcome.NOT_CONFIGURED;
    }
}