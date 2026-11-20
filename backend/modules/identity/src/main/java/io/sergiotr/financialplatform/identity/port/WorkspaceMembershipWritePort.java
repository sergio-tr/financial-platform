package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceMembership;

public interface WorkspaceMembershipWritePort {

    int countActiveOwners(WorkspaceId workspaceId);

    /**
     * Inserts a new membership. Fails when an active membership already exists for the user in the workspace.
     */
    boolean insertMembership(WorkspaceMembership membership);

    /**
     * Replaces {@code expected} with {@code updated} only when the stored row matches {@code expected} exactly
     * (compare-and-set on {@code authRevision} and fields).
     */
    boolean compareAndReplace(WorkspaceMembership expected, WorkspaceMembership updated);
}