package io.sergiotr.financialplatform.identity.application.synthetic.challenge;

import static org.junit.jupiter.api.Assertions.*;

import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthFixtureSupport;
import io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthenticationStateMachine;
import io.sergiotr.financialplatform.identity.application.synthetic.VerifiedPrimaryForMfaIssuance;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeConsumePort;
import java.time.Clock;
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
        validIssuance = mintIssuanceThroughStateMachine(
                USER, 3L, SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
    }

  private VerifiedPrimaryForMfaIssuance mintIssuanceThroughStateMachine(
            UserId user, long authRevision, String primaryPassword) {
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        var machine = new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa(), lifecycle);
        var primary = machine.transition(
                machine.begin(user, authRevision),
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                primaryPassword);
        var verified = primary.nextSession().orElseThrow();
        var proof = verified.verifiedPrimaryProof().orElseThrow();
        return new VerifiedPrimaryForMfaIssuance(
                user, authRevision, AuthenticationStage.MFA_REQUIRED, proof);
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
        var other = UserId.parse("10000000-0000-4000-8000-000000000099");
        securityStore.put(other, 1L, UserAccountSecurityStatus.ENABLED);
        var otherIssuance = mintIssuanceThroughStateMachine(
                other, 1L, SyntheticAuthFixtureSupport.BOB_PRIMARY_PASSWORD);
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
        securityStore.put(USER, 3L, UserAccountSecurityStatus.DISABLED);
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        assertEquals(
                TrustedPreMfaChallengeLifecycleService.ValidationOutcome.DISABLED_SUBJECT,
                lifecycle.validateForTotp(Optional.of(id)));
    }

    @Test
    void lockedSubjectDenied() {
        securityStore.put(USER, 3L, UserAccountSecurityStatus.LOCKED);
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        assertEquals(
                TrustedPreMfaChallengeLifecycleService.ValidationOutcome.LOCKED_SUBJECT,
                lifecycle.validateForTotp(Optional.of(id)));
    }

    @Test
    void expiredChallengeDenied() {
        var id = lifecycle.issueAfterVerifiedPrimary(validIssuance);
        var lateClock = Clock.fixed(clock.instant().plus(Duration.ofMinutes(20)), ZoneOffset.UTC);
        var lateLifecycle = new TrustedPreMfaChallengeLifecycleService(lateClock, securityStore);
        var validation = lateLifecycle.validateForTotp(Optional.of(id));
        assertEquals(TrustedPreMfaChallengeLifecycleService.ValidationOutcome.EXPIRED, validation);
    }

    @Test
    void wrongStageEnrollmentChallengeNotValidForTotp() {
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        var machine = new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa(), lifecycle);
        var verified = machine.transition(
                        machine.begin(USER, 3L),
                        AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                        SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD)
                .nextSession()
                .orElseThrow();
        var enrollment = machine.transition(
                verified, AuthenticationTransitionEvent.DIRECT_MFA_ENROLLMENT, null);
        var challengeId = enrollment.nextSession().orElseThrow().preMfaChallengeId().orElseThrow();
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