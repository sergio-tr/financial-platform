package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;

public record Workspace(WorkspaceId id, WorkspaceStatus status) {

    public Workspace {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(status, "status");
    }

    public boolean permitsAccess() {
        return status == WorkspaceStatus.ACTIVE;
    }
}