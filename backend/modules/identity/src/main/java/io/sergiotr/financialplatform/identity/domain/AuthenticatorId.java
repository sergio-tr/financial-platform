package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record AuthenticatorId(UUID value) {

    public AuthenticatorId {
        Objects.requireNonNull(value, "value");
    }

    public static AuthenticatorId of(UUID value) {
        return new AuthenticatorId(value);
    }

    public static AuthenticatorId parse(String raw) {
        return new AuthenticatorId(UUID.fromString(raw));
    }
}