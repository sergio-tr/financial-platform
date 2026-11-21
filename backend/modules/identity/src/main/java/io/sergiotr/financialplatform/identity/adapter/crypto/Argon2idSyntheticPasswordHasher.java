package io.sergiotr.financialplatform.identity.adapter.crypto;

import io.sergiotr.financialplatform.identity.port.SyntheticPasswordHashPort;
import java.util.Objects;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Argon2id via Spring Security ({@link Argon2PasswordEncoder#defaultsForSpringSecurity_v5_8()}).
 * SER-24 iteration-16 floor: m=19MiB, t=2, p=1.
 */
public final class Argon2idSyntheticPasswordHasher implements SyntheticPasswordHashPort {

    /** SER-24 iteration-16 floor: 19 MiB memory cost in KiB. */
    public static final int MEMORY_COST_KIB = 19 * 1024;

    public static final int ITERATIONS = 2;

    public static final String FORMAT_ID = "spring-security-argon2id-ser24-i16";

    private final PasswordEncoder delegate;

    public Argon2idSyntheticPasswordHasher() {
        this(new Argon2PasswordEncoder(16, 32, 1, MEMORY_COST_KIB, ITERATIONS));
    }

    Argon2idSyntheticPasswordHasher(PasswordEncoder delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public String encode(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedHash) {
        if (rawPassword == null || encodedHash == null || encodedHash.isBlank()) {
            return false;
        }
        if (!isStrictArgon2idEncoding(encodedHash)) {
            return false;
        }
        try {
            return delegate.matches(rawPassword, encodedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Recognized Argon2id encodings use the variant prefix and SER-24 I16 floor parameters embedded by this hasher.
     * Legacy Spring Security Argon2id hashes with other parameters may still verify when {@link #requiresUpgrade} is
     * true; they are rejected here to avoid prefix-only acceptance.
     */
    boolean isStrictArgon2idEncoding(String encodedHash) {
        if (encodedHash == null || encodedHash.isBlank() || !encodedHash.startsWith("$argon2id$")) {
            return false;
        }
        return encodedHash.contains("m=" + MEMORY_COST_KIB)
                && encodedHash.contains("t=" + ITERATIONS)
                && encodedHash.contains("p=1");
    }

    @Override
    public boolean requiresUpgrade(String encodedHash) {
        if (encodedHash == null || encodedHash.isBlank()) {
            return true;
        }
        try {
            return delegate.upgradeEncoding(encodedHash);
        } catch (IllegalArgumentException ex) {
            return true;
        }
    }

    @Override
    public java.util.Optional<String> upgradeAfterVerifiedMatch(String encodedHash, String rawPassword) {
        Objects.requireNonNull(encodedHash, "encodedHash");
        Objects.requireNonNull(rawPassword, "rawPassword");
        if (!matches(rawPassword, encodedHash)) {
            return java.util.Optional.empty();
        }
        if (!requiresUpgrade(encodedHash)) {
            return java.util.Optional.of(encodedHash);
        }
        return java.util.Optional.of(encode(rawPassword));
    }
}