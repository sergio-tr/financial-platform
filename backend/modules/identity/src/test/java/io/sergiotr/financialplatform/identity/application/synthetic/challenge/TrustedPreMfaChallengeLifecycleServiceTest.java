package io.sergiotr.financialplatform.identity.application.synthetic.challenge;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeConsumePort;
import io.sergiotr.financialplatform.identity.domain.UserId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrustedPreMfaChallengeLifecycleServiceTest {

    private static final UserId USER = UserId.parse("10000000-0000-4000-8000-000000000001");

    private InMemoryAccountSecurityAuthRevisionStore revisions;
    private TrustedPreMfaChallengeLifecycleService lifecycle;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneOffset.UTC);
        revisions = new InMemoryAccountSecurityAuthRevisionStore();
        revisions.setRevision(USER, 3L);
        lifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisions);
    }

    @Test
    void issueAfterPrimaryVerifiedThenValidateForTotp() {
        var id = lifecycle.issue(USER, 3L, AuthenticationStage.MFA_REQUIRED);
        assertNotNull(id);
        var validation = lifecycle.validateForTotp(Optional.of(id), false);
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.VALID, validation);
    }

    @Test
    void forgedChallengeIdNotFound() {
        var validation = lifecycle.validateForTotp(
                Optional.of(io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId.generate()),
                false);
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.NOT_FOUND, validation);
    }

    @Test
    void staleAuthRevisionDenied() {
        var id = lifecycle.issue(USER, 3L, AuthenticationStage.MFA_REQUIRED);
        revisions.setRevision(USER, 4L);
        var validation = lifecycle.validateForTotp(Optional.of(id), false);
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.STALE_AUTH_REVISION, validation);
    }

    @Test
    void expiredChallengeDenied() {
        var store = new TrustedPreMfaChallengeLifecycleService.InMemoryTrustedPreMfaChallengeStore();
        var issuing =
                new TrustedPreMfaChallengeLifecycleService(clock, revisions, store);
        var id = issuing.issue(USER, 3L, AuthenticationStage.MFA_REQUIRED);
        var lateClock = Clock.fixed(clock.instant().plus(Duration.ofMinutes(16)), ZoneOffset.UTC);
        var lateLifecycle = new TrustedPreMfaChallengeLifecycleService(lateClock, revisions, store);
        var validation = lateLifecycle.validateForTotp(Optional.of(id), false);
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.EXPIRED, validation);
    }

    @Test
    void enrollmentStageWrongForTotpValidation() {
        var id = lifecycle.issue(USER, 3L, AuthenticationStage.MFA_ENROLLMENT_REQUIRED);
        var validation = lifecycle.validateForTotp(Optional.of(id), false);
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.WRONG_STAGE, validation);
    }

    @Test
    void consumedChallengeNotActive() {
        var id = lifecycle.issue(USER, 3L, AuthenticationStage.MFA_REQUIRED);
        assertEquals(
                TrustedPreMfaChallengeConsumePort.ConsumeOutcome.CONSUMED, lifecycle.consumeIfActive(id, 3L));
        var validation = lifecycle.validateForTotp(Optional.of(id), false);
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.NOT_ACTIVE, validation);
    }
}