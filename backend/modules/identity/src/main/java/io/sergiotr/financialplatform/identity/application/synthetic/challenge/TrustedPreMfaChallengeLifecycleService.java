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
import io.sergiotr.financialplatform.identity.port.TrustedPrimaryAuthEvidencePort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

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
    private final TrustedPrimaryAuthEvidencePort primaryAuthEvidence;
    private final TrustedPreMfaChallengeStore store;

    public TrustedPreMfaChallengeLifecycleService(
            Clock clock,
            AccountSecuritySnapshotReadPort securityRead,
            TrustedPrimaryAuthEvidencePort primaryAuthEvidence) {
        this(clock, securityRead, primaryAuthEvidence, new InMemoryTrustedPreMfaChallengeStore());
    }

    TrustedPreMfaChallengeLifecycleService(
            Clock clock,
            AccountSecuritySnapshotReadPort securityRead,
            TrustedPrimaryAuthEvidencePort primaryAuthEvidence,
            TrustedPreMfaChallengeStore store) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.securityRead = Objects.requireNonNull(securityRead, "securityRead");
        this.primaryAuthEvidence = Objects.requireNonNull(primaryAuthEvidence, "primaryAuthEvidence");
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Issues an opaque challenge only after re-verifying Argon2id primary evidence and authoritative
     * account snapshot. Not exposed as a forgeable public issue(UserId, long, Stage) API.
     */
    public TrustedPreMfaChallengeId issueAfterVerifiedPrimary(VerifiedPrimaryForMfaIssuance issuance) {
        Objects.requireNonNull(issuance, "issuance");
        if (!primaryAuthEvidence.acceptsPrimaryAuth(issuance.subjectUserId(), issuance.primaryEvidenceRef())) {
            throw new IllegalStateException("primary factor not verified for challenge issuance");
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