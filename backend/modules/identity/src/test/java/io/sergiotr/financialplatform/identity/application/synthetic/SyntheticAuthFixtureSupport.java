package io.sergiotr.financialplatform.identity.application.synthetic;

import io.sergiotr.financialplatform.identity.adapter.crypto.Argon2idSyntheticPasswordHasher;
import io.sergiotr.financialplatform.identity.adapter.crypto.Argon2idTrustedPrimaryAuthEvidence;
import io.sergiotr.financialplatform.identity.adapter.memory.InMemorySyntheticPasswordCredentialStore;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticFixtureMfaEvidence;
import io.sergiotr.financialplatform.identity.adapter.memory.SyntheticWorkspaceFixtures;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticCommonPasswordBlocklist;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordPolicy;
import io.sergiotr.financialplatform.identity.application.synthetic.password.SyntheticPasswordService;
import io.sergiotr.financialplatform.identity.adapter.memory.InMemoryAccountSecurityAuthRevisionStore;
import io.sergiotr.financialplatform.identity.application.synthetic.challenge.TrustedPreMfaChallengeLifecycleService;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.AuthenticationTransitionEvent;
import io.sergiotr.financialplatform.identity.domain.SyntheticPasswordCredential;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.TrustedMfaEvidencePort;
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import java.time.Clock;
import java.util.Map;

/** Generates Argon2id hashes at runtime for synthetic auth tests (no committed password material). */
public final class SyntheticAuthFixtureSupport {

    public static final String ALICE_PRIMARY_PASSWORD = "synthetic-primary-alice-test-only";
    public static final String BOB_PRIMARY_PASSWORD = "synthetic-primary-bob-test-only";
    public static final String MFA_ALICE = "syn-mfa-alice";
    public static final String MFA_BOB = "syn-mfa-bob";

    private SyntheticAuthFixtureSupport() {}

    public static SyntheticPrimaryMfaAuth aliceAndBobPrimaryArgon2() {
        var hasher = new Argon2idSyntheticPasswordHasher();
        var passwordService = new SyntheticPasswordService(
                new SyntheticPasswordPolicy(new SyntheticCommonPasswordBlocklist().loadFromClasspath()), hasher);
        var store = new InMemorySyntheticPasswordCredentialStore();
        store.put(new SyntheticPasswordCredential(
                SyntheticWorkspaceFixtures.ALICE, hasher.encode(ALICE_PRIMARY_PASSWORD), false));
        store.put(new SyntheticPasswordCredential(
                SyntheticWorkspaceFixtures.BOB, hasher.encode(BOB_PRIMARY_PASSWORD), false));
        TrustedPrimaryAuthEvidencePort primary = new Argon2idTrustedPrimaryAuthEvidence(store, passwordService);
        TrustedMfaEvidencePort mfa = new SyntheticFixtureMfaEvidence(Map.of(
                SyntheticWorkspaceFixtures.ALICE, MFA_ALICE,
                SyntheticWorkspaceFixtures.BOB, MFA_BOB));
        return new SyntheticPrimaryMfaAuth(primary, mfa);
    }

    public static InMemorySyntheticPasswordCredentialStore disabledAliceStore(Argon2idSyntheticPasswordHasher hasher) {
        var store = new InMemorySyntheticPasswordCredentialStore();
        store.put(new SyntheticPasswordCredential(
                SyntheticWorkspaceFixtures.ALICE, hasher.encode(ALICE_PRIMARY_PASSWORD), true));
        return store;
    }

    public record SyntheticPrimaryMfaAuth(
            TrustedPrimaryAuthEvidencePort primary, TrustedMfaEvidencePort mfa) {}

    /**
     * Issues a pre-MFA challenge through verified primary transition + server-minted proof (no password
     * retention on the session).
     */
    public static TrustedPreMfaChallengeId issueMfaChallengeAfterPrimary(
            Clock clock,
            InMemoryAccountSecurityAuthRevisionStore securityStore,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision,
            String primaryPassword) {
        var auth = aliceAndBobPrimaryArgon2();
        return issueMfaChallengeAfterPrimary(
                clock, lifecycle, user, authRevision, primaryPassword, auth.primary(), auth.mfa());
    }

