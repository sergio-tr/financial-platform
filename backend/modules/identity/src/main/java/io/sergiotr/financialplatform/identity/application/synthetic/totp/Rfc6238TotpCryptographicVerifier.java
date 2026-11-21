package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * RFC 6238 TOTP (HMAC-SHA1, 30s step, six digits). SER-24 Iteration 16 parameters.
 * Cryptographic match only; does not complete MFA without durable CAS.
 */
public final class Rfc6238TotpCryptographicVerifier {

    public static final int TIME_STEP_SECONDS = 30;
    public static final int PERMITTED_WINDOW_STEPS = 1;
    public static final int CODE_DIGITS = 6;

    private final Clock clock;

    public Rfc6238TotpCryptographicVerifier(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Greatest admissible matching step strictly greater than {@code lastAcceptedStep}, if any.
     */
    public OptionalLong findGreatestMatchingStep(TotpSeed seed, String totpCode, long lastAcceptedStep) {
        Objects.requireNonNull(seed, "seed");
        if (!isValidTotpCode(totpCode)) {
            return OptionalLong.empty();
        }
        long currentStep = epochSecondToStep(clock.instant());
        long best = Long.MIN_VALUE;
        byte[] key = seed.copyMaterial();
        try {
            for (long offset = -PERMITTED_WINDOW_STEPS; offset <= PERMITTED_WINDOW_STEPS; offset++) {
                long candidate = currentStep + offset;
                if (candidate <= lastAcceptedStep) {
                    continue;
                }
                String expected = computeTotpCode(key, candidate);
                if (constantTimeEquals(expected, totpCode) && candidate > best) {
                    best = candidate;
                }
            }
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
        }
        return best == Long.MIN_VALUE ? OptionalLong.empty() : OptionalLong.of(best);
    }

    /** True when {@code totpCode} matches a permitted-window step but none are strictly after {@code lastAcceptedStep}. */
    public boolean matchesOnlyStaleSteps(TotpSeed seed, String totpCode, long lastAcceptedStep) {
        long currentStep = epochSecondToStep(clock.instant());
        byte[] key = seed.copyMaterial();
        try {
            for (long offset = -PERMITTED_WINDOW_STEPS; offset <= PERMITTED_WINDOW_STEPS; offset++) {
                long candidate = currentStep + offset;
                if (candidate > lastAcceptedStep) {
                    continue;
                }
                if (constantTimeEquals(computeTotpCode(key, candidate), totpCode)) {
                    return true;
                }
            }
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
        }
        return false;
    }

    public boolean matchesWithExpandedWindow(TotpSeed seed, String totpCode, int extraStepsBeyondPermitted) {
        long currentStep = epochSecondToStep(clock.instant());
        byte[] key = seed.copyMaterial();
        try {
            int limit = PERMITTED_WINDOW_STEPS + extraStepsBeyondPermitted;
            for (long offset = -limit; offset <= limit; offset++) {
                long candidate = currentStep + offset;
                if (candidate < 0) {
                    continue;
                }
                if (constantTimeEquals(computeTotpCode(key, candidate), totpCode)) {
                    return true;
                }
            }
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
        }
        return false;
    }

  /** RFC 6238 / known-test helper: compute code for an explicit epoch second. */
    public OptionalInt codeAtEpochSecond(TotpSeed seed, long epochSecond) {
        Objects.requireNonNull(seed, "seed");
        byte[] key = seed.copyMaterial();
        try {
            long step = epochSecondToStep(Instant.ofEpochSecond(epochSecond));
            return OptionalInt.of(Integer.parseInt(computeTotpCode(key, step)));
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
        }
    }

    public static boolean isValidTotpCode(String totpCode) {
        if (totpCode == null || totpCode.length() != CODE_DIGITS) {
            return false;
        }
        for (int i = 0; i < CODE_DIGITS; i++) {
            char c = totpCode.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    static long epochSecondToStep(Instant instant) {
        long seconds = instant.getEpochSecond();
        if (seconds < 0) {
            throw new IllegalArgumentException("negative epoch");
        }
        return Math.floorDiv(seconds, TIME_STEP_SECONDS);
    }

    static String computeTotpCode(byte[] key, long timeStep) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(longToBytes(timeStep));
            int offset = hash[hash.length - 1] & 0x0f;
            int binary =
                    ((hash[offset] & 0x7f) << 24)
                            | ((hash[offset + 1] & 0xff) << 16)
                            | ((hash[offset + 2] & 0xff) << 8)
                            | (hash[offset + 3] & 0xff);
            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HmacSHA1 unavailable", e);
        }
    }

    private static byte[] longToBytes(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(java.nio.charset.StandardCharsets.US_ASCII), b.getBytes(
                java.nio.charset.StandardCharsets.US_ASCII));
    }
}