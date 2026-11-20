package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "value");
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId parse(String raw) {
        return new UserId(UUID.fromString(raw));
    }
}