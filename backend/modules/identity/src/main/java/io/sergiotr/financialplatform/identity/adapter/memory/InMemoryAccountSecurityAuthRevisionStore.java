package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.AccountSecuritySnapshot;
import io.sergiotr.financialplatform.identity.port.AccountSecuritySnapshotReadPort;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Synthetic DEV/TEST authoritative account security; not durable production state. */
public final class InMemoryAccountSecurityAuthRevisionStore implements AccountSecuritySnapshotReadPort {

    private record Entry(long authRevision, UserAccountSecurityStatus status) {}

    private final Map<UserId, Entry> byUser = new ConcurrentHashMap<>();

    public void setRevision(UserId userId, long revision) {
        byUser.compute(
                userId,
                (id, existing) -> new Entry(
                        revision,
                        existing == null ? UserAccountSecurityStatus.ENABLED : existing.status()));
    }

    public void setStatus(UserId userId, UserAccountSecurityStatus status) {
        byUser.compute(
                userId,
                (id, existing) -> new Entry(existing == null ? 0L : existing.authRevision(), status));
    }

    public void put(UserId userId, long revision, UserAccountSecurityStatus status) {
        byUser.put(userId, new Entry(revision, status));
    }

    @Override
    public Optional<AccountSecuritySnapshot> currentSnapshot(UserId userId) {
        return Optional.ofNullable(byUser.get(userId))
                .map(entry -> new AccountSecuritySnapshot(entry.authRevision(), entry.status()));
    }
}