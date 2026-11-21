package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.AccountSecurityAuthRevisionReadPort;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Synthetic DEV/TEST authoritative authRevision; not durable production state. */
public final class InMemoryAccountSecurityAuthRevisionStore implements AccountSecurityAuthRevisionReadPort {

    private final Map<UserId, Long> revisions = new ConcurrentHashMap<>();

    public void setRevision(UserId userId, long revision) {
        revisions.put(userId, revision);
    }

    @Override
    public Optional<Long> currentAuthRevision(UserId userId) {
        return Optional.ofNullable(revisions.get(userId));
    }
}