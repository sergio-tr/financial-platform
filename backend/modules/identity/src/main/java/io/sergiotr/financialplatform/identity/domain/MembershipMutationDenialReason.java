package io.sergiotr.financialplatform.identity.domain;

/** Denial for synthetic membership writes (not transport-authenticated). */
public enum MembershipMutationDenialReason {
    AUTHORIZATION_DENIED,
    TARGET_NOT_MEMBER,
    STALE_WRITE,
    PRIVILEGE_ESCALATION,
    LAST_OWNER_PROTECTED,
    DUPLICATE_ACTIVE_MEMBERSHIP
}