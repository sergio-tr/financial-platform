package io.sergiotr.financialplatform.identity.application;

import io.sergiotr.financialplatform.identity.domain.AuthorizationDecision;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.MembershipMutationResult;
import io.sergiotr.financialplatform.identity.domain.TrustedWorkspaceActor;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.domain.WorkspaceRole;
import io.sergiotr.financialplatform.identity.port.WorkspaceActorContextPort;
import java.util.Objects;
import java.util.Optional;

/**
 * Application entry that derives the acting user only from {@link WorkspaceActorContextPort}, never from caller
 * parameters representing identity proof.
 */
public final class TrustedWorkspaceActorBoundary {

    private final WorkspaceActorContextPort actorContext;
    private final WorkspaceAuthorizationService authorization;
    private final WorkspaceMembershipLifecycleService membershipLifecycle;

    public TrustedWorkspaceActorBoundary(
            WorkspaceActorContextPort actorContext,
            WorkspaceAuthorizationService authorization,
            WorkspaceMembershipLifecycleService membershipLifecycle) {
        this.actorContext = Objects.requireNonNull(actorContext, "actorContext");
        this.authorization = Objects.requireNonNull(authorization, "authorization");
        this.membershipLifecycle =
                Objects.requireNonNull(membershipLifecycle, "membershipLifecycle");
    }

    public AuthorizationDecision authorize(WorkspaceId workspaceId, WorkspaceOperation operation) {
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(operation, "operation");
        return resolveAssuredActor()
                .map(actor -> authorization.authorize(actor.userId(), workspaceId, operation, actor.authRevision()))
                .orElseGet(this::unauthenticatedDecision);
    }

    public MembershipMutationResult addMember(
            WorkspaceId workspaceId, UserId newMemberUserId, WorkspaceRole role) {
        return resolveAssuredActor()
                .map(actor -> membershipLifecycle.addMember(
                        actor.userId(), actor.authRevision(), workspaceId, newMemberUserId, role))
                .orElseGet(this::unauthenticatedMutation);
    }

    public MembershipMutationResult changeRole(
            WorkspaceId workspaceId,
            UserId targetUserId,
            WorkspaceRole newRole,
            long expectedTargetAuthRevision) {
        return resolveAssuredActor()
                .map(actor -> membershipLifecycle.changeRole(
                        actor.userId(),
                        actor.authRevision(),
                        workspaceId,
                        targetUserId,
                        newRole,
                        expectedTargetAuthRevision))
                .orElseGet(this::unauthenticatedMutation);
    }

    public MembershipMutationResult revokeMember(
            WorkspaceId workspaceId, UserId targetUserId, long expectedTargetAuthRevision) {
        return resolveAssuredActor()
                .map(actor -> membershipLifecycle.revokeMember(
                        actor.userId(),
                        actor.authRevision(),
                        workspaceId,
                        targetUserId,
                        expectedTargetAuthRevision))
                .orElseGet(this::unauthenticatedMutation);
    }

    private Optional<TrustedWorkspaceActor> resolveAssuredActor() {
        var actor = actorContext.currentActor();
        if (actor.isEmpty()) {
            return Optional.empty();
        }
        var current = actor.get();
        if (current.assurance() == SecurityAssuranceStatus.ANONYMOUS) {
            return Optional.empty();
        }
        if (!current.hasWorkspaceAssurance()) {
            return Optional.empty();
        }
        return Optional.of(current);
    }

    private AuthorizationDecision unauthenticatedDecision() {
        var actor = actorContext.currentActor();
        if (actor.isPresent() && actor.get().assurance() == SecurityAssuranceStatus.PRE_MFA) {
            return AuthorizationDecision.deny(AuthorizationDenialReason.INSUFFICIENT_ASSURANCE);
        }
        return AuthorizationDecision.deny(AuthorizationDenialReason.UNAUTHENTICATED);
    }

    private MembershipMutationResult unauthenticatedMutation() {
        var denied = unauthenticatedDecision();
        return MembershipMutationResult.authorizationDenied(denied.denialReason());
    }
}