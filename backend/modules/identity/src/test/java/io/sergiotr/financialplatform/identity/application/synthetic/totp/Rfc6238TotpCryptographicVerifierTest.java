package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Rfc6238TotpCryptographicVerifierTest {

    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    static Stream<Arguments> rfcVectors() {
        return Stream.of(
                Arguments.of(59L, "287082"),
                Arguments.of(1_111_111_109L, "081804"),
                Arguments.of(1_111_111_111L, "050471"),
                Arguments.of(1_234_567_890L, "005924"),
                Arguments.of(2_000_000_000L, "279037"),
                Arguments.of(20_000_000_000L, "353130"));
    }

    @ParameterizedTest
    @MethodSource("rfcVectors")
    void rfc6238PublicVectors(long epochSecond, String expectedSixDigit) {
        var clock = Clock.fixed(Instant.ofEpochSecond(epochSecond), ZoneOffset.UTC);
        var verifier = new Rfc6238TotpCryptographicVerifier(clock);
        var seed = new TotpSeed(RFC_SECRET);
        var code = verifier.codeAtEpochSecond(seed, epochSecond);
        assertTrue(code.isPresent());
        assertEquals(Integer.parseInt(expectedSixDigit), code.getAsInt());
        assertEquals(
                expectedSixDigit,
                String.format("%06d", code.getAsInt()));
    }

    @Test
    void greatestMatchingStepWithinWindow() {
        long epoch = 1_234_567_890L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var verifier = new Rfc6238TotpCryptographicVerifier(clock);
        var seed = new TotpSeed(RFC_SECRET);
        String code = "005924";
        long step = Rfc6238TotpCryptographicVerifier.epochSecondToStep(Instant.ofEpochSecond(epoch));
        assertTrue(verifier.findGreatestMatchingStep(seed, code, step - 1).orElse(-1) >= step);
        assertTrue(verifier.findGreatestMatchingStep(seed, code, step).isEmpty());
    }

    @Test
    void rejectsCodeOutsidePermittedWindow() {
        long epoch = 1_234_567_890L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var verifier = new Rfc6238TotpCryptographicVerifier(clock);
        var seed = new TotpSeed(RFC_SECRET);
        long step = Rfc6238TotpCryptographicVerifier.epochSecondToStep(Instant.ofEpochSecond(epoch));
        String twoStepsAhead =
                Rfc6238TotpCryptographicVerifier.computeTotpCode(RFC_SECRET.clone(), step + 2);
        assertFalse(verifier.findGreatestMatchingStep(seed, twoStepsAhead, -1).isPresent());
        assertTrue(verifier.matchesWithExpandedWindow(seed, twoStepsAhead, 1));
    }

    @Test
    void rejectsMalformedCodes() {
        var verifier = new Rfc6238TotpCryptographicVerifier(Clock.systemUTC());
        var seed = new TotpSeed(RFC_SECRET);
        assertFalse(Rfc6238TotpCryptographicVerifier.isValidTotpCode(null));
        assertFalse(Rfc6238TotpCryptographicVerifier.isValidTotpCode("12345"));
        assertFalse(Rfc6238TotpCryptographicVerifier.isValidTotpCode("1234567"));
        assertFalse(Rfc6238TotpCryptographicVerifier.isValidTotpCode("12a456"));
        assertTrue(verifier.findGreatestMatchingStep(seed, "12a456", -1).isEmpty());
    }

    @Test
    void leadingZerosPreserved() {
        long epoch = 1_111_111_109L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var verifier = new Rfc6238TotpCryptographicVerifier(clock);
        assertEquals("081804", String.format("%06d", verifier.codeAtEpochSecond(new TotpSeed(RFC_SECRET), epoch).orElseThrow()));
    }
}