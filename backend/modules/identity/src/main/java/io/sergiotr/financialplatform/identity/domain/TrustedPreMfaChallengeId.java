package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Opaque server-issued pre-MFA challenge identifier; not derivable from {@link UserId}. */
public record TrustedPreMfaChallengeId(UUID value) {
    public TrustedPreMfaChallengeId {
        Objects.requireNonNull(value, "value");
    }

    public static TrustedPreMfaChallengeId generate() {
        return new TrustedPreMfaChallengeId(UUID.randomUUID());
    }

    public static TrustedPreMfaChallengeId of(UUID id) {
        return new TrustedPreMfaChallengeId(id);
    }

    @Override
    public String toString() {
        return "TrustedPreMfaChallengeId[opaque]";
    }
}