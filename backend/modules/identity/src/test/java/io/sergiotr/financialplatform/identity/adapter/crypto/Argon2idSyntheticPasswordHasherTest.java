package io.sergiotr.financialplatform.identity.adapter.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class Argon2idSyntheticPasswordHasherTest {

    private final Argon2idSyntheticPasswordHasher hasher = new Argon2idSyntheticPasswordHasher();

    @Test
    void encodeProducesDistinctSaltsAndParsableParameters() {
        String password = "valid-password!!";
        String first = hasher.encode(password);
        String second = hasher.encode(password);
        assertNotEquals(first, second);
        assertTrue(first.startsWith("$argon2id$"));
        assertTrue(first.contains("m=" + Argon2idSyntheticPasswordHasher.MEMORY_COST_KIB));
        assertTrue(first.contains("t=" + Argon2idSyntheticPasswordHasher.ITERATIONS));
        assertTrue(first.contains("p=1"));
        assertTrue(hasher.matches(password, first));
        assertTrue(hasher.matches(password, second));
        assertFalse(hasher.matches("wrong", first));
    }

    @Test
    void matchesRejectsMalformedHashes() {
        assertFalse(hasher.matches("any-password", "$argon2id$malformed"));
        assertFalse(hasher.matches("any-password", "not-a-hash"));
        assertFalse(hasher.matches("any-password", ""));
    }

    @Test
    void freshHashDoesNotRequireUpgrade() {
        String encoded = hasher.encode("upgrade-check-password");
        assertFalse(hasher.requiresUpgrade(encoded));
        assertEquals(
                encoded,
                hasher.upgradeAfterVerifiedMatch(encoded, "upgrade-check-password").orElseThrow());
    }

    @Test
    void upgradeAfterVerifiedMatchRejectsUnverifiedPassword() {
        String encoded = hasher.encode("known-password");
        assertTrue(hasher.upgradeAfterVerifiedMatch(encoded, "forged-password").isEmpty());
    }

    @Test
    void matchesRejectsWrongVariantOrParameters() {
        String valid = hasher.encode("param-check");
        assertTrue(hasher.matches("param-check", valid));
        assertFalse(hasher.matches("param-check", valid.replace("$argon2id$", "$argon2i$")));
        assertFalse(hasher.matches("param-check", "$argon2id$v=19$m=4096,t=2,p=1$salt$hash"));
    }

    @Test
    void verifyLegacyShortPasswordWithoutPolicy() {
        String shortPassword = "legacy-short";
        String encoded = hasher.encode(shortPassword);
        assertTrue(hasher.matches(shortPassword, encoded));
    }

    @Test
    void unicodePasswordRoundTrip() {
        String emojiPassword = "🔒".repeat(8) + "abcdefg";
        assertEquals(15, emojiPassword.codePointCount(0, emojiPassword.length()));
        String encoded = hasher.encode(emojiPassword);
        assertTrue(hasher.matches(emojiPassword, encoded));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SYNTHETIC_ARGON2_CALIBRATION", matches = "true")
    void calibrationWithinHostTargetWindow() {
        String password = "calibration-probe-password";
        long start = System.nanoTime();
        String encoded = hasher.encode(password);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(hasher.matches(password, encoded));
        assertTrue(elapsedMs >= 200, "elapsedMs=" + elapsedMs);
        assertTrue(elapsedMs <= 2_500, "elapsedMs=" + elapsedMs);
    }
}