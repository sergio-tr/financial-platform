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

    public static final String FORMAT_ID = "spring-security-argon2id-v5_8";

    private final PasswordEncoder delegate;

    public Argon2idSyntheticPasswordHasher() {
        this(Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
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
        if (!encodedHash.startsWith("$argon2")) {
            return false;
        }
        try {
            return delegate.matches(rawPassword, encodedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
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
    public String upgrade(String encodedHash, String rawPassword) {
        Objects.requireNonNull(encodedHash, "encodedHash");
        Objects.requireNonNull(rawPassword, "rawPassword");
        if (requiresUpgrade(encodedHash)) {
            return encode(rawPassword);
        }
        return encodedHash;
    }
}