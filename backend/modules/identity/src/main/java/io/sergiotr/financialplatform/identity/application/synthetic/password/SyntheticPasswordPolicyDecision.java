package io.sergiotr.financialplatform.identity.application.synthetic.password;

import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordPolicyViolation;
import java.util.Optional;

public record SyntheticPasswordPolicyDecision(boolean accepted, SyntheticPasswordPolicyViolation reason) {

    public static SyntheticPasswordPolicyDecision accepted() {
        return new SyntheticPasswordPolicyDecision(true, null);
    }

    public static SyntheticPasswordPolicyDecision rejected(SyntheticPasswordPolicyViolation reason) {
        return new SyntheticPasswordPolicyDecision(false, reason);
    }

    public Optional<SyntheticPasswordPolicyViolation> violationReason() {
        return Optional.ofNullable(reason);
    }
}