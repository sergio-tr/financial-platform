package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.FailClosedAuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.AuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.port.TotpAuthenticatorBindingReadPort;
import io.sergiotr.financialplatform.identity.port.TotpSecretReadPort;
import io.sergiotr.financialplatform.identity.port.TrustedMfaChallengeContextPort;
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
    private static final UserId OWNER = UserId.parse("10000000-0000-4000-8000-000000000001");
    private static final UserId FOREIGN = UserId.parse("10000000-0000-4000-8000-000000000099");

    private static TotpMfaVerificationService service(
            Clock clock,
            UserId trustedUser,
            TotpAuthenticatorBindingReadPort bindings,
            AuthenticatorStepCompareAndSetPort cas) {
        TrustedMfaChallengeContextPort challenge = () -> Optional.ofNullable(trustedUser);
        TotpSecretReadPort secrets = id -> Optional.of(new TotpSeed(RFC_SECRET));
        return new TotpMfaVerificationService(clock, challenge, secrets, bindings, cas);
    }

    private static TotpAuthenticatorBindingReadPort activeBinding(UserId owner, long lastAcceptedStep) {
        return id -> Optional.of(new TotpAuthenticatorBindingReadPort.Binding(
                owner, TotpAuthenticatorBindingReadPort.Status.ACTIVE, lastAcceptedStep, 0L));
    }

    @Test
    void failClosedWhenDurableCasNotConfigured() {
        long epoch = 1_234_567_890L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var crypto = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format("%06d", crypto.codeAtEpochSecond(new TotpSeed(RFC_SECRET), epoch).orElseThrow());
        var service = service(
                clock,
                OWNER,
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(
                TotpVerificationOutcome.REPLAY_PERSISTENCE_NOT_CONFIGURED, service.verify(AUTH, code));
    }

    @Test
    void fakeInMemoryCasAcceptsOnceNotProductionProof() {
        long epoch = 1_234_567_890L;
        var clock = Clock.fixed(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
        var crypto = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format("%06d", crypto.codeAtEpochSecond(new TotpSeed(RFC_SECRET), epoch).orElseThrow());
        var fakeCas = new UnitTestOnlyInMemoryStepCas(OWNER);
        var service = service(clock, OWNER, activeBinding(OWNER, -1), fakeCas);
        assertEquals(TotpVerificationOutcome.ACCEPTED, service.verify(AUTH, code));
        assertEquals(
                TotpVerificationOutcome.REPLAY_STEP_ALREADY_ACCEPTED,
                service.verify(AUTH, code));
    }

    @Test
    void deniesInactiveAuthenticatorAndUnknownSecret() {
        var clock = Clock.fixed(Instant.ofEpochSecond(1_234_567_890L), ZoneOffset.UTC);
        var missingSecret = service(
                clock,
                OWNER,
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        TotpSecretReadPort noSecret = id -> Optional.empty();
        var serviceNoSecret = new TotpMfaVerificationService(
                clock,
                () -> Optional.of(OWNER),
                noSecret,
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.SECRET_UNAVAILABLE, serviceNoSecret.verify(AUTH, "005924"));

        TotpAuthenticatorBindingReadPort revoked =
                id -> Optional.of(new TotpAuthenticatorBindingReadPort.Binding(
                        OWNER, TotpAuthenticatorBindingReadPort.Status.REVOKED, -1, 0));
        var serviceInactive = service(clock, OWNER, revoked, new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_INACTIVE, serviceInactive.verify(AUTH, "005924"));
        assertNotNull(missingSecret);
    }

    @Test
    void deniesMissingTrustedChallengeAndOwnerMismatch() {
        var clock = Clock.fixed(Instant.ofEpochSecond(1_234_567_890L), ZoneOffset.UTC);
        var noChallenge = new TotpMfaVerificationService(
                clock,
                Optional::empty,
                id -> Optional.of(new TotpSeed(RFC_SECRET)),
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.TRUSTED_CHALLENGE_MISSING, noChallenge.verify(AUTH, "005924"));

        var foreignActor = service(
                clock,
                FOREIGN,
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_OWNER_MISMATCH, foreignActor.verify(AUTH, "005924"));
    }

    @Test
    void deniesUnknownAuthenticator() {
        var clock = Clock.fixed(Instant.ofEpochSecond(1_234_567_890L), ZoneOffset.UTC);
        TotpAuthenticatorBindingReadPort unknown = id -> Optional.empty();
        var service = service(clock, OWNER, unknown, new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_UNKNOWN, service.verify(AUTH, "005924"));
    }

    /**
     * Test-only fake; NOT concurrent replay proof (labeled in tests).
     */
    static final class UnitTestOnlyInMemoryStepCas implements AuthenticatorStepCompareAndSetPort {
        private final UserId owner;
        private final Map<AuthenticatorId, AtomicLong> steps = new HashMap<>();
        private final Map<AuthenticatorId, Long> versions = new HashMap<>();

        UnitTestOnlyInMemoryStepCas(UserId owner) {
            this.owner = owner;
        }

        @Override
        public CasAttemptResult compareAndSetAcceptedStep(
                AuthenticatorId authenticatorId,
                UserId expectedOwnerUserId,
                long matchedStep,
                long observedLastAcceptedStep,
                long observedReplayStateVersion) {
            if (!owner.equals(expectedOwnerUserId)) {
                return new CasAttemptResult(CasOutcome.OWNER_MISMATCH, observedLastAcceptedStep);
            }
            long currentVersion = versions.getOrDefault(authenticatorId, 0L);
            if (currentVersion != observedReplayStateVersion) {
                return new CasAttemptResult(CasOutcome.STALE_RACE, observedLastAcceptedStep);
            }
            var holder = steps.computeIfAbsent(authenticatorId, id -> new AtomicLong(-1));
            if (holder.get() != observedLastAcceptedStep) {
                return new CasAttemptResult(CasOutcome.STALE_RACE, holder.get());
            }
            if (matchedStep <= holder.get()) {
                return new CasAttemptResult(CasOutcome.STALE_RACE, holder.get());
            }
            holder.set(matchedStep);
            versions.put(authenticatorId, currentVersion + 1);
            return new CasAttemptResult(CasOutcome.ACCEPTED, matchedStep);
        }
    }
}