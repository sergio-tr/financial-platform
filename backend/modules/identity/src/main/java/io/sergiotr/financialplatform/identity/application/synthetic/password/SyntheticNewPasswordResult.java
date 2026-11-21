package io.sergiotr.financialplatform.identity.application.synthetic.password;

import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordPolicyViolation;
import java.util.Optional;

public record SyntheticNewPasswordResult(
        boolean accepted, String encodedHash, SyntheticPasswordPolicyViolation reason) {

    public static SyntheticNewPasswordResult hashed(String encodedHash) {
        return new SyntheticNewPasswordResult(true, encodedHash, null);
    }

    public static SyntheticNewPasswordResult rejected(SyntheticPasswordPolicyViolation reason) {
        return new SyntheticNewPasswordResult(false, null, reason);
    }

    public Optional<SyntheticPasswordPolicyViolation> violationReason() {
        return Optional.ofNullable(reason);
    }
}