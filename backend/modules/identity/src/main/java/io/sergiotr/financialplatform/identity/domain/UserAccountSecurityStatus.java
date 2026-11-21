package io.sergiotr.financialplatform.identity.domain;

/** Server-owned account security status for synthetic challenge/TOTP gates (not client-supplied). */
public enum UserAccountSecurityStatus {
    ENABLED,
    DISABLED,
    LOCKED,
    REVOKED;

    public boolean permitsMfaChallenge() {
        return this == ENABLED;
    }
}