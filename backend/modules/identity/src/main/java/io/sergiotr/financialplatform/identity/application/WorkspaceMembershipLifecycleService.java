package io.sergiotr.financialplatform.identity.application;

import io.sergiotr.financialplatform.identity.domain.MembershipMutationDenialReason;
import io.sergiotr.financialplatform.identity.domain.MembershipMutationResult;
import io.sergiotr.financialplatform.identity.domain.MembershipStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceMembership;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.port.WorkspaceAccessReadPort;
import io.sergiotr.financialplatform.identity.port.WorkspaceMembershipWritePort;
import java.util.Objects;
import java.util.Optional;

/**
 * Synthetic DEV/TEST membership mutations (add / role change / revoke). Caller supplies actor context;
 * {@code clientAuthRevision} is not authenticated identity proof.
 */
public final class WorkspaceMembershipLifecycleService {

    private final WorkspaceAccessReadPort access;
    private final WorkspaceMembershipWritePort write;
    private final WorkspaceAuthorizationService authorization;

    public WorkspaceMembershipLifecycleService(
            WorkspaceAccessReadPort access,
            WorkspaceMembershipWritePort write,
            WorkspaceAuthorizationService authorization) {
        this.access = Objects.requireNonNull(access, "access");
        this.write = Objects.requireNonNull(write, "write");
        this.authorization = Objects.requireNonNull(authorization, "authorization");
    }

    public MembershipMutationResult addMember(
            UserId actorUserId,
            long actorAuthRevision,
            WorkspaceId workspaceId,
            UserId newMemberUserId,
            WorkspaceRole role) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(newMemberUserId, "newMemberUserId");
        Objects.requireNonNull(role, "role");

        var auth =
                authorization.authorize(actorUserId, workspaceId, WorkspaceOperation.MANAGE_MEMBERS, actorAuthRevision);
        if (!auth.permitted()) {
            return MembershipMutationResult.authorizationDenied(auth.denialReason());
        }

        var actorMembership = access.findMembership(actorUserId, workspaceId).filter(WorkspaceMembership::isActive);
        if (actorMembership.isEmpty()) {
            return MembershipMutationResult.authorizationDenied(auth.denialReason());
        }
        if (!actorMayAssignRole(actorMembership.get().role(), role)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.PRIVILEGE_ESCALATION);
        }

        var existing = access.findMembership(newMemberUserId, workspaceId);
        if (existing.isPresent() && existing.get().isActive()) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.DUPLICATE_ACTIVE_MEMBERSHIP);
        }

        var created = new WorkspaceMembership(
                newMemberUserId, workspaceId, role, MembershipStatus.ACTIVE, initialRevision(existing));
        if (!write.insertMembership(created)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.DUPLICATE_ACTIVE_MEMBERSHIP);
        }
        return MembershipMutationResult.applied(created);
    }

    public MembershipMutationResult changeRole(
            UserId actorUserId,
            long actorAuthRevision,
            WorkspaceId workspaceId,
            UserId targetUserId,
            WorkspaceRole newRole,
            long expectedTargetAuthRevision) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(targetUserId, "targetUserId");
        Objects.requireNonNull(newRole, "newRole");

        var auth =
                authorization.authorize(actorUserId, workspaceId, WorkspaceOperation.MANAGE_MEMBERS, actorAuthRevision);
        if (!auth.permitted()) {
            return MembershipMutationResult.authorizationDenied(auth.denialReason());
        }

        var target = access.findMembership(targetUserId, workspaceId);
        if (target.isEmpty() || !target.get().isActive()) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.TARGET_NOT_MEMBER);
        }
        var current = target.get();
        if (current.authRevision() != expectedTargetAuthRevision) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.STALE_WRITE);
        }

        var actorMembership = access.findMembership(actorUserId, workspaceId).orElseThrow();
        if (!actorMayAssignRole(actorMembership.role(), newRole)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.PRIVILEGE_ESCALATION);
        }
        if (wouldViolateLastOwner(workspaceId, current, newRole)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.LAST_OWNER_PROTECTED);
        }

        var updated = new WorkspaceMembership(
                current.userId(),
                current.workspaceId(),
                newRole,
                MembershipStatus.ACTIVE,
                current.authRevision() + 1);
        if (!write.compareAndReplace(current, updated)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.STALE_WRITE);
        }
        return MembershipMutationResult.applied(updated);
    }

    public MembershipMutationResult revokeMember(
            UserId actorUserId,
            long actorAuthRevision,
            WorkspaceId workspaceId,
            UserId targetUserId,
            long expectedTargetAuthRevision) {
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(targetUserId, "targetUserId");

        var auth =
                authorization.authorize(actorUserId, workspaceId, WorkspaceOperation.MANAGE_MEMBERS, actorAuthRevision);
        if (!auth.permitted()) {
            return MembershipMutationResult.authorizationDenied(auth.denialReason());
        }

        var target = access.findMembership(targetUserId, workspaceId);
        if (target.isEmpty() || !target.get().isActive()) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.TARGET_NOT_MEMBER);
        }
        var current = target.get();
        if (current.authRevision() != expectedTargetAuthRevision) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.STALE_WRITE);
        }
        if (wouldViolateLastOwner(workspaceId, current, null)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.LAST_OWNER_PROTECTED);
        }

        var updated = new WorkspaceMembership(
                current.userId(),
                current.workspaceId(),
                current.role(),
                MembershipStatus.REMOVED,
                current.authRevision() + 1);
        if (!write.compareAndReplace(current, updated)) {
            return MembershipMutationResult.mutationDenied(MembershipMutationDenialReason.STALE_WRITE);
        }
        return MembershipMutationResult.applied(updated);
    }

    private static boolean actorMayAssignRole(WorkspaceRole actorRole, WorkspaceRole assignedRole) {
        if (actorRole == WorkspaceRole.OWNER) {
            return true;
        }
        return assignedRole.rank() <= actorRole.rank();
    }

    private boolean wouldViolateLastOwner(WorkspaceId workspaceId, WorkspaceMembership target, WorkspaceRole newRole) {
        if (target.role() != WorkspaceRole.OWNER) {
            return false;
        }
        if (newRole == WorkspaceRole.OWNER) {
            return false;
        }
        return write.countActiveOwners(workspaceId) <= 1;
    }

    private static long initialRevision(Optional<WorkspaceMembership> prior) {
        return prior.map(m -> m.authRevision() + 1).orElse(1L);
    }
}