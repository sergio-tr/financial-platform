package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Optional;

/** Authoritative server authRevision for account security decisions (synthetic DEV/TEST; not production RLS). */
public interface AccountSecurityAuthRevisionReadPort {

    Optional<Long> currentAuthRevision(UserId userId);
}