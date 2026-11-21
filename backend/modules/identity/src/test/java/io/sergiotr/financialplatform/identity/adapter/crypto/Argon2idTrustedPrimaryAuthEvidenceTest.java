package io.sergiotr.financialplatform.identity.adapter.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthFixtureSupport;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticCommonPasswordBlocklist;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordPolicy;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordService;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.UserId;
import org.junit.jupiter.api.Test;

class Argon2idTrustedPrimaryAuthEvidenceTest {

  @Test
  void acceptsMatchingPrimaryPasswordForKnownUser() {
    var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
    assertTrue(auth.primary().acceptsPrimaryAuth(
        SyntheticWorkspaceFixtures.ALICE, SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD));
    assertFalse(auth.primary().acceptsPrimaryAuth(
        SyntheticWorkspaceFixtures.ALICE, "wrong-password-material"));
  }

  @Test
  void deniesUnknownForeignAndDisabledUsers() {
    var hasher = new Argon2idSyntheticPasswordHasher();
    var passwordService = new SyntheticPasswordService(
        new SyntheticPasswordPolicy(new SyntheticCommonPasswordBlocklist().loadFromClasspath()), hasher);
    var disabledStore = SyntheticAuthFixtureSupport.disabledAliceStore(hasher);
    var primary = new Argon2idTrustedPrimaryAuthEvidence(disabledStore, passwordService);
    assertFalse(primary.acceptsPrimaryAuth(
        SyntheticWorkspaceFixtures.ALICE, SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD));
    assertFalse(primary.acceptsPrimaryAuth(
        UserId.parse("10000000-0000-4000-8000-000000009999"),
        SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD));
  }

  @Test
  void passwordSuccessDoesNotSkipMfa() {
    var auth = SyntheticAuthFixtureSupport.aliceAndBobPrimaryArgon2();
    var clock = java.time.Clock.systemUTC();
    var machine = new io.sergiotr.financialplatform.identity.application.synthetic.SyntheticAuthenticationStateMachine(
        clock, auth.primary(), auth.mfa());
    var session = machine.begin(SyntheticWorkspaceFixtures.ALICE, 2L);
    var primary = machine.transition(
        session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, SyntheticAuthFixtureSupport.ALICE_PRIMARY_PASSWORD);
    assertTrue(primary.result().applied());
    assertEquals(
        AuthenticationStage.PRIMARY_AUTH_VERIFIED, primary.nextSession().orElseThrow().stage());
    assertFalse(primary.nextSession().orElseThrow().toTrustedActor().hasWorkspaceAssurance());
  }
}