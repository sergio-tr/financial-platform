package io.sergiotr.financialplatform.identity.domain;

/** SER-92 canonical enum {@code WorkspaceStatus}. */
public enum WorkspaceStatus {
    ACTIVE,
    DELETION_REQUESTED,
    DELETING,
    DELETED
}