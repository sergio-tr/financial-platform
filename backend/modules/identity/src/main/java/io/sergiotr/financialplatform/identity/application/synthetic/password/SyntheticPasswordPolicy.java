package io.sergiotr.financialplatform.identity.application.synthetic.password;

import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordPolicyViolation;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * SER-24 / SER-150 synthetic password policy for <em>new</em> passwords only.
 * No trim, Unicode normalization, or composition restrictions.
 */
public final class SyntheticPasswordPolicy {

    public static final int MIN_CODE_POINTS = 15;
    public static final int MAX_CODE_POINTS = 256;

    private final Set<String> blockedCommonPasswordsLowercase;

    public SyntheticPasswordPolicy(Set<String> blockedCommonPasswords) {
        Objects.requireNonNull(blockedCommonPasswords, "blockedCommonPasswords");
        this.blockedCommonPasswordsLowercase =
                Set.copyOf(blockedCommonPasswords.stream().map(SyntheticPasswordPolicy::toBlockKey).toList());
    }

    public SyntheticPasswordPolicyDecision evaluateNewPassword(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        int codePoints = rawPassword.codePointCount(0, rawPassword.length());
        if (codePoints < MIN_CODE_POINTS) {
            return SyntheticPasswordPolicyDecision.reject(SyntheticPasswordPolicyViolation.TOO_SHORT);
        }
        if (codePoints > MAX_CODE_POINTS) {
            return SyntheticPasswordPolicyDecision.reject(SyntheticPasswordPolicyViolation.TOO_LONG);
        }
        String blockKey = toBlockKey(rawPassword);
        if (blockedCommonPasswordsLowercase.contains(blockKey)) {
            return SyntheticPasswordPolicyDecision.reject(SyntheticPasswordPolicyViolation.BLOCKED_COMMON_PASSWORD);
        }
        return SyntheticPasswordPolicyDecision.permit();
    }

    private static String toBlockKey(String rawPassword) {
        return rawPassword.toLowerCase(Locale.ROOT);
    }
}