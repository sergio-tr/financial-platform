package io.sergiotr.financialplatform.identity.port;

import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.domain.Workspace;
import io.sergiotr.financialplatform.identity.domain.WorkspaceId;
import io.sergiotr.financialplatform.identity.domain.WorkspaceMembership;
import java.util.Optional;

public interface WorkspaceAccessReadPort {

    Optional<Workspace> findWorkspace(WorkspaceId workspaceId);

    Optional<WorkspaceMembership> findMembership(UserId userId, WorkspaceId workspaceId);
}