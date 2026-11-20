package io.sergiotr.financialplatform.identity.domain;

import java.util.Objects;

/**
 * Active membership binds a subject to a workspace with a role. {@code authRevision} increments when
 * role or status changes so stale client contexts can be denied (REQ-WORKSPACE-004).
 */
public record WorkspaceMembership(
        UserId userId,
        WorkspaceId workspaceId,
        WorkspaceRole role,
        MembershipStatus status,
        long authRevision) {

    public WorkspaceMembership {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(status, "status");
        if (authRevision < 0) {
            throw new IllegalArgumentException("authRevision");
        }
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }
}