package io.sergiotr.financialplatform.identity.domain;

/** Synthetic stand-in for host-local assurance tiers (no transport/session implementation in V1 slice). */
public enum SecurityAssuranceStatus {
    ANONYMOUS,
    PRE_MFA,
    AUTHENTICATED_FULL
}