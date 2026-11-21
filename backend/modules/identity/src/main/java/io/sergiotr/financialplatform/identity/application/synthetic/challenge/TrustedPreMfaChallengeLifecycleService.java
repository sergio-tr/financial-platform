package io.sergiotr.financialplatform.identity.application.synthetic.challenge;

import io.sergiotr.financialplatform.identity.domain.AuthenticationStage;
import io.sergiotr.financialplatform.identity.domain.TrustedPreMfaChallengeId;
import io.sergiotr.financialplatform.identity.domain.UserId;
import io.sergiotr.financialplatform.identity.port.AccountSecurityAuthRevisionReadPort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeConsumePort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeIssuePort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeLookupPort;
import io.sergiotr.financialplatform.identity.port.TrustedPreMfaChallengeSnapshot;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Server-owned pre-MFA challenge issuance, validation, and consume (synthetic DEV/TEST). */
public final class TrustedPreMfaChallengeLifecycleService
        implements TrustedPreMfaChallengeIssuePort, TrustedPreMfaChallengeLookupPort, TrustedPreMfaChallengeConsumePort {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    public enum ValidationOutcome {
        VALID,
        MISSING,
        NOT_FOUND,
        NOT_ACTIVE,
        EXPIRED,
        WRONG_STAGE,
        STALE_AUTH_REVISION,
        DISABLED_SUBJECT
    }

    private final Clock clock;
    private final AccountSecurityAuthRevisionReadPort authRevisionRead;
    private final TrustedPreMfaChallengeStore store;

    public TrustedPreMfaChallengeLifecycleService(Clock clock, AccountSecurityAuthRevisionReadPort authRevisionRead) {
        this(clock, authRevisionRead, new InMemoryTrustedPreMfaChallengeStore());
    }

    TrustedPreMfaChallengeLifecycleService(
            Clock clock, AccountSecurityAuthRevisionReadPort authRevisionRead, TrustedPreMfaChallengeStore store) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.authRevisionRead = Objects.requireNonNull(authRevisionRead, "authRevisionRead");
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public TrustedPreMfaChallengeId issue(
            UserId subjectUserId, long authRevision, AuthenticationStage stageAfterPrimaryVerified) {
        Objects.requireNonNull(subjectUserId, "subjectUserId");
        Objects.requireNonNull(stageAfterPrimaryVerified, "stageAfterPrimaryVerified");
        if (stageAfterPrimaryVerified != AuthenticationStage.MFA_REQUIRED
                && stageAfterPrimaryVerified != AuthenticationStage.MFA_ENROLLMENT_REQUIRED) {
            throw new IllegalArgumentException("challenge only after verified primary for MFA stages");
        }
        var authoritative = authRevisionRead.currentAuthRevision(subjectUserId);
        if (authoritative.isEmpty() || authoritative.get() != authRevision) {
            throw new IllegalStateException("authRevision not authoritative for issue");
        }
        Instant issuedAt = clock.instant();
        var id = TrustedPreMfaChallengeId.generate();
        var snapshot = new TrustedPreMfaChallengeSnapshot(
                id,
                subjectUserId,
                authRevision,
                stageAfterPrimaryVerified,
                issuedAt,
                issuedAt.plus(DEFAULT_TTL),
                TrustedPreMfaChallengeSnapshot.LifecycleStatus.ACTIVE);
        store.put(snapshot);
        return id;
    }

    @Override
    public Optional<TrustedPreMfaChallengeSnapshot> find(TrustedPreMfaChallengeId challengeId) {
        return store.find(challengeId);
    }

    public ValidationOutcome validateForTotp(Optional<TrustedPreMfaChallengeId> challengeId, boolean subjectDisabled) {
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
        if (subjectDisabled) {
            return ValidationOutcome.DISABLED_SUBJECT;
        }
        var authoritative = authRevisionRead.currentAuthRevision(snapshot.subjectUserId());
        if (authoritative.isEmpty() || authoritative.get() != snapshot.boundAuthRevision()) {
            return ValidationOutcome.STALE_AUTH_REVISION;
        }
        return ValidationOutcome.VALID;
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