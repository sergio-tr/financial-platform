package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;

/** Server-owned synthetic password record for DEV/TEST verification only. */
public record SyntheticPasswordCredential(UserId userId, String encodedHash, boolean disabled) {

    public SyntheticPasswordCredential {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(encodedHash, "encodedHash");
        if (encodedHash.isBlank()) {
            throw new IllegalArgumentException("encodedHash must not be blank");
        }
    }
}