    public static TrustedPreMfaChallengeId issueMfaChallengeAfterPrimary(
            Clock clock,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision,
            String primaryPassword,
            TrustedPrimaryAuthEvidencePort primaryEvidence,
            TrustedMfaEvidencePort mfaEvidence) {
        var machine = new SyntheticAuthenticationStateMachine(clock, primaryEvidence, mfaEvidence, lifecycle);
        var session = machine.begin(user, authRevision);
        var primary =
                machine.transition(session, AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH, primaryPassword);
        if (!primary.result().applied()) {
            throw new IllegalStateException("primary transition not applied");
        }
        var verified = primary.nextSession().orElseThrow();
        var proof = verified.verifiedPrimaryProof().orElseThrow();
        return lifecycle.issueAfterVerifiedPrimary(
                new VerifiedPrimaryForMfaIssuance(user, authRevision, AuthenticationStage.MFA_REQUIRED, proof));
    }

    /** Builds issuance DTO with server-minted proof (for tests outside the synthetic package). */
    public static VerifiedPrimaryForMfaIssuance mintVerifiedPrimaryForMfaIssuance(
            Clock clock,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision,
            String primaryPassword,
            AuthenticationStage targetStage) {
        var auth = aliceAndBobPrimaryArgon2();
        return mintVerifiedPrimaryForMfaIssuance(
                clock, lifecycle, user, authRevision, primaryPassword, targetStage, auth.primary(), auth.mfa());
    }

    public static VerifiedPrimaryForMfaIssuance mintVerifiedPrimaryForMfaIssuance(
            Clock clock,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision,
            String primaryPassword,
            AuthenticationStage targetStage,
            TrustedPrimaryAuthEvidencePort primaryEvidence,
            TrustedMfaEvidencePort mfaEvidence) {
        var machine = new SyntheticAuthenticationStateMachine(clock, primaryEvidence, mfaEvidence, lifecycle);
        var primary = machine.transition(
                machine.begin(user, authRevision),
                AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                primaryPassword);
        if (!primary.result().applied()) {
            throw new IllegalStateException("primary transition not applied");
        }
        var proof = primary.nextSession().orElseThrow().verifiedPrimaryProof().orElseThrow();
        return new VerifiedPrimaryForMfaIssuance(user, authRevision, targetStage, proof);
    }

    public static TrustedPreMfaChallengeId issueEnrollmentChallengeAfterPrimary(
            Clock clock,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision,
            String primaryPassword) {
        var auth = aliceAndBobPrimaryArgon2();
        return issueEnrollmentChallengeAfterPrimary(clock, auth, lifecycle, user, authRevision, primaryPassword);
    }

    public static TrustedPreMfaChallengeId issueEnrollmentChallengeAfterPrimary(
            Clock clock,
            SyntheticPrimaryMfaAuth auth,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision) {
        String password =
                SyntheticWorkspaceFixtures.ALICE.equals(user) ? ALICE_PRIMARY_PASSWORD : BOB_PRIMARY_PASSWORD;
        return issueEnrollmentChallengeAfterPrimary(clock, auth, lifecycle, user, authRevision, password);
    }

    public static TrustedPreMfaChallengeId issueEnrollmentChallengeAfterPrimary(
            Clock clock,
            SyntheticPrimaryMfaAuth auth,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision,
            String primaryPassword) {
        var machine = new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa(), lifecycle);
        var verified = machine.transition(
                        machine.begin(user, authRevision),
                        AuthenticationTransitionEvent.VERIFY_PRIMARY_AUTH,
                        primaryPassword)
                .nextSession()
                .orElseThrow();
        var enrollment = machine.transition(
                verified, AuthenticationTransitionEvent.DIRECT_MFA_ENROLLMENT, null);
        return enrollment.nextSession().orElseThrow().preMfaChallengeId().orElseThrow();
    }

    public static SyntheticAuthenticationStateMachine stateMachineWithChallenge(
            Clock clock, SyntheticPrimaryMfaAuth auth, TrustedPreMfaChallengeLifecycleService lifecycle) {
        return new SyntheticAuthenticationStateMachine(clock, auth.primary(), auth.mfa(), lifecycle);
    }

    public static TrustedPreMfaChallengeId issueMfaChallengeAfterPrimary(
            Clock clock,
            SyntheticPrimaryMfaAuth auth,
            TrustedPreMfaChallengeLifecycleService lifecycle,
            UserId user,
            long authRevision) {
        String password =
                SyntheticWorkspaceFixtures.ALICE.equals(user) ? ALICE_PRIMARY_PASSWORD : BOB_PRIMARY_PASSWORD;
        return issueMfaChallengeAfterPrimary(clock, lifecycle, user, authRevision, password, auth.primary(), auth.mfa());
    }
}