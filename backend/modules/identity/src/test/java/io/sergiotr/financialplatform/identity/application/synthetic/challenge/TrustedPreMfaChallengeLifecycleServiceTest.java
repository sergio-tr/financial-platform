package io.sergiotr.financialplatform.identity.application.synthetic.challenge;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
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
        lifecycle = new TrustedPreMfaChallengeLifecycleService(clock, securityStore);
        validIssuance = SyntheticAuthFixtureSupport.mintVerifiedPrimaryForMfaIssuance(
                clock,
                lifecycle,
                USER,
                3L,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD,
                AuthenticationStage.MFA_REQUIRED);
    }

    @Test
    void issueAfterPrimaryVerifiedThenValidateForTotp() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        assertNotNull(id);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.VALID, validation);
    }

    @Test
    void consumedPrimaryProofCannotIssueTwice() {
        lifecycle.issueAfterVerifiedPrimary(validIssuance);
        assertThrows(IllegalStateException.class, () -> lifecycle.issueAfterVerifiedPrimary(validIssuance));
    }

    @Test
    void arbitraryUserIdProofMismatchRejected() {
        securityStore.put(SyntheticWorkspaceFixtures.BOB, 1L, UserAccountSecurityStatus.ENABLED);
        var otherIssuance = SyntheticAuthFixtureSupport.mintVerifiedPrimaryForMfaIssuance(
                clock,
                lifecycle,
                SyntheticWorkspaceFixtures.BOB,
                1L,
                SyntheticAuthFixtureSupport.BOB_PRIMARY_PASSWORD,
                AuthenticationStage.MFA_REQUIRED);
        var swapped = new VerifiedPrimaryForMfaIssuance(
                USER, 3L, AuthenticationStage.MFA_REQUIRED, otherIssuance.primaryProof());
        assertThrows(IllegalArgumentException.class, () -> lifecycle.issueAfterVerifiedPrimary(swapped));
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
    void disabledSubjectDenied() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        securityStore.put(USER, 3L, UserAccountSecurityStatus.DISABLED);
        assertEquals(
                TrustedPreMfaChallengeLifecycleService.ValidationOutcome.DISABLED_SUBJECT,
                lifecycle.validateForTotp(Optional.of(id)));
    }

    @Test
    void lockedSubjectDenied() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        securityStore.put(USER, 3L, UserAccountSecurityStatus.LOCKED);
        assertEquals(
                TrustedPreMfaChallengeLifecycleService.ValidationOutcome.LOCKED_SUBJECT,
                lifecycle.validateForTotp(Optional.of(id)));
    }

    @Test
    void expiredChallengeDenied() {
        var store = new TrustedPreMfaChallengeLifecycleService.InMemoryTrustedPreMfaChallengeStore();
        var issuing = new TrustedPreMfaChallengeLifecycleService(clock, securityStore, store);
        var issuance = SyntheticAuthFixtureSupport.mintVerifiedPrimaryForMfaIssuance(
                clock,
                issuing,
                USER,
                3L,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD,
                AuthenticationStage.MFA_REQUIRED);
        var id = issuing.issueAfterVerifiedPrimary(issuance);
        var lateClock = Clock.fixed(clock.instant().plus(Duration.ofMinutes(20)), ZoneOffset.UTC);
        var lateLifecycle = new TrustedPreMfaChallengeLifecycleService(lateClock, securityStore, store);
        var validation = lateLifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.EXPIRED, validation);
    }

    @Test
    void wrongStageEnrollmentChallengeNotValidForTotp() {
        var challengeId = SyntheticAuthFixtureSupport.issueEnrollmentChallengeAfterPrimary(
                clock,
                lifecycle,
                USER,
                3L,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        assertEquals(
                TrustedPreMfaChallengeLifecycleService.ValidationOutcome.WRONG_STAGE,
                lifecycle.validateForTotp(Optional.of(challengeId)));
    }

    @Test
    void consumedChallengeNotActive() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        var consume = lifecycle.consumeIfActive(id, 3L);
        assertEquals(TrustedPreMfaChallengeConsumePort.ConsumeOutcome.CONSUMED, consume);
        var validation = lifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.NOT_ACTIVE, validation);
    }
}