package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import java.util.Objects;

/** Authoritative synthetic account security snapshot (revision + lifecycle status). */
public record AccountSecuritySnapshot(long authRevision, UserAccountSecurityStatus status) {

    public AccountSecuritySnapshot {
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
        Objects.requireNonNull(status, "status");
    }
}