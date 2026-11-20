package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.MembershipStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.Workspace;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceMembership;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.domain.WorkspaceStatus;
import io.sergiotr.financialplatform.identity.port.WorkspaceAccessReadPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory synthetic fixture store (DEV/TEST only). Not thread-safe; not for production.
 */
public final class InMemorySyntheticWorkspaceStore implements WorkspaceAccessReadPort {

    private final Map<WorkspaceId, Workspace> workspaces = new HashMap<>();
    private final Map<MembershipKey, WorkspaceMembership> memberships = new HashMap<>();

    public void putWorkspace(WorkspaceId id, WorkspaceStatus status) {
        workspaces.put(id, new Workspace(id, status));
    }

    public void putMembership(
            UserId userId,
            WorkspaceId workspaceId,
            WorkspaceRole role,
            MembershipStatus status,
            long authRevision) {
        memberships.put(
                new MembershipKey(userId, workspaceId),
                new WorkspaceMembership(userId, workspaceId, role, status, authRevision));
    }

    @Override
    public Optional<Workspace> findWorkspace(WorkspaceId workspaceId) {
        return Optional.ofNullable(workspaces.get(workspaceId));
    }

    @Override
    public Optional<WorkspaceMembership> findMembership(UserId userId, WorkspaceId workspaceId) {
        return Optional.ofNullable(memberships.get(new MembershipKey(userId, workspaceId)));
    }

    private record MembershipKey(UserId userId, WorkspaceId workspaceId) {}
}