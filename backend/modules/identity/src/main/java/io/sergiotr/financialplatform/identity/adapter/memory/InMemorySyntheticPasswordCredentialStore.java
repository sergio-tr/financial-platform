package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordCredential;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.SyntheticPasswordCredentialReadPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemorySyntheticPasswordCredentialStore implements SyntheticPasswordCredentialReadPort {

    private final Map<UserId, SyntheticPasswordCredential> byUser = new HashMap<>();

    public void put(SyntheticPasswordCredential credential) {
        Objects.requireNonNull(credential, "credential");
        byUser.put(credential.userId(), credential);
    }

    @Override
    public Optional<SyntheticPasswordCredential> findByUserId(UserId userId) {
        Objects.requireNonNull(userId, "userId");
        return Optional.ofNullable(byUser.get(userId));
    }
}