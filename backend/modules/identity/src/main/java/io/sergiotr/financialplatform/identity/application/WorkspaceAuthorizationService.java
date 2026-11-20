package io.sergiotr.financialplatform.identity.application;

import io.sergiotr.financialplatform.identity.domain.AuthorizationDecision;
import io.sergiotr.financialplatform.identity.domain.AuthorizationDenialReason;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceOperation;
import io.sergiotr.financialplatform.identity.port.WorkspaceAccessReadPort;
import java.util.Objects;

/**
 * Deny-by-default workspace authorization for synthetic DEV/TEST fixtures (no login, session, or transport).
 */
public final class WorkspaceAuthorizationService {

    private final WorkspaceAccessReadPort access;

    public WorkspaceAuthorizationService(WorkspaceAccessReadPort access) {
        this.access = Objects.requireNonNull(access, "access");
    }

    public AuthorizationDecision authorize(
            UserId subjectUserId, WorkspaceId workspaceId, WorkspaceOperation operation, long clientAuthRevision) {

        Objects.requireNonNull(subjectUserId, "subjectUserId");
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(operation, "operation");
        if (clientAuthRevision < 0) {
            throw new IllegalArgumentException("clientAuthRevision");
        }

        var workspace = access.findWorkspace(workspaceId);
        if (workspace.isEmpty()) {
            return AuthorizationDecision.deny(AuthorizationDenialReason.UNKNOWN_WORKSPACE);
        }
        if (!workspace.get().permitsAccess()) {
            return AuthorizationDecision.deny(AuthorizationDenialReason.WORKSPACE_NOT_ACCESSIBLE);
        }

        var membership = access.findMembership(subjectUserId, workspaceId);
        if (membership.isEmpty()) {
            return AuthorizationDecision.deny(AuthorizationDenialReason.FOREIGN_WORKSPACE);
        }
        var member = membership.get();
        if (!member.isActive()) {
            return AuthorizationDecision.deny(AuthorizationDenialReason.MEMBERSHIP_REVOKED);
        }
        if (clientAuthRevision != member.authRevision()) {
            if (clientAuthRevision < member.authRevision()) {
                return AuthorizationDecision.deny(AuthorizationDenialReason.STALE_AUTH_CONTEXT);
            }
            return AuthorizationDecision.deny(AuthorizationDenialReason.FUTURE_AUTH_CONTEXT);
        }

        if (!member.role().isAtLeast(operation.minimumRole())) {
            return AuthorizationDecision.deny(AuthorizationDenialReason.INSUFFICIENT_ROLE);
        }

        return AuthorizationDecision.permit();
    }
}