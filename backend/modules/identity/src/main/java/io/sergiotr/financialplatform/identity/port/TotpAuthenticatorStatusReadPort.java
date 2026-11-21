package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;

/** @deprecated Prefer {@link TotpAuthenticatorBindingReadPort}. */
@Deprecated(forRemoval = false)
public interface TotpAuthenticatorStatusReadPort extends TotpAuthenticatorBindingReadPort {

    /** @deprecated Use {@link #findBinding(AuthenticatorId)} */
    @Deprecated(forRemoval = false)
    default Status status(AuthenticatorId authenticatorId) {
        return findBinding(authenticatorId).map(Binding::status).orElse(Status.UNKNOWN);
    }
}