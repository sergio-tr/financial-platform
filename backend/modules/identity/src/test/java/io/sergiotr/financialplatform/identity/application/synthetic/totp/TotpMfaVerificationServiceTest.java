package io.sergiotr.financialplatform.identity.application.synthetic.totp;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.FailClosedAuthenticatorStepCompareAndSetPort;
import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import io.sergiotr.financialplatform.identity.domain.AuthenticatorId;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TotpMfaVerificationServiceTest {

    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    private static final AuthenticatorId AUTH =
            AuthenticatorId.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
    private static final UserId OWNER = UserId.parse("10000000-0000-4000-8000-000000000001");
    private static final UserId FOREIGN = UserId.parse("10000000-0000-4000-8000-000000000099");

    private InMemoryAccountSecurityAuthRevisionStore revisions;
    private TrustedPreMfaChallengeLifecycleService lifecycle;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.ofEpochSecond(1_234_567_890L), ZoneOffset.UTC);
        revisions = new InMemoryAccountSecurityAuthRevisionStore();
        revisions.setRevision(OWNER, 3L);
        revisions.setRevision(FOREIGN, 3L);
        lifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisions);
    }

    private IssueAndContext issueChallenge(UserId owner, long authRevision) {
        var challengeId = lifecycle.issue(owner, authRevision, AuthenticationStage.MFA_REQUIRED);
        TrustedMfaChallengeContextPort challenge = () -> Optional.of(challengeId);
        return new IssueAndContext(challengeId, challenge);
    }

    private record IssueAndContext(
            io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId challengeId,
            TrustedMfaChallengeContextPort context) {}

    private TotpMfaVerificationService service(
            TrustedMfaChallengeContextPort challenge,
            TotpAuthenticatorBindingReadPort bindings,
            AuthenticatorStepCompareAndSetPort cas) {
        TotpSecretReadPort secrets = id -> Optional.of(new TotpSeed(RFC_SECRET));
        return new TotpMfaVerificationService(clock, challenge, lifecycle, secrets, bindings, cas);
    }

    private static TotpAuthenticatorBindingReadPort activeBinding(UserId owner, long lastAcceptedStep) {
        return id -> Optional.of(new TotpAuthenticatorBindingReadPort.Binding(
                owner, TotpAuthenticatorBindingReadPort.Status.ACTIVE, lastAcceptedStep, 0L));
    }

    @Test
    void failClosedWhenDurableCasNotConfigured() {
        var crypto = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format("%06d", crypto.codeAtEpochSecond(new TotpSeed(RFC_SECRET), 1_234_567_890L)
                .orElseThrow());
        var issued = issueChallenge(OWNER, 3L);
        var svc = service(issued.context(), activeBinding(OWNER, -1), new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.REPLAY_PERSISTENCE_NOT_CONFIGURED, svc.verify(AUTH, code));
    }

    @Test
    void fakeInMemoryCasAcceptsOnceNotProductionProof() {
        var crypto = new Rfc6238TotpCryptographicVerifier(clock);
        String code = String.format("%06d", crypto.codeAtEpochSecond(new TotpSeed(RFC_SECRET), 1_234_567_890L)
                .orElseThrow());
        var fakeCas = new UnitTestOnlyInMemoryStepCas(OWNER);
        var issued = issueChallenge(OWNER, 3L);
        var svc = service(issued.context(), activeBinding(OWNER, -1), fakeCas);
        assertEquals(TotpVerificationOutcome.ACCEPTED, svc.verify(AUTH, code));
        assertEquals(TotpVerificationOutcome.REPLAY_STEP_ALREADY_ACCEPTED, svc.verify(AUTH, code));
    }

    @Test
    void deniesInactiveAuthenticatorAndUnknownSecret() {
        var issued = issueChallenge(OWNER, 3L);
        TotpSecretReadPort noSecret = id -> Optional.empty();
        var serviceNoSecret = new TotpMfaVerificationService(
                clock,
                issued.context(),
                lifecycle,
                noSecret,
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.SECRET_UNAVAILABLE, serviceNoSecret.verify(AUTH, "005924"));

        TotpAuthenticatorBindingReadPort revoked =
                id -> Optional.of(new TotpAuthenticatorBindingReadPort.Binding(
                        OWNER, TotpAuthenticatorBindingReadPort.Status.REVOKED, -1, 0));
        var serviceInactive = service(issued.context(), revoked, new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_INACTIVE, serviceInactive.verify(AUTH, "005924"));
    }

    @Test
    void deniesMissingTrustedChallengeAndOwnerMismatch() {
        var noChallenge = new TotpMfaVerificationService(
                clock,
                Optional::empty,
                lifecycle,
                id -> Optional.of(new TotpSeed(RFC_SECRET)),
                activeBinding(OWNER, -1),
                new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.TRUSTED_CHALLENGE_MISSING, noChallenge.verify(AUTH, "005924"));

        var foreignIssued = issueChallenge(FOREIGN, 3L);
        var foreignActor = service(
                foreignIssued.context(), activeBinding(OWNER, -1), new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_OWNER_MISMATCH, foreignActor.verify(AUTH, "005924"));
    }

    @Test
    void deniesStaleAuthRevisionBeforeTotp() {
        var issued = issueChallenge(OWNER, 3L);
        revisions.setRevision(OWNER, 4L);
        var svc = service(issued.context(), activeBinding(OWNER, -1), new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.TRUSTED_CHALLENGE_STALE_AUTH_REVISION, svc.verify(AUTH, "005924"));
    }

    @Test
    void deniesUnknownAuthenticator() {
        var issued = issueChallenge(OWNER, 3L);
        TotpAuthenticatorBindingReadPort unknown = id -> Optional.empty();
        var svc = service(issued.context(), unknown, new FailClosedAuthenticatorStepCompareAndSetPort());
        assertEquals(TotpVerificationOutcome.AUTHENTICATOR_UNKNOWN, svc.verify(AUTH, "005924"));
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