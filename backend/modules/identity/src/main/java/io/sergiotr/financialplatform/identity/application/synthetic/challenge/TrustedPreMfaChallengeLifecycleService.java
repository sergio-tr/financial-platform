package io.sergiotr.financialplatform.identity.application.synthetic.challenge;

import io.sergiotr.financialplatform.identity.application.synthetic.VerifiedPrimaryForMfaIssuance;
import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.UserAccountSecurityStatus;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.AccountSecuritySnapshotReadPort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeConsumePort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeLookupPort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeSnapshot;
import io.sergiotr.financialplatform.identity.application.synthetic.VerifiedPrimaryAuthProof;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-owned pre-MFA challenge issuance, validation, and consume (synthetic DEV/TEST). */
public final class TrustedPreMfaChallengeLifecycleService
        implements TrustedPreMfaChallengeLookupPort, TrustedPreMfaChallengeConsumePort {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    public enum ValidationOutcome {
        VALID,
        MISSING,
        NOT_FOUND,
        NOT_ACTIVE,
        EXPIRED,
        WRONG_STAGE,
        STALE_AUTH_REVISION,
        DISABLED_SUBJECT,
        LOCKED_SUBJECT,
        REVOKED_SUBJECT,
        SUBJECT_UNKNOWN
    }

    private final Clock clock;
    private final AccountSecuritySnapshotReadPort securityRead;
    private final TrustedPreMfaChallengeStore store;
    private final ConcurrentHashMap<UUID, VerifiedPrimaryAuthProof> registeredPrimaryProofs = new ConcurrentHashMap<>();

    public TrustedPreMfaChallengeLifecycleService(Clock clock, AccountSecuritySnapshotReadPort securityRead) {
        this(clock, securityRead, new InMemoryTrustedPreMfaChallengeStore());
    }

    TrustedPreMfaChallengeLifecycleService(
            Clock clock, AccountSecuritySnapshotReadPort securityRead, TrustedPreMfaChallengeStore store) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.securityRead = Objects.requireNonNull(securityRead, "securityRead");
        this.store = Objects.requireNonNull(store, "store");
    }

    /** Registers a proof minted by the synthetic state machine after trusted primary verification. */
    public void registerVerifiedPrimaryProof(VerifiedPrimaryAuthProof proof) {
        Objects.requireNonNull(proof, "proof");
        registeredPrimaryProofs.put(proof.proofId(), proof);
    }

    /**
     * Issues an opaque challenge only after validating a server-minted primary proof and authoritative
     * account snapshot. Does not accept client credential material or re-hash passwords.
     */
    public TrustedPreMfaChallengeId issueAfterVerifiedPrimary(VerifiedPrimaryForMfaIssuance issuance) {
        Objects.requireNonNull(issuance, "issuance");
        var proof = issuance.primaryProof();
        var registered = registeredPrimaryProofs.remove(proof.proofId());
        if (registered == null || !registered.equals(proof)) {
            throw new IllegalStateException("primary proof not registered or already consumed");
        }
        if (!proof.isActiveAt(clock.instant())) {
            throw new IllegalStateException("primary proof expired");
        }
        var snapshot = securityRead.currentSnapshot(issuance.subjectUserId());
        if (snapshot.isEmpty()) {
            throw new IllegalStateException("subject security snapshot unknown");
        }
        if (!snapshot.get().status().permitsMfaChallenge()) {
            throw new IllegalStateException("subject not permitted for challenge issuance");
        }
        if (snapshot.get().authRevision() != issuance.authRevision()) {
            throw new IllegalStateException("authRevision not authoritative for issue");
        }
        Instant issuedAt = clock.instant();
        var id = TrustedPreMfaChallengeId.generate();
        var challenge = new TrustedPreMfaChallengeSnapshot(
                id,
                issuance.subjectUserId(),
                issuance.authRevision(),
                issuance.targetMfaStage(),
                issuedAt,
                issuedAt.plus(DEFAULT_TTL),
                TrustedPreMfaChallengeSnapshot.LifecycleStatus.ACTIVE);
        store.put(challenge);
        return id;
    }

    @Override
    public Optional<TrustedPreMfaChallengeSnapshot> find(TrustedPreMfaChallengeId challengeId) {
        return store.find(challengeId);
    }

    public ValidationOutcome validateForTotp(Optional<TrustedPreMfaChallengeId> challengeId) {
        if (challengeId.isEmpty()) {
            return ValidationOutcome.MISSING;
        }
        var snapshotOpt = store.find(challengeId.get());
        if (snapshotOpt.isEmpty()) {
            return ValidationOutcome.NOT_FOUND;
        }
        var snapshot = snapshotOpt.get();
        if (snapshot.status() != TrustedPreMfaChallengeSnapshot.LifecycleStatus.ACTIVE) {
            return ValidationOutcome.NOT_ACTIVE;
        }
        if (!clock.instant().isBefore(snapshot.expiresAt())) {
            return ValidationOutcome.EXPIRED;
        }
        if (snapshot.boundStage() != AuthenticationStage.MFA_REQUIRED) {
            return ValidationOutcome.WRONG_STAGE;
        }
        return validateAuthoritativeSnapshot(snapshot);
    }

    /** Validates an active trusted challenge bound to {@link AuthenticationStage#MFA_ENROLLMENT_REQUIRED}. */
    public ValidationOutcome validateForEnrollment(TrustedPreMfaChallengeId challengeId) {
        Objects.requireNonNull(challengeId, "challengeId");
        var snapshotOpt = store.find(challengeId);
        if (snapshotOpt.isEmpty()) {
            return ValidationOutcome.NOT_FOUND;
        }
        var snapshot = snapshotOpt.get();
        if (snapshot.status() != TrustedPreMfaChallengeSnapshot.LifecycleStatus.ACTIVE) {
            return ValidationOutcome.NOT_ACTIVE;
        }
        if (!clock.instant().isBefore(snapshot.expiresAt())) {
            return ValidationOutcome.EXPIRED;
        }
        if (snapshot.boundStage() != AuthenticationStage.MFA_ENROLLMENT_REQUIRED) {
            return ValidationOutcome.WRONG_STAGE;
        }
        return validateAuthoritativeSnapshot(snapshot);
    }

    private ValidationOutcome validateAuthoritativeSnapshot(TrustedPreMfaChallengeSnapshot snapshot) {
        var authoritative = securityRead.currentSnapshot(snapshot.subjectUserId());
        if (authoritative.isEmpty()) {
            return ValidationOutcome.SUBJECT_UNKNOWN;
        }
        var statusOutcome = mapAccountStatus(authoritative.get().status());
        if (statusOutcome != null) {
            return statusOutcome;
        }
        if (authoritative.get().authRevision() != snapshot.boundAuthRevision()) {
            return ValidationOutcome.STALE_AUTH_REVISION;
        }
        return ValidationOutcome.VALID;
    }

    private static ValidationOutcome mapAccountStatus(UserAccountSecurityStatus status) {
        return switch (status) {
            case ENABLED -> null;
            case DISABLED -> ValidationOutcome.DISABLED_SUBJECT;
            case LOCKED -> ValidationOutcome.LOCKED_SUBJECT;
            case REVOKED -> ValidationOutcome.REVOKED_SUBJECT;
        };
    }

    @Override
    public ConsumeOutcome consumeIfActive(TrustedPreMfaChallengeId challengeId, long expectedAuthRevision) {
        return store.consumeIfActive(challengeId, expectedAuthRevision);
    }

    interface TrustedPreMfaChallengeStore {
        void put(TrustedPreMfaChallengeSnapshot snapshot);

        Optional<TrustedPreMfaChallengeSnapshot> find(TrustedPreMfaChallengeId challengeId);

        ConsumeOutcome consumeIfActive(TrustedPreMfaChallengeId challengeId, long expectedAuthRevision);
    }

    static final class InMemoryTrustedPreMfaChallengeStore implements TrustedPreMfaChallengeStore {
        private final java.util.concurrent.ConcurrentHashMap<TrustedPreMfaChallengeId, TrustedPreMfaChallengeSnapshot> byId =
                new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public void put(TrustedPreMfaChallengeSnapshot snapshot) {
            byId.put(snapshot.challengeId(), snapshot);
        }

        @Override
        public Optional<TrustedPreMfaChallengeSnapshot> find(TrustedPreMfaChallengeId challengeId) {
            return Optional.ofNullable(byId.get(challengeId));
        }

        @Override
        public ConsumeOutcome consumeIfActive(TrustedPreMfaChallengeId challengeId, long expectedAuthRevision) {
            for (;;) {
                var current = byId.get(challengeId);
                if (current == null) {
                    return ConsumeOutcome.NOT_FOUND;
                }
                if (current.boundAuthRevision() != expectedAuthRevision) {
                    return ConsumeOutcome.STALE_REVISION;
                }
                if (current.status() != TrustedPreMfaChallengeSnapshot.LifecycleStatus.ACTIVE) {
                    return ConsumeOutcome.NOT_ACTIVE;
                }
                var consumed = new TrustedPreMfaChallengeSnapshot(
                        current.challengeId(),
                        current.subjectUserId(),
                        current.boundAuthRevision(),
                        current.boundStage(),
                        current.issuedAt(),
                        current.expiresAt(),
                        TrustedPreMfaChallengeSnapshot.LifecycleStatus.CONSUMED);
                if (byId.replace(challengeId, current, consumed)) {
                    return ConsumeOutcome.CONSUMED;
                }
            }
        }
    }
}