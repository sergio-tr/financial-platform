package io.sergiotr.financialplatform.identity.domain;

/** Host-local authentication progression (SER-92 AuthenticationStage); no transport/session in this slice. */
public enum AuthenticationStage {
    ANONYMOUS,
    PRIMARY_AUTH_VERIFIED,
    MFA_REQUIRED,
    MFA_ENROLLMENT_REQUIRED,
    AUTHENTICATED,
    RECOVERY_REQUIRED;

    public SecurityAssuranceStatus toAssuranceStatus() {
        return switch (this) {
            case ANONYMOUS -> SecurityAssuranceStatus.ANONYMOUS;
            case AUTHENTICATED -> SecurityAssuranceStatus.AUTHENTICATED_FULL;
            case PRIMARY_AUTH_VERIFIED, MFA_REQUIRED, MFA_ENROLLMENT_REQUIRED, RECOVERY_REQUIRED ->
                    SecurityAssuranceStatus.PRE_MFA;
        };
    }
}