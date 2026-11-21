package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Optional;

/**
 * Server-owned authenticator ownership, lifecycle status, and durable replay checkpoint (SER-24 I20).
 */
public interface TotpAuthenticatorBindingReadPort {

    enum Status {
        ACTIVE,
        DISABLED,
        REVOKED,
        UNKNOWN
    }

    record Binding(
            UserId ownerUserId,
            Status status,
            long lastAcceptedStep,
            long replayStateVersion) {}

    Optional<Binding> findBinding(AuthenticatorId authenticatorId);
}