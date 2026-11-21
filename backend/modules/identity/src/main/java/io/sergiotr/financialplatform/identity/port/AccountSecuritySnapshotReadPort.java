package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserId;
import java.util.Optional;

/** Server-owned account security snapshot; fail closed when unknown. */
public interface AccountSecuritySnapshotReadPort {

    Optional<AccountSecuritySnapshot> currentSnapshot(UserId userId);
}