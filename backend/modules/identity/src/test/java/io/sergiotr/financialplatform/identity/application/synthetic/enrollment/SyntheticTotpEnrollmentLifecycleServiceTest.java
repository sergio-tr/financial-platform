package io.sergiotr.financialplatform.identity.application.synthetic.enrollment;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.FailClosedTotpEnrollmentRuntimeActivationPort;
import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthFixtureSupport;
import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import io.sergiotr.financialplatform.identity.application.synthetic.totp.Rfc6238TotpCryptographicVerifier;
import io.sergiotr.financialplatform.identity.domain.TotpSeed;
import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import io.sergiotr.financialplatform.identity.port.TotpEnrollmentRuntimeActivationPort;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyntheticTotpEnrollmentLifecycleServiceTest {

    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    private MutableClock clock;
    private InMemoryAccountSecurityAuthRevisionStore revisionStore;
    private TrustedPreMfaChallengeLifecycleService challengeLifecycle;
    private SyntheticAuthFixtureSupport.SyntheticPrimaryMfaAuth auth;
    private SyntheticTotpEnrollmentLifecycleService enrollment;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-21T10:00:00Z"), ZoneOffset.UTC);
        revisionStore = new InMemoryAccountSecurityAuthRevisionStore();
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 2L);
        challengeLifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisionStore);
        auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        enrollment = new SyntheticTotpEnrollmentLifecycleService(
                clock,
                revisionStore,
                challengeLifecycle,
                new FailClosedTotpEnrollmentRuntimeActivationPort(),
                () -> RFC_SECRET.clone());
    }

    @Test
    void happyPathPrimaryToConfirmedSyntheticWithoutAuthenticatedFull() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        assertEquals(TotpEnrollmentOutcome.Result.PENDING_STARTED, started.result());
        assertTrue(started.pendingId().isPresent());
        assertTrue(started.pendingAuthenticatorId().isPresent());

        String code = currentTotpCode();
        var confirmed = enrollment.confirm(
                started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, code);
        assertEquals(TotpEnrollmentOutcome.Result.CONFIRMED_SYNTHETIC, confirmed.result());
        assertNotEquals(TotpEnrollmentOutcome.Result.DENIED_INVALID_TOTP, confirmed.result());
    }

    @Test
    void pendingRecordToStringNeverContainsSeedMaterial() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        var recordText = started.toString();
        assertFalse(recordText.contains("12345678901234567890"));
        assertFalse(recordText.matches(".*\\d{6}.*secret.*"));
    }

    @Test
    void exactTenMinuteTtlDeniesAfterExpiry() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        clock.advance(SyntheticTotpEnrollmentLifecycleService.PENDING_TTL);
        var denied = enrollment.confirm(
                started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, currentTotpCode());
        assertEquals(TotpEnrollmentOutcome.Result.DENIED_EXPIRED, denied.result());
    }

    @Test
    void rejectsWrongStageChallenge() {
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 2L);
        var mfaChallenge = SyntheticAuthFixtureSupport.issueMfaChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var denied = enrollment.beginPending(mfaChallenge);
        assertEquals(TotpEnrollmentOutcome.Result.DENIED_WRONG_STAGE, denied.result());
    }

    @Test
    void rejectsDuplicatePendingForSameChallenge() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        assertEquals(TotpEnrollmentOutcome.Result.PENDING_STARTED, enrollment.beginPending(challengeId).result());
        assertEquals(
                TotpEnrollmentOutcome.Result.DENIED_DUPLICATE_PENDING, enrollment.beginPending(challengeId).result());
    }

    @Test
    void rejectsDoubleConfirm() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        var first = enrollment.confirm(
                started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, currentTotpCode());
        assertEquals(TotpEnrollmentOutcome.Result.CONFIRMED_SYNTHETIC, first.result());
        var second = enrollment.confirm(
                started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, currentTotpCode());
        assertEquals(TotpEnrollmentOutcome.Result.DENIED_NOT_PENDING, second.result());
    }

    @Test
    void rejectsCodeOutsideWindow() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        long outsideEpoch =
                clock.instant().getEpochSecond() + 2L * Rfc6238TotpCryptographicVerifier.TIME_STEP_SECONDS;
        var outsideVerifier = new Rfc6238TotpCryptographicVerifier(
                Clock.fixed(Instant.ofEpochSecond(outsideEpoch), ZoneOffset.UTC));
        String outside = String.format(
                "%06d",
                outsideVerifier
                        .codeAtEpochSecond(new TotpSeed(RFC_SECRET), outsideEpoch)
                        .orElseThrow());
        var denied = enrollment.confirm(
                started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, outside);
        assertEquals(TotpEnrollmentOutcome.Result.DENIED_INVALID_TOTP, denied.result());
    }

    @Test
    void rejectsForeignOwner() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        var denied = enrollment.confirm(
                started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.BOB, currentTotpCode());
        assertEquals(TotpEnrollmentOutcome.Result.DENIED_WRONG_OWNER, denied.result());
    }

    @Test
    void cancelRemovesPending() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        assertEquals(
                TotpEnrollmentOutcome.Result.CANCELLED,
                enrollment.cancel(started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE).result());
        assertEquals(
                TotpEnrollmentOutcome.Result.DENIED_NOT_PENDING,
                enrollment.confirm(
                                started.pendingId().orElseThrow(),
                                SyntheticWorkspaceFixtures.ALICE,
                                currentTotpCode())
                        .result());
    }

    @Test
    void staleAuthRevisionDeniedOnConfirm() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 99L);
        assertEquals(
                TotpEnrollmentOutcome.Result.DENIED_STALE_AUTH_REVISION,
                enrollment
                        .confirm(started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, currentTotpCode())
                        .result());
    }

    @Test
    void disabledSubjectDeniedOnConfirm() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = enrollment.beginPending(challengeId);
        revisionStore.setStatus(SyntheticWorkspaceFixtures.ALICE, UserAccountSecurityStatus.DISABLED);
        assertEquals(
                TotpEnrollmentOutcome.Result.DENIED_DISABLED,
                enrollment
                        .confirm(started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, currentTotpCode())
                        .result());
    }

    @Test
    void runtimeActivationRemainsFailClosed() {
        var tracking = new TrackingActivationPort();
        var svc = new SyntheticTotpEnrollmentLifecycleService(
                clock, revisionStore, challengeLifecycle, tracking, () -> RFC_SECRET.clone());
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock, auth, challengeLifecycle, SyntheticWorkspaceFixtures.ALICE, 2L);
        var started = svc.beginPending(challengeId);
        svc.confirm(started.pendingId().orElseThrow(), SyntheticWorkspaceFixtures.ALICE, currentTotpCode());
        assertEquals(TotpEnrollmentRuntimeActivationPort.ActivationOutcome.NOT_CONFIGURED, tracking.lastOutcome);
    }

    private String currentTotpCode() {
        var verifier = new Rfc6238TotpCryptographicVerifier(clock);
        return String.format(
                "%06d",
                verifier.codeAtEpochSecond(new TotpSeed(RFC_SECRET), clock.instant().getEpochSecond()).orElseThrow());
    }

    static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant start, ZoneId zone) {
            this.instant = Objects.requireNonNull(start);
            this.zone = Objects.requireNonNull(zone);
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    static final class TrackingActivationPort implements TotpEnrollmentRuntimeActivationPort {
        ActivationOutcome lastOutcome;

        @Override
        public ActivationOutcome activateForRuntimeLogin(
                io.sergiotr.financialplatform.identity.domain.UserId owner,
                io.sergiotr.financialplatform.identity.domain.AuthenticatorId authenticatorId) {
            lastOutcome = ActivationOutcome.NOT_CONFIGURED;
            return lastOutcome;
        }
    }
}