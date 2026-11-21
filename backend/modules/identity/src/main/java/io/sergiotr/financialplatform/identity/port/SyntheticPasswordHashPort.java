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

    String upgrade(String encodedHash);
}