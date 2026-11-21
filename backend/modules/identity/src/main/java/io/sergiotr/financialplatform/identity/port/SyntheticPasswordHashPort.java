package io.sergiotr.financialplatform.identity.port;

/**
 * Synthetic DEV/TEST password hashing (Argon2id). Not wired to HTTP login or full authentication assurance.
 */
public interface SyntheticPasswordHashPort {

    /** Encoded hash using the active versioned Argon2id parameters (SER-24 I16 floor). */
    String encode(String rawPassword);

    /** Constant-time aware verify; does not apply new-password policy. */
    boolean matches(String rawPassword, String encodedHash);

    boolean requiresUpgrade(String encodedHash);

    /**
     * Re-encodes with current parameters only after a successful {@link #matches(String, String)} for the same inputs.
     * Never mints a replacement hash from an unverified password.
     */
    java.util.Optional<String> upgradeAfterVerifiedMatch(String encodedHash, String rawPassword);
}