package io.sergiotr.financialplatform.identity.application.synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionDenialReason;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyntheticAuthenticationStateMachineTest {

    private SyntheticAuthenticationStateMachine machine;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC);
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        machine = new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa());
    }

    @Test
    void argon2PrimaryVerifiedDoesNotGrantAuthenticatedFull() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        var primary = machine.transition(
                session,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        assertTrue(primary.result().applied());
        var afterPrimary = primary.nextSession().orElseThrow();
        assertEquals(AuthenticationStage.PRIMARY_AUTH_VERIFIED, afterPrimary.stage());
        assertThrows(IllegalStateException.class, afterPrimary::toTrustedActor);
    }

    @Test
    void primaryVerifiedToMfaRequiredIssuesOpaqueChallenge() {
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        var revisionStore = new InMemoryAccountSecurityAuthRevisionStore();
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 2L);
        var lifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisionStore);
        var machine = new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa(), lifecycle);
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        var primary = machine.transition(
                session,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        var mfaRequired = machine.transition(
                primary.nextSession().orElseThrow(),
                AuthenticationTransitionEvent.DIRECT_MFA_CHALLENGE,
                null);
        var issued = mfaRequired.nextSession().orElseThrow();
        assertTrue(issued.preMfaChallengeId().isPresent());
        assertEquals(AuthenticationStage.MFA_REQUIRED, issued.stage());
    }

    @Test
    void sessionToStringNeverContainsPrimaryPasswordMaterial() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        var primary = machine.transition(
                session,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        var afterPrimary = primary.nextSession().orElseThrow();
        String sessionText = afterPrimary.toString();
        assertFalse(sessionText.contains(SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD));
        assertTrue(afterPrimary.verifiedPrimaryProof().isPresent());
        assertFalse(
                afterPrimary.verifiedPrimaryProof().orElseThrow().toString().contains(SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD));
    }

    @Test
    void fullPathThroughMfaChallenge() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        assertEquals(AuthenticationStage.ANONYMOUS, session.stage());

        var primary = machine.transition(
                session,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        assertTrue(primary.result().applied());
        session = primary.nextSession().orElseThrow();

        var mfaRequired = machine.transition(session, AuthenticationTransitionEvent.DIRECT_MFA_CHALLENGE, null);
        assertEquals(AuthenticationStage.MFA_REQUIRED, mfaRequired.result().stage().orElseThrow());
        session = mfaRequired.nextSession().orElseThrow();

        var authenticated =
                machine.transition(session, AuthenticationTransitionEvent.VERIFY_MFA, SyntheticAuthFixtureSupport.MFA_ALICE);
        assertEquals(AuthenticationStage.AUTHENTICATED, authenticated.result().stage().orElseThrow());
        session = authenticated.nextSession().orElseThrow();

        assertTrue(session.toTrustedActor().hasWorkspaceAssurance());
        assertEquals(SecurityAssuranceStatus.AUTHENTICATED_FULL, session.toTrustedActor().assurance());
    }

    @Test
    void enrollmentPathRequiresPrimaryThenTrustedChallengeOnly() {
        var revisionStore = new InMemoryAccountSecurityAuthRevisionStore();
        revisionStore.setRevision(SyntheticWorkspaceFixtures.ALICE, 1L);
        var lifecycle = new TrustedPreMfaChallengeLifecycleService(clock, revisionStore);
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        var enrolledMachine =
                new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa(), lifecycle);

        var anonymous = enrolledMachine.begin(SyntheticWorkspaceFixtures.ALICE, 1L);
        var primary = enrolledMachine.transition(
                anonymous,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
        assertTrue(primary.result().applied());
        var afterPrimary = primary.nextSession().orElseThrow();

        var enrollment =
                enrolledMachine.transition(afterPrimary, AuthenticationTransitionEvent.DIRECT_MFA_ENROLLMENT, null);
        assertTrue(enrollment.result().applied());
        var enrolled = enrollment.nextSession().orElseThrow();
        assertEquals(AuthenticationStage.MFA_ENROLLMENT_REQUIRED, enrolled.stage());
        assertTrue(enrolled.preMfaChallengeId().isPresent());
        assertThrows(IllegalStateException.class, enrolled::toTrustedActor);

        var legacyComplete = enrolledMachine.transition(
                enrolled, AuthenticationTransitionEvent.COMPLETE_MFA_ENROLLMENT, SyntheticAuthFixtureSupport.MFA_ALICE);
        assertEquals(AuthenticationTransitionDenialReason.INVALID_TRANSITION, legacyComplete.result().denialReason());
    }

    @Test
    void rejectsSkippingPrimaryAuth() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        var skip = machine.transition(
                session, AuthenticationTransitionEvent.VERIFY_MFA, SyntheticAuthFixtureSupport.MFA_ALICE);
        assertEquals(AuthenticationTransitionDenialReason.INVALID_TRANSITION, skip.result().denialReason());
        assertTrue(skip.nextSession().isEmpty());
    }

    @Test
    void rejectsExpiredSession() {
        var expiredClock = Clock.fixed(Instant.parse("2026-11-20T14:00:00Z"), ZoneOffset.UTC);
        var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
        var expiredMachine = new SyntheticAuthenticationStateMachine(expiredClock, auth.primary(), auth.mfa());
        var session = new SyntheticAuthSession(
                SyntheticWorkspaceFixtures.ALICE,
                AuthenticationStage.ANONYMOUS,
                2L,
                Instant.parse("2026-11-20T12:05:00Z"),
                false,
                AuthSessionTrustMark.bootstrap(),
                Optional.empty(),
                Optional.empty());
        var result = expiredMachine.transition(
                session,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
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
                AuthSessionTrustMark.bootstrap(),
                Optional.empty(),
                Optional.empty());
        var result = machine.transition(
                session,
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
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
                AuthSessionTrustMark.intermediate(),
                Optional.empty(),
                Optional.empty());
        assertThrows(IllegalStateException.class, forged::toTrustedActor);
    }
}