package io.sergiotr.financialplatform.identity.domain;

/** Synthetic-only transition triggers; evidence is verified server-side, never client booleans. */
public enum AuthenticationTransitionEvent {
    VERIFY_PRIMARY_AUTH,
    DIRECT_MFA_CHALLENGE,
    DIRECT_MFA_ENROLLMENT,
    VERIFY_MFA,
    COMPLETE_MFA_ENROLLMENT,
    ENTER_RECOVERY,
    EXPIRE
}