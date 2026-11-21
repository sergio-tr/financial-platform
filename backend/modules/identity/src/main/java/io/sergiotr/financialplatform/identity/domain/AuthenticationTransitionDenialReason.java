package io.sergiotr.financialplatform.identity.domain;

public enum AuthenticationTransitionDenialReason {
    NONE,
    INVALID_TRANSITION,
    EXPIRED,
    DISABLED,
    INSUFFICIENT_EVIDENCE,
    STALE
}