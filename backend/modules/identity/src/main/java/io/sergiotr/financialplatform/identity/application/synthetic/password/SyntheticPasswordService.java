package io.sergiotr.financialplatform.identity.application.synthetic.password;

import io.sergiotr.financialplatform.identity.port.SyntheticPasswordHashPort;
import java.util.Objects;

/**
 * Coordinates new-password policy with Argon2id hashing. Successful verify does not elevate auth assurance.
 */
public final class SyntheticPasswordService {

    private final SyntheticPasswordPolicy policy;
    private final SyntheticPasswordHashPort hasher;

    public SyntheticPasswordService(SyntheticPasswordPolicy policy, SyntheticPasswordHashPort hasher) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

  /** Hash a new password after policy checks. */
    public SyntheticNewPasswordResult hashNewPassword(String rawPassword) {
        var decision = policy.evaluateNewPassword(rawPassword);
        if (!decision.permitted()) {
            return SyntheticNewPasswordResult.reject(decision.violationReason().orElseThrow());
        }
        return SyntheticNewPasswordResult.hashed(hasher.encode(rawPassword));
    }

    /** Verify an existing stored hash; policy is intentionally not applied. */
    public boolean verifyExistingPassword(String rawPassword, String encodedHash) {
        return hasher.matches(rawPassword, encodedHash);
    }
}