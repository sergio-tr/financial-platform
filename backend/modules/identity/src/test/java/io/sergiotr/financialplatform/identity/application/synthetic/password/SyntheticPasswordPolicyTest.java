package io.sergiotr.financialplatform.identity.application.synthetic.password;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordPolicyViolation;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SyntheticPasswordPolicyTest {

    private final SyntheticPasswordPolicy policy =
            new SyntheticPasswordPolicy(Set.of("password", "qwerty"));

    @Test
    void acceptsFifteenCodePointsWithoutNormalization() {
        String fifteen = "a".repeat(15);
        assertTrue(policy.evaluateNewPassword(fifteen).permitted());
        assertTrue(policy.evaluateNewPassword(" " + fifteen).permitted());
    }

    @Test
    void rejectsTooShortAndTooLong() {
        assertFalse(policy.evaluateNewPassword("short").permitted());
        assertTrue(
                policy.evaluateNewPassword("short")
                        .violationReason()
                        .filter(v -> v == SyntheticPasswordPolicyViolation.TOO_SHORT)
                        .isPresent());
        String tooLong = "x".repeat(SyntheticPasswordPolicy.MAX_CODE_POINTS + 1);
        assertFalse(policy.evaluateNewPassword(tooLong).permitted());
    }

    @Test
    void rejectsBlockedCommonPasswordCaseInsensitive() {
        assertFalse(policy.evaluateNewPassword("QwErTy").permitted());
        assertFalse(policy.evaluateNewPassword("qwerty").permitted());
    }
}