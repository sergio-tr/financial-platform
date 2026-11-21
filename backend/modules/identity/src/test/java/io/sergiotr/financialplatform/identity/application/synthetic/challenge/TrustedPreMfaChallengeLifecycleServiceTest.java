package io.sergiotr.financialplatform.identity.application.synthetic.challenge;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthFixtureSupport;
import io.sergiotr.financialplatform.identity.application.synthetic.VerifiedPrimaryForMfaIssuance;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeConsumePort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrustedPreMfaChallengeLifecycleServiceTest {

    private static final UserId USER = UserId.parse("10000000-0000-4000-8000-000000000001");

    private InMemoryAccountSecurityAuthRevisionStore securityStore;
    private TrustedPreMfaChallengeLifecycleService lifecycle;
    private Clock clock;
    private VerifiedPrimaryForMfaIssuance validIssuance;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);
        securityStore = new InMemoryAccountSecurityAuthRevisionStore();
        securityStore.put(USER, 3L, UserAccountSecurityStatus.ENABLED);
        var primary = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2().primary();
        lifecycle = new TrustedPreMfaChallengeLifecycleService(clock, securityStore, primary);
        validIssuance = new VerifiedPrimaryForMfaIssuance(
                USER,
                3L,
                AuthenticationStage.MFA_REQUIRED,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
    }

    @Test
    void issueAfterPrimaryVerifiedThenValidateForTotp() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        assertNotNull(id);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.VALID, validation);
    }

    @Test
    void forgedIssueWithoutPrimaryEvidenceRejected() {
        var forged = new VerifiedPrimaryForMfaIssuance(
                USER, 3L, AuthenticationStage.MFA_REQUIRED, "not-a-real-password");
        assertThrows(IllegalStateException.class, () -> lifecycle.issueAfterVerifiedPrimary(forged));
    }

    @Test
    void arbitraryUserIdIssueRejected() {
        var other = UserId.parse("10000000-0000-4000-8000-000000000099");
        securityStore.put(other, 1L, UserAccountSecurityStatus.ENABLED);
        var forged = new VerifiedPrimaryForMfaIssuance(
                other,
                1L,
                AuthenticationStage.MFA_REQUIRED,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        assertThrows(IllegalStateException.class, () -> lifecycle.issueAfterVerifiedPrimary(forged));
    }

    @Test
    void forgedChallengeIdNotFound() {
        var validation = lifecycle.validateForTotp(
                Optional.of(io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId.generate()));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.NOT_FOUND, validation);
    }

    @Test
    void staleAuthRevisionDenied() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        securityStore.setRevision(USER, 4L);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.STALE_AUTH_REVISION, validation);
    }

    @Test
    void disabledSubjectDeniedBeforeTotp() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        securityStore.put(USER, 3L, UserAccountSecurityStatus.DISABLED);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.DISABLED_SUBJECT, validation);
    }

    @Test
    void lockedSubjectDeniedBeforeTotp() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        securityStore.put(USER, 3L, UserAccountSecurityStatus.LOCKED);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.LOCKED_SUBJECT, validation);
    }

    @Test
    void expiredChallengeDenied() {
        var store = new TrustedPreMfaChallengeLifecycleService.InMemoryTrustedPreMfaChallengeStore();
        var primary = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2().primary();
        var issuing = new TrustedPreMfaChallengeLifecycleService(clock, securityStore, primary, store);
        var id = issuing.issueAfterVerifiedPrimary(validIssuance);
        var lateClock = Clock.fixed(clock.instant().plus(Duration.ofMinutes(16)), ZoneOffset.UTC);
        var lateLifecycle = new TrustedPreMfaChallengeLifecycleService(lateClock, securityStore, primary, store);
        var validation = lateLifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.EXPIRED, validation);
    }

    @Test
    void enrollmentStageWrongForTotpValidation() {
        var enrollment = new VerifiedPrimaryForMfaIssuance(
                USER,
                3L,
                AuthenticationStage.MFA_ENROLLMENT_REQUIRED,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        var id = lifecycle.issueAfterVerifiedPrimary(enrollment);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.WRONG_STAGE, validation);
    }

    @Test
    void consumedChallengeNotActive() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        assertEquals(
                TrustedPreMfaChallengeConsumePort.ConsumeOutcome.CONSUMED, lifecycle.consumeIfActive(id, 3L));
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.NOT_ACTIVE, validation);
    }
}