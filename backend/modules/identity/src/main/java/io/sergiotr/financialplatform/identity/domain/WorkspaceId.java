package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record WorkspaceId(UUID value) {

    public WorkspaceId {
        Objects.requireNonNull(value, "value");
    }

    public static WorkspaceId of(UUID value) {
        return new WorkspaceId(value);
    }

    public static WorkspaceId parse(String raw) {
        return new WorkspaceId(UUID.fromString(raw));
    }
}