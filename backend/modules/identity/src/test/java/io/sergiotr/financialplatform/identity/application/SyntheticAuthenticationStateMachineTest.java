package io.sergiotr.financialplatform.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticTrustedAuthEvidence;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionDenialReason;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.SecurityAssuranceStatus;
import io.sergiotr.financialplatform.identity.domain.SyntheticAuthSession;
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
        assertThat(session.stage()).isEqualTo(AuthenticationStage.ANONYMOUS);

        var primary = machine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertThat(primary.applied()).isTrue();
        session = machine.advance(session, primary.stage().orElseThrow());

        var mfaRequired = machine.transition(session, AuthenticationTransitionEvent.DIRECT_MFA_CHALLENGE, null);
        assertThat(mfaRequired.stage()).contains(AuthenticationStage.MFA_REQUIRED);
        session = machine.advance(session, mfaRequired.stage().orElseThrow());

        var authenticated = machine.transition(session, AuthenticationTransitionEvent.VERIFY_MFA, MFA);
        assertThat(authenticated.stage()).contains(AuthenticationStage.AUTHENTICATED);
        session = machine.advance(session, authenticated.stage().orElseThrow());

        assertThat(session.toTrustedActor().hasWorkspaceAssurance()).isTrue();
        assertThat(session.toTrustedActor().assurance()).isEqualTo(SecurityAssuranceStatus.AUTHENTICATED_FULL);
    }

    @Test
    void enrollmentPathRequiresEvidence() {
        var session = machine.advance(
                machine.begin(SyntheticWorkspaceFixtures.ALICE, 1L), AuthenticationStage.PRIMARY_AUTH_VERIFIED);
        session = machine.advance(
                session,
                machine.transition(session, AuthenticationTransitionEvent.DIRECT_MFA_ENROLLMENT, null)
                        .stage()
                        .orElseThrow());

        var badEvidence = machine.transition(session, AuthenticationTransitionEvent.COMPLETE_MFA_ENROLLMENT, "forged");
        assertThat(badEvidence.denialReason()).isEqualTo(AuthenticationTransitionDenialReason.INSUFFICIENT_EVIDENCE);

        var ok = machine.transition(session, AuthenticationTransitionEvent.COMPLETE_MFA_ENROLLMENT, MFA);
        assertThat(ok.stage()).contains(AuthenticationStage.AUTHENTICATED);
    }

    @Test
    void rejectsSkippingPrimaryAuth() {
        var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
        var skip = machine.transition(session, AuthenticationTransitionEvent.VERIFY_MFA, MFA);
        assertThat(skip.denialReason()).isEqualTo(AuthenticationTransitionDenialReason.INVALID_TRANSITION);
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
                false);
        var result = expiredMachine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertThat(result.denialReason()).isEqualTo(AuthenticationTransitionDenialReason.EXPIRED);
    }

    @Test
    void rejectsDisabledAccount() {
        var session = new SyntheticAuthSession(
                SyntheticWorkspaceFixtures.ALICE,
                AuthenticationStage.ANONYMOUS,
                2L,
                Instant.parse("2026-11-20T12:20:00Z"),
                true);
        var result = machine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, PRIMARY);
        assertThat(result.denialReason()).isEqualTo(AuthenticationTransitionDenialReason.DISABLED);
    }
}