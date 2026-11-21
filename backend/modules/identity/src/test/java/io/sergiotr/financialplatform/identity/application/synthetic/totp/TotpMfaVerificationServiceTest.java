package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.FailClosedAuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import io.sergiotr.financialplatform.identity.port.AuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.port.TotpAuthenticatorStatusReadPort;
import io.sergiotr.financialplatform.identity.port.TotpSecretReadPort;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TotpMfaVerificationServiceTest {

    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    private static final AuthenticatorId AUTH = AuthenticatorId.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));

    @Test
    void failClosedWhenDurableCasNotConfigured() {
        long epoch = 1_234_567_890L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var crypto = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format("%06d", crypto.codeAtEpochSecond(new TotpSeed(RFC_SECRET), epoch).orElseThrow());
        var service = new TotpMfaVerificationService(
                clock,
                id -> Optional.of(new TotpSeed(RFC_SECRET)),
                id -> TotpAuthenticatorStatusReadPort.Status.ACTIVE,
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(
                TotpVerificationOutcome.REPLAY_PERSISTENCE_NOT_CONFIGURED,
                service.verify(AUTH, code, -1));
    }

    @Test
    void fakeInMemoryCasAcceptsOnceNotProductionProof() {
        long epoch = 1_234_567_890L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var crypto = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format("%06d", crypto.codeAtEpochSecond(new TotpSeed(RFC_SECRET), epoch).orElseThrow());
        var fakeCas = new UnitTestOnlyInMemoryStepCas();
        var service = new TotpMfaVerificationService(
                clock,
                id -> Optional.of(new TotpSeed(RFC_SECRET)),
                id -> TotpAuthenticatorStatusReadPort.Status.ACTIVE,
                fakeCas);
        assertEquals(TotpVerificationOutcome.ACCEPTED, service.verify(AUTH, code, -1));
        assertEquals(TotpVerificationOutcome.REPLAY_STEP_ALREADY_ACCEPTED, service.verify(AUTH, code, fakeCas.lastAccepted()));
    }

    @Test
    void deniesInactiveAuthenticatorAndUnknownSecret() {
        var clock = Clock.fixed(Instant.ofEpochSecond(1_234_567_890L), ZoneOffset.UTC);
        var service = new TotpMfaVerificationService(
                clock,
                id -> Optional.empty(),
                id -> TotpAuthenticatorStatusReadPort.Status.ACTIVE,
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.SECRET_UNAVAILABLE, service.verify(AUTH, "005924", -1));

        var serviceInactive = new TotpMfaVerificationService(
                clock,
                id -> Optional.of(new TotpSeed(RFC_SECRET)),
                id -> TotpAuthenticatorStatusReadPort.Status.REVOKED,
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_INACTIVE, serviceInactive.verify(AUTH, "005924", -1));
    }

    /**
     * Test-only fake; NOT concurrent replay proof (labeled in tests).
     */
    static final class UnitTestOnlyInMemoryStepCas implements AuthenticatorStepCompareAndSetPort {
        private final Map<AuthenticatorId, AtomicLong> steps = new HashMap<>();

        @Override
        public CasAttemptResult compareAndSetAcceptedStep(
                AuthenticatorId authenticatorId, long matchedStep, long observedLastAcceptedStep) {
            var holder = steps.computeIfAbsent(authenticatorId, id -> new AtomicLong(-1));
            if (holder.get() != observedLastAcceptedStep) {
                return new CasAttemptResult(CasOutcome.STALE_RACE, holder.get());
            }
            if (matchedStep <= holder.get()) {
                return new CasAttemptResult(CasOutcome.STALE_RACE, holder.get());
            }
            holder.set(matchedStep);
            return new CasAttemptResult(CasOutcome.ACCEPTED, matchedStep);
        }

        long lastAccepted() {
            return steps.getOrDefault(AUTH, new AtomicLong(-1)).get();
        }
    }
}