package io.sergiotr.financialplatform.identity.adapter.memory;

import io.sergiotr.financialplatform.identity.domain.MembershipStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.Workspace;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceMembership;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.domain.WorkspaceStatus;
import io.sergiotr.financialplatform.identity.port.WorkspaceAccessReadPort;
import io.sergiotr.financialplatform.identity.port.WorkspaceMembershipWritePort;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory synthetic fixture store (DEV/TEST only). Not thread-safe; not for production.
 */
public final class InMemorySyntheticWorkspaceStore
        implements WorkspaceAccessReadPort, WorkspaceMembershipWritePort {

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

    @Override
    public int countActiveOwners(WorkspaceId workspaceId) {
        int count = 0;
        for (WorkspaceMembership membership : memberships.values()) {
            if (membership.workspaceId().equals(workspaceId)
                    && membership.isActive()
                    && membership.role() == WorkspaceRole.OWNER) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean insertMembership(WorkspaceMembership membership) {
        var key = new MembershipKey(membership.userId(), membership.workspaceId());
        var existing = memberships.get(key);
        if (existing != null && existing.isActive()) {
            return false;
        }
        memberships.put(key, membership);
        return true;
    }

    @Override
    public boolean compareAndReplace(WorkspaceMembership expected, WorkspaceMembership updated) {
        var key = new MembershipKey(expected.userId(), expected.workspaceId());
        var current = memberships.get(key);
        if (current == null || !membershipsEqual(current, expected)) {
            return false;
        }
        memberships.put(key, updated);
        return true;
    }

    private static boolean membershipsEqual(WorkspaceMembership left, WorkspaceMembership right) {
        return left.userId().equals(right.userId())
                && left.workspaceId().equals(right.workspaceId())
                && left.role() == right.role()
                && left.status() == right.status()
                && left.authRevision() == right.authRevision();
    }

    private record MembershipKey(UserId userId, WorkspaceId workspaceId) {}
}