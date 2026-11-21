package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;

/** Active authenticator ownership/status (server-owned). */
public interface TotpAuthenticatorStatusReadPort {

    enum Status {
        ACTIVE,
        DISABLED,
        REVOKED,
        UNKNOWN
    }

    Status status(AuthenticatorId authenticatorId);
}