package io.sergiotr.financialplatform.identity.application.synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticTrustedAuthEvidence;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionDenialReason;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyntheticAuthenticationStateMachineTest {

    private static final String PRIMARY = "syn-primary-alice";
    private static final String MFA = "syn-mfa-alice";

    private SyntheticAuthenticationStateMachine machine;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC);
        var evidence = new SyntheticTrustedAuthEvidence(
                Map.of(SyntheticWorkspaceFixtures.ALICE, PRIMARY),
                Map.of(SyntheticWorkspaceFixtures.ALICE, MFA));
        machine = new SyntheticAuthenticationStateMachine(clock, evidence, evidence);
    }

    @Test
    void fullPathThroughMfaChallenge() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        assertEquals(AuthenticationStage.ANONYMOUS, session.stage());

        var primary = machine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertTrue(primary.result().applied());
        session = primary.nextSession().orElseThrow();

        var mfaRequired = machine.transition(session, AuthenticationTransitionEvent.DIRECT_MFA_CHALLENGE, null);
        assertEquals(AuthenticationStage.MFA_REQUIRED, mfaRequired.result().stage().orElseThrow());
        session = mfaRequired.nextSession().orElseThrow();

        var authenticated = machine.transition(session, AuthenticationTransitionEvent.VERIFY_MFA, MFA);
        assertEquals(AuthenticationStage.AUTHENTICATED, authenticated.result().stage().orElseThrow());
        session = authenticated.nextSession().orElseThrow();

        assertTrue(session.toTrustedActor().hasWorkspaceAssurance());
        assertEquals(SecurityAssuranceStatus.AUTHENTICATED_FULL, session.toTrustedActor().assurance());
    }

    @Test
    void enrollmentPathRequiresPrimaryEvidenceThenEnrollmentEvidence() {
        var anonymous = machine.begin(SyntheticWorkspaceFixtures.ALICE, 1L);
        var primary = machine.transition(anonymous, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertTrue(primary.result().applied());
        var afterPrimary = primary.nextSession().orElseThrow();

        var enrollment =
                machine.transition(afterPrimary, AuthenticationTransitionEvent.DIRECT_MFA_ENROLLMENT, null);
        assertTrue(enrollment.result().applied());
        var enrolled = enrollment.nextSession().orElseThrow();

        var badEvidence =
                machine.transition(enrolled, AuthenticationTransitionEvent.COMPLETE_MFA_ENROLLMENT, "forged");
        assertEquals(AuthenticationTransitionDenialReason.INSUFFICIENT_EVIDENCE, badEvidence.result().denialReason());

        var ok = machine.transition(enrolled, AuthenticationTransitionEvent.COMPLETE_MFA_ENROLLMENT, MFA);
        assertEquals(AuthenticationStage.AUTHENTICATED, ok.result().stage().orElseThrow());
        assertTrue(ok.nextSession().orElseThrow().toTrustedActor().hasWorkspaceAssurance());
    }

    @Test
    void rejectsSkippingPrimaryAuth() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        var skip = machine.transition(session, AuthenticationTransitionEvent.VERIFY_MFA, MFA);
        assertEquals(AuthenticationTransitionDenialReason.INVALID_TRANSITION, skip.result().denialReason());
        assertTrue(skip.nextSession().isEmpty());
    }

    @Test
    void rejectsExpiredSession() {
        var expiredClock = Clock.fixed(Instant.parse("2026-11-20T14:00:00Z"), ZoneOffset.UTC);
        var evidence = new SyntheticTrustedAuthEvidence(
                Map.of(SyntheticWorkspaceFixtures.ALICE, PRIMARY), Map.of(SyntheticWorkspaceFixtures.ALICE, MFA));
        var expiredMachine = new SyntheticAuthenticationStateMachine(expiredClock, evidence, evidence);
        var session = new SyntheticAuthSession(
                SyntheticWorkspaceFixtures.ALICE,
                AuthenticationStage.ANONYMOUS,
                2L,
                Instant.parse("2026-11-20T12:05:00Z"),
                false,
                AuthSessionTrustMark.bootstrap());
        var result = expiredMachine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertEquals(AuthenticationTransitionDenialReason.EXPIRED, result.result().denialReason());
    }

    @Test
    void rejectsDisabledAccount() {
        var session = new SyntheticAuthSession(
                SyntheticWorkspaceFixtures.ALICE,
                AuthenticationStage.ANONYMOUS,
                2L,
                Instant.parse("2026-11-20T12:20:00Z"),
                true,
                AuthSessionTrustMark.bootstrap());
        var result = machine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertEquals(AuthenticationTransitionDenialReason.DISABLED, result.result().denialReason());
        assertFalse(result.result().applied());
    }

    @Test
    void forgedAuthenticatedStageWithoutTrustMarkCannotBecomeTrustedActor() {
        var forged = new SyntheticAuthSession(
                SyntheticWorkspaceFixtures.ALICE,
                AuthenticationStage.AUTHENTICATED,
                2L,
                Instant.parse("2026-11-20T12:20:00Z"),
                false,
                AuthSessionTrustMark.intermediate());
        assertThrows(IllegalStateException.class, forged::toTrustedActor);
    }
}