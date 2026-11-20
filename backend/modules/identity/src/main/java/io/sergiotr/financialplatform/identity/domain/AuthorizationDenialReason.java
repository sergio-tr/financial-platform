package io.sergiotr.financialplatform.identity.domain;

public enum AuthorizationDenialReason {
    UNKNOWN_WORKSPACE,
    WORKSPACE_NOT_ACCESSIBLE,
    FOREIGN_WORKSPACE,
    MEMBERSHIP_REVOKED,
    STALE_AUTH_CONTEXT,
    FUTURE_AUTH_CONTEXT,
    INSUFFICIENT_ROLE
}