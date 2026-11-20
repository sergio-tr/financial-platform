package io.sergiotr.financialplatform.identity.domain;

/**
 * SER-92 workspace canonical enum {@code WorkspaceRole}.
 */
public enum WorkspaceRole {
    VIEWER,
    MEMBER,
    ADMIN,
    OWNER;

    public boolean isAtLeast(WorkspaceRole required) {
        return rank() >= required.rank();
    }

    int rank() {
        return switch (this) {
            case VIEWER -> 0;
            case MEMBER -> 1;
            case ADMIN -> 2;
            case OWNER -> 3;
        };
    }
}