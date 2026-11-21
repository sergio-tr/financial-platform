package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import java.util.Optional;

/** Server-owned TOTP seed read model. Production persistence awaits SER-103/109 clearance. */
public interface TotpSecretReadPort {

    Optional<TotpSeed> findSeed(AuthenticatorId authenticatorId);
}