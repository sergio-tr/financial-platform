package io.sergiotr.financialplatform.identity.domain;

/** Violations for new-password policy only (verify path is not subject to these rules). */
public enum SyntheticPasswordPolicyViolation {
    TOO_SHORT,
    TOO_LONG,
    BLOCKED_COMMON_PASSWORD
}