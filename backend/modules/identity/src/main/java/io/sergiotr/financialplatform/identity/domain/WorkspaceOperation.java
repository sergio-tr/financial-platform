package io.sergiotr.financialplatform.identity.domain;

/**
 * Scoped workspace operation requiring a minimum membership role (synthetic vertical; not a public API contract).
 */
public enum WorkspaceOperation {
    VIEW_WORKSPACE(WorkspaceRole.VIEWER),
    MANAGE_MEMBERS(WorkspaceRole.ADMIN),
    CHANGE_WORKSPACE_SETTINGS(WorkspaceRole.ADMIN),
    REQUEST_WORKSPACE_DELETION(WorkspaceRole.OWNER);

    private final WorkspaceRole minimumRole;

    WorkspaceOperation(WorkspaceRole minimumRole) {
        this.minimumRole = minimumRole;
    }

    public WorkspaceRole minimumRole() {
        return minimumRole;
    }
}